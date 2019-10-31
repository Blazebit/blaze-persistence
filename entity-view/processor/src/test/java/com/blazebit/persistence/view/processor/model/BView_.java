package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.StaticMetamodel;
import com.blazebit.persistence.view.metamodel.SingularAttribute;

import javax.annotation.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
@StaticMetamodel(BView.class)
public abstract class BView_ {

    public static volatile SingularAttribute<BView, Integer> id;
    public static volatile SingularAttribute<BView, String> name;
    public static volatile SingularAttribute<BView, Integer> parent;

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PARENT = "parent";

}
