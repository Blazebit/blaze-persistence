/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.view.spring {
    requires java.sql;
    requires jakarta.persistence;
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires com.blazebit.persistence.view;
    requires spring.tx;
    requires jakarta.transaction;
    exports com.blazebit.persistence.integration.view.spring;
    exports com.blazebit.persistence.integration.view.spring.impl to spring.beans;
    opens com.blazebit.persistence.integration.view.spring.impl to spring.core, spring.context;
    // This also opens up the XSD file
    opens com.blazebit.persistence.integration.view.spring;
    provides com.blazebit.persistence.view.spi.TransactionAccessFactory with com.blazebit.persistence.integration.view.spring.impl.SpringTransactionAccessFactory;
}