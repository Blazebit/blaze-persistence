/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.simple.creatable.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.update.embeddable.simple.model.UpdatableDocumentEmbeddableWithMapsViewBase;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentEmbeddableWithMapsView extends UpdatableDocumentEmbeddableWithMapsViewBase {

    @UpdatableMapping(cascade = { CascadeType.PERSIST })
    public Map<String, NameObject> getNameMap();

    public void setNameMap(Map<String, NameObject> nameMap);

}
