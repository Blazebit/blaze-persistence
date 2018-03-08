/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class DocumentViewAbstractClass implements DocumentViewInterface {

    private static final long serialVersionUID = 1L;

    private final long age;
    private final Integer contactPersonNumber;
    private final String optionalParameter;

    public DocumentViewAbstractClass(
        @Mapping("age + 1") Long age,
        @MappingParameter("contactPersonNumber") Integer contactPersonNumber,
        @MappingParameter("optionalParameter") String optionalParameter
    ) {
        this.age = age;
        this.contactPersonNumber = contactPersonNumber;
        this.optionalParameter = optionalParameter;
    }

    public long getAge() {
        return age;
    }

    public Integer getContactPersonNumber() {
        return contactPersonNumber;
    }

    public String getOptionalParameter() {
        return optionalParameter;
    }

    @Mapping("name")
    abstract String getDefaultName();
    abstract void setDefaultName(String defaultName);

    public abstract EntityViewManager getEntityViewManager();

    public String withName(String name) {
        String oldName = getDefaultName();
        setDefaultName(name);
        return oldName;
    }
}
