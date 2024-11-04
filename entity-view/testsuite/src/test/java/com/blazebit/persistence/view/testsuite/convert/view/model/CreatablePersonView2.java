/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.convert.view.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.ConvertOption;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostConvert;
import com.blazebit.persistence.view.UpdatableMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
@EntityView(Person.class)
@CreatableEntityView
public abstract class CreatablePersonView2 implements PersonView {

    @PostConvert
    void postConvert() {
        setFriend(getFriend() == null ? null : evm().convertWith(getFriend(), CreatablePersonView.class, ConvertOption.CREATE_NEW).excludeAttribute("id").convert());
    }

    abstract EntityViewManager evm();

    public abstract void setName(String name);

    @UpdatableMapping(cascade = CascadeType.PERSIST)
    public abstract SimplePersonView getFriend();
    abstract void setFriend(SimplePersonView friend);
}
