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

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.MapChangeModel;
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.type.TypedValue;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractMapChangeModel<K, V> extends AbstractPluralChangeModel<Map<K, V>, V, MapDirtyChecker<Map<K, V>, K, V>> implements MapChangeModel<K, V> {

    private final ManagedViewTypeImplementor<K> keyType;
    private final BasicTypeImpl<K> keyBasicType;
    private final boolean isElementEntityType;

    public AbstractMapChangeModel(ManagedViewTypeImplementor<K> keyType, BasicTypeImpl<K> keyBasicType, ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType, Map<K, V> initial, Map<K, V> current, MapDirtyChecker<Map<K, V>, K, V> pluralDirtyChecker) {
        super(type, basicType, initial, current, pluralDirtyChecker);
        this.keyType = keyType;
        this.keyBasicType = keyBasicType;
        this.isElementEntityType = basicType != null && basicType.isJpaEntity();
    }

    @Override
    public List<SingularChangeModel<V>> getElementChanges() {
        if (current == null || current.isEmpty()) {
            return getRemovedElements();
        }
        if (initial == null || initial.isEmpty()) {
            return getAddedElements();
        }
        boolean mutable = basicType == null || basicType.getUserType().isMutable();
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<V> addedElements = ((RecordingMap<?, ?, V>) current).getAddedElements();
            Set<V> removedElements = ((RecordingMap<?, ?, V>) current).getRemovedElements();
            List<SingularChangeModel<V>> elementChanges = new ArrayList<>(addedElements.size() + removedElements.size());

            if (mutable) {
                for (V value : current.values()) {
                    if (!addedElements.contains(value) && !removedElements.contains(value)) {
                        addElementChangeModelIfDirty(elementChanges, value, value);
                    }
                }
            }
            addAddedElementChangeModels(elementChanges, addedElements);
            addRemovedElementChangeModels(elementChanges, removedElements);
            return elementChanges;
        } else {
            List<SingularChangeModel<V>> elementChanges = new ArrayList<>(current.size());

            if (basicType != null) {
                BasicUserType<V> userType = basicType.getUserType();
                if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                    // Collect removed elements
                    for (Map.Entry<K, V> entry : initial.entrySet()) {
                        if (!current.containsKey(entry.getKey())) {
                            addElementChangeModel(elementChanges, entry.getValue(), null);
                        }
                    }
                    addModifiedElementChangeModels(elementChanges, current.entrySet());
                    return elementChanges;
                }
                TypedValue<V> typedValue = new TypedValue<>(userType);

                if (keyBasicType != null) {
                    TypedValue<K> typedKey = new TypedValue<>(keyBasicType.getUserType());
                    // Collect added and modified elements
                    for (Map.Entry<K, V> entry : current.entrySet()) {
                        K key = entry.getKey();
                        V value = entry.getValue();
                        if (value != null) {
                            typedKey.setValue(key);
                            typedValue.setValue(value);
                            V oldValue = initial.get(typedKey);
                            if (!typedValue.equals(oldValue)) {
                                addElementChangeModel(elementChanges, oldValue, value);
                            } else {
                                addElementChangeModelIfDirty(elementChanges, oldValue, value);
                            }
                        }
                    }
                    // Collect removed elements
                    for (Map.Entry<K, V> entry : initial.entrySet()) {
                        K key = entry.getKey();
                        V value = entry.getValue();
                        typedKey.setValue(key);

                        if (value == null) {
                            V newValue = current.get(typedKey);
                            if (newValue != null) {
                                addElementChangeModel(elementChanges, newValue, null);
                            }
                        } else {
                            V newValue = current.get(typedKey);
                            typedValue.setValue(value);
                            if (!typedValue.equals(newValue)) {
                                addElementChangeModel(elementChanges, value, null);
                            }
                        }
                    }
                } else {
                    // Collect added and modified elements
                    for (Map.Entry<K, V> entry : current.entrySet()) {
                        K key = entry.getKey();
                        V value = entry.getValue();
                        if (value != null) {
                            V oldValue = initial.get(key);
                            typedValue.setValue(value);
                            if (oldValue == null || !typedValue.equals(oldValue)) {
                                addElementChangeModel(elementChanges, oldValue, value);
                            } else {
                                addElementChangeModelIfDirty(elementChanges, oldValue, value);
                            }
                        }
                    }
                    // Collect removed elements
                    for (Map.Entry<K, V> entry : initial.entrySet()) {
                        K key = entry.getKey();
                        V value = entry.getValue();

                        if (value == null) {
                            V newValue = current.get(key);
                            if (newValue != null) {
                                addElementChangeModel(elementChanges, newValue, null);
                            }
                        } else {
                            V newValue = current.get(key);
                            typedValue.setValue(value);
                            if (!typedValue.equals(newValue)) {
                                addElementChangeModel(elementChanges, value, null);
                            }
                        }
                    }
                }
            } else {
                // Collect added and modified elements
                for (Map.Entry<K, V> entry : current.entrySet()) {
                    K key = entry.getKey();
                    V value = entry.getValue();

                    if (value != null) {
                        V oldValue = initial.get(key);
                        if (!value.equals(oldValue)) {
                            addElementChangeModel(elementChanges, oldValue, value);
                        } else {
                            addElementChangeModelIfDirty(elementChanges, oldValue, value);
                        }
                    }
                }
                // Collect removed elements
                for (Map.Entry<K, V> entry : initial.entrySet()) {
                    K key = entry.getKey();
                    V value = entry.getValue();

                    if (value == null) {
                        V newValue = current.get(key);
                        if (newValue != null) {
                            addElementChangeModel(elementChanges, newValue, null);
                        }
                    } else {
                        V newValue = current.get(key);
                        if (!value.equals(newValue)) {
                            addElementChangeModel(elementChanges, value, null);
                        }
                    }
                }
            }

            return elementChanges;
        }
    }

    @Override
    public List<SingularChangeModel<V>> getAddedElements() {
        if (current == null || current.isEmpty()) {
            return Collections.emptyList();
        }
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<V> addedElements = ((RecordingMap<?, ?, V>) current).getAddedElements();
            List<SingularChangeModel<V>> addedElementModels = new ArrayList<>(addedElements.size());
            addAddedElementChangeModels(addedElementModels, addedElements);
            return addedElementModels;
        } else if (initial != current) {
            if (initial == null) {
                List<SingularChangeModel<V>> addedElementModels = new ArrayList<>(current.size());
                addAddedElementChangeModels(addedElementModels, current.values());
                return addedElementModels;
            } else {
                List<SingularChangeModel<V>> addedElementModels = new ArrayList<>();
                if (basicType != null) {
                    BasicUserType<V> userType = basicType.getUserType();
                    if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                        for (Map.Entry<K, V> entry : current.entrySet()) {
                            if (!initial.containsKey(entry.getKey())) {
                                addElementChangeModel(addedElementModels, null, entry.getValue());
                            }
                        }
                        return addedElementModels;
                    }
                    TypedValue<V> typedValue = new TypedValue<>(userType);

                    if (keyBasicType != null) {
                        TypedValue<K> typedKey = new TypedValue<>(keyBasicType.getUserType());
                        // Collect added and modified elements
                        for (Map.Entry<K, V> entry : current.entrySet()) {
                            K key = entry.getKey();
                            V value = entry.getValue();
                            if (value != null) {
                                typedValue.setValue(value);
                                typedKey.setValue(key);
                                V oldValue = initial.get(typedKey);
                                if (oldValue == null || !typedValue.equals(oldValue)) {
                                    addElementChangeModel(addedElementModels, oldValue, value);
                                }
                            }
                        }
                    } else {
                        // Collect added and modified elements
                        for (Map.Entry<K, V> entry : current.entrySet()) {
                            K key = entry.getKey();
                            V value = entry.getValue();
                            if (value != null) {
                                typedValue.setValue(value);
                                V oldValue = initial.get(key);
                                if (oldValue == null || !typedValue.equals(oldValue)) {
                                    addElementChangeModel(addedElementModels, oldValue, value);
                                }
                            }
                        }
                    }
                } else {
                    for (Map.Entry<K, V> entry : current.entrySet()) {
                        K key = entry.getKey();
                        V value = entry.getValue();

                        if (value != null) {
                            V oldValue = initial.get(key);
                            if (!value.equals(oldValue)) {
                                addElementChangeModel(addedElementModels, oldValue, value);
                            }
                        }
                    }
                }

                return addedElementModels;
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<SingularChangeModel<V>> getRemovedElements() {
        if (current == null || current.isEmpty()) {
            if (initial == null) {
                return Collections.emptyList();
            } else if (initial != current) {
                List<SingularChangeModel<V>> removedElementModels = new ArrayList<>(initial.size());
                addRemovedElementChangeModels(removedElementModels, initial.values());
                return removedElementModels;
            }
        }
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<V> removedElements = ((RecordingMap<?, ?, V>) current).getRemovedElements();
            List<SingularChangeModel<V>> removedElementModels = new ArrayList<>(removedElements.size());
            addRemovedElementChangeModels(removedElementModels, removedElements);
            return removedElementModels;
        } else if (initial != current) {
            if (initial == null) {
                return Collections.emptyList();
            } else {
                List<SingularChangeModel<V>> removedElementModels = new ArrayList<>();
                if (basicType != null) {
                    BasicUserType<V> userType = basicType.getUserType();
                    if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                        for (Map.Entry<K, V> entry : initial.entrySet()) {
                            if (!current.containsKey(entry.getKey())) {
                                addElementChangeModel(removedElementModels, entry.getValue(), null);
                            }
                        }
                        return removedElementModels;
                    }
                    TypedValue<V> typedValue = new TypedValue<>(userType);

                    if (keyBasicType != null) {
                        TypedValue<K> typedKey = new TypedValue<>(keyBasicType.getUserType());
                        // Collect removed elements
                        for (Map.Entry<K, V> entry : initial.entrySet()) {
                            K key = entry.getKey();
                            V value = entry.getValue();
                            typedKey.setValue(key);

                            if (value == null) {
                                V newValue = current.get(typedKey);
                                if (newValue != null) {
                                    addElementChangeModel(removedElementModels, value, newValue);
                                }
                            } else {
                                V newValue = current.get(typedKey);
                                typedValue.setValue(value);
                                if (!typedValue.equals(newValue)) {
                                    addElementChangeModel(removedElementModels, value, newValue);
                                }
                            }
                        }
                    } else {
                        // Collect removed elements
                        for (Map.Entry<K, V> entry : initial.entrySet()) {
                            K key = entry.getKey();
                            V value = entry.getValue();

                            if (value == null) {
                                V newValue = current.get(key);
                                if (newValue != null) {
                                    addElementChangeModel(removedElementModels, value, newValue);
                                }
                            } else {
                                V newValue = current.get(key);
                                typedValue.setValue(value);
                                if (!typedValue.equals(newValue)) {
                                    addElementChangeModel(removedElementModels, value, newValue);
                                }
                            }
                        }
                    }
                } else {
                    for (Map.Entry<K, V> entry : initial.entrySet()) {
                        K key = entry.getKey();
                        V value = entry.getValue();

                        if (value != null) {
                            V newValue = current.get(key);
                            if (!value.equals(newValue)) {
                                addElementChangeModel(removedElementModels, value, newValue);
                            }
                        }
                    }
                }

                return removedElementModels;
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<SingularChangeModel<V>> getMutatedElements() {
        if (current == null || current.isEmpty()) {
            return Collections.emptyList();
        }
        if (basicType != null && !basicType.getUserType().isMutable()) {
            return Collections.emptyList();
        }

        List<SingularChangeModel<V>> elementModels = new ArrayList<>();
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<V> removedElements = ((RecordingMap<?, ?, V>) current).getRemovedElements();
            Set<V> addedElements = ((RecordingMap<?, ?, V>) current).getAddedElements();
            for (V o : current.values()) {
                if (!removedElements.contains(o) && !addedElements.contains(o)) {
                    addElementChangeModelIfDirty(elementModels, o, o);
                }
            }
        } else {
            if (initial == null || initial.isEmpty()) {
                return Collections.emptyList();
            }
            Map<V, V> valueMap = new HashMap<>(initial.size());
            for (V v : initial.values()) {
                valueMap.put(v, v);
            }
            if (basicType != null) {
                TypedValue<V> typedValue = new TypedValue<>(basicType.getUserType());

                if (isElementEntityType) {
                    for (V value : current.values()) {
                        typedValue.setValue(value);
                        if (valueMap.containsKey(typedValue)) {
                            addElementChangeModelIfDirty(elementModels, valueMap.get(typedValue), value);
                        }
                    }
                } else {
                    // Since value types have no identity, elements for a key are also considered mutated when the entry existed before
                    if (keyBasicType != null) {
                        TypedValue<K> typedKey = new TypedValue<>(keyBasicType.getUserType());

                        for (Map.Entry<K, V> entry : current.entrySet()) {
                            K key = entry.getKey();
                            V value = entry.getValue();
                            if (value != null) {
                                typedKey.setValue(key);
                                typedValue.setValue(value);
                                if (valueMap.containsKey(typedValue)) {
                                    addElementChangeModelIfDirty(elementModels, valueMap.get(typedValue), value);
                                } else if (initial.containsKey(typedKey)) {
                                    addElementChangeModelIfDirty(elementModels, initial.get(typedKey), value);
                                }
                            }
                        }
                    } else {
                        for (Map.Entry<K, V> entry : current.entrySet()) {
                            K key = entry.getKey();
                            V value = entry.getValue();
                            if (value != null) {
                                typedValue.setValue(value);
                                if (valueMap.containsKey(typedValue)) {
                                    addElementChangeModelIfDirty(elementModels, valueMap.get(typedValue), value);
                                } else if (initial.containsKey(key)) {
                                    addElementChangeModelIfDirty(elementModels, initial.get(key), value);
                                }
                            }
                        }
                    }
                }
            } else {
                for (V o : current.values()) {
                    if (valueMap.containsKey(o)) {
                        addElementChangeModelIfDirty(elementModels, valueMap.get(o), o);
                    }
                }
            }
        }

        return elementModels;
    }

    @Override
    public List<SingularChangeModel<K>> getKeyChanges() {
        if (current == null || current.isEmpty()) {
            return getRemovedKeys();
        }
        if (initial == null || initial.isEmpty()) {
            return getAddedKeys();
        }
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<K> addedKeys = ((RecordingMap<?, K, ?>) current).getAddedKeys();
            Set<K> removedKeys = ((RecordingMap<?, K, ?>) current).getRemovedKeys();
            List<SingularChangeModel<K>> keyChanges = new ArrayList<>(addedKeys.size() + removedKeys.size());

            if (keyBasicType != null) {
                BasicUserType<K> userType = keyBasicType.getUserType();
                if (userType.isMutable()) {
                    if (!userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                        for (K o : initial.keySet()) {
                            if (!addedKeys.contains(o)) {
                                addKeyChangeModel(keyChanges, o, null);
                            }
                        }
                        addAddedKeyChangeModels(keyChanges, current.keySet());
                        return keyChanges;
                    }
                    TypedValue<K> value = new TypedValue<>(userType);
                    for (K o : current.keySet()) {
                        value.setValue(o);
                        if (!addedKeys.contains(value) && !removedKeys.contains(value)) {
                            addKeyChangeModel(keyChanges, o, o);
                        }
                    }
                }
            } else {
                for (K o : current.keySet()) {
                    if (!addedKeys.contains(o) && !removedKeys.contains(o)) {
                        addKeyChangeModel(keyChanges, o, o);
                    }
                }
            }

            addAddedKeyChangeModels(keyChanges, addedKeys);
            addRemovedKeyChangeModels(keyChanges, removedKeys);
            return keyChanges;
        } else {
            List<SingularChangeModel<K>> keyChanges = new ArrayList<>(current.size());

            if (keyBasicType != null) {
                BasicUserType<K> userType = keyBasicType.getUserType();
                if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                    addAddedKeyChangeModels(keyChanges, current.keySet());
                    addRemovedKeyChangeModels(keyChanges, initial.keySet());
                    return keyChanges;
                }
                TypedValue<K> value = new TypedValue<>(userType);
                // Collect added and modified elements
                for (K o : current.keySet()) {
                    value.setValue(o);
                    if (!initial.containsKey(value)) {
                        addKeyChangeModel(keyChanges, null, o);
                    }
                }
                // Collect removed elements
                for (K o : initial.keySet()) {
                    value.setValue(o);
                    if (!current.containsKey(value)) {
                        addKeyChangeModel(keyChanges, o, null);
                    }
                }
            } else {
                // Collect added and modified keys
                for (K key : current.keySet()) {
                    if (!initial.containsKey(key)) {
                        addKeyChangeModel(keyChanges, null, key);
                    } else {
                        addKeyChangeModelIfDirty(keyChanges, key, key);
                    }
                }
                // Collect removed keys
                for (K key : initial.keySet()) {
                    if (!current.containsKey(key)) {
                        addKeyChangeModel(keyChanges, key, null);
                    }
                }
            }

            return keyChanges;
        }
    }

    @Override
    public List<SingularChangeModel<K>> getAddedKeys() {
        if (current == null || current.isEmpty()) {
            return Collections.emptyList();
        }
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<K> addedKeys = ((RecordingMap<?, K, ?>) current).getAddedKeys();
            List<SingularChangeModel<K>> addedKeyModels = new ArrayList<>(addedKeys.size());
            if (keyBasicType != null) {
                BasicUserType<K> userType = keyBasicType.getUserType();
                if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                    Set<K> removedKeys = ((RecordingMap<?, K, ?>) current).getRemovedKeys();
                    for (K o : current.keySet()) {
                        if (!removedKeys.contains(o)) {
                            addKeyChangeModel(addedKeyModels, null, o);
                        }
                    }
                    return addedKeyModels;
                }
            }
            addAddedKeyChangeModels(addedKeyModels, addedKeys);
            return addedKeyModels;
        } else if (initial != current) {
            if (initial == null) {
                List<SingularChangeModel<K>> addedKeyModels = new ArrayList<>(current.size());
                addAddedKeyChangeModels(addedKeyModels, current.keySet());
                return addedKeyModels;
            } else {
                List<SingularChangeModel<K>> addedKeyModels = new ArrayList<>();
                if (keyBasicType != null) {
                    BasicUserType<K> userType = keyBasicType.getUserType();
                    if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                        addAddedKeyChangeModels(addedKeyModels, current.keySet());
                        return addedKeyModels;
                    }
                    TypedValue<K> value = new TypedValue<>(userType);
                    for (K o : current.keySet()) {
                        value.setValue(o);
                        if (!initial.containsKey(value)) {
                            addKeyChangeModel(addedKeyModels, null, o);
                        }
                    }
                } else {
                    for (K key : current.keySet()) {
                        if (!initial.containsKey(key)) {
                            addKeyChangeModel(addedKeyModels, null, key);
                        }
                    }
                }

                return addedKeyModels;
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<SingularChangeModel<K>> getRemovedKeys() {
        if (current == null || current.isEmpty()) {
            if (initial == null) {
                return Collections.emptyList();
            } else if (initial != current) {
                List<SingularChangeModel<K>> removedKeyModels = new ArrayList<>(initial.size());
                addRemovedKeyChangeModels(removedKeyModels, initial.keySet());
                return removedKeyModels;
            }
        }
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<K> removedKeys = ((RecordingMap<?, K, ?>) current).getRemovedKeys();
            List<SingularChangeModel<K>> removedKeyModels = new ArrayList<>(removedKeys.size());
            if (keyBasicType != null) {
                BasicUserType<K> userType = keyBasicType.getUserType();
                if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                    Set<K> addedKeys = ((RecordingMap<?, K, ?>) current).getAddedKeys();
                    for (K o : initial.keySet()) {
                        if (!addedKeys.contains(o)) {
                            addKeyChangeModel(removedKeyModels, o, null);
                        }
                    }
                    return removedKeyModels;
                }
            }
            addRemovedKeyChangeModels(removedKeyModels, removedKeys);
            return removedKeyModels;
        } else if (initial != current) {
            if (initial == null) {
                return Collections.emptyList();
            } else {
                List<SingularChangeModel<K>> removedKeyModels = new ArrayList<>();
                if (keyBasicType != null) {
                    BasicUserType<K> userType = keyBasicType.getUserType();
                    if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                        addRemovedKeyChangeModels(removedKeyModels, initial.keySet());
                        return removedKeyModels;
                    }
                    TypedValue<K> value = new TypedValue<>(userType);
                    for (K o : initial.keySet()) {
                        value.setValue(o);
                        if (!current.containsKey(value)) {
                            addKeyChangeModel(removedKeyModels, o, null);
                        }
                    }
                } else {
                    for (K key : initial.keySet()) {
                        if (!current.containsKey(key)) {
                            addKeyChangeModel(removedKeyModels, key, null);
                        }
                    }
                }

                return removedKeyModels;
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<SingularChangeModel<K>> getMutatedKeys() {
        if (current == null || current.isEmpty()) {
            return Collections.emptyList();
        }
        if (keyBasicType != null && (keyBasicType.getUserType().isMutable() || keyBasicType.getUserType().isMutable() && !keyBasicType.getUserType().supportsDirtyChecking())) {
            return Collections.emptyList();
        }

        List<SingularChangeModel<K>> keyModels = new ArrayList<>();
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<K> removedKeys = ((RecordingMap<?, K, ?>) current).getRemovedKeys();
            Set<K> addedKeys = ((RecordingMap<?, K, ?>) current).getAddedKeys();
            if (keyBasicType != null) {
                TypedValue<K> value = new TypedValue<>(keyBasicType.getUserType());
                for (K o : current.keySet()) {
                    value.setValue(o);
                    if (!removedKeys.contains(value) && !addedKeys.contains(value)) {
                        addKeyChangeModelIfDirty(keyModels, o, o);
                    }
                }
            } else {
                for (K o : current.keySet()) {
                    if (!removedKeys.contains(o) && !addedKeys.contains(o)) {
                        addKeyChangeModelIfDirty(keyModels, o, o);
                    }
                }
            }
        } else {
            if (initial == null || initial.isEmpty()) {
                return Collections.emptyList();
            }
            if (keyBasicType != null) {
                TypedValue<K> value = new TypedValue<>(keyBasicType.getUserType());
                for (K o : current.keySet()) {
                    value.setValue(o);
                    if (initial.containsKey(value)) {
                        addKeyChangeModelIfDirty(keyModels, o, o);
                    }
                }
            } else {
                for (K o : current.keySet()) {
                    if (initial.containsKey(o)) {
                        addKeyChangeModelIfDirty(keyModels, o, o);
                    }
                }
            }
        }

        return keyModels;
    }

    @Override
    public List<SingularChangeModel<?>> getObjectChanges() {
        if (current == null || current.isEmpty()) {
            return getRemovedObjects();
        }
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<K> addedKeys = ((RecordingMap<?, K, ?>) current).getAddedKeys();
            Set<V> addedElements = ((RecordingMap<?, ?, V>) current).getAddedElements();
            Set<K> removedKeys = ((RecordingMap<?, K, ?>) current).getRemovedKeys();
            Set<V> removedElements = ((RecordingMap<?, ?, V>) current).getRemovedElements();
            List<SingularChangeModel<?>> objectChanges = new ArrayList<>(addedKeys.size() + addedElements.size() + removedKeys.size() + removedElements.size());
            List<SingularChangeModel<K>> keyModels = (List<SingularChangeModel<K>>) (List<?>) objectChanges;
            List<SingularChangeModel<V>> valueModels = (List<SingularChangeModel<V>>) (List<?>) objectChanges;
            boolean keyMutable = keyBasicType == null || keyBasicType.getUserType().isMutable();
            boolean valueMutable = basicType == null || basicType.getUserType().isMutable();

            for (Map.Entry<K, V> entry : current.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();

                if (keyMutable && !addedKeys.contains(key) && !removedKeys.contains(key)) {
                    addKeyChangeModel(keyModels, key, key);
                }

                if (valueMutable && !addedElements.contains(value) && !removedElements.contains(value)) {
                    addElementChangeModel(valueModels, value, value);
                }
            }
            addAddedKeyChangeModels(keyModels, addedKeys);
            addRemovedKeyChangeModels(keyModels, removedKeys);
            addAddedElementChangeModels(valueModels, addedElements);
            addRemovedElementChangeModels(valueModels, removedElements);
            return objectChanges;
        } else {
            List<SingularChangeModel<?>> objectChanges = new ArrayList<>(current.size());
            List<SingularChangeModel<K>> keyModels = (List<SingularChangeModel<K>>) (List<?>) objectChanges;
            List<SingularChangeModel<V>> valueModels = (List<SingularChangeModel<V>>) (List<?>) objectChanges;
            boolean keyMutable = keyBasicType == null || keyBasicType.getUserType().isMutable();
            boolean valueMutable = basicType == null || basicType.getUserType().isMutable();

            // Collect added and modified elements
            for (Map.Entry<K, V> entry : current.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();

                if (!initial.containsKey(key)) {
                    addKeyChangeModel(keyModels, null, key);
                } else if (keyMutable) {
                    addKeyChangeModelIfDirty(keyModels, key, key);
                }

                if (value != null) {
                    V oldValue = initial.get(key);
                    if (!value.equals(oldValue)) {
                        addElementChangeModel(valueModels, oldValue, value);
                    } else if (valueMutable) {
                        addElementChangeModelIfDirty(valueModels, oldValue, value);
                    }
                }
            }
            // Collect removed elements
            for (Map.Entry<K, V> entry : initial.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();

                if (!current.containsKey(key)) {
                    addKeyChangeModel(keyModels, key, null);
                }

                if (value == null) {
                    V newValue = current.get(key);
                    if (newValue != null) {
                        addElementChangeModel(valueModels, value, newValue);
                    }
                } else {
                    V newValue = current.get(key);
                    if (!value.equals(newValue)) {
                        addElementChangeModel(valueModels, value, newValue);
                    }
                }
            }

            return objectChanges;
        }
    }

    @Override
    public List<SingularChangeModel<?>> getAddedObjects() {
        if (current == null || current.isEmpty()) {
            return Collections.emptyList();
        }
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<K> addedKeys = ((RecordingMap<?, K, ?>) current).getAddedKeys();
            Set<V> addedElements = ((RecordingMap<?, ?, V>) current).getAddedElements();
            List<SingularChangeModel<?>> objectChanges = new ArrayList<>(addedKeys.size() + addedElements.size());
            List<SingularChangeModel<K>> keyModels = (List<SingularChangeModel<K>>) (List<?>) objectChanges;
            List<SingularChangeModel<V>> valueModels = (List<SingularChangeModel<V>>) (List<?>) objectChanges;
            addAddedKeyChangeModels(keyModels, addedKeys);
            addAddedElementChangeModels(valueModels, addedElements);
            return objectChanges;
        } else if (initial != current) {
            List<SingularChangeModel<?>> objectChanges = new ArrayList<>(current.size());
            List<SingularChangeModel<K>> keyModels = (List<SingularChangeModel<K>>) (List<?>) objectChanges;
            List<SingularChangeModel<V>> valueModels = (List<SingularChangeModel<V>>) (List<?>) objectChanges;
            if (initial == null) {
                for (Map.Entry<K, V> entry : current.entrySet()) {
                    addKeyChangeModel(keyModels, null, entry.getKey());
                    addElementChangeModel(valueModels, null, entry.getValue());
                }
            } else {
                // Collect added elements
                for (Map.Entry<K, V> entry : current.entrySet()) {
                    K key = entry.getKey();
                    V value = entry.getValue();

                    if (!initial.containsKey(key)) {
                        addKeyChangeModel(keyModels, null, key);
                    }

                    if (value != null) {
                        V oldValue = initial.get(key);
                        if (!value.equals(oldValue)) {
                            addElementChangeModel(valueModels, oldValue, value);
                        }
                    }
                }
            }

            return objectChanges;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<SingularChangeModel<?>> getRemovedObjects() {
        if (current == null || current.isEmpty()) {
            if (initial == null) {
                return Collections.emptyList();
            } else if (initial != current) {
                List<SingularChangeModel<?>> removedModels = new ArrayList<>(initial.size());
                List<SingularChangeModel<K>> keyModels = (List<SingularChangeModel<K>>) (List<?>) removedModels;
                List<SingularChangeModel<V>> valueModels = (List<SingularChangeModel<V>>) (List<?>) removedModels;
                for (Map.Entry<K, V> entry : initial.entrySet()) {
                    addKeyChangeModel(keyModels, entry.getKey(), null);
                    addElementChangeModel(valueModels, entry.getValue(), null);
                }
                return removedModels;
            }
        }
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<K> removedKeys = ((RecordingMap<?, K, ?>) current).getRemovedKeys();
            Set<V> removedElements = ((RecordingMap<?, ?, V>) current).getRemovedElements();
            List<SingularChangeModel<?>> objectChanges = new ArrayList<>(removedKeys.size() + removedElements.size());
            List<SingularChangeModel<K>> keyModels = (List<SingularChangeModel<K>>) (List<?>) objectChanges;
            List<SingularChangeModel<V>> valueModels = (List<SingularChangeModel<V>>) (List<?>) objectChanges;
            addRemovedKeyChangeModels(keyModels, removedKeys);
            addRemovedElementChangeModels(valueModels, removedElements);
            return objectChanges;
        } else if (initial != current) {
            if (initial == null) {
                return Collections.emptyList();
            } else {
                List<SingularChangeModel<?>> objectChanges = new ArrayList<>(current.size());
                List<SingularChangeModel<K>> keyModels = (List<SingularChangeModel<K>>) (List<?>) objectChanges;
                List<SingularChangeModel<V>> valueModels = (List<SingularChangeModel<V>>) (List<?>) objectChanges;
                // Collect added elements
                for (Map.Entry<K, V> entry : initial.entrySet()) {
                    K key = entry.getKey();
                    V value = entry.getValue();

                    if (!current.containsKey(key)) {
                        addKeyChangeModel(keyModels, key, null);
                    }

                    if (value != null) {
                        V newValue = current.get(key);
                        if (!value.equals(newValue)) {
                            addElementChangeModel(valueModels, value, newValue);
                        }
                    }
                }

                return objectChanges;
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SingularChangeModel<?>> getMutatedObjects() {
        if (current == null || current.isEmpty()) {
            return Collections.emptyList();
        }

        List<SingularChangeModel<?>> models = new ArrayList<>();
        List<SingularChangeModel<K>> keyModels = (List<SingularChangeModel<K>>) (List<?>) models;
        List<SingularChangeModel<V>> valueModels = (List<SingularChangeModel<V>>) (List<?>) models;
        if (initial == current && current instanceof RecordingMap<?, ?, ?>) {
            Set<K> removedKeys = ((RecordingMap<?, K, ?>) current).getRemovedKeys();
            Set<K> addedKeys = ((RecordingMap<?, K, ?>) current).getAddedKeys();
            Set<V> removedElements = ((RecordingMap<?, ?, V>) current).getRemovedElements();
            Set<V> addedElements = ((RecordingMap<?, ?, V>) current).getAddedElements();
            for (Map.Entry<K, V> entry : current.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                if (!removedKeys.contains(key) && !addedKeys.contains(key)) {
                    addKeyChangeModelIfDirty(keyModels, key, key);
                }
                if (!removedElements.contains(value) && !addedElements.contains(value)) {
                    addElementChangeModelIfDirty(valueModels, value, value);
                }
            }
        } else {
            if (initial == null || initial.isEmpty()) {
                return Collections.emptyList();
            }
            Map<V, V> valueMap = new HashMap<>(initial.size());
            for (V v : initial.values()) {
                valueMap.put(v, v);
            }
            if (basicType != null) {
                TypedValue<V> typedValue = new TypedValue<>(basicType.getUserType());
                for (Map.Entry<K, V> entry : current.entrySet()) {
                    K key = entry.getKey();
                    V value = entry.getValue();
                    if (initial.containsKey(key)) {
                        addKeyChangeModelIfDirty(keyModels, key, key);
                    }
                    if (value != null) {
                        typedValue.setValue(value);
                        if (valueMap.containsKey(typedValue)) {
                            addElementChangeModelIfDirty(valueModels, valueMap.get(typedValue), value);
                        } else if (initial.containsKey(key)) {
                            addElementChangeModelIfDirty(valueModels, initial.get(key), value);
                        }
                    }
                }
            }
        }

        return models;
    }

    private void addAddedElementChangeModels(List<SingularChangeModel<V>> elementModels, Collection<V> elements) {
        for (V o : elements) {
            addElementChangeModel(elementModels, null, o);
        }
    }

    private void addRemovedElementChangeModels(List<SingularChangeModel<V>> elementModels, Collection<V> elements) {
        for (V o : elements) {
            addElementChangeModel(elementModels, o, null);
        }
    }

    private void addModifiedElementChangeModels(List<SingularChangeModel<V>> elementModels, Set<Map.Entry<K, V>> entries) {
        for (Map.Entry<K, V> entry : entries) {
            addElementChangeModel(elementModels, initial.get(entry.getKey()), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void addElementChangeModel(List<SingularChangeModel<V>> elementModels, V initial, V current) {
        DirtyChecker<V> dirtyChecker = pluralDirtyChecker.getElementDirtyChecker(current != null ? current : initial);
        AbstractChangeModel<V, V> elementChangeModel = getObjectChangeModel(type == null ? basicType : type, initial, current, dirtyChecker);
        elementModels.add((SingularChangeModel<V>) elementChangeModel);
    }

    @SuppressWarnings("unchecked")
    private void addElementChangeModelIfDirty(List<SingularChangeModel<V>> elementModels, V initial, V current) {
        DirtyChecker<V> dirtyChecker = pluralDirtyChecker.getElementDirtyChecker(current != null ? current : initial);
        AbstractChangeModel<V, V> elementChangeModel = getObjectChangeModel(type == null ? basicType : type, initial, current, dirtyChecker);
        if (elementChangeModel.isDirty()) {
            elementModels.add((SingularChangeModel<V>) elementChangeModel);
        }
    }

    private void addAddedKeyChangeModels(List<SingularChangeModel<K>> elementModels, Collection<K> elements) {
        for (K o : elements) {
            addKeyChangeModel(elementModels, null, o);
        }
    }

    private void addRemovedKeyChangeModels(List<SingularChangeModel<K>> elementModels, Collection<K> elements) {
        for (K o : elements) {
            addKeyChangeModel(elementModels, o, null);
        }
    }

    @SuppressWarnings("unchecked")
    private void addKeyChangeModel(List<SingularChangeModel<K>> elementModels, K initial, K o) {
        DirtyChecker<K> dirtyChecker = pluralDirtyChecker.getKeyDirtyChecker(o);
        AbstractChangeModel<K, K> elementChangeModel = getObjectChangeModel(keyType == null ? keyBasicType : keyType, initial, o, dirtyChecker);
        elementModels.add((SingularChangeModel<K>) elementChangeModel);
    }

    @SuppressWarnings("unchecked")
    private void addKeyChangeModelIfDirty(List<SingularChangeModel<K>> elementModels, K initial, K o) {
        DirtyChecker<K> dirtyChecker = pluralDirtyChecker.getKeyDirtyChecker(o);
        AbstractChangeModel<K, K> elementChangeModel = getObjectChangeModel(keyType == null ? keyBasicType : keyType, initial, o, dirtyChecker);
        if (elementChangeModel.isDirty()) {
            elementModels.add((SingularChangeModel<K>) elementChangeModel);
        }
    }

    @Override
    public boolean isDirty(String attributePath) {
        if (type == null) {
            throw new IllegalArgumentException("Invalid dereference of the collection element basic type " + basicType + " of the path: " + attributePath);
        }
        if (current == null) {
            // An attribute of a null object is never dirty
            return false;
        }

        if (current instanceof RecordingMap<?, ?, ?> && !((RecordingMap<?, ?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return false;
        }

        for (V o : current.values()) {
            DirtyChecker<V> dirtyChecker = pluralDirtyChecker.getElementDirtyChecker(o);
            if (isDirty(type, o, o, dirtyChecker, attributePath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isChanged(String attributePath) {
        if (type == null) {
            throw new IllegalArgumentException("Invalid dereference of the collection element basic type " + basicType + " of the path: " + attributePath);
        }
        if (current == null) {
            // An attribute of a null object is never dirty
            return false;
        }

        if (current instanceof RecordingMap<?, ?, ?> && !((RecordingMap<?, ?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return false;
        }

        for (V o : current.values()) {
            DirtyChecker<V> dirtyChecker = pluralDirtyChecker.getElementDirtyChecker(o);
            if (isChanged(type, o, o, dirtyChecker, attributePath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isKeyDirty(String attributePath) {
        if (keyType == null) {
            throw new IllegalArgumentException("Invalid dereference of the collection key basic type " + keyBasicType + " of the path: " + attributePath);
        }
        if (current == null) {
            // An attribute of a null object is never dirty
            return false;
        }

        if (current instanceof RecordingMap<?, ?, ?> && !((RecordingMap<?, ?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return false;
        }

        for (K o : current.keySet()) {
            DirtyChecker<K> keyDirtyChecker = pluralDirtyChecker.getKeyDirtyChecker(o);
            if (isDirty(keyType, o,  o, keyDirtyChecker, attributePath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isKeyChanged(String attributePath) {
        if (keyType == null) {
            throw new IllegalArgumentException("Invalid dereference of the collection key basic type " + keyBasicType + " of the path: " + attributePath);
        }
        if (current == null) {
            // An attribute of a null object is never dirty
            return false;
        }

        if (current instanceof RecordingMap<?, ?, ?> && !((RecordingMap<?, ?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return false;
        }

        for (K o : current.keySet()) {
            DirtyChecker<K> keyDirtyChecker = pluralDirtyChecker.getKeyDirtyChecker(o);
            if (isChanged(keyType, o, o, keyDirtyChecker, attributePath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <X> List<SingularChangeModel<X>> keyGet(SingularAttribute<K, X> attribute) {
        return (List<SingularChangeModel<X>>) (List<?>) keyGetAll(getMutableKeyAttribute(attribute));
    }

    @Override
    public <E, C extends Collection<E>> List<PluralChangeModel<C, E>> keyGet(PluralAttribute<K, C, E> attribute) {
        return (List<PluralChangeModel<C, E>>) (List<?>) keyGetAll(getMutableKeyAttribute(attribute));
    }

    @Override
    public <K1, V1> List<MapChangeModel<K1, V1>> keyGet(MapAttribute<K, K1, V1> attribute) {
        return (List<MapChangeModel<K1, V1>>) (List<?>) this.<Map<K1, V1>>keyGetAll(getMutableKeyAttribute(attribute));
    }

    protected final AbstractMethodAttribute<?, ?> getMutableKeyAttribute(Attribute<?, ?> attribute) {
        AbstractMethodAttribute<?, ?> methodAttribute;
        if (!(attribute instanceof MethodAttribute<?, ?>) || (methodAttribute = (AbstractMethodAttribute<?, ?>) attribute).getDirtyStateIndex() == -1) {
            throw new IllegalArgumentException("Invalid attribute that is not mutable: " + attribute);
        }
        if (attribute.getDeclaringType() != keyType) {
            throw new IllegalArgumentException("Invalid attribute that is not declared by the expected type: " + keyType + " but declared by: " + attribute.getDeclaringType());
        }
        return methodAttribute;
    }

    @Override
    public <X> List<? extends ChangeModel<X>> get(String attributePath) {
        if (type == null) {
            throw new IllegalArgumentException("Invalid dereference of the collection element basic type " + basicType);
        }
        if (current == null) {
            // An attribute of a null object is never dirty
            return Collections.emptyList();
        }

        if (current instanceof RecordingMap<?, ?, ?> && !((RecordingMap<?, ?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return Collections.emptyList();
        }

        List<ChangeModel<Object>> models = new ArrayList<>(current.size());
        for (V o : current.values()) {
            DirtyChecker<V> dirtyChecker =  pluralDirtyChecker.getElementDirtyChecker(o);
            models.addAll(getAll(type, o, dirtyChecker, attributePath));
        }

        return (List<? extends ChangeModel<X>>) (List<?>) models;
    }

    @Override
    public <X> List<? extends ChangeModel<X>> keyGet(String attributePath) {
        if (keyType == null) {
            throw new IllegalArgumentException("Invalid dereference of the collection key basic type " + keyBasicType);
        }
        if (current == null) {
            // An attribute of a null object is never dirty
            return Collections.emptyList();
        }

        if (current instanceof RecordingMap<?, ?, ?> && !((RecordingMap<?, ?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return Collections.emptyList();
        }

        List<ChangeModel<Object>> models = new ArrayList<>(current.size());
        for (K o : current.keySet()) {
            DirtyChecker<K> keyDirtyChecker = pluralDirtyChecker.getKeyDirtyChecker(o);
            models.addAll(getAll(keyType, o, keyDirtyChecker, attributePath));
        }

        return (List<? extends ChangeModel<X>>) (List<?>) models;
    }

    @Override
    protected <X> List<? extends ChangeModel<X>> getAll(AbstractMethodAttribute<?, ?> methodAttribute) {
        if (type == null) {
            throw new IllegalArgumentException("Invalid dereference of the collection element basic type " + basicType);
        }
        if (current == null) {
            // An attribute of a null object is never dirty
            return Collections.emptyList();
        }

        if (current instanceof RecordingMap<?, ?, ?> && !((RecordingMap<?, ?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return Collections.emptyList();
        }
        if (current instanceof RecordingMap<?, ?, ?>) {
            RecordingMap<?, ?, ?> recordingMap = (RecordingMap<?, ?, ?>) (RecordingMap<?, ?, ?>) current;

            List<ChangeModel<Object>> models = new ArrayList<>(this.current.size());
            Set<?> addedElements = recordingMap.getAddedElements();
            for (V o : this.current.values()) {
                DirtyChecker<V> dirtyChecker =  pluralDirtyChecker.getElementDirtyChecker(o);
                AbstractChangeModel<V, V> elementChangeModel;
                if (addedElements.contains(o)) {
                    elementChangeModel = getObjectChangeModel(type, null, o, dirtyChecker);
                } else {
                    elementChangeModel = getObjectChangeModel(type, o, o, dirtyChecker);
                }

                models.add(elementChangeModel.get(methodAttribute));
            }
            return (List<? extends ChangeModel<X>>) (List<?>) models;
        } else {
            List<ChangeModel<Object>> models = new ArrayList<>(current.size());
            Set<?> initialElements = initial == null ? Collections.emptySet() : new HashSet<>(initial.values());
            for (V o : current.values()) {
                DirtyChecker<V> dirtyChecker = pluralDirtyChecker.getElementDirtyChecker(o);
                AbstractChangeModel<V, V> elementChangeModel;
                if (initialElements.contains(o)) {
                    elementChangeModel = getObjectChangeModel(type, o, o, dirtyChecker);
                } else {
                    elementChangeModel = getObjectChangeModel(type, null, o, dirtyChecker);
                }
                models.add(elementChangeModel.get(methodAttribute));
            }
            return (List<? extends ChangeModel<X>>) (List<?>) models;
        }
    }

    protected final <X> List<? extends ChangeModel<X>> keyGetAll(AbstractMethodAttribute<?, ?> methodAttribute) {
        if (keyType == null) {
            throw new IllegalArgumentException("Invalid dereference of the collection key basic type " + keyBasicType);
        }
        if (current == null) {
            // An attribute of a null object is never dirty
            return Collections.emptyList();
        }

        if (current instanceof RecordingMap<?, ?, ?> && !((RecordingMap<?, ?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return Collections.emptyList();
        }

        if (current instanceof RecordingMap<?, ?, ?>) {
            RecordingMap<?, ?, ?> recordingMap = (RecordingMap<?, ?, ?>) (RecordingMap<?, ?, ?>) current;

            List<ChangeModel<Object>> models = new ArrayList<>(this.current.size());
            Set<?> addedKeys = recordingMap.getAddedKeys();
            for (K o : this.current.keySet()) {
                DirtyChecker<K> keyDirtyChecker = pluralDirtyChecker.getKeyDirtyChecker(o);
                AbstractChangeModel<K, K> elementChangeModel;
                if (addedKeys.contains(o)) {
                    elementChangeModel = getObjectChangeModel(keyType, null, o, keyDirtyChecker);
                } else {
                    elementChangeModel = getObjectChangeModel(keyType, o, o, keyDirtyChecker);
                }

                models.add(elementChangeModel.get(methodAttribute));
            }
            return (List<? extends ChangeModel<X>>) (List<?>) models;
        } else {
            List<ChangeModel<Object>> models = new ArrayList<>(current.size());
            Set<?> initialKeys = initial == null ? Collections.emptySet() : initial.keySet();
            for (K o : current.keySet()) {
                DirtyChecker<K> keyDirtyChecker = pluralDirtyChecker.getKeyDirtyChecker(o);
                AbstractChangeModel<K, K> elementChangeModel;
                if (initialKeys.contains(o)) {
                    elementChangeModel = getObjectChangeModel(keyType, o, o, keyDirtyChecker);
                } else {
                    elementChangeModel = getObjectChangeModel(keyType, null, o, keyDirtyChecker);
                }
                models.add(elementChangeModel.get(methodAttribute));
            }
            return (List<? extends ChangeModel<X>>) (List<?>) models;
        }
    }
}
