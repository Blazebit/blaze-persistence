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
import com.blazebit.persistence.impl.JPAInfo;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.testsuite.base.AbstractPersistenceTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class AbstractCoreTest extends AbstractPersistenceTest {

    private JPAInfo jpaInfo;
    protected String ON_CLAUSE;

    @Override
    public void init() {
        super.init();
        jpaInfo = new JPAInfo(em);
        ON_CLAUSE = jpaInfo.getOnClause();
    }
    
    @Override
    protected CriteriaBuilderConfiguration configure(CriteriaBuilderConfiguration config) {
        config = super.configure(config);
        config.registerFunction("zero", new ZeroFunction());
        config.registerFunction("concatenate", new ConcatenateFunction());
        return config;
    }
    
    protected Set<String> getRegisteredFunctions() {
        return new HashSet<String>(Arrays.asList(
                // internal functions
                "page_position",
                // test functions
                "zero", "concatenate"
        ));
    }
    
    protected String listParameter(String name) {
        // Workaround for HHH-7407
        if (jpaInfo.isHibernate) {
            return "(:" + name + ")";
        } else {
            return ":" + name;
        }
    }

    protected String joinAliasValue(String alias) {
        return jpaInfo.getCollectionValueFunction() != null ? jpaInfo.getCollectionValueFunction() + "(" + alias + ")" : alias;
    }
    
    protected String function(String name, String... args) {
        if (containsIgnoreCase(getRegisteredFunctions(), name)) {
            if (jpaInfo.isHibernate) {
                StringBuilder sb = new StringBuilder();
                sb.append(name).append('(');
                StringUtils.join(sb, ",", args);
                sb.append(')');
                return sb.toString();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("OPERATOR('").append(name).append('\'');

                for (String arg : args) {
                    sb.append(",");
                    sb.append(arg);
                }

                sb.append(')');
                return sb.toString();
            }
        } else if (jpaInfo.isJPA21) {
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
