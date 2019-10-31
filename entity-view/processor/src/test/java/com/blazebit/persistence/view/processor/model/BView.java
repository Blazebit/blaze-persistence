package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.processor.model.sub.BaseView;

import java.io.Serializable;

@EntityView(BView.class)
public abstract class BView<X extends Serializable> extends BaseView<Integer> {
    public abstract String getName();

    public abstract void setName(String name);
}
