/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.impl.hibernate;

import org.hibernate.persister.collection.CollectionPersister;

import javax.persistence.EntityManager;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class HibernateJpa21Provider extends HibernateJpaProvider {

    private final int major;
    private final int minor;
    private final int fix;

    public HibernateJpa21Provider(EntityManager em, String dbms, Map<String, CollectionPersister> collectionPersisters, int major, int minor, int fix) {
        super(em, dbms, collectionPersisters);
        this.major = major;
        this.minor = minor;
        this.fix = fix;
    }

    @Override
    public boolean supportsJpa21() {
        return true;
    }

    @Override
    public boolean supportsEntityJoin() {
        return major > 5 || major == 5 && minor >= 1;
    }

    @Override
    public boolean needsJoinSubqueryRewrite() {
        // Got fixed in 5.2.3: https://hibernate.atlassian.net/browse/HHH-9329 but is still buggy: https://hibernate.atlassian.net/browse/HHH-11401
        return major < 5 || major == 5 && minor < 2 || major == 5 && minor == 2 && fix < 7;
    }

    @Override
    public String getOnClause() {
        return "ON";
    }

}
