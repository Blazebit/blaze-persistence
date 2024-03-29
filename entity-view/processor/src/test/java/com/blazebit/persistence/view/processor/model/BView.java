package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.PostLoad;
import com.blazebit.persistence.view.Self;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.processor.model.sub.BaseView;

import java.io.Serializable;

@EntityView(BView.class)
public abstract class BView<X extends Serializable> extends BaseView<Integer> {

    private final String capturedName;
    private String postLoadName;

    @ViewConstructor("create")
    public BView() {
        this(null);
    }

    public BView(@Self BView self) {
        this.capturedName = self == null ? null : self.getName();
    }

    @PostLoad
    void postLoad() {
        this.postLoadName = getName();
    }

    public abstract String getName();

    public abstract void setName(String name);

    public String getCapturedName() {
        return capturedName;
    }

    public String getPostLoadName() {
        return postLoadName;
    }
}
