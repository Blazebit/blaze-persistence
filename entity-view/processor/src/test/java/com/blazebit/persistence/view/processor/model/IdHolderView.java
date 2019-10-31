package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.IdMapping;

import java.io.Serializable;

public interface IdHolderView<ID extends Serializable> {
    @IdMapping
    ID getId();
}
