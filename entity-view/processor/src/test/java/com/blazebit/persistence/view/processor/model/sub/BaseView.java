package com.blazebit.persistence.view.processor.model.sub;

import com.blazebit.persistence.view.processor.model.IdHolderView;

import java.io.Serializable;

public abstract class BaseView<ID extends Serializable> implements IdHolderView<ID> {

    abstract ID getParent();

    abstract void setParent(ID parent);
}
