/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Person;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@EntityView(Person.class)
public interface InvalidJoinOrderPersonView extends IdHolderView<Long> {

    @Mapping("COALESCE(partnerDocument.contacts[1].name, partnerDocument.contacts[partnerDocument.defaultContact].name)")
    String getPartnerDocumentContactName();

    @Mapping("COALESCE(friend.localized[1], friend.localized[partnerDocument.defaultContact])")
    String getFriendName();
}
