/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.model;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class DocumentCount {

    private final Long count;

    public DocumentCount(Long count) {
        this.count = count;
    }

    public Long getCount() {
        return count;
    }
}
