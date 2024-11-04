/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.basic.creatableonly.model;

import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.update.basic.model.UpdatableDocumentBasicWithMapsViewBase;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface UpdatableDocumentBasicWithMapsView extends UpdatableDocumentBasicWithMapsViewBase {

    @UpdatableMapping(updatable = false, cascade = { CascadeType.PERSIST })
    public Map<String, String> getStringMap();
    
    public void setStringMap(Map<String, String> stringMap);

}
