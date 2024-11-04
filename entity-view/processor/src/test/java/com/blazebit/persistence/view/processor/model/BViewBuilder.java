package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.EntityViewBuilder;
import com.blazebit.persistence.view.EntityViewBuilderBase;
import com.blazebit.persistence.view.EntityViewBuilderListener;
import com.blazebit.persistence.view.EntityViewNestedBuilder;
import com.blazebit.persistence.view.RecordingContainer;
import com.blazebit.persistence.view.SingularNameEntityViewBuilderListener;
import com.blazebit.persistence.view.StaticBuilder;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.CollectionAttribute;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SetAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
@StaticBuilder(BView.class)
public abstract class BViewBuilder<X extends Serializable, BuilderType extends EntityViewBuilderBase<BView, BuilderType>> implements EntityViewBuilderBase<BView, BuilderType> {

    protected Integer id;
    protected String name;
    protected Integer parent;
    protected final Map<String, Object> blazePersistenceOptionalParameters;

    public BViewBuilder(Map<String, Object> blazePersistenceOptionalParameters) {
        this.id = null;
        this.name = null;
        this.parent = null;
        this.blazePersistenceOptionalParameters = blazePersistenceOptionalParameters;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public BuilderType withId(Integer id) {
        this.id = id;
        return (BuilderType) this;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public BuilderType withName(String name) {
        this.name = name;
        return (BuilderType) this;
    }
    public Integer getParent() {
        return parent;
    }
    public void setParent(Integer parent) {
        this.parent = parent;
    }
    public BuilderType withParent(Integer parent) {
        this.parent = parent;
        return (BuilderType) this;
    }

    protected <ElementType> ElementType get(Attribute<?, ?> attr) {
        if (attr instanceof MethodAttribute) {
            return get(((MethodAttribute) attr).getName());
        } else {
            return get(((ParameterAttribute) attr).getIndex());
        }
    }

    protected <CollectionType extends Collection<Object>> CollectionType getCollection(Attribute<?, ?> attr) {
        if (attr instanceof MethodAttribute) {
            return getCollection(((MethodAttribute) attr).getName());
        } else {
            return getCollection(((ParameterAttribute) attr).getIndex());
        }
    }

    protected <CollectionType extends Map<Object, Object>> CollectionType getMap(Attribute<?, ?> attr) {
        if (attr instanceof MethodAttribute) {
            return getMap(((MethodAttribute) attr).getName());
        } else {
            return getMap(((ParameterAttribute) attr).getIndex());
        }
    }

    protected <CollectionType extends Collection<Object>> CollectionType getCollection(String attr) {
        Object currentValue = get(attr);
        if (currentValue == null) {
            with(attr, null);
            currentValue = get(attr);
        }
        if (currentValue instanceof RecordingContainer<?>) {
            return (CollectionType) ((RecordingContainer<?>) currentValue).getDelegate();
        } else {
            return (CollectionType) currentValue;
        }
    }

    protected <CollectionType extends Map<Object, Object>> CollectionType getMap(String attr) {
        Object currentValue = get(attr);
        if (currentValue == null) {
            with(attr, null);
            currentValue = get(attr);
        }
        if (currentValue instanceof RecordingContainer<?>) {
            return (CollectionType) ((RecordingContainer<?>) currentValue).getDelegate();
        } else {
            return (CollectionType) currentValue;
        }
    }

    protected <CollectionType extends Collection<Object>> CollectionType getCollection(int attr) {
        Object currentValue = get(attr);
        if (currentValue == null) {
            with(attr, null);
            currentValue = get(attr);
        }
        if (currentValue instanceof RecordingContainer<?>) {
            return (CollectionType) ((RecordingContainer<?>) currentValue).getDelegate();
        } else {
            return (CollectionType) currentValue;
        }
    }

    protected <CollectionType extends Map<Object, Object>> CollectionType getMap(int attr) {
        Object currentValue = get(attr);
        if (currentValue == null) {
            with(attr, null);
            currentValue = get(attr);
        }
        if (currentValue instanceof RecordingContainer<?>) {
            return (CollectionType) ((RecordingContainer<?>) currentValue).getDelegate();
        } else {
            return (CollectionType) currentValue;
        }
    }

    protected void addListValue(List<Object> list, int index, Object value) {
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
    public <ElementType> ElementType get(String attribute) {
        switch (attribute) {
            case "id":
                return (ElementType) (Object) this.id;
            case "name":
                return (ElementType) (Object) this.name;
            case "parent":
                return (ElementType) (Object) this.parent;
        }
        throw new IllegalArgumentException("Unknown attribute: " + attribute);
    }

    @Override
    public <ElementType> ElementType get(SingularAttribute<BView, ElementType> attribute) {
        return get((Attribute<?, ?>) attribute);
    }

    @Override
    public <CollectionType> CollectionType get(PluralAttribute<BView, CollectionType, ?> attribute) {
        return get((Attribute<?, ?>) attribute);
    }

    @Override
    public BuilderType with(String attribute, Object value) {
        switch (attribute) {
            case "id":
                this.id = value == null ? null : (Integer) value;
                break;
            case "name":
                this.name = value == null ? null : (String) value;
                break;
            case "parent":
                this.parent = value == null ? null : (Integer) value;
                break;
            default:
                throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }
        return (BuilderType) this;
    }

    @Override
    public <ElementType> BuilderType with(SingularAttribute<BView, ElementType> attribute, ElementType value) {
        if (attribute instanceof MethodAttribute) {
            return with(((MethodAttribute) attribute).getName(), value);
        } else {
            return with(((ParameterAttribute) attribute).getIndex(), value);
        }
    }

    @Override
    public <CollectionType> BuilderType with(PluralAttribute<BView, CollectionType, ?> attribute, CollectionType value) {
        if (attribute instanceof MethodAttribute) {
            return with(((MethodAttribute) attribute).getName(), value);
        } else {
            return with(((ParameterAttribute) attribute).getIndex(), value);
        }
    }

    @Override
    public BuilderType withElement(String attribute, Object value) {
        getCollection(attribute).add(value);
        return (BuilderType) this;
    }

    @Override
    public BuilderType withElement(int parameterIndex, Object value) {
        getCollection(parameterIndex).add(value);
        return (BuilderType) this;
    }

    @Override
    public BuilderType withListElement(String attribute, int index, Object value) {
        List<Object> list = getCollection(attribute);
        addListValue(list, index, value);
        return (BuilderType) this;
    }

    @Override
    public BuilderType withListElement(int parameterIndex, int index, Object value) {
        List<Object> list = getCollection(parameterIndex);
        addListValue(list, index, value);
        return (BuilderType) this;
    }

    @Override
    public BuilderType withEntry(String attribute, Object key, Object value) {
        Map<Object, Object> map = getMap(attribute);
        map.put(key, value);
        return (BuilderType) this;
    }

    @Override
    public BuilderType withEntry(int parameterIndex, Object key, Object value) {
        Map<Object, Object> map = getMap(parameterIndex);
        map.put(key, value);
        return (BuilderType) this;
    }

    @Override
    public <ElementType> BuilderType withElement(CollectionAttribute<BView, ElementType> attribute, ElementType value) {
        getCollection(attribute).add(value);
        return (BuilderType) this;
    }

    @Override
    public <ElementType> BuilderType withElement(SetAttribute<BView, ElementType> attribute, ElementType value) {
        getCollection(attribute).add(value);
        return (BuilderType) this;
    }

    @Override
    public <ElementType> BuilderType withElement(ListAttribute<BView, ElementType> attribute, ElementType value) {
        getCollection(attribute).add(value);
        return (BuilderType) this;
    }

    @Override
    public <ElementType> BuilderType withListElement(ListAttribute<BView, ElementType> attribute, int index, ElementType value) {
        List<Object> list = getCollection(attribute);
        addListValue(list, index, value);
        return (BuilderType) this;
    }

    @Override
    public <KeyType, ElementType> BuilderType withEntry(MapAttribute<BView, KeyType, ElementType> attribute, KeyType key, ElementType value) {
        Map<Object, Object> map = getMap(attribute);
        map.put(key, value);
        return (BuilderType) this;
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withSingularBuilder(String attribute) {
        switch (attribute) {
        }
        throw new IllegalArgumentException("Unknown attribute: " + attribute);
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withCollectionBuilder(String attribute) {
        switch (attribute) {
        }
        throw new IllegalArgumentException("Unknown attribute: " + attribute);
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withListBuilder(String attribute) {
        switch (attribute) {
        }
        throw new IllegalArgumentException("Unknown attribute: " + attribute);
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withListBuilder(String attribute, int index) {
        switch (attribute) {
        }
        throw new IllegalArgumentException("Unknown attribute: " + attribute);
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withSetBuilder(String attribute) {
        switch (attribute) {
        }
        throw new IllegalArgumentException("Unknown attribute: " + attribute);
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withMapBuilder(String attribute, Object key) {
        switch (attribute) {
        }
        throw new IllegalArgumentException("Unknown attribute: " + attribute);
    }

    @Override
    public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?>, ?> withMapBuilder(String attribute) {
        switch (attribute) {
        }
        throw new IllegalArgumentException("Unknown attribute: " + attribute);
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(SingularAttribute<BView, ElementType> attr) {
        if (attr instanceof MethodAttribute) {
            return withSingularBuilder(((MethodAttribute) attr).getName());
        } else {
            return withSingularBuilder(((ParameterAttribute) attr).getIndex());
        }
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(CollectionAttribute<BView, ElementType> attr) {
        if (attr instanceof MethodAttribute) {
            return withCollectionBuilder(((MethodAttribute) attr).getName());
        } else {
            return withCollectionBuilder(((ParameterAttribute) attr).getIndex());
        }
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(ListAttribute<BView, ElementType> attr) {
        if (attr instanceof MethodAttribute) {
            return withListBuilder(((MethodAttribute) attr).getName());
        } else {
            return withListBuilder(((ParameterAttribute) attr).getIndex());
        }
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(ListAttribute<BView, ElementType> attr, int index) {
        if (attr instanceof MethodAttribute) {
            return withListBuilder(((MethodAttribute) attr).getName(), index);
        } else {
            return withListBuilder(((ParameterAttribute) attr).getIndex(), index);
        }
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(SetAttribute<BView, ElementType> attr) {
        if (attr instanceof MethodAttribute) {
            return withSetBuilder(((MethodAttribute) attr).getName());
        } else {
            return withSetBuilder(((ParameterAttribute) attr).getIndex());
        }
    }

    @Override
    public <KeyType, ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(MapAttribute<BView, KeyType, ElementType> attr, KeyType key) {
        if (attr instanceof MethodAttribute) {
            return withMapBuilder(((MethodAttribute) attr).getName(), key);
        } else {
            return withMapBuilder(((ParameterAttribute) attr).getIndex(), key);
        }
    }

    @Override
    public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?>, ?> withBuilder(MapAttribute<BView, KeyType, ElementType> attr) {
        if (attr instanceof MethodAttribute) {
            return withMapBuilder(((MethodAttribute) attr).getName());
        } else {
            return withMapBuilder(((ParameterAttribute) attr).getIndex());
        }
    }


    public static class Create<X extends Serializable> extends BViewBuilder<X, EntityViewBuilder<BView>> implements EntityViewBuilder<BView> {

        public Create(Map<String, Object> blazePersistenceOptionalParameters) {
            super(blazePersistenceOptionalParameters);
        }

        @Override
        public BView build() {
            return new BViewImpl(
                    this.id,
                    this.name,
                    this.parent
            );
        }

        @Override
        public Create<X> with(int parameterIndex, Object value) {
            switch (parameterIndex) {
                default:
                    throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
            }
        }

        @Override
        public <ElementType> ElementType get(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        public Create<X> withId(Integer id) {
            this.id = id;
            return (Create<X>) this;
        }
        public Create<X> withName(String name) {
            this.name = name;
            return (Create<X>) this;
        }
        public Create<X> withParent(Integer parent) {
            this.parent = parent;
            return (Create<X>) this;
        }

        @Override
        public Create<X> with(String attribute, Object value) {
            switch (attribute) {
                case "id":
                    this.id = value == null ? null : (Integer) value;
                    break;
                case "name":
                    this.name = value == null ? null : (String) value;
                    break;
                case "parent":
                    this.parent = value == null ? null : (Integer) value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown attribute: " + attribute);
            }
            return (Create<X>) this;
        }

        @Override
        public <ElementType> Create<X> with(SingularAttribute<BView, ElementType> attribute, ElementType value) {
            if (attribute instanceof MethodAttribute) {
                return with(((MethodAttribute) attribute).getName(), value);
            } else {
                return with(((ParameterAttribute) attribute).getIndex(), value);
            }
        }

        @Override
        public <CollectionType> Create<X> with(PluralAttribute<BView, CollectionType, ?> attribute, CollectionType value) {
            if (attribute instanceof MethodAttribute) {
                return with(((MethodAttribute) attribute).getName(), value);
            } else {
                return with(((ParameterAttribute) attribute).getIndex(), value);
            }
        }

        @Override
        public Create<X> withElement(String attribute, Object value) {
            getCollection(attribute).add(value);
            return (Create<X>) this;
        }

        @Override
        public Create<X> withElement(int parameterIndex, Object value) {
            getCollection(parameterIndex).add(value);
            return (Create<X>) this;
        }

        @Override
        public Create<X> withListElement(String attribute, int index, Object value) {
            List<Object> list = getCollection(attribute);
            addListValue(list, index, value);
            return (Create<X>) this;
        }

        @Override
        public Create<X> withListElement(int parameterIndex, int index, Object value) {
            List<Object> list = getCollection(parameterIndex);
            addListValue(list, index, value);
            return (Create<X>) this;
        }

        @Override
        public Create<X> withEntry(String attribute, Object key, Object value) {
            Map<Object, Object> map = getMap(attribute);
            map.put(key, value);
            return (Create<X>) this;
        }

        @Override
        public Create<X> withEntry(int parameterIndex, Object key, Object value) {
            Map<Object, Object> map = getMap(parameterIndex);
            map.put(key, value);
            return (Create<X>) this;
        }

        @Override
        public <ElementType> Create<X> withElement(CollectionAttribute<BView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Create<X>) this;
        }

        @Override
        public <ElementType> Create<X> withElement(SetAttribute<BView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Create<X>) this;
        }

        @Override
        public <ElementType> Create<X> withElement(ListAttribute<BView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Create<X>) this;
        }

        @Override
        public <ElementType> Create<X> withListElement(ListAttribute<BView, ElementType> attribute, int index, ElementType value) {
            List<Object> list = getCollection(attribute);
            addListValue(list, index, value);
            return (Create<X>) this;
        }

        @Override
        public <KeyType, ElementType> Create<X> withEntry(MapAttribute<BView, KeyType, ElementType> attribute, KeyType key, ElementType value) {
            Map<Object, Object> map = getMap(attribute);
            map.put(key, value);
            return (Create<X>) this;
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withSingularBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withCollectionBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withListBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withListBuilder(String attribute, int index) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withSetBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withMapBuilder(String attribute, Object key) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?>, ?> withMapBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withBuilder(SingularAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withSingularBuilder(((MethodAttribute) attr).getName());
            } else {
                return withSingularBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withBuilder(CollectionAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withCollectionBuilder(((MethodAttribute) attr).getName());
            } else {
                return withCollectionBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withBuilder(ListAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withListBuilder(((MethodAttribute) attr).getName());
            } else {
                return withListBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withBuilder(ListAttribute<BView, ElementType> attr, int index) {
            if (attr instanceof MethodAttribute) {
                return withListBuilder(((MethodAttribute) attr).getName(), index);
            } else {
                return withListBuilder(((ParameterAttribute) attr).getIndex(), index);
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withBuilder(SetAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withSetBuilder(((MethodAttribute) attr).getName());
            } else {
                return withSetBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withBuilder(MapAttribute<BView, KeyType, ElementType> attr, KeyType key) {
            if (attr instanceof MethodAttribute) {
                return withMapBuilder(((MethodAttribute) attr).getName(), key);
            } else {
                return withMapBuilder(((ParameterAttribute) attr).getIndex(), key);
            }
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?>, ?> withBuilder(MapAttribute<BView, KeyType, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withMapBuilder(((MethodAttribute) attr).getName());
            } else {
                return withMapBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withSingularBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withCollectionBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withListBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withListBuilder(int parameterIndex, int index) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withSetBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?> withMapBuilder(int parameterIndex, Object key) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Create<X>, ?>, ?> withMapBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }
    }

    public static class Init<X extends Serializable> extends BViewBuilder<X, EntityViewBuilder<BView>> implements EntityViewBuilder<BView> {
        protected BView self;

        public Init(Map<String, Object> blazePersistenceOptionalParameters) {
            super(blazePersistenceOptionalParameters);
        }

        @Override
        public BView build() {
            return new BViewImpl(
                    this.id,
                    this.name,
                    this.parent,
                    this.self
            );
        }

        @Override
        public Init<X> with(int parameterIndex, Object value) {
            switch (parameterIndex) {
                case 0:
                    this.self = value == null ? null : (BView) value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
            }
            return this;
        }

        @Override
        public <ElementType> ElementType get(int parameterIndex) {
            switch (parameterIndex) {
                case 0:
                    return (ElementType) (Object) this.self;
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        public Init<X> withId(Integer id) {
            this.id = id;
            return (Init<X>) this;
        }
        public Init<X> withName(String name) {
            this.name = name;
            return (Init<X>) this;
        }
        public Init<X> withParent(Integer parent) {
            this.parent = parent;
            return (Init<X>) this;
        }

        @Override
        public Init<X> with(String attribute, Object value) {
            switch (attribute) {
                case "id":
                    this.id = value == null ? null : (Integer) value;
                    break;
                case "name":
                    this.name = value == null ? null : (String) value;
                    break;
                case "parent":
                    this.parent = value == null ? null : (Integer) value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown attribute: " + attribute);
            }
            return (Init<X>) this;
        }

        @Override
        public <ElementType> Init<X> with(SingularAttribute<BView, ElementType> attribute, ElementType value) {
            if (attribute instanceof MethodAttribute) {
                return with(((MethodAttribute) attribute).getName(), value);
            } else {
                return with(((ParameterAttribute) attribute).getIndex(), value);
            }
        }

        @Override
        public <CollectionType> Init<X> with(PluralAttribute<BView, CollectionType, ?> attribute, CollectionType value) {
            if (attribute instanceof MethodAttribute) {
                return with(((MethodAttribute) attribute).getName(), value);
            } else {
                return with(((ParameterAttribute) attribute).getIndex(), value);
            }
        }

        @Override
        public Init<X> withElement(String attribute, Object value) {
            getCollection(attribute).add(value);
            return (Init<X>) this;
        }

        @Override
        public Init<X> withElement(int parameterIndex, Object value) {
            getCollection(parameterIndex).add(value);
            return (Init<X>) this;
        }

        @Override
        public Init<X> withListElement(String attribute, int index, Object value) {
            List<Object> list = getCollection(attribute);
            addListValue(list, index, value);
            return (Init<X>) this;
        }

        @Override
        public Init<X> withListElement(int parameterIndex, int index, Object value) {
            List<Object> list = getCollection(parameterIndex);
            addListValue(list, index, value);
            return (Init<X>) this;
        }

        @Override
        public Init<X> withEntry(String attribute, Object key, Object value) {
            Map<Object, Object> map = getMap(attribute);
            map.put(key, value);
            return (Init<X>) this;
        }

        @Override
        public Init<X> withEntry(int parameterIndex, Object key, Object value) {
            Map<Object, Object> map = getMap(parameterIndex);
            map.put(key, value);
            return (Init<X>) this;
        }

        @Override
        public <ElementType> Init<X> withElement(CollectionAttribute<BView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Init<X>) this;
        }

        @Override
        public <ElementType> Init<X> withElement(SetAttribute<BView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Init<X>) this;
        }

        @Override
        public <ElementType> Init<X> withElement(ListAttribute<BView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Init<X>) this;
        }

        @Override
        public <ElementType> Init<X> withListElement(ListAttribute<BView, ElementType> attribute, int index, ElementType value) {
            List<Object> list = getCollection(attribute);
            addListValue(list, index, value);
            return (Init<X>) this;
        }

        @Override
        public <KeyType, ElementType> Init<X> withEntry(MapAttribute<BView, KeyType, ElementType> attribute, KeyType key, ElementType value) {
            Map<Object, Object> map = getMap(attribute);
            map.put(key, value);
            return (Init<X>) this;
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withSingularBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withCollectionBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withListBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withListBuilder(String attribute, int index) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withSetBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withMapBuilder(String attribute, Object key) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?>, ?> withMapBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(SingularAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withSingularBuilder(((MethodAttribute) attr).getName());
            } else {
                return withSingularBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(CollectionAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withCollectionBuilder(((MethodAttribute) attr).getName());
            } else {
                return withCollectionBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(ListAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withListBuilder(((MethodAttribute) attr).getName());
            } else {
                return withListBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(ListAttribute<BView, ElementType> attr, int index) {
            if (attr instanceof MethodAttribute) {
                return withListBuilder(((MethodAttribute) attr).getName(), index);
            } else {
                return withListBuilder(((ParameterAttribute) attr).getIndex(), index);
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(SetAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withSetBuilder(((MethodAttribute) attr).getName());
            } else {
                return withSetBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(MapAttribute<BView, KeyType, ElementType> attr, KeyType key) {
            if (attr instanceof MethodAttribute) {
                return withMapBuilder(((MethodAttribute) attr).getName(), key);
            } else {
                return withMapBuilder(((ParameterAttribute) attr).getIndex(), key);
            }
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?>, ?> withBuilder(MapAttribute<BView, KeyType, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withMapBuilder(((MethodAttribute) attr).getName());
            } else {
                return withMapBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withSingularBuilder(int parameterIndex) {
            switch (parameterIndex) {
                case 0:
                    return (EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?>) withParamSelfBuilder();
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withCollectionBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withListBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withListBuilder(int parameterIndex, int index) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withSetBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withMapBuilder(int parameterIndex, Object key) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?>, ?> withMapBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }
        public BView getParamSelf() {
            return self;
        }
        public void setParamSelf(BView self) {
            this.self = self;
        }
        public Init<X> withParamSelf(BView self) {
            this.self = self;
            return (Init<X>) this;
        }
        public <SubX extends Serializable> BViewBuilder.Nested<SubX, ? extends Init<X>> withParamSelfBuilder() {
            return new BViewBuilder.Nested<>(blazePersistenceOptionalParameters, new SingularNameEntityViewBuilderListener(this, "self"), (Init<X>) this);
        }

    }

    public static class Nested<X extends Serializable, BuilderResult> extends BViewBuilder<X, Nested<X, BuilderResult>> implements EntityViewNestedBuilder<BView, BuilderResult, Nested<X, BuilderResult>> {

        private final EntityViewBuilderListener blazePersistenceListener;
        private final BuilderResult blazePersistenceResult;

        public Nested(Map<String, Object> blazePersistenceOptionalParameters, EntityViewBuilderListener blazePersistenceListener, BuilderResult blazePersistenceResult) {
            super(blazePersistenceOptionalParameters);
            this.blazePersistenceListener = blazePersistenceListener;
            this.blazePersistenceResult = blazePersistenceResult;
        }

        @Override
        public BuilderResult build() {
            blazePersistenceListener.onBuildComplete(new BViewImpl(
                    this.id,
                    this.name,
                    this.parent
            ));
            return blazePersistenceResult;
        }

        @Override
        public Nested<X, BuilderResult> with(int parameterIndex, Object value) {
            switch (parameterIndex) {
                default:
                    throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
            }
        }

        @Override
        public <ElementType> ElementType get(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        public Nested<X, BuilderResult> withId(Integer id) {
            this.id = id;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withName(String name) {
            this.name = name;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withParent(Integer parent) {
            this.parent = parent;
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public Nested<X, BuilderResult> with(String attribute, Object value) {
            switch (attribute) {
                case "id":
                    this.id = value == null ? null : (Integer) value;
                    break;
                case "name":
                    this.name = value == null ? null : (String) value;
                    break;
                case "parent":
                    this.parent = value == null ? null : (Integer) value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown attribute: " + attribute);
            }
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> Nested<X, BuilderResult> with(SingularAttribute<BView, ElementType> attribute, ElementType value) {
            if (attribute instanceof MethodAttribute) {
                return with(((MethodAttribute) attribute).getName(), value);
            } else {
                return with(((ParameterAttribute) attribute).getIndex(), value);
            }
        }

        @Override
        public <CollectionType> Nested<X, BuilderResult> with(PluralAttribute<BView, CollectionType, ?> attribute, CollectionType value) {
            if (attribute instanceof MethodAttribute) {
                return with(((MethodAttribute) attribute).getName(), value);
            } else {
                return with(((ParameterAttribute) attribute).getIndex(), value);
            }
        }

        @Override
        public Nested<X, BuilderResult> withElement(String attribute, Object value) {
            getCollection(attribute).add(value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public Nested<X, BuilderResult> withElement(int parameterIndex, Object value) {
            getCollection(parameterIndex).add(value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public Nested<X, BuilderResult> withListElement(String attribute, int index, Object value) {
            List<Object> list = getCollection(attribute);
            addListValue(list, index, value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public Nested<X, BuilderResult> withListElement(int parameterIndex, int index, Object value) {
            List<Object> list = getCollection(parameterIndex);
            addListValue(list, index, value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public Nested<X, BuilderResult> withEntry(String attribute, Object key, Object value) {
            Map<Object, Object> map = getMap(attribute);
            map.put(key, value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public Nested<X, BuilderResult> withEntry(int parameterIndex, Object key, Object value) {
            Map<Object, Object> map = getMap(parameterIndex);
            map.put(key, value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> Nested<X, BuilderResult> withElement(CollectionAttribute<BView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> Nested<X, BuilderResult> withElement(SetAttribute<BView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> Nested<X, BuilderResult> withElement(ListAttribute<BView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> Nested<X, BuilderResult> withListElement(ListAttribute<BView, ElementType> attribute, int index, ElementType value) {
            List<Object> list = getCollection(attribute);
            addListValue(list, index, value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <KeyType, ElementType> Nested<X, BuilderResult> withEntry(MapAttribute<BView, KeyType, ElementType> attribute, KeyType key, ElementType value) {
            Map<Object, Object> map = getMap(attribute);
            map.put(key, value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withSingularBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withCollectionBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withListBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withListBuilder(String attribute, int index) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withSetBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withMapBuilder(String attribute, Object key) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?>, ?> withMapBuilder(String attribute) {
            switch (attribute) {
            }
            throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(SingularAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withSingularBuilder(((MethodAttribute) attr).getName());
            } else {
                return withSingularBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(CollectionAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withCollectionBuilder(((MethodAttribute) attr).getName());
            } else {
                return withCollectionBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(ListAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withListBuilder(((MethodAttribute) attr).getName());
            } else {
                return withListBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(ListAttribute<BView, ElementType> attr, int index) {
            if (attr instanceof MethodAttribute) {
                return withListBuilder(((MethodAttribute) attr).getName(), index);
            } else {
                return withListBuilder(((ParameterAttribute) attr).getIndex(), index);
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(SetAttribute<BView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withSetBuilder(((MethodAttribute) attr).getName());
            } else {
                return withSetBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(MapAttribute<BView, KeyType, ElementType> attr, KeyType key) {
            if (attr instanceof MethodAttribute) {
                return withMapBuilder(((MethodAttribute) attr).getName(), key);
            } else {
                return withMapBuilder(((ParameterAttribute) attr).getIndex(), key);
            }
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?>, ?> withBuilder(MapAttribute<BView, KeyType, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withMapBuilder(((MethodAttribute) attr).getName());
            } else {
                return withMapBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withSingularBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withCollectionBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withListBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withListBuilder(int parameterIndex, int index) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withSetBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withMapBuilder(int parameterIndex, Object key) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?>, ?> withMapBuilder(int parameterIndex) {
            switch (parameterIndex) {
            }
            throw new IllegalArgumentException("Unknown parameter index: " + parameterIndex);
        }
    }
}