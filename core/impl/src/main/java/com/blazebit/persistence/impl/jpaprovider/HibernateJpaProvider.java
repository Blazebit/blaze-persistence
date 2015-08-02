/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.impl.jpaprovider;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManager;

import com.blazebit.reflection.ExpressionUtils;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class HibernateJpaProvider implements JpaProvider {

	private static final ConcurrentMap<WeakReference<Class<?>>, Boolean> isMySQLDialectCache = new ConcurrentHashMap<WeakReference<Class<?>>, Boolean>();
	private final boolean isMySQL;

	public HibernateJpaProvider(EntityManager em) {
		try {
			if (em == null) {
				isMySQL = false;
			} else {
				Object session = em.unwrap(Class.forName("org.hibernate.Session"));
				Class<?> dialectClass = ExpressionUtils.getValue(session, "sessionFactory.dialect").getClass();
				WeakReference<Class<?>> key = new WeakReference<Class<?>>(dialectClass);
				Boolean cacheValue = isMySQLDialectCache.get(key);
				
				if (cacheValue == null) {
					Set<Class<?>> types = ReflectionUtils.getSuperTypes(dialectClass);
					cacheValue = types.contains(Class.forName("org.hibernate.dialect.MySQLDialect"));
					isMySQLDialectCache.put(key, cacheValue);
				}
				
				isMySQL = cacheValue;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
    @Override
    public boolean supportsJpa21() {
        return false;
    }

    @Override
    public boolean needsBracketsForListParamter() {
        return true;
    }
    
    @Override
    public String getBooleanExpression(boolean value) {
        return value ? "CASE WHEN 1 = 1 THEN true ELSE false END"
                     : "CASE WHEN 1 = 1 THEN false ELSE true END";
    }
    
    @Override
    public String getBooleanConditionalExpression(boolean value) {
        return value ? "1 = 1" : "1 = 0";
    }

    @Override
	public String escapeCharacter(char character) {
		if (character == '\\') {
			return "\\\\";
		} else {
			return Character.toString(character);
		}
	}

    @Override
    public boolean supportsNullPrecedenceExpression() {
    	return !isMySQL;
    }
    
    @Override
	public String renderNullPrecedence(String expression, String resolvedExpression, String order, String nulls) {
		if (nulls == null) {
			return expression + " " + order;
		} else {
	    	if (isMySQL) {
	    		// Unfortunately we have to take care of that our selves because the SQL generation has a bug for MySQL
	    		StringBuilder sb = new StringBuilder();
	    		sb.append("CASE WHEN ").append(resolvedExpression != null ? resolvedExpression : expression).append(" IS NULL THEN ");
	    		if ("FIRST".equals(nulls)) {
					sb.append( "0 ELSE 1" );
				} else {
					sb.append( "1 ELSE 0" );
				}
				sb.append( " END, " );
				sb.append(expression).append(" ").append(order);
				return sb.toString();
	    	} else {
				return expression + " " + order + " NULLS " + nulls;
			}
    	}
	}

    @Override
    public String getOnClause() {
        return "WITH";
    }

    @Override
    public String getCollectionValueFunction() {
        return null;
    }

    @Override
    public Class<?> getDefaultQueryResultType() {
        return Object.class;
    }

    @Override
    public String getCustomFunctionInvocation(String functionName, int argumentCount) {
        return functionName + "(";
    }

}
