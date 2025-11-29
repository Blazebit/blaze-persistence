package com.blazebit.persistence.view.processor.model.sub;

@javax.annotation.processing.Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
public abstract class BaseView_com_blazebit_persistence_view_processor_model_BView<ID extends java.io.Serializable, X extends java.io.Serializable> extends com.blazebit.persistence.view.processor.model.BView<X> {

    public BaseView_com_blazebit_persistence_view_processor_model_BView() {
        super();
    }

    public BaseView_com_blazebit_persistence_view_processor_model_BView(com.blazebit.persistence.view.processor.model.BView self) {
        super(self);
    }

    public abstract java.lang.Integer getParent();

    public abstract void setParent(java.lang.Integer arg0);
    public abstract void setParent(ID arg0);
}
