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
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SetAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractChangeModel<C, E> implements ChangeModel<C> {

    protected final ManagedViewTypeImplementor<E> type;
    protected final BasicTypeImpl<E> basicType;

    public AbstractChangeModel(ManagedViewTypeImplementor<E> type, BasicTypeImpl<E> basicType) {
        this.type = type;
        this.basicType = basicType;
    }

    protected abstract <X> ChangeModel<X> get(AbstractMethodAttribute<?, ?> methodAttribute);

    protected final AbstractMethodAttribute<?, ?> getMutableAttribute(Attribute<?, ?> attribute) {
        AbstractMethodAttribute<?, ?> methodAttribute;
        if (!(attribute instanceof MethodAttribute<?, ?>) || (methodAttribute = (AbstractMethodAttribute<?, ?>) attribute).getDirtyStateIndex() == -1) {
            throw new IllegalArgumentException("Invalid attribute that is not mutable: " + attribute);
        }
        if (attribute.getDeclaringType() != type) {
            throw new IllegalArgumentException("Invalid attribute that is not declared by the expected type: " + type + " but declared by: " + attribute.getDeclaringType());
        }
        return methodAttribute;
    }

    protected final void validateAttributePath(ManagedViewType<?> elementType, String attributePath) {
        if (elementType == null) {
            throw new IllegalArgumentException("Invalid dereference of a basic type by attribute path: " + attributePath);
        }
        String[] parts = attributePath.split("\\.");

        for (int i = 0; i < parts.length; i++) {
            getAttribute(elementType, attributePath, parts[i]);
        }
    }

    @SuppressWarnings("unchecked")
    protected final AbstractMethodAttribute<?, ?> getAttribute(Type<?> type, String attributePath, String attributeName) {
        if (type instanceof ManagedViewType<?>) {
            Attribute<?, ?> attribute = ((ManagedViewType) type).getAttribute(attributeName);
            AbstractMethodAttribute<?, ?> methodAttribute;
            if (!(attribute instanceof MethodAttribute<?, ?>) || (methodAttribute = (AbstractMethodAttribute<?, ?>) attribute).getDirtyStateIndex() == -1) {
                throw new IllegalArgumentException("Invalid dereference non-mutable attribute " + attributeName + " of the path: " + attributePath);
            }
            return methodAttribute;
        } else {
            throw new IllegalArgumentException("Invalid dereference of the basic type " + type + " by attribute " + attributeName + " of the path: " + attributePath);
        }
    }

    protected final ManagedViewType<?> getType(AbstractMethodAttribute<?, ?> attribute) {
        if (attribute instanceof SingularAttribute<?, ?>) {
            return (ManagedViewType<?>) ((SingularAttribute<?, ?>) attribute).getType();
        } else {
            return (ManagedViewType<?>) ((PluralAttribute<?, ?, ?>) attribute).getElementType();
        }
    }

    @SuppressWarnings("unchecked")
    protected final boolean isDirty(ManagedViewType<?> elementType, DirtyStateTrackable initial, DirtyStateTrackable current, DirtyChecker<? extends DirtyStateTrackable> dirtyChecker, String attributePath) {
        if (current == null || initial == current && !current.$$_isDirty()) {
            // An attribute of a null object is never dirty
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return false;
        }

        ManagedViewType<?> currentType = elementType;
        DirtyStateTrackable initialObject;
        DirtyStateTrackable currentObject = current;
        DirtyChecker<DirtyStateTrackable> currentChecker = (DirtyChecker<DirtyStateTrackable>) dirtyChecker;
        String[] parts = attributePath.split("\\.");
        int end = parts.length - 1;
        for (int i = 0; i < end; i++) {
            AbstractMethodAttribute<?, ?> attribute = getAttribute(currentType, attributePath, parts[i]);
            currentType = getType(attribute);
            initialObject = (DirtyStateTrackable) currentObject.$$_getInitialState()[attribute.getDirtyStateIndex()];
            currentObject = (DirtyStateTrackable) currentObject.$$_getMutableState()[attribute.getDirtyStateIndex()];

            // The target attribute can't be dirty if the object is null
            if (currentObject == null) {
                return true;
            }
            // If the source object hasn't changed and isn't dirty, the target attribute can't be dirty
            if (initialObject == currentObject && !currentObject.$$_isDirty()) {
                return false;
            }

            currentChecker = currentChecker.<DirtyStateTrackable>getNestedCheckers(currentObject)[attribute.getDirtyStateIndex()];
        }

        AbstractMethodAttribute<?, ?> attribute = getAttribute(currentType, attributePath, parts[end]);
        DirtyChecker<Object> lastChecker = currentChecker.getNestedCheckers(currentObject)[attribute.getDirtyStateIndex()];
        Object lastInitialObject = currentObject.$$_getInitialState()[attribute.getDirtyStateIndex()];
        Object lastCurrentObject = currentObject.$$_getMutableState()[attribute.getDirtyStateIndex()];
        return lastChecker.getDirtyKind(lastInitialObject, lastCurrentObject) != DirtyChecker.DirtyKind.NONE;
    }

    @SuppressWarnings("unchecked")
    protected final boolean isChanged(ManagedViewType<?> elementType, DirtyStateTrackable initial, DirtyStateTrackable current, DirtyChecker<? extends DirtyStateTrackable> dirtyChecker, String attributePath) {
        if (current == null || initial == current && !current.$$_isDirty()) {
            // An attribute of a null object is never dirty
            // Also if the dirty tracker reports that the object isn't dirty, no need for further checks
            return false;
        }

        ManagedViewType<?> currentType = elementType;
        DirtyStateTrackable initialObject;
        DirtyStateTrackable currentObject = current;
        DirtyChecker<DirtyStateTrackable> currentChecker = (DirtyChecker<DirtyStateTrackable>) dirtyChecker;
        String[] parts = attributePath.split("\\.");
        int end = parts.length - 1;
        for (int i = 0; i < end; i++) {
            AbstractMethodAttribute<?, ?> attribute = getAttribute(currentType, attributePath, parts[i]);
            currentType = getType(attribute);
            initialObject = (DirtyStateTrackable) currentObject.$$_getInitialState()[attribute.getDirtyStateIndex()];
            currentObject = (DirtyStateTrackable) currentObject.$$_getMutableState()[attribute.getDirtyStateIndex()];

            // If a source object was changed, we consider it changed
            if (initialObject == null) {
                return currentObject != null;
            }
            if (currentObject == null) {
                return true;
            }
            // If the objects are the same and isn't considered dirty, it's considered not changed
            if (initialObject == currentObject && !currentObject.$$_isDirty()) {
                return false;
            }

            currentChecker = currentChecker.<DirtyStateTrackable>getNestedCheckers(currentObject)[attribute.getDirtyStateIndex()];
        }

        AbstractMethodAttribute<?, ?> attribute = getAttribute(currentType, attributePath, parts[end]);
        DirtyChecker<Object> lastChecker = currentChecker.getNestedCheckers(currentObject)[attribute.getDirtyStateIndex()];
        Object lastInitialObject = currentObject.$$_getInitialState()[attribute.getDirtyStateIndex()];
        Object lastCurrentObject = currentObject.$$_getMutableState()[attribute.getDirtyStateIndex()];
        return lastChecker.getDirtyKind(lastInitialObject, lastCurrentObject) != DirtyChecker.DirtyKind.NONE;
    }

    protected final <X> ChangeModel<X> getEmptyChangeModel(ManagedViewType<?> currentType, String attributePath, String[] parts, int index) {
        int end = parts.length - 1;
        while (index++ < end) {
            AbstractMethodAttribute<?, ?> attribute = getAttribute(currentType, attributePath, parts[index]);
            currentType = getType(attribute);
        }

        AbstractMethodAttribute<?, ?> methodAttribute = getAttribute(currentType, attributePath, parts[end]);
        return getEmptyChangeModel(methodAttribute);
    }

    @SuppressWarnings("unchecked")
    protected final <X> ChangeModel<X> getEmptyChangeModel(AbstractMethodAttribute<?, ?> methodAttribute) {
        if (methodAttribute instanceof SingularAttribute<?, ?>) {
            Type<?> type = ((SingularAttribute<?, ?>) methodAttribute).getType();
            if (type instanceof ManagedViewType<?>) {
                return new EmptySingularChangeModel<>((ManagedViewTypeImplementor<X>) type, null);
            } else {
                return new EmptySingularChangeModel<>(null, (BasicTypeImpl<X>) type);
            }
        } else if (methodAttribute instanceof MapAttribute<?, ?, ?>) {
            MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) methodAttribute;
            ManagedViewTypeImplementor<Object> elementType = null;
            ManagedViewTypeImplementor<Object> keyType = null;
            BasicTypeImpl<Object> elementBasicType = null;
            BasicTypeImpl<Object> keyBasicType = null;
            if (mapAttribute.getElementType() instanceof ManagedViewType<?>) {
                elementType = (ManagedViewTypeImplementor<Object>) mapAttribute.getElementType();
            } else {
                elementBasicType = (BasicTypeImpl<Object>) mapAttribute.getElementType();
            }
            if (mapAttribute.getKeyType() instanceof ManagedViewType<?>) {
                keyType = (ManagedViewTypeImplementor<Object>) mapAttribute.getKeyType();
            } else {
                keyBasicType = (BasicTypeImpl<Object>) mapAttribute.getKeyType();
            }
            return (ChangeModel<X>) new EmptyMapChangeModel<>(elementType, elementBasicType, keyType, keyBasicType);
        } else {
            PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) methodAttribute;
            ManagedViewTypeImplementor<Object> elementType = null;
            BasicTypeImpl<Object> elementBasicType = null;
            if (pluralAttribute.getElementType() instanceof ManagedViewType<?>) {
                elementType = (ManagedViewTypeImplementor<Object>) pluralAttribute.getElementType();
            } else {
                elementBasicType = (BasicTypeImpl<Object>) pluralAttribute.getElementType();
            }
            if (pluralAttribute instanceof SetAttribute<?, ?>) {
                return (ChangeModel<X>) new EmptySetChangeModel<>(elementType, elementBasicType);
            } else if (pluralAttribute instanceof ListAttribute<?, ?>) {
                return (ChangeModel<X>) new EmptyListChangeModel<>(elementType, elementBasicType);
            } else {
                return (ChangeModel<X>) new EmptyCollectionChangeModel<>(elementType, elementBasicType);
            }
        }
    }

    protected final <X> ChangeModel<X> get(ManagedViewType<?> elementType, DirtyStateTrackable object, DirtyChecker<? extends DirtyStateTrackable> dirtyChecker, String attributePath) {
        ManagedViewType<?> currentType = elementType;
        DirtyStateTrackable currentObject = object;
        DirtyChecker<DirtyStateTrackable> currentChecker = (DirtyChecker<DirtyStateTrackable>) dirtyChecker;
        String[] parts = attributePath.split("\\.");
        if (object == null) {
            return getEmptyChangeModel(currentType, attributePath, parts, 0);
        }
        int end = parts.length - 1;
        for (int i = 0; i < end; i++) {
            AbstractMethodAttribute<?, ?> attribute = getAttribute(currentType, attributePath, parts[i]);
            currentType = getType(attribute);
            Object o = currentObject.$$_getMutableState()[attribute.getDirtyStateIndex()];

            if (o == null) {
                return getEmptyChangeModel(currentType, attributePath, parts, i + 1);
            } else if (!(o instanceof DirtyStateTrackable)) {
                throw new IllegalArgumentException("Illegal plural attribute dereferencing in the attribute path " + attributePath + " for SingularAttribute#get(String). Use SingularAttribute#getAll(String) instead!");
            }

            currentObject = (DirtyStateTrackable) o;
            currentChecker = currentChecker.<DirtyStateTrackable>getNestedCheckers(currentObject)[attribute.getDirtyStateIndex()];
        }

        AbstractMethodAttribute<?, ?> lastAttribute = getAttribute(currentType, attributePath, parts[end]);
        return getChangeModel(currentObject, lastAttribute, currentChecker);
    }

    @SuppressWarnings("unchecked")
    protected final <X> List<? extends ChangeModel<X>> getAll(ManagedViewType<?> elementType, DirtyStateTrackable object, DirtyChecker<? extends DirtyStateTrackable> dirtyChecker, String attributePath) {
        ManagedViewType<?> currentType = elementType;
        DirtyStateTrackable currentObject = object;
        DirtyChecker<DirtyStateTrackable> currentChecker = (DirtyChecker<DirtyStateTrackable>) dirtyChecker;
        String[] parts = attributePath.split("\\.");
        if (object == null) {
            return (List<? extends ChangeModel<X>>) (List<?>) Collections.singletonList(getEmptyChangeModel(currentType, attributePath, parts, 0));
        }
        int end = parts.length - 1;
        for (int i = 0; i < end; i++) {
            AbstractMethodAttribute<?, ?> attribute = getAttribute(currentType, attributePath, parts[i]);
            currentType = getType(attribute);
            Object o = currentObject.$$_getMutableState()[attribute.getDirtyStateIndex()];
            currentChecker = currentChecker.<DirtyStateTrackable>getNestedCheckers(currentObject)[attribute.getDirtyStateIndex()];

            if (o == null) {
                return (List<? extends ChangeModel<X>>) (List<?>) Collections.singletonList(getEmptyChangeModel(currentType, attributePath, parts, i + 1));
            } else if (!(o instanceof DirtyStateTrackable)) {
                return getChangeModelList(currentType, o, currentChecker, parts, i + 1, attributePath);
            }

            currentObject = (DirtyStateTrackable) o;
        }

        AbstractMethodAttribute<?, ?> lastAttribute = getAttribute(currentType, attributePath, parts[end]);
        return (List<? extends ChangeModel<X>>) (List<?>) Collections.singletonList(getChangeModel(currentObject, lastAttribute, currentChecker));
    }

    @SuppressWarnings("unchecked")
    protected final <X> List<? extends ChangeModel<X>> getChangeModelList(ManagedViewType<?> currentType, Object o, DirtyChecker<?> currentChecker, String[] parts, int start, String attributePath) {
        PluralDirtyChecker<Object, DirtyStateTrackable> pluralDirtyChecker = (PluralDirtyChecker<Object, DirtyStateTrackable>) currentChecker;
        String subPath = join('.', parts, start, attributePath.length());
        List<ChangeModel<Object>> models;
        if (o instanceof Collection<?>) {
            Collection<DirtyStateTrackable> collection = (Collection<DirtyStateTrackable>) o;
            models = new ArrayList<>(collection.size());
            for (DirtyStateTrackable element : collection) {
                models.addAll(getAll(currentType, element, pluralDirtyChecker.getElementDirtyChecker(element), subPath));
            }
        } else {
            Map<?, DirtyStateTrackable> map = (Map<?, DirtyStateTrackable>) o;
            models = new ArrayList<>(map.size());
            for (DirtyStateTrackable element : map.values()) {
                models.addAll(getAll(currentType, element, pluralDirtyChecker.getElementDirtyChecker(element), subPath));
            }
        }
        return (List<? extends ChangeModel<X>>) (List<?>) models;
    }

    private String join(char delimiter, String[] parts, int start, int size) {
        StringBuilder sb = new StringBuilder(size);
        int end = parts.length - 1;
        for (int i = start; i < end; i++) {
            sb.append(parts[i]).append(delimiter);
        }
        sb.append(parts[end]);
        return sb.toString();
    }

    protected final List<ChangeModel<?>> getDirtyChanges(ManagedViewTypeImplementor<?> elementType, DirtyStateTrackable object, DirtyChecker<? extends DirtyStateTrackable> dirtyChecker) {
        if (object == null || !object.$$_isDirty()) {
            return Collections.emptyList();
        }

        long dirty = object.$$_getSimpleDirty();
        Object[] initialState = object.$$_getInitialState();
        Object[] dirtyState = object.$$_getMutableState();
        @SuppressWarnings("unchecked")
        DirtyChecker<Object>[] nestedCheckers = ((DirtyChecker<DirtyStateTrackable>) dirtyChecker).getNestedCheckers(object);
        List<ChangeModel<?>> list = new ArrayList<>();

        for (int i = 0; i < dirtyState.length; i++) {
            long mask = 1L << i;
            if ((dirty & mask) != 0) {
                if (nestedCheckers[i].getDirtyKind(initialState[i], dirtyState[i]) != DirtyChecker.DirtyKind.NONE) {
                    list.add(getChangeModel(initialState[i], dirtyState[i], elementType.getMutableAttribute(i), nestedCheckers[i]));
                }
            }
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    protected final <X> ChangeModel<X> getChangeModel(DirtyStateTrackable object, AbstractMethodAttribute<?, ?> methodAttribute, DirtyChecker<? extends DirtyStateTrackable> dirtyChecker) {
        DirtyChecker<Object> attributeDirtyChecker = ((DirtyChecker<DirtyStateTrackable>) dirtyChecker).getNestedCheckers(object)[methodAttribute.getDirtyStateIndex()];
        Object attributeObject = object.$$_getMutableState()[methodAttribute.getDirtyStateIndex()];
        Object initialAttributeObject = object.$$_getInitialState()[methodAttribute.getDirtyStateIndex()];
        return getChangeModel(initialAttributeObject, attributeObject, methodAttribute, attributeDirtyChecker);
    }

    @SuppressWarnings("unchecked")
    protected <X> ChangeModel<X> getChangeModel(Object initialAttributeObject, Object attributeObject, AbstractMethodAttribute<?, ?> methodAttribute, DirtyChecker<Object> attributeDirtyChecker) {
        if (methodAttribute instanceof SingularAttribute<?, ?>) {
            Type<?> type = ((SingularAttribute<?, ?>) methodAttribute).getType();
            if (type instanceof ManagedViewType<?>) {
                return new ViewSingularChangeModel((ManagedViewTypeImplementor<X>) type, (DirtyStateTrackable) initialAttributeObject, (DirtyStateTrackable) attributeObject, attributeDirtyChecker);
            } else {
                return (ChangeModel<X>) new BasicSingularChangeModel<>((BasicTypeImpl<Object>) type, initialAttributeObject, attributeObject, attributeDirtyChecker);
            }
        } else if (methodAttribute instanceof MapAttribute<?, ?, ?>) {
            MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) methodAttribute;
            MapDirtyChecker<Map<Object, Object>, Object, Object> mapDirtyChecker = (MapDirtyChecker<Map<Object, Object>, Object, Object>) (DirtyChecker<?>) attributeDirtyChecker;
            ManagedViewTypeImplementor<Object> elementType = null;
            ManagedViewTypeImplementor<Object> keyType = null;
            BasicTypeImpl<Object> elementBasicType = null;
            BasicTypeImpl<Object> keyBasicType = null;
            if (mapAttribute.getElementType() instanceof ManagedViewType<?>) {
                elementType = (ManagedViewTypeImplementor<Object>) mapAttribute.getElementType();
            } else {
                elementBasicType = (BasicTypeImpl<Object>) mapAttribute.getElementType();
            }
            if (mapAttribute.getKeyType() instanceof ManagedViewType<?>) {
                keyType = (ManagedViewTypeImplementor<Object>) mapAttribute.getKeyType();
            } else {
                keyBasicType = (BasicTypeImpl<Object>) mapAttribute.getKeyType();
            }
            return (ChangeModel<X>) new MapChangeModelImpl<>(keyType, keyBasicType, elementType, elementBasicType, (Map<Object, Object>) initialAttributeObject, (Map<Object, Object>) attributeObject, mapDirtyChecker);
        } else {
            PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) methodAttribute;
            PluralDirtyChecker<? extends Collection<Object>, Object> pluralDirtyChecker = (PluralDirtyChecker<Collection<Object>, Object>) (DirtyChecker<?>) attributeDirtyChecker;
            ManagedViewTypeImplementor<Object> elementType = null;
            BasicTypeImpl<Object> elementBasicType = null;
            if (pluralAttribute.getElementType() instanceof ManagedViewType<?>) {
                elementType = (ManagedViewTypeImplementor<Object>) pluralAttribute.getElementType();
            } else {
                elementBasicType = (BasicTypeImpl<Object>) pluralAttribute.getElementType();
            }
            if (pluralAttribute instanceof SetAttribute<?, ?>) {
                return (ChangeModel<X>) new SetChangeModelImpl<>(elementType, elementBasicType, (Set<Object>) initialAttributeObject, (Set<Object>) attributeObject, (PluralDirtyChecker<Set<Object>, Object>) pluralDirtyChecker);
            } else if (pluralAttribute instanceof ListAttribute<?, ?>) {
                return (ChangeModel<X>) new ListChangeModelImpl<>(elementType, elementBasicType, (List<Object>) initialAttributeObject, (List<Object>) attributeObject, (PluralDirtyChecker<List<Object>, Object>) pluralDirtyChecker);
            } else {
                return (ChangeModel<X>) new CollectionChangeModelImpl<>(elementType, elementBasicType, (Collection<Object>) initialAttributeObject, (Collection<Object>) attributeObject, (PluralDirtyChecker<Collection<Object>, Object>) pluralDirtyChecker);
            }
        }
    }

}
