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

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.spi.JpaProvider;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.OneToOneType;
import org.hibernate.type.Type;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class HibernateJpaProvider implements JpaProvider {

    private final DB db;
    private final Map<String, EntityPersister> entityPersisters;
    private final Map<String, CollectionPersister> collectionPersisters;
    private final ConcurrentMap<ColumnSharingCacheKey, Boolean> columnSharingCache = new ConcurrentHashMap<>();

    private static enum DB {
        OTHER,
        MY_SQL,
        DB2,
        MSSQL;
    }

    public HibernateJpaProvider(EntityManager em, String dbms, Map<String, EntityPersister> entityPersisters, Map<String, CollectionPersister> collectionPersisters) {
        try {
            if (em == null) {
                db = DB.OTHER;
            } else if ("mysql".equals(dbms)) {
                db = DB.MY_SQL;
            } else if ("db2".equals(dbms)) {
                db = DB.DB2;
            } else if ("microsoft".equals(dbms)) {
                db = DB.MSSQL;
            } else {
                db = DB.OTHER;
            }
            this.entityPersisters = entityPersisters;
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
        return db == DB.OTHER;
    }

    @Override
    public void renderNullPrecedence(StringBuilder sb, String expression, String resolvedExpression, String order, String nulls) {
        if (nulls != null) {
            if (db != DB.OTHER) {
                if (db == DB.DB2) {
                    if (("FIRST".equals(nulls) && "DESC".equalsIgnoreCase(order)) || ("LAST".equals(nulls) && "ASC".equalsIgnoreCase(order))) {
                        // The following are ok according to DB2 docs
                        // ASC + NULLS LAST
                        // DESC + NULLS FIRST
                        sb.append(expression);
                        if (order != null) {
                            sb.append(" ").append(order).append(" NULLS ").append(nulls);
                        }
                        return;
                    }
                } else if (db == DB.MSSQL) {
                    if (("ASC".equalsIgnoreCase(order) && "FIRST".equals(nulls)) || ("DESC".equalsIgnoreCase(order) && "LAST".equals(nulls))) {
                        // The following are the defaults, so just let them through
                        // ASC + NULLS FIRST
                        // DESC + NULLS LAST
                        sb.append(expression);
                        if (order != null) {
                            sb.append(" ").append(order);
                        }
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
                sb.append(expression);
                if (order != null) {
                    sb.append(" ").append(order);
                }
            } else {
                sb.append(expression);
                if (order != null) {
                    sb.append(' ').append(order).append(" NULLS ").append(nulls);
                }
            }
        } else {
            sb.append(expression);
            if (order != null) {
                sb.append(' ').append(order);
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
    public boolean supportsCollectionValueDereference() {
        return false;
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
    public boolean supportsTreatCorrelation() {
        return false;
    }

    @Override
    public boolean supportsRootTreatJoin() {
        return false;
    }

    @Override
    public boolean supportsRootTreatTreatJoin() {
        return false;
    }

    @Override
    public boolean supportsSubtypePropertyResolving() {
        return true;
    }

    @Override
    public boolean supportsSubtypeRelationResolving() {
        return true;
    }

    @Override
    public boolean supportsCountStar() {
        return true;
    }

    @Override
    public boolean isForeignJoinColumn(ManagedType<?> ownerType, String attributeName) {
        AbstractEntityPersister persister = (AbstractEntityPersister) entityPersisters.get(ownerType.getJavaType().getName());
        Type propertyType = persister.getPropertyType(attributeName);

        if (propertyType instanceof OneToOneType) {
            ForeignKeyDirection direction = ((OneToOneType) propertyType).getForeignKeyDirection();
            // Types changed between 4 and 5 so we check it like this. Essentially we check if the TO_PARENT direction is used
            return direction.toString().regionMatches(true, 0, "to", 0, 2);
        }

        // Every entity persister has "owned" properties on table number 0, others have higher numbers
        int tableNumber = persister.getSubclassPropertyTableNumber(attributeName);
        return tableNumber >= persister.getEntityMetamodel().getSubclassEntityNames().size();
    }

    @Override
    public boolean isColumnShared(ManagedType<?> ownerType, String attributeName) {
        AbstractEntityPersister persister = (AbstractEntityPersister) entityPersisters.get(ownerType.getJavaType().getName());
        if (!(persister instanceof SingleTableEntityPersister) && !(persister instanceof UnionSubclassEntityPersister)) {
            return false;
        }

        ColumnSharingCacheKey cacheKey = new ColumnSharingCacheKey(ownerType, attributeName);
        Boolean result = columnSharingCache.get(cacheKey);
        if (result != null) {
            return result;
        }

        if (persister instanceof SingleTableEntityPersister) {
            SingleTableEntityPersister singleTableEntityPersister = (SingleTableEntityPersister) persister;
            SingleTableEntityPersister rootPersister = (SingleTableEntityPersister) entityPersisters.get(singleTableEntityPersister.getRootEntityName());
            result = isColumnShared(singleTableEntityPersister, rootPersister.getName(), rootPersister.getSubclassClosure(), attributeName);
        } else if (persister instanceof UnionSubclassEntityPersister) {
            UnionSubclassEntityPersister unionSubclassEntityPersister = (UnionSubclassEntityPersister) persister;
            UnionSubclassEntityPersister rootPersister = (UnionSubclassEntityPersister) entityPersisters.get(unionSubclassEntityPersister.getRootEntityName());
            result = isColumnShared(unionSubclassEntityPersister, rootPersister.getName(), rootPersister.getSubclassClosure(), attributeName);
        }

        columnSharingCache.put(cacheKey, result);
        return result;
    }

    @Override
    public ConstraintType requiresTreatFilter(ManagedType<?> type, String attributeName, JoinType joinType) {
        // When we don't support treat joins(Hibernate 4.2), we need to put the constraints into the WHERE clause
        if (!supportsTreatJoin() && joinType == JoinType.INNER) {
            return ConstraintType.WHERE;
        }
        AbstractEntityPersister persister = (AbstractEntityPersister) entityPersisters.get(type.getJavaType().getName());
        Type propertyType = persister.getPropertyType(attributeName);

        if (!(propertyType instanceof AssociationType)) {
            return ConstraintType.NONE;
        }

        // When the inner treat joined element is collection, we always need the constraint, see HHH-??? TODO: report issue
        if (joinType == JoinType.INNER && propertyType instanceof CollectionType) {
            CollectionType collectionType = (CollectionType) propertyType;
            // When the inner treat joined element is an inverse collection, we currently have to put the constraint to the WHERE clause, see HHH-??? TODO: report issue
            ForeignKeyDirection direction = collectionType.getForeignKeyDirection();
            // Types changed between 4 and 5 so we check it like this. Essentially we check if the TO_PARENT direction is used
            if (direction.toString().regionMatches(true, 0, "to", 0, 2)) {
                return ConstraintType.WHERE;
            }

            return ConstraintType.ON;
        }

        String propertyEntityName = ((AssociationType) propertyType).getAssociatedEntityName(persister.getFactory());
        AbstractEntityPersister propertyTypePersister = (AbstractEntityPersister) entityPersisters.get(propertyEntityName);

        // When the treat joined element is a union subclass, we always need the constraint, see HHH-??? TODO: report issue
        if (propertyTypePersister instanceof UnionSubclassEntityPersister) {
            return ConstraintType.ON;
        }

        return ConstraintType.NONE;
    }

    private boolean isColumnShared(AbstractEntityPersister persister, String rootName, String[] subclassNames, String attributeName) {
        String[] columnNames = persister.getSubclassPropertyColumnNames(attributeName);
        for (String subclass: subclassNames) {
            if (!subclass.equals(persister.getName()) && !subclass.equals(rootName)) {
                AbstractEntityPersister subclassPersister = (AbstractEntityPersister) entityPersisters.get(subclass);
                if (isColumnShared(subclassPersister, columnNames)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isColumnShared(AbstractEntityPersister subclassPersister, String[] columnNames) {
        List<String> propertiesToCheck = new ArrayList<>(Arrays.asList(subclassPersister.getPropertyNames()));
        while (!propertiesToCheck.isEmpty()) {
            String propertyName = propertiesToCheck.remove(propertiesToCheck.size() - 1);
            Type propertyType = subclassPersister.getPropertyType(propertyName);
            if (propertyType instanceof ComponentType) {
                ComponentType componentType = (ComponentType) propertyType;
                for (String subPropertyName : componentType.getPropertyNames()) {
                    propertiesToCheck.add(propertyName + "." + subPropertyName);
                }
            } else {
                String[] subclassColumnNames = subclassPersister.getSubclassPropertyColumnNames(propertyName);
                if (Arrays.deepEquals(columnNames, subclassColumnNames)) {
                    return true;
                }
            }
        }

        return false;
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
            PluralAttribute.CollectionType collectionType = ((PluralAttribute<?, ?, ?>) attribute).getCollectionType();
            if (collectionType == PluralAttribute.CollectionType.COLLECTION) {
                return true;
            } else if (collectionType == PluralAttribute.CollectionType.LIST) {
                StringBuilder sb = new StringBuilder(200);
                sb.append(attribute.getDeclaringType().getJavaType().getName());
                sb.append('.');
                sb.append(attribute.getName());

                CollectionPersister persister = collectionPersisters.get(sb.toString());
                return !persister.hasIndex();
            }
        }

        return false;
    }

    @Override
    public boolean supportsSingleValuedAssociationIdExpressions() {
        return true;
    }

    @Override
    public boolean supportsForeignAssociationInOnClause() {
        return false;
    }

    @Override
    public boolean supportsTransientEntityAsParameter() {
        return true;
    }

    @Override
    public boolean needsAssociationToIdRewriteInOnClause() {
        return true;
    }

    @Override
    public boolean needsBrokenAssociationToIdRewriteInOnClause() {
        return true;
    }

    @Override
    public boolean needsTypeConstraintForColumnSharing() {
        return true;
    }

    private static class ColumnSharingCacheKey {
        private final ManagedType<?> managedType;
        private final String attributeName;

        public ColumnSharingCacheKey(ManagedType<?> managedType, String attributeName) {
            this.managedType = managedType;
            this.attributeName = attributeName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ColumnSharingCacheKey that = (ColumnSharingCacheKey) o;

            if (!managedType.equals(that.managedType)) {
                return false;
            }
            return attributeName.equals(that.attributeName);
        }

        @Override
        public int hashCode() {
            int result = managedType.hashCode();
            result = 31 * result + attributeName.hashCode();
            return result;
        }
    }

}
