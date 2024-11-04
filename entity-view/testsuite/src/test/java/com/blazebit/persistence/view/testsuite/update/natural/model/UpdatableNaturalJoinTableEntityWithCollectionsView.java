/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.natural.model;

import com.blazebit.persistence.testsuite.entity.NaturalIdJoinTableEntity;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@UpdatableEntityView
@EntityView(NaturalIdJoinTableEntity.class)
public interface UpdatableNaturalJoinTableEntityWithCollectionsView {
    
    @IdMapping
    public Long getId();

    public Long getVersion();

    public String getIsbn();

    public Set<BookIsbnView> getOneToManyBook();
    
    public void setOneToManyBook(Set<BookIsbnView> oneToManyBook);

}
