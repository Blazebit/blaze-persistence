/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Person.class)
public interface PersonDuplicateCollectionUsageValidationView extends IdHolderView<Long> {
    
    @Mapping("ownedDocuments.id")
    public Set<Long> getOwnedDocumentIds();

    @Mapping("ownedDocuments.name")
    public Set<String> getOwnerDocumentNames();
}
