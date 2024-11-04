/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.convert.view.model.sub;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.Self;

import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Document.class)
public abstract class DocumentCloneParentView implements Serializable {

    private final String param2;

    public DocumentCloneParentView(@Self DocumentCloneParentView self) {
        this.param2 = self.getParam();
    }

    @IdMapping
    public abstract Long getId();

    @MappingParameter("test")
    protected abstract String getParam();

    public String getParam2() {
        return param2;
    }
}
