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

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.blazebit.persistence.CTECriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.RecursiveCTECriteriaBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class CriteriaBuilderImpl<T> extends AbstractQueryBuilder<T, CriteriaBuilder<T>> implements CriteriaBuilder<T> {
	
	private final CTEManager<T> cteManager;
	
	// Cache
    protected String cachedCteQueryString;

    public CriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, String alias, Set<String> registeredFunctions) {
        super(cbf, em, dbmsDialect, clazz, alias, registeredFunctions);
        this.cteManager = new CTEManager<T>(cbf, em, dbmsDialect, registeredFunctions);
    }

    @Override
    public CriteriaBuilder<T> from(Class<?> clazz) {
        return super.from(clazz);
    }

    @Override
    public CriteriaBuilder<T> from(Class<?> clazz, String alias) {
        return super.from(clazz, alias);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> SelectObjectBuilder<CriteriaBuilder<Y>> selectNew(Class<Y> clazz) {
        return (SelectObjectBuilder<CriteriaBuilder<Y>>) super.selectNew(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> CriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder) {
        return (CriteriaBuilder<Y>) super.selectNew(builder);
    }
    
    public <Y> CTECriteriaBuilder<Y, T> with(Class<Y> cteClass) {
    	return cteManager.with(cteClass, this);
    }

    public <Y> RecursiveCTECriteriaBuilder<Y, T> withRecursive(Class<Y> cteClass) {
    	return cteManager.withRecursive(cteClass, this);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public TypedQuery<T> getQuery() {
    	TypedQuery<T> query;
    	if (cteManager.getCtes().size() > 0) {
	        TypedQuery<T> baseQuery = (TypedQuery<T>) em.createQuery(getBaseQueryString(), selectManager.getExpectedQueryResultType());
	        String sqlQuery = cbf.getExtendedQuerySupport().getSql(em, baseQuery);
	        StringBuilder sb = new StringBuilder(cteManager.getCtes().size() * 100 + sqlQuery.length() + 50);
	        
	        sb.append(dbmsDialect.getWithClause(cteManager.isRecursive()));
	        sb.append(" ");
	        
	        for (CTEInfo cteInfo : cteManager.getCtes()) {
	    		String cteNonRecursiveQueryString = cteInfo.nonRecursiveCriteriaBuilder.getQueryString();
	    		Query cteNonRecursiveQuery = em.createQuery(cteNonRecursiveQueryString);
	    		// TODO: set parameters
	    		String cteNonRecursiveSqlQuery = cbf.getExtendedQuerySupport().getSql(em, cteNonRecursiveQuery);
	    		
		        sb.append(cteInfo.name);
		        sb.append('(');
	
	        	final List<String> attributes = cteInfo.attributes; 
	    		sb.append(attributes.get(0));
	    		
	        	for (int i = 1; i < attributes.size(); i++) {
	        		sb.append(", ");
	        		sb.append(attributes.get(i));
	        	}
	
	        	sb.append(')');
	        	
	        	sb.append(" AS(\n");
	        	
	        	sb.append(cteNonRecursiveSqlQuery);

	            // TODO: this is a hibernate specific integration detail
	            final String subselect = "( select * from " + cteInfo.name + " )";
	            sqlQuery = sqlQuery.replace(subselect, cteInfo.name);
	            
	        	if (cteInfo.recursive) {
		    		String cteRecursiveQueryString = cteInfo.recursiveCriteriaBuilder.getQueryString();
		    		Query cteRecursiveQuery = em.createQuery(cteRecursiveQueryString);
                    
                    // TODO: set parameters
                    String cteRecursiveSqlQuery = cbf.getExtendedQuerySupport().getSql(em, cteRecursiveQuery);

    	            // TODO: this is a hibernate specific integration detail
		            cteRecursiveSqlQuery = cteRecursiveSqlQuery.replace(subselect, cteInfo.name);
		    		
		        	sb.append("\nUNION ALL\n");
		        	sb.append(cteRecursiveSqlQuery);
	        	} else if (!dbmsDialect.supportsNonRecursiveWithClause()) {
		        	sb.append("\nUNION ALL\n");
		        	sb.append("SELECT ");
		        	
		        	sb.append("NULL");
		    		
		        	for (int i = 1; i < attributes.size(); i++) {
		        		sb.append(", ");
		        		sb.append("NULL");
		        	}
		        	
		        	sb.append(" FROM DUAL WHERE 1=0");
	        	}
	        	
	        	sb.append("\n)");
	        }
        	
            sb.append("\n");
	        sb.append(sqlQuery);
            
            String finalQuery = sb.toString();
            query = new CustomSQLTypedQuery<T>(baseQuery, em, cbf.getExtendedQuerySupport(), finalQuery);
            // TODO: parameters?
            // TODO: object builder?
    	} else {
	        query = (TypedQuery<T>) em.createQuery(getBaseQueryString(), selectManager.getExpectedQueryResultType());
	        if (firstResult != 0) {
	        	query.setFirstResult(firstResult);
	        }
	        if (maxResults != Integer.MAX_VALUE) {
	        	query.setMaxResults(maxResults);
	        }
	        if (selectManager.getSelectObjectBuilder() != null) {
	            query = transformQuery(query);
	        }
	
	        parameterizeQuery(query);
    	}
    	
        if (firstResult != 0) {
        	query.setFirstResult(firstResult);
        }
        if (maxResults != Integer.MAX_VALUE) {
        	query.setMaxResults(maxResults);
        }
        
        return query;
    }

    @Override
    public String getQueryString() {
        prepareAndCheck();
        return getCteQueryString0();
    }
    
    protected String getBaseQueryString() {
        prepareAndCheck();
        return getQueryString0();
    }
    
    @Override
    protected void clearCache() {
    	super.clearCache();
    	cachedCteQueryString = null;
    }

    protected String getCteQueryString0() {
        if (cachedCteQueryString == null) {
            cachedCteQueryString = getCteQueryString1();
        }

        return cachedCteQueryString;
    }

    protected String getCteQueryString1() {
        StringBuilder sbSelectFrom = new StringBuilder();
        cteManager.buildClause(sbSelectFrom);
        getQueryString1(sbSelectFrom);
        return sbSelectFrom.toString();
    }

}
