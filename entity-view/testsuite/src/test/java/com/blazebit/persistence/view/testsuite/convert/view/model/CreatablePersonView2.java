/*
 * Copyright 2014 - 2022 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
