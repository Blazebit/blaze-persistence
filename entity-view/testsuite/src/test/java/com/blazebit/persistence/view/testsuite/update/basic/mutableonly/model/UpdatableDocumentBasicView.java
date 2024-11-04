/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.basic.mutableonly.model;

import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.update.basic.model.UpdatableDocumentBasicViewBase;

import java.util.Date;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface UpdatableDocumentBasicView extends UpdatableDocumentBasicViewBase {

    @UpdatableMapping(updatable = false, cascade = { CascadeType.UPDATE })
    public Date getLastModified();
    
    public void setLastModified(Date date);

}
