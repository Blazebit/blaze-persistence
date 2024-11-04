/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Person.class)
public interface PersonValidationView extends IdHolderView<Long> {
    
    // map collection target entity property to collection of property type 
    @Mapping("ownedDocuments.id")
    public Set<Long> getOwnedDocumentIds();

    // The case when a collection is mapped, but we are sure there is only one element
    @Mapping("partnerDocument.versions.url")
    public String getVersionUrl();
    
    @Mapping("CONCAT(COALESCE(name,'-'),' ',COALESCE(partnerDocument.name,'-'))")
    public abstract String getCoalescingConcat();
    
    // Parameters are totally ok
    @Mapping("COALESCE(partnerDocument.contacts[:firstContact].name, partnerDocument.contacts[partnerDocument.defaultContact].name)")
    public String getName();
    
    @Mapping("CASE WHEN age = 9 and id IS NOT NULL THEN 0 ELSE 1 END")
    public abstract Integer getNestedCaseWhen();
}
