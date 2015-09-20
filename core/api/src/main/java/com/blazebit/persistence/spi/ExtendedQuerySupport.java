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
package com.blazebit.persistence.spi;

import java.sql.Connection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Interface implemented by the criteria provider.
 *
 * It is invoked to do some extended functionality like retrieving sql and
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface ExtendedQuerySupport {

    public String getSql(EntityManager em, Query query);
    
    public Connection getConnection(EntityManager em);
    
    public <T> List<T> getResultList(EntityManager em, TypedQuery<T> query, String sqlOverride);
    
    public <T> T getSingleResult(EntityManager em, TypedQuery<T> query, String sqlOverride);
}
