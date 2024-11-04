/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.basic.creatableonly.model;

import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.update.basic.model.UpdatableDocumentBasicWithCollectionsViewBase;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface UpdatableDocumentBasicWithCollectionsView extends UpdatableDocumentBasicWithCollectionsViewBase {

    @UpdatableMapping(updatable = false, cascade = { CascadeType.PERSIST })
    public List<String> getStrings();
    
    public void setStrings(List<String> strings);

}
