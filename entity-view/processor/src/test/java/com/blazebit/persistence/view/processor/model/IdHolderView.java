package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.filter.EqualFilter;

import java.io.Serializable;

public interface IdHolderView<ID extends Serializable> extends Serializable {
    @IdMapping
    @AttributeFilter(EqualFilter.class)
    ID getId();
}
