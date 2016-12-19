/*
 * Copyright 2014 - 2016 Blazebit.
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
import com.blazebit.persistence.impl.expression.NumericLiteral;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PathReference;
import com.blazebit.persistence.impl.expression.PropertyExpression;
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
import com.blazebit.persistence.impl.util.MetamodelUtils;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.ValuesStrategy;

import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
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
    private final EntityMetamodel metamodel; // needed for model-aware joins
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
        Class<?> clazz = node.getPropertyClass();
        String treatFunction = node.getValuesFunction();
        int valueCount = node.getValueCount();
        int attributeCount = node.getAttributeCount();
        Query valuesExampleQuery = node.getValueQuery();
        String valuesClause = node.getValuesClause();
        String valuesAliases = node.getValuesAliases();

        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, implicit, true, aliasManager);
        JoinNode rootNode;

        if (node.getCorrelationParent() != null) {
            throw new IllegalArgumentException("Cloning subqueries not yet implemented!");
        } else {
            rootNode = new JoinNode(rootAliasInfo, clazz, treatFunction, valueCount, attributeCount, valuesExampleQuery, valuesClause, valuesAliases);
        }

        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);

        for (JoinTreeNode treeNode : node.getNodes().values()) {
            applyFrom(rootNode, treeNode);
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
        JoinNode node = new JoinNode(parent, treeNode, oldNode.getParentTreatType(), newAliasInfo, oldNode.getJoinType(), oldNode.getPropertyClass(), oldNode.getTreatType());
        newAliasInfo.setJoinNode(node);

        node.setFetch(oldNode.isFetch());
        if (oldNode.getOnPredicate() != null) {
            node.setOnPredicate(subqueryInitFactory.reattachSubqueries(oldNode.getOnPredicate().clone(true)));
        }

        for (JoinTreeNode oldTreeNode : oldNode.getNodes().values()) {
            applyFrom(node, oldTreeNode);
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
        public void visit(FunctionExpression expression) {
            super.visit(expression);
            if ("KEY".equals(expression.getFunctionName()) || "INDEX".equals(expression.getFunctionName())) {
                // We know it can only be a path
                PathExpression pathExpression = (PathExpression) expression.getExpressions().get(0);
                JoinNode node = (JoinNode) pathExpression.getBaseNode();
                Attribute<?, ?> attribute = node.getParentTreeNode().getAttribute();
                // Exclude element collections as they are not problematic
                if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                    // There are weird mappings possible, we have to check if the attribute is a join table
                    if (jpaProvider.isJoinTable(attribute)) {
                        keyRestrictedLeftJoins.add(node);
                    }
                }
            }
        }
    }

    String addRootValues(Class<?> clazz, Class<?> valueClazz, String rootAlias, int valueCount, String treatFunction, String castedParameter, boolean identifiableReference) {
        if (rootAlias == null) {
            throw new IllegalArgumentException("Illegal empty alias for the VALUES clause: " + clazz.getName());
        }

        ValuesStrategy strategy = mainQuery.dbmsDialect.getValuesStrategy();
        String dummyTable = mainQuery.dbmsDialect.getDummyTable();

        // TODO: we should do batching to avoid filling query caches
        ManagedType<?> managedType = mainQuery.metamodel.getManagedType(clazz);
        Set<Attribute<?, ?>> attributeSet;

        if (identifiableReference) {
            attributeSet = (Set<Attribute<?, ?>>) (Set<?>) Collections.singleton(JpaUtils.getIdAttribute((EntityType<?>) managedType));
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
        Query valuesExampleQuery = getValuesExampleQuery(clazz, identifiableReference, rootAlias, treatFunction, castedParameter, attributeSet, parameterNames, pathExpressions, valuesSb, strategy, dummyTable);
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
        JoinNode rootNode = new JoinNode(rootAliasInfo, clazz, treatFunction, valueCount, attributeSet.size(), valuesExampleQuery, valuesClause, valuesAliases);
        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);
        entityFunctionNodes.add(rootNode);
        return rootAlias;
    }

    private String getValuesAliases(String tableAlias, int attributeCount, String exampleQuerySql, StringBuilder whereClauseSb, String filterNullsTableAlias, ValuesStrategy strategy, String dummyTable) {
        final String select = "select ";
        int startIndex = exampleQuerySql.indexOf(select) + select.length();
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

        // Search for table alias usages
        final String searchAlias = tableAlias + ".";
        int searchIndex = startIndex;
        int attributeNumber = 1;
        while ((searchIndex = exampleQuerySql.indexOf(searchAlias, searchIndex)) > -1 && searchIndex < endIndex) {
            searchIndex += searchAlias.length();

            whereClauseSb.append(' ');
            if (attributeNumber > 1) {
                whereClauseSb.append("or ");
            }
            whereClauseSb.append(filterNullsTableAlias);
            whereClauseSb.append('.');

            if (strategy == ValuesStrategy.SELECT_VALUES) {
                // TODO: This naming is actually H2 specific
                sb.append('c');
                sb.append(attributeNumber);
                sb.append(' ');
            } else if (strategy == ValuesStrategy.SELECT_UNION) {
                sb.append("null as ");
            }

            // Append chars until non-identifier char was found
            for (; searchIndex < endIndex; searchIndex++) {
                final char c = exampleQuerySql.charAt(searchIndex);
                // NOTE: We expect users to be sane and only allow letters, numbers and underscore in an identifier
                if (Character.isLetterOrDigit(c) || c == '_') {
                    sb.append(c);
                    whereClauseSb.append(c);
                } else {
                    sb.append(',');
                    break;
                }
            }

            whereClauseSb.append(" is not null");
            attributeNumber++;
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

    private Query getValuesExampleQuery(Class<?> clazz, boolean identifiableReference, String prefix, String treatFunction, String castedParameter, Set<Attribute<?, ?>> attributeSet, String[][] parameterNames, ValueRetriever<?, ?>[] pathExpressions, StringBuilder valuesSb, ValuesStrategy strategy, String dummyTable) {
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
            String[] columnTypes = metamodel.getAttributeColumnTypeMapping(clazz).get(attribute.getName()).getValue();
            attributeParameter[0] = getCastedParameters(new StringBuilder(), mainQuery.dbmsDialect, columnTypes);
            pathExpressions[0] = new SimpleValueRetriever();
            sb.append(attributes[0]);
            sb.append(',');
        } else {
            Iterator<Attribute<?, ?>> iter = attributeSet.iterator();
            Map<String, Map.Entry<AttributePath, String[]>> mapping =  metamodel.getAttributeColumnTypeMapping(clazz);
            StringBuilder paramBuilder = new StringBuilder();
            for (int i = 0; i < attributes.length; i++) {
                sb.append("e.");
                Attribute<?, ?> attribute = iter.next();
                attributes[i] = attribute.getName();
                String[] columnTypes = mapping.get(attribute.getName()).getValue();
                attributeParameter[i] = getCastedParameters(paramBuilder, mainQuery.dbmsDialect, columnTypes);
                pathExpressions[i] = com.blazebit.reflection.ExpressionUtils.getExpression(clazz, attributes[i]);
                sb.append(attributes[i]);
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
                if (treatFunction != null) {
                    sb.append(treatFunction);
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

    String addRoot(EntityType<?> clazz, String rootAlias) {
        if (rootAlias == null) {
            // TODO: not sure if other JPA providers support case sensitive queries like hibernate
            StringBuilder sb = new StringBuilder(clazz.getName());
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            String alias = sb.toString();

            if (aliasManager.getAliasInfo(alias) == null) {
                rootAlias = alias;
            } else {
                rootAlias = aliasManager.generatePostfixedAlias(alias);
            }
        }
        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, true, aliasManager);
        JoinNode rootNode = new JoinNode(null, null, null, rootAliasInfo, null, clazz.getJavaType(), null);
        rootAliasInfo.setJoinNode(rootNode);
        rootNodes.add(rootNode);
        // register root alias in aliasManager
        aliasManager.registerAliasInfo(rootAliasInfo);
        return rootAlias;
    }

    String addRoot(String correlationPath, String rootAlias) {
        // TODO: TREAT support is missing
        String[] parts = correlationPath.split("\\.");

        String correlationParentAlias = parts[0];
        String correlationPathNoAlias = correlationPath.substring(parts[0].length() + 1);

        // We assume that this is a subquery join manager here
        AliasInfo aliasInfo = aliasManager.getAliasInfo(correlationParentAlias);
        if (aliasInfo == null || !(aliasInfo instanceof JoinAliasInfo) || aliasInfo.getAliasOwner() == aliasManager) {
            throw new IllegalArgumentException("No join node for the alias '" + correlationParentAlias + "' could be found in a parent query!");
        }

        JoinNode correlationParent = ((JoinAliasInfo) aliasInfo).getJoinNode();
        Class<?> attributeType;
        if (correlationPathNoAlias.indexOf('.') < 0) {
            Attribute<?, ?> attribute = JpaUtils.getAttribute(metamodel.managedType(correlationParent.getPropertyClass()), correlationPathNoAlias);
            attributeType = JpaUtils.resolveFieldClass(correlationParent.getPropertyClass(), attribute);
        } else {
            ManagedType<?> managedType = MetamodelUtils.resolveManagedTargetType(metamodel, correlationParent.getPropertyClass(), correlationPath.substring(correlationParentAlias.length() + 1, correlationPath.lastIndexOf('.')));
            Attribute<?, ?> secondLastAttribute = MetamodelUtils.resolveTargetAttribute(metamodel, correlationParent.getPropertyClass(), correlationPathNoAlias);
            attributeType = JpaUtils.resolveFieldClass(managedType.getJavaType(), secondLastAttribute);
        }

        if (rootAlias == null) {
            // TODO: not sure if other JPA providers support case sensitive queries like hibernate
            StringBuilder sb = new StringBuilder(attributeType.getSimpleName());
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            String alias = sb.toString();

            if (aliasManager.getAliasInfo(alias) == null) {
                rootAlias = alias;
            } else {
                rootAlias = aliasManager.generatePostfixedAlias(alias);
            }
        }
        // TODO: Implement treat support for correlated subqueries
        String treatType = null;
        JoinAliasInfo rootAliasInfo = new JoinAliasInfo(rootAlias, rootAlias, true, true, aliasManager);
        JoinNode rootNode = new JoinNode(correlationParent, correlationPathNoAlias, treatType, rootAliasInfo, attributeType, null);
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
        if (rootNodes.size() > 1) {
            throw new IllegalArgumentException(string);
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

    private void fillCollectionJoinsNodesRec(JoinNode node, Set<JoinNode> collectionNodes) {
        for (JoinTreeNode treeNode : node.getNodes().values()) {
            if (treeNode.isCollection()) {
                collectionNodes.addAll(treeNode.getJoinNodes().values());
                for (JoinNode childNode : treeNode.getJoinNodes().values()) {
                    fillCollectionJoinsNodesRec(childNode, collectionNodes);
                }
            }
        }
    }

    public JoinManager getParent() {
        return parent;
    }

    public SubqueryInitiatorFactory getSubqueryInitFactory() {
        return subqueryInitFactory;
    }

    Set<JoinNode> buildClause(StringBuilder sb, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes, boolean externalRepresenation, List<String> whereConjuncts) {
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
                ManagedType<?> type = metamodel.getManagedType(rootNode.getPropertyClass());
                final int attributeCount = rootNode.getAttributeCount();
                if (rootNode.getPropertyClass() != ValuesEntity.class) {
                    if (type instanceof EntityType<?>) {
                        sb.append(((EntityType) type).getName());
                    } else {
                        sb.append(type.getJavaType().getSimpleName());
                    }
                }
                sb.append("(VALUES");

                for (int valueNumber = 0; valueNumber < rootNode.getValueCount(); valueNumber++) {
                    sb.append(" (");

                    for (int j = 0; j < attributeCount; j++) {
                        sb.append("?,");
                    }

                    sb.setCharAt(sb.length() - 1, ')');
                    sb.append(',');
                }

                sb.setCharAt(sb.length() - 1, ')');
            } else {
                if (correlationParent != null) {
                    sb.append(correlationParent.getAliasInfo().getAlias());
                    sb.append('.');
                    sb.append(rootNode.getCorrelationPath());
                } else {
                    EntityType<?> type = metamodel.entity(rootNode.getPropertyClass());
                    sb.append(type.getName());
                }
            }

            sb.append(' ');

            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }

            sb.append(rootNode.getAliasInfo().getAlias());

            // TODO: not sure if needed since applyImplicitJoins will already invoke that
            rootNode.registerDependencies();
            applyJoins(sb, rootNode.getAliasInfo(), rootNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches);
            if (!rootNode.getEntityJoinNodes().isEmpty()) {
                // TODO: Fix this with #216
                boolean isCollection = true;
                if (mainQuery.jpaProvider.supportsEntityJoin()) {
                    applyJoins(sb, rootNode.getAliasInfo(), new ArrayList<JoinNode>(rootNode.getEntityJoinNodes()), isCollection, clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches);
                } else {
                    Set<JoinNode> entityNodes = rootNode.getEntityJoinNodes();
                    for (JoinNode entityNode : entityNodes) {
                        // Collect the join nodes referring to collections
                        if (collectCollectionJoinNodes && isCollection) {
                            collectionJoinNodes.add(entityNode);
                        }

                        sb.append(", ");

                        EntityType<?> type = metamodel.entity(entityNode.getPropertyClass());
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
                            queryGenerator.setQueryBuffer(tempSb);
                            SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.PREDICATE);
                            entityNode.getOnPredicate().accept(queryGenerator);
                            queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
                            whereConjuncts.add(tempSb.toString());
                        }

                        applyJoins(sb, entityNode.getAliasInfo(), entityNode.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches);
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

    private void renderJoinNode(StringBuilder sb, JoinAliasInfo joinBase, JoinNode node, String aliasPrefix, boolean renderFetches) {
        if (!renderedJoins.contains(node)) {
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
            if (node.isFetch() && renderFetches) {
                sb.append("FETCH ");
            }

            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }

            if (node.getTreatType() != null) {
                if (mainQuery.jpaProvider.supportsTreatJoin()) {
                    sb.append("TREAT(");
                    renderParentAlias(sb, node, joinBase.getAlias());
                    sb.append(node.getParentTreeNode().getRelationName());
                    sb.append(" AS ");
                    sb.append(node.getTreatType());
                    sb.append(") ");
                } else if (mainQuery.jpaProvider.supportsSubtypePropertyResolving()) {
                    sb.append(joinBase.getAlias()).append('.').append(node.getParentTreeNode().getRelationName()).append(' ');
                } else {
                    throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
                }
            } else if (node.getAliasInfo().isRootNode()) {
                sb.append(metamodel.entity(node.getPropertyClass()).getName()).append(' ');
            } else {
                renderParentAlias(sb, node, joinBase.getAlias());
                sb.append(node.getParentTreeNode().getRelationName()).append(' ');
            }

            if (aliasPrefix != null) {
                sb.append(aliasPrefix);
            }

            sb.append(node.getAliasInfo().getAlias());

            if (node.getOnPredicate() != null && !node.getOnPredicate().getChildren().isEmpty()) {
                sb.append(joinRestrictionKeyword);
                queryGenerator.setQueryBuffer(sb);
                SimpleQueryGenerator.BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = queryGenerator.setBooleanLiteralRenderingContext(SimpleQueryGenerator.BooleanLiteralRenderingContext.PREDICATE);
                node.getOnPredicate().accept(queryGenerator);
                queryGenerator.setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
            }
            renderedJoins.add(node);
        }
    }

    private void renderParentAlias(StringBuilder sb, JoinNode parentNode, String alias) {
        if (parentNode.getParentTreatType() != null) {
            if (mainQuery.jpaProvider.supportsRootTreatJoin()) {
                sb.append("TREAT(");
                sb.append(alias);
                sb.append(" AS ");
                sb.append(parentNode.getParentTreatType());
                sb.append(").");
            } else if (mainQuery.jpaProvider.supportsSubtypePropertyResolving()) {
                sb.append(alias).append('.');
            } else {
                throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
            }
        } else {
            sb.append(alias).append('.');
        }
    }

    private void renderReverseDependency(StringBuilder sb, JoinNode dependency, String aliasPrefix, boolean renderFetches) {
        if (dependency.getParent() != null) {
            renderReverseDependency(sb, dependency.getParent(), aliasPrefix, renderFetches);
            if (!dependency.getDependencies().isEmpty()) {
                markedJoinNodes.add(dependency);
                try {
                    for (JoinNode dep : dependency.getDependencies()) {
                        if (markedJoinNodes.contains(dep)) {
                            throw new IllegalStateException("Cyclic join dependency detected at absolute path ["
                                    + dep.getAliasInfo().getAbsolutePath() + "] with alias [" + dep.getAliasInfo().getAlias() + "]");
                        }
                        // render reverse dependencies
                        renderReverseDependency(sb, dep, aliasPrefix, renderFetches);
                    }
                } finally {
                    markedJoinNodes.remove(dependency);
                }
            }
            renderJoinNode(sb, dependency.getParent().getAliasInfo(), dependency, aliasPrefix, renderFetches);
        }
    }

    private void applyJoins(StringBuilder sb, JoinAliasInfo joinBase, Map<String, JoinTreeNode> nodes, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes, boolean renderFetches) {
        for (Map.Entry<String, JoinTreeNode> nodeEntry : nodes.entrySet()) {
            JoinTreeNode treeNode = nodeEntry.getValue();
            List<JoinNode> stack = new ArrayList<JoinNode>();
            stack.addAll(treeNode.getJoinNodes().descendingMap().values());

            applyJoins(sb, joinBase, stack, treeNode.isCollection(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches);
        }
    }

    private void applyJoins(StringBuilder sb, JoinAliasInfo joinBase, List<JoinNode> stack, boolean isCollection, Set<ClauseType> clauseExclusions, String aliasPrefix, boolean collectCollectionJoinNodes, boolean renderFetches) {
        while (!stack.isEmpty()) {
            JoinNode node = stack.remove(stack.size() - 1);
            // If the clauses in which a join node occurs are all excluded or the join node is not mandatory for the cardinality, we skip it
            if (!clauseExclusions.isEmpty() && clauseExclusions.containsAll(node.getClauseDependencies()) && !node.isCardinalityMandatory()) {
                continue;
            }

            stack.addAll(node.getEntityJoinNodes());

            // We have to render any dependencies this join node has before actually rendering itself
            if (!node.getDependencies().isEmpty()) {
                renderReverseDependency(sb, node, aliasPrefix, renderFetches);
            }

            // Collect the join nodes referring to collections
            if (collectCollectionJoinNodes && isCollection) {
                collectionJoinNodes.add(node);
            }

            // Finally render this join node
            renderJoinNode(sb, joinBase, node, aliasPrefix, renderFetches);

            // Render child nodes recursively
            if (!node.getNodes().isEmpty()) {
                applyJoins(sb, node.getAliasInfo(), node.getNodes(), clauseExclusions, aliasPrefix, collectCollectionJoinNodes, renderFetches);
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

    private boolean isExternal(Expression path, PathElementExpression firstElem) {
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

            // an external select alias must not be dereferenced
            if (aliasInfo instanceof SelectInfo) {
                throw new ExternalAliasDereferencingException("Start alias [" + startAlias + "] of path [" + path.toString()
                        + "] is external and must not be dereferenced");
            }

            // the alias is external so we do not have to treat it
            return true;
        } else if (aliasInfo.getAliasOwner() == aliasManager) {
            // the alias originates from the current query builder an is therefore not external
            return false;
        } else {
            throw new IllegalStateException("Alias [" + aliasInfo.getAlias() + "] originates from an unknown query");
        }
    }

    private boolean isJoinableSelectAlias(PathExpression pathExpr, boolean fromSelect, boolean fromSubquery) {
        // We can skip this check if the first element is not a simple property
        if (!(pathExpr.getExpressions().get(0) instanceof PropertyExpression)) {
            return false;
        }

        boolean singlePathElement = pathExpr.getExpressions().size() == 1;
        String startAlias = pathExpr.getExpressions().get(0).toString();

        AliasInfo aliasInfo = aliasManager.getAliasInfo(startAlias);
        if (aliasInfo == null) {
            return false;
        }

        if (aliasInfo instanceof SelectInfo && !fromSelect && !fromSubquery) {
            // select alias
            if (!singlePathElement) {
                throw new IllegalStateException("Path starting with select alias not allowed");
            }

            // might be joinable
            return true;
        }

        return false;
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
        JoinNode entityJoinNode = new JoinNode(baseNode, null, null, joinAliasInfo, type, entityType.getJavaType(), null);
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
        JoinResult result;
        JoinNode current;
        if (expr instanceof PathExpression) {
            PathExpression pathExpression = (PathExpression) expr;

            if (isExternal(pathExpression) || isJoinableSelectAlias(pathExpression, false, false)) {
                throw new IllegalArgumentException("No external path or select alias allowed in join path");
            }

            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            elementExpr = pathElements.get(pathElements.size() - 1);
            result = implicitJoin(null, pathExpression, 0, pathElements.size() - 1);
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
                result = implicitJoin(null, pathExpression, 0, pathElements.size() - 1);
                current = result.baseNode;
            } else {
                throw new IllegalArgumentException("Unexpected expression type[" + expression.getClass().getSimpleName() + "] in treat expression: " + treatExpression);
            }
        } else {
            throw new IllegalArgumentException("Join path [" + path + "] is not a path");
        }

        if (elementExpr instanceof ArrayExpression) {
            throw new IllegalArgumentException("Array expressions are not allowed!");
        } else {
            String treatType = null;
            if (expr instanceof TreatExpression) {
                treatType = ((TreatExpression) expr).getType();
            }

            List<String> joinRelationAttributes = result.addToList(new ArrayList<String>());
            joinRelationAttributes.add(elementExpr.toString());
            current = current == null ? getRootNodeOrFail("Could not join path [" + path + "] because it did not use an absolute path but multiple root nodes are available!") : current;
            result = createOrUpdateNode(current, result.typeName, joinRelationAttributes, treatType, alias, type, false, defaultJoin);
        }

        if (fetch) {
            fetchPath(result.baseNode);
        }

        return result.baseNode;
    }

    public void implicitJoin(Expression expression, boolean objectLeafAllowed, String targetType, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired) {
        implicitJoin(expression, objectLeafAllowed, targetType, fromClause, fromSubquery, fromSelectAlias, joinRequired, false);
    }

    @SuppressWarnings("checkstyle:methodlength")
    public void implicitJoin(Expression expression, boolean objectLeafAllowed, String targetTypeName, ClauseType fromClause, boolean fromSubquery, boolean fromSelectAlias, boolean joinRequired, boolean fetch) {
        PathExpression pathExpression;
        if (expression instanceof PathExpression) {
            pathExpression = (PathExpression) expression;

            // If joinable select alias, it is guaranteed to have only a single element
            if (isJoinableSelectAlias(pathExpression, fromClause == ClauseType.SELECT, fromSubquery)) {
                String alias = pathExpression.getExpressions().get(0).toString();
                Expression expr = ((SelectInfo) aliasManager.getAliasInfo(alias)).getExpression();

                // this check is necessary to prevent infinite recursion in the case of e.g. SELECT name AS name
                if (!fromSelectAlias) {
                    // we have to do this implicit join because we might have to adjust the selectOnly flag in the referenced join nodes
                    implicitJoin(expr, true, targetTypeName, fromClause, fromSubquery, true, joinRequired);
                }
                return;
            } else if (isExternal(pathExpression)) {
                // try to set base node and field for the external expression based
                // on existing joins in the super query
                parent.implicitJoin(pathExpression, true, targetTypeName, fromClause, true, fromSelectAlias, joinRequired);
                return;
            }

            // First try to implicit join indices of array expressions since we will need their base nodes
            List<PathElementExpression> pathElements = pathExpression.getExpressions();
            int pathElementSize = pathElements.size();
            for (int i = 0; i < pathElementSize; i++) {
                PathElementExpression pathElem = pathElements.get(i);
                if (pathElem instanceof ArrayExpression) {
                    implicitJoin(((ArrayExpression) pathElem).getIndex(), false, null, fromClause, fromSubquery, fromSelectAlias, joinRequired);
                }
            }

            PathElementExpression elementExpr = pathElements.get(pathElements.size() - 1);
            boolean singleValuedAssociationIdExpression = false;
            JoinNode current = null;
            String currentTreatType = null;
            List<String> resultFields = new ArrayList<String>();
            JoinResult currentResult;

            JoinNode possibleRoot;
            int startIndex = 0;

            // Skip root speculation if this is just a single element path
            if (pathElements.size() > 1 && (possibleRoot = getRootNode(pathElements.get(0))) != null) {
                startIndex = 1;
                current = possibleRoot;
            }

            if (mainQuery.jpaProvider.supportsSingleValuedAssociationIdExpressions() && pathElements.size() > startIndex + 1) {
                int maybeSingularAssociationIndex = pathElements.size() - 2;
                int maybeSingularAssociationIdIndex = pathElements.size() - 1;
                currentResult = implicitJoin(current, pathExpression, startIndex, maybeSingularAssociationIndex);
                current = currentResult.baseNode;
                resultFields = currentResult.addToList(resultFields);

                singleValuedAssociationIdExpression = isSingleValuedAssociationId(currentResult, pathElements);

                if (singleValuedAssociationIdExpression) {
                } else {
                    if (currentResult.hasField()) {
                        // currentResult.typeName?
                        // Redo the joins for embeddables by moving the start index back
                        currentResult = implicitJoin(current, pathExpression, maybeSingularAssociationIndex - currentResult.fields.size(), maybeSingularAssociationIdIndex);
                        if (currentResult.fields != resultFields) {
                            resultFields.clear();
                        }
                    } else {
                        currentResult = implicitJoin(current, pathExpression, maybeSingularAssociationIndex, maybeSingularAssociationIdIndex);
                    }

                    current = currentResult.baseNode;
                    currentTreatType = currentResult.typeName;
                    resultFields = currentResult.addToList(resultFields);
                }
            } else {
                currentResult = implicitJoin(current, pathExpression, startIndex, pathElements.size() - 1);
                current = currentResult.baseNode;
                currentTreatType = currentResult.typeName;
                resultFields = currentResult.addToList(resultFields);
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
                        implicitJoin(selectExpr, objectLeafAllowed, null, fromClause, fromSubquery, true, joinRequired);
                    }
                    PathExpression selectPathExpr = (PathExpression) selectExpr;
                    result = new JoinResult((JoinNode) selectPathExpr.getBaseNode(), Arrays.asList(selectPathExpr.getField()));
                } else {
                    JoinNode pathJoinNode = ((JoinAliasInfo) aliasInfo).getJoinNode();
                    if (targetTypeName != null) {
                        // Treated root path
                        ManagedType<?> targetType = metamodel.managedType(targetTypeName);
                        result = new JoinResult(pathJoinNode, null, targetTypeName);
                    } else {
                        // Naked join alias usage like in "KEY(joinAlias)"
                        result = new JoinResult(pathJoinNode, null);
                    }
                }
            } else {
                // current might be null
                if (current == null) {
                    if (rootNodes.size() > 1) {
                        throw new IllegalArgumentException("Could not join path [" + expression + "] because it did not use an absolute path but multiple root nodes are available!");
                    }

                    current = rootNodes.get(0);
                }

                if (singleValuedAssociationIdExpression) {
                    String associationName = pathElements.get(pathElements.size() - 2).toString();
                    AliasInfo a = null;
                    JoinTreeNode treeNode;

                    if (currentResult.hasField()) {
                        associationName = currentResult.joinFields(associationName);
                    } else {
                        a = aliasManager.getAliasInfoForBottomLevel(associationName);
                    }

                    if (a != null) {
                        result = new JoinResult(((JoinAliasInfo) a).getJoinNode(), Arrays.asList(elementExpr.toString()));
                    } else {
                        treeNode = current.getNodes().get(associationName);

                        if (treeNode != null && treeNode.getDefaultNode() != null) {
                            result = new JoinResult(treeNode.getDefaultNode(), Arrays.asList(elementExpr.toString()));
                        } else {
                            result = new JoinResult(current, Arrays.asList(associationName, elementExpr.toString()));
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
                        currentResult = createOrUpdateNode(current, currentTreatType, Arrays.asList(joinRelationName), null, joinAlias, null, true, false);
                        current = currentResult.baseNode;
                        // TODO: Not sure if necessary
                        if (currentResult.hasField()) {
                            throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part [" + currentResult.joinFields()
                                    + "]");
                        }
                        generateAndApplyOnPredicate(current, arrayExpr);
                    }

                    result = new JoinResult(current, null);
                } else if (!pathExpression.isUsedInCollectionFunction()) {
                    if (resultFields.isEmpty()) {
                        result = implicitJoinSingle(current, currentTreatType, elementExpr.toString(), objectLeafAllowed, joinRequired);
                    } else {
                        resultFields.add(elementExpr.toString());

                        if (!validPath(JpaUtils.getManagedType(metamodel, current.getPropertyClass(), currentTreatType), resultFields)) {
                            throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part ["
                                    + StringUtils.join(".", resultFields) + "]");
                        }

                        result = implicitJoinSingle(current, currentTreatType, StringUtils.join(".", resultFields), objectLeafAllowed, joinRequired);
                    }
                } else {
                    if (resultFields.isEmpty()) {
                        result = new JoinResult(current, Arrays.asList(elementExpr.toString()));
                    } else {
                        resultFields.add(elementExpr.toString());

                        if (!validPath(JpaUtils.getManagedType(metamodel, current.getPropertyClass(), currentTreatType), resultFields)) {
                            throw new IllegalArgumentException("The join path [" + pathExpression + "] has a non joinable part ["
                                    + StringUtils.join(".", resultFields) + "]");
                        }

                        result = new JoinResult(current, resultFields);
                    }
                }
            }

            if (fetch) {
                fetchPath(result.baseNode);
            }

            // Don't forget to update the clause dependencies!!
            if (fromClause != null) {
                updateClauseDependencies(result.baseNode, fromClause);
            }

            if (result.isLazy()) {
                pathExpression.setPathReference(new LazyPathReference(result.baseNode, result.joinFields(), result.typeName));
            } else {
                pathExpression.setPathReference(new SimplePathReference(result.baseNode, result.joinFields(), result.typeName));
            }

            if (result.hasTreatedSubpath) {
                pathExpression.setHasTreatedSubpath(true);
            }
        } else if (expression instanceof FunctionExpression) {
            List<Expression> expressions = ((FunctionExpression) expression).getExpressions();
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                implicitJoin(expressions.get(i), objectLeafAllowed, null, fromClause, fromSubquery, fromSelectAlias, joinRequired);
            }
        } else if (expression instanceof ArrayExpression || expression instanceof GeneralCaseExpression || expression instanceof TreatExpression) {
            // NOTE: I haven't found a use case for this yet, so I'd like to throw an exception instead of silently not supporting this
            throw new IllegalArgumentException("Unsupported expression type for implicit joining found: " + expression.getClass());
        }
    }

    private static class LazyPathReference implements PathReference {
        private final JoinNode baseNode;
        private final String field;
        private final String typeName;

        public LazyPathReference(JoinNode baseNode, String field, String typeName) {
            this.baseNode = baseNode;
            this.field = field;
            this.typeName = typeName;
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
        public String getTreatTypeName() {
            return typeName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((baseNode == null) ? 0 : baseNode.hashCode());
            result = prime * result + ((field == null) ? 0 : field.hashCode());
            result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
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
            if (typeName == null) {
                if (other.getTreatTypeName() != null) {
                    return false;
                }
            } else if (!typeName.equals(other.getTreatTypeName())) {
                return false;
            }
            return true;
        }
    }

    private boolean validPath(ManagedType<?> t, List<String> pathElements) {
        for (int i = 0; i < pathElements.size(); i++) {
            String element = pathElements.get(i);
            Set<Attribute<?, ?>> attributes = JpaUtils.getAttributesPolymorphic(metamodel, t, element);

            if (attributes.isEmpty()) {
                return false;
            } else if (attributes.size() == 1) {
                t = JpaUtils.getManagedTypeOrNull(metamodel, attributes.iterator().next().getJavaType());
            } else {
                // Only consider a path valid when all possible paths along the polymorphic hierarchy are valid
                for (Attribute<?, ?> attr : attributes) {
                    if (!validPath(JpaUtils.getManagedTypeOrNull(metamodel, attr.getJavaType()), pathElements.subList(i, pathElements.size() - 1))) {
                        return false;
                    }
                }

                return true;
            }
        }

        return true;
    }

    private boolean isSingleValuedAssociationId(JoinResult joinResult, List<PathElementExpression> pathElements) {
        JoinNode parent = joinResult.baseNode;
        String parentTypeName = joinResult.typeName;
        int maybeSingularAssociationIndex = pathElements.size() - 2;
        int maybeSingularAssociationIdIndex = pathElements.size() - 1;
        ManagedType<?> baseType;
        Set<Attribute<?, ?>> maybeSingularAssociationAttributes;
        String maybeSingularAssociationName = getSimpleName(pathElements.get(maybeSingularAssociationIndex));

        if (parent == null) {
            // This is the case when we have exactly 2 path elements
            AliasInfo a = aliasManager.getAliasInfo(maybeSingularAssociationName);

            if (a == null) {
                // if the path element is no alias we can do some optimizations
                baseType = metamodel.managedType(getRootNodeOrFail("Ambiguous join path [" + maybeSingularAssociationName + "] because of multiple root nodes!").getPropertyClass());
                maybeSingularAssociationAttributes = JpaUtils.getAttributesPolymorphic(metamodel, baseType, maybeSingularAssociationName);
            } else if (!(a instanceof JoinAliasInfo)) {
                throw new IllegalArgumentException("Can't dereference select alias in the expression!");
            } else {
                // If there is a JoinAliasInfo for the path element, we have to use the alias
                // So we return false in order to signal that a normal implicit join should be done
                return false;
            }

        } else {
            Class<?> parentClass = parent.getPropertyClass();
            baseType = JpaUtils.getManagedType(metamodel, parentClass, parentTypeName);

            if (joinResult.hasField()) {
                Attribute<?, ?> fieldAttribute = JpaUtils.getPolymorphicAttribute(metamodel, baseType, joinResult.joinFields());
                baseType = metamodel.managedType(fieldAttribute.getJavaType());
            }

            maybeSingularAssociationAttributes = JpaUtils.getAttributesPolymorphic(metamodel, baseType, maybeSingularAssociationName);
        }

        if (maybeSingularAssociationAttributes.isEmpty()) {
            return false;
        }

        for (Attribute<?, ?> maybeSingularAssociation : maybeSingularAssociationAttributes) {
            if (maybeSingularAssociation.getPersistentAttributeType() != Attribute.PersistentAttributeType.MANY_TO_ONE
                // TODO: to be able to support ONE_TO_ONE we need to know where the FK is
                // && maybeSingularAssociation.getPersistentAttributeType() != Attribute.PersistentAttributeType.ONE_TO_ONE
            ) {
                return false;
            }

            Class<?> maybeSingularAssociationClass = JpaUtils.resolveFieldClass(baseType.getJavaType(), maybeSingularAssociation);
            ManagedType<?> maybeSingularAssociationType = metamodel.managedType(maybeSingularAssociationClass);
            String maybeSingularAssociationIdName = getSimpleName(pathElements.get(maybeSingularAssociationIdIndex));
            Set<Attribute<?, ?>> maybeSingularAssociationIdAttributes = JpaUtils.getAttributesPolymorphic(metamodel, maybeSingularAssociationType, maybeSingularAssociationIdName);

            if (maybeSingularAssociationIdAttributes.isEmpty()) {
                return false;
            }

            for (Attribute<?, ?> maybeSingularAssociationId : maybeSingularAssociationIdAttributes) {
                if (!(maybeSingularAssociationId instanceof SingularAttribute<?, ?>)) {
                    return false;
                }

                if (!((SingularAttribute<?, ?>) maybeSingularAssociationId).isId()) {
                    return false;
                }
            }
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
        keyPath.setPathReference(new SimplePathReference(joinNode, null, null));
        final String accessFunction;
        Attribute<?, ?> arrayBaseAttribute = joinNode.getParentTreeNode().getAttribute();
        if (arrayBaseAttribute instanceof ListAttribute<?, ?>) {
            accessFunction = "INDEX";
        } else {
            accessFunction = "KEY";
        }
        FunctionExpression keyExpression = new FunctionExpression(accessFunction, Arrays.asList((Expression) keyPath));
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

    private JoinResult implicitJoin(JoinNode current, PathExpression pathExpression, int start, int end) {
        List<PathElementExpression> pathElements = pathExpression.getExpressions();
        List<String> resultFields = new ArrayList<String>();
        String currentTargetType = null;
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

                current = current == null ? getRootNodeOrFail("Ambiguous join path [" + joinRelationName + "] because of multiple root nodes!") : current;
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
                    final JoinResult result = createOrUpdateNode(current, currentTargetType, joinRelationAttributes, null, joinAlias, null, true, false);
                    current = result.baseNode;
                    resultFields = result.addToList(resultFields);
                    generateAndApplyOnPredicate(current, arrayExpr);
                }

                // Reset target type
                currentTargetType = null;
            } else if (elementExpr instanceof TreatExpression) {
                if (i != 0 || current != null) {
                    throw new IllegalArgumentException("A treat expression should be the first element in a path!");
                }
                TreatExpression treatExpression = (TreatExpression) elementExpr;
                boolean fromSubquery = false;
                boolean fromSelectAlias = false;
                boolean joinRequired = false;
                boolean fetch = false;

                // TODO: reuse existing treated join node or create one? not sure if it wasn't better to just pass it through to the persistence provider
                if (treatExpression.getExpression() instanceof PathExpression) {
                    PathExpression treatedPathExpression = (PathExpression) treatExpression.getExpression();
                    implicitJoin(treatedPathExpression, true, treatExpression.getType(), null, fromSubquery, fromSelectAlias, true, fetch);
                    JoinNode treatedJoinNode = (JoinNode) treatedPathExpression.getBaseNode();
                    current = treatedJoinNode;
                    currentTargetType = treatExpression.getType();
                } else {
                    throw new UnsupportedOperationException("Unsupported treated expression type: " + treatExpression.getExpression().getClass());
                }
            } else if (pathElements.size() == 1 && (aliasInfo = aliasManager.getAliasInfoForBottomLevel(elementExpr.toString())) != null) {
                if (aliasInfo instanceof SelectInfo) {
                    throw new IllegalArgumentException("Can't dereference a select alias");
                } else {
                    // Join alias usage like in "joinAlias.relationName"
                    current = ((JoinAliasInfo) aliasInfo).getJoinNode();
                }
                // Reset target type
                currentTargetType = null;
            } else {
                if (!resultFields.isEmpty()) {
                    resultFields.add(elementExpr.toString());
                    JoinResult currentResult = createOrUpdateNode(current, currentTargetType, resultFields, null, null, null, true, true);
                    current = currentResult.baseNode;
                    if (!currentResult.hasField()) {
                        resultFields.clear();
                    }
                    // Reset target type
                    currentTargetType = null;
                } else {
                    final JoinResult result = implicitJoinSingle(current, currentTargetType, elementExpr.toString());
                    if (current != result.baseNode) {
                        current = result.baseNode;
                        // Reset target type
                        currentTargetType = null;
                    }
                    resultFields = result.addToList(resultFields);
                }

            }
        }

        if (resultFields.isEmpty()) {
            return new JoinResult(current, null, currentTargetType);
        } else {
            return new JoinResult(current, resultFields, currentTargetType);
        }
    }

    private JoinResult implicitJoinSingle(JoinNode baseNode, String baseNodeTreatType, String attributeName) {
        if (baseNode == null) {
            // When no base is given, check if the attribute name is an alias
            AliasInfo aliasInfo = aliasManager.getAliasInfoForBottomLevel(attributeName);
            if (aliasInfo != null && aliasInfo instanceof JoinAliasInfo) {
                // if it is, we can just return the join node
                return new JoinResult(((JoinAliasInfo) aliasInfo).getJoinNode(), null);
            }
        }

        // If we have no base node, root is assumed
        if (baseNode == null) {
            baseNode = getRootNodeOrFail("Ambiguous join path [" + attributeName + "] because of multiple root nodes!");
        }

        // check if the path is joinable, assuming it is relative to the root (implicit root prefix)
        return createOrUpdateNode(baseNode, baseNodeTreatType, Arrays.asList(attributeName), null, null, null, true, true);
    }

    private JoinResult implicitJoinSingle(JoinNode baseNode, String treatTypeName, String attributeName, boolean objectLeafAllowed, boolean joinRequired) {
        JoinNode newBaseNode;
        String field;
        boolean hasTreatedSubpath = false;
        boolean lazy = false;
        // The given path may be relative to the root or it might be an alias
        if (objectLeafAllowed) {
            Class<?> baseNodeClass = baseNode.getPropertyClass();
            String typeName;
            ManagedType<?> baseNodeType;

            if (treatTypeName != null) {
                typeName = treatTypeName;
                baseNodeType = metamodel.managedType(treatTypeName);
            } else {
                typeName = baseNodeClass.getSimpleName();
                baseNodeType = metamodel.managedType(baseNodeClass);
            }

            Attribute<?, ?> attr = JpaUtils.getSimpleAttributeForImplicitJoining(metamodel, baseNodeType, attributeName);
            if (attr == null) {
                throw new IllegalArgumentException("Field with name '" + attributeName + "' was not found within managed type " + typeName);
            }

            if (joinRequired || attr.isCollection()) {
                final JoinResult newBaseNodeResult = implicitJoinSingle(baseNode, treatTypeName, attributeName);
                newBaseNode = newBaseNodeResult.baseNode;
                // check if the last path element was also joined
                if (newBaseNode != baseNode) {
                    field = null;
                } else {
                    hasTreatedSubpath = treatTypeName != null;
                    field = attributeName;
                }
            } else {
                newBaseNode = baseNode;
                field = attributeName;
                lazy = true;
                hasTreatedSubpath = treatTypeName != null;
            }
        } else {
            Class<?> baseNodeType = baseNode.getPropertyClass();
            Attribute<?, ?> attr = JpaUtils.getSimpleAttributeForImplicitJoining(metamodel, metamodel.managedType(baseNodeType), attributeName);
            if (attr == null) {
                throw new IllegalArgumentException("Field with name " + attributeName + " was not found within class " + baseNodeType.getName());
            }
            if (JpaUtils.isJoinable(attr)) {
                throw new IllegalArgumentException("No object leaf allowed but " + attributeName + " is an object leaf");
            }
            newBaseNode = baseNode;
            field = attributeName;
        }
        return new JoinResult(newBaseNode, field == null ? null : Arrays.asList(field), lazy, hasTreatedSubpath);
    }

    private void updateClauseDependencies(JoinNode baseNode, ClauseType clauseDependency) {
        JoinNode current = baseNode;
        while (current != null) {
            // setSelectOnlyFalse for all JoinNodes that are used in the WITH clause of the current node
            for (JoinNode dependency : current.getDependencies()) {
                updateClauseDependencies(dependency, clauseDependency);
            }

            current.getClauseDependencies().add(clauseDependency);
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

    private JoinResult createOrUpdateNode(JoinNode baseNode, String baseNodeTreatType, List<String> joinRelationAttributes, String treatType, String alias, JoinType joinType, boolean implicit, boolean defaultJoin) {
        Class<?> baseNodeType = baseNode.getPropertyClass();
        ManagedType<?> type = metamodel.managedType(baseNodeType);
        String joinRelationName = StringUtils.join(".", joinRelationAttributes);
        AttributeJoinResult attrJoinResult = JpaUtils.getAttributeForJoining(metamodel, type, joinRelationName);
        Attribute<?, ?> attr = attrJoinResult.getAttribute();
        if (attr == null) {
            throw new IllegalArgumentException("Field with name " + joinRelationName + " was not found within class " + baseNodeType.getName());
        }
        Class<?> resolvedFieldClass = JpaUtils.resolveFieldClass(attrJoinResult.getContainingClass(), attr);

        if (!JpaUtils.isJoinable(attr)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(new StringBuilder("Field with name ").append(joinRelationName)
                        .append(" of class ")
                        .append(baseNodeType.getName())
                        .append(" is parseable and therefore it has not to be fetched explicitly.")
                        .toString());
            }
            return new JoinResult(baseNode, joinRelationAttributes);
        }

        if (implicit) {
            String aliasToUse = alias == null ? attr.getName() : alias;
            alias = aliasManager.generatePostfixedAlias(aliasToUse);
        }

        if (joinType == null) {
            joinType = getModelAwareType(baseNode, attr);
        }

        if (baseNodeTreatType != null) {
            // Verify it's a valid type
            metamodel.managedType(baseNodeTreatType).getJavaType();
        }
        if (treatType != null) {
            // Verify it's a valid type
            metamodel.managedType(treatType).getJavaType();
        }

        JoinNode newNode = getOrCreate(baseNode, baseNodeTreatType, joinRelationName, resolvedFieldClass, treatType, alias, joinType, "Ambiguous implicit join", implicit, defaultJoin, attr);

        return new JoinResult(newNode, null);
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

    private JoinNode getOrCreate(JoinNode baseNode, String baseNodeTreatType, String joinRelationName, Class<?> joinRelationClass, String treatType, String alias, JoinType type, String errorMessage, boolean implicit, boolean defaultJoin, Attribute<?, ?> attribute) {
        JoinTreeNode treeNode = baseNode.getOrCreateTreeNode(joinRelationName, attribute);
        JoinNode node = treeNode.getJoinNode(alias, defaultJoin);
        String currentJoinPath = baseNode.getAliasInfo().getAbsolutePath() + "." + joinRelationName;
        if (node == null) {
            // a join node for the join relation does not yet exist
            checkAliasIsAvailable(alias, currentJoinPath, errorMessage);

            // the alias might have to be postfixed since it might already exist in parent queries
            if (implicit && aliasManager.getAliasInfo(alias) != null) {
                alias = aliasManager.generatePostfixedAlias(alias);
            }

            JoinAliasInfo newAliasInfo = new JoinAliasInfo(alias, currentJoinPath, implicit, false, aliasManager);
            aliasManager.registerAliasInfo(newAliasInfo);
            node = new JoinNode(baseNode, treeNode, baseNodeTreatType, newAliasInfo, type, joinRelationClass, treatType);
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

            if (treatType != null) {
                if (!treatType.equals(node.getTreatType())) {
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
     * Base node will NOT be fetched
     *
     * @param node
     */
    private void fetchPath(JoinNode node) {
        JoinNode currentNode = node;
        while (currentNode != null) {
            currentNode.setFetch(true);
            // fetches implicitly need to be selected
            currentNode.getClauseDependencies().add(ClauseType.SELECT);
            currentNode = currentNode.getParent();
        }
    }

    // TODO: needs equals-hashCode implementation
    private static class JoinResult {

        final JoinNode baseNode;
        final List<String> fields;
        final String typeName;
        final boolean lazy;
        final boolean hasTreatedSubpath;

        public JoinResult(JoinNode baseNode, List<String> fields) {
            this(baseNode, fields, null);
        }

        public JoinResult(JoinNode baseNode, List<String> fields, String typeName) {
            this.baseNode = baseNode;
            this.fields = fields;
            this.typeName = typeName;
            this.lazy = false;
            this.hasTreatedSubpath = false;
        }

        public JoinResult(JoinNode baseNode, List<String> fields, boolean lazy, boolean hasTreatedSubpath) {
            this(baseNode, fields, null, lazy, hasTreatedSubpath);
        }

        public JoinResult(JoinNode baseNode, List<String> fields, String typeName, boolean lazy, boolean hasTreatedSubpath) {
            this.baseNode = baseNode;
            this.fields = fields;
            this.typeName = typeName;
            this.lazy = lazy;
            this.hasTreatedSubpath = hasTreatedSubpath;
        }

        private boolean hasField() {
            return fields != null && !fields.isEmpty();
        }

        private String joinFields(String field) {
            if (fields == null || fields.isEmpty()) {
                return null;
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
                public void visit(FunctionExpression expression) {
                    boolean old = isKeyFunction;
                    this.isKeyFunction = com.blazebit.persistence.impl.util.ExpressionUtils.isKeyFunction(expression) || com.blazebit.persistence.impl.util.ExpressionUtils.isIndexFunction(expression);
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
