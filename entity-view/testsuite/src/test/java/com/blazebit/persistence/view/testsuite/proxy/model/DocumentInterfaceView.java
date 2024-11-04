/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Document.class)
public interface DocumentInterfaceView extends IdHolderView<Long>, ContactHolderView<Integer> {

    public String getName();

    @Mapping("contacts[:contactPersonNumber]")
    public Person getMyContactPerson();

    @Mapping("contacts[1]")
    public Person getFirstContactPerson();
}
