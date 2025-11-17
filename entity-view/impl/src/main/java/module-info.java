/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.view.impl {
    requires java.naming;
    requires org.javassist;
    requires com.blazebit.persistence.core.parser;
    requires com.blazebit.persistence.view;
    requires jakarta.transaction;
    requires com.blazebit.persistence.core.impl;
    requires com.blazebit.common.utils;
    exports com.blazebit.persistence.view.impl;
    exports com.blazebit.persistence.view.impl.accessor;
    exports com.blazebit.persistence.view.impl.change;
    exports com.blazebit.persistence.view.impl.collection;
    exports com.blazebit.persistence.view.impl.entity;
    exports com.blazebit.persistence.view.impl.filter;
    exports com.blazebit.persistence.view.impl.macro;
    exports com.blazebit.persistence.view.impl.mapper;
    exports com.blazebit.persistence.view.impl.metamodel;
    exports com.blazebit.persistence.view.impl.metamodel.analysis;
    exports com.blazebit.persistence.view.impl.metamodel.attribute;
    exports com.blazebit.persistence.view.impl.objectbuilder;
    exports com.blazebit.persistence.view.impl.objectbuilder.mapper;
    exports com.blazebit.persistence.view.impl.objectbuilder.transformator;
    exports com.blazebit.persistence.view.impl.objectbuilder.transformer;
    exports com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;
    exports com.blazebit.persistence.view.impl.proxy;
    exports com.blazebit.persistence.view.impl.tx;
    exports com.blazebit.persistence.view.impl.type;
    exports com.blazebit.persistence.view.impl.update;
    exports com.blazebit.persistence.view.impl.update.flush;
    exports com.blazebit.persistence.view.impl.update.listener;
    provides com.blazebit.persistence.view.spi.EntityViewConfigurationProvider with com.blazebit.persistence.view.impl.EntityViewConfigurationProviderImpl;
    uses com.blazebit.persistence.view.spi.TransactionAccessFactory;
}