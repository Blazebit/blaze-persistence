/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.basic.mutable.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.update.basic.model.UpdatableDocumentBasicWithMapsViewBase;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentBasicWithMapsView extends UpdatableDocumentBasicWithMapsViewBase {

    @UpdatableMapping
    public Map<String, String> getStringMap();
    
    public void setStringMap(Map<String, String> stringMap);

}
