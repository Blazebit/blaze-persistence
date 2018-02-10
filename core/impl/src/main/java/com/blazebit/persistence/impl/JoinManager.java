/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.impl;

import com.blazebit.lang.StringUtils;
import com.blazebit.lang.ValueRetriever;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.impl.builder.predicate.JoinOnBuilderImpl;
import com.blazebit.persistence.impl.builder.predicate.PredicateBuilderEndedListenerImpl;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.ListIndexExpression;
import com.blazebit.persistence.impl.expression.MapEntryExpression;
import com.blazebit.persistence.impl.expression.MapKeyExpression;
import com.blazebit.persistence.impl.expression.MapValueExpression;
import com.blazebit.persistence.impl.expression.NumericLiteral;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PathReference;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.QualifiedExpression;
import com.blazebit.persistence.impl.expression.SimplePathReference;
import com.blazebit.persistence.impl.expression.StringLiteral;
import com.blazebit.persistence.impl.expression.TreatExpression;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import com.blazebit.persistence.impl.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.impl.function.entity.ValuesEntity;
import com.blazebit.persistence.impl.predicate.CompoundPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import com.blazebit.persistence.impl.transform.ExpressionModifierVisitor;
import com.blazebit.persistence.impl.util.JpaMetamodelUtils;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.ValuesStrategy;

import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.0
 */
public class JoinManager extends AbstractManager<ExpressionModifier> {

    private static final Logger LOG = Logger.getLogger(JoinManager.class.getName());

    // we might have multiple nodes that depend on the same unresolved alias,
    // hence we need a List of NodeInfos.
    // e.g. SELECT a.X, a.Y FROM A a
    // a is unresolved for both X and Y
    private final List<JoinNode> rootNodes = new ArrayList<JoinNode>(1);
    private final Set<JoinNode> entityFunctionNodes = new LinkedHashSet<JoinNode>();
    // root entity class
    private final String joinRestrictionKeyword;
    private final MainQuery mainQuery;
    private final AliasManager aliasManager;
    private final EntityMetamodelImpl metamodel; // needed for model-aware joins
    private final JoinManager parent;
    private final JoinOnBuilderEndedListener joinOnBuilderListener;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;

    // helper collections for join rendering
    private final Set<JoinNode> collectionJoinNodes = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());
    private final Set<JoinNode> renderedJoins = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());
    private final Set<JoinNode> markedJoinNodes = Collections.newSetFromMap(new IdentityHashMap<JoinNode, Boolean>());

    JoinManager(MainQuery mainQuery, ResolvingQueryGenerator queryGenerator, AliasManager aliasManager, JoinManager parent, ExpressionFactory expressionFactory) {
        super(queryGenerator, mainQuery.parameterManager, null);
        this.mainQuery = mainQuery;
        this.aliasManager = aliasManager;
        this.metamodel = mainQuery.metamodel;
        this.parent = parent;
        this.joinRestrictionKeyword = " " + mainQuery.jpaProvider.getOnClause() + " ";
        this.joinOnBuilderListener = new JoinOnBuilderEndedListener();
        this.subqueryInitFactory = new SubqueryInitiatorFactory(mainQuery, aliasManager, this);
        this.expressionFactory = expressionFactory;
    }

    void applyFrom(JoinManager joinManager) {
        for (JoinNode node : joinManager.rootNodes) {
            JoinNode rootNode = applyFrom(node);

            if (node.getValueQuery() != null) {
                // TODO: At the moment the value type is without meaning
                ParameterManager.ParameterImpl<?> param = joinManager.parameterManager.getParameter(node.getAlias());
                ValuesParameterBinder binder = ((ParameterManager.ValuesParameterWrapper) param.getParameterValue()).getBinder();

                parameterManager.registerValuesParameter(rootNode.getAlias(), null, binder.getParameterNames(), binder.getPathExpressions());
                entityFunctionNodes.add(rootNode);
            }
        }
    }

    private JoinNode applyFrom(JoinNode node) {
        String rootAlias = node.getAlias();
        boolean implicit = node.getAliasInfo().isImplicit();

        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, implicit, true, aliasManager);
        JoinNode rootNode;

        if (node.getCorrelationParent() != null) {
            throw new UnsupportedOperationException("Cloning subqueries not yet implemented!");
        } else {
            rootNode = node.cloneRootNode(rootAliasInfo);
        }

        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);

        for (JoinTreeNode treeNode : node.getNodes().values()) {
            applyFrom(rootNode, treeNode);
        }

        if (!node.getTreatedJoinNodes().isEmpty()) {
            throw new UnsupportedOperationException("Cloning joins with treat joins is not yet implemented!");
        }

        return rootNode;
    }

    private void applyFrom(JoinNode parent, JoinTreeNode treeNode) {
        JoinTreeNode newTreeNode = parent.getOrCreateTreeNode(treeNode.getRelationName(), treeNode.getAttribute());
        for (Map.Entry<String, JoinNode> nodeEntry : treeNode.getJoinNodes().entrySet()) {
            JoinNode newNode = applyFrom(parent, newTreeNode, nodeEntry.getKey(), nodeEntry.getValue());
            newTreeNode.addJoinNode(newNode, nodeEntry.getValue() == treeNode.getDefaultNode());
        }
    }

    private JoinNode applyFrom(JoinNode parent, JoinTreeNode treeNode, String alias, JoinNode oldNode) {
        boolean implicit = oldNode.getAliasInfo().isImplicit();
        String currentJoinPath = parent.getAliasInfo().getAbsolutePath() + "." + treeNode.getRelationName();
        JoinAliasInfo newAliasInfo = new JoinAliasInfo(alias, currentJoinPath, implicit, false, aliasManager);
        aliasManager.registerAliasInfo(newAliasInfo);
        JoinNode node = oldNode.cloneJoinNode(parent, treeNode, newAliasInfo);
        newAliasInfo.setJoinNode(node);

        if (oldNode.getOnPredicate() != null) {
            node.setOnPredicate(subqueryInitFactory.reattachSubqueries(oldNode.getOnPredicate().clone(true)));
        }

        for (JoinTreeNode oldTreeNode : oldNode.getNodes().values()) {
            applyFrom(node, oldTreeNode);
        }

        if (!oldNode.getTreatedJoinNodes().isEmpty()) {
            throw new UnsupportedOperationException("Cloning joins with treat joins is not yet implemented!");
        }

        return node;
    }

    @Override
    public ClauseType getClauseType() {
        return ClauseType.JOIN;
    }

    Set<JoinNode> getKeyRestrictedLeftJoins() {
        if (!mainQuery.jpaProvider.needsJoinSubqueryRewrite()) {
            return Collections.emptySet();
        }

        Set<JoinNode> keyRestrictedLeftJoins = new HashSet<JoinNode>();
        acceptVisitor(new KeyRestrictedLeftJoinCollectingVisitor(mainQuery.jpaProvider, keyRestrictedLeftJoins));
        return keyRestrictedLeftJoins;
    }

    static class KeyRestrictedLeftJoinCollectingVisitor extends VisitorAdapter implements JoinNodeVisitor {

        final JpaProvider jpaProvider;
        final Set<JoinNode> keyRestrictedLeftJoins;

        public KeyRestrictedLeftJoinCollectingVisitor(JpaProvider jpaProvider, Set<JoinNode> keyRestrictedLeftJoins) {
            this.jpaProvider = jpaProvider;
            this.keyRestrictedLeftJoins = keyRestrictedLeftJoins;
        }

        @Override
        public void visit(JoinNode node) {
            if (node.getJoinType() == JoinType.LEFT && node.getOnPredicate() != null) {
                node.getOnPredicate().accept(this);
            }
        }

        @Override
        public void visit(MapKeyExpression expression) {
            super.visit(expression);
            visitKeyOrIndexExpression(expression.getPath());
        }

        @Override
        public void visit(ListIndexExpression expression) {
            super.visit(expression);
            visitKeyOrIndexExpression(expression.getPath());
        }

        private void visitKeyOrIndexExpression(PathExpression pathExpression) {
            JoinNode node = (JoinNode) pathExpression.getBaseNode();
            Attribute<?, ?> attribute = node.getParentTreeNode().getAttribute();
            // Exclude element collections as they are not problematic
            if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                // There are weird mappings possible, we have to check if the attribute is a join table
                if (jpaProvider.getJoinTable(node.getParent().getEntityType(), attribute.getName()) != null) {
                    keyRestrictedLeftJoins.add(node);
                }
            }
        }
    }

    String addRootValues(Class<?> clazz, Class<?> valueClazz, String rootAlias, int valueCount, String typeName, String castedParameter, boolean identifiableReference) {
        if (rootAlias == null) {
            throw new IllegalArgumentException("Illegal empty alias for the VALUES clause: " + clazz.getName());
        }

        ValuesStrategy strategy = mainQuery.dbmsDialect.getValuesStrategy();
        String dummyTable = mainQuery.dbmsDialect.getDummyTable();

        // TODO: we should do batching to avoid filling query caches
        ManagedType<?> managedType = mainQuery.metamodel.getManagedType(clazz);
        String idAttributeName = null;
        Set<Attribute<?, ?>> attributeSet;

        if (identifiableReference) {
            SingularAttribute<?, ?> idAttribute = JpaMetamodelUtils.getIdAttribute((EntityType<?>) managedType);
            idAttributeName = idAttribute.getName();
            attributeSet = (Set<Attribute<?, ?>>) (Set<?>) Collections.singleton(idAttribute);
        } else {
            Set<Attribute<?, ?>> originalAttributeSet = (Set<Attribute<?, ?>>) managedType.getAttributes();
            attributeSet = new LinkedHashSet<>(originalAttributeSet.size());
            for (Attribute<?, ?> attr : originalAttributeSet) {
                // Filter out collection attributes
                if (!attr.isCollection()) {
                    attributeSet.add(attr);
                }
            }
        }

        String[][] parameterNames = new String[valueCount][attributeSet.size()];
        ValueRetriever<Object, Object>[] pathExpressions = new ValueRetriever[attributeSet.size()];

        StringBuilder valuesSb = new StringBuilder(20 + valueCount * attributeSet.size() * 3);
        Query valuesExampleQuery = getValuesExampleQuery(clazz, identifiableReference, rootAlias, typeName, castedParameter, attributeSet, parameterNames, pathExpressions, valuesSb, strategy, dummyTable);
        parameterManager.registerValuesParameter(rootAlias, valueClazz, parameterNames, pathExpressions);

        String exampleQuerySql = mainQuery.cbf.getExtendedQuerySupport().getSql(mainQuery.em, valuesExampleQuery);
        String exampleQuerySqlAlias = mainQuery.cbf.getExtendedQuerySupport().getSqlAlias(mainQuery.em, valuesExampleQuery, "e");
        StringBuilder whereClauseSb = new StringBuilder(exampleQuerySql.length());
        String filterNullsTableAlias = "fltr_nulls_tbl_als_";
        String valuesAliases = getValuesAliases(exampleQuerySqlAlias, attributeSet.size(), exampleQuerySql, whereClauseSb, filterNullsTableAlias, strategy, dummyTable);

        if (strategy == ValuesStrategy.SELECT_VALUES) {
            valuesSb.insert(0, valuesAliases);
            valuesSb.append(')');
            valuesAliases = null;
        } else if (strategy == ValuesStrategy.SELECT_UNION) {
            valuesSb.insert(0, valuesAliases);
            mainQuery.dbmsDialect.appendExtendedSql(valuesSb, DbmsStatementType.SELECT, true, true, null, Integer.toString(valueCount + 1), "1", null, null);
            valuesSb.append(')');
            valuesAliases = null;
        }

        boolean filterNulls = mainQuery.getQueryConfiguration().isValuesClauseFilterNullsEnabled();
        if (filterNulls) {
            valuesSb.insert(0, "(select * from ");
            valuesSb.append(' ');
            valuesSb.append(filterNullsTableAlias);
            if (valuesAliases != null) {
                valuesSb.append(valuesAliases);
                valuesAliases = null;
            }
            valuesSb.append(whereClauseSb);
            valuesSb.append(')');
        }

        String valuesClause = valuesSb.toString();

        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, true, aliasManager);
        JoinNode rootNode = JoinNode.createValuesRootNode(managedType, typeName, valueCount, idAttributeName, valuesExampleQuery, valuesClause, valuesAliases, rootAliasInfo);
        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);
        entityFunctionNodes.add(rootNode);
        return rootAlias;
    }

    private String getValuesAliases(String tableAlias, int attributeCount, String exampleQuerySql, StringBuilder whereClauseSb, String filterNullsTableAlias, ValuesStrategy strategy, String dummyTable) {
        int startIndex =  SqlUtils.indexOfSelect(exampleQuerySql);
        int endIndex = exampleQuerySql.indexOf(" from ");

        StringBuilder sb;

        if (strategy == ValuesStrategy.VALUES) {
            sb = new StringBuilder((endIndex - startIndex) - (tableAlias.length() + 3) * attributeCount);
            sb.append('(');
        } else if (strategy == ValuesStrategy.SELECT_VALUES) {
            sb = new StringBuilder(endIndex - startIndex);
            sb.append("(select ");
        } else if (strategy == ValuesStrategy.SELECT_UNION) {
            sb = new StringBuilder((endIndex - startIndex) - (tableAlias.length() + 3) * attributeCount);
            sb.append("(select ");
        } else {
            throw new IllegalArgumentException("Unsupported values strategy: " + strategy);
        }

        whereClauseSb.append(" where");
        String[] columnNames = SqlUtils.getSelectItemColumns(exampleQuerySql, startIndex);

        for (int i = 0; i < columnNames.length; i++) {
            whereClauseSb.append(' ');
            if (i > 0) {
                whereClauseSb.append("or ");
            }
            whereClauseSb.append(filterNullsTableAlias);
            whereClauseSb.append('.');
            whereClauseSb.append(columnNames[i]);
            whereClauseSb.append(" is not null");

            if (strategy == ValuesStrategy.SELECT_VALUES) {
                // TODO: This naming is actually H2 specific
                sb.append('c');
                sb.append(i + 1);
                sb.append(' ');
            } else if (strategy == ValuesStrategy.SELECT_UNION) {
                sb.append("null as ");
            }

            sb.append(columnNames[i]);
            sb.append(',');
        }

        if (strategy == ValuesStrategy.VALUES) {
            sb.setCharAt(sb.length() - 1, ')');
        } else if (strategy == ValuesStrategy.SELECT_VALUES) {
            sb.setCharAt(sb.length() - 1, ' ');
            sb.append(" from ");
        } else if (strategy == ValuesStrategy.SELECT_UNION) {
            sb.setCharAt(sb.length() - 1, ' ');
            if (dummyTable != null) {
                sb.append(" from ");
                sb.append(dummyTable);
            }
        }

        return sb.toString();
    }

    static class SimpleValueRetriever implements ValueRetriever<Object, Object> {
        @Override
        public Object getValue(Object target) {
            return target;
        }
    }

    private static String getCastedParameters(StringBuilder sb, DbmsDialect dbmsDialect, String[] types) {
        sb.setLength(0);
        if (dbmsDialect.needsCastParameters()) {
            for (int i = 0; i < types.length; i++) {
                sb.append(dbmsDialect.cast("?", types[i]));
                sb.append(',');
            }
        } else {
            for (int i = 0; i < types.length; i++) {
                sb.append("?,");
            }
        }

        return sb.substring(0, sb.length() - 1);
    }

    private Query getValuesExampleQuery(Class<?> clazz, boolean identifiableReference, String prefix, String typeName, String castedParameter, Set<Attribute<?, ?>> attributeSet, String[][] parameterNames, ValueRetriever<?, ?>[] pathExpressions, StringBuilder valuesSb, ValuesStrategy strategy, String dummyTable) {
        int valueCount = parameterNames.length;
        String[] attributes = new String[attributeSet.size()];
        String[] attributeParameter = new String[attributeSet.size()];
        // This size estimation roughly assumes a maximum attribute name length of 15
        StringBuilder sb = new StringBuilder(50 + valueCount * prefix.length() * attributeSet.size() * 50);
        sb.append("SELECT ");

        if (clazz == ValuesEntity.class) {
            sb.append("e.");
            attributes[0] = attributeSet.iterator().next().getName();
            attributeParameter[0] = mainQuery.dbmsDialect.needsCastParameters() ? castedParameter : "?";
            pathExpressions[0] = new SimpleValueRetriever();
            sb.append(attributes[0]);
            sb.append(',');
        } else if (identifiableReference) {
            sb.append("e.");
            Attribute<?, ?> attribute = attributeSet.iterator().next();
            attributes[0] = attribute.getName();
            String[] columnTypes = metamodel.getManagedType(ExtendedManagedType.class, clazz).getAttribute(attribute.getName()).getColumnTypes();
            attributeParameter[0] = getCastedParameters(new StringBuilder(), mainQuery.dbmsDialect, columnTypes);
            pathExpressions[0] = com.blazebit.reflection.ExpressionUtils.getExpression(clazz, attributes[0]);
            sb.append(attributes[0]);
            sb.append(',');
        } else {
            Iterator<Attribute<?, ?>> iter = attributeSet.iterator();
            Map<String, ExtendedAttribute> mapping =  metamodel.getManagedType(ExtendedManagedType.class, clazz).getAttributes();
            StringBuilder paramBuilder = new StringBuilder();
            for (int i = 0; i < attributes.length; i++) {
                sb.append("e.");
                Attribute<?, ?> attribute = iter.next();
                attributes[i] = attribute.getName();
                ExtendedAttribute entry = mapping.get(attribute.getName());
                String[] columnTypes = entry.getColumnTypes();
                attributeParameter[i] = getCastedParameters(paramBuilder, mainQuery.dbmsDialect, columnTypes);
                pathExpressions[i] = com.blazebit.reflection.ExpressionUtils.getExpression(clazz, attributes[i]);
                sb.append(attributes[i]);

                // When the class for which we want a VALUES clause has *ToOne relations, we need to put their ids into the select
                // otherwise we would fetch all of the types attributes, but the VALUES clause can only ever contain the id
                if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC &&
                        attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.EMBEDDED) {
                    ManagedType<?> managedAttributeType = metamodel.managedType(entry.getElementClass());
                    Attribute<?, ?> attributeTypeIdAttribute = JpaMetamodelUtils.getIdAttribute((IdentifiableType<?>) managedAttributeType);
                    sb.append('.');
                    sb.append(attributeTypeIdAttribute.getName());
                }

                sb.append(',');
            }
        }

        sb.setCharAt(sb.length() - 1, ' ');
        sb.append("FROM ");
        sb.append(clazz.getName());
        sb.append(" e WHERE 1=1");

        if (strategy == ValuesStrategy.SELECT_VALUES || strategy == ValuesStrategy.VALUES) {
            valuesSb.append("(VALUES ");
        } else if (strategy == ValuesStrategy.SELECT_UNION) {
            // Nothing to do here
        } else {
            throw new IllegalArgumentException("Unsupported values strategy: " + strategy);
        }

        for (int i = 0; i < valueCount; i++) {
            if (strategy == ValuesStrategy.SELECT_UNION) {
                valuesSb.append(" union all select ");
            } else {
                valuesSb.append('(');
            }

            for (int j = 0; j < attributes.length; j++) {
                sb.append(" OR ");
                if (typeName != null) {
                    sb.append("TREAT_");
                    sb.append(typeName.toUpperCase());
                    sb.append('(');
                    sb.append("e.");
                    sb.append(attributes[j]);
                    sb.append(')');
                } else {
                    sb.append("e.");
                    sb.append(attributes[j]);
                }

                sb.append(" = ");

                sb.append(':');
                int start = sb.length();

                sb.append(prefix);
                sb.append('_');
                sb.append(attributes[j]);
                sb.append('_').append(i);

                String paramName = sb.substring(start, sb.length());
                parameterNames[i][j] = paramName;

                valuesSb.append(attributeParameter[j]);
                valuesSb.append(',');
            }

            if (strategy == ValuesStrategy.SELECT_UNION) {
                valuesSb.setCharAt(valuesSb.length() - 1, ' ');
                if (dummyTable != null) {
                    valuesSb.append("from ");
                    valuesSb.append(dummyTable);
                    valuesSb.append(' ');
                }
            } else {
                valuesSb.setCharAt(valuesSb.length() - 1, ')');
                valuesSb.append(',');
            }
        }

        if (strategy == ValuesStrategy.SELECT_UNION) {
            valuesSb.setCharAt(valuesSb.length() - 1, ' ');
        } else {
            valuesSb.setCharAt(valuesSb.length() - 1, ')');
        }

        String exampleQueryString = sb.toString();
        Query q = mainQuery.em.createQuery(exampleQueryString);

        return q;
    }

    String addRoot(EntityType<?> entityType, String rootAlias) {
        if (rootAlias == null) {
            // TODO: not sure if other JPA providers support case sensitive queries like hibernate
            StringBuilder sb = new StringBuilder(entityType.getName());
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            String alias = sb.toString();

            if (aliasManager.getAliasInfo(alias) == null) {
                rootAlias = alias;
            } else {
                rootAlias = aliasManager.generateRootAlias(alias);
            }
        }
        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, true, aliasManager);
        JoinNode rootNode = JoinNode.createRootNode(entityType, rootAliasInfo);
        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);
        return rootAlias;
    }

    String addRoot(String correlationPath, String rootAlias) {
        Expression expr = expressionFactory.createJoinPathExpression(correlationPath);

        PathElementExpression elementExpr;
        EntityType<?> treatEntityType = null;
        JoinResult result;
        JoinNode correlationParent = null;
        if (expr instanceof PathExpression) {
            PathExpression pathExpression = (PathExpression) expr;

            if (isJoinableSelectAlias(pathExpression, false, false)) {
                throw new IllegalArgumentException("No select alias allowed in join path");
            }

            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            elementExpr = pathElements.get(pathElements.size() - 1);
            if (pathElements.size() > 1) {
                result = implicitJoin(null, pathExpression, null, 0, pathElements.size() - 1, true);
                correlationParent = result.baseNode;
            } else {
                result = new JoinResult(null, null, null);
            }
        } else if (expr instanceof TreatExpression) {
            TreatExpression treatExpression = (TreatExpression) expr;
            Expression expression = treatExpression.getExpression();

            if (expression instanceof PathExpression) {
                PathExpression pathExpression = (PathExpression) expression;
                List<PathElementExpression> pathElements = pathExpression.getExpressions();
                elementExpr = pathElements.get(pathElements.size() - 1);
                result = implicitJoin(null, pathExpression, null, 0, pathElements.size() - 1, true);
                correlationParent = result.baseNode;
                treatEntityType = metamodel.entity(treatExpression.getType());
            } else {
                throw new IllegalArgumentException("Unexpected expression type[" + expression.getClass().getSimpleName() + "] in treat expression: " + treatExpression);
            }
        } else {
            throw new IllegalArgumentException("Correlation join path [" + correlationPath + "] is not a valid join path");
        }

        if (elementExpr instanceof ArrayExpression) {
            throw new IllegalArgumentException("Array expressions are not allowed!");
        }

        if (correlationParent == null) {
            correlationParent = getRootNodeOrFail("Could not join correlation path [", correlationPath, "] because it did not use an absolute path but multiple root nodes are available!");
        }

        if (correlationParent.getAliasInfo().getAliasOwner() == aliasManager) {
            throw new IllegalArgumentException("The correlation path '" + correlationPath + "' does not seem to be part of a parent query!");
        }

        String correlatedAttribute;
        Expression correlatedAttributeExpr;

        if (result.hasField()) {
            correlatedAttribute = result.joinFields(elementExpr.toString());
            correlatedAttributeExpr = expressionFactory.createSimpleExpression(correlatedAttribute, false);
        } else {
            correlatedAttribute = elementExpr.toString();
            correlatedAttributeExpr = elementExpr;
        }

        AttributeHolder joinResult = JpaUtils.getAttributeForJoining(metamodel, correlationParent.getType(), correlatedAttributeExpr, null);
        Class<?> attributeType = joinResult.getAttributeJavaType();

        if (rootAlias == null) {
            StringBuilder sb = new StringBuilder(attributeType.getSimpleName());
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            String alias = sb.toString();

            if (aliasManager.getAliasInfo(alias) == null) {
                rootAlias = alias;
            } else {
                rootAlias = aliasManager.generateRootAlias(alias);
            }
        }

        Type<?> type = metamodel.type(attributeType);
        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, true, aliasManager);
        JoinNode rootNode = JoinNode.createCorrelationRootNode(correlationParent, correlatedAttribute, type, treatEntityType, rootAliasInfo);
        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);
        return rootAlias;
    }

    void removeRoot() {
        // We only use this to remove implicit root nodes
        JoinNode rootNode = rootNodes.remove(0);
        aliasManager.unregisterAliasInfoForBottomLevel(rootNode.getAliasInfo());
    }

    JoinNode getRootNodeOrFail(String string) {
        return getRootNodeOrFail(string, "", "");
    }

    JoinNode getRootNodeOrFail(String prefix, Object middle, String suffix) {
        if (rootNodes.size() > 1) {
            throw new IllegalArgumentException(prefix + middle + suffix);
        }

        return rootNodes.get(0);
    }

    JoinNode getRootNode(Expression expression) {
        String alias;
        if (expression instanceof PropertyExpression) {
            alias = expression.toString();
        } else {
            return null;
        }

        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            JoinNode node = nodes.get(i);
            if (alias.equals(node.getAliasInfo().getAlias())) {
                return node;
            }
        }

        return null;
    }

    public List<JoinNode> getRoots() {
        return rootNodes;
    }

    boolean hasCollections() {
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            if (nodes.get(i).hasCollections()) {
                return true;
            }
        }

        return false;
    }

    boolean hasJoins() {
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            JoinNode n = nodes.get(i);
            if (!n.getNodes().isEmpty() || !n.getEntityJoinNodes().isEmpty()) {
                return true;
            }
            if  (!n.getTreatedJoinNodes().isEmpty()) {
                for (JoinNode treatedNode : n.getTreatedJoinNodes().values()) {
                    if (!treatedNode.getNodes().isEmpty() || !treatedNode.getEntityJoinNodes().isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    boolean hasEntityFunctions() {
        return entityFunctionNodes.size() > 0;
    }

    public Set<JoinNode> getCollectionJoins() {
        if (rootNodes.isEmpty()) {
            return Collections.EMPTY_SET;
        } else {
            Set<JoinNode> collectionJoins = rootNodes.get(0).getCollectionJoins();
            for (int i = 1; i < rootNodes.size(); i++) {
                collectionJoins.addAll(rootNodes.get(i).getCollectionJoins());
            }
            return collectionJoins;
        }
    }

    Set<JoinNode> getEntityFunctionNodes() {
        return entityFunctionNodes;
    }

    public JoinManager getParent() {
        return parent;
    }

    public AliasManager getAliasManager() {
        return aliasManager;
    }

    public SubqueryInitiatorFactory getSubqueryInitFactory() {
        return subqueryInitFactory;
    }

    Set<JoinNode> buildClause(StringBuilder sb, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes, boolean externalRepresenation, List<String> whereConjuncts, Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities, Set<JoinNode> nodesToFetch) {
        final boolean renderFetches = !clauseExclusions.contains(ClauseType.SELECT);
        StringBuilder tempSb = null;
        collectionJoinNodes.clear();
        renderedJoins.clear();
        sb.append(" FROM ");

        // TODO: we might have dependencies to other from clause elements which should also be accounted for
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(", ");
            }

            JoinNode rootNode = nodes.get(i);
            JoinNode correlationParent = rootNode.getCorrelationParent();

            if (externalRepresenation && rootNode.getValueCount() > 0) {
                ManagedType<?> type = rootNode.getManagedType();
                if (type.getJavaType() == ValuesEntity.class) {
                    sb.append(rootNode.getValuesTypeName());
                } else {
                    if (type instanceof EntityType<?>) {
                        sb.append(((EntityType) type).getName());
                        if (rootNode.getValuesIdName() != null) {
                            sb.append('.').append(rootNode.getValuesIdName());
                        }
                    } else {
                        sb.append(rootNode.getValuesTypeName());
                    }
                }
                sb.append("(");
                sb.append(rootNode.getValueCount());
                sb.append(" VALUES)");
            } else if (externalRepresenation && explicitVersionEntities.get(rootNode.getType()) != null) {
                DbmsModificationState state = explicitVersionEntities.get(rootNode.getType()).get(rootNode.getAlias());
                EntityType<?> type = rootNode.getEntityType();
                if (state == DbmsModificationState.NEW) {
                    sb.append("NEW(");
                } else {
                    sb.append("OLD(");
                }
                sb.append(type.getName());
                sb.append(')');
            } else {
                if (correlationParent != null) {
                    renderCorrelationJoinPath(sb, correlationParent.getAliasInfo(), rootNode);
                } else {
                    EntityType<?> type = rootNode.getEntityType();
                    sb.append(type.getName());
                }
            }

            sb.append(' ');

            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }

            sb.append(rootNode.getAliasInfo().getAlias());
            renderedJoins.add(rootNode);

            // TODO: not sure if needed since applyImplicitJoins will already invoke that
            rootNode.registerDependencies();
            applyJoins(sb, rootNode.getAliasInfo(), rootNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, nodesToFetch, whereConjuncts);
            for (JoinNode treatedNode : rootNode.getTreatedJoinNodes().values()) {
                applyJoins(sb, treatedNode.getAliasInfo(), treatedNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, nodesToFetch, whereConjuncts);
            }
            if (!rootNode.getEntityJoinNodes().isEmpty()) {
                // TODO: Fix this with #216
                boolean isCollection = true;
                if (mainQuery.jpaProvider.supportsEntityJoin()) {
                    applyJoins(sb, rootNode.getAliasInfo(), new ArrayList<JoinNode>(rootNode.getEntityJoinNodes()), isCollection, clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, nodesToFetch, whereConjuncts);
                } else {
                    Set<JoinNode> entityNodes = rootNode.getEntityJoinNodes();
                    for (JoinNode entityNode : entityNodes) {
                        // Collect the join nodes referring to collections
                        if (collectCollectionJoinNodes && isCollection) {
                            collectionJoinNodes.add(entityNode);
                        }

                        sb.append(", ");

                        EntityType<?> type = entityNode.getEntityType();
                        sb.append(type.getName());

                        sb.append(' ');

                        if (aliasPrefix != null) {
                            sb.append(aliasPrefix);
                        }

                        sb.append(entityNode.getAliasInfo().getAlias());

                        // TODO: not sure if needed since applyImplicitJoins will already invoke that
                        entityNode.registerDependencies();

                        if (entityNode.getOnPredicate() != null && !entityNode.getOnPredicate().getChildren().isEmpty()) {
                            if (tempSb == null) {
                                tempSb = new StringBuilder();
                            } else {
                                tempSb.setLength(0);
                            }
                            queryGenerator.setClauseType(ClauseType.JOIN);
                            queryGenerator.setQueryBuffer(tempSb);
                            SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.PREDICATE);
                            queryGenerator.generate(entityNode.getOnPredicate());
                            queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
                            queryGenerator.setClauseType(null);
                            whereConjuncts.add(tempSb.toString());
                        }

                        renderedJoins.add(entityNode);
                        applyJoins(sb, entityNode.getAliasInfo(), entityNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, nodesToFetch, whereConjuncts);
                        for (JoinNode treatedNode : entityNode.getTreatedJoinNodes().values()) {
                            applyJoins(sb, treatedNode.getAliasInfo(), treatedNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, nodesToFetch, whereConjuncts);
                        }
                    }
                }
            }
        }

        return collectionJoinNodes;
    }

    void verifyBuilderEnded() {
        joinOnBuilderListener.verifyBuilderEnded();
    }

    void acceptVisitor(JoinNodeVisitor v) {
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            nodes.get(i).accept(v);
        }
    }

    public boolean acceptVisitor(Expression.ResultVisitor<Boolean> aggregateDetector, boolean stopValue) {
        Boolean stop = Boolean.valueOf(stopValue);

        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            if (stop.equals(nodes.get(i).accept(new AbortableOnClauseJoinNodeVisitor(aggregateDetector, stopValue)))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void apply(ExpressionModifierVisitor<? super ExpressionModifier> visitor) {
        List<JoinNode> nodes = rootNodes;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            nodes.get(i).accept(visitor);
        }
    }

    private void renderJoinNode(StringBuilder sb, JoinAliasInfo joinBase, JoinNode node, String aliasPrefix, boolean renderFetches, Set<JoinNode> nodesToFetch, List<String> whereConjuncts) {
        if (!renderedJoins.contains(node)) {
            // We determine the nodes that should be fetched by analyzing the fetch owners during implicit joining
            final boolean fetch = nodesToFetch.contains(node) && renderFetches;
            // Don't render key joins unless fetching is specified on it
            if (node.isQualifiedJoin() && !fetch) {
                renderedJoins.add(node);
                return;
            }
            // We only render treat joins, but not treated join nodes. These treats are just "optional casts" that don't affect joining
            if (node.isTreatedJoinNode()) {
                renderedJoins.add(node);
                return;
            }
            switch (node.getJoinType()) {
                case INNER:
                    sb.append(" JOIN ");
                    break;
                case LEFT:
                    sb.append(" LEFT JOIN ");
                    break;
                case RIGHT:
                    sb.append(" RIGHT JOIN ");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown join type: " + node.getJoinType());
            }

            if (fetch) {
                sb.append("FETCH ");
            }

            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }

            String onCondition = renderJoinPath(sb, joinBase, node, whereConjuncts);
            sb.append(' ');

            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }

            sb.append(node.getAliasInfo().getAlias());
            renderedJoins.add(node);

            if (node.getOnPredicate() != null && !node.getOnPredicate().getChildren().isEmpty()) {
                sb.append(joinRestrictionKeyword);

                // Always render the ON condition in parenthesis to workaround an EclipseLink bug in entity join parsing
                sb.append('(');

                if (onCondition != null) {
                    sb.append(onCondition).append(" AND ");
                }

                queryGenerator.setClauseType(ClauseType.JOIN);
                queryGenerator.setQueryBuffer(sb);
                SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.PREDICATE);
                queryGenerator.setRenderedJoinNodes(renderedJoins);
                queryGenerator.generate(node.getOnPredicate());
                queryGenerator.setRenderedJoinNodes(null);
                queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
                queryGenerator.setClauseType(null);
                sb.append(')');
            } else if (onCondition != null) {
                sb.append(joinRestrictionKeyword);
                sb.append('(');
                sb.append(onCondition);
                sb.append(')');
            }
        }
    }

    private void renderCorrelationJoinPath(StringBuilder sb, JoinAliasInfo joinBase, JoinNode node) {
        if (node.getTreatType() != null) {
            final boolean renderTreat = mainQuery.jpaProvider.supportsTreatJoin() &&
                    (!mainQuery.jpaProvider.supportsSubtypeRelationResolving() || node.getJoinType() == JoinType.INNER);
            if (renderTreat) {
                sb.append("TREAT(");
                renderAlias(sb, joinBase.getJoinNode(), mainQuery.jpaProvider.supportsRootTreat());
                sb.append('.');
                sb.append(node.getCorrelationPath());
                sb.append(" AS ");
                sb.append(node.getTreatType().getName());
                sb.append(')');
            } else if (mainQuery.jpaProvider.supportsSubtypeRelationResolving()) {
                sb.append(joinBase.getAlias()).append('.').append(node.getCorrelationPath());
            } else {
                throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
            }
        } else {
            JoinNode baseNode = joinBase.getJoinNode();
            if (baseNode.getTreatType() != null) {
                if (mainQuery.jpaProvider.supportsRootTreatJoin()) {
                    baseNode.appendAlias(sb, true);
                } else if (mainQuery.jpaProvider.supportsSubtypeRelationResolving()) {
                    baseNode.appendAlias(sb, false);
                } else {
                    throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
                }
            } else {
                baseNode.appendAlias(sb, false);
            }

            sb.append('.').append(node.getCorrelationPath());
        }
    }

    private String renderJoinPath(StringBuilder sb, JoinAliasInfo joinBase, JoinNode node, List<String> whereConjuncts) {
        if (node.getTreatType() != null) {
            // We render the treat join only if it makes sense. If we have e.g. a left join and the provider supports
            // implicit relation resolving then there is no point in rendering the treat join. On the contrary, that might lead to wrong results
            final boolean renderTreat = mainQuery.jpaProvider.supportsTreatJoin() &&
                    (!mainQuery.jpaProvider.supportsSubtypeRelationResolving() || node.getJoinType() == JoinType.INNER);
            final String onCondition;
            final JoinNode baseNode = joinBase.getJoinNode();
            final String treatType = node.getTreatType().getName();
            final String relationName = node.getParentTreeNode().getRelationName();
            JpaProvider.ConstraintType constraintType = mainQuery.jpaProvider.requiresTreatFilter(baseNode.getEntityType(), relationName, node.getJoinType());
            if (constraintType != JpaProvider.ConstraintType.NONE) {
                String constraint = "TYPE(" + node.getAlias() + ") = " + treatType;
                if (constraintType == JpaProvider.ConstraintType.WHERE) {
                    whereConjuncts.add(constraint);
                    onCondition = null;
                } else {
                    onCondition = constraint;
                }
            } else {
                onCondition = null;
            }
            if (renderTreat) {
                sb.append("TREAT(");
                renderAlias(sb, baseNode, mainQuery.jpaProvider.supportsRootTreatTreatJoin());
                sb.append('.');
                sb.append(relationName);
                sb.append(" AS ");
                sb.append(treatType);
                sb.append(')');
            } else if (mainQuery.jpaProvider.supportsSubtypeRelationResolving()) {
                sb.append(joinBase.getAlias()).append('.').append(node.getParentTreeNode().getRelationName());
            } else {
                throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
            }
            return onCondition;
        } else if (node.getCorrelationPath() == null && node.getAliasInfo().isRootNode()) {
            sb.append(node.getEntityType().getName());
        } else if (node.isQualifiedJoin()) {
            sb.append(node.getQualificationExpression());
            sb.append('(');
            sb.append(joinBase.getJoinNode().getAlias());
            sb.append(')');
        } else {
            renderAlias(sb, joinBase.getJoinNode(), mainQuery.jpaProvider.supportsRootTreatJoin());
            sb.append('.').append(node.getParentTreeNode().getRelationName());
        }

        return null;
    }

    private void renderAlias(StringBuilder sb, JoinNode baseNode, boolean supportsTreat) {
        if (baseNode.getTreatType() != null) {
            if (supportsTreat) {
                baseNode.appendAlias(sb, true);
            } else if (mainQuery.jpaProvider.supportsSubtypeRelationResolving()) {
                baseNode.appendAlias(sb, false);
            } else {
                throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
            }
        } else {
            baseNode.appendAlias(sb, false);
        }
    }

    private void renderReverseDependency(StringBuilder sb, JoinNode dependency, String aliasPrefix, boolean renderFetches, Set<JoinNode> nodesToFetch, List<String> whereConjuncts) {
        if (dependency.getParent() != null) {
            renderReverseDependency(sb, dependency.getParent(), aliasPrefix, renderFetches, nodesToFetch, whereConjuncts);
            if (!dependency.getDependencies().isEmpty()) {
                markedJoinNodes.add(dependency);
                try {
                    for (JoinNode dep : dependency.getDependencies()) {
                        if (markedJoinNodes.contains(dep)) {
                            throw new IllegalStateException("Cyclic join dependency detected at absolute path ["
                                    + dep.getAliasInfo().getAbsolutePath() + "] with alias [" + dep.getAliasInfo().getAlias() + "]");
                        }
                        // render reverse dependencies
                        renderReverseDependency(sb, dep, aliasPrefix, renderFetches, nodesToFetch, whereConjuncts);
                    }
                } finally {
                    markedJoinNodes.remove(dependency);
                }
            }
            renderJoinNode(sb, dependency.getParent().getAliasInfo(), dependency, aliasPrefix, renderFetches, nodesToFetch, whereConjuncts);
        }
    }

    private void applyJoins(StringBuilder sb, JoinAliasInfo joinBase, Map<String, JoinTreeNode> nodes, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes, boolean renderFetches, Set<JoinNode> nodesToFetch, List<String> whereConjuncts) {
        for (Map.Entry<String, JoinTreeNode> nodeEntry : nodes.entrySet()) {
            JoinTreeNode treeNode = nodeEntry.getValue();
            List<JoinNode> stack = new ArrayList<JoinNode>();
            stack.addAll(treeNode.getJoinNodes().descendingMap().values());

            applyJoins(sb, joinBase, stack, treeNode.isCollection(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, nodesToFetch, whereConjuncts);
        }
    }

    private void applyJoins(StringBuilder sb, JoinAliasInfo joinBase, List<JoinNode> stack, boolean isCollection, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes, boolean renderFetches, Set<JoinNode> nodesToFetch, List<String> whereConjuncts) {
        while (!stack.isEmpty()) {
            JoinNode node = stack.remove(stack.size() - 1);
            // If the clauses in which a join node occurs are all excluded or the join node is not mandatory for the cardinality, we skip it
            if (!clauseExclusions.isEmpty() && clauseExclusions.containsAll(node.getClauseDependencies()) && !node.isCardinalityMandatory()) {
                continue;
            }

            stack.addAll(node.getEntityJoinNodes());
            stack.addAll(node.getTreatedJoinNodes().values());

            // We have to render any dependencies this join node has before actually rendering itself
            if (!node.getDependencies().isEmpty()) {
                renderReverseDependency(sb, node, aliasPrefix, renderFetches, nodesToFetch, whereConjuncts);
            }

            // Collect the join nodes referring to collections
            if (collectCollectionJoinNodes && isCollection) {
                collectionJoinNodes.add(node);
            }

            // Finally render this join node
            renderJoinNode(sb, joinBase, node, aliasPrefix, renderFetches, nodesToFetch, whereConjuncts);

            // Render child nodes recursively
            if (!node.getNodes().isEmpty()) {
                applyJoins(sb, node.getAliasInfo(), node.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches, nodesToFetch, whereConjuncts);
            }
        }
    }

    private boolean isExternal(PathExpression path) {
        PathElementExpression firstElem = path.getExpressions().get(0);
        return isExternal(path, firstElem);
    }

    private boolean isExternal(TreatExpression treatExpression) {
        Expression expression = treatExpression.getExpression();

        if (expression instanceof PathExpression) {
            PathExpression path = (PathExpression) expression;
            PathElementExpression firstElem = path.getExpressions().get(0);
            return isExternal(path, firstElem);
        } else if (expression instanceof FunctionExpression) {
            // Can only be key or value function
            PathExpression path = (PathExpression) ((FunctionExpression) expression).getExpressions().get(0);
            PathElementExpression firstElem = path.getExpressions().get(0);
            return isExternal(path, firstElem);
        } else {
            throw new IllegalArgumentException("Unexpected expression type[" + expression.getClass().getSimpleName() + "] in treat expression: " + treatExpression);
        }
    }

    private boolean isExternal(PathExpression path, PathElementExpression firstElem) {
        String startAlias;
        if (firstElem instanceof ArrayExpression) {
            startAlias = ((ArrayExpression) firstElem).getBase().toString();
        } else if (firstElem instanceof TreatExpression) {
            Expression treatedExpression = ((TreatExpression) firstElem).getExpression();

            if (treatedExpression instanceof PathExpression) {
                treatedExpression = ((PathExpression) treatedExpression).getExpressions().get(0);
            }

            if (treatedExpression instanceof ArrayExpression) {
                startAlias = ((ArrayExpression) treatedExpression).getBase().toString();
            } else if (treatedExpression instanceof TreatExpression) {
                startAlias = ((TreatExpression) treatedExpression).getExpression().toString();
            } else {
                startAlias = treatedExpression.toString();
            }
        } else {
            startAlias = firstElem.toString();
        }

        AliasInfo aliasInfo = aliasManager.getAliasInfo(startAlias);
        if (aliasInfo == null) {
            return false;
        }

        if (parent != null && aliasInfo.getAliasOwner() != aliasManager) {
            // the alias exists but originates from the parent query builder

            // an external select alias must not be de-referenced
            if (path.getExpressions().size() > 1) {
                // But if check if the expression really is just an alias reference or the
                if (aliasInfo instanceof SelectInfo) {
                    throw new ExternalAliasDereferencingException("Start alias [" + startAlias + "] of path [" + path.toString()
                            + "] is external and must not be dereferenced");
                }
            }

            // the alias is external so we do not have to treat it
            return true;
        } else if (aliasInfo.getAliasOwner() == aliasManager) {
            // the alias originates from the current query builder and is therefore not external
            return false;
        } else {
            throw new IllegalStateException("Alias [" + aliasInfo.getAlias() + "] originates from an unknown query");
        }
    }

    public boolean isJoinableSelectAlias(PathExpression pathExpr, boolean fromSelect, boolean fromSubquery) {
        return getJoinableSelectAlias(pathExpr, fromSelect, fromSubquery) != null;
    }

    public Expression getJoinableSelectAlias(PathExpression pathExpr, boolean fromSelect, boolean fromSubquery) {
        // We can skip this check if the first element is not a simple property
        if (!(pathExpr.getExpressions().get(0) instanceof PropertyExpression)) {
            return null;
        }

        boolean singlePathElement = pathExpr.getExpressions().size() == 1;
        String startAlias = pathExpr.getExpressions().get(0).toString();

        AliasInfo aliasInfo = aliasManager.getAliasInfo(startAlias);
        if (aliasInfo == null) {
            return null;
        }

        if (aliasInfo instanceof SelectInfo && !fromSelect && !fromSubquery) {
            // select alias
            if (!singlePathElement) {
                throw new IllegalStateException("Path starting with select alias not allowed");
            }

            // might be joinable
            Expression expression = ((SelectInfo) aliasInfo).getExpression();
            // If the expression the alias refers to and the expression are the same, we are resolving an ambiguous alias expression
            if (expression == pathExpr) {
                return null;
            }

            return expression;
        }

        return null;
    }

    <X> JoinOnBuilder<X> joinOn(X result, String base, Class<?> clazz, String alias, JoinType type) {
        PathExpression basePath = expressionFactory.createPathExpression(base);
        EntityType<?> entityType = metamodel.entity(clazz);

        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty alias!");
        }
        if (type != JoinType.INNER && !mainQuery.jpaProvider.supportsEntityJoin()) {
            throw new IllegalArgumentException("The JPA provider does not support entity joins and an emulation for non-inner entity joins is not implemented!");
        }

        List<PathElementExpression> propertyExpressions = basePath.getExpressions();
        JoinNode baseNode;
        if (propertyExpressions.size() > 1) {
            AliasInfo aliasInfo = aliasManager.getAliasInfo(propertyExpressions.get(0).toString());

            if (aliasInfo == null || !(aliasInfo instanceof JoinAliasInfo)) {
                throw new IllegalArgumentException("The base '" + base + "' is not a valid join alias!");
            }

            baseNode = ((JoinAliasInfo) aliasInfo).getJoinNode();
            for (int i = 1; i < propertyExpressions.size(); i++) {
                String relationName = propertyExpressions.get(i).toString();
                JoinTreeNode treeNode = baseNode.getNodes().get(relationName);
                if (treeNode == null) {
                    break;
                }
                baseNode = treeNode.getDefaultNode();
                if (baseNode == null) {
                    break;
                }
            }
            if (baseNode == null) {
                throw new IllegalArgumentException("The base '" + base + "' is not a valid join alias!");
            }
        } else {
            AliasInfo aliasInfo = aliasManager.getAliasInfo(base);

            if (aliasInfo == null || !(aliasInfo instanceof JoinAliasInfo)) {
                throw new IllegalArgumentException("The base '" + base + "' is not a valid join alias!");
            }
            baseNode = ((JoinAliasInfo) aliasInfo).getJoinNode();
        }

        JoinAliasInfo joinAliasInfo = new JoinAliasInfo(alias, null, false, true, aliasManager);
        JoinNode entityJoinNode = JoinNode.createEntityJoinNode(baseNode, type, entityType, joinAliasInfo);
        joinAliasInfo.setJoinNode(entityJoinNode);
        baseNode.addEntityJoin(entityJoinNode);
        aliasManager.registerAliasInfo(joinAliasInfo);

        joinOnBuilderListener.joinNode = entityJoinNode;
        return joinOnBuilderListener.startBuilder(new JoinOnBuilderImpl<X>(result, joinOnBuilderListener, parameterManager, expressionFactory, subqueryInitFactory));
    }

    <X> JoinOnBuilder<X> joinOn(X result, String path, String alias, JoinType type, boolean defaultJoin) {
        joinOnBuilderListener.joinNode = join(path, alias, type, false, defaultJoin);
        return joinOnBuilderListener.startBuilder(new JoinOnBuilderImpl<X>(result, joinOnBuilderListener, parameterManager, expressionFactory, subqueryInitFactory));
    }

    JoinNode join(String path, String alias, JoinType type, boolean fetch, boolean defaultJoin) {
        Expression expr = expressionFactory.createJoinPathExpression(path);
        PathElementExpression elementExpr;
        String treatType = null;
        JoinResult result;
        JoinNode current;
        if (expr instanceof PathExpression) {
            PathExpression pathExpression = (PathExpression) expr;

            if (isExternal(pathExpression) || isJoinableSelectAlias(pathExpression, false, false)) {
                throw new IllegalArgumentException("No external path or select alias allowed in join path");
            }

            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            elementExpr = pathElements.get(pathElements.size() - 1);
            result = implicitJoin(null, pathExpression, null, 0, pathElements.size() - 1, false);
            current = result.baseNode;
        } else if (expr instanceof TreatExpression) {
            TreatExpression treatExpression = (TreatExpression) expr;

            if (isExternal(treatExpression)) {
                throw new IllegalArgumentException("No external path or select alias allowed in join path");
            }

            Expression expression = treatExpression.getExpression();

            if (expression instanceof PathExpression) {
                PathExpression pathExpression = (PathExpression) expression;
                List<PathElementExpression> pathElements = pathExpression.getExpressions();
                elementExpr = pathElements.get(pathElements.size() - 1);
                result = implicitJoin(null, pathExpression, null, 0, pathElements.size() - 1, false);
                current = result.baseNode;
                treatType = treatExpression.getType();
            } else {
                throw new IllegalArgumentException("Unexpected expression type[" + expression.getClass().getSimpleName() + "] in treat expression: " + treatExpression);
            }
        } else {
            throw new IllegalArgumentException("Join path [" + path + "] is not a path");
        }

        if (elementExpr instanceof ArrayExpression) {
            throw new IllegalArgumentException("Array expressions are not allowed!");
        } else if (elementExpr instanceof MapKeyExpression) {
            MapKeyExpression mapKeyExpression = (MapKeyExpression) elementExpr;
            boolean fromSubquery = false;
            boolean fromSelectAlias = false;
            boolean joinRequired = true;
            current = joinMapKey(mapKeyExpression, alias, null, fromSubquery, fromSelectAlias, joinRequired, fetch, false, defaultJoin);
            result = new JoinResult(current, null, current.getType());
        } else {
            List<String> joinRelationAttributes = result.addToList(new ArrayList<String>());
            joinRelationAttributes.add(elementExpr.toString());
            current = current == null ? getRootNodeOrFail("Could not join path [", path, "] because it did not use an absolute path but multiple root nodes are available!") : current;
            result = createOrUpdateNode(current, joinRelationAttributes, treatType, alias, type, false, defaultJoin);
        }

        if (fetch) {
            fetchPath(result.baseNode);
        }

        return result.baseNode;
    }

    public void implicitJoin(Expression expression, boolean objectLeafAllowed, String targetType, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean idRemovable) {
        implicitJoin(expression, objectLeafAllowed, targetType, fromClause, fromSubquery, fromSelectAlias, joinRequired, idRemovable, false);
    }

    @SuppressWarnings("checkstyle:methodlength")
    public void implicitJoin(Expression expression, boolean objectLeafAllowed, String targetTypeName, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean idRemovable, boolean fetch) {
        PathExpression pathExpression;
        if (expression instanceof PathExpression) {
            pathExpression = (PathExpression) expression;

            Expression aliasedExpression;
            // If joinable select alias, it is guaranteed to have only a single element
            if ((aliasedExpression = getJoinableSelectAlias(pathExpression, fromClause == ClauseType.SELECT, fromSubquery)) != null) {
                // this check is necessary to prevent infinite recursion in the case of e.g. SELECT name AS name
                if (!fromSelectAlias) {
                    // we have to do this implicit join because we might have to adjust the selectOnly flag in the referenced join nodes
                    implicitJoin(aliasedExpression, true, null, fromClause, fromSubquery, true, joinRequired, false);
                }
                return;
            } else if (isExternal(pathExpression)) {
                // try to set base node and field for the external expression based
                // on existing joins in the super query
                parent.implicitJoin(pathExpression, true, targetTypeName, fromClause, true, fromSelectAlias, joinRequired, false);
                return;
            }

            // First try to implicit join indices of array expressions since we will need their base nodes
            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            int pathElementSize = pathElements.size();
            for (int i = 0; i < pathElementSize; i++) {
                PathElementExpression pathElem = pathElements.get(i);
                if (pathElem instanceof ArrayExpression) {
                    implicitJoin(((ArrayExpression) pathElem).getIndex(), false, null, fromClause, fromSubquery, fromSelectAlias, joinRequired, false);
                }
            }

            PathElementExpression elementExpr = pathElements.get(pathElements.size() - 1);
            boolean singleValuedAssociationIdExpression = false;
            JoinNode current = null;
            List<String> resultFields = new ArrayList<String>();
            JoinResult currentResult;

            JoinNode possibleRoot;
            int startIndex = 0;

            // Skip root speculation if this is just a single element path
            if (pathElements.size() > 1 && (possibleRoot = getRootNode(pathElements.get(0))) != null) {
                startIndex = 1;
                current = possibleRoot;
            }

            if (pathElements.size() > startIndex + 1) {
                int maybeSingularAssociationIndex = pathElements.size() - 2;
                int maybeSingularAssociationIdIndex = pathElements.size() - 1;
                currentResult = implicitJoin(current, pathExpression, fromClause, startIndex, maybeSingularAssociationIndex, false);
                current = currentResult.baseNode;
                resultFields = currentResult.addToList(resultFields);

                singleValuedAssociationIdExpression = isSingleValuedAssociationId(currentResult, pathElements, idRemovable);

                if (singleValuedAssociationIdExpression) {
                    if (!mainQuery.jpaProvider.supportsSingleValuedAssociationIdExpressions()) {
                        if (idRemovable) {
                            // remove the id part only if we come from a predicate
                            elementExpr = null;
                            if (current == null) {
                                // This is the case when we use a join alias like "alias.id"
                                // We need to resolve the base since it might not be the root node
                                AliasInfo a = aliasManager.getAliasInfo(pathElements.get(maybeSingularAssociationIndex).toString());
                                // We know this can only be a join node alias
                                current = ((JoinAliasInfo) a).getJoinNode();
                                resultFields = Collections.emptyList();
                            }
                        } else {
                            // Need a normal join
                            currentResult = implicitJoin(current, pathExpression, fromClause, maybeSingularAssociationIndex, pathElements.size() - 1, false);
                            current = currentResult.baseNode;
                            resultFields = currentResult.addToList(resultFields);
                            singleValuedAssociationIdExpression = false;
                        }
                    }
                } else {
                    if (currentResult.hasField()) {
                        // currentResult.typeName?
                        // Redo the joins for embeddables by moving the start index back
                        currentResult = implicitJoin(current, pathExpression, fromClause, maybeSingularAssociationIndex - currentResult.fields.size(), maybeSingularAssociationIdIndex, false);
                        if (currentResult.fields != resultFields) {
                            resultFields.clear();
                        }
                    } else {
                        currentResult = implicitJoin(current, pathExpression, fromClause, maybeSingularAssociationIndex, maybeSingularAssociationIdIndex, false);
                    }

                    current = currentResult.baseNode;
                    resultFields = currentResult.addToList(resultFields);
                }
            } else {
                // Single element expression like "alias", "relation", "property" or "alias.relation"
                currentResult = implicitJoin(current, pathExpression, fromClause, startIndex, pathElements.size() - 1, false);
                current = currentResult.baseNode;
                resultFields = currentResult.addToList(resultFields);

                if (idRemovable) {
                    if (current != null) {
                        // If there is a "base node" i.e. a current, the expression has 2 elements
                        if (isId(current.getType(), elementExpr)) {
                            // We remove the "id" part
                            elementExpr = null;
                            // Treat it like a single valued association id expression
                            singleValuedAssociationIdExpression = true;
                        }
                    } else {
                        // There is no base node, this is a expression with 1 element
                        // Either relative or a direct alias
                        String elementExpressionString;
                        if (elementExpr instanceof ArrayExpression) {
                            elementExpressionString = ((ArrayExpression) elementExpr).getBase().toString();
                        } else {
                            elementExpressionString = elementExpr.toString();
                        }
                        AliasInfo a = aliasManager.getAliasInfo(elementExpressionString);
                        if (a == null) {
                            // If the element expression is an alias, there is nothing to replace
                            current = getRootNodeOrFail("Could not join path [", expression, "] because it did not use an absolute path but multiple root nodes are available!");
                            if (isId(current.getType(), elementExpr)) {
                                // We replace the "id" part with the alias
                                elementExpr = new PropertyExpression(current.getAlias());
                            }
                        }
                    }
                }
            }

            JoinResult result;
            AliasInfo aliasInfo;

            // The case of a simple join alias usage
            if (pathElements.size() == 1 && !fromSelectAlias
                    && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null) {
                // No need to assert the resultFields here since they can't appear anyways if we enter this branch
                if (aliasInfo instanceof SelectInfo) {
                    if (targetTypeName != null) {
                        throw new IllegalArgumentException("The select alias '" + aliasInfo.getAlias()
                                + "' can not be used for a treat expression!.");
                    }

                    // We actually allow usage of select aliases in expressions, but JPA doesn't, so we have to resolve them here
                    Expression selectExpr = ((SelectInfo) aliasInfo).getExpression();

                    if (!(selectExpr instanceof PathExpression)) {
                        throw new RuntimeException("The select expression '" + selectExpr.toString()
                                + "' is not a simple path expression! No idea how to implicit join that.");
                    }
                    // join the expression behind a select alias once when it is encountered the first time
                    if (((PathExpression) selectExpr).getBaseNode() == null) {
                        implicitJoin(selectExpr, objectLeafAllowed, null, fromClause, fromSubquery, true, joinRequired, false);
                    }
                    PathExpression selectPathExpr = (PathExpression) selectExpr;
                    PathReference reference = selectPathExpr.getPathReference();
                    result = new JoinResult((JoinNode) selectPathExpr.getBaseNode(), Arrays.asList(selectPathExpr.getField()), reference.getType());
                } else {
                    JoinNode pathJoinNode = ((JoinAliasInfo) aliasInfo).getJoinNode();
                    if (targetTypeName != null) {
                        // Treated root path
                        ManagedType<?> targetType = metamodel.managedType(targetTypeName);
                        result = new JoinResult(pathJoinNode, null, targetType.getJavaType());
                    } else {
                        // Naked join alias usage like in "KEY(joinAlias)"
                        result = new JoinResult(pathJoinNode, null, pathJoinNode.getType());
                    }
                }
            } else if (pathElements.size() == 1 && elementExpr instanceof QualifiedExpression) {
                QualifiedExpression qualifiedExpression = (QualifiedExpression) elementExpr;
                JoinNode baseNode;
                if (elementExpr instanceof MapKeyExpression) {
                    baseNode = joinMapKey((MapKeyExpression) elementExpr, null, fromClause, fromSubquery, fromSelectAlias, true, fetch, true, true);
                } else if (elementExpr instanceof ListIndexExpression) {
                    baseNode = joinListIndex((ListIndexExpression) elementExpr, null, fromClause, fromSubquery, fromSelectAlias, true, fetch, true, true);
                } else if (elementExpr instanceof MapEntryExpression) {
                    baseNode = joinMapEntry((MapEntryExpression) elementExpr, null, fromClause, fromSubquery, fromSelectAlias, true, fetch, true, true);
                } else if (elementExpr instanceof MapValueExpression) {
                    implicitJoin(qualifiedExpression.getPath(), objectLeafAllowed, targetTypeName, fromClause, fromSubquery, fromSelectAlias, joinRequired, false, fetch);
                    baseNode = (JoinNode) qualifiedExpression.getPath().getBaseNode();
                } else {
                    throw new IllegalArgumentException("Unknown qualified expression type: " + elementExpr);
                }

                result = new JoinResult(baseNode, null, baseNode.getType());
            } else {
                // current might be null
                if (current == null) {
                    current = getRootNodeOrFail("Could not join path [", expression, "] because it did not use an absolute path but multiple root nodes are available!");
                }

                if (singleValuedAssociationIdExpression) {
                    String associationName = pathElements.get(pathElements.size() - 2).toString();
                    AliasInfo singleValuedAssociationRootAliasInfo = null;
                    JoinTreeNode treeNode;

                    if (currentResult.hasField()) {
                        associationName = currentResult.joinFields(associationName);
                    } else if (pathElements.size() == 2) {
                        // If this path is composed of only two elements, the association name could represent an alias
                        singleValuedAssociationRootAliasInfo = aliasManager.getAliasInfoForBottomLevel(associationName);
                    }

                    if (singleValuedAssociationRootAliasInfo != null) {
                        JoinNode singleValuedAssociationRoot = ((JoinAliasInfo) singleValuedAssociationRootAliasInfo).getJoinNode();
                        if (elementExpr != null) {
                            AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(
                                    metamodel,
                                    singleValuedAssociationRoot.getType(),
                                    elementExpr,
                                    singleValuedAssociationRoot.getAlias()
                            );
                            Class<?> type = attributeHolder.getAttributeJavaType();
                            result = new JoinResult(singleValuedAssociationRoot, Arrays.asList(elementExpr.toString()), type);
                        } else {
                            result = new JoinResult(singleValuedAssociationRoot, null, singleValuedAssociationRoot.getType());
                        }
                    } else {
                        treeNode = current.getNodes().get(associationName);

                        if (treeNode != null && treeNode.getDefaultNode() != null) {
                            if (elementExpr != null) {
                                AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(
                                        metamodel,
                                        treeNode.getDefaultNode().getType(),
                                        elementExpr,
                                        treeNode.getDefaultNode().getAlias()
                                );
                                Class<?> type = attributeHolder.getAttributeJavaType();
                                result = new JoinResult(treeNode.getDefaultNode(), Arrays.asList(elementExpr.toString()), type);
                            } else {
                                result = new JoinResult(treeNode.getDefaultNode(), null, treeNode.getDefaultNode().getType());
                            }
                        } else {
                            if (elementExpr != null) {
                                String elementString = elementExpr.toString();
                                Expression resultExpr = expressionFactory.createSimpleExpression(associationName + '.' + elementString, false);
                                AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(
                                        metamodel,
                                        current.getType(),
                                        resultExpr,
                                        current.getAlias()
                                );
                                Class<?> type = attributeHolder.getAttributeJavaType();
                                result = new JoinResult(current, Arrays.asList(associationName, elementString), type);
                            } else {
                                Expression resultExpr = expressionFactory.createSimpleExpression(associationName, false);
                                AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(
                                        metamodel,
                                        current.getType(),
                                        resultExpr,
                                        current.getAlias()
                                );
                                Class<?> type = attributeHolder.getAttributeJavaType();
                                result = new JoinResult(current, Arrays.asList(associationName), type);
                            }
                        }
                    }
                } else if (elementExpr instanceof ArrayExpression) {
                    // TODO: Not sure if necessary
                    if (!resultFields.isEmpty()) {
                        throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part ["
                                + StringUtils.join(".", resultFields) + "]");
                    }

                    ArrayExpression arrayExpr = (ArrayExpression) elementExpr;
                    String joinRelationName = arrayExpr.getBase().toString();

                    // Find a node by a predicate match
                    JoinNode matchingNode;

                    if (pathElements.size() == 1 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(joinRelationName)) != null) {
                        // The first node is allowed to be a join alias
                        if (aliasInfo instanceof SelectInfo) {
                            throw new IllegalArgumentException("Illegal reference to the select alias '" + joinRelationName + "'");
                        }
                        current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                        generateAndApplyOnPredicate(current, arrayExpr);
                    } else if ((matchingNode = findNode(current, joinRelationName, arrayExpr)) != null) {
                        // We found a join node for the same join relation with the same array expression predicate
                        current = matchingNode;
                    } else {
                        String joinAlias = getJoinAlias(arrayExpr);
                        currentResult = createOrUpdateNode(current, Arrays.asList(joinRelationName), null, joinAlias, null, true, false);
                        current = currentResult.baseNode;
                        // TODO: Not sure if necessary
                        if (currentResult.hasField()) {
                            throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part [" + currentResult.joinFields()
                                    + "]");
                        }
                        generateAndApplyOnPredicate(current, arrayExpr);
                    }

                    result = new JoinResult(current, null, current.getType());
                } else if (!pathExpression.isUsedInCollectionFunction()) {
                    if (resultFields.isEmpty()) {
                        result = implicitJoinSingle(current, elementExpr.toString(), objectLeafAllowed, joinRequired);
                    } else {
                        resultFields.add(elementExpr.toString());

                        String attributeName = StringUtils.join(".", resultFields);
                        // Validates and gets the path type
                        getPathType(current.getType(), attributeName, pathExpression);

                        result = implicitJoinSingle(current, attributeName, objectLeafAllowed, joinRequired);
                    }
                } else {
                    if (resultFields.isEmpty()) {
                        String attributeName = elementExpr.toString();
                        Class<?> type = getPathType(current.getType(), attributeName, pathExpression);
                        result = new JoinResult(current, Arrays.asList(attributeName), type);
                    } else {
                        resultFields.add(elementExpr.toString());

                        String attributeName = StringUtils.join(".", resultFields);
                        Class<?> type = getPathType(current.getType(), attributeName, pathExpression);

                        result = new JoinResult(current, resultFields, type);
                    }
                }
            }

            if (fetch) {
                fetchPath(result.baseNode);
            }

            // Don't forget to update the clause dependencies!!
            if (fromClause != null) {
                updateClauseDependencies(result.baseNode, fromClause, new HashSet<JoinNode>());
            }

            if (result.isLazy()) {
                pathExpression.setPathReference(new LazyPathReference(result.baseNode, result.joinFields(), result.type));
            } else {
                pathExpression.setPathReference(new SimplePathReference(result.baseNode, result.joinFields(), result.type));
            }
        } else if (expression instanceof FunctionExpression) {
            List<Expression> expressions = ((FunctionExpression) expression).getExpressions();
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                implicitJoin(expressions.get(i), objectLeafAllowed, null, fromClause, fromSubquery, fromSelectAlias, joinRequired, false);
            }
        } else if (expression instanceof MapKeyExpression) {
            MapKeyExpression mapKeyExpression = (MapKeyExpression) expression;
            joinMapKey(mapKeyExpression, null, fromClause, fromSubquery, fromSelectAlias, joinRequired, fetch, true, true);
        } else if (expression instanceof QualifiedExpression) {
            implicitJoin(((QualifiedExpression) expression).getPath(), objectLeafAllowed, null, fromClause, fromSubquery, fromSelectAlias, joinRequired, false);
        } else if (expression instanceof ArrayExpression || expression instanceof GeneralCaseExpression || expression instanceof TreatExpression) {
            // TODO: Having a treat expression actually makes sense here for fetchOnly
            // NOTE: I haven't found a use case for this yet, so I'd like to throw an exception instead of silently not supporting this
            throw new IllegalArgumentException("Unsupported expression for implicit joining found: " + expression);
        } else {
            // Other expressions don't need handling
        }
    }

    private JoinNode getFetchOwner(JoinNode node) {
        while (node.isFetch()) {
            node = node.getParent();
        }
        return node;
    }

    private static class LazyPathReference implements PathReference {
        private final JoinNode baseNode;
        private final String field;
        private final Class<?> type;

        public LazyPathReference(JoinNode baseNode, String field, Class<?> type) {
            this.baseNode = baseNode;
            this.field = field;
            this.type = type;
        }

        @Override
        public JoinNode getBaseNode() {
            JoinTreeNode subNode = baseNode.getNodes().get(field);
            if (subNode != null && subNode.getDefaultNode() != null) {
                return subNode.getDefaultNode();
            }

            return baseNode;
        }

        @Override
        public String getField() {
            JoinTreeNode subNode = baseNode.getNodes().get(field);
            if (subNode != null && subNode.getDefaultNode() != null) {
                return null;
            }

            return field;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((baseNode == null) ? 0 : baseNode.hashCode());
            result = prime * result + ((field == null) ? 0 : field.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof PathReference)) {
                return false;
            }
            PathReference other = (PathReference) obj;
            if (baseNode == null) {
                if (other.getBaseNode() != null) {
                    return false;
                }
            } else if (!baseNode.equals(other.getBaseNode())) {
                return false;
            }
            if (field == null) {
                if (other.getField() != null) {
                    return false;
                }
            } else if (!field.equals(other.getField())) {
                return false;
            }
            return true;
        }
    }

    private Class<?> getPathType(Class<?> baseType, String expression, PathExpression pathExpression) {
        try {
            return JpaUtils.getAttributeForJoining(metamodel, baseType, expressionFactory.createPathExpression(expression), null).getAttributeJavaType();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part ["
                    + expression + "]");
        }
    }

    private boolean isSingleValuedAssociationId(JoinResult joinResult, List<PathElementExpression> pathElements, boolean idRemovable) {
        JoinNode parent = joinResult.baseNode;
        int maybeSingularAssociationIndex = pathElements.size() - 2;
        int maybeSingularAssociationIdIndex = pathElements.size() - 1;
        Type<?> baseType;
        AttributeHolder maybeSingularAssociationJoinResult;
        PathElementExpression maybeSingularAssociationNameExpression = pathElements.get(maybeSingularAssociationIndex);
        String maybeSingularAssociationName = getSimpleName(maybeSingularAssociationNameExpression);

        if (parent == null) {
            // This is the case when we have exactly 2 path elements
            if (maybeSingularAssociationNameExpression instanceof TreatExpression) {
                // When we dereference a treat expression, we simply say this can never be a single valued association id
                return false;
            } else {
                AliasInfo a = aliasManager.getAliasInfo(maybeSingularAssociationName);

                if (a == null) {
                    // if the path element is no alias we can do some optimizations
                    parent = getRootNodeOrFail("Ambiguous join path [", maybeSingularAssociationName, "] because of multiple root nodes!");
                    baseType = parent.getManagedType();
                    maybeSingularAssociationJoinResult = JpaUtils.getAttributeForJoining(metamodel, baseType.getJavaType(), maybeSingularAssociationNameExpression, parent.getAlias());
                } else if (!(a instanceof JoinAliasInfo)) {
                    throw new IllegalArgumentException("Can't dereference select alias in the expression!");
                } else {
                    // If there is a JoinAliasInfo for the path element, we have to use the alias
                    // We can only "consider" this path a single valued association id when we are about to "remove" the id part
                    if (idRemovable) {
                        Class<?> maybeSingularAssociationClass = ((JoinAliasInfo) a).getJoinNode().getType();
                        PathElementExpression maybeSingularAssociationIdExpression = pathElements.get(maybeSingularAssociationIdIndex);

                        return isId(maybeSingularAssociationClass, maybeSingularAssociationIdExpression);
                    } else {
                        // Otherwise we return false in order to signal that a normal implicit join should be done
                        return false;
                    }
                }
            }
        } else {
            if (joinResult.hasField()) {
                Expression fieldExpression = expressionFactory.createPathExpression(joinResult.joinFields());
                AttributeHolder result = JpaUtils.getAttributeForJoining(metamodel, parent.getType(), fieldExpression, parent.getAlias());
                baseType = metamodel.type(result.getAttributeJavaType());
            } else {
                baseType = parent.getNodeType();
            }

            maybeSingularAssociationJoinResult = JpaUtils.getAttributeForJoining(metamodel, baseType.getJavaType(), maybeSingularAssociationNameExpression, null);
        }

        Attribute<?, ?> maybeSingularAssociation = maybeSingularAssociationJoinResult.getAttribute();
        if (maybeSingularAssociation == null) {
            // A naked root treat like TREAT(alias AS Subtype) has no attribute
            return false;
        }

        if (maybeSingularAssociation.getPersistentAttributeType() != Attribute.PersistentAttributeType.MANY_TO_ONE
                && maybeSingularAssociation.getPersistentAttributeType() != Attribute.PersistentAttributeType.ONE_TO_ONE
        ) {
            // Attributes that are not ManyToOne or OneToOne can't possibly be single value association sources
            return false;
        }

        if (maybeSingularAssociation instanceof MapKeyAttribute<?, ?>) {
            // Skip the foreign join column check for map keys
            // They aren't allowed as join sources in the JPA providers yet so we can only render them directly
        } else if (baseType instanceof EmbeddableType<?>) {
            // Get the base type. This is important if the path is "deeper" i.e. when having embeddables
            baseType = parent.getNodeType();
            String attributePath = joinResult.joinFields(maybeSingularAssociationName);
            if (mainQuery.jpaProvider.isForeignJoinColumn((EntityType<?>) baseType, attributePath)) {
                return false;
            }
        } else if (mainQuery.jpaProvider.isForeignJoinColumn((EntityType<?>) baseType, maybeSingularAssociation.getName())) {
            return false;
        }

        Class<?> maybeSingularAssociationClass = maybeSingularAssociationJoinResult.getAttributeJavaType();
        PathElementExpression maybeSingularAssociationIdExpression = pathElements.get(maybeSingularAssociationIdIndex);

        return isId(maybeSingularAssociationClass, maybeSingularAssociationIdExpression);
    }

    private boolean isId(Class<?> managedTypeClass, Expression idExpression) {
        AttributeHolder maybeSingularAssociationIdJoinResult = JpaUtils.getAttributeForJoining(metamodel, managedTypeClass, idExpression, null);

        Attribute<?, ?> maybeSingularAssociationId = maybeSingularAssociationIdJoinResult.getAttribute();
        if (!(maybeSingularAssociationId instanceof SingularAttribute<?, ?>)) {
            return false;
        }

        if (!((SingularAttribute<?, ?>) maybeSingularAssociationId).isId()) {
            return false;
        }

        return true;
    }

    private String getSimpleName(PathElementExpression element) {
        if (element == null) {
            return null;
        } else if (element instanceof ArrayExpression) {
            return ((ArrayExpression) element).getBase().getProperty();
        } else {
            return element.toString();
        }
    }

    private String getJoinAlias(ArrayExpression expr) {
        StringBuilder sb = new StringBuilder(expr.getBase().toString());
        Expression indexExpr = expr.getIndex();

        if (indexExpr instanceof ParameterExpression) {
            ParameterExpression indexParamExpr = (ParameterExpression) indexExpr;
            sb.append('_');
            sb.append(indexParamExpr.getName());
        } else if (indexExpr instanceof PathExpression) {
            PathExpression indexPathExpr = (PathExpression) indexExpr;
            sb.append('_');
            sb.append(((JoinNode) indexPathExpr.getBaseNode()).getAliasInfo().getAlias());

            if (indexPathExpr.getField() != null) {
                sb.append('_');
                sb.append(indexPathExpr.getField().replaceAll("\\.", "_"));
            }
        } else if (indexExpr instanceof NumericLiteral) {
            sb.append('_');
            sb.append(((NumericLiteral) indexExpr).getValue());
        } else if (indexExpr instanceof StringLiteral) {
            sb.append('_');
            sb.append(((StringLiteral) indexExpr).getValue());
        } else {
            throw new IllegalStateException("Invalid array index expression " + indexExpr.toString());
        }

        return sb.toString();
    }

    private EqPredicate getArrayExpressionPredicate(JoinNode joinNode, ArrayExpression arrayExpr) {
        PathExpression keyPath = new PathExpression(new ArrayList<PathElementExpression>(), true);
        keyPath.getExpressions().add(new PropertyExpression(joinNode.getAliasInfo().getAlias()));
        keyPath.setPathReference(new SimplePathReference(joinNode, null, joinNode.getType()));
        Attribute<?, ?> arrayBaseAttribute = joinNode.getParentTreeNode().getAttribute();
        Expression keyExpression;
        if (arrayBaseAttribute instanceof ListAttribute<?, ?>) {
            keyExpression = new ListIndexExpression(keyPath);
        } else {
            keyExpression = new MapKeyExpression(keyPath);
        }
        return new EqPredicate(keyExpression, arrayExpr.getIndex());
    }

    private void registerDependencies(final JoinNode joinNode, CompoundPredicate onExpression) {
        onExpression.accept(new VisitorAdapter() {

            @Override
            public void visit(PathExpression pathExpr) {
                // prevent loop dependencies to the same join node
                if (pathExpr.getBaseNode() != joinNode) {
                    joinNode.getDependencies().add((JoinNode) pathExpr.getBaseNode());
                }
            }

        });
    }

    private void generateAndApplyOnPredicate(JoinNode joinNode, ArrayExpression arrayExpr) {
        EqPredicate valueKeyFilterPredicate = getArrayExpressionPredicate(joinNode, arrayExpr);

        if (joinNode.getOnPredicate() != null) {
            CompoundPredicate currentPred = joinNode.getOnPredicate();

            // Only add the predicate if it isn't contained yet
            if (!findPredicate(currentPred, valueKeyFilterPredicate)) {
                currentPred.getChildren().add(valueKeyFilterPredicate);
                registerDependencies(joinNode, currentPred);
            }
        } else {
            CompoundPredicate onAndPredicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.AND);
            onAndPredicate.getChildren().add(valueKeyFilterPredicate);
            joinNode.setOnPredicate(onAndPredicate);
            registerDependencies(joinNode, onAndPredicate);
        }
    }

    private JoinResult implicitJoin(JoinNode current, PathExpression pathExpression, ClauseType fromClause, int start, int end, boolean allowParentAliases) {
        List<PathElementExpression> pathElements = pathExpression.getExpressions();
        List<String> resultFields = new ArrayList<String>();
        PathElementExpression elementExpr;

        for (int i = start; i < end; i++) {
            AliasInfo aliasInfo;
            elementExpr = pathElements.get(i);

            if (elementExpr instanceof ArrayExpression) {
                ArrayExpression arrayExpr = (ArrayExpression) elementExpr;
                String joinRelationName;
                List<String> joinRelationAttributes;

                if (!resultFields.isEmpty()) {
                    resultFields.add(arrayExpr.getBase().toString());
                    joinRelationAttributes = resultFields;
                    resultFields = new ArrayList<String>();
                    joinRelationName = StringUtils.join(".", joinRelationAttributes);
                } else {
                    joinRelationName = arrayExpr.getBase().toString();
                    joinRelationAttributes = Arrays.asList(joinRelationName);
                }

                current = current == null ? getRootNodeOrFail("Ambiguous join path [", joinRelationName, "] because of multiple root nodes!") : current;
                // Find a node by a predicate match
                JoinNode matchingNode = findNode(current, joinRelationName, arrayExpr);

                if (matchingNode != null) {
                    current = matchingNode;
                } else if (i == 0 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(joinRelationName)) != null) {
                    // The first node is allowed to be a join alias
                    if (aliasInfo instanceof SelectInfo) {
                        throw new IllegalArgumentException("Illegal reference to the select alias '" + joinRelationName + "'");
                    }
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                    generateAndApplyOnPredicate(current, arrayExpr);
                } else {
                    String joinAlias = getJoinAlias(arrayExpr);
                    final JoinResult result = createOrUpdateNode(current, joinRelationAttributes, null, joinAlias, null, true, false);
                    current = result.baseNode;
                    resultFields = result.addToList(resultFields);
                    generateAndApplyOnPredicate(current, arrayExpr);
                }
            } else if (elementExpr instanceof TreatExpression) {
                if (i != 0 || current != null) {
                    throw new IllegalArgumentException("A treat expression should be the first element in a path!");
                }
                TreatExpression treatExpression = (TreatExpression) elementExpr;
                boolean fromSubquery = false;
                boolean fromSelectAlias = false;
                boolean joinRequired = false;
                boolean fetch = false;

                if (treatExpression.getExpression() instanceof PathExpression) {
                    PathExpression treatedPathExpression = (PathExpression) treatExpression.getExpression();
                    implicitJoin(treatedPathExpression, true, treatExpression.getType(), fromClause, fromSubquery, fromSelectAlias, true, false, fetch);
                    JoinNode treatedJoinNode = (JoinNode) treatedPathExpression.getBaseNode();
                    EntityType<?> treatType = metamodel.getEntity(treatExpression.getType());
                    current = treatedJoinNode.getTreatedJoinNode(treatType);
                } else {
                    throw new UnsupportedOperationException("Unsupported treated expression type: " + treatExpression.getExpression().getClass());
                }
            } else if (elementExpr instanceof MapKeyExpression) {
                MapKeyExpression mapKeyExpression = (MapKeyExpression) elementExpr;
                boolean fromSubquery = false;
                boolean fromSelectAlias = false;
                boolean joinRequired = true;
                boolean fetch = false;
                current = joinMapKey(mapKeyExpression, null, fromClause, fromSubquery, fromSelectAlias, joinRequired, fetch, true, true);
            } else if (elementExpr instanceof MapValueExpression) {
                MapValueExpression mapValueExpression = (MapValueExpression) elementExpr;
                boolean fromSubquery = false;
                boolean fromSelectAlias = false;
                boolean joinRequired = true;
                boolean fetch = false;

                implicitJoin(mapValueExpression.getPath(), true, null, fromClause, fromSubquery, fromSelectAlias, joinRequired, fetch);
                current = (JoinNode) mapValueExpression.getPath().getBaseNode();
            } else if (pathElements.size() == 1 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null) {
                if (aliasInfo instanceof SelectInfo) {
                    throw new IllegalArgumentException("Can't dereference a select alias");
                } else {
                    // Join alias usage like in "joinAlias.relationName"
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                }
            } else {
                if (!resultFields.isEmpty()) {
                    resultFields.add(elementExpr.toString());
                    JoinResult currentResult = createOrUpdateNode(current, resultFields, null, null, null, true, true);
                    current = currentResult.baseNode;
                    if (!currentResult.hasField()) {
                        resultFields.clear();
                    }
                } else {
                    final JoinResult result = implicitJoinSingle(current, elementExpr.toString(), allowParentAliases);
                    if (current != result.baseNode) {
                        current = result.baseNode;
                    }
                    resultFields = result.addToList(resultFields);
                }

            }
        }

        if (resultFields.isEmpty()) {
            return new JoinResult(current, null, current == null ? null : current.getType());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(resultFields.get(0));
            for (int i = 1; i < resultFields.size(); i++) {
                sb.append('.');
                sb.append(resultFields.get(i));
            }
            Expression expression = expressionFactory.createSimpleExpression(sb.toString(), false);
            Class<?> type = JpaUtils.getAttributeForJoining(metamodel, current.getType(), expression, current.getAlias()).getAttributeJavaType();
            return new JoinResult(current, resultFields, type);
        }
    }

    private JoinNode joinMapKey(MapKeyExpression mapKeyExpression, String alias, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean fetch, boolean implicit, boolean defaultJoin) {
        implicitJoin(mapKeyExpression.getPath(), true, null, fromClause, fromSubquery, fromSelectAlias, joinRequired, false, fetch);
        JoinNode current = (JoinNode) mapKeyExpression.getPath().getBaseNode();
        String joinRelationName = "KEY(" + current.getParentTreeNode().getRelationName() + ")";
        MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) current.getParentTreeNode().getAttribute();
        Attribute<?, ?> keyAttribute = new MapKeyAttribute<>(mapAttribute);
        String aliasToUse = alias == null ? current.getParentTreeNode().getRelationName().replaceAll("\\.", "_") + "_key" : alias;
        Type<?> joinRelationType = metamodel.type(mapAttribute.getKeyJavaType());
        current = getOrCreate(current, joinRelationName, joinRelationType, null, aliasToUse, JoinType.LEFT, "Ambiguous implicit join", implicit, true, keyAttribute);
        return current;
    }

    private JoinNode joinMapEntry(MapEntryExpression mapEntryExpression, String alias, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean fetch, boolean implicit, boolean defaultJoin) {
        implicitJoin(mapEntryExpression.getPath(), true, null, fromClause, fromSubquery, fromSelectAlias, joinRequired, false, fetch);
        JoinNode current = (JoinNode) mapEntryExpression.getPath().getBaseNode();
        String joinRelationName = "ENTRY(" + current.getParentTreeNode().getRelationName() + ")";
        MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) current.getParentTreeNode().getAttribute();
        Attribute<?, ?> entryAttribute = new MapEntryAttribute<>(mapAttribute);
        String aliasToUse = alias == null ? current.getParentTreeNode().getRelationName().replaceAll("\\.", "_") + "_entry" : alias;
        Type<?> joinRelationType = metamodel.type(Map.Entry.class);
        current = getOrCreate(current, joinRelationName, joinRelationType, null, aliasToUse, JoinType.LEFT, "Ambiguous implicit join", implicit, true, entryAttribute);
        return current;
    }

    private JoinNode joinListIndex(ListIndexExpression listIndexExpression, String alias, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean fetch, boolean implicit, boolean defaultJoin) {
        implicitJoin(listIndexExpression.getPath(), true, null, fromClause, fromSubquery, fromSelectAlias, joinRequired, false, fetch);
        JoinNode current = (JoinNode) listIndexExpression.getPath().getBaseNode();
        String joinRelationName = "INDEX(" + current.getParentTreeNode().getRelationName() + ")";
        ListAttribute<?, ?> listAttribute = (ListAttribute<?, ?>) current.getParentTreeNode().getAttribute();
        Attribute<?, ?> indexAttribute = new ListIndexAttribute<>(listAttribute);
        String aliasToUse = alias == null ? current.getParentTreeNode().getRelationName().replaceAll("\\.", "_") + "_index" : alias;
        Type<?> joinRelationType = metamodel.type(Integer.class);
        current = getOrCreate(current, joinRelationName, joinRelationType, null, aliasToUse, JoinType.LEFT, "Ambiguous implicit join", implicit, true, indexAttribute);
        return current;
    }

    private JoinResult implicitJoinSingle(JoinNode baseNode, String attributeName, boolean allowParentAliases) {
        if (baseNode == null) {
            // When no base is given, check if the attribute name is an alias
            AliasInfo aliasInfo = allowParentAliases ?
                    aliasManager.getAliasInfo(attributeName) :
                    aliasManager.getAliasInfoForBottomLevel(attributeName);
            if (aliasInfo != null && aliasInfo instanceof JoinAliasInfo) {
                JoinNode node = ((JoinAliasInfo) aliasInfo).getJoinNode();
                // if it is, we can just return the join node
                return new JoinResult(node, null, node.getType());
            }
        }

        // If we have no base node, root is assumed
        if (baseNode == null) {
            baseNode = getRootNodeOrFail("Ambiguous join path [", attributeName, "] because of multiple root nodes!");
        }

        // check if the path is joinable, assuming it is relative to the root (implicit root prefix)
        return createOrUpdateNode(baseNode, Arrays.asList(attributeName), null, null, null, true, true);
    }

    private JoinResult implicitJoinSingle(JoinNode baseNode, String attributeName, boolean objectLeafAllowed, boolean joinRequired) {
        JoinNode newBaseNode;
        String field;
        Class<?> type;
        boolean lazy = false;
        // The given path may be relative to the root or it might be an alias
        if (objectLeafAllowed) {
            Type<?> baseNodeType = baseNode.getNodeType();

            AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(metamodel, baseNodeType.getJavaType(), expressionFactory.createJoinPathExpression(attributeName), baseNode.getAlias());
            Attribute<?, ?> attr = attributeHolder.getAttribute();
            if (attr == null) {
                throw new IllegalArgumentException("Field with name '" + attributeName + "' was not found within managed type " + baseNodeType.getJavaType().getName());
            }

            if (joinRequired || attr.isCollection()) {
                final JoinResult newBaseNodeResult = implicitJoinSingle(baseNode, attributeName, false);
                newBaseNode = newBaseNodeResult.baseNode;
                // check if the last path element was also joined
                if (newBaseNode != baseNode) {
                    field = null;
                    type = newBaseNode.getType();
                } else {
                    field = attributeName;
                    type = attributeHolder.getAttributeJavaType();
                }
            } else {
                newBaseNode = baseNode;
                field = attributeName;
                type = attributeHolder.getAttributeJavaType();
                lazy = true;
            }
        } else {
            Class<?> baseNodeType = baseNode.getType();
            AttributeHolder attributeHolder = JpaUtils.getAttributeForJoining(metamodel, baseNodeType, expressionFactory.createJoinPathExpression(attributeName), baseNode.getAlias());
            Attribute<?, ?> attr = attributeHolder.getAttribute();
            if (attr == null) {
                throw new IllegalArgumentException("Field with name " + attributeName + " was not found within class " + baseNodeType.getName());
            }
            if (JpaMetamodelUtils.isJoinable(attr)) {
                throw new IllegalArgumentException("No object leaf allowed but " + attributeName + " is an object leaf");
            }
            newBaseNode = baseNode;
            field = attributeName;
            type = attr.getJavaType();
        }
        return new JoinResult(newBaseNode, field == null ? null : Arrays.asList(field), type, lazy);
    }

    private void updateClauseDependencies(JoinNode baseNode, ClauseType clauseDependency, Set<JoinNode> seenNodes) {
        if (!seenNodes.add(baseNode)) {
            // Cyclic dependency
            throw new IllegalStateException("Cyclic join dependency: " + seenNodes);
        }

        JoinNode current = baseNode;
        while (current != null) {
            // update the ON clause dependent nodes to also have a clause dependency
            for (JoinNode dependency : current.getDependencies()) {
                updateClauseDependencies(dependency, clauseDependency, seenNodes);
            }

            current.getClauseDependencies().add(clauseDependency);

            // If the parent node was a dependency, we are done with cycle checking
            // as it has been checked by the recursive call before
            if (current.getDependencies().contains(current.getParent())) {
                break;
            }
            current = current.getParent();
        }
    }

    private JoinType getModelAwareType(JoinNode baseNode, Attribute<?, ?> attr) {
        if (baseNode.getJoinType() == JoinType.LEFT) {
            return JoinType.LEFT;
        }

        if ((attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE)
                && ((SingularAttribute<?, ?>) attr).isOptional() == false) {
            return JoinType.INNER;
        } else {
            return JoinType.LEFT;
        }
    }

    private JoinResult createOrUpdateNode(JoinNode baseNode, List<String> joinRelationAttributes, String treatType, String alias, JoinType joinType, boolean implicit, boolean defaultJoin) {
        Class<?> baseNodeType = baseNode.getType();
        String joinRelationName = StringUtils.join(".", joinRelationAttributes);
        AttributeHolder attrJoinResult = JpaUtils.getAttributeForJoining(metamodel, baseNodeType, expressionFactory.createJoinPathExpression(joinRelationName), baseNode.getAlias());
        Attribute<?, ?> attr = attrJoinResult.getAttribute();
        if (attr == null) {
            throw new IllegalArgumentException("Field with name " + joinRelationName + " was not found within class " + baseNodeType.getName());
        }

        if (!JpaMetamodelUtils.isJoinable(attr)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(new StringBuilder("Field with name ").append(joinRelationName)
                        .append(" of class ")
                        .append(baseNodeType.getName())
                        .append(" is parseable and therefore it has not to be fetched explicitly.")
                        .toString());
            }
            return new JoinResult(baseNode, joinRelationAttributes, attrJoinResult.getAttributeJavaType());
        }

        if (implicit) {
            String aliasToUse = alias == null ? attr.getName() : alias;
            alias = aliasManager.generateJoinAlias(aliasToUse);
        }

        if (joinType == null) {
            joinType = getModelAwareType(baseNode, attr);
        }

        Type<?> joinRelationType = metamodel.type(attrJoinResult.getAttributeJavaType());
        JoinNode newNode = getOrCreate(baseNode, joinRelationName, joinRelationType, treatType, alias, joinType, "Ambiguous implicit join", implicit, defaultJoin, attr);

        return new JoinResult(newNode, null, newNode.getType());
    }

    private void checkAliasIsAvailable(String alias, String currentJoinPath, String errorMessage) {
        AliasInfo oldAliasInfo = aliasManager.getAliasInfoForBottomLevel(alias);
        if (oldAliasInfo instanceof SelectInfo) {
            throw new IllegalStateException("Alias [" + oldAliasInfo.getAlias() + "] already used as select alias");
        }
        JoinAliasInfo oldJoinAliasInfo = (JoinAliasInfo) oldAliasInfo;
        if (oldJoinAliasInfo != null) {
            if (!oldJoinAliasInfo.getAbsolutePath().equals(currentJoinPath)) {
                throw new IllegalArgumentException(errorMessage);
            } else {
                throw new RuntimeException("Probably a programming error if this happens. An alias[" + alias + "] for the same join path["
                        + currentJoinPath + "] is available but the join node is not!");
            }
        }
    }

    private JoinNode getOrCreate(JoinNode baseNode, String joinRelationName, Type<?> joinRelationType, String treatType, String alias, JoinType type, String errorMessage, boolean implicit, boolean defaultJoin, Attribute<?, ?> attribute) {
        JoinTreeNode treeNode = baseNode.getOrCreateTreeNode(joinRelationName, attribute);
        JoinNode node = treeNode.getJoinNode(alias, defaultJoin);
        String qualificationExpression = null;

        if (attribute instanceof QualifiedAttribute) {
            qualificationExpression = ((QualifiedAttribute) attribute).getQualificationExpression();
        }

        EntityType<?> treatJoinType;
        String currentJoinPath;

        if (treatType != null) {
            // Verify it's a valid type
            treatJoinType = metamodel.getEntity(treatType);
            currentJoinPath = "TREAT(" + baseNode.getAliasInfo().getAbsolutePath() + "." + joinRelationName + " AS " + treatJoinType.getName() + ")";
        } else {
            treatJoinType = null;
            currentJoinPath = baseNode.getAliasInfo().getAbsolutePath() + "." + joinRelationName;
        }

        if (node == null) {
            // a join node for the join relation does not yet exist
            checkAliasIsAvailable(alias, currentJoinPath, errorMessage);

            // the alias might have to be postfixed since it might already exist in parent queries
            if (implicit && aliasManager.getAliasInfo(alias) != null) {
                alias = aliasManager.generateJoinAlias(alias);
            }

            JoinAliasInfo newAliasInfo = new JoinAliasInfo(alias, currentJoinPath, implicit, false, aliasManager);
            aliasManager.registerAliasInfo(newAliasInfo);
            node = JoinNode.createAssociationJoinNode(baseNode, treeNode, type, joinRelationType, treatJoinType, qualificationExpression, newAliasInfo);
            newAliasInfo.setJoinNode(node);
            treeNode.addJoinNode(node, defaultJoin);
        } else {
            JoinAliasInfo nodeAliasInfo = node.getAliasInfo();

            if (!alias.equals(nodeAliasInfo.getAlias())) {
                // Aliases for the same join paths don't match
                if (nodeAliasInfo.isImplicit() && !implicit) {
                    // Overwrite implicit aliases
                    aliasManager.unregisterAliasInfoForBottomLevel(nodeAliasInfo);
                    // we must alter the nodeAliasInfo instance since this instance is also set on the join node

                    // TODO: we must update the key for the JoinNode in the respective JoinTreeNode
                    nodeAliasInfo.setAlias(alias);
                    nodeAliasInfo.setImplicit(false);
                    // We can only change the join type if the existing node is implicit and the update on the node is not implicit
                    node.setJoinType(type);

                    aliasManager.registerAliasInfo(nodeAliasInfo);
                } else if (!nodeAliasInfo.isImplicit() && !implicit) {
                    throw new IllegalArgumentException("Alias conflict [" + nodeAliasInfo.getAlias() + "=" + nodeAliasInfo.getAbsolutePath() + ", "
                            + alias + "=" + currentJoinPath + "]");
                }
            }

            if (treatJoinType != null) {
                if (node.getTreatType() == null) {
                    node = node.getTreatedJoinNode(treatJoinType);
                } else if (!treatJoinType.equals(node.getTreatType())) {
                    throw new IllegalArgumentException("A join node [" + nodeAliasInfo.getAlias() + "=" + nodeAliasInfo.getAbsolutePath() + "] "
                            + "for treat type [" + treatType + "] conflicts with the existing treat type [" + node.getTreatType() + "]");
                }
            }
        }
        return node;
    }

    private JoinNode findNode(JoinNode baseNode, String joinRelationName, ArrayExpression arrayExpression) {
        JoinTreeNode treeNode = baseNode.getNodes().get(joinRelationName);

        if (treeNode == null) {
            return null;
        }

        for (JoinNode node : treeNode.getJoinNodes().values()) {
            Predicate pred = getArrayExpressionPredicate(node, arrayExpression);
            CompoundPredicate compoundPredicate = node.getOnPredicate();

            if (findPredicate(compoundPredicate, pred)) {
                return node;
            }
        }

        return null;
    }

    private boolean findPredicate(CompoundPredicate compoundPredicate, Predicate pred) {
        if (compoundPredicate != null) {
            List<Predicate> children = compoundPredicate.getChildren();
            int size = children.size();
            for (int i = 0; i < size; i++) {
                if (pred.equals(children.get(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Fetch the given node only.
     *
     * @param node
     */
    private void fetchPath(JoinNode node) {
        node.setFetch(true);
        // fetches implicitly need to be selected
        node.getClauseDependencies().add(ClauseType.SELECT);
    }

    // TODO: needs equals-hashCode implementation
    private static class JoinResult {

        final JoinNode baseNode;
        final List<String> fields;
        final Class<?> type;
        final boolean lazy;

        public JoinResult(JoinNode baseNode, List<String> fields, Class<?> type) {
            this.baseNode = baseNode;
            this.fields = fields;
            this.type = type;
            this.lazy = false;
        }

        public JoinResult(JoinNode baseNode, List<String> fields, Class<?> type, boolean lazy) {
            this.baseNode = baseNode;
            this.fields = fields;
            this.type = type;
            this.lazy = lazy;
        }

        private boolean hasField() {
            return fields != null && !fields.isEmpty();
        }

        private String joinFields(String field) {
            if (fields == null || fields.isEmpty()) {
                return field;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(fields.get(0));
            for (int i = 1; i < fields.size(); i++) {
                sb.append('.');
                sb.append(fields.get(i));
            }

            if (field != null) {
                sb.append('.');
                sb.append(field);
            }

            return sb.toString();
        }

        private String joinFields() {
            return joinFields(null);
        }

        private List<String> addToList(List<String> resultFields) {
            if (hasField()) {
                if (resultFields != fields) {
                    resultFields.addAll(fields);
                }
            }

            return resultFields;
        }

        private boolean isLazy() {
            return lazy;
        }

    }

    private class JoinOnBuilderEndedListener extends PredicateBuilderEndedListenerImpl {

        private JoinNode joinNode;

        @Override
        public void onBuilderEnded(PredicateBuilder builder) {
            super.onBuilderEnded(builder);
            Predicate predicate = builder.getPredicate();
            predicate.accept(new VisitorAdapter() {

                private boolean isKeyFunction;

                @Override
                public void visit(ListIndexExpression expression) {
                    boolean old = isKeyFunction;
                    this.isKeyFunction = true;
                    super.visit(expression);
                    this.isKeyFunction = old;
                }

                @Override
                public void visit(MapKeyExpression expression) {
                    boolean old = isKeyFunction;
                    this.isKeyFunction = true;
                    super.visit(expression);
                    this.isKeyFunction = old;
                }

                @Override
                public void visit(PathExpression expression) {
                    expression.setCollectionKeyPath(isKeyFunction);
                    super.visit(expression);
                }

            });
            joinNode.setOnPredicate((CompoundPredicate) predicate);
        }
    }
}
