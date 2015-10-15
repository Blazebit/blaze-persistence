package com.blazebit.persistence.impl;

import com.blazebit.persistence.spi.SetOperationType;


class SetOperationInfo {
	final SetOperationType type;
	final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> criteriaBuilder;
	
    public SetOperationInfo(SetOperationType type, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> criteriaBuilder) {
        this.type = type;
        this.criteriaBuilder = criteriaBuilder;
    }
}