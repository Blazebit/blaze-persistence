/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.view.EntityViewBuilderBase;
import com.blazebit.persistence.view.EntityViewNestedBuilder;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.metamodel.AbstractAttribute;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.AbstractParameterAttribute;
import com.blazebit.persistence.view.impl.metamodel.ConstrainedAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;
import com.blazebit.persistence.view.impl.proxy.AbstractReflectionInstantiator;
import com.blazebit.persistence.view.impl.proxy.ConstructorReflectionInstantiator;
import com.blazebit.persistence.view.impl.proxy.ObjectInstantiator;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.CollectionAttribute;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SetAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class EntityViewBuilderBaseImpl<T, X extends EntityViewBuilderBase<T, X>> implements EntityViewBuilderBase<T, X> {

    private final EntityViewManagerImpl evm;
    private final ManagedViewTypeImplementor<T> managedViewType;
    private final MappingConstructorImpl<T> mappingConstructor;
    private final Map<String, Object> optionalParameters;
    private final ObjectInstantiator<T> objectInstantiator;
    private final Object[] tuple;
    private final int parameterOffset;

    public EntityViewBuilderBaseImpl(EntityViewManagerImpl evm, ManagedViewTypeImplementor<T> managedViewType, MappingConstructorImpl<T> mappingConstructor, Map<ManagedViewType<? extends T>, String> inheritanceSubtypeMappings, Map<String, Object> optionalParameters) {
        this.evm = evm;
        this.managedViewType = managedViewType;
        this.optionalParameters = optionalParameters;

        if (mappingConstructor == null) {
            mappingConstructor = managedViewType.getDefaultConstructor();
            if (managedViewType.getConstructors().size() > 1) {
                if (mappingConstructor == null) {
                    throw new IllegalArgumentException("The given view type '" + managedViewType.getJavaType().getName() + "' has multiple constructors and the given constructor is null, but the view type has no default 'init' constructor!");
                }
            }
        }
        this.mappingConstructor = mappingConstructor;
        ManagedViewTypeImpl.InheritanceSubtypeConfiguration<T> inheritanceSubtypeConfiguration = managedViewType.getInheritanceSubtypeConfiguration(inheritanceSubtypeMappings);
        List<AbstractParameterAttribute<? super T, ?>> parameterAttributeList;

        if (mappingConstructor == null) {
            parameterAttributeList = Collections.emptyList();
        } else {
            parameterAttributeList = mappingConstructor.getSubtypeConstructorConfiguration(inheritanceSubtypeMappings).getParameterAttributesClosure();
        }

        List<Class<?>> parameterTypes;
        if (parameterAttributeList.isEmpty()) {
            parameterTypes = inheritanceSubtypeConfiguration.getParameterTypes();
        } else {
            parameterTypes = new ArrayList<>(inheritanceSubtypeConfiguration.getParameterTypes().size() + parameterAttributeList.size());
            parameterTypes.addAll(inheritanceSubtypeConfiguration.getParameterTypes());
            for (ParameterAttribute<? super T, ?> parameterAttribute : parameterAttributeList) {
                parameterTypes.add(parameterAttribute.getConvertedJavaType());
            }
        }

        Class<?>[] constructorParameterTypes = parameterTypes.toArray(new Class[parameterTypes.size()]);
        this.objectInstantiator = new ConstructorReflectionInstantiator<>(mappingConstructor, evm.getProxyFactory(), managedViewType, null, constructorParameterTypes, evm, inheritanceSubtypeConfiguration.getMutableBasicUserTypes(), Collections.<AbstractReflectionInstantiator.TypeConverterEntry>emptyList());
        this.tuple = new Object[constructorParameterTypes.length];
        for (Map.Entry<ManagedViewTypeImpl.AttributeKey, ConstrainedAttribute<AbstractMethodAttribute<? super T, ?>>> attributeEntry : inheritanceSubtypeConfiguration.getAttributesClosure().entrySet()) {
            if (attributeEntry.getValue().getAttribute().getMappingType() == Attribute.MappingType.PARAMETER) {
                tuple[attributeEntry.getValue().getAttribute().getAttributeIndex()] = optionalParameters.get(attributeEntry.getValue().getAttribute().getMapping());
            }
        }
        this.parameterOffset = inheritanceSubtypeConfiguration.getAttributesClosure().size();
        for (AbstractParameterAttribute<? super T, ?> parameterAttribute : parameterAttributeList) {
            if (parameterAttribute.getMappingType() == Attribute.MappingType.PARAMETER) {
                tuple[parameterOffset + parameterAttribute.getIndex()] = optionalParameters.get(parameterAttribute.getMapping());
            }
        }
    }

    public ManagedViewTypeImplementor<T> getManagedViewType() {
        return managedViewType;
    }

    public Object[] getTuple() {
        return tuple;
    }

    protected T buildObject() {
        return objectInstantiator.newInstance(tuple);
    }

    private AbstractMethodAttribute<?, ?> getAttribute(String attribute) {
        MethodAttribute<? super T, ?> attr = managedViewType.getAttribute(attribute);
        if (attr == null) {
            throw new IllegalArgumentException("Unknown attribute '" + attribute + "' on type: " + managedViewType.getJavaType().getName());
        }
        return (AbstractMethodAttribute<?, ?>) attr;
    }

    private AbstractParameterAttribute<?, ?> getAttribute(int parameterIndex) {
        if (mappingConstructor == null) {
            throw new IllegalArgumentException("The view '" + managedViewType.getJavaType().getName() + "' does not have a constructor");
        }
        if (parameterIndex >= mappingConstructor.getParameterAttributes().size()) {
            throw new IllegalArgumentException("The constructor '" + mappingConstructor.getName() + "' of the view '" + managedViewType.getJavaType().getName() + "' only has " + mappingConstructor.getParameterAttributes().size() + " parameters! Illegal index: " + parameterIndex);
        }
        return (AbstractParameterAttribute<?, ?>) mappingConstructor.getParameterAttribute(parameterIndex);
    }

    private AbstractAttribute<?, ?> getAttribute(Attribute<?, ?> attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("Null attribute");
        }
        if (!(attribute instanceof AbstractAttribute<?, ?>)) {
            throw new IllegalArgumentException("Illegal unknown attribute type: " + attribute);
        }
        if (managedViewType != attribute.getDeclaringType()) {
            throw new IllegalArgumentException("Attribute is declared by " + attribute.getDeclaringType().getJavaType().getName() + " but the type that is created is: " + managedViewType.getJavaType().getName());
        }
        return (AbstractAttribute<?, ?>) attribute;
    }

    private <E> E getValue(Type<?> type, E value) {
        Class<?> javaType = type.getJavaType();
        if (value != null && !javaType.isInstance(value)) {
            throw new IllegalArgumentException("Expected value of type " + javaType.getName() + " but got: " + value);
        }
        return value;
    }

    private Object setValue(AbstractAttribute<?, ?> attr, Object value) {
        Class<?> type = attr.getJavaType();
        if (value == null) {
            if (attr instanceof MapAttribute<?, ?, ?>) {
                if (attr.needsDirtyTracker()) {
                    value = attr.getMapInstantiator().createRecordingCollection(0);
                } else {
                    value = attr.getMapInstantiator().createCollection(0);
                }
            } else if (attr instanceof PluralAttribute<?, ?, ?>) {
                if (attr.needsDirtyTracker()) {
                    value = attr.getCollectionInstantiator().createRecordingCollection(0);
                } else {
                    value = attr.getCollectionInstantiator().createCollection(0);
                }
            } else {
                if (type.isPrimitive()) {
                    if (type == long.class) {
                        value = 0L;
                    } else if (type == float.class) {
                        value = 0F;
                    } else if (type == double.class) {
                        value = 0D;
                    } else if (type == short.class) {
                        value = (short) 0;
                    } else if (type == byte.class) {
                        value = (byte) 0;
                    } else if (type == char.class) {
                        value = '\u0000';
                    } else if (type == boolean.class) {
                        value = false;
                    } else {
                        value = 0;
                    }
                } else {
                    // TODO: Handle flat views?
                }
            }
        } else {
            if (!type.isInstance(value)) {
                throw new IllegalArgumentException("Expected value of type " + type.getName() + " but got: " + value);
            }
        }

        if (attr instanceof AbstractMethodAttribute<?, ?>) {
            tuple[((AbstractMethodAttribute<?, ?>) attr).getAttributeIndex()] = value;
        } else {
            tuple[parameterOffset + ((AbstractParameterAttribute<?, ?>) attr).getIndex()] = value;
        }
        return value;
    }

    private <A> A getValue(AbstractAttribute<?, ?> attr) {
        if (attr instanceof AbstractMethodAttribute<?, ?>) {
            return (A) tuple[((AbstractMethodAttribute<?, ?>) attr).getAttributeIndex()];
        } else {
            return (A) tuple[parameterOffset + ((AbstractParameterAttribute<?, ?>) attr).getIndex()];
        }
    }

    private <A extends Collection<Object>> A getCollection(AbstractAttribute<?, ?> attr) {
        Object currentValue = getValue(attr);
        if (currentValue == null) {
            currentValue = setValue(attr, null);
        }
        if (currentValue instanceof RecordingCollection<?, ?>) {
            return (A) ((RecordingCollection<?, ?>) currentValue).getDelegate();
        } else {
            return (A) currentValue;
        }
    }

    private <A extends Map<Object, Object>> A getMap(AbstractAttribute<?, ?> attr) {
        Object currentValue = getValue(attr);
        if (currentValue == null) {
            currentValue = setValue(attr, null);
        }
        if (currentValue instanceof RecordingMap<?, ?, ?>) {
            return (A) ((RecordingMap<?, ?, ?>) currentValue).getDelegate();
        } else {
            return (A) currentValue;
        }
    }

    private void checkAttribute(AbstractAttribute<?, ?> attr, Class<?> attributeType, String type) {
        if (!attributeType.isInstance(attr)) {
            if (attr instanceof PluralAttribute<?, ?, ?>) {
                throw new IllegalArgumentException("Expected " + type + " attribute but " + getLocation(attr) + " was: " + ((PluralAttribute<?, ?, ?>) attr).getCollectionType());
            } else {
                throw new IllegalArgumentException("Expected " + type + " attribute but " + getLocation(attr) + " was: " + attr.getAttributeType());
            }
        }
    }

    private String getLocation(AbstractAttribute<?, ?> attribute) {
        if (attribute instanceof AbstractMethodAttribute<?, ?>) {
            return "attribute '" + ((AbstractMethodAttribute<?, ?>) attribute).getName() + "'";
        } else {
            return "parameter at index '" + ((AbstractParameterAttribute<?, ?>) attribute).getIndex() + "'";
        }
    }

    private void checkType(Type<?> type, String kind, AbstractAttribute<?, ?> attribute) {
        if (!(type instanceof ManagedViewTypeImplementor<?>)) {
            throw new IllegalArgumentException("The " + kind + " type of the " + getLocation(attribute) + " is not an entity view type, but: " + type.getJavaType().getName());
        }
    }

    @Override
    public X with(String attribute, Object value) {
        AbstractMethodAttribute<?, ?> attr = getAttribute(attribute);
        setValue(attr, value);
        return (X) this;
    }

    @Override
    public X with(int parameterIndex, Object value) {
        AbstractParameterAttribute<?, ?> attr = getAttribute(parameterIndex);
        setValue(attr, value);
        return (X) this;
    }

    @Override
    public <E> X with(SingularAttribute<T, E> attribute, E value) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        setValue(attr, value);
        return (X) this;
    }

    @Override
    public <C> X with(PluralAttribute<T, C, ?> attribute, C value) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        setValue(attr, value);
        return (X) this;
    }

    @Override
    public <E> E get(String attribute) {
        return (E) tuple[getAttribute(attribute).getAttributeIndex()];
    }

    @Override
    public <E> E get(int parameterIndex) {
        return getValue(getAttribute(parameterIndex));
    }

    @Override
    public <E> E get(SingularAttribute<T, E> attribute) {
        return getValue(getAttribute(attribute));
    }

    @Override
    public <C> C get(PluralAttribute<T, C, ?> attribute) {
        return getValue(getAttribute(attribute));
    }

    @Override
    public X withElement(String attribute, Object value) {
        return withElement(getAttribute(attribute), value);
    }

    @Override
    public X withElement(int parameterIndex, Object value) {
        return withElement(getAttribute(parameterIndex), value);
    }

    private X withElement(AbstractAttribute<?, ?> attr, Object value) {
        if (attr instanceof MapAttribute<?, ?, ?>) {
            if (value instanceof Map.Entry<?, ?>) {
                Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) value;
                return withEntry(attr, entry.getKey(), entry.getValue());
            } else {
                throw new IllegalArgumentException("Can not add element to map attribute. Use withEntry() or withMapBuilder()!");
            }
        } else if (attr instanceof SingularAttribute<?, ?>) {
            throw new IllegalArgumentException("Can not add element to singular attribute. Use with()!");
        }
        value = getValue(attr.getElementType(), value);
        getCollection(attr).add(value);
        return (X) this;
    }

    @Override
    public X withListElement(String attribute, int index, Object value) {
        return withListElement(getAttribute(attribute), index, value);
    }

    @Override
    public X withListElement(int parameterIndex, int index, Object value) {
        return withListElement(getAttribute(parameterIndex), index, value);
    }

    private X withListElement(AbstractAttribute<?, ?> attr, int index, Object value) {
        checkAttribute(attr, ListAttribute.class, "List");
        value = getValue(attr.getElementType(), value);
        List<Object> list = getCollection(attr);
        addListValue(list, index,value);

        return (X) this;
    }

    private void addListValue(List<Object> list, int index, Object value) {
        if (index > list.size()) {
            for (int i = list.size(); i < index; i++) {
                list.add(null);
            }
            list.add(value);
        } else if (index < list.size()) {
            list.set(index, value);
        } else {
            list.add(value);
        }
    }

    @Override
    public X withEntry(String attribute, Object key, Object value) {
        return withEntry(getAttribute(attribute), key, value);
    }

    @Override
    public X withEntry(int parameterIndex, Object key, Object value) {
        return withEntry(getAttribute(parameterIndex), key, value);
    }

    private X withEntry(AbstractAttribute<?, ?> attr, Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Illegal null key!");
        }
        checkAttribute(attr, MapAttribute.class, "Map");
        key = getValue(((MapAttribute<?, ?, ?>) attr).getKeyType(), key);
        value = getValue(attr.getElementType(), value);
        Map<Object, Object> map = getMap(attr);
        map.put(key, value);
        return (X) this;
    }

    @Override
    public <E> X withElement(CollectionAttribute<T, E> attribute, E value) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        value = getValue(attr.getElementType(), value);
        getCollection(attr).add(value);
        return (X) this;
    }

    @Override
    public <E> X withElement(SetAttribute<T, E> attribute, E value) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        value = getValue(attr.getElementType(), value);
        getCollection(attr).add(value);
        return (X) this;
    }

    @Override
    public <E> X withElement(ListAttribute<T, E> attribute, E value) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        value = getValue(attr.getElementType(), value);
        getCollection(attr).add(value);
        return (X) this;
    }

    @Override
    public <E> X withListElement(ListAttribute<T, E> attribute, int index, E value) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        value = getValue(attr.getElementType(), value);
        List<Object> list = getCollection(attr);
        addListValue(list, index,value);
        return (X) this;
    }

    @Override
    public <K, V> X withEntry(MapAttribute<T, K, V> attribute, K key, V value) {
        return withEntry(getAttribute(attribute), key, value);
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withSingularBuilder(String attribute) {
        return withSingularBuilder(getAttribute(attribute));
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withSingularBuilder(int parameterIndex) {
        return withSingularBuilder(getAttribute(parameterIndex));
    }

    private <E> EntityViewNestedBuilder<E, X> withSingularBuilder(AbstractAttribute<?, ?> attr) {
        checkAttribute(attr, SingularAttribute.class, "Singular");
        checkType(attr.getElementType(), "attribute", attr);

        Map<ManagedViewType<? extends E>, String> inheritanceSubtypeMappings = ((SingularAttribute<?, E>) attr).getInheritanceSubtypeMappings();
        int index;
        if (attr instanceof AbstractMethodAttribute<?, ?>) {
            index = ((AbstractMethodAttribute<?, ?>) attr).getAttributeIndex();
        } else {
            index = parameterOffset + ((AbstractParameterAttribute<?, ?>) attr).getIndex();
        }
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<E>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new SingularEntityViewBuilderListener(tuple, index));
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withCollectionBuilder(String attribute) {
        return withCollectionBuilder(getAttribute(attribute));
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withCollectionBuilder(int parameterIndex) {
        return withCollectionBuilder(getAttribute(parameterIndex));
    }

    private <E> EntityViewNestedBuilder<E, X> withCollectionBuilder(AbstractAttribute<?, ?> attr) {
        if (attr instanceof MapAttribute<?, ?, ?>) {
            throw new IllegalArgumentException("Can not add element to map attribute. Use withMapBuilder()!");
        } else if (attr instanceof SingularAttribute<?, ?>) {
            throw new IllegalArgumentException("Can not add element to singular attribute. Use withBuilder()!");
        }
        checkType(attr.getElementType(), "element", attr);
        Map<ManagedViewType<? extends E>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, E>) attr).getElementInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<E>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new CollectionEntityViewBuilderListener(getCollection(attr)));
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withListBuilder(String attribute) {
        AbstractMethodAttribute<?, ?> attr = getAttribute(attribute);
        checkAttribute(attr, ListAttribute.class, "List");
        return withCollectionBuilder(attribute);
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withListBuilder(int parameterIndex) {
        AbstractAttribute<?, ?> attr = getAttribute(parameterIndex);
        checkAttribute(attr, ListAttribute.class, "List");
        return withCollectionBuilder(parameterIndex);
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withListBuilder(String attribute, int index) {
        return withListBuilder(getAttribute(attribute), index);
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withListBuilder(int parameterIndex, int index) {
        return withListBuilder(getAttribute(parameterIndex), index);
    }

    public <E> EntityViewNestedBuilder<E, X> withListBuilder(AbstractAttribute<?, ?> attr, int index) {
        checkAttribute(attr, ListAttribute.class, "List");
        checkType(attr.getElementType(), "element", attr);
        Map<ManagedViewType<? extends E>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, E>) attr).getElementInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<E>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new ListEntityViewBuilderListener((List<Object>) getCollection(attr), index));
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withSetBuilder(String attribute) {
        return withSetBuilder(getAttribute(attribute));
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withSetBuilder(int parameterIndex) {
        return withSetBuilder(getAttribute(parameterIndex));
    }

    private <E> EntityViewNestedBuilder<E, X> withSetBuilder(AbstractAttribute<?, ?> attr) {
        checkAttribute(attr, SetAttribute.class, "Set");
        checkType(attr.getElementType(), "element", attr);
        Map<ManagedViewType<? extends E>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, E>) attr).getElementInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<E>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new CollectionEntityViewBuilderListener(getCollection(attr)));
    }

    @Override
    public <V> EntityViewNestedBuilder<V, X> withMapBuilder(String attribute, Object key) {
        return withMapBuilder(getAttribute(attribute), key);
    }

    @Override
    public <V> EntityViewNestedBuilder<V, X> withMapBuilder(int parameterIndex, Object key) {
        return withMapBuilder(getAttribute(parameterIndex), key);
    }

    private <V> EntityViewNestedBuilder<V, X> withMapBuilder(AbstractAttribute<?, ?> attr, Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Illegal null key!");
        }
        checkAttribute(attr, MapAttribute.class, "Map");
        Type<?> keyType = ((MapAttribute<?, ?, ?>) attr).getKeyType();
        checkType(attr.getElementType(), "element", attr);
        key = getValue(keyType, key);
        Map<ManagedViewType<? extends V>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, V>) attr).getElementInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<V>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new MapEntityViewBuilderListener(getMap(attr), key));
    }

    @Override
    public <K, V> EntityViewNestedBuilder<K, EntityViewNestedBuilder<V, X>> withMapBuilder(String attribute) {
        return withMapBuilder(getAttribute(attribute));
    }

    @Override
    public <K, V> EntityViewNestedBuilder<K, EntityViewNestedBuilder<V, X>> withMapBuilder(int parameterIndex) {
        return withMapBuilder(getAttribute(parameterIndex));
    }

    private <K, V> EntityViewNestedBuilder<K, EntityViewNestedBuilder<V, X>> withMapBuilder(AbstractAttribute<?, ?> attr) {
        checkAttribute(attr, MapAttribute.class, "Map");
        checkType(((MapAttribute<?, ?, ?>) attr).getKeyType(), "key", attr);
        checkType(attr.getElementType(), "element", attr);
        MapKeyEntityViewBuilderListener listener = new MapKeyEntityViewBuilderListener(getMap(attr));
        Map<ManagedViewType<? extends V>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, V>) attr).getElementInheritanceSubtypeMappings();
        EntityViewNestedBuilder<V, X> valueBuilder = new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<V>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, listener);
        Map<ManagedViewType<? extends K>, String> keyInheritanceSubtypeMappings = ((MapAttribute<?, K, ?>) attr).getKeyInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<K>) attr.getElementType(), keyInheritanceSubtypeMappings, optionalParameters, valueBuilder, listener);
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withBuilder(SingularAttribute<T, E> attribute) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        checkType(attr.getElementType(), "attribute", attr);
        Map<ManagedViewType<? extends E>, String> inheritanceSubtypeMappings = ((SingularAttribute<?, E>) attr).getInheritanceSubtypeMappings();
        int index;
        if (attr instanceof AbstractMethodAttribute<?, ?>) {
            index = ((AbstractMethodAttribute<?, ?>) attr).getAttributeIndex();
        } else {
            index = parameterOffset + ((AbstractParameterAttribute<?, ?>) attr).getIndex();
        }
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<E>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new SingularEntityViewBuilderListener(tuple, index));
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withBuilder(CollectionAttribute<T, E> attribute) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        checkType(attr.getElementType(), "element", attr);
        Map<ManagedViewType<? extends E>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, E>) attr).getElementInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<E>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new CollectionEntityViewBuilderListener(getCollection(attr)));
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withBuilder(ListAttribute<T, E> attribute) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        checkType(attr.getElementType(), "element", attr);
        Map<ManagedViewType<? extends E>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, E>) attr).getElementInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<E>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new CollectionEntityViewBuilderListener(getCollection(attr)));
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withBuilder(ListAttribute<T, E> attribute, int index) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        checkType(attr.getElementType(), "element", attr);
        Map<ManagedViewType<? extends E>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, E>) attr).getElementInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<E>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new ListEntityViewBuilderListener((List<Object>) getCollection(attr), index));
    }

    @Override
    public <E> EntityViewNestedBuilder<E, X> withBuilder(SetAttribute<T, E> attribute) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        checkType(attr.getElementType(), "element", attr);
        Map<ManagedViewType<? extends E>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, E>) attr).getElementInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<E>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new CollectionEntityViewBuilderListener(getCollection(attr)));
    }

    @Override
    public <K, V> EntityViewNestedBuilder<K, EntityViewNestedBuilder<V, X>> withBuilder(MapAttribute<T, K, V> attribute) {
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        checkType(((MapAttribute<?, ?, ?>) attr).getKeyType(), "key", attr);
        checkType(attr.getElementType(), "element", attr);
        MapKeyEntityViewBuilderListener listener = new MapKeyEntityViewBuilderListener(getMap(attr));
        Map<ManagedViewType<? extends V>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, V>) attr).getElementInheritanceSubtypeMappings();
        EntityViewNestedBuilder<V, X> valueBuilder = new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<V>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, listener);
        Map<ManagedViewType<? extends K>, String> keyInheritanceSubtypeMappings = ((MapAttribute<?, K, ?>) attr).getKeyInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<K>) attr.getElementType(), keyInheritanceSubtypeMappings, optionalParameters, valueBuilder, listener);
    }

    @Override
    public <K, V> EntityViewNestedBuilder<V, X> withBuilder(MapAttribute<T, K, V> attribute, K key) {
        if (key == null) {
            throw new IllegalArgumentException("Illegal null key!");
        }
        AbstractAttribute<?, ?> attr = getAttribute(attribute);
        checkAttribute(attr, MapAttribute.class, "Map");
        Type<?> keyType = ((MapAttribute<?, ?, ?>) attr).getKeyType();
        checkType(attr.getElementType(), "element", attr);
        key = getValue(keyType, key);
        Map<ManagedViewType<? extends V>, String> inheritanceSubtypeMappings = ((PluralAttribute<?, ?, V>) attr).getElementInheritanceSubtypeMappings();
        return new EntityViewNestedBuilderImpl<>(evm, (ManagedViewTypeImplementor<V>) attr.getElementType(), inheritanceSubtypeMappings, optionalParameters, (X) this, new MapEntityViewBuilderListener(getMap(attr), key));
    }
}