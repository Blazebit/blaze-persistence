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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.parser.EntityMetamodel;
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
import com.blazebit.persistence.view.impl.mapper.Mapper;
import com.blazebit.persistence.view.impl.mapper.Mappers;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;

import javax.persistence.Query;
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

    public static <E> InverseFlusher<E> forAttribute(EntityViewManagerImpl evm, ManagedViewType<?> viewType, AbstractMethodAttribute<?, ?> attribute, TypeDescriptor childTypeDescriptor) {
        if (attribute.getMappedBy() != null) {
            String attributeLocation = attribute.getLocation();
            Type<?> elementType = attribute instanceof PluralAttribute<?, ?, ?> ? ((PluralAttribute<?, ?, ?>) attribute).getElementType() : ((SingularAttribute<?, ?>) attribute).getType();
            Class<?> elementEntityClass = null;

            AttributeAccessor parentReferenceAttributeAccessor = null;
            Mapper<Object, Object> parentEntityOnChildViewMapper = null;
            Mapper<Object, Object> parentEntityOnChildEntityMapper = null;
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
                    parentEntityOnChildEntityMapper = (Mapper<Object, Object>) Mappers.forEntityAttributeMapping(
                            evm,
                            viewType.getEntityClass(),
                            childViewType.getEntityClass(),
                            attribute.getWritableMappedByMappings()
                    );
                    childReferenceViewToEntityMapper = new LoadOrPersistViewToEntityMapper(
                            attributeLocation,
                            evm,
                            childViewType.getJavaType(),
                            Collections.<Type<?>>emptySet(),
                            Collections.<Type<?>>emptySet(),
                            new ReferenceEntityLoader(evm, childViewType, EntityViewUpdaterImpl.createViewIdMapper(evm, childViewType)),
                            Accessors.forViewId(evm, childViewType, true),
                            true
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
                }
            } else {
                if (childTypeDescriptor.isSubview()) {
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
                            Collections.<Type<?>>emptySet(),
                            Collections.<Type<?>>emptySet(),
                            new ReferenceEntityLoader(evm, childViewType, EntityViewUpdaterImpl.createViewIdMapper(evm, childViewType)),
                            Accessors.forViewId(evm, childViewType, true),
                            true
                    );
                    parentEntityOnChildEntityMapper = Mappers.forAccessor(parentReferenceAttributeAccessor);
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
                    parentEntityOnChildViewMapper = Mappers.forAccessor(parentReferenceAttributeAccessor);
                }
            }

            DirtyAttributeFlusher<?, Object, Object> parentReferenceAttributeFlusher = new ParentReferenceAttributeFlusher<>(
                    evm,
                    viewType.getEntityClass(),
                    attributeLocation,
                    attribute.getMappedBy(),
                    attribute.getWritableMappedByMappings(),
                    parentReferenceTypeDescriptor,
                    parentReferenceAttributeAccessor,
                    parentEntityOnChildViewMapper
            );

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
                ExtendedAttribute extendedAttribute = elementManagedType.getAttribute(mapping);
                if (childTypeDescriptor.isSubview()) {
                    deleter = new ViewTypeCascadeDeleter(childTypeDescriptor.getViewToEntityMapper());
                } else if (childTypeDescriptor.isJpaEntity()) {
                    deleter = new UnmappedBasicAttributeCascadeDeleter(evm, mapping, extendedAttribute, mapping + "." + parentIdAttributeName, false);
                }
            }

            if (childTypeDescriptor.isSubview()) {
                ViewType<?> childViewType = (ViewType<?>) elementType;
                childViewToEntityMapper = new InverseViewToEntityMapper(
                        evm,
                        childViewType,
                        parentEntityOnChildViewMapper,
                        parentEntityOnChildEntityMapper,
                        childTypeDescriptor.getViewToEntityMapper(),
                        parentReferenceAttributeFlusher,
                        EntityViewUpdaterImpl.createIdFlusher(evm, childViewType, EntityViewUpdaterImpl.createViewIdMapper(evm, childViewType))
                );
            } else if (childTypeDescriptor.isJpaEntity()) {
                Class<?> childType = elementType.getJavaType();
                childEntityToEntityMapper = new InverseEntityToEntityMapper(
                        evm,
                        evm.getMetamodel().getEntityMetamodel().entity(childType),
                        parentEntityOnChildEntityMapper,
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
                    parentEntityOnChildEntityMapper,
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

    public void flushQuerySetElement(UpdateContext context, Object element, Object view, String parameterPrefix, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        if (childViewToEntityMapper != null) {
            flushQuerySetEntityOnViewElement(context, element, getParentEntityReference(context, view), parameterPrefix, nestedGraphNode);
        } else {
            flushQuerySetEntityOnEntityElement(context, element, getParentEntityReference(context, view), parameterPrefix, nestedGraphNode);
        }
    }

    public void flushQuerySetEntityOnElement(UpdateContext context, Object element, E entity, String parameterPrefix, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        if (childViewToEntityMapper != null) {
            flushQuerySetEntityOnViewElement(context, element, entity, parameterPrefix, nestedGraphNode);
        } else {
            flushQuerySetEntityOnEntityElement(context, element, entity, parameterPrefix, nestedGraphNode);
        }
    }

    private void flushQuerySetEntityOnViewElement(UpdateContext context, Object element, E newValue, String parameterPrefix, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        flushQuerySetEntityOnElement(context, element, newValue, parameterPrefix, nestedGraphNode, childViewToEntityMapper);
    }

    private void flushQuerySetEntityOnEntityElement(UpdateContext context, Object element, E newValue, String parameterPrefix, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        parentEntityOnChildEntityMapper.map(newValue, element);
        flushQuerySetEntityOnElement(context, element, newValue, parameterPrefix, nestedGraphNode, childEntityToEntityMapper);
    }

    private void flushQuerySetEntityOnElement(UpdateContext context, Object element, E newValue, String parameterPrefix, DirtyAttributeFlusher<?, E, Object> nestedGraphNode, InverseElementToEntityMapper elementToEntityMapper) {
        if (shouldPersist(element) || nestedGraphNode != null && !nestedGraphNode.supportsQueryFlush()) {
            elementToEntityMapper.flushEntity(context, newValue, element, nestedGraphNode);
        } else {
            int orphanRemovalStartIndex = context.getOrphanRemovalDeleters().size();
            Query q = elementToEntityMapper.createInverseUpdateQuery(context, element, nestedGraphNode, parentReferenceAttributeFlusher);
            if (nestedGraphNode != null) {
                nestedGraphNode.flushQuery(context, parameterPrefix, q, null, element, null);
            }
            parentReferenceAttributeFlusher.flushQuery(context, parameterPrefix, q, null, newValue, null);
            if (q != null) {
                int updated = q.executeUpdate();

                if (updated != 1) {
                    throw new OptimisticLockException(null, element);
                }
            }
            context.removeOrphans(orphanRemovalStartIndex);
        }
    }

    public void flushEntitySetElement(UpdateContext context, Iterable<?> elements, E newValue) {
        if (childViewToEntityMapper != null) {
            for (Object element : elements) {
                flushEntitySetViewElement(context, element, newValue, null);
            }
        } else {
            for (Object element : elements) {
                flushEntitySetEntityElement(context, element, newValue, null);
            }
        }
    }

    public void flushEntitySetElement(UpdateContext context, Object element, E newValue, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        if (childViewToEntityMapper != null) {
            flushEntitySetViewElement(context, element, newValue, nestedGraphNode);
        } else {
            flushEntitySetEntityElement(context, element, newValue, nestedGraphNode);
        }
    }

    private void flushEntitySetViewElement(UpdateContext context, Object child, E newParent, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        childViewToEntityMapper.flushEntity(context, newParent, child, nestedGraphNode);
    }

    private void flushEntitySetEntityElement(UpdateContext context, Object child, E newParent, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        childEntityToEntityMapper.flushEntity(context, newParent, child, nestedGraphNode);
    }

    private boolean shouldPersist(Object view) {
        return view instanceof EntityViewProxy && ((EntityViewProxy) view).$$_isNew();
    }
}
