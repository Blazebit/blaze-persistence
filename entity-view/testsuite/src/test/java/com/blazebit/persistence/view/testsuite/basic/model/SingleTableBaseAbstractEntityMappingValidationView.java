package com.blazebit.persistence.view.testsuite.basic.model;


import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

@EntityView(SingleTableBase.class)
@CreatableEntityView
public interface SingleTableBaseAbstractEntityMappingValidationView extends IdHolderView<Long> {

}
