/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.IntIdEntity;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.entity.Version;
import com.blazebit.persistence.entity.Workflow;
import com.blazebit.persistence.function.ConcatenateFunction;
import com.blazebit.persistence.function.ZeroFunction;
import com.blazebit.persistence.impl.jpaprovider.JpaProvider;
import com.blazebit.persistence.impl.jpaprovider.JpaProviders;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.testsuite.base.AbstractPersistenceTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class AbstractCoreTest extends AbstractPersistenceTest {

    private static final JpaProvider staticJpaProvider;
    private JpaProvider jpaProvider;
    protected String ON_CLAUSE;
    
    static {
        staticJpaProvider = JpaProviders.resolveJpaProvider(null);
    }

    @Override
    public void init() {
        super.init();
        jpaProvider = JpaProviders.resolveJpaProvider(em);
        ON_CLAUSE = jpaProvider.getOnClause();
    }
    
    @Override
    protected CriteriaBuilderConfiguration configure(CriteriaBuilderConfiguration config) {
        config = super.configure(config);
        config.registerFunction(new JpqlFunctionGroup("zero", new ZeroFunction()));
        config.registerFunction(new JpqlFunctionGroup("concatenate", new ConcatenateFunction()));
        return config;
    }
    
    protected Set<String> getRegisteredFunctions() {
        return new HashSet<String>(Arrays.asList(
                // internal functions
                "limit",
                "page_position",
                "group_concat",
                "second", "minute", "hour", "day", "month", "year",
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
        return jpaProvider.getCollectionValueFunction() != null ? jpaProvider.getCollectionValueFunction() + "(" + alias + ")" : alias;
    }
    
    protected String booleanExpression(boolean value) {
        return jpaProvider.getBooleanExpression(value);
    }
    
    protected String booleanConditionalExpression(boolean value) {
        return jpaProvider.getBooleanConditionalExpression(value);
    }

    protected String escapeCharacter(char character) {
    	return jpaProvider.escapeCharacter(character);
    }

    protected String renderNullPrecedence(String expression, String resolvedExpression, String order, String nulls) {
    	return jpaProvider.renderNullPrecedence(expression, resolvedExpression, order, nulls);
    }

    protected String renderNullPrecedence(String expression, String order, String nulls) {
    	return renderNullPrecedence(expression, expression, order, nulls);
    }

    protected static String staticJoinAliasValue(String alias) {
        return staticJpaProvider.getCollectionValueFunction() != null ? staticJpaProvider.getCollectionValueFunction() + "(" + alias + ")" : alias;
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

}
