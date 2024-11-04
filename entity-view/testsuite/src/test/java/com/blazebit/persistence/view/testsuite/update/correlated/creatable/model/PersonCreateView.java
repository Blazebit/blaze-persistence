/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.correlated.creatable.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(Person.class)
@CreatableEntityView
public interface PersonCreateView extends UpdatablePersonView {

    public void setName(String name);

    public DocumentIdView getPartnerDocument();

    public void setPartnerDocument(DocumentIdView partnerDocument);

    public Map<Integer, String> getLocalized();
}
