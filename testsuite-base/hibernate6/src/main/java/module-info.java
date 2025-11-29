/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.testsuite.base {
    requires java.naming;
	requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires com.blazebit.persistence.testsuite.base.jpa;
    requires com.blazebit.persistence.core;
    exports com.blazebit.persistence.testsuite.base;
}