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

package com.blazebit.persistence.testsuite;

import java.util.*;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;
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

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class AbstractCoreTest extends AbstractPersistenceTest {

    protected static final JpaProvider STATIC_JPA_PROVIDER;
    protected static final String ON_CLAUSE;

    protected JpaProvider jpaProvider;

    private CriteriaBuilderConfiguration config;
    private String dbms;
    
    static {
        EntityManagerFactoryIntegrator integrator = ServiceLoader.load(EntityManagerFactoryIntegrator.class).iterator().next();
        STATIC_JPA_PROVIDER = integrator.getJpaProviderFactory(null).createJpaProvider(null);
        ON_CLAUSE = STATIC_JPA_PROVIDER.getOnClause();
    }

    @Override
    public void init() {
        super.init();
        jpaProvider = cbf.getService(JpaProviderFactory.class).createJpaProvider(em);
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
        
        this.config = config;
        return config;
    }
    
    protected Set<String> getRegisteredFunctions() {
        return new HashSet<String>(Arrays.asList(
                // internal functions
                "count_star",
                "limit",
                "page_position",
                "set_union", "set_union_all", "set_intersect", "set_intersect_all", "set_except", "set_except_all",
                "treat_boolean", "treat_byte", "treat_short", "treat_integer", "treat_long", "treat_float", "treat_double",
                "treat_character", "treat_string", "treat_biginteger", "treat_bigdecimal", "treat_time", "treat_date", "treat_timestamp", "treat_calendar",
                "cast_boolean", "cast_byte", "cast_short", "cast_integer", "cast_long", "cast_float", "cast_double",
                "cast_character", "cast_string", "cast_biginteger", "cast_bigdecimal", "cast_time", "cast_date", "cast_timestamp", "cast_calendar",
                "group_concat",
                "second", "minute", "hour", "day", "month", "year",
                "second_diff", "minute_diff", "hour_diff", "day_diff", "month_diff", "year_diff",
                "count_tuple",
                // test functions
                "zero", "concatenate"
        ));
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
        alias = jpaProvider.getCollectionValueFunction() != null ? jpaProvider.getCollectionValueFunction() + "(" + alias + ")" : alias;

        if (field == null) {
            return alias;
        }
        return alias + "." + field;
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

    protected String renderNullPrecedenceGroupBy(String resolvedExpression, String order, String nulls) {
        StringBuilder sb = new StringBuilder();
        jpaProvider.renderNullPrecedenceGroupBy(sb, resolvedExpression, resolvedExpression, order, nulls);
        return sb.toString();
    }

    protected String groupBy(String... groupBys) {
        Set<String> distinctGroupBys = new LinkedHashSet<String>();
        distinctGroupBys.addAll(Arrays.asList(groupBys));
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
        sb.append(jpaProvider.getCustomFunctionInvocation("COUNT_TUPLE", 1) + "'DISTINCT', ").append(string).append(")");
        
        if (!distinct) {
            String countStar;
            if (jpaProvider.supportsCountStar()) {
                countStar = "COUNT(*";
            } else {
                countStar = jpaProvider.getCustomFunctionInvocation("COUNT_STAR", 0);
            }
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

    protected String treatJoin(String path, Class<?> type) {
        if (jpaProvider.supportsTreatJoin()) {
            return "TREAT(" + path + " AS " + type.getSimpleName() + ")";
        } else if (jpaProvider.supportsSubtypePropertyResolving()) {
            return path;
        }

        throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
    }

    protected String treatRootJoin(String path, Class<?> type, String property) {
        if (jpaProvider.supportsRootTreatJoin()) {
            return "TREAT(" + path + " AS " + type.getSimpleName() + ")." + property;
        } else if (jpaProvider.supportsSubtypePropertyResolving()) {
            return path + "." + property;
        }

        throw new IllegalArgumentException("Treat should not be used as the JPA provider does not support subtype property access!");
    }

    protected String renderNullPrecedence(String expression, String order, String nulls) {
        return renderNullPrecedence(expression, expression, order, nulls);
    }

    protected static String staticJoinAliasValue(String alias, String field) {
        alias = STATIC_JPA_PROVIDER.getCollectionValueFunction() != null ? STATIC_JPA_PROVIDER.getCollectionValueFunction() + "(" + alias + ")" : alias;
        if (field == null) {
            return alias;
        }
        return alias + "." + field;
    }
    
    protected String function(String name, String... args) {
        if (containsIgnoreCase(getRegisteredFunctions(), name)) {
            StringBuilder sb = new StringBuilder();
            sb.append(jpaProvider.getCustomFunctionInvocation(name, args.length));
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
    
    private boolean containsIgnoreCase(Collection<String> list, String string) {
        for (String s : list) {
            if (s.equalsIgnoreCase(string)) {
                return true;
            }
        }
        
        return false;
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
    
    protected void transactional(TxVoidWork work) {
        TxSupport.transactional(em, work);
    }
    
    protected <V> V transactional(TxWork<V> work) {
        return TxSupport.transactional(em, work);
    }

}
