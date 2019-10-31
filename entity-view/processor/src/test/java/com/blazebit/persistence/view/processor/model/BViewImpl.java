package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.processor.model.sub.BaseView_com_blazebit_persistence_view_processor_model_BView;

import java.io.Serializable;
import javax.annotation.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
public class BViewImpl<X extends Serializable> extends BaseView_com_blazebit_persistence_view_processor_model_BView<X> {

    private final Integer id;
    private String name;
    private Integer parent;

    public BViewImpl() {
        this.id = null;
        this.name = null;
        this.parent = null;
    }

    public BViewImpl(Integer id) {
        this.id = id;
        this.name = null;
        this.parent = null;
    }

    public BViewImpl(Integer id, String name, Integer parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getParent() {
        return parent;
    }

    @Override
    public void setParent(Integer parent) {
        this.parent = parent;
    }
}
