package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.StaticMetamodel;
import com.blazebit.persistence.view.metamodel.ListAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;

import java.io.Serializable;
import javax.annotation.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
@StaticMetamodel(AView.class)
public abstract class AView_ {

    public static volatile SingularAttribute<AView, Integer> age;
    public static volatile SingularAttribute<AView, Integer> id;
    public static volatile SingularAttribute<AView, String> name;
    public static volatile ListAttribute<AView, String> names;
    public static volatile ListAttribute<AView, Serializable> test;

    public static final String AGE = "age";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String NAMES = "names";
    public static final String TEST = "test";

}
