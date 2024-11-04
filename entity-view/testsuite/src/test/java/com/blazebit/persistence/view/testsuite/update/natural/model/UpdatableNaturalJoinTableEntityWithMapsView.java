/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.natural.model;

import com.blazebit.persistence.testsuite.entity.NaturalIdJoinTableEntity;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@UpdatableEntityView
@EntityView(NaturalIdJoinTableEntity.class)
public interface UpdatableNaturalJoinTableEntityWithMapsView {
    
    @IdMapping
    public Long getId();

    public Long getVersion();

    public String getIsbn();

    public Map<String, BookIsbnView> getManyToManyBook();

    public void setManyToManyBook(Map<String, BookIsbnView> manyToManyBook);

}
