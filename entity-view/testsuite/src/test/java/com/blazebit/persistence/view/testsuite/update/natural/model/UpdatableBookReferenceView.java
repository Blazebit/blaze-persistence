/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.natural.model;

import com.blazebit.persistence.testsuite.entity.BookISBNReferenceEntity;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@UpdatableEntityView
@EntityView(BookISBNReferenceEntity.class)
public interface UpdatableBookReferenceView {
    
    @IdMapping
    public Long getId();

    public Long getVersion();

    public BookIsbnView getBook();

    public void setBook(BookIsbnView book);

    public BookIdView getBookNormal();

    public void setBookNormal(BookIdView bookNormal);
}
