/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.singular.model;

import com.blazebit.persistence.testsuite.entity.SingularListEntity;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@UpdatableEntityView
@EntityView(SingularListEntity.class)
public interface SingularListEntityView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @MappingSingular
    @UpdatableMapping
    public List<String> getList();

    public void setList(List<String> list);
}
