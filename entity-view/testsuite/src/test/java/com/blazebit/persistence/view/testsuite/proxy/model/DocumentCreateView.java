/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.PostCreate;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@CreatableEntityView(validatePersistability = false)
@UpdatableEntityView
@EntityView(Document.class)
public abstract class DocumentCreateView implements DocumentInterfaceView {

    private boolean postCreated;

    @PostCreate
    void postCreate() {
        postCreated = true;
    }

    public boolean isPostCreated() {
        return postCreated;
    }

    @UpdatableMapping
    public abstract Map<Integer, Person> getContacts();

    public abstract void setContacts(Map<Integer, Person> localized);
}
