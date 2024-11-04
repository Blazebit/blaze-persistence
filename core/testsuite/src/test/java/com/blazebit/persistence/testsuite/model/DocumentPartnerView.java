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
public class DocumentPartnerView {

    private final Object partners;

    public DocumentPartnerView(Object partners, Object localized) {
        this.partners = partners;
    }

    public Object getPartners() {
        return partners;
    }
}
