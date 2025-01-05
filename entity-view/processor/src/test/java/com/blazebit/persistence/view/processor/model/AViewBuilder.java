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
import java.util.Optional;
import java.util.Set;
import javax.annotation.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
@StaticBuilder(AView.class)
public abstract class AViewBuilder<X extends Serializable, BuilderType extends EntityViewBuilderBase<AView, BuilderType>> implements EntityViewBuilderBase<AView, BuilderType> {

    protected int age;
    protected byte[] bytes;
    protected Integer id;
    protected List<Map<String, String>> jsonMap;
    protected List<Object> listMappingParameter;
    protected Map<String, String> map;
    protected List<Set<String>> multiNames;
    protected String name;
    protected List<String> names;
    protected BView optionalValue;
    protected List<X> test;
    protected X test2;
    protected final Map<String, Object> blazePersistenceOptionalParameters;

    public AViewBuilder(Map<String, Object> blazePersistenceOptionalParameters) {
        this.age = 0;
        this.bytes = null;
        this.id = null;
        this.jsonMap = null;
        this.listMappingParameter = (List<Object>) blazePersistenceOptionalParameters.get("listMappingParameter");
        this.map = null;
        this.multiNames = (List<Set<String>>) (java.util.List<?>) AView_.multiNames.getCollectionInstantiator().createCollection(0);
        this.name = null;
        this.names = (List<String>) (java.util.List<?>) AView_.names.getCollectionInstantiator().createCollection(0);
        this.optionalValue = null;
        this.test = (List<X>) (java.util.List<?>) AView_.test.getCollectionInstantiator().createCollection(0);
        this.test2 = null;
        this.blazePersistenceOptionalParameters = blazePersistenceOptionalParameters;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public BuilderType withAge(int age) {
        this.age = age;
        return (BuilderType) this;
    }
    public byte[] getBytes() {
        return bytes;
    }
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
    public BuilderType withBytes(byte[] bytes) {
        this.bytes = bytes;
        return (BuilderType) this;
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
    public List<Map<String, String>> getJsonMap() {
        return jsonMap;
    }
    public void setJsonMap(List<Map<String, String>> jsonMap) {
        this.jsonMap = jsonMap;
    }
    public BuilderType withJsonMap(List<Map<String, String>> jsonMap) {
        this.jsonMap = jsonMap;
        return (BuilderType) this;
    }
    public List<Object> getListMappingParameter() {
        return listMappingParameter;
    }
    public void setListMappingParameter(List<Object> listMappingParameter) {
        this.listMappingParameter = listMappingParameter;
    }
    public BuilderType withListMappingParameter(List<Object> listMappingParameter) {
        this.listMappingParameter = listMappingParameter;
        return (BuilderType) this;
    }
    public Map<String, String> getMap() {
        return map;
    }
    public void setMap(Map<String, String> map) {
        this.map = map;
    }
    public BuilderType withMap(Map<String, String> map) {
        this.map = map;
        return (BuilderType) this;
    }
    public List<Set<String>> getMultiNames() {
        return multiNames;
    }
    public void setMultiNames(List<Set<String>> multiNames) {
        this.multiNames = multiNames;
    }
    public BuilderType withMultiNames(List<Set<String>> multiNames) {
        this.multiNames = multiNames;
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
    public List<String> getNames() {
        return names;
    }
    public void setNames(List<String> names) {
        this.names = names;
    }
    public BuilderType withNames(List<String> names) {
        this.names = names;
        return (BuilderType) this;
    }
    public BView getOptionalValue() {
        return optionalValue;
    }
    public void setOptionalValue(BView optionalValue) {
        this.optionalValue = optionalValue;
    }
    public BuilderType withOptionalValue(BView optionalValue) {
        this.optionalValue = optionalValue;
        return (BuilderType) this;
    }
    public <SubX extends Serializable> BViewBuilder.Nested<SubX, ? extends BuilderType> withOptionalValueBuilder() {
        return new BViewBuilder.Nested<>(blazePersistenceOptionalParameters, new SingularNameEntityViewBuilderListener(this, "optionalValue"), (BuilderType) this);
    }
    public List<X> getTest() {
        return test;
    }
    public void setTest(List<X> test) {
        this.test = test;
    }
    public BuilderType withTest(List<X> test) {
        this.test = test;
        return (BuilderType) this;
    }
    public X getTest2() {
        return test2;
    }
    public void setTest2(X test2) {
        this.test2 = test2;
    }
    public BuilderType withTest2(X test2) {
        this.test2 = test2;
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
            case "age":
                return (ElementType) (Object) this.age;
            case "bytes":
                return (ElementType) (Object) this.bytes;
            case "id":
                return (ElementType) (Object) this.id;
            case "jsonMap":
                return (ElementType) (Object) this.jsonMap;
            case "listMappingParameter":
                return (ElementType) (Object) this.listMappingParameter;
            case "map":
                return (ElementType) (Object) this.map;
            case "multiNames":
                return (ElementType) (Object) this.multiNames;
            case "name":
                return (ElementType) (Object) this.name;
            case "names":
                return (ElementType) (Object) this.names;
            case "optionalValue":
                return (ElementType) (Object) this.optionalValue;
            case "test":
                return (ElementType) (Object) this.test;
            case "test2":
                return (ElementType) (Object) this.test2;
        }
        throw new IllegalArgumentException("Unknown attribute: " + attribute);
    }

    @Override
    public <ElementType> ElementType get(SingularAttribute<AView, ElementType> attribute) {
        return get((Attribute<?, ?>) attribute);
    }

    @Override
    public <CollectionType> CollectionType get(PluralAttribute<AView, CollectionType, ?> attribute) {
        return get((Attribute<?, ?>) attribute);
    }

    @Override
    public BuilderType with(String attribute, Object value) {
        switch (attribute) {
            case "age":
                this.age = value == null ? 0 : (int) value;
                break;
            case "bytes":
                this.bytes = value == null ? null : (byte[]) value;
                break;
            case "id":
                this.id = value == null ? null : (Integer) value;
                break;
            case "jsonMap":
                this.jsonMap = value == null ? null : (List<Map<String, String>>) value;
                break;
            case "listMappingParameter":
                this.listMappingParameter = value == null ? null : (List<Object>) value;
                break;
            case "map":
                this.map = value == null ? null : (Map<String, String>) value;
                break;
            case "multiNames":
                this.multiNames = value == null ? (List<Set<String>>) (java.util.List<?>) AView_.multiNames.getCollectionInstantiator().createCollection(0) : (List<Set<String>>) value;
                break;
            case "name":
                this.name = value == null ? null : (String) value;
                break;
            case "names":
                this.names = value == null ? (List<String>) (java.util.List<?>) AView_.names.getCollectionInstantiator().createCollection(0) : (List<String>) value;
                break;
            case "optionalValue":
                this.optionalValue = value == null ? null : (BView) value;
                break;
            case "test":
                this.test = value == null ? (List<X>) (java.util.List<?>) AView_.test.getCollectionInstantiator().createCollection(0) : (List<X>) value;
                break;
            case "test2":
                this.test2 = value == null ? null : (X) value;
                break;
            default:
                throw new IllegalArgumentException("Unknown attribute: " + attribute);
        }
        return (BuilderType) this;
    }

    @Override
    public <ElementType> BuilderType with(SingularAttribute<AView, ElementType> attribute, ElementType value) {
        if (attribute instanceof MethodAttribute) {
            return with(((MethodAttribute) attribute).getName(), value);
        } else {
            return with(((ParameterAttribute) attribute).getIndex(), value);
        }
    }

    @Override
    public <CollectionType> BuilderType with(PluralAttribute<AView, CollectionType, ?> attribute, CollectionType value) {
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
    public <ElementType> BuilderType withElement(CollectionAttribute<AView, ElementType> attribute, ElementType value) {
        getCollection(attribute).add(value);
        return (BuilderType) this;
    }

    @Override
    public <ElementType> BuilderType withElement(SetAttribute<AView, ElementType> attribute, ElementType value) {
        getCollection(attribute).add(value);
        return (BuilderType) this;
    }

    @Override
    public <ElementType> BuilderType withElement(ListAttribute<AView, ElementType> attribute, ElementType value) {
        getCollection(attribute).add(value);
        return (BuilderType) this;
    }

    @Override
    public <ElementType> BuilderType withListElement(ListAttribute<AView, ElementType> attribute, int index, ElementType value) {
        List<Object> list = getCollection(attribute);
        addListValue(list, index, value);
        return (BuilderType) this;
    }

    @Override
    public <KeyType, ElementType> BuilderType withEntry(MapAttribute<AView, KeyType, ElementType> attribute, KeyType key, ElementType value) {
        Map<Object, Object> map = getMap(attribute);
        map.put(key, value);
        return (BuilderType) this;
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withSingularBuilder(String attribute) {
        switch (attribute) {
            case "optionalValue":
                return (EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?>) (EntityViewNestedBuilder<?, ?, ?>) withOptionalValueBuilder();
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
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(SingularAttribute<AView, ElementType> attr) {
        if (attr instanceof MethodAttribute) {
            return withSingularBuilder(((MethodAttribute) attr).getName());
        } else {
            return withSingularBuilder(((ParameterAttribute) attr).getIndex());
        }
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(CollectionAttribute<AView, ElementType> attr) {
        if (attr instanceof MethodAttribute) {
            return withCollectionBuilder(((MethodAttribute) attr).getName());
        } else {
            return withCollectionBuilder(((ParameterAttribute) attr).getIndex());
        }
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(ListAttribute<AView, ElementType> attr) {
        if (attr instanceof MethodAttribute) {
            return withListBuilder(((MethodAttribute) attr).getName());
        } else {
            return withListBuilder(((ParameterAttribute) attr).getIndex());
        }
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(ListAttribute<AView, ElementType> attr, int index) {
        if (attr instanceof MethodAttribute) {
            return withListBuilder(((MethodAttribute) attr).getName(), index);
        } else {
            return withListBuilder(((ParameterAttribute) attr).getIndex(), index);
        }
    }

    @Override
    public <ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(SetAttribute<AView, ElementType> attr) {
        if (attr instanceof MethodAttribute) {
            return withSetBuilder(((MethodAttribute) attr).getName());
        } else {
            return withSetBuilder(((ParameterAttribute) attr).getIndex());
        }
    }

    @Override
    public <KeyType, ElementType> EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?> withBuilder(MapAttribute<AView, KeyType, ElementType> attr, KeyType key) {
        if (attr instanceof MethodAttribute) {
            return withMapBuilder(((MethodAttribute) attr).getName(), key);
        } else {
            return withMapBuilder(((ParameterAttribute) attr).getIndex(), key);
        }
    }

    @Override
    public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends BuilderType, ?>, ?> withBuilder(MapAttribute<AView, KeyType, ElementType> attr) {
        if (attr instanceof MethodAttribute) {
            return withMapBuilder(((MethodAttribute) attr).getName());
        } else {
            return withMapBuilder(((ParameterAttribute) attr).getIndex());
        }
    }


    public static class Init<X extends Serializable> extends AViewBuilder<X, EntityViewBuilder<AView>> implements EntityViewBuilder<AView> {

        public Init(Map<String, Object> blazePersistenceOptionalParameters) {
            super(blazePersistenceOptionalParameters);
        }

        @Override
        public AView build() {
            return new AViewImpl(
                    this.id,
                    this.age,
                    this.bytes,
                    this.jsonMap,
                    this.listMappingParameter,
                    this.map,
                    this.multiNames,
                    this.name,
                    this.names,
                    (Optional<BView>) AView_.optionalValue.attr().getType().getConverter().convertToViewType(this.optionalValue),
                    this.test,
                    this.test2
            );
        }

        @Override
        public Init<X> with(int parameterIndex, Object value) {
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

        public Init<X> withAge(int age) {
            this.age = age;
            return (Init<X>) this;
        }
        public Init<X> withBytes(byte[] bytes) {
            this.bytes = bytes;
            return (Init<X>) this;
        }
        public Init<X> withId(Integer id) {
            this.id = id;
            return (Init<X>) this;
        }
        public Init<X> withJsonMap(List<Map<String, String>> jsonMap) {
            this.jsonMap = jsonMap;
            return (Init<X>) this;
        }
        public Init<X> withListMappingParameter(List<Object> listMappingParameter) {
            this.listMappingParameter = listMappingParameter;
            return (Init<X>) this;
        }
        public Init<X> withMap(Map<String, String> map) {
            this.map = map;
            return (Init<X>) this;
        }
        public Init<X> withMultiNames(List<Set<String>> multiNames) {
            this.multiNames = multiNames;
            return (Init<X>) this;
        }
        public Init<X> withName(String name) {
            this.name = name;
            return (Init<X>) this;
        }
        public Init<X> withNames(List<String> names) {
            this.names = names;
            return (Init<X>) this;
        }
        public Init<X> withOptionalValue(BView optionalValue) {
            this.optionalValue = optionalValue;
            return (Init<X>) this;
        }
        public <SubX extends Serializable> BViewBuilder.Nested<SubX, ? extends Init<X>> withOptionalValueBuilder() {
            return new BViewBuilder.Nested<>(blazePersistenceOptionalParameters, new SingularNameEntityViewBuilderListener(this, "optionalValue"), (Init<X>) this);
        }
        public Init<X> withTest(List<X> test) {
            this.test = test;
            return (Init<X>) this;
        }
        public Init<X> withTest2(X test2) {
            this.test2 = test2;
            return (Init<X>) this;
        }

        @Override
        public Init<X> with(String attribute, Object value) {
            switch (attribute) {
                case "age":
                    this.age = value == null ? 0 : (int) value;
                    break;
                case "bytes":
                    this.bytes = value == null ? null : (byte[]) value;
                    break;
                case "id":
                    this.id = value == null ? null : (Integer) value;
                    break;
                case "jsonMap":
                    this.jsonMap = value == null ? null : (List<Map<String, String>>) value;
                    break;
                case "listMappingParameter":
                    this.listMappingParameter = value == null ? null: (List<Object>) value;
                    break;
                case "map":
                    this.map = value == null ? null : (Map<String, String>) value;
                    break;
                case "multiNames":
                    this.multiNames = value == null ? (List<Set<String>>) (java.util.List<?>) AView_.multiNames.getCollectionInstantiator().createCollection(0) : (List<Set<String>>) value;
                    break;
                case "name":
                    this.name = value == null ? null : (String) value;
                    break;
                case "names":
                    this.names = value == null ? (List<String>) (java.util.List<?>) AView_.names.getCollectionInstantiator().createCollection(0) : (List<String>) value;
                    break;
                case "optionalValue":
                    this.optionalValue = value == null ? null : (BView) value;
                    break;
                case "test":
                    this.test = value == null ? (List<X>) (java.util.List<?>) AView_.test.getCollectionInstantiator().createCollection(0) : (List<X>) value;
                    break;
                case "test2":
                    this.test2 = value == null ? null : (X) value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown attribute: " + attribute);
            }
            return (Init<X>) this;
        }

        @Override
        public <ElementType> Init<X> with(SingularAttribute<AView, ElementType> attribute, ElementType value) {
            if (attribute instanceof MethodAttribute) {
                return with(((MethodAttribute) attribute).getName(), value);
            } else {
                return with(((ParameterAttribute) attribute).getIndex(), value);
            }
        }

        @Override
        public <CollectionType> Init<X> with(PluralAttribute<AView, CollectionType, ?> attribute, CollectionType value) {
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
        public <ElementType> Init<X> withElement(CollectionAttribute<AView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Init<X>) this;
        }

        @Override
        public <ElementType> Init<X> withElement(SetAttribute<AView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Init<X>) this;
        }

        @Override
        public <ElementType> Init<X> withElement(ListAttribute<AView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Init<X>) this;
        }

        @Override
        public <ElementType> Init<X> withListElement(ListAttribute<AView, ElementType> attribute, int index, ElementType value) {
            List<Object> list = getCollection(attribute);
            addListValue(list, index, value);
            return (Init<X>) this;
        }

        @Override
        public <KeyType, ElementType> Init<X> withEntry(MapAttribute<AView, KeyType, ElementType> attribute, KeyType key, ElementType value) {
            Map<Object, Object> map = getMap(attribute);
            map.put(key, value);
            return (Init<X>) this;
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withSingularBuilder(String attribute) {
            switch (attribute) {
                case "optionalValue":
                    return (EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?>) (EntityViewNestedBuilder<?, ?, ?>) withOptionalValueBuilder();
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
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(SingularAttribute<AView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withSingularBuilder(((MethodAttribute) attr).getName());
            } else {
                return withSingularBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(CollectionAttribute<AView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withCollectionBuilder(((MethodAttribute) attr).getName());
            } else {
                return withCollectionBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(ListAttribute<AView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withListBuilder(((MethodAttribute) attr).getName());
            } else {
                return withListBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(ListAttribute<AView, ElementType> attr, int index) {
            if (attr instanceof MethodAttribute) {
                return withListBuilder(((MethodAttribute) attr).getName(), index);
            } else {
                return withListBuilder(((ParameterAttribute) attr).getIndex(), index);
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(SetAttribute<AView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withSetBuilder(((MethodAttribute) attr).getName());
            } else {
                return withSetBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withBuilder(MapAttribute<AView, KeyType, ElementType> attr, KeyType key) {
            if (attr instanceof MethodAttribute) {
                return withMapBuilder(((MethodAttribute) attr).getName(), key);
            } else {
                return withMapBuilder(((ParameterAttribute) attr).getIndex(), key);
            }
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?>, ?> withBuilder(MapAttribute<AView, KeyType, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withMapBuilder(((MethodAttribute) attr).getName());
            } else {
                return withMapBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Init<X>, ?> withSingularBuilder(int parameterIndex) {
            switch (parameterIndex) {
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
    }

    public static class Nested<X extends Serializable, BuilderResult> extends AViewBuilder<X, Nested<X, BuilderResult>> implements EntityViewNestedBuilder<AView, BuilderResult, Nested<X, BuilderResult>> {

        private final EntityViewBuilderListener blazePersistenceListener;
        private final BuilderResult blazePersistenceResult;

        public Nested(Map<String, Object> blazePersistenceOptionalParameters, EntityViewBuilderListener blazePersistenceListener, BuilderResult blazePersistenceResult) {
            super(blazePersistenceOptionalParameters);
            this.blazePersistenceListener = blazePersistenceListener;
            this.blazePersistenceResult = blazePersistenceResult;
        }

        @Override
        public BuilderResult build() {
            blazePersistenceListener.onBuildComplete(new AViewImpl(
                    this.id,
                    this.age,
                    this.bytes,
                    this.jsonMap,
                    this.listMappingParameter,
                    this.map,
                    this.multiNames,
                    this.name,
                    this.names,
                    (Optional<BView>) AView_.optionalValue.attr().getType().getConverter().convertToViewType(this.optionalValue),
                    this.test,
                    this.test2
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

        public Nested<X, BuilderResult> withAge(int age) {
            this.age = age;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withBytes(byte[] bytes) {
            this.bytes = bytes;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withId(Integer id) {
            this.id = id;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withJsonMap(List<Map<String, String>> jsonMap) {
            this.jsonMap = jsonMap;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withListMappingParameter(List<Object> listMappingParameter) {
            this.listMappingParameter = listMappingParameter;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withMap(Map<String, String> map) {
            this.map = map;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withMultiNames(List<Set<String>> multiNames) {
            this.multiNames = multiNames;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withName(String name) {
            this.name = name;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withNames(List<String> names) {
            this.names = names;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withOptionalValue(BView optionalValue) {
            this.optionalValue = optionalValue;
            return (Nested<X, BuilderResult>) this;
        }
        public <SubX extends Serializable> BViewBuilder.Nested<SubX, ? extends Nested<X, BuilderResult>> withOptionalValueBuilder() {
            return new BViewBuilder.Nested<>(blazePersistenceOptionalParameters, new SingularNameEntityViewBuilderListener(this, "optionalValue"), (Nested<X, BuilderResult>) this);
        }
        public Nested<X, BuilderResult> withTest(List<X> test) {
            this.test = test;
            return (Nested<X, BuilderResult>) this;
        }
        public Nested<X, BuilderResult> withTest2(X test2) {
            this.test2 = test2;
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public Nested<X, BuilderResult> with(String attribute, Object value) {
            switch (attribute) {
                case "age":
                    this.age = value == null ? 0 : (int) value;
                    break;
                case "bytes":
                    this.bytes = value == null ? null : (byte[]) value;
                    break;
                case "id":
                    this.id = value == null ? null : (Integer) value;
                    break;
                case "jsonMap":
                    this.jsonMap = value == null ? null : (List<Map<String, String>>) value;
                    break;
                case "listMappingParameter":
                    this.listMappingParameter = value == null ? null : (List<Object>) value;
                    break;
                case "map":
                    this.map = value == null ? null : (Map<String, String>) value;
                    break;
                case "multiNames":
                    this.multiNames = value == null ? (List<Set<String>>) (java.util.List<?>) AView_.multiNames.getCollectionInstantiator().createCollection(0) : (List<Set<String>>) value;
                    break;
                case "name":
                    this.name = value == null ? null : (String) value;
                    break;
                case "names":
                    this.names = value == null ? (List<String>) (java.util.List<?>) AView_.names.getCollectionInstantiator().createCollection(0) : (List<String>) value;
                    break;
                case "optionalValue":
                    this.optionalValue = value == null ? null : (BView) value;
                    break;
                case "test":
                    this.test = value == null ? (List<X>) (java.util.List<?>) AView_.test.getCollectionInstantiator().createCollection(0) : (List<X>) value;
                    break;
                case "test2":
                    this.test2 = value == null ? null : (X) value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown attribute: " + attribute);
            }
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> Nested<X, BuilderResult> with(SingularAttribute<AView, ElementType> attribute, ElementType value) {
            if (attribute instanceof MethodAttribute) {
                return with(((MethodAttribute) attribute).getName(), value);
            } else {
                return with(((ParameterAttribute) attribute).getIndex(), value);
            }
        }

        @Override
        public <CollectionType> Nested<X, BuilderResult> with(PluralAttribute<AView, CollectionType, ?> attribute, CollectionType value) {
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
        public <ElementType> Nested<X, BuilderResult> withElement(CollectionAttribute<AView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> Nested<X, BuilderResult> withElement(SetAttribute<AView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> Nested<X, BuilderResult> withElement(ListAttribute<AView, ElementType> attribute, ElementType value) {
            getCollection(attribute).add(value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> Nested<X, BuilderResult> withListElement(ListAttribute<AView, ElementType> attribute, int index, ElementType value) {
            List<Object> list = getCollection(attribute);
            addListValue(list, index, value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <KeyType, ElementType> Nested<X, BuilderResult> withEntry(MapAttribute<AView, KeyType, ElementType> attribute, KeyType key, ElementType value) {
            Map<Object, Object> map = getMap(attribute);
            map.put(key, value);
            return (Nested<X, BuilderResult>) this;
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withSingularBuilder(String attribute) {
            switch (attribute) {
                case "optionalValue":
                    return (EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?>) (EntityViewNestedBuilder<?, ?, ?>) withOptionalValueBuilder();
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
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(SingularAttribute<AView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withSingularBuilder(((MethodAttribute) attr).getName());
            } else {
                return withSingularBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(CollectionAttribute<AView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withCollectionBuilder(((MethodAttribute) attr).getName());
            } else {
                return withCollectionBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(ListAttribute<AView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withListBuilder(((MethodAttribute) attr).getName());
            } else {
                return withListBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(ListAttribute<AView, ElementType> attr, int index) {
            if (attr instanceof MethodAttribute) {
                return withListBuilder(((MethodAttribute) attr).getName(), index);
            } else {
                return withListBuilder(((ParameterAttribute) attr).getIndex(), index);
            }
        }

        @Override
        public <ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(SetAttribute<AView, ElementType> attr) {
            if (attr instanceof MethodAttribute) {
                return withSetBuilder(((MethodAttribute) attr).getName());
            } else {
                return withSetBuilder(((ParameterAttribute) attr).getIndex());
            }
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?> withBuilder(MapAttribute<AView, KeyType, ElementType> attr, KeyType key) {
            if (attr instanceof MethodAttribute) {
                return withMapBuilder(((MethodAttribute) attr).getName(), key);
            } else {
                return withMapBuilder(((ParameterAttribute) attr).getIndex(), key);
            }
        }

        @Override
        public <KeyType, ElementType> EntityViewNestedBuilder<KeyType, ? extends EntityViewNestedBuilder<ElementType, ? extends Nested<X, BuilderResult>, ?>, ?> withBuilder(MapAttribute<AView, KeyType, ElementType> attr) {
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
