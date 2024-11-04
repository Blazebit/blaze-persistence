/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.PostCreate;
import com.blazebit.persistence.view.testsuite.proxy.model.IdHolderView;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Document.class)
public interface DocumentJava8View extends IdHolderView<Long> {

    @PostCreate
    default void init() {
        setName("INIT");
    }

    public String getName();

    public void setName(String name);
}
