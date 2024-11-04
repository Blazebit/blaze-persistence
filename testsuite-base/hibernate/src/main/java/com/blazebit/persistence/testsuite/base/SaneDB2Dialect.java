/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base;

import org.hibernate.dialect.DB2Dialect;

public class SaneDB2Dialect extends DB2Dialect {

    @Override
    public String getSelectSequenceNextValString(String sequenceName) {
        return "next value for " + sequenceName;
    }
}
