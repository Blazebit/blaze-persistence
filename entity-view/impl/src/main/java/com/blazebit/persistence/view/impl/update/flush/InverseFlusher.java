/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.entity.InverseElementToEntityMapper;
import com.blazebit.persistence.view.impl.entity.InverseEntityToEntityMapper;
import com.blazebit.persistence.view.impl.entity.InverseViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.LoadOnlyViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.LoadOrPersistViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.ReferenceEntityLoader;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.mapper.CollectionAddMapper;
import com.blazebit.persistence.view.impl.mapper.CollectionRemoveMapper;
import com.blazebit.persistence.view.impl.mapper.Mapper;
import com.blazebit.persistence.view.impl.mapper.Mappers;
import com.blazebit.persistence.view.impl.mapper.NullMapper;
import com.blazebit.persistence.view.impl.mapper.SimpleMapper;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.type.EntityBasicUserType;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class InverseFlusher<E> {

    private final Class<?> parentEntityClass;
    private final String attributeName;
    private final String parentIdAttributeName;
    private final String childIdAttributeName;
    private final UnmappedAttributeCascadeDeleter deleter;
    // Maps the parent view object to an entity via means of em.getReference
    private final ViewToEntityMapper parentReferenceViewToEntityMapper;
    // Allows to flush a parent reference value for a child element
    private final DirtyAttributeFlusher<?, E, Object> parentReferenceAttributeFlusher;

    /* The following are set when the element is a view */

    // Maps the parent entity object on to the child view object
    private final Mapper<E, Object> parentEntityOnChildViewMapper;
    private final InverseViewToEntityMapper childViewToEntityMapper;
    // Maps a child view object to an entity via means of em.getReference
    private final ViewToEntityMapper childReferenceViewToEntityMapper;

    /* The rest is set when the element is an entity */

    // Maps the parent entity object on to the child entity object
    private final Mapper<E, Object> parentEntityOnChildEntityMapper;
    private final InverseEntityToEntityMapper childEntityToEntityMapper;

    public InverseFlusher(Class<?> parentEntityClass, String attributeName, String parentIdAttributeName, String childIdAttributeName, UnmappedAttributeCascadeDeleter deleter,
                          ViewToEntityMapper parentReferenceViewToEntityMapper, DirtyAttributeFlusher<?, E, Object> parentReferenceAttributeFlusher,
                          Mapper<E, Object> parentEntityOnChildViewMapper, InverseViewToEntityMapper childViewToEntityMapper, ViewToEntityMapper childReferenceViewToEntityMapper,
                          Mapper<E, Object> parentEntityOnChildEntityMapper, InverseEntityToEntityMapper childEntityToEntityMapper) {
        this.parentEntityClass = parentEntityClass;
        this.attributeName = attributeName;
        this.parentIdAttributeName = parentIdAttributeName;
        this.childIdAttributeName = childIdAttributeName;
        this.deleter = deleter;
        this.parentReferenceViewToEntityMapper = parentReferenceViewToEntityMapper;
        this.parentReferenceAttributeFlusher = parentReferenceAttributeFlusher;
        this.parentEntityOnChildViewMapper = parentEntityOnChildViewMapper;
        this.childViewToEntityMapper = childViewToEntityMapper;
        this.childReferenceViewToEntityMapper = childReferenceViewToEntityMapper;
        this.parentEntityOnChildEntityMapper = parentEntityOnChildEntityMapper;
        this.childEntityToEntityMapper = childEntityToEntityMapper;
    }

    public static <E> InverseFlusher<E> forAttribute(EntityViewManagerImpl evm, ManagedViewType<?> viewType, AbstractMethodAttribute<?, ?> attribute, TypeDescriptor childTypeDescriptor, EntityViewUpdaterImpl owner, String ownerMapping) {
        if (attribute.getMappedBy() != null) {
            String attributeLocation = attribute.getLocation();
            Type<?> elementType = attribute instanceof PluralAttribute<?, ?, ?> ? ((PluralAttribute<?, ?, ?>) attribute).getElementType() : ((SingularAttribute<?, ?>) attribute).getType();
            Class<?> elementEntityClass = null;

            AttributeAccessor parentReferenceAttributeAccessor = null;
            Mapper<Object, Object> parentEntityOnChildViewMapper = null;
            Mapper<Object, Object> parentEntityOnChildEntityAddMapper = null;
            Mapper<Object, Object> parentEntityOnChildEntityRemoveMapper = null;
            InverseViewToEntityMapper childViewToEntityMapper = null;
            InverseEntityToEntityMapper childEntityToEntityMapper = null;
            ViewToEntityMapper parentReferenceViewToEntityMapper = new LoadOnlyViewToEntityMapper(
                    new ReferenceEntityLoader(evm, viewType, EntityViewUpdaterImpl.createViewIdMapper(evm, viewType)),
                    Accessors.forViewId(evm, (ViewType<?>) viewType, true),
                    evm.getEntityIdAccessor());
            ViewToEntityMapper childReferenceViewToEntityMapper = null;
            TypeDescriptor parentReferenceTypeDescriptor = TypeDescriptor.forInverseAttribute(parentReferenceViewToEntityMapper);

            if (attribute.getWritableMappedByMappings() != null) {
                // This happens when the mapped by attribute is insertable=false and updatable=false
                if (childTypeDescriptor.isSubview()) {
                    // TODO: actually, we need AttributeMapper for all possible subtypes
                    ViewType<?> childViewType = (ViewType<?>) elementType;
                    elementEntityClass = childViewType.getEntityClass();
                    parentEntityOnChildViewMapper = (Mapper<Object, Object>) Mappers.forViewConvertToViewAttributeMapping(
                            evm,
                            (ViewType<Object>) viewType,
                            (ViewType<Object>) childViewType,
                            attribute.getMappedBy(),
                            (Mapper<Object, Object>) Mappers.forEntityAttributeMappingConvertToViewAttributeMapping(
                                    evm,
                                    viewType.getEntityClass(),
                                    childViewType,
                                    attribute.getWritableMappedByMappings()
                            )
                    );
                    parentEntityOnChildEntityAddMapper = parentEntityOnChildEntityRemoveMapper = (Mapper<Object, Object>) Mappers.forEntityAttributeMapping(
                            evm,
                            viewType.getEntityClass(),
                            childViewType.getEntityClass(),
                            attribute.getWritableMappedByMappings()
                    );
                    childReferenceViewToEntityMapper = new LoadOrPersistViewToEntityMapper(
                            attributeLocation,
                            evm,
                            childViewType.getJavaType(),
                            attribute.getReadOnlyAllowedSubtypes(),
                            attribute.getPersistCascadeAllowedSubtypes(),
                            attribute.getUpdateCascadeAllowedSubtypes(),
                            new ReferenceEntityLoader(evm, childViewType, EntityViewUpdaterImpl.createViewIdMapper(evm, childViewType)),
                            Accessors.forViewId(evm, childViewType, true),
                            true,
                            owner,
                            ownerMapping
                    );
                } else if (childTypeDescriptor.isJpaEntity()) {
                    Class<?> childType = elementType.getJavaType();
                    elementEntityClass = childType;
                    parentEntityOnChildViewMapper = (Mapper<Object, Object>) Mappers.forEntityAttributeMapping(
                            evm,
                            viewType.getEntityClass(),
                            childType,
                            attribute.getWritableMappedByMappings()
                    );
                    parentEntityOnChildEntityAddMapper = parentEntityOnChildEntityRemoveMapper = (Mapper<Object, Object>) Mappers.forEntityAttributeMapping(
                            evm,
                            viewType.getEntityClass(),
                            elementEntityClass,
                            attribute.getWritableMappedByMappings()
                    );
                }
            } else {
                if (childTypeDescriptor.isSubview()) {
                    // TODO: actually, we need AttributeMapper for all possible subtypes
                    ViewType<?> childViewType = (ViewType<?>) elementType;
                    elementEntityClass = childViewType.getEntityClass();
                    parentReferenceAttributeAccessor = Accessors.forEntityMapping(
                            evm,
                            childViewType.getEntityClass(),
                            attribute.getMappedBy()
                    );
                    childReferenceViewToEntityMapper = new LoadOrPersistViewToEntityMapper(
                            attributeLocation,
                            evm,
                            childViewType.getJavaType(),
                            attribute.getReadOnlyAllowedSubtypes(),
                            attribute.getPersistCascadeAllowedSubtypes(),
                            attribute.getUpdateCascadeAllowedSubtypes(),
                            new ReferenceEntityLoader(evm, childViewType, EntityViewUpdaterImpl.createViewIdMapper(evm, childViewType)),
                            Accessors.forViewId(evm, childViewType, true),
                            true,
                            owner,
                            ownerMapping
                    );
                    parentEntityOnChildEntityAddMapper = parentEntityOnChildEntityRemoveMapper = Mappers.forAccessor(parentReferenceAttributeAccessor);
                    parentEntityOnChildViewMapper = (Mapper<Object, Object>) Mappers.forViewConvertToViewAttributeMapping(
                            evm,
                            (ViewType<Object>) viewType,
                            childViewType,
                            attribute.getMappedBy(),
                            null
                    );
                } else if (childTypeDescriptor.isJpaEntity()) {
                    Class<?> childType = elementType.getJavaType();
                    elementEntityClass = childType;
                    parentReferenceAttributeAccessor = Accessors.forEntityMapping(
                            evm,
                            childType,
                            attribute.getMappedBy()
                    );
                    parentEntityOnChildEntityAddMapper = parentEntityOnChildEntityRemoveMapper = Mappers.forAccessor(parentReferenceAttributeAccessor);
                    parentEntityOnChildViewMapper = Mappers.forAccessor(parentReferenceAttributeAccessor);
                }
            }

            DirtyAttributeFlusher<?, Object, ? extends Object> parentReferenceAttributeFlusher;
            ManagedType<?> managedType = evm.getMetamodel().getEntityMetamodel().getManagedType(elementEntityClass);
            Attribute<?, ?> inverseAttribute = JpaMetamodelUtils.getAttribute(managedType, attribute.getMappedBy());
            // Many-To-Many relation can't be handled by the inverse flushers
            if (inverseAttribute != null && inverseAttribute.isCollection()) {
                // A collection can only have a single attribute, so it's safe to assume a SimpleMapper
                parentEntityOnChildEntityAddMapper = new CollectionAddMapper<>(parentEntityOnChildEntityAddMapper == null ? parentReferenceAttributeAccessor : ((SimpleMapper<Object, Object>) parentEntityOnChildEntityAddMapper).getAttributeAccessor());
                parentEntityOnChildEntityRemoveMapper = new CollectionRemoveMapper<>(parentEntityOnChildEntityRemoveMapper == null ? parentReferenceAttributeAccessor : ((SimpleMapper<Object, Object>) parentEntityOnChildEntityRemoveMapper).getAttributeAccessor());
                parentReferenceAttributeFlusher = new ParentCollectionReferenceAttributeFlusher<>(
                        attributeLocation,
                        attribute.getMappedBy(),
                        viewType.getFlushStrategy(),
                        parentReferenceAttributeAccessor,
                        null,
                        null,
                        null,
                        TypeDescriptor.forInverseCollectionAttribute(viewType.getEntityClass(), new EntityBasicUserType(evm.getJpaProvider()))
                );
            } else {
                parentEntityOnChildEntityRemoveMapper = new NullMapper<>(parentEntityOnChildEntityRemoveMapper);
                parentReferenceAttributeFlusher = new ParentReferenceAttributeFlusher<>(
                        evm,
                        viewType.getEntityClass(),
                        attributeLocation,
                        attribute.getMappedBy(),
                        attribute.getWritableMappedByMappings(),
                        parentReferenceTypeDescriptor,
                        parentReferenceAttributeAccessor,
                        parentEntityOnChildViewMapper
                );
            }

            UnmappedAttributeCascadeDeleter deleter = null;
            String parentIdAttributeName = null;
            String childIdAttributeName = null;
            // Only construct when orphanRemoval or delete cascading is enabled, orphanRemoval implies delete cascading
            if (attribute.isDeleteCascaded()) {
                EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
                ExtendedManagedType elementManagedType = entityMetamodel.getManagedType(ExtendedManagedType.class, elementEntityClass);
                parentIdAttributeName = entityMetamodel.getManagedType(ExtendedManagedType.class, viewType.getEntityClass()).getIdAttribute().getName();
                childIdAttributeName = elementManagedType.getIdAttribute().getName();

                String mapping = attribute.getMappedBy();
                if (mapping != null) {
                    if (mapping.isEmpty()) {
                        deleter = new UnmappedWritableBasicAttributeSetNullCascadeDeleter(evm, elementManagedType, attribute.getWritableMappedByMappings());
                    } else {
                        ExtendedAttribute extendedAttribute = elementManagedType.getAttribute(mapping);
                        if (childTypeDescriptor.isSubview()) {
                            deleter = new ViewTypeCascadeDeleter(childTypeDescriptor.getViewToEntityMapper());
                        } else if (childTypeDescriptor.isJpaEntity()) {
                            deleter = new UnmappedBasicAttributeCascadeDeleter(evm, mapping, extendedAttribute, mapping + "." + parentIdAttributeName, false);
                        }
                    }
                }
            }

            if (childTypeDescriptor.isSubview()) {
                ViewType<?> childViewType = (ViewType<?>) elementType;
                childViewToEntityMapper = new InverseViewToEntityMapper(
                        evm,
                        childViewType,
                        parentEntityOnChildViewMapper,
                        parentEntityOnChildEntityAddMapper,
                        parentEntityOnChildEntityRemoveMapper,
                        childTypeDescriptor.getViewToEntityMapper(),
                        parentReferenceAttributeFlusher,
                        EntityViewUpdaterImpl.createIdFlusher(evm, childViewType, EntityViewUpdaterImpl.createViewIdMapper(evm, childViewType))
                );
            } else if (childTypeDescriptor.isJpaEntity()) {
                Class<?> childType = elementType.getJavaType();
                childEntityToEntityMapper = new InverseEntityToEntityMapper(
                        evm,
                        evm.getMetamodel().getEntityMetamodel().entity(childType),
                        parentEntityOnChildEntityAddMapper,
                        parentEntityOnChildEntityRemoveMapper,
                        parentReferenceAttributeFlusher
                );
            }

            return new InverseFlusher(
                    viewType.getEntityClass(),
                    attribute.getMapping(),
                    parentIdAttributeName,
                    childIdAttributeName,
                    deleter,
                    parentReferenceViewToEntityMapper,
                    parentReferenceAttributeFlusher,
                    parentEntityOnChildViewMapper,
                    childViewToEntityMapper,
                    childReferenceViewToEntityMapper,
                    parentEntityOnChildEntityAddMapper,
                    childEntityToEntityMapper
            );
        }

        return null;
    }

    public List<PostFlushDeleter> removeByOwnerId(UpdateContext context, Object ownerId) {
        EntityViewManagerImpl evm = context.getEntityViewManager();
        List<Object> elementIds = (List<Object>) evm.getCriteriaBuilderFactory().create(context.getEntityManager(), parentEntityClass, "e")
                .where(parentIdAttributeName).eq(ownerId)
                .select("e." + attributeName + "." + childIdAttributeName)
                .getResultList();
        if (!elementIds.isEmpty()) {
            // We must always delete this, otherwise we might get a constraint violation because of the cascading delete
            DeleteCriteriaBuilder<?> cb = evm.getCriteriaBuilderFactory().deleteCollection(context.getEntityManager(), parentEntityClass, "e", attributeName);
            cb.where(parentIdAttributeName).eq(ownerId);
            cb.executeUpdate();
        }

        return Collections.<PostFlushDeleter>singletonList(new PostFlushInverseCollectionElementByIdDeleter(deleter, elementIds));
    }

    public void removeElement(UpdateContext context, Object ownerEntity, Object element) {
        if (childViewToEntityMapper != null) {
            removeViewElement(context, element);
        } else {
            removeEntityElement(context, element);
        }
    }

    public void removeElements(UpdateContext context, Iterable<?> elements) {
        if (childViewToEntityMapper != null) {
            for (Object element : elements) {
                removeViewElement(context, element);
            }
        } else {
            for (Object element : elements) {
                removeEntityElement(context, element);
            }
        }
    }

    private void removeViewElement(UpdateContext context, Object element) {
        childReferenceViewToEntityMapper.remove(context, element);
    }

    private void removeEntityElement(UpdateContext context, Object element) {
        context.getEntityManager().remove(element);
    }

    private E getParentEntityReference(UpdateContext context, Object view) {
        return view == null ? null : (E) parentReferenceViewToEntityMapper.applyToEntity(context, null, view);
    }

    public void flushQuerySetElement(UpdateContext context, Object element, Object oldParent, Object view, String parameterPrefix, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        if (childViewToEntityMapper != null) {
            flushQuerySetEntityOnViewElement(context, element, getParentEntityReference(context, oldParent), getParentEntityReference(context, view), parameterPrefix, nestedGraphNode);
        } else {
            flushQuerySetEntityOnEntityElement(context, element, getParentEntityReference(context, oldParent), getParentEntityReference(context, view), parameterPrefix, nestedGraphNode);
        }
    }

    public void flushQuerySetEntityOnElement(UpdateContext context, Object element, E oldParent, E entity, String parameterPrefix, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        if (childViewToEntityMapper != null) {
            flushQuerySetEntityOnViewElement(context, element, oldParent, entity, parameterPrefix, nestedGraphNode);
        } else {
            flushQuerySetEntityOnEntityElement(context, element, oldParent, entity, parameterPrefix, nestedGraphNode);
        }
    }

    private void flushQuerySetEntityOnViewElement(UpdateContext context, Object element, E oldParent, E newValue, String parameterPrefix, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        flushQuerySetEntityOnElement(context, element, oldParent, newValue, parameterPrefix, nestedGraphNode, childViewToEntityMapper);
    }

    private void flushQuerySetEntityOnEntityElement(UpdateContext context, Object element, E oldParent, E newValue, String parameterPrefix, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        parentEntityOnChildEntityMapper.map(newValue, element);
        flushQuerySetEntityOnElement(context, element, oldParent, newValue, parameterPrefix, nestedGraphNode, childEntityToEntityMapper);
    }

    private void flushQuerySetEntityOnElement(UpdateContext context, Object element, E oldParent, E newValue, String parameterPrefix, DirtyAttributeFlusher<?, E, Object> nestedGraphNode, InverseElementToEntityMapper elementToEntityMapper) {
        if (shouldPersist(element) || nestedGraphNode != null && !nestedGraphNode.supportsQueryFlush()) {
            elementToEntityMapper.flushEntity(context, oldParent, newValue, element, nestedGraphNode);
        } else {
            int orphanRemovalStartIndex = context.getOrphanRemovalDeleters().size();
            Query q = elementToEntityMapper.createInverseUpdateQuery(context, element, nestedGraphNode, parentReferenceAttributeFlusher);
            if (nestedGraphNode != null) {
                nestedGraphNode.flushQuery(context, parameterPrefix, q, element, null, element, null);
            }
            parentReferenceAttributeFlusher.flushQuery(context, parameterPrefix, q, null, null, newValue, null);
            if (q != null) {
                int updated = q.executeUpdate();

                if (updated != 1) {
                    throw new OptimisticLockException(null, element);
                }
            }
            context.removeOrphans(orphanRemovalStartIndex);
        }
    }

    public void flushEntitySetElement(UpdateContext context, Iterable<?> elements, E oldParent, E newValue) {
        if (childViewToEntityMapper != null) {
            for (Object element : elements) {
                flushEntitySetViewElement(context, element, oldParent, newValue, null);
            }
        } else {
            for (Object element : elements) {
                flushEntitySetEntityElement(context, element, oldParent, newValue, null);
            }
        }
    }

    public void flushEntitySetElement(UpdateContext context, Object element, E oldParent, E newValue, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        if (childViewToEntityMapper != null) {
            flushEntitySetViewElement(context, element, oldParent, newValue, nestedGraphNode);
        } else {
            flushEntitySetEntityElement(context, element, oldParent, newValue, nestedGraphNode);
        }
    }

    private void flushEntitySetViewElement(UpdateContext context, Object child, E oldParent, E newParent, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        childViewToEntityMapper.flushEntity(context, oldParent, newParent, child, nestedGraphNode);
    }

    private void flushEntitySetEntityElement(UpdateContext context, Object child, E oldParent, E newParent, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        childEntityToEntityMapper.flushEntity(context, oldParent, newParent, child, nestedGraphNode);
    }

    private boolean shouldPersist(Object view) {
        return view instanceof EntityViewProxy && ((EntityViewProxy) view).$$_isNew();
    }

    public boolean supportsQueryFlush() {
        return parentReferenceAttributeFlusher.supportsQueryFlush();
    }
}
