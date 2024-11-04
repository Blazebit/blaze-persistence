/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model;

import com.blazebit.persistence.view.IdMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface PersonBaseView {

    @IdMapping
    public Long getId();

    public String getName();
}
