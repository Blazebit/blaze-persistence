/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Document.class)
public abstract class UnsafeDocumentClassView implements DocumentInterfaceView {

    private static final long serialVersionUID = 1L;
    
    private final long age;
    private final Integer contactPersonNumber;
    private final String testName;

    public UnsafeDocumentClassView(
        @Mapping("age + 1") Long age,
        @MappingParameter("contactPersonNumber") Integer contactPersonNumber
    ) {
        this.testName = getName();
        this.age = age;
        this.contactPersonNumber = contactPersonNumber;
    }
    
    public String getUnsafeName() {
        return testName;
    }

    public long getAge() {
        return age;
    }

    public Integer getContactPersonNumber() {
        return contactPersonNumber;
    }
}
