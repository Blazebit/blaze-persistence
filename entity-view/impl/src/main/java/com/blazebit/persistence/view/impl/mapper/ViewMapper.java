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

package com.blazebit.persistence.view.impl.mapper;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.collection.CollectionInstantiator;
import com.blazebit.persistence.view.impl.collection.MapInstantiator;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.metamodel.AbstractAttribute;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.proxy.ConvertReflectionInstantiator;
import com.blazebit.persistence.view.impl.proxy.DirtyTracker;
import com.blazebit.persistence.view.impl.proxy.ObjectInstantiator;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;

import java.util.ArrayList;
import java.util.Collection;
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
    private final AttributeAccessor[] sourceAccessors;
    private final ObjectInstantiator<T> objectInstantiator;

    public ViewMapper(ManagedViewType<S> sourceType, ManagedViewType<T> targetType, boolean ignoreMissing, boolean markNew, EntityViewManager entityViewManager, ProxyFactory proxyFactory) {
        if (!targetType.getEntityClass().isAssignableFrom(sourceType.getEntityClass())) {
            throw inconvertible("Incompatible entity types!", sourceType, targetType);
        }

        Set<MethodAttribute<? super T, ?>> attributes = targetType.getAttributes();
        Class<?>[] parameterTypes = new Class[attributes.size()];
        AttributeAccessor[] sourceAccessors = new AttributeAccessor[attributes.size()];
        Iterator<MethodAttribute<? super T, ?>> iterator = attributes.iterator();
        MethodAttribute<? super T, ?> idAttribute = null;
        List<Integer> dirtyMapping = new ArrayList<>();
        int i = 0;

        // Id attribute is always the first
        if (targetType instanceof ViewType<?>) {
            idAttribute = ((ViewType<T>) targetType).getIdAttribute();
            parameterTypes[i] = idAttribute.getConvertedJavaType();
            sourceAccessors[i] = createAccessor(sourceType, targetType, ignoreMissing, markNew, entityViewManager, proxyFactory, idAttribute);
            i++;
        }

        // For each target attribute
        while (iterator.hasNext()) {
            MethodAttribute<? super T, ?> targetAttribute = iterator.next();
            if (targetAttribute != idAttribute) {
                parameterTypes[i] = targetAttribute.getConvertedJavaType();
                sourceAccessors[i] = createAccessor(sourceType, targetType, ignoreMissing, markNew, entityViewManager, proxyFactory, targetAttribute);
                if (!markNew) {
                    // Extract a mapping from target dirty state index to the source
                    int dirtyStateIndex = ((AbstractMethodAttribute<?, ?>) targetAttribute).getDirtyStateIndex();
                    if (dirtyStateIndex != -1) {
                        MethodAttribute<? super S, ?> sourceAttribute = sourceType.getAttribute(targetAttribute.getName());
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
                }
                i++;
            }
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

        this.sourceAccessors = sourceAccessors;
        this.objectInstantiator = new ConvertReflectionInstantiator<>(proxyFactory, targetType, parameterTypes, markNew, entityViewManager);
    }

    private AttributeAccessor createAccessor(ManagedViewType<S> sourceType, ManagedViewType<T> targetType, boolean ignoreMissing, boolean markNew, EntityViewManager entityViewManager, ProxyFactory proxyFactory, MethodAttribute<? super T, ?> targetAttribute) {
        // Try to find a source attribute
        MethodAttribute<? super S, ?> sourceAttribute = sourceType.getAttribute(targetAttribute.getName());

        if (sourceAttribute == null) {
            // Optionally ignore missing attributes
            if (ignoreMissing) {
                return null;
            }
            throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type is missing in source type!", sourceType, targetType);
        }

        // Handle conversion from one type to another
        if (targetAttribute.isCollection()) {
            if (targetAttribute.getConvertedJavaType() != sourceAttribute.getConvertedJavaType()) {
                throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type has a different plural type than in source type!", sourceType, targetType);
            }
            PluralAttribute<?, ?, ?> targetPluralAttr = (PluralAttribute<?, ?, ?>) targetAttribute;
            PluralAttribute<?, ?, ?> sourcePluralAttr = (PluralAttribute<?, ?, ?>) sourceAttribute;
            ViewMapper<Object, Object> valueMapper = null;

            if (targetAttribute.isSubview()) {
                valueMapper = createViewMapper(sourcePluralAttr.getElementType(), targetPluralAttr.getElementType(), ignoreMissing, markNew, entityViewManager, proxyFactory);
            } else if (targetPluralAttr.getElementType() != sourcePluralAttr.getElementType()) {
                throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type has a different element type than in source type!", sourceType, targetType);
            }

            boolean needsDirtyTracker = ((AbstractAttribute<?, ?>) targetAttribute).needsDirtyTracker();
            if (targetPluralAttr.getCollectionType() == PluralAttribute.CollectionType.MAP) {
                MapAttribute<?, ?, ?> targetMapAttr = (MapAttribute<?, ?, ?>) targetAttribute;
                MapAttribute<?, ?, ?> sourceMapAttr = (MapAttribute<?, ?, ?>) sourceAttribute;
                ViewMapper<Object, Object> keyMapper = null;

                if (targetMapAttr.isKeySubview()) {
                    keyMapper = createViewMapper(sourceMapAttr.getKeyType(), targetMapAttr.getKeyType(), ignoreMissing, markNew, entityViewManager, proxyFactory);
                } else if (targetMapAttr.getKeyType() != sourceMapAttr.getKeyType()) {
                    throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type has a different key type than in source type!", sourceType, targetType);
                }

                MapInstantiator<?, ?> mapInstantiator = ((AbstractAttribute<?, ?>) targetAttribute).getMapInstantiator();
                return new MapMappingAccessor(Accessors.forViewAttribute(null, sourceAttribute, true), needsDirtyTracker, !markNew, mapInstantiator, keyMapper, valueMapper);
            } else {
                CollectionInstantiator collectionInstantiator = ((AbstractAttribute<?, ?>) targetAttribute).getCollectionInstantiator();
                return new CollectionMappingAccessor(Accessors.forViewAttribute(null, sourceAttribute, true), needsDirtyTracker, !markNew, collectionInstantiator, valueMapper);
            }
        } else if (targetAttribute.isSubview()) {
            ViewMapper<Object, Object> mapper = createViewMapper(((SingularAttribute<?, ?>) sourceAttribute).getType(), ((SingularAttribute<?, ?>) targetAttribute).getType(), ignoreMissing, markNew, entityViewManager, proxyFactory);
            return new AttributeMappingAccessor(Accessors.forViewAttribute(null, sourceAttribute, true), mapper);
        } else if (targetAttribute.getConvertedJavaType() != sourceAttribute.getConvertedJavaType()) {
            throw inconvertible("Attribute '" + targetAttribute.getName() + "' from target type has a different type than in source type!", sourceType, targetType);
        } else {
            return Accessors.forViewAttribute(null, sourceAttribute, true);
        }
    }

    private ViewMapper<Object, Object> createViewMapper(Type<?> source, Type<?> target, boolean ignoreMissing, boolean markNew, EntityViewManager entityViewManager, ProxyFactory proxyFactory) {
        ManagedViewType<Object> sourceType = (ManagedViewType<Object>) source;
        ManagedViewType<Object> targetType = (ManagedViewType<Object>) target;
        return new ViewMapper<>(sourceType, targetType, ignoreMissing, markNew, entityViewManager, proxyFactory);
    }

    private RuntimeException inconvertible(String reason, ManagedViewType<S> sourceType, ManagedViewType<T> targetType) {
        return new IllegalArgumentException("Can't convert from '" + sourceType.getJavaType().getName() + "' to '" + targetType.getJavaType().getName() + "'! " + reason);
    }

    public T map(S source) {
        Object[] tuple = new Object[sourceAccessors.length];
        for (int i = 0; i < sourceAccessors.length; i++) {
            if (sourceAccessors[i] != null) {
                tuple[i] = sourceAccessors[i].getValue(source);
            }
        }
        T result = objectInstantiator.newInstance(tuple);
        if (dirtyMapping != null && source instanceof DirtyTracker) {
            DirtyTracker oldDirtyTracker = (DirtyTracker) source;
            DirtyTracker dirtyTracker = (DirtyTracker) result;
            for (int i = 0; i < dirtyMapping.length; i++) {
                if (oldDirtyTracker.$$_isDirty(dirtyMapping[i])) {
                    dirtyTracker.$$_markDirty(i);
                }
            }
        }
        return result;
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class Key<S, T> {
        private final ManagedViewType<S> sourceType;
        private final ManagedViewType<T> targetType;
        private final boolean ignoreMissing;
        private final boolean markNew;

        public Key(ManagedViewType<S> sourceType, ManagedViewType<T> targetType, boolean ignoreMissing, boolean markNew) {
            this.sourceType = sourceType;
            this.targetType = targetType;
            this.ignoreMissing = ignoreMissing;
            this.markNew = markNew;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Key<?, ?> key = (Key<?, ?>) o;

            if (ignoreMissing != key.ignoreMissing) {
                return false;
            }
            if (markNew != key.markNew) {
                return false;
            }
            if (!sourceType.equals(key.sourceType)) {
                return false;
            }
            return targetType.equals(key.targetType);
        }

        @Override
        public int hashCode() {
            int result = sourceType.hashCode();
            result = 31 * result + targetType.hashCode();
            result = 31 * result + (ignoreMissing ? 1 : 0);
            result = 31 * result + (markNew ? 1 : 0);
            return result;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class MapMappingAccessor extends ReadOnlyAccessor {
        private final AttributeAccessor accessor;
        private final boolean recording;
        private final boolean copyDirtyState;
        private final MapInstantiator<?, ?> mapInstantiator;
        private final ViewMapper<Object, Object> keyMapper;
        private final ViewMapper<Object, Object> valueMapper;

        public MapMappingAccessor(AttributeAccessor accessor, boolean recording, boolean copyDirtyState, MapInstantiator<?, ?> mapInstantiator, ViewMapper<Object, Object> keyMapper, ViewMapper<Object, Object> valueMapper) {
            this.accessor = accessor;
            this.recording = recording;
            this.copyDirtyState = copyDirtyState;
            this.mapInstantiator = mapInstantiator;
            this.keyMapper = keyMapper;
            this.valueMapper = valueMapper;
        }

        @Override
        public Object getValue(Object object) {
            Map<Object, Object> map = (Map<Object, Object>) accessor.getValue(object);
            Map<Object, Object> newMap = null;
            Map<Object, Object> backingMap;
            if (map != null) {
                Map<Object, Object> objectMapping = null;
                if (recording) {
                    RecordingMap<?, ?, ?> recordingMap = mapInstantiator.createRecordingCollection(map.size());
                    newMap = (Map<Object, Object>) recordingMap;
                    if (copyDirtyState) {
                        backingMap = (Map<Object, Object>) recordingMap.getDelegate();
                        if (map instanceof RecordingMap<?, ?, ?> && (keyMapper != null || valueMapper != null)) {
                            // We have to map the removed objects separately as these might be required for cascading actions
                            objectMapping = new IdentityHashMap<>(map.size() * 2);
                            if (keyMapper != null) {
                                for (Object e : ((RecordingMap<?, ?, ?>) map).getRemovedKeys()) {
                                    objectMapping.put(e, keyMapper.map(e));
                                }
                            }
                            if (valueMapper != null) {
                                for (Object e : ((RecordingMap<?, ?, ?>) map).getRemovedElements()) {
                                    objectMapping.put(e, valueMapper.map(e));
                                }
                            }
                        }
                    } else {
                        backingMap = newMap;
                    }
                } else {
                    newMap = backingMap = (Map<Object, Object>) mapInstantiator.createCollection(map.size());
                }

                if (keyMapper != null && valueMapper != null) {
                    if (objectMapping == null) {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            backingMap.put(keyMapper.map(entry.getKey()), valueMapper.map(entry.getValue()));
                        }
                    } else {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            Object newKey = keyMapper.map(entry.getKey());
                            Object newValue = valueMapper.map(entry.getValue());
                            objectMapping.put(entry.getKey(), newKey);
                            objectMapping.put(entry.getValue(), newValue);
                            backingMap.put(newKey, newValue);
                        }
                    }
                } else if (keyMapper != null) {
                    if (objectMapping == null) {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            backingMap.put(keyMapper.map(entry.getKey()), entry.getValue());
                        }
                    } else {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            Object newKey = keyMapper.map(entry.getKey());
                            objectMapping.put(entry.getKey(), newKey);
                            backingMap.put(newKey, entry.getValue());
                        }
                    }
                } else if (valueMapper != null) {
                    if (objectMapping == null) {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            backingMap.put(entry.getKey(), valueMapper.map(entry.getValue()));
                        }
                    } else {
                        for (Map.Entry<Object, Object> entry : map.entrySet()) {
                            Object newValue = valueMapper.map(entry.getValue());
                            objectMapping.put(entry.getValue(), newValue);
                            backingMap.put(entry.getKey(), newValue);
                        }
                    }
                } else {
                    backingMap.putAll(map);
                }

                if (recording && copyDirtyState && map instanceof RecordingMap<?, ?, ?>) {
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
    private static class CollectionMappingAccessor extends ReadOnlyAccessor {
        private final AttributeAccessor accessor;
        private final boolean recording;
        private final boolean copyDirtyState;
        private final CollectionInstantiator collectionInstantiator;
        private final ViewMapper<Object, Object> valueMapper;

        public CollectionMappingAccessor(AttributeAccessor accessor, boolean recording, boolean copyDirtyState, CollectionInstantiator collectionInstantiator, ViewMapper<Object, Object> valueMapper) {
            this.accessor = accessor;
            this.recording = recording;
            this.copyDirtyState = copyDirtyState;
            this.collectionInstantiator = collectionInstantiator;
            this.valueMapper = valueMapper;
        }

        @Override
        public Object getValue(Object object) {
            Collection<Object> collection = (Collection<Object>) accessor.getValue(object);
            Collection<Object> newCollection = null;
            Collection<Object> backingCollection;
            if (collection != null) {
                Map<Object, Object> objectMapping = null;
                if (recording) {
                    RecordingCollection<?, ?> coll = collectionInstantiator.createRecordingCollection(collection.size());
                    newCollection = (Collection<Object>) coll;
                    if (copyDirtyState) {
                        backingCollection = (Collection<Object>) coll.getDelegate();
                        if (collection instanceof RecordingCollection<?, ?> && valueMapper != null) {
                            objectMapping = new IdentityHashMap<>(collection.size());
                            for (Object e : ((RecordingCollection<?, ?>) collection).getRemovedElements()) {
                                objectMapping.put(e, valueMapper.map(e));
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
                            backingCollection.add(valueMapper.map(o));
                        }
                    } else {
                        for (Object o : collection) {
                            Object newObject = valueMapper.map(o);
                            objectMapping.put(o, newObject);
                            backingCollection.add(newObject);
                        }
                    }
                } else {
                    backingCollection.addAll(collection);
                }

                if (recording && copyDirtyState && collection instanceof RecordingCollection<?, ?>) {
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
    private static class AttributeMappingAccessor extends ReadOnlyAccessor {
        private final AttributeAccessor accessor;
        private final ViewMapper<Object, Object> mapper;

        public AttributeMappingAccessor(AttributeAccessor accessor, ViewMapper<Object, Object> mapper) {
            this.accessor = accessor;
            this.mapper = mapper;
        }

        @Override
        public Object getValue(Object object) {
            Object value = accessor.getValue(object);
            if (value != null) {
                return mapper.map(value);
            }
            return null;
        }
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private abstract static class ReadOnlyAccessor implements AttributeAccessor {
        @Override
        public void setValue(Object object, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getOrCreateValue(Object object) {
            throw new UnsupportedOperationException();
        }

    }
}
