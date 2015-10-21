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
import java.util.Locale;
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

    private static final ConcurrentMap<WeakClassKey, DB> dbDialectCache = new ConcurrentHashMap<WeakClassKey, DB>();
    private final DB db;

    private static enum DB {
        OTHER,
        MY_SQL,
        DB2;
    }

    public HibernateJpaProvider(EntityManager em) {
        try {
            if (em == null) {
                db = DB.OTHER;
            } else {
                Object session = em.unwrap(Class.forName("org.hibernate.Session"));
                Class<?> dialectClass = ExpressionUtils.getValue(session, "sessionFactory.dialect").getClass();
                WeakClassKey key = new WeakClassKey(dialectClass);
                DB cacheValue = dbDialectCache.get(key);

                if (cacheValue == null) {
                    Set<Class<?>> types = ReflectionUtils.getSuperTypes(dialectClass);
                    if (types.contains(Class.forName("org.hibernate.dialect.MySQLDialect"))) {
                        cacheValue = DB.MY_SQL;
                    } else if (types.contains(Class.forName("org.hibernate.dialect.DB2Dialect"))) {
                        cacheValue = DB.DB2;
                    } else {
                        cacheValue = DB.OTHER;
                    }
                    
                    // When we have to add a new dialect, we probably got redeployed, so let's do a cleanup
                    for (WeakClassKey keyElement : dbDialectCache.keySet()) {
                        if (keyElement.get() == null) {
                            dbDialectCache.remove(keyElement);
                        }
                    }
                    
                    dbDialectCache.put(key, cacheValue);
                }

                db = cacheValue;
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
    public boolean supportsInsertStatement() {
        return true;
    }

    @Override
    public boolean needsBracketsForListParamter() {
        return true;
    }

    @Override
    public String getBooleanExpression(boolean value) {
        return value ? "CASE WHEN 1 = 1 THEN true ELSE false END" : "CASE WHEN 1 = 1 THEN false ELSE true END";
    }

    @Override
    public String getBooleanConditionalExpression(boolean value) {
        return value ? "1 = 1" : "1 = 0";
    }

    @Override
    public String escapeCharacter(char character) {
        if (character == '\\' && db == DB.MY_SQL) {
            return "\\\\";
        } else {
            return Character.toString(character);
        }
    }

    @Override
    public boolean supportsNullPrecedenceExpression() {
        return db != DB.MY_SQL && db != DB.DB2;
    }

    @Override
    public String renderNullPrecedence(String expression, String resolvedExpression, String order, String nulls) {
        if (nulls == null) {
            return expression + " " + order;
        } else {
            if (db == DB.MY_SQL) {
                // Unfortunately we have to take care of that our selves because the SQL generation has a bug for MySQL
                StringBuilder sb = new StringBuilder();
                sb.append("CASE WHEN ").append(resolvedExpression != null ? resolvedExpression : expression).append(" IS NULL THEN ");
                if ("FIRST".equals(nulls)) {
                    sb.append("0 ELSE 1");
                } else {
                    sb.append("1 ELSE 0");
                }
                sb.append(" END, ");
                sb.append(expression).append(" ").append(order);
                return sb.toString();
            } else if (db == DB.DB2) {
                if (("FIRST".equals(nulls) && "DESC".equalsIgnoreCase(order)) || ("LAST".equals(nulls) && "ASC".equalsIgnoreCase(order))) {
                    // The following are ok according to DB2 docs
                    // ASC + NULLS LAST
                    // DESC + NULLS FIRST
                    return expression + " " + order + " NULLS " + nulls;
                }

                // But for the rest we have to use case when
                return String.format(Locale.ENGLISH, "CASE WHEN %s IS NULL THEN %s ELSE %s END, %s %s", resolvedExpression != null ? resolvedExpression : expression, "FIRST".equals(nulls) ? "0" : "1", "FIRST".equals(nulls) ? "1" : "0", expression, order);
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
    
    static class WeakClassKey extends WeakReference<Class<?>> {
        /**
         * saved value of the referent's identity hash code, to maintain
         * a consistent hash code after the referent has been cleared
         */
        private final int hash;

        /**
         * Create a new WeakClassKey to the given object, registered
         * with a queue.
         */
        WeakClassKey(Class<?> cl) {
            super(cl);
            hash = System.identityHashCode(cl);
        }

        /**
         * Returns the identity hash code of the original referent.
         */
        @Override
        public int hashCode() {
            return hash;
        }

        /**
         * Returns true if the given object is this identical
         * WeakClassKey instance, or, if this object's referent has not
         * been cleared, if the given object is another WeakClassKey
         * instance with the identical non-null referent as this one.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (obj instanceof WeakClassKey) {
                Object referent = get();
                return (referent != null) &&
                       (referent == ((WeakClassKey) obj).get());
            } else {
                return false;
            }
        }
    }


}
