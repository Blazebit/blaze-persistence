/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import java.io.Serializable;
import java.util.Map;

import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
interface ContactHolderView<T> extends Serializable {

    @UpdatableMapping(updatable = false)
    public Map<T, Person> getContacts();

    public void setContacts(Map<T, Person> localized);
}
