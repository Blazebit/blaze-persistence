/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.integration.hibernate.base;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.integration.jpa.JpaMetamodelAccessorImpl;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaMetamodelAccessor;
import com.blazebit.persistence.spi.JpaProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.CascadingActions;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.metamodel.model.domain.ManagedDomainType;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.OneToManyPersister;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.CustomType;
import org.hibernate.type.EmbeddedComponentType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.OneToOneType;
import org.hibernate.type.Type;
import org.hibernate.usertype.EnhancedUserType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public class HibernateJpaProvider implements JpaProvider {

    private static final Logger LOG = Logger.getLogger(HibernateJpaProvider.class.getName());

    protected final PersistenceUnitUtil persistenceUnitUtil;
    protected final DB db;
    protected final MappingMetamodelImplementor mappingMetamodel;

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static enum DB {
        OTHER,
        MY_SQL,
        DB2,
        MSSQL;
    }

    public HibernateJpaProvider(PersistenceUnitUtil persistenceUnitUtil, String dbms, MappingMetamodelImplementor mappingMetamodel) {
        this.persistenceUnitUtil = persistenceUnitUtil;
        try {
            if ("mysql".equals(dbms) || "mysql8".equals(dbms)) {
                db = DB.MY_SQL;
            } else if ("db2".equals(dbms)) {
                db = DB.DB2;
            } else if ("microsoft".equals(dbms)) {
                db = DB.MSSQL;
            } else {
                db = DB.OTHER;
            }
            this.mappingMetamodel = mappingMetamodel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsEntityJoin() {
        return true;
    }

    @Override
    public boolean supportsCrossJoin() {
        return true;
    }

    @Override
    public boolean needsJoinSubqueryRewrite() {
        return false;
    }

    @Override
    public boolean supportsForeignAssociationInOnClause() {
        return true;
    }

    @Override
    public boolean needsAssociationToIdRewriteInOnClause() {
        return false;
    }

    @Override
    public boolean needsBrokenAssociationToIdRewriteInOnClause() {
        return false;
    }

    @Override
    public boolean supportsCollectionTableCleanupOnDelete() {
        return true;
    }

    @Override
    public boolean supportsJoinTableCleanupOnDelete() {
        return true;
    }

    @Override
    public boolean needsCorrelationPredicateWhenCorrelatingWithWhereClause() {
        // See https://hibernate.atlassian.net/browse/HHH-12942 for details
        return false;
    }

    @Override
    public boolean supportsSingleValuedAssociationNaturalIdExpressions() {
        return true;
    }

    @Override
    public boolean supportsGroupByEntityAlias() {
        return false;
    }

    @Override
    public boolean needsElementCollectionIdCutoff() {
        return false;
    }

    @Override
    public boolean needsUnproxyForFieldAccess() {
        return true;
    }

    @Override
    public boolean needsCaseWhenElseBranch() {
        return false;
    }

    @Override
    public boolean supportsLikePatternEscape() {
        return true;
    }

    @Override
    public boolean supportsJpa21() {
        return true;
    }

    @Override
    public boolean supportsInsertStatement() {
        return true;
    }

    @Override
    public boolean needsBracketsForListParameter() {
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
        return "NULLFN()";
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
                    if (("FIRST".equals(nulls) && "DESC".equalsIgnoreCase(order)) || "LAST".equals(nulls) && "ASC".equalsIgnoreCase(order)) {
                        // According to DB2 docs, the following are the defaults
                        // ASC + NULLS LAST
                        // DESC + NULLS FIRST
                        sb.append(expression);
                        sb.append(" ").append(order);
                        return;
                    }
                } else if (db == DB.MSSQL) {
                    if ("ASC".equalsIgnoreCase(order) && "FIRST".equals(nulls) || "DESC".equalsIgnoreCase(order) && "LAST".equals(nulls)) {
                        // The following are the defaults, so just let them through
                        // ASC + NULLS FIRST
                        // DESC + NULLS LAST
                        sb.append(expression);
                        sb.append(" ").append(order);
                        return;
                    }
                }

                // According to the following, MySQL sorts NULLS FIRST when ASC and NULLS LAST when DESC, so we only need the expression for opposite cases
                // https://dev.mysql.com/doc/refman/8.0/en/working-with-null.html
                if (db != DB.MY_SQL || "ASC".equalsIgnoreCase(order) && "LAST".equals(nulls) || "DESC".equalsIgnoreCase(order) && "FIRST".equals(nulls)) {
                    // Unfortunately we have to take care of that our selves because the SQL generation has a bug for MySQL: HHH-10241
                    sb.append("CASE WHEN ").append(resolvedExpression != null ? resolvedExpression : expression).append(" IS NULL THEN ");
                    if ("FIRST".equals(nulls)) {
                        sb.append("0 ELSE 1");
                    } else {
                        sb.append("1 ELSE 0");
                    }
                    sb.append(" END, ");
                }
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
        return "ON";
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
    public boolean supportsSubqueryLimitOffset() {
        return true;
    }

    @Override
    public boolean supportsSetOperations() {
        return true;
    }

    @Override
    public boolean supportsListagg() {
        return true;
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
        return true;
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
    public boolean supportsCustomFunctions() {
        return true;
    }

    @Override
    public boolean supportsNonScalarSubquery() {
        return true;
    }

    @Override
    public boolean supportsSubqueryInFunction() {
        return true;
    }

    @Override
    public boolean supportsSubqueryAliasShadowing() {
        return true;
    }

    protected final String getTypeName(ManagedType<?> ownerType) {
        Class<?> javaType = ownerType.getJavaType();
        return javaType == null || javaType == Map.class ? ((EntityType<?>) ownerType).getName() : javaType.getName();
    }

    protected final AbstractEntityPersister getEntityPersister(ManagedType<?> ownerType) {
        Class<?> javaType = ownerType.getJavaType();
        EntityPersister entityPersister;
        if (javaType == null) {
            entityPersister = mappingMetamodel.findEntityDescriptor(((EntityType<?>) ownerType).getName());
        } else {
            entityPersister = mappingMetamodel.findEntityDescriptor(javaType.getName());
        }
        if (entityPersister == null) {
            try {
                entityPersister = mappingMetamodel.findEntityDescriptor(((ManagedDomainType<?>) ownerType).getTypeName());
            } catch (Exception e) {
                throw new IllegalArgumentException("Couldn't get type name!", e);
            }
        }
        return (AbstractEntityPersister) entityPersister;
    }

    protected final QueryableCollection getCollectionPersister(ManagedType<?> ownerType, String attributeName) {
        AbstractEntityPersister entityPersister = getEntityPersister(ownerType);
        QueryableCollection collection;
        do {
            String ownerTypeName = entityPersister.getName();
            StringBuilder sb = new StringBuilder(ownerTypeName.length() + attributeName.length() + 1);
            sb.append(ownerTypeName);
            sb.append('.');
            sb.append(attributeName);

            collection = (QueryableCollection) mappingMetamodel.findCollectionDescriptor(sb.toString());
            if (collection == null) {
                String superclass = entityPersister.getEntityMetamodel().getSuperclass();
                entityPersister = superclass == null ? null : (AbstractEntityPersister) mappingMetamodel.findEntityDescriptor(superclass);
            }
        } while (collection == null && entityPersister != null);

        return collection;
    }

    @Override
    public String[] getDiscriminatorColumnCheck(EntityType<?> entityType) {
        AbstractEntityPersister entityPersister = getEntityPersister(entityType);
        if (entityPersister.isInherited()) {
            String discriminatorColumnName = entityPersister.getDiscriminatorColumnName();
            String discriminatorSQLValue = entityPersister.getDiscriminatorSQLValue();
            return new String[]{discriminatorColumnName, discriminatorSQLValue};
        }
        return null;
    }

    @Override
    public boolean isForeignJoinColumn(EntityType<?> ownerType, String attributeName) {
        AbstractEntityPersister persister = getEntityPersister(ownerType);
        Type propertyType = getPropertyType(persister, attributeName);

        if (propertyType instanceof org.hibernate.type.EntityType) {
            org.hibernate.type.EntityType entityType = (org.hibernate.type.EntityType) propertyType;
            // As of Hibernate 5.4 we noticed that we have to treat nullable associations as "foreign" as well
            if (entityType.isNullable()) {
                return true;
            }

            // OneToOnes can't have JoinTables as per spec
            // ManyToOnes can have JoinTables, which can be treated as non-foreign
            // if table group joins are supported.
            return false;
        }

        // Every entity persister has "owned" properties on table number 0, others have higher numbers
        int tableNumber = persister.getSubclassPropertyTableNumber(attributeName);
        return tableNumber >= persister.getEntityMetamodel().getSubclassEntityNames().size();
    }

    @Override
    public boolean isColumnShared(EntityType<?> ownerType, String attributeName) {
        AbstractEntityPersister persister = getEntityPersister(ownerType);
        if (!(persister instanceof SingleTableEntityPersister) && !(persister instanceof UnionSubclassEntityPersister)) {
            return false;
        }

        if (persister instanceof SingleTableEntityPersister) {
            SingleTableEntityPersister singleTableEntityPersister = (SingleTableEntityPersister) persister;
            SingleTableEntityPersister rootPersister = (SingleTableEntityPersister) mappingMetamodel.findEntityDescriptor(singleTableEntityPersister.getRootEntityName());
            return isColumnShared(singleTableEntityPersister, rootPersister.getName(), rootPersister.getEntityMetamodel().getSubclassEntityNames(), attributeName);
        } else if (persister instanceof UnionSubclassEntityPersister) {
            UnionSubclassEntityPersister unionSubclassEntityPersister = (UnionSubclassEntityPersister) persister;
            UnionSubclassEntityPersister rootPersister = (UnionSubclassEntityPersister) mappingMetamodel.findEntityDescriptor(unionSubclassEntityPersister.getRootEntityName());
            return isColumnShared(unionSubclassEntityPersister, rootPersister.getName(), rootPersister.getEntityMetamodel().getSubclassEntityNames(), attributeName);
        }

        return false;
    }

    @Override
    public ConstraintType requiresTreatFilter(EntityType<?> ownerType, String attributeName, JoinType joinType) {
        return ConstraintType.NONE;
    }

    protected boolean isForeignKeyDirectionToParent(org.hibernate.type.EntityType entityType) {
        ForeignKeyDirection direction = entityType.getForeignKeyDirection();
        // Types changed between 4 and 5 so we check it like this. Essentially we check if the TO_PARENT direction is used
        return direction.toString().regionMatches(true, 0, "to", 0, 2);
    }

    protected boolean isForeignKeyDirectionToParent(CollectionType collectionType) {
        ForeignKeyDirection direction = collectionType.getForeignKeyDirection();
        // Types changed between 4 and 5 so we check it like this. Essentially we check if the TO_PARENT direction is used
        return direction.toString().regionMatches(true, 0, "to", 0, 2);
    }

    private boolean isColumnShared(AbstractEntityPersister persister, String rootName, Set<String> subclassNames, String attributeName) {
        String[] columnNames = persister.getSubclassPropertyColumnNames(attributeName);
        for (String subclass : subclassNames) {
            if (!subclass.equals(persister.getName()) && !subclass.equals(rootName)) {
                AbstractEntityPersister subclassPersister = (AbstractEntityPersister) mappingMetamodel.findEntityDescriptor(subclass);
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
            Type propertyType = getPropertyType(subclassPersister, propertyName);
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
    public String getMappedBy(EntityType<?> ownerType, String attributeName) {
        CollectionPersister persister = getCollectionPersister(ownerType, attributeName);
        if (persister != null) {
            if (persister.isInverse()) {
                return getMappedBy(persister);
            } else if (persister instanceof OneToManyPersister) {
                // A one-to-many association without a join table is like an inverse association
                return "";
            }
        } else {
            AbstractEntityPersister entityPersister = getEntityPersister(ownerType);
            Type propertyType = getPropertyType(entityPersister, attributeName);
            if (propertyType instanceof OneToOneType) {
                return ((OneToOneType) propertyType).getRHSUniqueKeyPropertyName();
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getWritableMappedByMappings(EntityType<?> inverseType, EntityType<?> ownerType, String attributeName, String inverseAttribute) {
        AbstractEntityPersister entityPersister = getEntityPersister(ownerType);
        int propertyIndex = entityPersister.getEntityMetamodel().getPropertyIndex(attributeName);
        Type propertyType = entityPersister.getPropertyTypes()[propertyIndex];
        org.hibernate.type.EntityType ownerPropertyType;
        if (propertyType instanceof CollectionType) {
            QueryableCollection persister = getCollectionPersister(ownerType, attributeName);
            AbstractEntityPersister inversePersister = getEntityPersister(inverseType);
            if (!persister.isInverse() && persister.getTableName().equals(inversePersister.getTableName())) {
                // We have a one-to-many relationship that has just join columns

                // Find properties for element columns in entityPersister
                // Map to properties for key columns of inverseType
                Set<String> elementAttributes = getColumnMatchingAttributeNames(entityPersister, Arrays.asList((entityPersister.toColumns(attributeName))));
                Set<String> keyAttributes = removeIdentifierAccess(ownerType, elementAttributes, inverseType, getColumnMatchingAttributeNames(inversePersister, Arrays.asList(persister.getKeyColumnNames())));

                Map<String, String> mapping = new LinkedHashMap<>();
                Iterator<String> elemAttrIter = elementAttributes.iterator();
                Iterator<String> keyAttrIter = keyAttributes.iterator();

                while (elemAttrIter.hasNext()) {
                    mapping.put(elemAttrIter.next(), keyAttrIter.next());
                }

                if (mapping.isEmpty()) {
                    throw new IllegalArgumentException("Mapped by property '" + inverseType.getName() + "#" + attributeName + "' must be writable or the column must be part of the id!");
                }
                return mapping;
            } else {
                // We only support detection when the inverse collection is writable
                if (entityPersister.getEntityMetamodel().getPropertyInsertability()[propertyIndex]) {
                    return null;
                }
                throw new IllegalArgumentException("Mapped by property '" + inverseType.getName() + "#" + attributeName + "' must be writable!");
            }
        } else {
            // Either the mapped by property is writable
            if (entityPersister.getEntityMetamodel().getPropertyInsertability()[propertyIndex]) {
                return null;
            }
            // Or the columns of the mapped by property are part of the target id
            ownerPropertyType = (org.hibernate.type.EntityType) propertyType;
        }
        AbstractEntityPersister sourceType = (AbstractEntityPersister) mappingMetamodel.findEntityDescriptor(ownerPropertyType.getAssociatedEntityName());
        Type identifierType = ownerPropertyType.getIdentifierOrUniqueKeyType(entityPersister.getFactory());
        String sourcePropertyPrefix;
        String[] sourcePropertyNames;
        if (identifierType.isComponentType()) {
            ComponentType componentType = (ComponentType) identifierType;
            sourcePropertyPrefix = sourceType.getIdentifierPropertyName() == null ? "" : sourceType.getIdentifierPropertyName() + ".";
            sourcePropertyNames = componentType.getPropertyNames();
        } else {
            sourcePropertyPrefix = "";
            sourcePropertyNames = new String[]{sourceType.getIdentifierPropertyName()};
        }
        String[] targetColumnNames = entityPersister.getPropertyColumnNames(propertyIndex);
        Type targetIdType = entityPersister.getIdentifierType();
        if (targetIdType.isComponentType()) {
            ComponentType targetIdentifierType = (ComponentType) entityPersister.getIdentifierType();
            String targetPropertyPrefix = entityPersister.getIdentifierPropertyName() == null ? "" : entityPersister.getIdentifierPropertyName() + ".";
            String[] identifierColumnNames = entityPersister.getIdentifierColumnNames();
            String[] targetIdentifierTypePropertyNames = targetIdentifierType.getPropertyNames();
            Map<String, String> mapping = new LinkedHashMap<>();
            for (int i = 0; i < targetColumnNames.length; i++) {
                for (int j = 0; j < identifierColumnNames.length; j++) {
                    if (targetColumnNames[i].equals(identifierColumnNames[j])) {
                        mapping.put(sourcePropertyPrefix + sourcePropertyNames[i], targetPropertyPrefix + targetIdentifierTypePropertyNames[j]);
                        break;
                    }
                }
            }
            if (mapping.isEmpty()) {
                throw new IllegalArgumentException("Mapped by property '" + inverseType.getName() + "#" + attributeName + "' must be writable or the column must be part of the id!");
            }
            return mapping;
        } else {
            String targetIdColumnName = entityPersister.getIdentifierColumnNames()[0];
            if (!targetIdColumnName.equals(targetColumnNames[0])) {
                throw new IllegalArgumentException("Mapped by property '" + inverseType.getName() + "#" + attributeName + "' must be writable or the column must be part of the id!");
            }
            Map<String, String> mapping = new LinkedHashMap<>();
            mapping.put(sourcePropertyPrefix + sourcePropertyNames[0], entityPersister.getIdentifierPropertyName());
            return mapping;
        }
    }

    private Set<String> removeIdentifierAccess(EntityType<?> elementType, Set<String> elementAttributeNames, EntityType<?> ownerType, Set<String> columnMatchingAttributeNames) {
        Set<String> set = new LinkedHashSet<>();
        Iterator<String> iterator = elementAttributeNames.iterator();
        AbstractEntityPersister elementPersister = getEntityPersister(elementType);
        AbstractEntityPersister entityPersister = getEntityPersister(ownerType);
        List<String> addedAttributeNames = new ArrayList<>();
        for (String attributeName : columnMatchingAttributeNames) {
            String elementAttributeName = iterator.next();
            Type propertyType = getPropertyType(entityPersister, attributeName);
            if (propertyType instanceof org.hibernate.type.EntityType) {
                // If the columns refer to an association, we map that through instead of trying to set just the identifier properties
                if (elementPersister.getEntityName().equals(((org.hibernate.type.EntityType) propertyType).getAssociatedEntityName())) {
                    int identifierCount = getJoinMappingPropertyNames(ownerType, null, attributeName).size();
                    iterator.remove();
                    for (int i = 1; i < identifierCount; i++) {
                        iterator.next();
                        iterator.remove();
                    }
                    addedAttributeNames.add(attributeName);
                }
            } else {
                set.add(attributeName);
            }
        }

        for (String addedAttributeName : addedAttributeNames) {
            set.add(addedAttributeName);
            elementAttributeNames.add("");
        }

        return set;
    }

    protected String getMappedBy(CollectionPersister persister) {
        if (persister instanceof CustomCollectionPersister) {
            return ((CustomCollectionPersister) persister).getMappedByProperty();
        }

        throw new IllegalStateException("Custom persister configured that doesn't implement the CustomCollectionPersister interface: " + persister);
    }

    @Override
    public String[] getColumnNames(EntityType<?> entityType, String attributeName) {
        QueryableCollection collectionPersister = getCollectionPersister(entityType, attributeName);
        if (collectionPersister == null) {
            return getColumnNames(getEntityPersister(entityType), attributeName);
        } else {
            return collectionPersister.getElementColumnNames();
        }
    }

    public String[] getColumnNames(AbstractEntityPersister entityPersister, String attributeName) {
        try {
            return entityPersister.getPropertyColumnNames(attributeName);
        } catch (MappingException e) {
            // Workaround for HHH-15051
            int dotIndex = attributeName.lastIndexOf('.');
            if (dotIndex != -1) {
                String attributePrefix = attributeName.substring(0, dotIndex);
                Type propertyType = getPropertyType(entityPersister, attributePrefix);
                if (propertyType instanceof org.hibernate.type.EntityType) {
                    String[] columnNames = getColumnNames(entityPersister, attributePrefix);
                    org.hibernate.type.EntityType hibernateEntityType = (org.hibernate.type.EntityType) propertyType;
                    Type idType = hibernateEntityType.getIdentifierOrUniqueKeyType(entityPersister.getFactory());
                    String attributeSubName = attributeName.substring(dotIndex + 1);
                    if (idType instanceof CompositeType && ((CompositeType) idType).isEmbedded()) {
                        CompositeType idClassType = (CompositeType) idType;
                        int columnSpan = 0;
                        int propertyIndex = -1;
                        String[] propertyNames = idClassType.getPropertyNames();
                        Type[] subtypes = idClassType.getSubtypes();
                        for (int i = 0; i < propertyNames.length; i++) {
                            if (propertyNames[i].equals(attributeSubName)) {
                                propertyIndex = i;
                                break;
                            }
                            columnSpan += subtypes[i].getColumnSpan(entityPersister.getFactory());
                        }

                        if (propertyIndex != -1) {
                            String[] actualColumns = new String[subtypes[propertyIndex].getColumnSpan(entityPersister.getFactory())];
                            System.arraycopy(columnNames, columnSpan, actualColumns, 0, actualColumns.length);
                            return actualColumns;
                        }
                    } else if (attributeSubName.equals(hibernateEntityType.getIdentifierOrUniqueKeyPropertyName(entityPersister.getFactory()))) {
                        return columnNames;
                    }
                }
            }
            throw new RuntimeException("Unknown property [" + attributeName + "] of entity [" + entityPersister.getEntityName() + "]", e);
        }
    }

    @Override
    public String[] getColumnNames(EntityType<?> ownerType, String elementCollectionPath, String attributeName) {
        QueryableCollection persister = getCollectionPersister(ownerType, elementCollectionPath);
        String subAttributeName = attributeName.substring(elementCollectionPath.length() + 1);
        if (persister.getElementType() instanceof ComponentType) {
            ComponentType elementType = (ComponentType) persister.getElementType();
            String[] propertyNames = elementType.getPropertyNames();
            Type[] subtypes = elementType.getSubtypes();
            String[] propertyParts = subAttributeName.split("\\.");
            int offset = 0;
            for (int j = 0; j < propertyParts.length - 1; j++) {
                String propertyName = propertyParts[j];

                for (int i = 0; i < propertyNames.length; i++) {
                    int span = subtypes[i].getColumnSpan(persister.getFactory());
                    if (propertyName.equals(propertyNames[i])) {
                        if (subtypes[i] instanceof ComponentType) {
                            elementType = (ComponentType) subtypes[i];
                            propertyNames = elementType.getPropertyNames();
                            subtypes = elementType.getSubtypes();
                            break;
                        } else {
                            String[] columnNames = new String[span];
                            String[] elementColumnNames = persister.getElementColumnNames();
                            System.arraycopy(elementColumnNames, offset, columnNames, 0, span);
                            return columnNames;
                        }
                    } else {
                        offset += span;
                    }
                }
            }

            String propertyName = propertyParts[propertyParts.length - 1];
            for (int i = 0; i < propertyNames.length; i++) {
                int span = subtypes[i].getColumnSpan(persister.getFactory());
                if (propertyName.equals(propertyNames[i])) {
                    String[] columnNames = new String[span];
                    String[] elementColumnNames = persister.getElementColumnNames();
                    System.arraycopy(elementColumnNames, offset, columnNames, 0, span);
                    return columnNames;
                } else {
                    offset += span;
                }
            }
        } else if (persister.getElementType() instanceof org.hibernate.type.EntityType) {
            AbstractEntityPersister elementPersister = (AbstractEntityPersister) mappingMetamodel.findEntityDescriptor(((org.hibernate.type.EntityType) persister.getElementType()).getAssociatedEntityName());
            Type identifierType = ((org.hibernate.type.EntityType) persister.getElementType()).getIdentifierOrUniqueKeyType(persister.getFactory());
            String identifierOrUniqueKeyPropertyName = ((org.hibernate.type.EntityType) persister.getElementType()).getIdentifierOrUniqueKeyPropertyName(persister.getFactory());
            String prefix;
            if (identifierType instanceof EmbeddedComponentType) {
                String[] propertyNames = ((EmbeddedComponentType) identifierType).getPropertyNames();
                String[] columnNames = columnNamesByPropertyName(elementPersister, propertyNames, subAttributeName, "", persister.getElementColumnNames(), persister.getFactory());
                if (columnNames != null) {
                    return columnNames;
                }
            } else if (subAttributeName.equals(identifierOrUniqueKeyPropertyName)) {
                return persister.getElementColumnNames();
            } else if (identifierType instanceof ComponentType && subAttributeName.startsWith(prefix = identifierOrUniqueKeyPropertyName + ".")) {
                String[] propertyNames = ((ComponentType) identifierType).getPropertyNames();
                String[] columnNames = columnNamesByPropertyName(elementPersister, propertyNames, subAttributeName.substring(identifierOrUniqueKeyPropertyName.length() + 1), prefix, persister.getElementColumnNames(), persister.getFactory());
                if (columnNames != null) {
                    return columnNames;
                }
            }
        }

        throw new IllegalArgumentException("Couldn't find column names for " + getTypeName(ownerType) + "#" + attributeName);
    }

    private String[] columnNamesByPropertyName(AbstractEntityPersister persister, String[] propertyNames, String subAttributeName, String prefix, String[] elementColumnNames, Mapping factory) {
        int offset = 0;
        for (int i = 0; i < propertyNames.length; i++) {
            String propertyName = propertyNames[i];
            Type propertyType = getPropertyType(persister, prefix + propertyName);
            int span = propertyType.getColumnSpan(factory);
            if (subAttributeName.equals(propertyName)) {
                String[] columnNames = new String[span];
                System.arraycopy(elementColumnNames, offset, columnNames, 0, span);
                return columnNames;
            } else {
                offset += span;
            }
        }

        return null;
    }

    private String unquote(String name) {
        return name;
    }

    @Override
    public String[] getColumnTypes(EntityType<?> entityType, String attributeName) {
        QueryableCollection collectionPersister = getCollectionPersister(entityType, attributeName);
        if (collectionPersister == null) {
            AbstractEntityPersister entityPersister = getEntityPersister(entityType);
            SessionFactoryImplementor sfi = entityPersister.getFactory();
            String[] columnNames = getColumnNames(entityPersister, attributeName);
            Database database = sfi.getServiceRegistry().locateServiceBinding(Database.class).getService();
            Table[] tables;

            if (entityPersister instanceof JoinedSubclassEntityPersister) {
                tables = new Table[((JoinedSubclassEntityPersister) entityPersister).getSubclassTableSpan()];
                for (int i = 0; i < tables.length; i++) {
                    tables[i] = getTable(database, entityPersister.getSubclassTableName(i));
                }
            } else if (entityPersister instanceof UnionSubclassEntityPersister) {
                tables = new Table[((UnionSubclassEntityPersister) entityPersister).getSubclassTableSpan()];
                for (int i = 0; i < tables.length; i++) {
                    tables[i] = getTable(database, entityPersister.getSubclassTableName(i));
                }
            } else if (entityPersister instanceof SingleTableEntityPersister) {
                tables = new Table[((SingleTableEntityPersister) entityPersister).getSubclassTableSpan()];
                for (int i = 0; i < tables.length; i++) {
                    tables[i] = getTable(database, entityPersister.getSubclassTableName(i));
                }
            } else {
                tables = new Table[]{getTable(database, entityPersister.getTableName())};
            }

            boolean isSubselect = tables.length == 1 && tables[0] == null;

            if (isSubselect || isFormula(columnNames)) {
                Type propertyType = getPropertyType(entityPersister, attributeName);
                return getColumnTypeForPropertyType(entityType, attributeName, sfi, propertyType);
            }

            return getColumnTypesForColumnNames(entityType.getName(), columnNames, tables);
        } else {
            SessionFactoryImplementor sfi = collectionPersister.getFactory();
            Database database = sfi.getServiceRegistry().locateServiceBinding(Database.class).getService();
            Table[] tables;
            if (collectionPersister.isOneToMany() && collectionPersister.getElementType() instanceof org.hibernate.type.EntityType) {
                EntityPersister elementPersister = collectionPersister.getElementPersister();
                if (elementPersister instanceof UnionSubclassEntityPersister) {
                    tables = new Table[]{getTable(database, (String) collectionPersister.getElementPersister().getQuerySpaces()[0])};
                } else {
                    tables = new Table[]{getTable(database, collectionPersister.getTableName())};
                }
            } else {
                tables = new Table[]{getTable(database, collectionPersister.getTableName())};
            }
            return getColumnTypesForColumnNames(collectionPersister.getName(), collectionPersister.getElementColumnNames(), tables);
        }
    }

    private String[] getColumnTypesForColumnNames(String owner, String[] columnNames, Table[] tables) {
        String[] columnTypes = new String[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            Column column = null;
            for (int j = 0; j < tables.length; j++) {
                column = tables[j].getColumn(new Column(columnNames[i]));
                if (column != null) {
                    break;
                }
            }

            if (column == null) {
                throw new IllegalArgumentException("Could not find column '" + columnNames[i] + "' in entity: " + owner);
            }

            columnTypes[i] = column.getSqlType();
        }

        return columnTypes;
    }

    private String[] getColumnTypeForPropertyType(EntityType<?> entityType, String attributeName, SessionFactoryImplementor sfi, Type propertyType) {
        if (propertyType instanceof org.hibernate.type.EntityType) {
            propertyType = ((org.hibernate.type.EntityType) propertyType).getIdentifierOrUniqueKeyType(sfi);
        }

        return new String[]{
                sfi.getTypeConfiguration().getDdlTypeRegistry().getTypeName(
                        propertyType.getSqlTypeCodes(sfi)[0],
                        Size.nil()
                )
        };
    }

    @Override
    public String[] getColumnTypes(EntityType<?> ownerType, String elementCollectionPath, String attributeName) {
        QueryableCollection persister = getCollectionPersister(ownerType, elementCollectionPath);
        SessionFactoryImplementor sfi = persister.getFactory();
        String[] columnNames = null;
        Type propertyType = null;
        String subAttributeName = attributeName.substring(elementCollectionPath.length() + 1);
        if (persister.getElementType() instanceof ComponentType) {
            ComponentType elementType = (ComponentType) persister.getElementType();
            String[] propertyNames = elementType.getPropertyNames();
            Type[] subtypes = elementType.getSubtypes();
            String[] propertyParts = subAttributeName.split("\\.");
            int offset = 0;
            for (int j = 0; j < propertyParts.length - 1; j++) {
                String propertyName = propertyParts[j];

                for (int i = 0; i < propertyNames.length; i++) {
                    int span = subtypes[i].getColumnSpan(persister.getFactory());
                    if (propertyName.equals(propertyNames[i])) {
                        if (subtypes[i] instanceof ComponentType) {
                            elementType = (ComponentType) subtypes[i];
                            propertyNames = elementType.getPropertyNames();
                            subtypes = elementType.getSubtypes();
                            break;
                        } else {
                            columnNames = new String[span];
                            String[] elementColumnNames = persister.getElementColumnNames();
                            System.arraycopy(elementColumnNames, offset, columnNames, 0, span);
                            break;
                        }
                    } else {
                        offset += span;
                    }
                }
            }

            if (columnNames == null) {
                String propertyName = propertyParts[propertyParts.length - 1];
                for (int i = 0; i < propertyNames.length; i++) {
                    int span = subtypes[i].getColumnSpan(persister.getFactory());
                    if (propertyName.equals(propertyNames[i])) {
                        columnNames = new String[span];
                        String[] elementColumnNames = persister.getElementColumnNames();
                        System.arraycopy(elementColumnNames, offset, columnNames, 0, span);
                        break;
                    } else {
                        offset += span;
                    }
                }
            }
        } else if (persister.getElementType() instanceof org.hibernate.type.EntityType) {
            Type identifierType = ((org.hibernate.type.EntityType) persister.getElementType()).getIdentifierOrUniqueKeyType(persister.getFactory());
            String identifierOrUniqueKeyPropertyName = ((org.hibernate.type.EntityType) persister.getElementType()).getIdentifierOrUniqueKeyPropertyName(persister.getFactory());
            String prefix;
            if (identifierType instanceof EmbeddedComponentType) {
                String[] propertyNames = ((EmbeddedComponentType) identifierType).getPropertyNames();
                Type[] subtypes = ((EmbeddedComponentType) identifierType).getSubtypes();
                int offset = 0;
                for (int i = 0; i < propertyNames.length; i++) {
                    String propertyName = propertyNames[i];
                    int span = subtypes[i].getColumnSpan(persister.getFactory());
                    if (subAttributeName.equals(propertyName)) {
                        columnNames = new String[span];
                        String[] elementColumnNames = persister.getElementColumnNames();
                        System.arraycopy(elementColumnNames, offset, columnNames, 0, span);
                        propertyType = subtypes[i];
                        break;
                    } else {
                        offset += span;
                    }
                }
            } else if (subAttributeName.equals(identifierOrUniqueKeyPropertyName)) {
                columnNames = persister.getElementColumnNames();
                propertyType = identifierType;
            } else if (identifierType instanceof ComponentType && subAttributeName.startsWith(prefix = identifierOrUniqueKeyPropertyName + ".")) {
                String[] propertyNames = ((ComponentType) identifierType).getPropertyNames();
                Type[] subtypes = ((ComponentType) identifierType).getSubtypes();
                String subPropertyName = subAttributeName.substring(prefix.length());
                int offset = 0;
                for (int i = 0; i < propertyNames.length; i++) {
                    String propertyName = propertyNames[i];
                    int span = subtypes[i].getColumnSpan(persister.getFactory());
                    if (subPropertyName.equals(propertyName)) {
                        columnNames = new String[span];
                        String[] elementColumnNames = persister.getElementColumnNames();
                        System.arraycopy(elementColumnNames, offset, columnNames, 0, span);
                        propertyType = subtypes[i];
                        break;
                    } else {
                        offset += span;
                    }
                }
            }
        }

        if (columnNames == null) {
            throw new IllegalArgumentException("Couldn't find column names for " + getTypeName(ownerType) + "#" + attributeName);
        }

        if (isFormula(columnNames)) {
            return getColumnTypeForPropertyType(ownerType, attributeName, sfi, propertyType);
        }

        Database database = sfi.getServiceRegistry().locateServiceBinding(Database.class).getService();
        Table[] tables = new Table[]{getTable(database, persister.getTableName())};

        return getColumnTypesForColumnNames(ownerType.getName(), columnNames, tables);
    }

    private Table getTable(Database database, String tableName) {
        Table table = database.getTable(unquote(tableName));

        if (table == null) {
            // It might happen that the boot model does not consider the schema so we skip it here
            table = database.getTable(unquote(tableName.substring(tableName.lastIndexOf('.') + 1)));
        }
        return table;
    }

    private boolean isFormula(String[] columnNames) {
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i] == null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public JoinTable getJoinTable(EntityType<?> ownerType, String attributeName) {
        CollectionPersister persister = getCollectionPersister(ownerType, attributeName);
        if (persister instanceof QueryableCollection) {
            QueryableCollection queryableCollection = (QueryableCollection) persister;

            if (!queryableCollection.getElementType().isEntityType()) {
                String[] targetColumnMetaData = queryableCollection.getElementColumnNames();
                Map<String, String> targetColumnMapping = new LinkedHashMap<>();

                for (int i = 0; i < targetColumnMetaData.length; i++) {
                    targetColumnMapping.put(targetColumnMetaData[i], targetColumnMetaData[i]);
                }
                return createJoinTable(ownerType, queryableCollection, targetColumnMapping, null, attributeName);
            } else if (queryableCollection.getElementPersister() instanceof Joinable) {
                String elementTableName = ((Joinable) queryableCollection.getElementPersister()).getTableName();
                if (!queryableCollection.getTableName().equals(elementTableName)) {
                    String[] targetColumnMetaData = queryableCollection.getElementColumnNames();
                    AbstractEntityPersister elementPersister = (AbstractEntityPersister) queryableCollection.getElementPersister();
                    String identifierOrUniqueKeyPropertyName = ((ManyToOneType) persister.getElementType()).getIdentifierOrUniqueKeyPropertyName(persister.getFactory());
                    String[] targetPrimaryKeyColumnMetaData = identifierOrUniqueKeyPropertyName == null ?
                            elementPersister.getKeyColumnNames() : // IdClass returns null for getIdentifierOrUniqueKeyPropertyName
                            getColumnNames(elementPersister, identifierOrUniqueKeyPropertyName);

                    Map<String, String> targetIdColumnMapping = new LinkedHashMap<>();

                    for (int i = 0; i < targetColumnMetaData.length; i++) {
                        targetIdColumnMapping.put(targetColumnMetaData[i], targetPrimaryKeyColumnMetaData[i]);
                    }
                    Set<String> idAttributeNames = getColumnMatchingAttributeNames(elementPersister, Arrays.asList(targetPrimaryKeyColumnMetaData));
                    return createJoinTable(ownerType, queryableCollection, targetIdColumnMapping, idAttributeNames, attributeName);
                }
            }
        }
        return null;
    }

    private JoinTable createJoinTable(EntityType<?> ownerType, QueryableCollection queryableCollection, Map<String, String> targetColumnMapping, Set<String> targetIdAttributeNames, String attributeName) {
        String[] indexColumnNames = queryableCollection.getIndexColumnNames();
        Map<String, String> keyColumnMapping = null;
        Map<String, String> keyColumnTypes = null;
        if (indexColumnNames != null && indexColumnNames[0] != null) {
            keyColumnMapping = new LinkedHashMap<>(indexColumnNames.length);
            keyColumnTypes = new LinkedHashMap<>(indexColumnNames.length);
            if (queryableCollection.getKeyType().isEntityType()) {
                throw new IllegalArgumentException("Determining the join table key foreign key mappings is not yet supported!");
            } else {
                SessionFactoryImplementor sfi = queryableCollection.getFactory();
                Database database = sfi.getServiceRegistry().locateServiceBinding(Database.class).getService();
                Table[] tables = new Table[]{getTable(database, queryableCollection.getTableName())};
                keyColumnMapping.put(indexColumnNames[0], indexColumnNames[0]);
                keyColumnTypes.put(indexColumnNames[0], getColumnTypesForColumnNames(queryableCollection.getName(), queryableCollection.getIndexColumnNames(), tables)[0]);
            }
        }
        AbstractEntityPersister ownerEntityPersister = (AbstractEntityPersister) queryableCollection.getOwnerEntityPersister();
        String[] primaryKeyColumnMetaData;
        if (queryableCollection.getKeyType() instanceof EmbeddedComponentType) {
            String[] propertyNames = ((EmbeddedComponentType) queryableCollection.getKeyType()).getPropertyNames();
            List<String> columnNames = new ArrayList<>(propertyNames.length);
            for (String propertyName : propertyNames) {
                for (String propertyColumnName : getColumnNames(ownerEntityPersister, propertyName)) {
                    columnNames.add(propertyColumnName);
                }
            }

            primaryKeyColumnMetaData = columnNames.toArray(new String[columnNames.size()]);
        } else {
            primaryKeyColumnMetaData = ownerEntityPersister.getKeyColumnNames();
        }
        String[] foreignKeyColumnMetaData = queryableCollection.getKeyColumnNames();
        Map<String, String> idColumnMapping = new LinkedHashMap<>(primaryKeyColumnMetaData.length);
        for (int i = 0; i < foreignKeyColumnMetaData.length; i++) {
            idColumnMapping.put(foreignKeyColumnMetaData[i], primaryKeyColumnMetaData[i]);
        }
        Set<String> idAttributeNames = getColumnMatchingAttributeNames(ownerEntityPersister, Arrays.asList(primaryKeyColumnMetaData));
        if (targetIdAttributeNames == null) {
            Type elementType = queryableCollection.getElementType();
            if (elementType instanceof ComponentType) {
                targetIdAttributeNames = new LinkedHashSet<>();
                collectPropertyNames(targetIdAttributeNames, null, elementType, queryableCollection.getFactory());
            }
        }

        return new JoinTable(
                queryableCollection.getTableName(),
                idAttributeNames,
                idColumnMapping,
                keyColumnMapping,
                keyColumnTypes,
                targetIdAttributeNames,
                targetColumnMapping
        );
    }

    private static Set<String> getColumnMatchingAttributeNames(AbstractEntityPersister ownerEntityPersister, List<String> idColumnNames) {
        Set<String> idAttributeNames = new LinkedHashSet<>();
        Type identifierType = ownerEntityPersister.getIdentifierType();
        if (identifierType instanceof ComponentType) {
            String[] idPropertyNames = ((ComponentType) identifierType).getPropertyNames();
            for (String propertyName : idPropertyNames) {
                String attributeName = ownerEntityPersister.getIdentifierPropertyName() == null ? "" : ownerEntityPersister.getIdentifierPropertyName() + "." + propertyName;
                String[] propertyColumnNames = ownerEntityPersister.getSubclassPropertyColumnNames(attributeName);
                if (propertyColumnNames != null) {
                    for (int j = 0; j < propertyColumnNames.length; j++) {
                        String propertyColumnName = propertyColumnNames[j];
                        if (idColumnNames.contains(propertyColumnName)) {
                            idAttributeNames.add(attributeName);
                            break;
                        }
                    }
                }
            }
            // We assume that when a primary identifier attribute is part of the id column names, that we are done
            if (!idAttributeNames.isEmpty()) {
                return idAttributeNames;
            }
        } else {
            for (String identifierColumnName : ownerEntityPersister.getIdentifierColumnNames()) {
                if (idColumnNames.contains(identifierColumnName)) {
                    idAttributeNames.add(ownerEntityPersister.getIdentifierPropertyName());
                    return idAttributeNames;
                }
            }
        }
        String[] propertyNames = ownerEntityPersister.getPropertyNames();
        for (int i = 0; i < propertyNames.length; i++) {
            String propertyName = propertyNames[i];
            String[] propertyColumnNames = ownerEntityPersister.getSubclassPropertyColumnNames(propertyName);
            if (propertyColumnNames != null) {
                for (int j = 0; j < propertyColumnNames.length; j++) {
                    String propertyColumnName = propertyColumnNames[j];
                    if (idColumnNames.contains(propertyColumnName)) {
                        // We need to ignore/special case embedded component types
                        // In 5.x, these properties returned "null" column names so we didn't handle them
                        // In 6.0, the column names are correct but we don't want the synthetic property name
                        if (propertyName.charAt(0) == '_') {
                            Type propertyType = ownerEntityPersister.getSubclassPropertyType(i);
                            if (propertyType instanceof EmbeddedComponentType) {
                                idAttributeNames.addAll(Arrays.asList(((EmbeddedComponentType) propertyType).getPropertyNames()));
                                break;
                            }
                        }
                        idAttributeNames.add(propertyName);
                        break;
                    }
                }
            }
        }
        return idAttributeNames;
    }

    @Override
    public boolean isBag(EntityType<?> ownerType, String attributeName) {
        CollectionPersister persister = null;
        IdentifiableType<?> type = ownerType;
        String typeName = getTypeName(ownerType);
        StringBuilder sb = new StringBuilder(typeName.length() + attributeName.length() + 1);
        while (persister == null && type != null) {
            sb.setLength(0);
            sb.append(getTypeName(type));
            sb.append('.');
            sb.append(attributeName);
            persister = mappingMetamodel.findCollectionDescriptor(sb.toString());
            type = type.getSupertype();
        }

        return persister != null && !persister.hasIndex() && !persister.isInverse() && !(getAttribute(ownerType, attributeName) instanceof SetAttribute<?, ?>);
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String attributeName) {
        AbstractEntityPersister entityPersister = getEntityPersister(ownerType);
        if (entityPersister != null) {
            EntityMetamodel entityMetamodel = entityPersister.getEntityMetamodel();
            Integer index = entityMetamodel.getPropertyIndexOrNull(attributeName);
            if (index != null) {
                return entityMetamodel.getCascadeStyles()[index].hasOrphanDelete();
            }
        }

        return false;
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String elementCollectionPath, String attributeName) {
        Type elementType = getCollectionPersister(ownerType, elementCollectionPath).getElementType();
        if (!(elementType instanceof ComponentType)) {
            // This can only happen for collection/join table target attributes, where it is irrelevant
            return false;
        }
        ComponentType componentType = (ComponentType) elementType;
        String subAttribute = attributeName.substring(elementCollectionPath.length() + 1);
        // Component types only store direct properties, so we have to go deeper
        String[] propertyParts = subAttribute.split("\\.");
        int propertyIndex = 0;
        for (; propertyIndex < propertyParts.length - 1; propertyIndex++) {
            int index = componentType.getPropertyIndex(propertyParts[propertyIndex]);
            Type propertyType = componentType.getSubtypes()[index];
            if (propertyType instanceof ComponentType) {
                componentType = (ComponentType) propertyType;
            } else {
                // The association property is just as good as the id property of the association for our purposes
                // So we stop here and query the association property instead
                break;
            }
        }

        return componentType.getCascadeStyle(propertyIndex).hasOrphanDelete();
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String attributeName) {
        AbstractEntityPersister entityPersister = getEntityPersister(ownerType);
        if (entityPersister != null) {
            EntityMetamodel entityMetamodel = entityPersister.getEntityMetamodel();
            Integer index = entityMetamodel.getPropertyIndexOrNull(attributeName);
            if (index != null) {
                return entityMetamodel.getCascadeStyles()[index].doCascade(CascadingActions.DELETE);
            }
        }

        return false;
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String elementCollectionPath, String attributeName) {
        Type elementType = getCollectionPersister(ownerType, elementCollectionPath).getElementType();
        if (!(elementType instanceof ComponentType)) {
            // This can only happen for collection/join table target attributes, where it is irrelevant
            return false;
        }
        ComponentType componentType = (ComponentType) elementType;
        String subAttribute = attributeName.substring(elementCollectionPath.length() + 1);
        // Component types only store direct properties, so we have to go deeper
        String[] propertyParts = subAttribute.split("\\.");
        int propertyIndex = 0;
        for (; propertyIndex < propertyParts.length - 1; propertyIndex++) {
            int index = componentType.getPropertyIndex(propertyParts[propertyIndex]);
            Type propertyType = componentType.getSubtypes()[index];
            if (propertyType instanceof ComponentType) {
                componentType = (ComponentType) propertyType;
            } else {
                // The association property is just as good as the id property of the association for our purposes
                // So we stop here and query the association property instead
                break;
            }
        }
        int leafPropertyIndex = componentType.getPropertyIndex(propertyParts[propertyIndex]);
        return componentType.getCascadeStyle(leafPropertyIndex).doCascade(CascadingActions.DELETE);
    }

    @Override
    public boolean hasJoinCondition(ManagedType<?> owner, String elementCollectionPath, String attributeName) {
        QueryableCollection persister;
        Type propertyType;
        AbstractEntityPersister entityPersister = getEntityPersister(owner);
        if (entityPersister == null) {
            return false;
        }
        SessionFactoryImplementor factory = entityPersister.getFactory();
        if (elementCollectionPath != null && (persister = getCollectionPersister(owner, elementCollectionPath)) != null) {
            Type elementType = persister.getElementType();
            if (!(elementType instanceof ComponentType)) {
                // This can only happen for collection/join table target attributes, where it is irrelevant
                return false;
            }
            ComponentType componentType = (ComponentType) elementType;
            String subAttribute = attributeName.substring(elementCollectionPath.length() + 1);
            // Component types only store direct properties, so we have to go deeper
            String[] propertyParts = subAttribute.split("\\.");
            for (int i = 0; i < propertyParts.length - 1; i++) {
                int index = componentType.getPropertyIndex(propertyParts[i]);
                propertyType = componentType.getSubtypes()[index];
                if (propertyType instanceof ComponentType) {
                    componentType = (ComponentType) propertyType;
                } else {
                    // This can only happen for collection/join table target attributes, where it is irrelevant
                    return false;
                }
            }

            propertyType = componentType.getSubtypes()[componentType.getPropertyIndex(propertyParts[propertyParts.length - 1])];
        } else {
            propertyType = getPropertyType(entityPersister, attributeName);
        }

        if (propertyType instanceof CollectionType) {
            return ((QueryableCollection) mappingMetamodel.findCollectionDescriptor(((CollectionType) propertyType).getRole())).hasWhere();
        } else {
            if (propertyType instanceof org.hibernate.type.EntityType) {
                AbstractEntityPersister elementPersister = (AbstractEntityPersister) mappingMetamodel.findEntityDescriptor(((org.hibernate.type.EntityType) propertyType).getAssociatedEntityName());
                // I know this is ugly for determining whether a @Where predicate exists, but that's as good as it gets for now
                try {
                    elementPersister.applyWhereRestrictions(null, null, true, null);
                    return false;
                } catch (NullPointerException ex) {
                    return true;
                }
            }
        }

        return false;
    }

    private Type getPropertyType(AbstractEntityPersister entityPersister, String attributeName) {
        try {
            return entityPersister.getPropertyType(attributeName);
        } catch (QueryException ex) {
            // Workaround for HHH-15051
            int dotIndex = attributeName.lastIndexOf('.');
            if (dotIndex != -1) {
                Type propertyType = getPropertyType(entityPersister, attributeName.substring(0, dotIndex));
                if (propertyType instanceof org.hibernate.type.EntityType) {
                    org.hibernate.type.EntityType entityType = (org.hibernate.type.EntityType) propertyType;
                    Type idType = entityType.getIdentifierOrUniqueKeyType(entityPersister.getFactory());
                    String attributeSubName = attributeName.substring(dotIndex + 1);
                    if (idType instanceof CompositeType && ((CompositeType) idType).isEmbedded()) {
                        CompositeType idClassType = (CompositeType) idType;
                        int propertyIndex = Arrays.asList(idClassType.getPropertyNames()).indexOf(attributeSubName);
                        if (propertyIndex != -1) {
                            return idClassType.getSubtypes()[propertyIndex];
                        }
                    } else if (attributeSubName.equals(entityType.getIdentifierOrUniqueKeyPropertyName(entityPersister.getFactory()))) {
                        return idType;
                    }
                }
            }
            throw ex;
        }
    }

    @Override
    public boolean containsEntity(EntityManager em, Class<?> entityClass, Object id) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        EntityKey entityKey = session.generateEntityKey((Serializable) id, mappingMetamodel.findEntityDescriptor(entityClass.getName()));
        PersistenceContext pc = session.getPersistenceContext();
        return pc.getEntity(entityKey) != null || pc.getProxy(entityKey) != null;
    }

    private Attribute<?, ?> getAttribute(EntityType<?> ownerType, String attributeName) {
        if (attributeName.indexOf('.') == -1) {
            return ownerType.getAttribute(attributeName);
        }
        ManagedType<?> t = ownerType;
        Attribute<?, ?> attr = null;
        String[] parts = attributeName.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            attr = t.getAttribute(parts[i]);
            if (i + 1 != parts.length) {
                if (attr instanceof SingularAttribute<?, ?>) {
                    SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attr;
                    if (singularAttribute.getType().getPersistenceType() != jakarta.persistence.metamodel.Type.PersistenceType.BASIC) {
                        t = (ManagedType<?>) singularAttribute.getType();
                        continue;
                    }
                }

                throw new IllegalArgumentException("Illegal attribute name for type [" + ownerType.getJavaType().getName() + "]: " + attributeName);
            }
        }

        return attr;
    }

    @Override
    public boolean supportsSingleValuedAssociationIdExpressions() {
        return true;
    }

    @Override
    public boolean supportsUpdateSetEmbeddable() {
        // Tried it, but the SQL generation seems to mess up...
        return false;
    }

    @Override
    public boolean supportsUpdateSetAssociationId() {
        return true;
    }

    @Override
    public boolean supportsTransientEntityAsParameter() {
        return true;
    }

    @Override
    public boolean needsTypeConstraintForColumnSharing() {
        return true;
    }

    @Override
    public void setCacheable(Query query) {
        query.setHint("org.hibernate.cacheable", true);
    }

    @Override
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> owner, String attributeName) {
        return new ArrayList<>(getJoinMappingPropertyNames(owner, null, attributeName).keySet());
    }

    @Override
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> owner, String elementCollectionPath, String attributeName) {
        return new ArrayList<>(getJoinMappingPropertyNames(owner, elementCollectionPath, attributeName).keySet());
    }

    @Override
    public Map<String, String> getJoinMappingPropertyNames(EntityType<?> owner, String elementCollectionPath, String attributeName) {
        QueryableCollection persister;
        Type propertyType;
        AbstractEntityPersister entityPersister = getEntityPersister(owner);
        SessionFactoryImplementor factory = entityPersister.getFactory();
        if (elementCollectionPath != null && (persister = getCollectionPersister(owner, elementCollectionPath)) != null) {
            ComponentType componentType = (ComponentType) persister.getElementType();
            String subAttribute = attributeName.substring(elementCollectionPath.length() + 1);
            // Component types only store direct properties, so we have to go deeper
            String[] propertyParts = subAttribute.split("\\.");
            for (int i = 0; i < propertyParts.length - 1; i++) {
                int index = componentType.getPropertyIndex(propertyParts[i]);
                propertyType = componentType.getSubtypes()[index];
                if (propertyType instanceof ComponentType) {
                    componentType = (ComponentType) propertyType;
                } else {
                    // A path expression shouldn't navigate over an association..
                    throw new IllegalStateException("Can't get the id properties for: " + attributeName);
                }
            }

            propertyType = componentType.getSubtypes()[componentType.getPropertyIndex(propertyParts[propertyParts.length - 1])];
        } else {
            propertyType = getPropertyType(entityPersister, attributeName);
        }

        List<String> identifierOrUniqueKeyPropertyNames = new ArrayList<>();
        List<String> sourceIdentifierOrUniqueKeyPropertyNames = new ArrayList<>();

        if (propertyType instanceof CollectionType) {
            Type elementType = ((CollectionType) propertyType).getElementType(factory);
            if (elementType instanceof org.hibernate.type.EntityType) {
                JoinTable joinTable = getJoinTable(owner, attributeName);
                Collection<String> targetAttributeNames = joinTable == null ? null : joinTable.getTargetAttributeNames();
                if (targetAttributeNames == null) {
                    collectPropertyNames(identifierOrUniqueKeyPropertyNames, null, elementType, factory);
                    String mappedBy = getMappedBy(getCollectionPersister(owner, attributeName));
                    if (mappedBy == null || mappedBy.isEmpty()) {
                        // Not sure if this can happen. If we can't determine targetAttributeNames or don't have a join table
                        // but also no mapped by attribute, I don't know if that is even a valid/possible mapping
                        if (((CollectionType) propertyType).useLHSPrimaryKey()) {
                            // TODO: this doesn't work for entities using an id class
                            sourceIdentifierOrUniqueKeyPropertyNames.add(entityPersister.getIdentifierPropertyName());
                        } else {
                            throw new IllegalArgumentException("One-to-many using natural key is unsupported!");
                        }
                    } else {
                        AbstractEntityPersister elementPersister = (AbstractEntityPersister) mappingMetamodel.findEntityDescriptor(((org.hibernate.type.EntityType) elementType).getAssociatedEntityName());
                        collectPropertyNames(sourceIdentifierOrUniqueKeyPropertyNames, null, getPropertyType(elementPersister, mappedBy), factory);
                        //                        if (sourceIdentifierOrUniqueKeyPropertyNames.size() != identifierOrUniqueKeyPropertyNames.size()) {
                        //                            // We have a inverse map or inverse indexed list here
                        //                            // Maybe at some point we can determine the index property name mapping as well
                        //                            persister = getCollectionPersister(owner, attributeName);
                        //                            Set<String> indexAttributeNames = getColumnMatchingAttributeNames(elementPersister, Arrays.asList(persister.getIndexColumnNames()));
                        //                            persister.getIndexType()
                        //                        }
                    }
                } else {
                    AbstractEntityPersister elementPersister = (AbstractEntityPersister) mappingMetamodel.findEntityDescriptor(((org.hibernate.type.EntityType) elementType).getAssociatedEntityName());
                    for (String targetAttributeName : targetAttributeNames) {
                        collectPropertyNames(identifierOrUniqueKeyPropertyNames, targetAttributeName, getPropertyType(elementPersister, targetAttributeName), factory);
                    }
                    for (String idAttributeName : joinTable.getIdAttributeNames()) {
                        collectPropertyNames(sourceIdentifierOrUniqueKeyPropertyNames, idAttributeName, getPropertyType(entityPersister, idAttributeName), factory);
                    }
                }
            }
        } else {
            collectPropertyNames(identifierOrUniqueKeyPropertyNames, null, propertyType, factory);
            if (propertyType instanceof org.hibernate.type.EntityType) {
                AbstractEntityPersister elementPersister = (AbstractEntityPersister) mappingMetamodel.findEntityDescriptor(((org.hibernate.type.EntityType) propertyType).getAssociatedEntityName());
                if (((org.hibernate.type.EntityType) propertyType).isReferenceToPrimaryKey()) {
                    if (elementPersister.getIdentifierType().isComponentType()) {
                        String targetPropertyPrefix = elementPersister.getIdentifierPropertyName() == null ? attributeName + "." : "";
                        for (int i = 0; i < identifierOrUniqueKeyPropertyNames.size(); i++) {
                            sourceIdentifierOrUniqueKeyPropertyNames.add(targetPropertyPrefix + identifierOrUniqueKeyPropertyNames.get(i));
                        }
                    } else {
                        sourceIdentifierOrUniqueKeyPropertyNames.add(elementPersister.getIdentifierPropertyName());
                    }
                } else if (propertyType instanceof OneToOneType) {
                    String mappedBy = ((OneToOneType) propertyType).getRHSUniqueKeyPropertyName();
                    if (mappedBy == null || mappedBy.isEmpty()) {
                        throw new IllegalArgumentException("One-to-one using natural key is unsupported!");
                    } else {
                        collectPropertyNames(sourceIdentifierOrUniqueKeyPropertyNames, null, getPropertyType(elementPersister, mappedBy), factory);
                    }
                }
            }
        }

        Map<String, String> joinMapping = new LinkedHashMap<>(identifierOrUniqueKeyPropertyNames.size());
        for (int i = 0; i < identifierOrUniqueKeyPropertyNames.size(); i++) {
            if (sourceIdentifierOrUniqueKeyPropertyNames.size() > i) {
                joinMapping.put(identifierOrUniqueKeyPropertyNames.get(i), sourceIdentifierOrUniqueKeyPropertyNames.get(i));
            } else {
                joinMapping.put(identifierOrUniqueKeyPropertyNames.get(i), null);
            }
        }

        return joinMapping;
    }

    private void collectPropertyNames(Collection<String> propertyNames, String prefix, Type propertyType, Mapping factory) {
        if (propertyType instanceof ComponentType) {
            ComponentType componentType = (ComponentType) propertyType;
            for (String propertyName : componentType.getPropertyNames()) {
                Type subtype = componentType.getSubtypes()[componentType.getPropertyIndex(propertyName)];
                collectPropertyNames(propertyNames, prefix == null ? propertyName : prefix + "." + propertyName, subtype, factory);
            }
        } else if (propertyType instanceof org.hibernate.type.EntityType) {
            org.hibernate.type.EntityType entityType = (org.hibernate.type.EntityType) propertyType;
            Type identifierOrUniqueKeyType = entityType.getIdentifierOrUniqueKeyType(factory);

            if (identifierOrUniqueKeyType instanceof EmbeddedComponentType) {
                EmbeddedComponentType embeddedComponentType = (EmbeddedComponentType) identifierOrUniqueKeyType;
                for (String propertyName : embeddedComponentType.getPropertyNames()) {
                    propertyNames.add(prefix == null ? propertyName : prefix + "." + propertyName);
                }
            } else {
                String identifierOrUniqueKeyPropertyName = entityType.getIdentifierOrUniqueKeyPropertyName(factory);
                collectPropertyNames(propertyNames, prefix == null ? identifierOrUniqueKeyPropertyName : prefix + "." + identifierOrUniqueKeyPropertyName, identifierOrUniqueKeyType, factory);
            }
        } else if (!(propertyType instanceof CollectionType) && prefix != null) {
            propertyNames.add(prefix);
        }
    }

    @Override
    public boolean supportsEnumLiteral(ManagedType<?> ownerType, String attributeName, boolean key) {
        if (ownerType instanceof EntityType<?>) {
            AbstractEntityPersister entityPersister = getEntityPersister(ownerType);
            Type propertyType;
            propertyType = getPropertyType(entityPersister, attributeName);
            if (propertyType instanceof CollectionType) {
                CollectionPersister collectionPersister = mappingMetamodel.findCollectionDescriptor(((CollectionType) propertyType).getRole());
                if (key) {
                    propertyType = collectionPersister.getIndexType();
                } else {
                    propertyType = collectionPersister.getElementType();
                }
            }
            if (propertyType instanceof CustomType) {
                return ((CustomType) propertyType).getUserType() instanceof EnhancedUserType;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean supportsTemporalLiteral() {
        return false;
    }

    @Override
    public boolean supportsNonDrivingAliasInOnClause() {
        return true;
    }

    @Override
    public boolean supportsSelectCompositeIdEntityInSubquery() {
        // Not yet implemented in Hibernate, see https://hibernate.atlassian.net/browse/HHH-14156
        return false;
    }

    @Override
    public boolean supportsProxyParameterForNonPkAssociation() {
        // Not yet implemented in Hibernate, see https://hibernate.atlassian.net/browse/HHH-14017
        return false;
    }

    @Override
    public Object getIdentifier(Object entity) {
        // Pre Hibernate 5.2, accessing the identifier through the PersistenceUnitUtil caused initialization of the proxy
        if (entity instanceof HibernateProxy) {
            return ((HibernateProxy) entity).getHibernateLazyInitializer().getIdentifier();
        }
        return persistenceUnitUtil.getIdentifier(entity);
    }

    @Override
    public <T> T unproxy(T entity) {
        if (entity instanceof HibernateProxy) {
            HibernateProxy hibernateProxy = (HibernateProxy) entity;
            LazyInitializer initializer = hibernateProxy.getHibernateLazyInitializer();
            return (T) initializer.getImplementation();
        } else {
            return entity;
        }
    }

    @Override
    public JpaMetamodelAccessor getJpaMetamodelAccessor() {
        return JpaMetamodelAccessorImpl.INSTANCE;
    }

}
