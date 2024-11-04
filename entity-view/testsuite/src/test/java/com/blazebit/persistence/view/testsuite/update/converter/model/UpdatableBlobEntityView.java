/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.converter.model;

import com.blazebit.persistence.testsuite.entity.BlobEntity;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.sql.Blob;
import java.time.Instant;
import java.util.Date;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(BlobEntity.class)
public interface UpdatableBlobEntityView {

    @IdMapping
    public Long getId();

    public Long getVersion();

    public String getName();

    public void setName(String name);

    public Instant getLastModified();

    public void setLastModified(Instant date);

    public Blob getBlob();

    public void setBlob(Blob blob);

}
