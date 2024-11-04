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
public class DocumentViewModel {

    private String name;
    private String ownerName;
    private String firstLocalizedItem;
    private String partnerDocumentName;

    public DocumentViewModel(String name) {
        this.name = name;
    }

    public DocumentViewModel(String name, String ownerName, String firstLocalizedItem, String secondLocalizedItem) {
        this.name = name;
        this.ownerName = ownerName;
        this.firstLocalizedItem = firstLocalizedItem;
        this.partnerDocumentName = secondLocalizedItem;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getFirstLocalizedItem() {
        return firstLocalizedItem;
    }

    public String getPartnerDocumentName() {
        return partnerDocumentName;
    }
}
