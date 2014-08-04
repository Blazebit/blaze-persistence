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

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.entity.Version;
import com.blazebit.persistence.impl.JPAInfo;
import com.blazebit.testsuite.base.AbstractPersistenceTest;

/**
 *
 * @author ccbem
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
    
    public String joinAliasValue(String alias) {
        return jpaInfo.getCollectionValueFunction() != null ? jpaInfo.getCollectionValueFunction() + "(" + alias + ")" : alias;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            Document.class,
            Version.class,
            Person.class
        };
    }
    
}
