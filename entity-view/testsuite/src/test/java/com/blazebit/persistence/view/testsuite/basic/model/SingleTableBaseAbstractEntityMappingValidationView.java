/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Philipp Eder
 * @since 1.6.0
 */
@EntityView(SingleTableBase.class)
@CreatableEntityView
public interface SingleTableBaseAbstractEntityMappingValidationView extends IdHolderView<Long> {

}
