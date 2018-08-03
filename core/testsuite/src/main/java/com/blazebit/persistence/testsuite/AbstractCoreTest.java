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

package com.blazebit.persistence.testsuite;

import java.util.*;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.testsuite.base.AbstractPersistenceTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.function.ConcatenateFunction;
import com.blazebit.persistence.testsuite.function.ZeroFunction;
import com.blazebit.persistence.testsuite.macro.PrefixJpqlMacro;
import com.blazebit.persistence.testsuite.tx.TxSupport;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.testsuite.tx.TxWork;

import javax.persistence.EntityManager;

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
    protected CriteriaBuilderConfiguration configure(CriteriaBuilderConfiguration config) {
        config = super.configure(config);
        config.registerFunction(new JpqlFunctionGroup("zero", new ZeroFunction()));
        config.registerFunction(new JpqlFunctionGroup("concatenate", new ConcatenateFunction()));

        config.registerMacro("prefix", new PrefixJpqlMacro());

        dbms = config.getEntityManagerIntegrators().get(0).getDbms(em.getEntityManagerFactory());
        if ("postgresql".equals(dbms)) {
            config.setProperty("com.blazebit.persistence.returning_clause_case_sensitive", "false");
        }
        
        return config;
    }
    
    /**
     * Only use this for parameters that have more than 1 value!
     * 
     * @param name
     * @return 
     */
    protected String listParameter(String name) {
        // Workaround for HHH-7407
        if (jpaProvider.needsBracketsForListParamter()) {
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

    protected String renderNullPrecedence(String expression, String resolvedExpression, String order, String nulls) {
        StringBuilder sb = new StringBuilder();
        jpaProvider.renderNullPrecedence(sb, expression, resolvedExpression, order, nulls);
        return sb.toString();
    }

    protected String renderNullPrecedenceGroupBy(String resolvedExpression) {
        StringBuilder sb = new StringBuilder();
        jpaProvider.renderNullPrecedence(sb, resolvedExpression, resolvedExpression, null, null);
        return sb.toString();
    }

    protected String groupBy(String... groupBys) {
        Set<String> distinctGroupBys = new LinkedHashSet<String>();
        distinctGroupBys.addAll(Arrays.asList(groupBys));
        return StringUtils.join(", ", distinctGroupBys);
    }

    protected String groupByPathExpressions(String groupByExpression, String... pathExpressions) {
        if (cbf.getService(DbmsDialect.class).supportsGroupByExpressionInHavingMatching()) {
            return groupByExpression;
        }
        Set<String> distinctGroupBys = new LinkedHashSet<String>();
        distinctGroupBys.addAll(Arrays.asList(pathExpressions));
        return StringUtils.join(", ", distinctGroupBys);
    }
    
    protected String countStar() {
        if (jpaProvider.supportsCountStar()) {
            return "COUNT(*)";
        } else {
            return function("COUNT_STAR");
        }
    }

    protected String countPaginated(String string, boolean distinct) {
        StringBuilder sb = new StringBuilder(20 + string.length());
        sb.append(function("COUNT_TUPLE", "'DISTINCT'", string));

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

    protected String treatRoot(String path, Class<?> type, String property) {
        if (jpaProvider.supportsRootTreat()) {
            return "TREAT(" + path + " AS " + type.getSimpleName() + ")." + property;
        } else if (jpaProvider.supportsSubtypePropertyResolving()) {
            return path + "." + property;
        }

        throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
    }

    protected String treatRootWhereFragment(String alias, Class<?> treatType, String after, boolean negatedContext) {
        String operator;
        String logicalOperator;
        String predicate;

        if (negatedContext) {
            operator = " = ";
            logicalOperator = " AND ";
        } else {
            operator = " <> ";
            logicalOperator = " OR ";
        }

        if (jpaProvider.supportsRootTreat()) {
            predicate = "TREAT(" + alias + " AS " + treatType.getSimpleName() + ")" + after;
        } else if (jpaProvider.supportsSubtypePropertyResolving()) {
            predicate = alias + after;

        } else {
            throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
        }

        return "(TYPE(" + alias + ")" + operator + treatType.getSimpleName() + logicalOperator + predicate + ")";
    }

    protected String treatJoinedConstraintFragment(String alias, Class<?> treatType, String after, boolean negatedContext) {
        if (jpaProvider.supportsTreatJoin()) {
            return alias + after;
        }
        return treatRootWhereFragment(alias, treatType, after, negatedContext);
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

    protected String treatJoin(String path, Class<?> type) {
        if (jpaProvider.supportsTreatJoin()) {
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
    
    protected String function(String name, String... args) {
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

    protected String resolveRegisteredFunctionName(String functionName) {
        return getIgnoreCase(STATIC_EMF_INTEGRATOR.getRegisteredFunctions(emf).keySet(), functionName);
    }
    
    private String getIgnoreCase(Collection<String> list, String string) {
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
