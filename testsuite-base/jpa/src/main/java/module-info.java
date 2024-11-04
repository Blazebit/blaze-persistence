/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.testsuite.base.jpa {
    requires com.blazebit.persistence.core.impl;
    requires com.blazebit.persistence.core;
    requires HikariCP.java7;
    requires datasource.proxy;
    requires junit;
    requires jakarta.persistence;
    requires io.github.classgraph;
    requires org.apache.httpcomponents.core5.httpcore5;
    exports com.blazebit.persistence.testsuite.base.jpa;
    exports com.blazebit.persistence.testsuite.base.jpa.category;
    exports com.blazebit.persistence.testsuite.base.jpa.cleaner;
}