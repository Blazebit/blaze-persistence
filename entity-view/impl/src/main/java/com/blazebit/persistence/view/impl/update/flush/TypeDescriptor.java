/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.entity.DefaultEntityLoaderFetchGraphNode;
import com.blazebit.persistence.view.impl.entity.DefaultEntityToEntityMapper;
import com.blazebit.persistence.view.impl.entity.ElementToEntityMapper;
import com.blazebit.persistence.view.impl.entity.EmbeddableUpdaterBasedViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.EntityLoader;
import com.blazebit.persistence.view.impl.entity.EntityLoaders;
import com.blazebit.persistence.view.impl.entity.EntityToEntityMapper;
import com.blazebit.persistence.view.impl.entity.LoadOrPersistViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.UpdaterBasedViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.MutableBasicUserType;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TypeDescriptor {

    private final boolean mutable;
    private final boolean identifiable;
    private final boolean jpaManaged;
    private final boolean jpaEntity;
    private final boolean shouldJpaMerge;
    private final boolean shouldJpaPersist;
    private final boolean cascadePersist;
    private final boolean cascadeUpdate;
    private final String entityIdAttributeName;
    private final String attributeIdAttributeName;
    private final Class<?> jpaType;
    private final TypeConverter<Object, Object> converter;
    private final BasicUserType<Object> basicUserType;
    private final EntityToEntityMapper entityToEntityMapper;
    private final ViewToEntityMapper viewToEntityMapper;
    private final ViewToEntityMapper loadOnlyViewToEntityMapper;

    public TypeDescriptor(boolean mutable, boolean identifiable, boolean jpaManaged, boolean jpaEntity, boolean shouldJpaMerge, boolean shouldJpaPersist, boolean cascadePersist, boolean cascadeUpdate, String entityIdAttributeName, String attributeIdAttributeName,
                          Class<?> jpaType, TypeConverter<Object, Object> converter, BasicUserType<Object> basicUserType, EntityToEntityMapper entityToEntityMapper, ViewToEntityMapper viewToEntityMapper, ViewToEntityMapper loadOnlyViewToEntityMapper) {
        this.mutable = mutable;
        this.identifiable = identifiable;
        this.jpaManaged = jpaManaged;
        this.jpaEntity = jpaEntity;
        this.shouldJpaMerge = shouldJpaMerge;
        this.shouldJpaPersist = shouldJpaPersist;
        this.cascadePersist = cascadePersist;
        this.cascadeUpdate = cascadeUpdate;
        this.entityIdAttributeName = entityIdAttributeName;
        this.attributeIdAttributeName = attributeIdAttributeName;
        this.jpaType = jpaType;
        this.converter = converter;
        this.basicUserType = basicUserType;
        this.entityToEntityMapper = entityToEntityMapper;
        this.viewToEntityMapper = viewToEntityMapper;
        this.loadOnlyViewToEntityMapper = loadOnlyViewToEntityMapper;
    }

    public static TypeDescriptor forType(EntityViewManagerImpl evm, Map<Object, EntityViewUpdaterImpl> localCache, EntityViewUpdaterImpl updater, AbstractMethodAttribute<?, ?> attribute, Type<?> type, EntityViewUpdaterImpl owner, String ownerMapping) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        String attributeLocation = attribute.getLocation();
        boolean cascadePersist = attribute.isPersistCascaded();
        boolean cascadeUpdate = attribute.isUpdateCascaded();
        Set<Type<?>> readOnlyAllowedSubtypes = attribute.getReadOnlyAllowedSubtypes();
        Set<Type<?>> persistAllowedSubtypes = attribute.getPersistCascadeAllowedSubtypes();
        Set<Type<?>> updateAllowedSubtypes = attribute.getUpdateCascadeAllowedSubtypes();
        EntityToEntityMapper entityToEntityMapper = null;
        ViewToEntityMapper viewToEntityMapper = null;
        ViewToEntityMapper loadOnlyViewToEntityMapper = null;

        final ManagedType<?> managedType = entityMetamodel.getManagedType(type.getJavaType());
        final boolean jpaEntity = managedType instanceof EntityType<?>;
        final boolean jpaManaged = managedType != null;
        final boolean mutable;
        final boolean identifiable;
        TypeConverter<Object, Object> converter = (TypeConverter<Object, Object>) type.getConverter();
        BasicUserType<Object> basicUserType;
        ManagedType<?> ownerEntityType = owner == null ? attribute.getDeclaringType().getJpaManagedType() : owner.getManagedViewType().getJpaManagedType();
        String attributeIdAttributeName = null;
        String entityIdAttributeName = null;
        Class<?> jpaType;
        // TODO: currently we only check if the declared type is mutable, but we have to let the collection flusher which types are considered updatable/creatable
        if (type instanceof BasicType<?>) {
            jpaType = type.getJavaType();
            basicUserType = (BasicUserType<Object>) ((BasicType<?>) type).getUserType();
            mutable = basicUserType.isMutable();
            identifiable = jpaEntity || !jpaManaged;
            if (jpaEntity) {
                Map<String, Map<?, ?>> fetchGraph = null;
                UnmappedAttributeCascadeDeleter deleter = null;

                // We only need to know the fetch graph when we actually do updates
                if (cascadeUpdate) {
                    fetchGraph = getFetchGraph(attribute.getFetches(), attribute.getMapping(), managedType);
                }

                if (ownerEntityType instanceof EntityType<?>) {
                    ExtendedManagedType<?> extendedManagedType = evm.getMetamodel().getEntityMetamodel().getManagedType(ExtendedManagedType.class, type.getJavaType());
                    entityIdAttributeName = extendedManagedType.getIdAttribute().getName();
                    attributeIdAttributeName = getAttributeElementIdentifier(evm, (EntityType<?>) ownerEntityType, attribute.getName(), ownerMapping, attribute.getMapping(), extendedManagedType.getType());

                    // Only construct when orphanRemoval or delete cascading is enabled, orphanRemoval implies delete cascading
                    if (attribute.isDeleteCascaded()) {
                        String mapping = attribute.getMapping();
                        ExtendedManagedType elementManagedType = entityMetamodel.getManagedType(ExtendedManagedType.class, attribute.getDeclaringType().getJpaManagedType());
                        deleter = new UnmappedBasicAttributeCascadeDeleter(
                                evm,
                                mapping,
                                elementManagedType.getAttribute(mapping),
                                mapping + "." + attributeIdAttributeName,
                                false
                        );
                    }
                }

                // TODO: EntityToEntityMapper should also use the attributeIdAttributeName
                entityToEntityMapper = new DefaultEntityToEntityMapper(
                        cascadePersist,
                        cascadeUpdate,
                        jpaType,
                        basicUserType,
                        new DefaultEntityLoaderFetchGraphNode(
                            evm, attribute.getName(), (EntityType<?>) managedType, fetchGraph
                        ),
                        deleter
                );
            }
        } else {
            ManagedViewTypeImplementor<?> elementType = (ManagedViewTypeImplementor<?>) type;
            jpaType = elementType.getEntityClass();
            basicUserType = null;
            mutable = elementType.isUpdatable() || elementType.isCreatable() || !persistAllowedSubtypes.isEmpty() || !updateAllowedSubtypes.isEmpty();
            Set<ManagedViewType<?>> viewTypes = attribute.getViewTypes();
            if (elementType.getJpaManagedType() instanceof EntityType<?> && ownerEntityType instanceof EntityType<?>) {
                ExtendedManagedType<?> extendedManagedType = evm.getMetamodel().getEntityMetamodel().getManagedType(ExtendedManagedType.class, elementType.getEntityClass());
                entityIdAttributeName = extendedManagedType.getIdAttribute().getName();
                attributeIdAttributeName = getAttributeElementIdentifier(evm, (EntityType<?>) ownerEntityType, attribute.getName(), ownerMapping, attribute.getMapping(), extendedManagedType.getType());
            }
            viewToEntityMapper = createViewToEntityMapper(
                evm,
                localCache,
                updater,
                attributeIdAttributeName,
                attribute.getMapping(),
                attributeLocation,
                elementType,
                cascadePersist,
                cascadeUpdate,
                readOnlyAllowedSubtypes,
                persistAllowedSubtypes,
                updateAllowedSubtypes,
                EntityLoaders.referenceLoaderForAttribute(evm, localCache, elementType, viewTypes, attributeIdAttributeName),
                owner,
                ownerMapping
            );
            loadOnlyViewToEntityMapper = createLoadOnlyViewToEntityMapper(
                attributeLocation,
                evm,
                localCache,
                attributeIdAttributeName,
                attribute.getMapping(),
                elementType,
                cascadePersist,
                cascadeUpdate,
                readOnlyAllowedSubtypes,
                persistAllowedSubtypes,
                updateAllowedSubtypes,
                EntityLoaders.referenceLoaderForAttribute(evm, localCache, elementType, viewTypes, attributeIdAttributeName),
                owner,
                ownerMapping
            );
            identifiable = viewToEntityMapper.getViewIdAccessor() != null;
        }

        final boolean shouldJpaMerge = jpaEntity && mutable && cascadeUpdate;
        final boolean shouldJpaPersist = jpaEntity && mutable && cascadePersist;
        final boolean shouldFlushUpdates = cascadeUpdate && !updateAllowedSubtypes.isEmpty();
        final boolean shouldFlushPersists = cascadePersist && !persistAllowedSubtypes.isEmpty();
        return new TypeDescriptor(
                mutable,
                identifiable,
                jpaManaged,
                jpaEntity,
                shouldJpaMerge,
                shouldJpaPersist,
                shouldFlushPersists,
                shouldFlushUpdates,
                entityIdAttributeName,
                attributeIdAttributeName,
                jpaType,
                converter,
                basicUserType,
                entityToEntityMapper,
                viewToEntityMapper,
                loadOnlyViewToEntityMapper
        );
    }

    public static TypeDescriptor forInverseAttribute(ViewToEntityMapper viewToEntityMapper) {
        return new TypeDescriptor(
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                viewToEntityMapper,
                viewToEntityMapper
        );
    }

    public static TypeDescriptor forInverseCollectionAttribute(Class<?> entityClass, BasicUserType<?> basicUserType) {
        return new TypeDescriptor(
                false,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                null,
                null,
                entityClass,
                null,
                (BasicUserType<Object>) basicUserType,
                null,
                null,
                null
        );
    }

    public static TypeDescriptor forEntityComponentType() {
        return new TypeDescriptor(
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static Map<String, Map<?, ?>> getFetchGraph(String[] fetches, String attributeMapping, ManagedType<?> managedType) {
        Map<String, Map<?, ?>> fetchGraph = null;
        boolean isEntity = managedType instanceof EntityType<?>;
        if (managedType != null && (isEntity || fetches.length > 0)) {
            fetchGraph = new HashMap<>();
            String partPrefix;

            // Entity types, contrary to embeddable types, can be join fetched
            if (isEntity) {
                partPrefix = "";
            } else {
                // If this is an embeddable type, the subgraph relations get prefixed with the embeddable attribute name
                partPrefix = attributeMapping + ".";
            }

            for (String subFetch : fetches) {
                String[] parts = subFetch.split("\\.");
                Map<String, Map<?, ?>> currentSubGraph = fetchGraph;

                for (String part : parts) {
                    String subPath = partPrefix + part;
                    Map<?, ?> map = currentSubGraph.get(subPath);
                    if (map == null) {
                        map = new HashMap<>();
                        currentSubGraph.put(subPath, map);
                    }

                    currentSubGraph = (Map<String, Map<?, ?>>) map;
                }
            }
        }
        return fetchGraph;
    }

    private static ViewToEntityMapper createViewToEntityMapper(EntityViewManagerImpl evm, Map<Object, EntityViewUpdaterImpl> localCache, EntityViewUpdaterImpl updater, String attributeIdAttributeName, String attributeMapping, String attributeLocation, ManagedViewType<?> viewType, boolean cascadePersist, boolean cascadeUpdate,
                                                               Set<Type<?>> readOnlyAllowedSubtypes, Set<Type<?>> persistAllowedSubtypes, Set<Type<?>> updateAllowedSubtypes, EntityLoader entityLoader, EntityViewUpdaterImpl owner, String ownerMapping) {
        AttributeAccessor viewIdAccessor = null;
        AttributeAccessor entityIdAccessor = null;
        if (viewType instanceof ViewType<?>) {
            viewIdAccessor = Accessors.forSubviewAssociationId(evm, viewType, attributeIdAttributeName, true);
            entityIdAccessor = Accessors.forEntityMapping(evm, viewType.getEntityClass(), attributeIdAttributeName);
        }
        Class<?> viewTypeClass = viewType.getJavaType();
        if (!cascadeUpdate) {
            return new LoadOrPersistViewToEntityMapper(
                    attributeLocation,
                    evm,
                    viewTypeClass,
                    readOnlyAllowedSubtypes,
                    persistAllowedSubtypes,
                    updateAllowedSubtypes,
                    entityLoader,
                    viewIdAccessor,
                    entityIdAccessor,
                    cascadePersist,
                    owner,
                    ownerMapping,
                    localCache
            );
        }

        if (evm.getMetamodel().getEntityMetamodel().getEntity(viewType.getEntityClass()) == null) {
            return new EmbeddableUpdaterBasedViewToEntityMapper(
                    attributeLocation,
                    evm,
                    viewTypeClass,
                    readOnlyAllowedSubtypes,
                    persistAllowedSubtypes,
                    updateAllowedSubtypes,
                    entityLoader,
                    cascadePersist,
                    null,
                    owner == null ? updater : owner,
                    ownerMapping == null ? attributeMapping : ownerMapping + "." + attributeMapping,
                    localCache
            );
        } else {
            return new UpdaterBasedViewToEntityMapper(
                    attributeLocation,
                    evm,
                    viewTypeClass,
                    readOnlyAllowedSubtypes,
                    persistAllowedSubtypes,
                    updateAllowedSubtypes,
                    entityLoader,
                    viewIdAccessor,
                    entityIdAccessor,
                    cascadePersist,
                    owner,
                    ownerMapping,
                    localCache
            );
        }
    }

    private static ViewToEntityMapper createLoadOnlyViewToEntityMapper(String attributeLocation, EntityViewManagerImpl evm, Map<Object, EntityViewUpdaterImpl> localCache, String attributeIdAttributeName, String attributeMapping, ManagedViewType<?> viewType, boolean cascadePersist, boolean cascadeUpdate,
                                                                       Set<Type<?>> readOnlyAllowedSubtypes, Set<Type<?>> persistAllowedSubtypes, Set<Type<?>> updateAllowedSubtypes, EntityLoader entityLoader, EntityViewUpdaterImpl owner, String ownerMapping) {
        AttributeAccessor viewIdAccessor = null;
        AttributeAccessor entityIdAccessor = null;
        if (viewType instanceof ViewType<?>) {
            viewIdAccessor = Accessors.forSubviewAssociationId(evm, viewType, attributeIdAttributeName, true);
            entityIdAccessor = Accessors.forEntityMapping(evm, viewType.getEntityClass(), attributeIdAttributeName);
        }
        if (evm.getMetamodel().getEntityMetamodel().getEntity(viewType.getEntityClass()) == null) {
            Class<?> viewTypeClass = viewType.getJavaType();
            return new EmbeddableUpdaterBasedViewToEntityMapper(
                    attributeLocation,
                    evm,
                    viewTypeClass,
                    readOnlyAllowedSubtypes,
                    persistAllowedSubtypes,
                    updateAllowedSubtypes,
                    entityLoader,
                    cascadePersist,
                    null,
                    owner,
                    ownerMapping == null ? attributeMapping : ownerMapping + "." + attributeMapping,
                    localCache
            );
        } else {
            Class<?> viewTypeClass = viewType.getJavaType();
            return new LoadOrPersistViewToEntityMapper(
                    attributeLocation,
                    evm,
                    viewTypeClass,
                    readOnlyAllowedSubtypes,
                    persistAllowedSubtypes,
                    updateAllowedSubtypes,
                    entityLoader,
                    viewIdAccessor,
                    entityIdAccessor,
                    cascadePersist,
                    owner,
                    ownerMapping,
                    localCache
            );
        }
    }

    public static String getAttributeElementIdentifier(EntityViewManagerImpl evm, EntityType<?> ownerEntityType, String attributeName, String ownerMapping, String attributeMapping, ManagedType<?> elementType) {
        if (attributeMapping == null) {
            return null;
        }
        JpaProvider jpaProvider = evm.getJpaProvider();
        Collection<String> joinTableOwnerProperties;
        if (ownerMapping == null) {
            joinTableOwnerProperties = jpaProvider.getJoinMappingPropertyNames(ownerEntityType, ownerMapping, attributeMapping).keySet();
        } else {
            joinTableOwnerProperties = jpaProvider.getJoinMappingPropertyNames(ownerEntityType, ownerMapping, ownerMapping + "." + attributeMapping).keySet();
        }
        if (joinTableOwnerProperties.isEmpty()) {
            if (elementType instanceof EntityType<?>) {
                return JpaMetamodelUtils.getSingleIdAttribute((EntityType<?>) elementType).getName();
            }
            throw new IllegalArgumentException("Couldn't find joinable owner properties for attribute " + attributeName + " of " + ownerEntityType.getJavaType().getName());
        } else if (joinTableOwnerProperties.size() != 1) {
            SingularAttribute<?, ?> singleIdAttribute = JpaMetamodelUtils.getSingleIdAttribute(ownerEntityType);
            // If the id type is an embeddable, we could get multiple properties, so we need to check if the property paths are properly prefixed
            String prefix = singleIdAttribute.getName() + ".";
            for (String joinTableOwnerProperty : joinTableOwnerProperties) {
                if (!joinTableOwnerProperty.startsWith(prefix)) {
                    throw new IllegalArgumentException("Multiple joinable owner properties for attribute " + attributeName + " of " + ownerEntityType.getJavaType().getName() + " found which is not yet supported. Consider using the primary key instead!");
                }
            }
            return singleIdAttribute.getName();
        }
        return joinTableOwnerProperties.iterator().next();
    }

    public Class<?> getJpaType() {
        return jpaType;
    }

    public boolean isJpaEmbeddable() {
        return jpaManaged && !jpaEntity;
    }

    public boolean shouldFlushMutations() {
        return mutable && (cascadePersist || cascadeUpdate);
    }

    public boolean shouldJpaPersistOrMerge() {
        return shouldJpaPersist || shouldJpaMerge;
    }

    public boolean isSubview() {
        return viewToEntityMapper != null;
    }

    public boolean isBasic() {
        return viewToEntityMapper == null && !jpaManaged;
    }

    public boolean supportsEqualityCheck() {
        return basicUserType != MutableBasicUserType.INSTANCE;
    }

    public boolean supportsDeepEqualityCheck() {
        return basicUserType == null || basicUserType.supportsDeepEqualChecking();
    }

    public boolean supportsDirtyCheck() {
        return isSubview() || basicUserType.supportsDirtyChecking()
                // We rely on the fact that we can detect whether we should persist
                || (isJpaEntity() && shouldJpaPersist() && !shouldJpaMerge());
    }

    public boolean isMutable() {
        return mutable;
    }

    public boolean isIdentifiable() {
        return identifiable;
    }

    public boolean isJpaManaged() {
        return jpaManaged;
    }

    public boolean isJpaEntity() {
        return jpaEntity;
    }

    public boolean shouldJpaMerge() {
        return shouldJpaMerge;
    }

    public boolean shouldJpaPersist() {
        return shouldJpaPersist;
    }

    public boolean isCascadePersist() {
        return cascadePersist;
    }

    public boolean isCascadeUpdate() {
        return cascadeUpdate;
    }

    public String getEntityIdAttributeName() {
        return entityIdAttributeName;
    }

    public String getAttributeIdAttributeName() {
        return attributeIdAttributeName;
    }

    public TypeConverter<Object, Object> getConverter() {
        return converter;
    }

    public BasicUserType<Object> getBasicUserType() {
        return basicUserType;
    }

    public ViewToEntityMapper getLoadOnlyViewToEntityMapper() {
        return loadOnlyViewToEntityMapper;
    }

    public ViewToEntityMapper getViewToEntityMapper() {
        return viewToEntityMapper;
    }

    public EntityToEntityMapper getEntityToEntityMapper() {
        return entityToEntityMapper;
    }

    public ElementToEntityMapper getElementToEntityMapper() {
        if (viewToEntityMapper != null) {
            return viewToEntityMapper;
        }
        return entityToEntityMapper;
    }
}
