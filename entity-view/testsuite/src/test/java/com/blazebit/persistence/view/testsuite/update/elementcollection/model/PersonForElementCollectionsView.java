/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.elementcollection.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForElementCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForElementCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
@UpdatableEntityView
@EntityView(PersonForElementCollections.class)
public interface PersonForElementCollectionsView {

    String getFullname();
    void setFullname(String fullname);

    DocumentForElementIdView getPartnerDocument();
    void setPartnerDocument(DocumentForElementIdView partnerDocument);
}
