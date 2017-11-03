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

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.type.TypedValue;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCollectionChangeModel<C extends Collection<V>, V> extends AbstractPluralChangeModel<C, V, PluralDirtyChecker<C, V>> {

    public AbstractCollectionChangeModel(ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType, C initial, C current, PluralDirtyChecker<C, V> pluralDirtyChecker) {
        super(type, basicType, initial, current, pluralDirtyChecker);
    }

    @Override
    public List<SingularChangeModel<V>> getElementChanges() {
        if (current == null || current.isEmpty()) {
            return getRemovedElements();
        }
        if (initial == current && current instanceof RecordingCollection<?, ?>) {
            Set<V> addedElements = ((RecordingCollection<?, V>) current).getAddedElements();
            Set<V> removedElements = ((RecordingCollection<?, V>) current).getRemovedElements();
            List<SingularChangeModel<V>> elementChanges = new ArrayList<>(addedElements.size() + removedElements.size());

            if (basicType != null) {
                BasicUserType<V> userType = basicType.getUserType();
                if (userType.isMutable()) {
                    if (!userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                        for (V o : initial) {
                            if (!addedElements.contains(o)) {
                                addElementChangeModel(elementChanges, o);
                            }
                        }
                        addElementChangeModels(elementChanges, current);
                        return elementChanges;
                    }
                    TypedValue<V> value = new TypedValue<>(userType);
                    for (V o : current) {
                        value.setValue(o);
                        if (!addedElements.contains(value) && !removedElements.contains(value)) {
                            addElementChangeModel(elementChanges, o);
                        }
                    }
                }
            } else {
                for (V o : current) {
                    if (!addedElements.contains(o) && !removedElements.contains(o)) {
                        addElementChangeModelIfDirty(elementChanges, o);
                    }
                }
            }
            addElementChangeModels(elementChanges, addedElements);
            addElementChangeModels(elementChanges, removedElements);
            return elementChanges;
        } else {
            List<SingularChangeModel<V>> elementChanges = new ArrayList<>(current.size());

            if (basicType != null) {
                BasicUserType<V> userType = basicType.getUserType();
                if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                    addElementChangeModels(elementChanges, current);
                    addElementChangeModels(elementChanges, initial);
                    return elementChanges;
                }
                TypedValue<V> value = new TypedValue<>(userType);
                // Collect added and modified elements
                for (V o : current) {
                    value.setValue(o);
                    if (!initial.contains(value)) {
                        addElementChangeModel(elementChanges, o);
                    }
                }
                // Collect removed elements
                for (V o : initial) {
                    value.setValue(o);
                    if (!current.contains(value)) {
                        addElementChangeModel(elementChanges, o);
                    }
                }
            } else {
                // Collect added and modified elements
                for (V o : current) {
                    if (!initial.contains(o)) {
                        addElementChangeModel(elementChanges, o);
                    } else {
                        addElementChangeModelIfDirty(elementChanges, o);
                    }
                }
                // Collect removed elements
                for (V o : initial) {
                    if (!current.contains(o)) {
                        addElementChangeModel(elementChanges, o);
                    }
                }
            }

            return elementChanges;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SingularChangeModel<V>> getAddedElements() {
        if (current == null || current.isEmpty()) {
            return Collections.emptyList();
        }
        if (initial == current && current instanceof RecordingCollection<?, ?>) {
            Set<V> addedElements = ((RecordingCollection<?, V>) current).getAddedElements();
            List<SingularChangeModel<V>> addedElementModels = new ArrayList<>(addedElements.size());
            if (basicType != null) {
                BasicUserType<V> userType = basicType.getUserType();
                if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                    Set<V> removedElements = ((RecordingCollection<?, V>) current).getRemovedElements();
                    for (V o : current) {
                        if (!removedElements.contains(o)) {
                            addElementChangeModel(addedElementModels, o);
                        }
                    }
                    return addedElementModels;
                }
            }
            addElementChangeModels(addedElementModels, addedElements);
            return addedElementModels;
        } else if (initial != current) {
            if (initial == null) {
                List<SingularChangeModel<V>> addedElementModels = new ArrayList<>(current.size());
                addElementChangeModels(addedElementModels, current);
                return addedElementModels;
            } else {
                List<SingularChangeModel<V>> addedElementModels = new ArrayList<>();
                if (basicType != null) {
                    BasicUserType<V> userType = basicType.getUserType();
                    if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                        addElementChangeModels(addedElementModels, current);
                        return addedElementModels;
                    }
                    TypedValue<V> value = new TypedValue<>(userType);
                    for (V o : current) {
                        value.setValue(o);
                        if (!initial.contains(value)) {
                            addElementChangeModel(addedElementModels, o);
                        }
                    }
                } else {
                    for (V o : current) {
                        if (!initial.contains(o)) {
                            addElementChangeModel(addedElementModels, o);
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
            } else {
                List<SingularChangeModel<V>> removedElementModels = new ArrayList<>(initial.size());
                addElementChangeModels(removedElementModels, initial);
                return removedElementModels;
            }
        }
        if (initial == current && current instanceof RecordingCollection<?, ?>) {
            Set<V> removedElements = ((RecordingCollection<?, V>) current).getRemovedElements();
            List<SingularChangeModel<V>> removedElementModels = new ArrayList<>(removedElements.size());
            if (basicType != null) {
                BasicUserType<V> userType = basicType.getUserType();
                if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                    Set<V> addedElements = ((RecordingCollection<?, V>) current).getAddedElements();
                    for (V o : initial) {
                        if (!addedElements.contains(o)) {
                            addElementChangeModel(removedElementModels, o);
                        }
                    }
                    return removedElementModels;
                }
            }
            addElementChangeModels(removedElementModels, removedElements);
            return removedElementModels;
        } else if (initial != current) {
            if (initial == null) {
                return Collections.emptyList();
            } else {
                List<SingularChangeModel<V>> removedElementModels = new ArrayList<>();
                if (basicType != null) {
                    BasicUserType<V> userType = basicType.getUserType();
                    if (userType.isMutable() && !userType.supportsDirtyChecking() && !userType.supportsDeepCloning()) {
                        addElementChangeModels(removedElementModels, initial);
                        return removedElementModels;
                    }
                    TypedValue<V> value = new TypedValue<>(userType);
                    for (V o : initial) {
                        value.setValue(o);
                        if (!current.contains(value)) {
                            addElementChangeModel(removedElementModels, o);
                        }
                    }
                } else {
                    for (V o : initial) {
                        if (!current.contains(o)) {
                            addElementChangeModel(removedElementModels, o);
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
        if (basicType != null && (!basicType.getUserType().isMutable() || basicType.getUserType().isMutable() && !basicType.getUserType().supportsDirtyChecking())) {
            return Collections.emptyList();
        }
        
        List<SingularChangeModel<V>> elementModels = new ArrayList<>();
        if (initial == current && current instanceof RecordingCollection<?, ?>) {
            Set<?> removedElements = ((RecordingCollection<?, ?>) current).getRemovedElements();
            Set<?> addedElements = ((RecordingCollection<?, ?>) current).getAddedElements();
            for (V o : current) {
                if (!removedElements.contains(o) && !addedElements.contains(o)) {
                    addElementChangeModelIfDirty(elementModels, o);
                }
            }
        } else {
            if (initial == null || initial.isEmpty()) {
                return Collections.emptyList();
            }
            if (basicType != null) {
                TypedValue<V> value = new TypedValue<>(basicType.getUserType());
                for (V o : current) {
                    value.setValue(o);
                    if (initial.contains(value)) {
                        addElementChangeModelIfDirty(elementModels, o);
                    }
                }
            } else {
                for (V o : current) {
                    if (initial.contains(o)) {
                        addElementChangeModelIfDirty(elementModels, o);
                    }
                }
            }
        }

        return elementModels;
    }

    private void addElementChangeModels(List<SingularChangeModel<V>> elementModels, Collection<V> elements) {
        for (V o : elements) {
            addElementChangeModel(elementModels, o);
        }
    }

    @SuppressWarnings("unchecked")
    private void addElementChangeModel(List<SingularChangeModel<V>> elementModels, V o) {
        DirtyChecker<V> dirtyChecker = pluralDirtyChecker.getElementDirtyChecker(o);
        AbstractChangeModel<V, V> elementChangeModel = getObjectChangeModel(type == null ? basicType : type, o, dirtyChecker);
        elementModels.add((SingularChangeModel<V>) elementChangeModel);
    }

    @SuppressWarnings("unchecked")
    private void addElementChangeModelIfDirty(List<SingularChangeModel<V>> elementModels, V o) {
        DirtyChecker<V> dirtyChecker = pluralDirtyChecker.getElementDirtyChecker(o);
        AbstractChangeModel<V, V> elementChangeModel = getObjectChangeModel(type == null ? basicType : type, o, dirtyChecker);
        if (elementChangeModel.isDirty()) {
            elementModels.add((SingularChangeModel<V>) elementChangeModel);
        }
    }

    @Override
    public boolean isDirty(String attributePath) {
        if (current == null) {
            // An attribute of a null object is never dirty
            return false;
        }

        if (current instanceof RecordingCollection<?, ?> && !((RecordingCollection<?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return false;
        }

        for (V o : current) {
            if (!(o instanceof DirtyStateTrackable)) {
                throw new IllegalArgumentException("Invalid dereference of the collection element basic type " + type + " of the path: " + attributePath);
            }
            DirtyChecker<DirtyStateTrackable> dirtyChecker = (DirtyChecker<DirtyStateTrackable>) pluralDirtyChecker.getElementDirtyChecker(o);
            if (isDirty(type, (DirtyStateTrackable) o, (DirtyStateTrackable) o, dirtyChecker, attributePath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isChanged(String attributePath) {
        if (current == null) {
            // An attribute of a null object is never dirty
            return false;
        }

        if (current instanceof RecordingCollection<?, ?> && !((RecordingCollection<?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return false;
        }

        for (V o : current) {
            if (!(o instanceof DirtyStateTrackable)) {
                throw new IllegalArgumentException("Invalid dereference of the collection element basic type " + type + " of the path: " + attributePath);
            }
            DirtyChecker<DirtyStateTrackable> dirtyChecker = (DirtyChecker<DirtyStateTrackable>) pluralDirtyChecker.getElementDirtyChecker(o);
            if (isChanged(type, (DirtyStateTrackable) o, (DirtyStateTrackable) o, dirtyChecker, attributePath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <X> List<? extends ChangeModel<X>> get(String attributePath) {
        if (current == null) {
            // An attribute of a null object is never dirty
            return Collections.emptyList();
        }

        if (current instanceof RecordingCollection<?, ?> && !((RecordingCollection<?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return Collections.emptyList();
        }

        List<ChangeModel<Object>> models = new ArrayList<>(current.size());
        for (V o : current) {
            if (!(o instanceof DirtyStateTrackable)) {
                throw new IllegalArgumentException("Invalid dereference of the collection element basic type " + type + " of the path: " + attributePath);
            }
            DirtyChecker<DirtyStateTrackable> dirtyChecker = (DirtyChecker<DirtyStateTrackable>) pluralDirtyChecker.getElementDirtyChecker(o);
            models.addAll(getAll(type, (DirtyStateTrackable) o, dirtyChecker, attributePath));
        }

        return (List<? extends ChangeModel<X>>) (List<?>) models;
    }

    @Override
    protected <X> List<? extends ChangeModel<X>> getAll(AbstractMethodAttribute<?, ?> methodAttribute) {
        if (current == null) {
            // An attribute of a null object is never dirty
            return Collections.emptyList();
        }

        if (current instanceof RecordingCollection<?, ?> && !((RecordingCollection<?, ?>) current).$$_isDirty()) {
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return Collections.emptyList();
        }

        List<ChangeModel<Object>> models = new ArrayList<>(current.size());
        for (V o : current) {
            if (!(o instanceof DirtyStateTrackable)) {
                throw new IllegalArgumentException("Invalid dereference of the collection element basic type " + type);
            }
            DirtyChecker<DirtyStateTrackable> dirtyChecker = (DirtyChecker<DirtyStateTrackable>) pluralDirtyChecker.getElementDirtyChecker(o);
            AbstractChangeModel<DirtyStateTrackable, DirtyStateTrackable> elementChangeModel = getObjectChangeModel((Type<DirtyStateTrackable>) (type == null ? basicType : type), (DirtyStateTrackable) o, dirtyChecker);
            models.add(elementChangeModel.get(methodAttribute));
        }

        return (List<? extends ChangeModel<X>>) (List<?>) models;
    }
}
