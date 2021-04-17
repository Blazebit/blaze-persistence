package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.StaticRelation;
import com.blazebit.persistence.view.metamodel.AttributePath;
import com.blazebit.persistence.view.metamodel.AttributePathWrapper;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.MethodSingularAttribute;
import javax.annotation.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
@StaticRelation(BView.class)
public class BViewRelation<T, A extends MethodAttribute<?, ?>> extends AttributePathWrapper<T, BView, BView> {

    public BViewRelation(AttributePath<T, BView, BView> path) {
        super(path);
    }

    public AttributePath<T, Integer, Integer> id() {
        MethodSingularAttribute<BView, Integer> attribute = BView_.id;
        return attribute == null ? getWrapped().<Integer>get("id") : getWrapped().get(attribute);
    }

    public AttributePath<T, String, String> name() {
        MethodSingularAttribute<BView, String> attribute = BView_.name;
        return attribute == null ? getWrapped().<String>get("name") : getWrapped().get(attribute);
    }

    public AttributePath<T, Integer, Integer> parent() {
        MethodSingularAttribute<BView, Integer> attribute = BView_.parent;
        return attribute == null ? getWrapped().<Integer>get("parent") : getWrapped().get(attribute);
    }

    public A attr() {
        return (A) getWrapped().getAttributes().get(getWrapped().getAttributes().size() - 1);
    }

}