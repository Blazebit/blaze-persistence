/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
