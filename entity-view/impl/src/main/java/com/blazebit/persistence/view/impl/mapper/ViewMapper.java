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

package com.blazebit.persistence.view.impl.mapper;

import com.blazebit.persistence.view.ConvertOption;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.MapInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.metamodel.AbstractAttribute;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.proxy.ConvertReflectionInstantiator;
import com.blazebit.persistence.view.impl.proxy.ObjectInstantiator;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.type.DirtyStateTrackable;
import com.blazebit.persistence.view.spi.type.DirtyTracker;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ViewMapper<S, T> {

    private final int[] dirtyMapping;
    private final boolean tryCopyInitialState;
    private final ObjectMapper[] objectMappers;
    private final ObjectInstantiator<T> objectInstantiator;
    private final EntityViewKindMapping entityViewKindMapping;
    private final Method postConvert;
    private final boolean postConvertUsesSource;

    public ViewMapper(ManagedViewType<S> sourceType, ManagedViewType<T> targetType, MappingConstructorImpl<T> targetConstructor, boolean ignoreMissing, Boolean maybeMarkNew, EntityViewManager entityViewManager, ProxyFactory proxyFactory, String prefix, Map<String, Key<Object, Object>> subMappers) {
        if ((sourceType != null) && !targetType.getEntityClass().isAssignableFrom(sourceType.getEntityClass())) {
            throw inconvertible("Incompatible entity types!", sourceType, targetType);
        }

        EntityViewKindMapping entityViewKindMapping;
        if (maybeMarkNew != null && maybeMarkNew) {
            entityViewKindMapping = EntityViewKindMapping.MARK_NEW;
        } else {
            if (targetType.isCreatable()) {
                entityViewKindMapping = EntityViewKindMapping.MAP_REFERENCE_AND_NEW;
            } else {
                entityViewKindMapping = EntityViewKindMapping.MAP_REFERENCE;
            }
        }
        List<ParameterAttribute<? super T, ?>> parameterAttributes;
        if (targetConstructor == null) {
            targetConstructor = getDefaultConstructor(targetType);
        }
        if (targetConstructor == null) {
            parameterAttributes = Collections.emptyList();
        } else {
            parameterAttributes = targetConstructor.getParameterAttributes();
        }
        Set<MethodAttribute<? super T, ?>> attributes = targetType.getAttributes();
        Class<?>[] parameterTypes = new Class[attributes.size() + parameterAttributes.size()];
        ObjectMapper[] objectMappers = new ObjectMapper[attributes.size() + parameterAttributes.size()];
        Iterator<MethodAttribute<? super T, ?>> iterator = attributes.iterator();
        MethodAttribute<? super T, ?> idAttribute = null;
        List<Integer> dirtyMapping = new ArrayList<>();
        int i = 0;

        // Id attribute is always the first
        if (targetType instanceof ViewType<?>) {
            idAttribute = ((ViewType<T>) targetType).getIdAttribute();
            parameterTypes[i] = idAttribute.getConvertedJavaType();
            objectMappers[i] = createAccessor(sourceType, targetType, ignoreMissing, entityViewKindMapping, entityViewManager, proxyFactory, idAttribute, prefix, subMappers);
            i++;
        }

        // For each target attribute
        while (iterator.hasNext()) {
            MethodAttribute<? super T, ?> targetAttribute = iterator.next();
            if (targetAttribute != idAttribute) {
                parameterTypes[i] = targetAttribute.getConvertedJavaType();
                objectMappers[i] = createAccessor(sourceType, targetType, ignoreMissing, entityViewKindMapping, entityViewManager, proxyFactory, targetAttribute, prefix, subMappers);
                // Extract a mapping from target dirty state index to the source
                int dirtyStateIndex = ((AbstractMethodAttribute<?, ?>) targetAttribute).getDirtyStateIndex();
                if (dirtyStateIndex != -1) {
                    // Does dirty state need to be copied from entities? Can this be done without modifying the entity class?
                    MethodAttribute<? super S, ?> sourceAttribute = (sourceType == null) ? null : sourceType.getAttribute(targetAttribute.getName());
                    if (sourceAttribute != null) {
                        int sourceIndex = ((AbstractMethodAttribute<?, ?>) sourceAttribute).getDirtyStateIndex();
                        if (sourceIndex != -1) {
                            for (int j = dirtyMapping.size(); j <= dirtyStateIndex; j++) {
                                dirtyMapping.add(-1);
                            }
                            dirtyMapping.set(dirtyStateIndex, sourceIndex);
                        }
                    }
                }
                i++;
            }
        }

        for (ParameterAttribute<? super T, ?> parameterAttribute : parameterAttributes) {
            parameterTypes[i] = parameterAttribute.getConvertedJavaType();
            if (parameterAttribute.getMappingType() == Attribute.MappingType.PARAMETER) {
                objectMappers[i] = new ParameterObjectMapper(((MappingAttribute<?, ?>) parameterAttribute).getMapping());
            }
            i++;
        }

        if (dirtyMapping.isEmpty()) {
            this.dirtyMapping = null;
        } else {
            int[] dirtyMappingArray = new int[dirtyMapping.size()];
            for (i = 0; i < dirtyMapping.size(); i++) {
                dirtyMappingArray[i] = dirtyMapping.get(i);
            }
            this.dirtyMapping = dirtyMappingArray;
        }

        this.tryCopyInitialState = entityViewKindMapping != EntityViewKindMapping.MARK_NEW;
        this.objectMappers = objectMappers;
        try {
            this.objectInstantiator = new ConvertReflectionInstantiator<>(proxyFactory, targetType, parameterTypes, parameterAttributes.size(), entityViewManager);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Empty constructor is required for conversion. Please make sure " + targetType.getJavaType().getName() + " has an empty constructor!", ex);
        }
        if (entityViewKindMapping == EntityViewKindMapping.MARK_NEW && !targetType.isCreatable()) {
            throw new IllegalArgumentException("Defined to convert to new object but target view type isn't annotated with @CreatableEntityView: " + targetType.getJavaType().getName());
        }
        this.entityViewKindMapping = entityViewKindMapping;
        Method postConvertMethod = targetType.getPostConvertMethod();
        if (postConvertMethod != null) {
            postConvertMethod.setAccessible(true);
        }
        this.postConvert = postConvertMethod;
        this.postConvertUsesSource = postConvertMethod != null && postConvertMethod.getParameterTypes().length != 0;
    }

    private ObjectMapper createAccessor(ManagedViewType<S> sourceType, ManagedViewType<T> targetType, boolean ignoreMissing, EntityViewKindMapping entityViewKindMapping, EntityViewManager entityViewManager, ProxyFactory proxyFactory, MethodAttribute<? super T, ?> targetAttribute, String prefix, Map<String, Key<Object, Object>> subMappers) {
        String newPrefix;
        if (prefix.isEmpty()) {
            newPrefix = targetAttribute.getName();
        } else {
            newPrefix = prefix + "." + targetAttribute.getName();
        }

        Type<?> attributeType;
        MappingConstructorImpl<?> constructor = null;
        Boolean maybeMarkNew = entityViewKindMapping == EntityViewKindMapping.MARK_NEW ? null : false;
        Key<Object, Object> subMapperKey = subMappers.get(newPrefix);
        if (subMapperKey == Key.EXCLUDE_MARKER) {
            return null;
        } else if (subMapperKey != null) {
            ignoreMissing = subMapperKey.ignoreMissing;
            maybeMarkNew = subMapperKey.markNew;
        }

        // Try to find a source attribute
        MethodAttribute<?, ?> sourceAttribute;
        AttributeAccessor accessor;
        if (sourceType == null) {
            sourceAttribute = null;
            if (targetAttribute.getMappingType() == Attribute.MappingType.PARAMETER) {
                return new ParameterObjectMapper(((MappingAttribute<?, ?>) targetAttribute).getMapping());
            }
            accessor = Accessors.forEntityMapping((EntityViewManagerImpl) entityViewManager, targetAttribute);
            if (accessor == null) {
                if (ignoreMissing) {
                    return null;
                }
                throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type is missing in source type!", targetType);
            }
        } else {
            sourceAttribute = sourceType.getAttribute(targetAttribute.getName());
            if (sourceAttribute == null) {
                if (targetAttribute.getMappingType() == Attribute.MappingType.PARAMETER) {
                    return new ParameterObjectMapper(((MappingAttribute<?, ?>) targetAttribute).getMapping());
                }
                // Optionally ignore missing attributes
                if (ignoreMissing) {
                    return null;
                }
                throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type is missing in source type!", sourceType, targetType);
            }
            accessor = Accessors.forViewAttribute(null, sourceAttribute, true);
        }

        // Handle conversion from one type to another
        if (targetAttribute.isCollection()) {
            if ((sourceAttribute != null) && (targetAttribute.getConvertedJavaType() != sourceAttribute.getConvertedJavaType())) {
                throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type has a different plural type than in source type!", sourceType, targetType);
            }
            PluralAttribute<?, ?, ?> targetPluralAttr = (PluralAttribute<?, ?, ?>) targetAttribute;
            Type<?> elementType = (sourceAttribute == null) ? null : ((PluralAttribute<?, ?, ?>) sourceAttribute).getElementType();
            ViewMapper<Object, Object> valueMapper = null;

            attributeType = targetPluralAttr.getElementType();
            if (subMapperKey != null) {
                attributeType = subMapperKey.targetType;
                constructor = subMapperKey.targetConstructor;
            }

            if (targetAttribute.isSubview()) {
                valueMapper = createViewMapper(elementType, attributeType, constructor, ignoreMissing, maybeMarkNew, entityViewManager, proxyFactory, newPrefix, subMappers);
            } else if ((sourceType != null) && (targetPluralAttr.getElementType() != elementType)) {
                throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type has a different element type than in source type!", sourceType, targetType);
            }

            boolean needsDirtyTracker = ((AbstractAttribute<?, ?>) targetAttribute).needsDirtyTracker();
            if (targetPluralAttr.getCollectionType() == PluralAttribute.CollectionType.MAP) {
                MapAttribute<?, ?, ?> targetMapAttr = (MapAttribute<?, ?, ?>) targetAttribute;
                Type<?> keyType = (sourceAttribute == null) ? null : ((MapAttribute<?, ?, ?>) sourceAttribute).getKeyType();
                ViewMapper<Object, Object> keyMapper = null;

                if (targetMapAttr.isKeySubview()) {
                    String newKeyPrefix = "KEY(" + newPrefix + ")";
                    Key<Object, Object> keySubMapperKey = subMappers.get(newKeyPrefix);
                    if (keySubMapperKey == Key.EXCLUDE_MARKER) {
                        keyMapper = null;
                    } else {
                        constructor = null;
                        Type<?> keyTargetType = targetMapAttr.getKeyType();
                        Boolean maybeMarkNewKey = entityViewKindMapping == EntityViewKindMapping.MARK_NEW ? null : false;
                        if (subMapperKey != null) {
                            constructor = keySubMapperKey.targetConstructor;
                            keyTargetType = keySubMapperKey.targetType;
                            ignoreMissing = keySubMapperKey.ignoreMissing;
                            maybeMarkNewKey = keySubMapperKey.markNew;
                        }

                        keyMapper = createViewMapper(keyType, keyTargetType, constructor, ignoreMissing, maybeMarkNewKey, entityViewManager, proxyFactory, newPrefix, subMappers);
                    }
                } else if ((sourceType != null) && (targetMapAttr.getKeyType() != keyType)) {
                    throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type has a different key type than in source type!", sourceType, targetType);
                }

                MapInstantiatorImplementor<?, ?> mapInstantiator = ((AbstractAttribute<?, ?>) targetAttribute).getMapInstantiator();
                return new MapObjectMapper(accessor, needsDirtyTracker, entityViewKindMapping != EntityViewKindMapping.MARK_NEW, mapInstantiator, keyMapper, valueMapper);
            } else {
                CollectionInstantiatorImplementor<?, ?> collectionInstantiator = ((AbstractAttribute<?, ?>) targetAttribute).getCollectionInstantiator();
                return new CollectionObjectMapper(accessor, needsDirtyTracker, entityViewKindMapping != EntityViewKindMapping.MARK_NEW, collectionInstantiator, valueMapper);
            }
        } else if (targetAttribute.isSubview()) {
            attributeType = ((SingularAttribute<?, ?>) targetAttribute).getType();
            if (subMapperKey != null) {
                attributeType = subMapperKey.targetType;
                constructor = subMapperKey.targetConstructor;
            }
            Type<?> type = (sourceAttribute == null) ? null : ((SingularAttribute<?, ?>) sourceAttribute).getType();
            ViewMapper<Object, Object> mapper = createViewMapper(type, attributeType, constructor, ignoreMissing, maybeMarkNew, entityViewManager, proxyFactory, newPrefix, subMappers);
            return new AttributeObjectMapper(accessor, mapper);
        } else if ((sourceAttribute != null) && (targetAttribute.getConvertedJavaType() != sourceAttribute.getConvertedJavaType())) {
            throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type has a different type than in source type!", sourceType, targetType);
        } else {
            return new PassthroughObjectMapper(accessor);
        }
    }

    private MappingConstructorImpl<T> getDefaultConstructor(ManagedViewType<T> targetType) {
        MappingConstructorImpl<T> constructor = (MappingConstructorImpl<T>) targetType.getConstructor("init");
        if (constructor == null) {
            switch (targetType.getConstructors().size()) {
                case 0:
                    break;
                case 1:
                    constructor = (MappingConstructorImpl<T>) targetType.getConstructors().iterator().next();
                    break;
                default:
                    constructor = (MappingConstructorImpl<T>) targetType.getConstructor();
                    break;
            }
        }
        return constructor;
    }

    private ViewMapper<Object, Object> createViewMapper(Type<?> source, Type<?> target, MappingConstructorImpl<?> targetConstructor, boolean ignoreMissing, Boolean markNew, EntityViewManager entityViewManager, ProxyFactory proxyFactory, String prefix, Map<String, Key<Object, Object>> subMappers) {
        ManagedViewType<Object> sourceType = (ManagedViewType<Object>) source;
        ManagedViewType<Object> targetType = (ManagedViewType<Object>) target;
        return new ViewMapper<>(sourceType, targetType, (MappingConstructorImpl<Object>) targetConstructor, ignoreMissing, markNew, entityViewManager, proxyFactory, prefix, subMappers);
    }

    private RuntimeException inconvertible(String reason, ManagedViewType<S> sourceType, ManagedViewType<T> targetType) {
        return new IllegalArgumentException("Can't convert from '" + sourceType.getJavaType().getName() + "' to '" + targetType.getJavaType().getName() + "'! " + reason);
    }

    private RuntimeException inconvertible(String reason, ManagedViewType<T> targetType) {
        return new IllegalArgumentException("Can't convert from '" + targetType.getEntityClass().getName() + "' to '" + targetType.getJavaType().getName() + "'! " + reason);
    }

    public T map(S source, Map<String, Object> optionalParameters) {
        Object[] tuple = new Object[objectMappers.length];
        for (int i = 0; i < objectMappers.length; i++) {
            if (objectMappers[i] != null) {
                tuple[i] = objectMappers[i].getValue(source, optionalParameters);
            }
        }
        T result = objectInstantiator.newInstance(tuple);
        boolean copiedInitialState = false;
        if (dirtyMapping != null && source instanceof DirtyTracker) {
            DirtyTracker oldDirtyTracker = (DirtyTracker) source;
            DirtyTracker dirtyTracker = (DirtyTracker) result;
            if (tryCopyInitialState && oldDirtyTracker instanceof DirtyStateTrackable && dirtyTracker instanceof DirtyStateTrackable) {
                Object[] oldInitial = ((DirtyStateTrackable) oldDirtyTracker).$$_getInitialState();
                Object[] newInitial = ((DirtyStateTrackable) dirtyTracker).$$_getInitialState();
                for (int i = 0; i < dirtyMapping.length; i++) {
                    int dirtyStateIndex = dirtyMapping[i];
                    if (oldDirtyTracker.$$_isDirty(dirtyStateIndex)) {
                        newInitial[i] = oldInitial[dirtyStateIndex];
                        dirtyTracker.$$_markDirty(i);
                    }
                }
                copiedInitialState = true;
            } else {
                for (int i = 0; i < dirtyMapping.length; i++) {
                    if (oldDirtyTracker.$$_isDirty(dirtyMapping[i])) {
                        dirtyTracker.$$_markDirty(i);
                    }
                }
            }
        }
        boolean tryResetInitialState = false;
        if (source instanceof EntityViewProxy) {
            //CHECKSTYLE:OFF: FallThrough
            switch (entityViewKindMapping) {
                case MAP_REFERENCE_AND_NEW:
                    if (((EntityViewProxy) source).$$_isNew()) {
                        ((MutableStateTrackable) result).$$_setIsNew(tryResetInitialState = true);
                    }
                case MAP_REFERENCE:
                    if (((EntityViewProxy) source).$$_isReference()) {
                        ((EntityViewProxy) result).$$_setIsReference(tryResetInitialState = true);
                    }
                    break;
                case MARK_NEW:
                    ((MutableStateTrackable) result).$$_setIsNew(tryResetInitialState = true);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported entity view kind mapping: " + entityViewKindMapping);
            }
            //CHECKSTYLE:ON: FallThrough
        } else if (entityViewKindMapping == EntityViewKindMapping.MARK_NEW) {
            ((MutableStateTrackable) result).$$_setIsNew(tryResetInitialState = true);
        }
        if (!copiedInitialState && tryResetInitialState && result instanceof DirtyStateTrackable) {
            // Reset the initial state i.e. mark it as new
            Object[] initialState = ((DirtyStateTrackable) result).$$_getInitialState();
            Arrays.fill(initialState, null);
        }
        if (postConvert != null) {
            try {
                if (postConvertUsesSource) {
                    postConvert.invoke(result, source);
                } else {
                    postConvert.invoke(result);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Error during invocation of post convert method!", ex);
            }
        }
        return result;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class Key<S, T> {

        public static final Key<Object, Object> EXCLUDE_MARKER = new Key<>(null, null, null, false, false);

        private final ManagedViewTypeImplementor<S> sourceType;
        private final ManagedViewTypeImplementor<T> targetType;
        private final MappingConstructorImpl<T> targetConstructor;
        private final boolean ignoreMissing;
        private final boolean markNew;

        public Key(ManagedViewTypeImplementor<S> sourceType, ManagedViewTypeImplementor<T> targetType, MappingConstructorImpl<T> targetConstructor, boolean ignoreMissing, boolean markNew) {
            this.sourceType = sourceType;
            this.targetType = targetType;
            this.targetConstructor = targetConstructor;
            this.ignoreMissing = ignoreMissing;
            this.markNew = markNew;
        }

        public ViewMapper<S, T> createViewMapper(EntityViewManager entityViewManager, ProxyFactory proxyFactory, Map<String, ViewMapper.Key<Object, Object>> subMappers) {
            return new ViewMapper<>(sourceType, targetType, targetConstructor, ignoreMissing, markNew, entityViewManager, proxyFactory, "", subMappers);
        }

        public static <Y> Key<Object, Y> create(ViewMetamodel metamodel, Object source, Class<Y> targetEntityViewClass, String constructorName, ConvertOption... convertOptions) {
            if (source instanceof EntityViewProxy) {
                EntityViewProxy sourceProxy = (EntityViewProxy) source;
                return (Key<Object, Y>) createWithConvertOptions(metamodel, sourceProxy.$$_getEntityViewClass(), targetEntityViewClass, constructorName, false, convertOptions);
            } else {
                ManagedViewType<Y> targetViewType = metamodel.managedView(targetEntityViewClass);
                if (targetViewType == null) {
                    throw new IllegalArgumentException("Unknown target view type: " + targetEntityViewClass.getName());
                } else if (!targetViewType.getEntityClass().isInstance(source)) {
                    throw new IllegalArgumentException("The source object is not an instance of the target views entity type " + targetViewType.getEntityClass().getName() + ": " + source.getClass().getName());
                }
                return createWithConvertOptions(metamodel, null, targetEntityViewClass, constructorName, true, convertOptions);
            }
        }

        public static <X, Y> Key<X, Y> create(ViewMetamodel metamodel, Class<X> sourceEntityViewClass, Class<Y> targetEntityViewClass, String constructorName, ConvertOption... convertOptions) {
            return createWithConvertOptions(metamodel, sourceEntityViewClass, targetEntityViewClass, constructorName, false, convertOptions);
        }

        public static <X, Y> Key<X, Y> create(ViewMetamodel metamodel, Class<Y> targetEntityViewClass, String constructorName, ConvertOption... convertOptions) {
            return createWithConvertOptions(metamodel, null, targetEntityViewClass, constructorName, true, convertOptions);
        }

        public static <X, Y> Key<X, Y> create(ViewMetamodel metamodel, Class<X> sourceEntityViewClass, Class<Y> targetEntityViewClass, String targetConstructorName, boolean ignoreMissingAttributes, boolean markNew) {
            return createWithFlags(metamodel, sourceEntityViewClass, targetEntityViewClass, targetConstructorName, false, ignoreMissingAttributes, markNew);
        }

        public ManagedViewTypeImplementor<S> getSourceType() {
            return sourceType;
        }

        public ManagedViewTypeImplementor<T> getTargetType() {
            return targetType;
        }

        public boolean isIgnoreMissing() {
            return ignoreMissing;
        }

        public boolean isMarkNew() {
            return markNew;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this == EXCLUDE_MARKER || o == EXCLUDE_MARKER || getClass() != o.getClass()) {
                return false;
            }

            Key<?, ?> key = (Key<?, ?>) o;

            if (ignoreMissing != key.ignoreMissing) {
                return false;
            }
            if (markNew != key.markNew) {
                return false;
            }
            if ((sourceType == null) && (key.sourceType != null)) {
                return false;
            } else if ((sourceType != null) && (!sourceType.equals(key.sourceType))) {
                return false;
            }
            return targetType.equals(key.targetType);
        }

        @Override
        public int hashCode() {
            if (this == EXCLUDE_MARKER) {
                return 0;
            }
            int result = sourceType == null ? 0 : sourceType.hashCode();
            result = 31 * result + targetType.hashCode();
            result = 31 * result + (ignoreMissing ? 1 : 0);
            result = 31 * result + (markNew ? 1 : 0);
            return result;
        }

        private static <X, Y> Key<X, Y> createWithConvertOptions(ViewMetamodel metamodel, Class<X> sourceEntityViewClass, Class<Y> targetEntityViewClass, String constructorName, boolean useEntityAsSource, ConvertOption... convertOptions) {
            boolean ignoreMissingAttributes = false;
            boolean markNew = false;
            for (ConvertOption copyOption : convertOptions) {
                switch (copyOption) {
                    case CREATE_NEW:
                        markNew = true;
                        break;
                    case IGNORE_MISSING_ATTRIBUTES:
                        ignoreMissingAttributes = true;
                        break;
                    default:
                        break;
                }
            }
            return createWithFlags(metamodel, sourceEntityViewClass, targetEntityViewClass, constructorName, useEntityAsSource, ignoreMissingAttributes, markNew);
        }

        private static <X, Y> Key<X, Y> createWithFlags(ViewMetamodel metamodel, Class<X> sourceEntityViewClass, Class<Y> targetEntityViewClass, String targetConstructorName, boolean useEntityAsSource, boolean ignoreMissingAttributes, boolean markNew) {
            ManagedViewTypeImplementor<X> sourceViewType = (ManagedViewTypeImplementor<X>) metamodel.managedView(sourceEntityViewClass);
            if (!useEntityAsSource && (sourceViewType == null)) {
                throw new IllegalArgumentException("Unknown source view type: " + sourceEntityViewClass.getName());
            }
            ManagedViewTypeImplementor<Y> targetViewType = (ManagedViewTypeImplementor<Y>) metamodel.managedView(targetEntityViewClass);
            if (targetViewType == null) {
                throw new IllegalArgumentException("Unknown target view type: " + targetEntityViewClass.getName());
            }
            MappingConstructorImpl<Y> constructor = null;
            if (targetConstructorName != null) {
                constructor = (MappingConstructorImpl<Y>) targetViewType.getConstructor(targetConstructorName);
                if (constructor == null) {
                    throw new IllegalArgumentException("Unknown constructor '" + targetConstructorName + "' on target view type: " + targetEntityViewClass.getName());
                }
            }

            return new ViewMapper.Key<>(sourceViewType, targetViewType, constructor, ignoreMissingAttributes, markNew);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class MapObjectMapper implements ObjectMapper {
        private final AttributeAccessor accessor;
        private final boolean recording;
        private final boolean tryCopyDirtyState;
        private final MapInstantiatorImplementor<?, ?> mapInstantiator;
        private final ViewMapper<Object, Object> keyMapper;
        private final ViewMapper<Object, Object> valueMapper;

        public MapObjectMapper(AttributeAccessor accessor, boolean recording, boolean tryCopyDirtyState, MapInstantiatorImplementor<?, ?> mapInstantiator, ViewMapper<Object, Object> keyMapper, ViewMapper<Object, Object> valueMapper) {
            this.accessor = accessor;
            this.recording = recording;
            this.tryCopyDirtyState = tryCopyDirtyState;
            this.mapInstantiator = mapInstantiator;
            this.keyMapper = keyMapper;
            this.valueMapper = valueMapper;
        }

        @Override
        public Object getValue(Object object, Map<String, Object> optionalParameters) {
            Map<Object, Object> map = (Map<Object, Object>) accessor.getValue(object);
            Map<Object, Object> newMap = null;
            Map<Object, Object> backingMap;
            if (map != null) {
                Map<Object, Object> objectMapping = null;
                if (recording) {
                    RecordingMap<?, ?, ?> recordingMap = mapInstantiator.createRecordingMap(map.size());
                    newMap = (Map<Object, Object>) recordingMap;
                    if (tryCopyDirtyState) {
                        backingMap = (Map<Object, Object>) recordingMap.getDelegate();
                        if (map instanceof RecordingMap<?, ?, ?> && (keyMapper != null || valueMapper != null)) {
                            // We have to map the removed objects separately as these might be required for cascading actions
                            objectMapping = new IdentityHashMap<>(map.size() * 2);
                            if (keyMapper != null) {
                                for (Object e : ((RecordingMap<?, ?, ?>) map).getRemovedKeys()) {
                                    objectMapping.put(e, keyMapper.map(e, optionalParameters));
                                }
                            }
                            if (valueMapper != null) {
                                for (Object e : ((RecordingMap<?, ?, ?>) map).getRemovedElements()) {
                                    objectMapping.put(e, valueMapper.map(e, optionalParameters));
                                }
                            }
                        }
                    } else {
                        backingMap = newMap;
                    }
                } else {
                    newMap = backingMap = (Map<Object, Object>) mapInstantiator.createMap(map.size());
                }

                if (keyMapper != null && valueMapper != null) {
                    if (objectMapping == null) {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            backingMap.put(keyMapper.map(entry.getKey(), optionalParameters), valueMapper.map(entry.getValue(), optionalParameters));
                        }
                    } else {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            Object newKey = keyMapper.map(entry.getKey(), optionalParameters);
                            Object newValue = valueMapper.map(entry.getValue(), optionalParameters);
                            objectMapping.put(entry.getKey(), newKey);
                            objectMapping.put(entry.getValue(), newValue);
                            backingMap.put(newKey, newValue);
                        }
                    }
                } else if (keyMapper != null) {
                    if (objectMapping == null) {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            backingMap.put(keyMapper.map(entry.getKey(), optionalParameters), entry.getValue());
                        }
                    } else {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            Object newKey = keyMapper.map(entry.getKey(), optionalParameters);
                            objectMapping.put(entry.getKey(), newKey);
                            backingMap.put(newKey, entry.getValue());
                        }
                    }
                } else if (valueMapper != null) {
                    if (objectMapping == null) {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            backingMap.put(entry.getKey(), valueMapper.map(entry.getValue(), optionalParameters));
                        }
                    } else {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            Object newValue = valueMapper.map(entry.getValue(), optionalParameters);
                            objectMapping.put(entry.getValue(), newValue);
                            backingMap.put(entry.getKey(), newValue);
                        }
                    }
                } else {
                    backingMap.putAll(map);
                }

                if (recording && tryCopyDirtyState && map instanceof RecordingMap<?, ?, ?>) {
                    ((RecordingMap<Map<Object, Object>, Object, Object>) newMap).setActions((RecordingMap<Map<Object, Object>, Object, Object>) map, objectMapping);
                }
            }
            return newMap;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class CollectionObjectMapper implements ObjectMapper {
        private final AttributeAccessor accessor;
        private final boolean recording;
        private final boolean tryCopyDirtyState;
        private final CollectionInstantiatorImplementor<?, ?> collectionInstantiator;
        private final ViewMapper<Object, Object> valueMapper;

        public CollectionObjectMapper(AttributeAccessor accessor, boolean recording, boolean tryCopyDirtyState, CollectionInstantiatorImplementor<?, ?> collectionInstantiator, ViewMapper<Object, Object> valueMapper) {
            this.accessor = accessor;
            this.recording = recording;
            this.tryCopyDirtyState = tryCopyDirtyState;
            this.collectionInstantiator = collectionInstantiator;
            this.valueMapper = valueMapper;
        }

        @Override
        public Object getValue(Object object, Map<String, Object> optionalParameters) {
            Collection<Object> collection = (Collection<Object>) accessor.getValue(object);
            Collection<Object> newCollection = null;
            Collection<Object> backingCollection;
            if (collection != null) {
                Map<Object, Object> objectMapping = null;
                if (recording) {
                    RecordingCollection<?, ?> coll = collectionInstantiator.createRecordingCollection(collection.size());
                    newCollection = (Collection<Object>) coll;
                    if (tryCopyDirtyState) {
                        backingCollection = (Collection<Object>) coll.getDelegate();
                        if (collection instanceof RecordingCollection<?, ?> && valueMapper != null) {
                            objectMapping = new IdentityHashMap<>(collection.size());
                            for (Object e : ((RecordingCollection<?, ?>) collection).getRemovedElements()) {
                                objectMapping.put(e, valueMapper.map(e, optionalParameters));
                            }
                        }
                    } else {
                        backingCollection = newCollection;
                    }
                } else {
                    newCollection = backingCollection = (Collection<Object>) collectionInstantiator.createCollection(collection.size());
                }

                if (valueMapper != null) {
                    if (objectMapping == null) {
                        for (Object o : collection) {
                            backingCollection.add(valueMapper.map(o, optionalParameters));
                        }
                    } else {
                        for (Object o : collection) {
                            Object newObject = valueMapper.map(o, optionalParameters);
                            objectMapping.put(o, newObject);
                            backingCollection.add(newObject);
                        }
                    }
                } else {
                    backingCollection.addAll(collection);
                }

                if (recording && tryCopyDirtyState && collection instanceof RecordingCollection<?, ?>) {
                    ((RecordingCollection<Collection<Object>, Object>) newCollection).setActions((RecordingCollection<Collection<Object>, Object>) collection, objectMapping);
                }
            }
            return newCollection;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class AttributeObjectMapper implements ObjectMapper {
        private final AttributeAccessor accessor;
        private final ViewMapper<Object, Object> mapper;

        public AttributeObjectMapper(AttributeAccessor accessor, ViewMapper<Object, Object> mapper) {
            this.accessor = accessor;
            this.mapper = mapper;
        }

        @Override
        public Object getValue(Object object, Map<String, Object> optionalParameters) {
            Object value = accessor.getValue(object);
            if (value != null) {
                return mapper.map(value, optionalParameters);
            }
            return null;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static class PassthroughObjectMapper implements ObjectMapper {
        private final AttributeAccessor accessor;

        public PassthroughObjectMapper(AttributeAccessor accessor) {
            this.accessor = accessor;
        }

        @Override
        public Object getValue(Object object, Map<String, Object> optionalParameters) {
            return accessor.getValue(object);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static class ParameterObjectMapper implements ObjectMapper {

        private final String parameterName;

        public ParameterObjectMapper(String parameterName) {
            this.parameterName = parameterName;
        }

        @Override
        public Object getValue(Object object, Map<String, Object> optionalParameters) {
            return optionalParameters.get(parameterName);
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static interface ObjectMapper {
        public Object getValue(Object object, Map<String, Object> optionalParameters);
    }

    /**
     * @author Christian Beikov
     * @since 1.6.0
     */
    private static enum EntityViewKindMapping {
        MAP_REFERENCE,
        MAP_REFERENCE_AND_NEW,
        MARK_NEW
    }
}
