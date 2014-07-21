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

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

/**
 *
 * @author ccbem
 */
// TODO: implement clone for query builder (deep copy)
public interface QueryBuilder<T, X extends QueryBuilder<T, X>> extends BaseQueryBuilder<X> {
    
    public TypedQuery<T> getQuery(EntityManager em);

    public String getQueryString();
    
    public X setParameter(String name, Object value);

    public X setParameter(String name, Calendar value, TemporalType temporalType);

    public X setParameter(String name, Date value, TemporalType temporalType);
    
    public boolean isParameterSet(String name);
    
    public Set<? extends Parameter<?>> getParameters();
    
    public List<T> getResultList(EntityManager em);

    public PaginatedCriteriaBuilder<T> page(int firstRow, int pageSize);

    /*
     * Join methods
     */
    public X join(String path, String alias, JoinType type, boolean fetch);

    public X innerJoinFetch(String path, String alias);

    public X leftJoinFetch(String path, String alias);

    public X outerJoinFetch(String path, String alias);

    public X rightJoinFetch(String path, String alias);

    /*
     * Select methods
     */

    public <Y> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(Class<Y> clazz);

    public <Y> SelectObjectBuilder<? extends QueryBuilder<Y, ?>> selectNew(Constructor<Y> constructor);

    /**
     * On the model we might have constructors with annotated arguments that contain the entity path for the constructor arguments
     * The
     * @param builder
     * @return 
     */
    public <Y> QueryBuilder<Y, ?> selectNew(ObjectBuilder<Y> builder);
}
