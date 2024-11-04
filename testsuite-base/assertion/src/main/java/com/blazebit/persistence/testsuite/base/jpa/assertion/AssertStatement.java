/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base.jpa.assertion;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface AssertStatement {
    public void validate(String query);
}
