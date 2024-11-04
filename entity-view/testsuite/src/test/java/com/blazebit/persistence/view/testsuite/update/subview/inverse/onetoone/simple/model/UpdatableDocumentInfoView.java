/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.onetoone.simple.model;

import com.blazebit.persistence.testsuite.entity.DocumentInfoSimple;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(DocumentInfoSimple.class)
public interface UpdatableDocumentInfoView extends DocumentInfoIdView {

    String getSomeInfo();
    void setSomeInfo(String someInfo);

}
