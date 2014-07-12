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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.QueryBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

/**
 *
 * @author ccbem
 */
// TODO: maybe unify PaginatedCriterBuilder and CriteriaBuilder? We could view the CriteriaBuilder as the standard case with 1 single page containing all entries
public class PaginatedCriteriaBuilderImpl<T> extends AbstractCriteriaBuilder<T, PaginatedCriteriaBuilder<T>> implements PaginatedCriteriaBuilder<T> {
    private final int firstRow;
    private final int pageSize;
    
    public PaginatedCriteriaBuilderImpl(AbstractCriteriaBuilder<T, ? extends QueryBuilder<T, ?>> baseBuilder, int firstRow, int pageSize) {
        super(baseBuilder);
        this.firstRow = firstRow;
        this.pageSize = pageSize;
//        this.parameters = baseBuilder.parameters;
//        super.paramNameGenerator = baseBuilder.paramNameGenerator;
    }

    @Override
    public PagedList<T> getResultList(EntityManager em) {
        String countQueryString = getPageCountQueryString();
        Query countQuery = em.createQuery(countQueryString);
        parameterizeQuery(countQuery);
        
        
        long totalSize = (Long) countQuery.getSingleResult();





        
        String idQueryString = getPageIdQueryString();
        Query idQuery = em.createQuery(idQueryString);
        parameterizeQuery(idQuery);
        
        // Lossy conversion since JPQL COUNT returns a long
        List ids = idQuery.setFirstResult((int)firstRow).setMaxResults((int)pageSize).getResultList();
        
        TypedQuery<T> mainQuery = getQuery(em);
        mainQuery.setParameter("ids", ids);
        
        PagedList<T> pagedResultList = new PagedListImpl<T>(totalSize);
        pagedResultList.addAll(mainQuery.getResultList());
        return pagedResultList;
    }
    
    @Override
    public String getPageCountQueryString() {
        verifyBuilderEnded();
        StringBuilder countQuery = new StringBuilder();
        
        applyImplicitJoins();
        applyArrayTransformations();
        
        countQuery.append("SELECT COUNT(*)");
        countQuery.append(" FROM ").append(fromClazz.getSimpleName()).append(' ').append(joinManager.getRootAlias());
        countQuery.append(joinManager.buildJoins(false));
        countQuery.append(whereManager.buildClause());
        countQuery.append(groupByManager.buildGroupBy());        
        countQuery.append(havingManager.buildClause());
//        applyWhere(queryGenerator, sb);
//        applyGroupBys(queryGenerator, sb, groupByInfos);
//        applyHavings(queryGenerator, sb);
        return countQuery.toString();
    }
    
    @Override
    public String getPageObjectQueryString() {
        return "";
    }

    @Override
    public String getQueryString() {
        verifyBuilderEnded();
        StringBuilder sb = new StringBuilder();
        applyImplicitJoins();
        applyArrayTransformations();
        
        Metamodel m = em.getMetamodel();
        EntityType<?> entityType = m.entity(fromClazz);
        String idName = entityType.getId(entityType.getIdType().getJavaType()).getName();
        
        
        sb.append(selectManager.buildSelect());
        if(sb.length() > 0){
            sb.append(' ');
        }
        sb.append("FROM ").append(fromClazz.getSimpleName()).append(' ').append(joinManager.getRootAlias());
        sb.append(joinManager.buildJoins(true));
        
        sb.append(whereManager.buildClause(true));
        sb.append(" AND ").append(joinManager.getRootAlias()).append('.').append(idName).append(" IN (:ids)");
        
        sb.append(groupByManager.buildGroupBy());        
        sb.append(havingManager.buildClause());
        sb.append(orderByManager.buildOrderBy());
        return sb.toString();
    }
    
    @Override
    public String getPageIdQueryString() {
        verifyBuilderEnded();
        StringBuilder idQuery = new StringBuilder();
        Metamodel m = em.getMetamodel();
        EntityType<?> entityType = m.entity(fromClazz);
        String idName = entityType.getId(entityType.getIdType().getJavaType()).getName();
        
        applyImplicitJoins();
        applyArrayTransformations();
        
        idQuery.append("SELECT DISTINCT ").append(joinManager.getRootAlias()).append('.').append(idName);
        idQuery.append(" FROM ").append(fromClazz.getSimpleName()).append(' ').append(joinManager.getRootAlias());
        idQuery.append(joinManager.buildJoins(false));
        idQuery.append(whereManager.buildClause());
        idQuery.append(groupByManager.buildGroupBy());        
        idQuery.append(havingManager.buildClause());
        idQuery.append(orderByManager.buildOrderBy());
        
        return idQuery.toString();
    }

    @Override
    public <Y> SelectObjectBuilder<PaginatedCriteriaBuilder<Y>> selectNew(Class<Y> clazz) {
        return (SelectObjectBuilder<PaginatedCriteriaBuilder<Y>>) super.selectNew(clazz);
    }
    
    @Override
    public <Y> PaginatedCriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder) {
        return (PaginatedCriteriaBuilder<Y>) super.selectNew(builder);
    }
}