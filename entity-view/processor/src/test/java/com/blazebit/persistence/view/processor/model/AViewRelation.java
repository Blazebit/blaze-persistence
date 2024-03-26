package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.StaticRelation;
import com.blazebit.persistence.view.metamodel.AttributeFilterMappingPath;
import com.blazebit.persistence.view.metamodel.AttributePath;
import com.blazebit.persistence.view.metamodel.AttributePathWrapper;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.MethodListAttribute;
import com.blazebit.persistence.view.metamodel.MethodMultiListAttribute;
import com.blazebit.persistence.view.metamodel.MethodSingularAttribute;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
@StaticRelation(AView.class)
public class AViewRelation<T, A extends MethodAttribute<?, ?>> extends AttributePathWrapper<T, AView, AView> {

    public AViewRelation(AttributePath<T, AView, AView> path) {
        super(path);
    }

    public AttributePath<T, Integer, Integer> age() {
        MethodSingularAttribute<AView, Integer> attribute = AView_.age;
        return attribute == null ? getWrapped().<Integer>get("age") : getWrapped().get(attribute);
    }

    public AttributePath<T, byte[], byte[]> bytes() {
        MethodSingularAttribute<AView, byte[]> attribute = AView_.bytes;
        return attribute == null ? getWrapped().<byte[]>get("bytes") : getWrapped().get(attribute);
    }

    public AttributePath<T, Integer, Integer> id() {
        MethodSingularAttribute<AView, Integer> attribute = AView_.id;
        return attribute == null ? getWrapped().<Integer>get("id") : getWrapped().get(attribute);
    }

    public AttributePath<T, List<Object>, List<Object>> listMappingParameter() {
        MethodSingularAttribute<AView, List<Object>> attribute = AView_.listMappingParameter;
        return attribute == null ? getWrapped().<List<Object>>get("listMappingParameter") : getWrapped().get(attribute);
    }

    public AttributePath<T, Map<String, String>, Map<String, String>> map() {
        MethodSingularAttribute<AView, Map<String, String>> attribute = AView_.map;
        return attribute == null ? getWrapped().<Map<String, String>>get("map") : getWrapped().get(attribute);
    }

    public AttributePath<T, String, Set<String>> multiNames() {
        MethodMultiListAttribute<AView, String, Set<String>> attribute = AView_.multiNames;
        return attribute == null ? getWrapped().<String, Set<String>>getMulti("multiNames") : getWrapped().get(attribute);
    }

    public AttributePath<T, String, String> name() {
        MethodSingularAttribute<AView, String> attribute = AView_.name;
        return attribute == null ? getWrapped().<String>get("name") : getWrapped().get(attribute);
    }

    public AttributePath<T, String, String> names() {
        MethodListAttribute<AView, String> attribute = AView_.names;
        return attribute == null ? getWrapped().<String>get("names") : getWrapped().get(attribute);
    }

    public BViewRelation<T, MethodSingularAttribute<AView, BView>> optionalValue() {
        BViewRelation<AView, MethodSingularAttribute<AView, BView>> relation = AView_.optionalValue;
        return new BViewRelation<>(relation == null ? getWrapped().<BView>get("optionalValue") : getWrapped().get(relation));
    }

    public AttributePath<T, Serializable, Serializable> test() {
        MethodListAttribute<AView, Serializable> attribute = AView_.test;
        return attribute == null ? getWrapped().<Serializable>get("test") : getWrapped().get(attribute);
    }

    public AttributePath<T, Serializable, Serializable> test2() {
        MethodSingularAttribute<AView, Serializable> attribute = AView_.test2;
        return attribute == null ? getWrapped().<Serializable>get("test2") : getWrapped().get(attribute);
    }

    public A attr() {
        return (A) getWrapped().getAttributes().get(getWrapped().getAttributes().size() - 1);
    }

    public AttributeFilterMappingPath<T, Integer> id_filter() {
        MethodSingularAttribute<AView, Integer> attribute = AView_.id;
        return attribute == null ? new AttributeFilterMappingPath<>(getWrapped().get("id"), "") : new AttributeFilterMappingPath<>(getWrapped().get(attribute), AView_.id_filter);
    }

    public AttributeFilterMappingPath<T, String> name_filter() {
        MethodSingularAttribute<AView, String> attribute = AView_.name;
        return attribute == null ? new AttributeFilterMappingPath<>(getWrapped().get("name"), "") : new AttributeFilterMappingPath<>(getWrapped().get(attribute), AView_.name_filter);
    }
}
