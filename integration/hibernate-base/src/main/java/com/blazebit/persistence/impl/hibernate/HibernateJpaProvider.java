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
package com.blazebit.persistence.impl.hibernate;

import com.blazebit.persistence.spi.JpaProvider;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Joinable;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class HibernateJpaProvider implements JpaProvider {

    private final DB db;
    private Map<String, CollectionPersister> collectionPersisters;

    private static enum DB {
        OTHER,
        MY_SQL,
        DB2;
    }

    public HibernateJpaProvider(EntityManager em, String dbms, Map<String, CollectionPersister> collectionPersisters) {
        try {
            if (em == null) {
                db = DB.OTHER;
            } else if ("mysql".equals(dbms)) {
                db = DB.MY_SQL;
            } else if ("db2".equals(dbms)) {
                db = DB.DB2;
            } else {
                db = DB.OTHER;
            }
            this.collectionPersisters = collectionPersisters;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsJpa21() {
        return false;
    }

    @Override
    public boolean supportsEntityJoin() {
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
    public boolean needsJoinSubqueryRewrite() {
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
    public String getNullExpression() {
        return "NULLIF(1,1)";
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
    public void renderNullPrecedence(StringBuilder sb, String expression, String resolvedExpression, String order, String nulls) {
        if (nulls != null) {
            if (db == DB.DB2 || db == DB.MY_SQL) {
                if (db == DB.DB2) {
                    if (("FIRST".equals(nulls) && "DESC".equalsIgnoreCase(order)) || ("LAST".equals(nulls) && "ASC".equalsIgnoreCase(order))) {
                        // The following are ok according to DB2 docs
                        // ASC + NULLS LAST
                        // DESC + NULLS FIRST
                        sb.append(expression).append(" ").append(order).append(" NULLS ").append(nulls);
                        return;
                    }
                }

                // Unfortunately we have to take care of that our selves because the SQL generation has a bug for MySQL: HHH-10241
                sb.append("CASE WHEN ").append(resolvedExpression != null ? resolvedExpression : expression).append(" IS NULL THEN ");
                if ("FIRST".equals(nulls)) {
                    sb.append("0 ELSE 1");
                } else {
                    sb.append("1 ELSE 0");
                }
                sb.append(" END, ");
                sb.append(expression).append(" ").append(order);
            } else {
                sb.append(expression).append(' ').append(order).append(" NULLS ").append(nulls);
            }
        } else {
            sb.append(expression).append(' ').append(order);
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
    	// Careful, PaginatedCriteriaBuilder has some dependency on the "length" of the string for rendering in the count query
        return functionName + "(";
    }

    @Override
    public boolean supportsRootTreat() {
        return false;
    }

    @Override
    public boolean supportsTreatJoin() {
        return false;
    }

    @Override
    public boolean supportsRootTreatJoin() {
        return false;
    }

    @Override
    public boolean supportsSubtypePropertyResolving() {
        return true;
    }

    @Override
    public boolean supportsCountStar() {
        return true;
    }

    @Override
    public boolean isJoinTable(Attribute<?, ?> attribute) {
        StringBuilder sb = new StringBuilder(200);
        sb.append(attribute.getDeclaringType().getJavaType().getName());
        sb.append('.');
        sb.append(attribute.getName());

        CollectionPersister persister = collectionPersisters.get(sb.toString());
        if (persister instanceof QueryableCollection) {
            QueryableCollection queryableCollection = (QueryableCollection) persister;
            if (queryableCollection.getElementPersister() instanceof Joinable) {
                String elementTableName = ((Joinable) queryableCollection.getElementPersister()).getTableName();
                return !queryableCollection.getTableName().equals(elementTableName);
            }
        }
        return false;
    }

    @Override
    public boolean isBag(Attribute<?, ?> attribute) {
        if (attribute instanceof PluralAttribute) {
            StringBuilder sb = new StringBuilder(200);
            sb.append(attribute.getDeclaringType().getJavaType().getName());
            sb.append('.');
            sb.append(attribute.getName());

            CollectionPersister persister = collectionPersisters.get(sb.toString());
            return !persister.hasIndex();
        }

        return false;
    }
}
