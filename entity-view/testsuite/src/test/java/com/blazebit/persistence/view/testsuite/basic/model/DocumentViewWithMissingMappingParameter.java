/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Document.class)
public abstract class DocumentViewWithMissingMappingParameter implements IdHolderView<Long> {

    private static final long serialVersionUID = 1L;

    private final long age;

    public DocumentViewWithMissingMappingParameter(Long age) {
        this.age = age;
    }

    public long getAge() {
        return age;
    }
}
