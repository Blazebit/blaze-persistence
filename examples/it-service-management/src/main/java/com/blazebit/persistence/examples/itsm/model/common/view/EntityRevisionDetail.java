/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.common.view;

import java.time.Instant;

import com.blazebit.persistence.examples.itsm.model.common.entity.EntityRevision;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(EntityRevision.class)
public interface EntityRevisionDetail {

    @IdMapping
    Long getId();

    Instant getTimestamp();

}