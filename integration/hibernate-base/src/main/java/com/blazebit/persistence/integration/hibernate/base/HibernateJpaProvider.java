/*
 * Copyright 2014 - 2018 Blazebit.
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
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaProvider;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.OneToOneType;
import org.hibernate.type.Type;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class HibernateJpaProvider implements JpaProvider {

    protected final DB db;
    protected final Map<String, EntityPersister> entityPersisters;
    protected final Map<String, CollectionPersister> collectionPersisters;

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

    public HibernateJpaProvider(String dbms, Map<String, EntityPersister> entityPersisters, Map<String, CollectionPersister> collectionPersisters) {
        try {
            if ("mysql".equals(dbms)) {
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
    public boolean isForeignJoinColumn(EntityType<?> ownerType, String attributeName) {
        AbstractEntityPersister persister = (AbstractEntityPersister) entityPersisters.get(ownerType.getJavaType().getName());
        Type propertyType = persister.getPropertyType(attributeName);

        if (propertyType instanceof OneToOneType) {
            return ((OneToOneType) propertyType).getRHSUniqueKeyPropertyName() != null;
        }

        // Every entity persister has "owned" properties on table number 0, others have higher numbers
        int tableNumber = persister.getSubclassPropertyTableNumber(attributeName);
        return tableNumber >= persister.getEntityMetamodel().getSubclassEntityNames().size();
    }

    @Override
    public boolean isColumnShared(EntityType<?> ownerType, String attributeName) {
        AbstractEntityPersister persister = (AbstractEntityPersister) entityPersisters.get(ownerType.getJavaType().getName());
        if (!(persister instanceof SingleTableEntityPersister) && !(persister instanceof UnionSubclassEntityPersister)) {
            return false;
        }

        if (persister instanceof SingleTableEntityPersister) {
            SingleTableEntityPersister singleTableEntityPersister = (SingleTableEntityPersister) persister;
            SingleTableEntityPersister rootPersister = (SingleTableEntityPersister) entityPersisters.get(singleTableEntityPersister.getRootEntityName());
            return isColumnShared(singleTableEntityPersister, rootPersister.getName(), rootPersister.getSubclassClosure(), attributeName);
        } else if (persister instanceof UnionSubclassEntityPersister) {
            UnionSubclassEntityPersister unionSubclassEntityPersister = (UnionSubclassEntityPersister) persister;
            UnionSubclassEntityPersister rootPersister = (UnionSubclassEntityPersister) entityPersisters.get(unionSubclassEntityPersister.getRootEntityName());
            return isColumnShared(unionSubclassEntityPersister, rootPersister.getName(), rootPersister.getSubclassClosure(), attributeName);
        }

        return false;
    }

    @Override
    public ConstraintType requiresTreatFilter(EntityType<?> ownerType, String attributeName, JoinType joinType) {
        // When we don't support treat joins(Hibernate 4.2), we need to put the constraints into the WHERE clause
        if (!supportsTreatJoin() && joinType == JoinType.INNER) {
            return ConstraintType.WHERE;
        }
        AbstractEntityPersister persister = (AbstractEntityPersister) entityPersisters.get(ownerType.getJavaType().getName());
        Type propertyType = persister.getPropertyType(attributeName);

        if (!(propertyType instanceof AssociationType)) {
            return ConstraintType.NONE;
        }

        // When the inner treat joined element is collection, we always need the constraint, see HHH-??? TODO: report issue
        if (propertyType instanceof CollectionType) {
            if (joinType == JoinType.INNER) {
                if (isForeignKeyDirectionToParent((CollectionType) propertyType)) {
                    return ConstraintType.WHERE;
                }

                return ConstraintType.ON;
            } else if (!((CollectionType) propertyType).getElementType(persister.getFactory()).isEntityType()) {
                return ConstraintType.NONE;
            }
        }

        String propertyEntityName = ((AssociationType) propertyType).getAssociatedEntityName(persister.getFactory());
        AbstractEntityPersister propertyTypePersister = (AbstractEntityPersister) entityPersisters.get(propertyEntityName);

        // When the treat joined element is a union subclass, we always need the constraint, see HHH-??? TODO: report issue
        if (propertyTypePersister instanceof UnionSubclassEntityPersister) {
            return ConstraintType.ON;
        }

        return ConstraintType.NONE;
    }

    protected boolean isForeignKeyDirectionToParent(CollectionType collectionType) {
        ForeignKeyDirection direction = collectionType.getForeignKeyDirection();
        // Types changed between 4 and 5 so we check it like this. Essentially we check if the TO_PARENT direction is used
        return direction.toString().regionMatches(true, 0, "to", 0, 2);
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
    public String getMappedBy(EntityType<?> ownerType, String attributeName) {
        String ownerTypeName = ownerType.getJavaType().getName();
        StringBuilder sb = new StringBuilder(ownerTypeName.length() + attributeName.length() + 1);
        sb.append(ownerType.getJavaType().getName());
        sb.append('.');
        sb.append(attributeName);

        CollectionPersister persister = collectionPersisters.get(sb.toString());
        if (persister != null) {
            if (persister.isInverse()) {
                return getMappedBy(persister);
            }
        } else {
            EntityPersister entityPersister = entityPersisters.get(ownerType.getJavaType().getName());
            Type propertyType = entityPersister.getPropertyType(attributeName);
            if (propertyType instanceof OneToOneType) {
                return ((OneToOneType) propertyType).getRHSUniqueKeyPropertyName();
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getWritableMappedByMappings(EntityType<?> inverseType, EntityType<?> ownerType, String attributeName) {
        AbstractEntityPersister entityPersister = (AbstractEntityPersister) entityPersisters.get(ownerType.getJavaType().getName());
        int propertyIndex = entityPersister.getEntityMetamodel().getPropertyIndex(attributeName);
        // Either the mapped by property is writable
        if (entityPersister.getEntityMetamodel().getPropertyInsertability()[propertyIndex]) {
            return null;
        }
        // Or the columns of the mapped by property are part of the target id
        org.hibernate.type.EntityType propertyType = (org.hibernate.type.EntityType) entityPersister.getPropertyType(attributeName);
        AbstractEntityPersister sourceType = (AbstractEntityPersister) entityPersisters.get(propertyType.getAssociatedEntityName());
        Type identifierType = propertyType.getIdentifierOrUniqueKeyType(null);
        String sourcePropertyPrefix;
        String[] sourcePropertyNames;
        if (identifierType.isComponentType()) {
            ComponentType componentType = (ComponentType) identifierType;
            sourcePropertyPrefix = sourceType.getIdentifierPropertyName() + ".";
            sourcePropertyNames = componentType.getPropertyNames();
        } else {
            sourcePropertyPrefix = "";
            sourcePropertyNames = new String[]{ sourceType.getIdentifierPropertyName() };
        }
        String[] targetColumnNames = entityPersister.getPropertyColumnNames(propertyIndex);
        Type targetIdType = entityPersister.getIdentifierType();
        if (targetIdType.isComponentType()) {
            ComponentType targetIdentifierType = (ComponentType) entityPersister.getIdentifierType();
            String targetPropertyPrefix = entityPersister.getIdentifierPropertyName() + ".";
            String[] identifierColumnNames = entityPersister.getIdentifierColumnNames();
            String[] targetIdentifierTypePropertyNames = targetIdentifierType.getPropertyNames();
            Map<String, String> mapping = new HashMap<>();
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
            Map<String, String> mapping = new HashMap<>();
            mapping.put(sourcePropertyPrefix + sourcePropertyNames[0], entityPersister.getIdentifierPropertyName());
            return mapping;
        }
    }

    protected String getMappedBy(CollectionPersister persister) {
        if (persister instanceof CustomCollectionPersister) {
            return ((CustomCollectionPersister) persister).getMappedByProperty();
        }

        throw new IllegalStateException("Custom persister configured that doesn't implement the CustomCollectionPersister interface: " + persister);
    }

    @Override
    public String[] getColumnNames(EntityType<?> entityType, String attributeName) {
        try {
            return ((AbstractEntityPersister) entityPersisters.get(entityType.getJavaType().getName())).getPropertyColumnNames(attributeName);
        } catch (MappingException e) {
            throw new RuntimeException("Unknown property [" + attributeName + "] of entity [" + entityType.getJavaType() + "]", e);
        }
    }

    @Override
    public String[] getColumnTypes(EntityType<?> entityType, String attributeName) {
        AbstractEntityPersister entityPersister = (AbstractEntityPersister) entityPersisters.get(entityType.getJavaType().getName());
        SessionFactoryImplementor sfi = entityPersister.getFactory();
        String[] columnNames = entityPersister.getPropertyColumnNames(attributeName);
        Database database = sfi.getServiceRegistry().locateServiceBinding(Database.class).getService();
        Table[] tables;

        if (entityPersister instanceof JoinedSubclassEntityPersister) {
            tables = new Table[((JoinedSubclassEntityPersister) entityPersister).getSubclassTableSpan()];
            for (int i = 0; i < tables.length; i++) {
                tables[i] = database.getTable(entityPersister.getSubclassTableName(i));
            }
        } else if (entityPersister instanceof UnionSubclassEntityPersister) {
            tables = new Table[((UnionSubclassEntityPersister) entityPersister).getSubclassTableSpan()];
            for (int i = 0; i < tables.length; i++) {
                tables[i] = database.getTable(entityPersister.getSubclassTableName(i));
            }
        } else if (entityPersister instanceof SingleTableEntityPersister) {
            tables = new Table[((SingleTableEntityPersister) entityPersister).getSubclassTableSpan()];
            for (int i = 0; i < tables.length; i++) {
                tables[i] = database.getTable(entityPersister.getSubclassTableName(i));
            }
        } else {
            tables = new Table[] { database.getTable(entityPersister.getTableName()) };
        }

        // In this case, the property might represent a formula
        if (columnNames.length == 1 && columnNames[0] == null) {
            Type propertyType = entityPersister.getPropertyType(attributeName);
            long length;
            int precision;
            int scale;
            try {
                Method m = Type.class.getMethod("dictatedSizes", Mapping.class);
                Object size = ((Object[]) m.invoke(propertyType, sfi))[0];
                length =    (long) size.getClass().getMethod("getLength").invoke(size);
                precision = (int)  size.getClass().getMethod("getPrecision").invoke(size);
                scale =     (int)  size.getClass().getMethod("getScale").invoke(size);
            } catch (Exception ex) {
                throw new RuntimeException("Could not determine the column type of the attribute: " + attributeName + " of the entity: " + entityType.getName());
            }

            return new String[] {
                    sfi.getDialect().getTypeName(
                            propertyType.sqlTypes(sfi)[0],
                            length,
                            precision,
                            scale
                    )
            };
        }

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
                throw new IllegalArgumentException("Could not find column '" + columnNames[i] + "' in for entity: " + entityType.getName());
            }

            columnTypes[i] = column.getSqlType();
        }

        return columnTypes;
    }

    @Override
    public JoinTable getJoinTable(EntityType<?> ownerType, String attributeName) {
        String ownerTypeName = ownerType.getJavaType().getName();
        StringBuilder sb = new StringBuilder(ownerTypeName.length() + attributeName.length() + 1);
        sb.append(ownerType.getJavaType().getName());
        sb.append('.');
        sb.append(attributeName);

        CollectionPersister persister = collectionPersisters.get(sb.toString());
        if (persister instanceof QueryableCollection) {
            QueryableCollection queryableCollection = (QueryableCollection) persister;

            if (!queryableCollection.getElementType().isEntityType()) {
                String[] targetColumnMetaData = queryableCollection.getElementColumnNames();
                Map<String, String> targetColumnMapping = new HashMap<>();

                for (int i = 0; i < targetColumnMetaData.length; i++) {
                    targetColumnMapping.put(targetColumnMetaData[i], targetColumnMetaData[i]);
                }
                return createJoinTable(queryableCollection, targetColumnMapping);
            } else if (queryableCollection.getElementPersister() instanceof Joinable) {
                String elementTableName = ((Joinable) queryableCollection.getElementPersister()).getTableName();
                if (!queryableCollection.getTableName().equals(elementTableName)) {
                    String[] targetColumnMetaData = queryableCollection.getElementColumnNames();
                    String[] targetPrimaryKeyColumnMetaData = ((AbstractEntityPersister) queryableCollection.getElementPersister()).getKeyColumnNames();
                    Map<String, String> targetIdColumnMapping = new HashMap<>();

                    for (int i = 0; i < targetColumnMetaData.length; i++) {
                        targetIdColumnMapping.put(targetColumnMetaData[i], targetPrimaryKeyColumnMetaData[i]);
                    }
                    return createJoinTable(queryableCollection, targetIdColumnMapping);
                }
            }
        }
        return null;
    }

    private JoinTable createJoinTable(QueryableCollection queryableCollection, Map<String, String> targetColumnMapping) {
        String[] indexColumnNames = queryableCollection.getIndexColumnNames();
        Map<String, String> keyColumnMapping = null;
        if (indexColumnNames != null) {
            keyColumnMapping = new HashMap<>(indexColumnNames.length);
            if (queryableCollection.getKeyType().isEntityType()) {
                throw new IllegalArgumentException("Determining the join table key foreign key mappings is not yet supported!");
            } else {
                keyColumnMapping.put(indexColumnNames[0], indexColumnNames[0]);
            }
        }
        String[] primaryKeyColumnMetaData = ((AbstractEntityPersister) queryableCollection.getOwnerEntityPersister()).getKeyColumnNames();
        String[] foreignKeyColumnMetaData = queryableCollection.getKeyColumnNames();
        Map<String, String> idColumnMapping = new HashMap<>(primaryKeyColumnMetaData.length);
        for (int i = 0; i < foreignKeyColumnMetaData.length; i++) {
            idColumnMapping.put(foreignKeyColumnMetaData[i], primaryKeyColumnMetaData[i]);
        }

        return new JoinTable(
                queryableCollection.getTableName(),
                idColumnMapping,
                keyColumnMapping,
                targetColumnMapping
        );
    }

    @Override
    public boolean isBag(EntityType<?> ownerType, String attributeName) {
        CollectionPersister persister = null;
        IdentifiableType<?> type = ownerType;
        StringBuilder sb = new StringBuilder(ownerType.getJavaType().getName().length() + attributeName.length() + 1);
        while (persister == null && type != null) {
            sb.setLength(0);
            sb.append(type.getJavaType().getName());
            sb.append('.');
            sb.append(attributeName);
            persister = collectionPersisters.get(sb.toString());
            type = type.getSupertype();
        }

        return persister != null && !persister.hasIndex() && !persister.isInverse() && !(getAttribute(ownerType, attributeName) instanceof SetAttribute<?, ?>);
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String attributeName) {
        AbstractEntityPersister entityPersister = (AbstractEntityPersister) entityPersisters.get(ownerType.getJavaType().getName());
        EntityMetamodel entityMetamodel = entityPersister.getEntityMetamodel();
        Integer index = entityMetamodel.getPropertyIndexOrNull(attributeName);
        if (index != null) {
            return entityMetamodel.getCascadeStyles()[index].hasOrphanDelete();
        }

        return false;
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String attributeName) {
        AbstractEntityPersister entityPersister = (AbstractEntityPersister) entityPersisters.get(ownerType.getJavaType().getName());
        EntityMetamodel entityMetamodel = entityPersister.getEntityMetamodel();
        Integer index = entityMetamodel.getPropertyIndexOrNull(attributeName);
        if (index != null) {
            return entityMetamodel.getCascadeStyles()[index].doCascade(CascadingAction.DELETE);
        }

        return false;
    }

    @Override
    public boolean containsEntity(EntityManager em, Class<?> entityClass, Object id) {
        SessionImplementor session = em.unwrap(SessionImplementor.class);
        EntityKey entityKey = session.generateEntityKey((Serializable) id, session.getFactory().getEntityPersister(entityClass.getName()));
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
                    if (singularAttribute.getType().getPersistenceType() != javax.persistence.metamodel.Type.PersistenceType.BASIC) {
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
    public boolean supportsForeignAssociationInOnClause() {
        return false;
    }

    @Override
    public boolean supportsUpdateSetEmbeddable() {
        // Tried it, but the SQL generation seems to mess up...
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

    @Override
    public boolean supportsCollectionTableCleanupOnDelete() {
        return false;
    }

    @Override
    public boolean supportsJoinTableCleanupOnDelete() {
        return true;
    }

    @Override
    public void setCacheable(Query query) {
        query.setHint("org.hibernate.cacheable", true);
    }
}
