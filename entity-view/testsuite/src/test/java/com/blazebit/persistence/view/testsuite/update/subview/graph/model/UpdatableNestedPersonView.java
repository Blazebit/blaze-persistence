/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.graph.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(Person.class)
public interface UpdatableNestedPersonView extends UpdatablePersonView {

    public UpdatableFriendPersonView getFriend();

    public void setFriend(UpdatableFriendPersonView friend);

    public UpdatableSimpleDocumentView getPartnerDocument();

    public void setPartnerDocument(UpdatableSimpleDocumentView partnerDocument);

}
