/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.unmapped.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(Person.class)
public interface UpdatablePersonView extends PersonIdView {

    public String getName();

    public void setName(String name);

    public Map<Integer, String> getLocalized();

    public void setLocalized(Map<Integer, String> localized);

}
