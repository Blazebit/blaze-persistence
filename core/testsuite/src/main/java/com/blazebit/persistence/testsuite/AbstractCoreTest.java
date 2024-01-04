/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.testsuite;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.ConfigurationProperties;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.impl.function.count.AbstractCountFunction;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.util.TypeUtils;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.testsuite.base.AbstractPersistenceTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.PolymorphicBase;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.function.ConcatenateFunction;
import com.blazebit.persistence.testsuite.function.ZeroFunction;
import com.blazebit.persistence.testsuite.macro.PrefixJpqlMacro;
import com.blazebit.persistence.testsuite.tx.TxSupport;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.testsuite.tx.TxWork;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class AbstractCoreTest extends AbstractPersistenceTest {

    protected static final JpaProvider STATIC_JPA_PROVIDER;
    private static final EntityManagerFactoryIntegrator STATIC_EMF_INTEGRATOR;
    private static final String ON_CLAUSE;

    protected String dbms;

    static {
        STATIC_EMF_INTEGRATOR = ServiceLoader.load(EntityManagerFactoryIntegrator.class).iterator().next();
        STATIC_JPA_PROVIDER = STATIC_EMF_INTEGRATOR.getJpaProviderFactory(null).createJpaProvider(null);
        ON_CLAUSE = STATIC_JPA_PROVIDER.getOnClause();
    }

    @Override
    protected boolean requiresCriteriaBuilderConfigurationCustomization() {
        return true;
    }

    @Override
    protected void configure(CriteriaBuilderConfiguration config) {
        super.configure(config);
        config.registerFunction(new JpqlFunctionGroup("zero", new ZeroFunction()));
        config.registerFunction(new JpqlFunctionGroup("concatenate", new ConcatenateFunction()));

        config.registerMacro("prefix", new PrefixJpqlMacro());

        dbms = config.getEntityManagerIntegrators().get(0).getDbms(emf);
        if ("postgresql".equals(dbms)) {
            config.setProperty(ConfigurationProperties.RETURNING_CLAUSE_CASE_SENSITIVE, "false");
        }
    }
    
    /**
     * Only use this for parameters that have more than 1 value!
     * 
     * @param name
     * @return 
     */
    protected String listParameter(String name) {
        // Workaround for HHH-7407
        if (jpaProvider.needsBracketsForListParameter()) {
            return "(:" + name + ")";
        } else {
            return ":" + name;
        }
    }

    protected String joinAliasValue(String alias) {
        return joinAliasValue(alias, null);
    }

    protected String joinAliasValue(String alias, String field) {
        return joinAliasValue(jpaProvider, alias, field);
    }

    private static String joinAliasValue(JpaProvider provider, String alias, String field) {
        if (provider.getCollectionValueFunction() != null && (field == null || provider.supportsCollectionValueDereference())) {
            alias = provider.getCollectionValueFunction() + "(" + alias + ")";
        }

        if (field == null) {
            return alias;
        }
        return alias + "." + field;
    }

    protected String onClause(String expression) {
        return " " + ON_CLAUSE + " (" + expression + ")";
    }
    
    protected String booleanConditionalExpression(boolean value) {
        return jpaProvider.getBooleanConditionalExpression(value);
    }

    protected String escapeCharacter(char character) {
        return jpaProvider.escapeCharacter(character);
    }

    protected String noEscape() {
        if (!jpaProvider.supportsLikePatternEscape() && dbmsDialect.getDefaultEscapeCharacter() != null) {
            return " ESCAPE ''";
        }
        return "";
    }

    protected String renderNullPrecedence(String expression, String resolvedExpression, String order, String nulls) {
        StringBuilder sb = new StringBuilder();
        jpaProvider.renderNullPrecedence(sb, expression, resolvedExpression, order, nulls);
        return sb.toString();
    }

    protected String renderNullPrecedenceGroupBy(String resolvedExpression, String order, String nulls) {
        if (jpaProvider.supportsNullPrecedenceExpression()) {
            return resolvedExpression;
        }

        StringBuilder sb = new StringBuilder(resolvedExpression.length() + 36);
        sb.append("CASE WHEN ");
        sb.append(resolvedExpression);
        sb.append(" IS NULL THEN ");
        if ("FIRST".equals(nulls)) {
            sb.append("0 ELSE 1 END");
        } else {
            sb.append("1 ELSE 0 END");
        }
        return sb.toString();
    }

    protected String groupBy(String... groupBys) {
        return StringUtils.join(", ", new LinkedHashSet<>(Arrays.asList(groupBys)));
    }

    protected String groupByPathExpressions(String... pathExpressions) {
        if (cbf.getService(DbmsDialect.class).supportsGroupByExpressionInHavingMatching()) {
            return "";
        }
        return ", " + StringUtils.join(", ", new LinkedHashSet<>(Arrays.asList(pathExpressions)));
    }

    protected String countStar() {
        if (jpaProvider.supportsCountStar()) {
            return "COUNT(*)";
        } else if (jpaProvider.supportsCustomFunctions()) {
            return function("COUNT_STAR");
        } else {
            return "COUNT(1)";
        }
    }

    protected String correlationPath(String correlationPath, Class<?> entityClass, String alias, String predicate) {
        return correlationPath(null, correlationPath, entityClass, alias, predicate, "");
    }

    protected String correlationPath(String correlationPath, Class<?> entityClass, String alias, String predicate, String normalSuffix) {
        return correlationPath(null, correlationPath, entityClass, alias, predicate, normalSuffix);
    }

    protected String correlationPath(Class<?> ownerEntity, String correlationPath, String alias, String predicate) {
        return correlationPath(ownerEntity, correlationPath, null, alias, predicate, "");
    }

    protected String correlationPath(Class<?> ownerEntity, String correlationPath, String alias, String predicate, String normalSuffix) {
        return correlationPath(ownerEntity, correlationPath, null, alias, predicate, normalSuffix);
    }

    protected String correlationPath(Class<?> ownerEntity, String correlationPath, Class<?> entityClass, String alias, String predicate, String normalSuffix) {
        if (jpaProvider.needsCorrelationPredicateWhenCorrelatingWithWhereClause()) {
            String attribute = correlationPath.substring(correlationPath.indexOf('.') + 1);
            if (ownerEntity != null) {
                return ownerEntity.getSimpleName() + " _synthetic_" + alias + " JOIN _synthetic_" + alias + "." + attribute + " " + alias + " WHERE _synthetic_" + alias + "." + predicate;
            } else {
                return entityClass.getSimpleName() + " " + alias + " WHERE " + alias + "." + predicate;
            }
        } else {
            return correlationPath + " " + alias + normalSuffix;
        }
    }

    protected String countPaginated(String string, boolean distinct) {
        StringBuilder sb = new StringBuilder(20 + string.length());
        sb.append(countTupleDistinct(string));

        if (!distinct) {
            String countStar = countStar();
            countStar = countStar.substring(0, countStar.length() - 1);
            for (int i = 0; i < sb.length() - 1; i++) {
                if (i < countStar.length()) {
                    sb.setCharAt(i, countStar.charAt(i));
                } else {
                    sb.setCharAt(i, ' ');
                }
            }
        }
        
        return sb.toString();
    }

    protected String countPaginatedBounded(String string, boolean distinct) {
        StringBuilder sb = new StringBuilder(20 + string.length());
        sb.append("DISTINCT ").append(string);

        if (!distinct) {
            for (int i = 0; i < sb.length() - 1; i++) {
                sb.setCharAt(i, ' ');
            }
            String suffix = "1, 'c')";
            String aliasFunctionInvocation = jpaProvider.getCustomFunctionInvocation("alias", 1);
            sb.replace(sb.length() - (aliasFunctionInvocation.length() + suffix.length()), sb.length() - suffix.length(), aliasFunctionInvocation);
            sb.replace(sb.length() - suffix.length(), sb.length(), suffix);
        }

        return sb.toString();
    }

    private static List<EntityType<?>> subtypes(EntityType<?> t, Metamodel metamodel) {
        List<EntityType<?>> list = new ArrayList<>();
        if (t.getJavaType() == null || t.getJavaType() == Map.class) {
            list.add(t);
        } else {
            for (EntityType<?> entity : metamodel.getEntities()) {
                if (entity.getJavaType() != null && t.getJavaType().isAssignableFrom(entity.getJavaType())) {
                    list.add(entity);
                }
            }
            Collections.sort(list, new Comparator<EntityType<?>>() {
                @Override
                public int compare(EntityType<?> o1, EntityType<?> o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
        return list;
    }

    protected String treatRoot(String path, Class<?> type, String property) {
        return treatRoot(path, type, property, false);
    }

    protected String treatRoot(String path, Class<?> type, String property, boolean subtypeProperty) {
        EntityType<?> entity = emf.getMetamodel().entity(type);
        String treatPath = null;
        if (jpaProvider.supportsRootTreat()) {
            return "TREAT(" + path + " AS " + type.getSimpleName() + ")." + property;
        } else if (jpaProvider.supportsSubtypePropertyResolving()) {
            treatPath = path + "." + property;
        }
        if (treatPath != null) {
            boolean addTypeCaseWhen;
            if (jpaProvider.isColumnShared(entity, property)) {
                addTypeCaseWhen = jpaProvider.needsTypeConstraintForColumnSharing();
            } else {
                if (subtypeProperty && jpaProvider.supportsSubtypePropertyResolving()) {
                    return treatPath;
                }
                addTypeCaseWhen = true;
            }
            if (addTypeCaseWhen) {
                StringBuilder sb = new StringBuilder();
                sb.append("CASE WHEN ");

                sb.append("TYPE(");
                sb.append(path);
                sb.append(") IN (");
                for (EntityType<?> subtype : subtypes(entity, emf.getMetamodel())) {
                    sb.append(subtype.getName());
                    sb.append(", ");
                }

                sb.setLength(sb.length() - 2);
                sb.append(") THEN ");
                sb.append(treatPath);

                if (jpaProvider.needsCaseWhenElseBranch()) {
                    sb.append(" ELSE NULL");
                }
                sb.append(" END");
                return sb.toString();
            }
        }
        throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
    }

    protected String treatRootWhereFragment(String alias, Class<?> rootType, Class<?> treatType, String after) {
        String operator;
        String logicalOperator;
        String predicate;
        StringBuilder sb = new StringBuilder();
        sb.append("(TYPE(").append(alias).append(") IN (");

        EntityMetamodel metamodel = cbf.getService(EntityMetamodel.class);
        for (EntityType<?> entitySubtype : metamodel.getEntitySubtypes(metamodel.entity(treatType))) {
            sb.append(entitySubtype.getName()).append(", ");
        }

        sb.setLength(sb.length() - 2);
        sb.append(") AND ");

        if (jpaProvider.supportsRootTreat()) {
            predicate = "TREAT(" + alias + " AS " + treatType.getSimpleName() + ")" + after;
        } else if (jpaProvider.supportsSubtypePropertyResolving()) {
            predicate = alias + after;
        } else {
            throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
        }

        sb.append(predicate).append(')');
        return sb.toString();
    }

    protected String treatJoinedConstraintFragment(String alias, Class<?> treatType, String after, boolean subtypeProperty) {
        if (jpaProvider.supportsTreatJoin() || subtypeProperty && jpaProvider.supportsSubtypePropertyResolving()) {
            return alias + after;
        }
        return treatRootWhereFragment(alias, PolymorphicBase.class, treatType, after);
    }

    protected String treatJoinWhereFragment(Class<?> sourceType, String attribute, String alias, Class<?> type, JoinType joinType, String whereFragment) {
        JpaProvider.ConstraintType constraintType = jpaProvider.requiresTreatFilter(em.getMetamodel().entity(sourceType), attribute, joinType);
        if (constraintType != JpaProvider.ConstraintType.WHERE) {
            return whereFragment == null ? "" : whereFragment;
        }
        String constraint = "TYPE(" + alias + ") = " + type.getSimpleName();
        if (whereFragment == null || whereFragment.isEmpty()) {
            return " WHERE " + constraint;
        } else {
            return whereFragment + " AND " + constraint;
        }
    }

    protected String treatJoin(String path, Class<?> type, JoinType joinType) {
        if (jpaProvider.supportsTreatJoin() && (!jpaProvider.supportsSubtypeRelationResolving() || joinType == JoinType.INNER)) {
            return "TREAT(" + path + " AS " + type.getSimpleName() + ")";
        } else if (jpaProvider.supportsSubtypeRelationResolving()) {
            return path;
        }

        throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
    }

    protected String treatRootJoin(String path, Class<?> type, String property) {
        if (jpaProvider.supportsRootTreatJoin()) {
            return "TREAT(" + path + " AS " + type.getSimpleName() + ")." + property;
        } else if (jpaProvider.supportsSubtypeRelationResolving()) {
            return path + "." + property;
        }

        throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
    }

    protected String treatRootTreatJoin(JoinType joinType, String path, Class<?> type, String property, Class<?> type2, String alias) {
        String joinPrefix;
        if (joinType == JoinType.INNER) {
            joinPrefix = "JOIN ";
        } else if (joinType == JoinType.LEFT) {
            joinPrefix = "LEFT JOIN ";
        } else if (joinType == JoinType.RIGHT) {
            joinPrefix = "RIGHT JOIN ";
        } else {
            throw new IllegalArgumentException("Invalid join type: " + joinType);
        }

        if (jpaProvider.supportsRootTreatTreatJoin()) {
            return joinPrefix + "TREAT(TREAT(" + path + " AS " + type.getSimpleName() + ")." + property + " AS " + type2.getSimpleName() + ") " + alias;
        } else if (jpaProvider.supportsSubtypeRelationResolving()) {
            if (jpaProvider.supportsTreatJoin() && joinType == JoinType.INNER) {
                String joinPath = joinPrefix + "TREAT(" + path + "." + property + " AS " + type2.getSimpleName() + ") " + alias;
                JpaProvider.ConstraintType constraintType = jpaProvider.requiresTreatFilter(em.getEntityManagerFactory().getMetamodel().entity(type), property, joinType);
                if (constraintType == JpaProvider.ConstraintType.ON) {
                    return joinPath + ON_CLAUSE + "TYPE(" + alias + ") = " + type2.getSimpleName();
                } else {
                    return joinPath;
                }
            } else {
                return joinPrefix + path + "." + property + " " + alias;
            }
        }

        throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
    }

    protected String renderNullPrecedence(String expression, String order, String nulls) {
        return renderNullPrecedence(expression, expression, order, nulls);
    }

    protected static String staticJoinAliasValue(String alias, String field) {
        return joinAliasValue(STATIC_JPA_PROVIDER, alias, field);
    }
    
    protected static String function(String name, String... args) {
        String registeredFunctionName;
        if ((registeredFunctionName = resolveRegisteredFunctionName(name)) != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(jpaProvider.getCustomFunctionInvocation(registeredFunctionName, args.length));
            StringUtils.join(sb, ",", args);
            sb.append(')');
            return sb.toString();
        } else if (jpaProvider.supportsJpa21()) {
            StringBuilder sb = new StringBuilder();
            sb.append("FUNCTION('").append(name).append('\'');
            
            for (String arg : args) {
                sb.append(",").append(arg);
            }
            
            sb.append(')');
            return sb.toString();
        } else {
            throw new IllegalArgumentException("Invalid JPA provider which does not support function syntax!");
        }
    }

    protected static String count(String... args) {
        return count(false, false, args);
    }

    protected static String countDistinct(String... args) {
        return count(false, true, args);
    }

    protected static String countTuple(String... args) {
        return count(true, false, args);
    }

    protected static String countTupleDistinct(String... args) {
        return count(true, true, args);
    }

    private static String count(boolean tuple, boolean distinct, String... args) {
        if (jpaProvider.supportsCountTuple()) {
            StringBuilder sb = new StringBuilder();
            sb.append("COUNT(");
            if (distinct) {
                sb.append("DISTINCT ");
            }
            if (tuple) {
                sb.append('(');
            }
            sb.append(args[0]);
            for (int i = 1; i < args.length; i++) {
                sb.append(", ");
                sb.append(args[i]);
            }
            if (tuple) {
                sb.append(')');
            }
            sb.append(')');
            return sb.toString();
        } else {
            if (distinct) {
                String[] newArgs = new String[args.length + 1];
                newArgs[0] = "'DISTINCT'";
                System.arraycopy(args, 0, newArgs, 1, args.length);
                return function(AbstractCountFunction.FUNCTION_NAME, newArgs);
            } else {
                return function(AbstractCountFunction.FUNCTION_NAME, args);
            }
        }
    }

    protected String singleValuedAssociationIdJoin(String singleValuedAssociationIdBasePath, String joinAlias, boolean optionalAssociation) {
        if (!jpaProvider.supportsSingleValuedAssociationIdExpressions()) {
            final String join;
            if (optionalAssociation) {
                join = "LEFT JOIN";
            } else {
                join = "JOIN";
            }
            return " " + join + " " + singleValuedAssociationIdBasePath + " " + joinAlias;
        } else {
            return "";
        }
    }

    protected String singleValuedAssociationIdPath(String idPath, String joinAlias) {
        final String[] pathParts = idPath.split("\\.");
        final String id = pathParts[pathParts.length - 1];
        if (jpaProvider.supportsSingleValuedAssociationIdExpressions()) {
            return idPath;
        } else {
            return joinAlias + "." + id;
        }
    }

    protected String singleValuedAssociationIdNullnessPath(String path, String id) {
        if (jpaProvider.supportsSingleValuedAssociationIdExpressions()) {
            return path + "." + id;
        } else {
            return path;
        }
    }

    protected static String tsLiteral(LocalDateTime value) {
        String literalValue = TypeUtils.getConverter(LocalDateTime.class, null).toString(value);
        if (jpaProvider.supportsTemporalLiteral()) {
            return literalValue;
        } else {
            return function("LITERAL_TIMESTAMP", TypeUtils.STRING_CONVERTER.toString(literalValue));
        }
    }

    protected static String tsLiteral(String literalValue) {
        if (jpaProvider.supportsTemporalLiteral()) {
            return literalValue;
        } else {
            return function("LITERAL_TIMESTAMP", TypeUtils.STRING_CONVERTER.toString(literalValue));
        }
    }

    protected static String tsLiteral(Calendar value) {
        String literalValue = TypeUtils.CALENDAR_CONVERTER.toString(value);
        if (jpaProvider.supportsTemporalLiteral()) {
            return literalValue;
        } else {
            return function("LITERAL_CALENDAR", TypeUtils.STRING_CONVERTER.toString(literalValue));
        }
    }

    protected static String dateLiteral(String literalValue) {
        if (jpaProvider.supportsTemporalLiteral()) {
            return literalValue;
        } else {
            return function("LITERAL_DATE", TypeUtils.STRING_CONVERTER.toString(literalValue));
        }
    }

    protected static String timeLiteral(String literalValue) {
        if (jpaProvider.supportsTemporalLiteral()) {
            return literalValue;
        } else {
            return function("LITERAL_TIME", TypeUtils.STRING_CONVERTER.toString(literalValue));
        }
    }

    protected static String resolveRegisteredFunctionName(String functionName) {
        return getIgnoreCase(STATIC_EMF_INTEGRATOR.getRegisteredFunctions(emf).keySet(), functionName);
    }
    
    private static String getIgnoreCase(Collection<String> list, String string) {
        for (String s : list) {
            if (s.equalsIgnoreCase(string)) {
                return s;
            }
        }
        
        return null;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Document.class,
            Version.class,
            Person.class,
            Workflow.class,
            IntIdEntity.class
        };
    }

    protected static <T> T[] concat(T[] array1, T... array2) {
        T[] newArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, newArray, array1.length, array2.length);
        return newArray;
    }
    
    protected void transactional(TxVoidWork work) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            TxSupport.transactional(em, work);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    protected <V> V transactional(TxWork<V> work) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            return TxSupport.transactional(em, work);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

}
