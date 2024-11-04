/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.PostCreate;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@CreatableEntityView(validatePersistability = false)
@UpdatableEntityView
@EntityView(Document.class)
public abstract class DocumentCreateViewWithPrimitiveArray implements DocumentInterfaceView {

    public abstract byte[] getByteArray();

    public abstract void setByteArray(byte[] byteArray);
}

