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
import com.blazebit.persistence.integration.jpa.JpaMetamodelAccessorImpl;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaMetamodelAccessor;
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
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EmbeddedComponentType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.OneToOneType;
import org.hibernate.type.Type;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class HibernateJpaProvider implements JpaProvider {

    private static final Method GET_TYPE_NAME;
    private static final Logger LOG = Logger.getLogger(HibernateJpaProvider.class.getName());

    protected final PersistenceUnitUtil persistenceUnitUtil;
    protected final DB db;
    protected final Map<String, EntityPersister> entityPersisters;
    protected final Map<String, CollectionPersister> collectionPersisters;

    private final boolean useQuoted;
    private final boolean supportsEntityJoin;
    private final boolean needsJoinSubqueryRewrite;
    private final boolean supportsForeignAssociationInOnClause;
    private final boolean needsAssociationToIdRewriteInOnClause;
    private final boolean needsBrokenAssociationToIdRewriteInOnClause;
    private final boolean supportsCollectionTableCleanupOnDelete;
    private final boolean supportsJoinTableCleanupOnDelete;
    private final boolean needsCorrelationPredicateWhenCorrelatingWithWhereClause;
    private final boolean supportsSingleValuedAssociationNaturalIdExpressions;
    private final boolean needsElementCollectionIdCutoffForCompositeIdOwner;

    static {
        Class<?> typeClass = null;
        // Hibernate 5.2+
        try {
            typeClass = Class.forName("org.hibernate.metamodel.internal.AbstractType");
        } catch (ClassNotFoundException cnfe) {
            // Ignore
        }
        // Hibernate 5.4+
        if (typeClass == null) {
            try {
                typeClass = Class.forName("org.hibernate.metamodel.model.domain.internal.AbstractType");
            } catch (ClassNotFoundException cnfe) {
                // Ignore
            }
        }
        // Hibernate 4.3+
        if (typeClass == null) {
            try {
                typeClass = Class.forName("org.hibernate.jpa.internal.metamodel.AbstractType");
            } catch (ClassNotFoundException cnfe) {
                // Ignore
            }
        }
        // Hibernate 4.2
        if (typeClass == null) {
            try {
                typeClass = Class.forName("org.hibernate.ejb.metamodel.AbstractType");
            } catch (ClassNotFoundException cnfe) {
                // Ignore
            }
        }
        if (typeClass != null) {
            try {
                GET_TYPE_NAME = typeClass.getMethod("getTypeName");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Unknown Hibernate version in use, could not find getTypeName method!", e);
            }
        } else {
            throw new IllegalStateException("Unknown Hibernate version in use, could not find AbstractType JPA metamodel class!");
        }
    }

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

    public HibernateJpaProvider(PersistenceUnitUtil persistenceUnitUtil, String dbms, Map<String, EntityPersister> entityPersisters, Map<String, CollectionPersister> collectionPersisters, int major, int minor, int fix, String type) {
        this.persistenceUnitUtil = persistenceUnitUtil;
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
            // Since Hibernate 5.0 the physical schema uses quoted identifiers as well
            this.useQuoted = major > 4;
            this.supportsEntityJoin = major > 5 || major == 5 && minor >= 1;
            // Got fixed in 5.2.3: https://hibernate.atlassian.net/browse/HHH-9329 but is still buggy: https://hibernate.atlassian.net/browse/HHH-11401
            this.needsJoinSubqueryRewrite = major < 5 || major == 5 && minor < 2 || major == 5 && minor == 2 && fix < 7;
            // Got fixed in 5.2.8: https://hibernate.atlassian.net/browse/HHH-11450
            this.supportsForeignAssociationInOnClause = major > 5 || major == 5 && minor > 2 || major == 5 && minor == 2 && fix >= 8;
            // Got fixed in 5.2.7: https://hibernate.atlassian.net/browse/HHH-2772
            this.needsAssociationToIdRewriteInOnClause = major < 5 || major == 5 && minor < 2 || major == 5 && minor == 2 && fix < 7;
            // Got fixed in 5.1.0: https://hibernate.atlassian.net/browse/HHH-7321
            this.needsBrokenAssociationToIdRewriteInOnClause = major < 5 || major == 5 && minor < 1;
            // Going to make this configurable in Hibernate in a future version
            this.supportsCollectionTableCleanupOnDelete = false;
            this.supportsJoinTableCleanupOnDelete = true;
            // See https://hibernate.atlassian.net/browse/HHH-12942 for details
            this.needsCorrelationPredicateWhenCorrelatingWithWhereClause = true;
            // See https://hibernate.atlassian.net/browse/HHH-12775 for details
            this.supportsSingleValuedAssociationNaturalIdExpressions = major > 5 || major == 5 && minor >= 4;
            // See https://hibernate.atlassian.net/browse/HHH-13045 for details
            this.needsElementCollectionIdCutoffForCompositeIdOwner = major < 5 || major == 5 && minor < 4 || major == 5 && minor == 4 && "SNAPSHOT".equals(type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsEntityJoin() {
        return supportsEntityJoin;
    }

    @Override
    public boolean needsJoinSubqueryRewrite() {
        return needsJoinSubqueryRewrite;
    }

    @Override
    public boolean supportsForeignAssociationInOnClause() {
        return supportsForeignAssociationInOnClause;
    }

    @Override
    public boolean needsAssociationToIdRewriteInOnClause() {
        return needsAssociationToIdRewriteInOnClause;
    }

    @Override
    public boolean needsBrokenAssociationToIdRewriteInOnClause() {
        return needsBrokenAssociationToIdRewriteInOnClause;
    }

    @Override
    public boolean supportsCollectionTableCleanupOnDelete() {
        return supportsCollectionTableCleanupOnDelete;
    }

    @Override
    public boolean supportsJoinTableCleanupOnDelete() {
        return supportsJoinTableCleanupOnDelete;
    }

    @Override
    public boolean needsCorrelationPredicateWhenCorrelatingWithWhereClause() {
        return needsCorrelationPredicateWhenCorrelatingWithWhereClause;
    }

    @Override
    public boolean supportsSingleValuedAssociationNaturalIdExpressions() {
        return supportsSingleValuedAssociationNaturalIdExpressions;
    }

    @Override
    public boolean needsElementCollectionIdCutoffForCompositeIdOwner() {
        return needsElementCollectionIdCutoffForCompositeIdOwner;
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

                // According to the following, MySQL sorts NULLS FIRST when ASC and NULLS LAST when DESC, so we only need the expression for opposite cases
                // https://dev.mysql.com/doc/refman/8.0/en/working-with-null.html
                if (db != DB.MY_SQL || ("ASC".equalsIgnoreCase(order) && "LAST".equals(nulls)) || ("DESC".equalsIgnoreCase(order) && "FIRST".equals(nulls))) {
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

    protected final String getTypeName(ManagedType<?> ownerType) {
        return ownerType.getJavaType() == null ? ((EntityType) ownerType).getName() : ownerType.getJavaType().getName();
    }

    protected final AbstractEntityPersister getEntityPersister(ManagedType<?> ownerType) {
        EntityPersister entityPersister;
        if (ownerType.getJavaType() == null) {
            entityPersister = entityPersisters.get(((EntityType) ownerType).getName());
        } else {
            entityPersister = entityPersisters.get(ownerType.getJavaType().getName());
        }
        if (entityPersister == null) {
            try {
                entityPersister = entityPersisters.get(GET_TYPE_NAME.invoke(ownerType));
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

            collection = (QueryableCollection) collectionPersisters.get(sb.toString());
            if (collection == null) {
                String superclass = entityPersister.getEntityMetamodel().getSuperclass();
                entityPersister = superclass == null ? null : (AbstractEntityPersister) entityPersisters.get(superclass);
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
            return new String[]{ discriminatorColumnName, discriminatorSQLValue };
        }
        return null;
    }

    @Override
    public boolean isForeignJoinColumn(EntityType<?> ownerType, String attributeName) {
        AbstractEntityPersister persister = getEntityPersister(ownerType);
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
        AbstractEntityPersister persister = getEntityPersister(ownerType);
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
        AbstractEntityPersister persister = getEntityPersister(ownerType);
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
        CollectionPersister persister = getCollectionPersister(ownerType, attributeName);
        if (persister != null) {
            if (persister.isInverse()) {
                return getMappedBy(persister);
            }
        } else {
            EntityPersister entityPersister = getEntityPersister(ownerType);
            Type propertyType = entityPersister.getPropertyType(attributeName);
            if (propertyType instanceof OneToOneType) {
                return ((OneToOneType) propertyType).getRHSUniqueKeyPropertyName();
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getWritableMappedByMappings(EntityType<?> inverseType, EntityType<?> ownerType, String attributeName) {
        AbstractEntityPersister entityPersister = getEntityPersister(ownerType);
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
            return getEntityPersister(entityType).getPropertyColumnNames(attributeName);
        } catch (MappingException e) {
            throw new RuntimeException("Unknown property [" + attributeName + "] of entity [" + entityType.getJavaType() + "]", e);
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
            int dotIndex = -1;
            do {
                String propertyName;
                if (dotIndex == -1) {
                    propertyName = subAttributeName;
                } else {
                    propertyName = subAttributeName.substring(0, dotIndex);
                }
                int offset = 0;
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
                // Component types do not store entries for the id properties of associations so we need to look for a sub-part of the attribute name
            } while ((dotIndex = subAttributeName.indexOf('.', dotIndex + 1)) != -1);
        } else if (persister.getElementType() instanceof org.hibernate.type.EntityType) {
            AbstractEntityPersister elementPersister = (AbstractEntityPersister) entityPersisters.get(((org.hibernate.type.EntityType) persister.getElementType()).getAssociatedEntityName());
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
            Type propertyType = persister.getPropertyType(prefix + propertyName);
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
        if (useQuoted || name == null || name.length() < 2) {
            return name;
        }
        final char first = name.charAt(0);
        final char last = name.charAt(name.length() - 1);
        if (first == '`' && last == '`'
                || first == '[' && last == ']'
                || first == '"' && last == '"') {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }

    @Override
    public String[] getColumnTypes(EntityType<?> entityType, String attributeName) {
        AbstractEntityPersister entityPersister = getEntityPersister(entityType);
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
                tables[i] = database.getTable(unquote(entityPersister.getSubclassTableName(i)));
            }
        } else if (entityPersister instanceof SingleTableEntityPersister) {
            tables = new Table[((SingleTableEntityPersister) entityPersister).getSubclassTableSpan()];
            for (int i = 0; i < tables.length; i++) {
                tables[i] = database.getTable(unquote(entityPersister.getSubclassTableName(i)));
            }
        } else {
            tables = new Table[] { database.getTable(unquote(entityPersister.getTableName())) };
        }

        // In this case, the property might represent a formula
        boolean isFormula = columnNames.length == 1 && columnNames[0] == null;
        boolean isSubselect = tables.length == 1 && tables[0] == null;

        if (isFormula || isSubselect) {
            Type propertyType = entityPersister.getPropertyType(attributeName);
            return getColumnTypeForPropertyType(entityType, attributeName, sfi, propertyType);
        }

        return getColumnTypesForColumnNames(entityType, columnNames, tables);
    }

    private String[] getColumnTypesForColumnNames(EntityType<?> entityType, String[] columnNames, Table[] tables) {
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
                throw new IllegalArgumentException("Could not find column '" + columnNames[i] + "' in entity: " + entityType.getName());
            }

            columnTypes[i] = column.getSqlType();
        }

        return columnTypes;
    }

    private String[] getColumnTypeForPropertyType(EntityType<?> entityType, String attributeName, SessionFactoryImplementor sfi, Type propertyType) {
        if (propertyType instanceof org.hibernate.type.EntityType) {
            propertyType = ((org.hibernate.type.EntityType) propertyType).getIdentifierOrUniqueKeyType(sfi);
        }

        long length =   Column.DEFAULT_LENGTH;
        int precision = Column.DEFAULT_PRECISION;
        int scale =     Column.DEFAULT_SCALE;

        try {
            Method m = Type.class.getMethod("dictatedSizes", Mapping.class);
            Object size = ((Object[]) m.invoke(propertyType, sfi))[0];
            length =    (long) size.getClass().getMethod("getLength").invoke(size);
            precision = (int)  size.getClass().getMethod("getPrecision").invoke(size);
            scale =     (int)  size.getClass().getMethod("getScale").invoke(size);
        } catch (Exception ex) {
            LOG.fine("Could not determine the column type of the attribute: " + attributeName + " of the entity: " + entityType.getName());
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
            int dotIndex = -1;
            do {
                String propertyName;
                if (dotIndex == -1) {
                    propertyName = subAttributeName;
                } else {
                    propertyName = subAttributeName.substring(0, dotIndex);
                }
                int offset = 0;
                for (int i = 0; i < propertyNames.length; i++) {
                    int span = subtypes[i].getColumnSpan(persister.getFactory());
                    if (propertyName.equals(propertyNames[i])) {
                        columnNames = new String[span];
                        propertyType = subtypes[i];
                        String[] elementColumnNames = persister.getElementColumnNames();
                        System.arraycopy(elementColumnNames, offset, columnNames, 0, span);
                        break;
                    } else {
                        offset += span;
                    }
                }
                // Component type do not store entries for the id properties of associations so we need to look for a sub-part of the attribute name
            } while (propertyType == null && (dotIndex = subAttributeName.indexOf('.', dotIndex + 1)) != -1);
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
        boolean isFormula = columnNames.length == 1 && columnNames[0] == null;

        if (isFormula) {
            return getColumnTypeForPropertyType(ownerType, attributeName, sfi, propertyType);
        }

        Database database = sfi.getServiceRegistry().locateServiceBinding(Database.class).getService();
        Table[] tables = new Table[]{ database.getTable(unquote(persister.getTableName())) };

        return getColumnTypesForColumnNames(ownerType, columnNames, tables);
    }

    @Override
    public JoinTable getJoinTable(EntityType<?> ownerType, String attributeName) {
        CollectionPersister persister = getCollectionPersister(ownerType, attributeName);
        if (persister instanceof QueryableCollection) {
            QueryableCollection queryableCollection = (QueryableCollection) persister;

            if (!queryableCollection.getElementType().isEntityType()) {
                String[] targetColumnMetaData = queryableCollection.getElementColumnNames();
                Map<String, String> targetColumnMapping = new HashMap<>();

                for (int i = 0; i < targetColumnMetaData.length; i++) {
                    targetColumnMapping.put(targetColumnMetaData[i], targetColumnMetaData[i]);
                }
                return createJoinTable(queryableCollection, targetColumnMapping, null, attributeName);
            } else if (queryableCollection.getElementPersister() instanceof Joinable) {
                String elementTableName = ((Joinable) queryableCollection.getElementPersister()).getTableName();
                if (!queryableCollection.getTableName().equals(elementTableName)) {
                    String[] targetColumnMetaData = queryableCollection.getElementColumnNames();
                    AbstractEntityPersister elementPersister = (AbstractEntityPersister) queryableCollection.getElementPersister();
                    String[] targetPrimaryKeyColumnMetaData = elementPersister.getKeyColumnNames();
                    Map<String, String> targetIdColumnMapping = new HashMap<>();

                    for (int i = 0; i < targetColumnMetaData.length; i++) {
                        targetIdColumnMapping.put(targetColumnMetaData[i], targetPrimaryKeyColumnMetaData[i]);
                    }
                    Set<String> idAttributeNames = getColumnMatchingAttributeNames(elementPersister, Arrays.asList(targetPrimaryKeyColumnMetaData));
                    return createJoinTable(queryableCollection, targetIdColumnMapping, idAttributeNames, attributeName);
                }
            }
        }
        return null;
    }

    private JoinTable createJoinTable(QueryableCollection queryableCollection, Map<String, String> targetColumnMapping, Set<String> targetIdAttributeNames, String attributeName) {
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
        AbstractEntityPersister ownerEntityPersister = (AbstractEntityPersister) queryableCollection.getOwnerEntityPersister();
        String[] primaryKeyColumnMetaData = ownerEntityPersister.getKeyColumnNames();
        String[] foreignKeyColumnMetaData = queryableCollection.getKeyColumnNames();
        Map<String, String> idColumnMapping = new HashMap<>(primaryKeyColumnMetaData.length);
        for (int i = 0; i < foreignKeyColumnMetaData.length; i++) {
            idColumnMapping.put(foreignKeyColumnMetaData[i], primaryKeyColumnMetaData[i]);
        }
        Set<String> idAttributeNames = getColumnMatchingAttributeNames(ownerEntityPersister, Arrays.asList(primaryKeyColumnMetaData));
        if (targetIdAttributeNames == null) {
            Type elementType = queryableCollection.getElementType();
            if (elementType instanceof ComponentType) {
                targetIdAttributeNames = new HashSet<>();
                collectPropertyNames(targetIdAttributeNames, null, elementType, queryableCollection.getFactory());
            }
        }

        return new JoinTable(
                queryableCollection.getTableName(),
                idAttributeNames,
                idColumnMapping,
                keyColumnMapping,
                targetIdAttributeNames,
                targetColumnMapping
        );
    }

    private static Set<String> getColumnMatchingAttributeNames(AbstractEntityPersister ownerEntityPersister, List<String> idColumnNames) {
        Set<String> idAttributeNames = new HashSet<>();
        Type identifierType = ownerEntityPersister.getIdentifierType();
        if (identifierType instanceof ComponentType) {
            String[] idPropertyNames = ((ComponentType) identifierType).getPropertyNames();
            for (String propertyName : idPropertyNames) {
                String attributeName = ownerEntityPersister.getIdentifierPropertyName() + "." + propertyName;
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
            persister = collectionPersisters.get(sb.toString());
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
                return entityMetamodel.getCascadeStyles()[index].doCascade(CascadingAction.DELETE);
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
        return componentType.getCascadeStyle(propertyIndex).doCascade(CascadingAction.DELETE);
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
    public boolean supportsUpdateSetEmbeddable() {
        // Tried it, but the SQL generation seems to mess up...
        return false;
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
        AbstractEntityPersister entityPersister = getEntityPersister(owner);
        Type propertyType = entityPersister.getPropertyType(attributeName);
        List<String> identifierOrUniqueKeyPropertyNames = new ArrayList<>();

        if (propertyType instanceof CollectionType) {
            Type elementType = ((CollectionType) propertyType).getElementType(entityPersister.getFactory());
            Collection<String> targetAttributeNames = getJoinTable(owner, attributeName).getTargetAttributeNames();
            if (targetAttributeNames == null) {
                collectPropertyNames(identifierOrUniqueKeyPropertyNames, null, elementType, entityPersister.getFactory());
            } else {
                AbstractEntityPersister elementPersister = (AbstractEntityPersister) entityPersisters.get(((org.hibernate.type.EntityType) elementType).getAssociatedEntityName());
                for (String targetAttributeName : targetAttributeNames) {
                    collectPropertyNames(identifierOrUniqueKeyPropertyNames, targetAttributeName, elementPersister.getPropertyType(targetAttributeName), entityPersister.getFactory());
                }
            }
        } else {
            collectPropertyNames(identifierOrUniqueKeyPropertyNames, null, propertyType, entityPersister.getFactory());
        }

        return identifierOrUniqueKeyPropertyNames;
    }

    @Override
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> owner, String elementCollectionPath, String attributeName) {
        QueryableCollection persister = getCollectionPersister(owner, elementCollectionPath);
        ComponentType componentType = (ComponentType) persister.getElementType();
        String subAttribute = attributeName.substring(elementCollectionPath.length() + 1);
        // Component types only store direct properties, so we have to go deeper
        String[] propertyParts = subAttribute.split("\\.");
        Type propertyType;
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
        List<String> identifierOrUniqueKeyPropertyNames = new ArrayList<>();
        collectPropertyNames(identifierOrUniqueKeyPropertyNames, null, propertyType, persister.getFactory());
        return identifierOrUniqueKeyPropertyNames;
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
                propertyNames.add(prefix == null ? identifierOrUniqueKeyPropertyName : prefix + "." + identifierOrUniqueKeyPropertyName);
            }
        } else if (!(propertyType instanceof CollectionType) && prefix != null) {
            propertyNames.add(prefix);
        }
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
