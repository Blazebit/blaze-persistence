/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.collection.model;

import java.util.Set;

import com.blazebit.persistence.testsuite.treat.entity.SingleTableSub1;
import com.blazebit.persistence.view.EntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(SingleTableSub1.class)
public interface SingleTableSub1View extends SingleTableBaseView {
    
    public Integer getSub1Value();

    // Keep this here and in SingleTableSub2View to trigger ConstrainedTupleListTransformer
    public Set<SingleTableSimpleView> getChildren();

}
