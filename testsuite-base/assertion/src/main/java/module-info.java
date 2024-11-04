/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.testsuite.assertion {
    requires net.sf.jsqlparser;
    requires junit;
    requires com.blazebit.persistence.testsuite.base.jpa;
    requires org.opentest4j;
    exports com.blazebit.persistence.testsuite.base.jpa.assertion;
}