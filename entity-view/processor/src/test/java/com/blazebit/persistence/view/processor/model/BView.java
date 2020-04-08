package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Self;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.processor.model.sub.BaseView;

import java.io.Serializable;

@EntityView(BView.class)
public abstract class BView<X extends Serializable> extends BaseView<Integer> {

    private final String capturedName;

    @ViewConstructor("create")
    public BView() {
        this(null);
    }

    public BView(@Self BView self) {
        this.capturedName = self == null ? null : self.getName();
    }

    public abstract String getName();

    public abstract void setName(String name);

    public String getCapturedName() {
        return capturedName;
    }
}
