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
 * @since 1.6.0
 */
@UpdatableEntityView
@EntityView(NaturalIdJoinTableEntity.class)
public interface UpdatableNaturalJoinTableEntityWithIdViewCollectionsView {
    
    @IdMapping
    public Long getId();

    public Long getVersion();

    public String getIsbn();

    public Set<BookIdView> getOneToManyBook();
    
    public void setOneToManyBook(Set<BookIdView> oneToManyBook);

}
