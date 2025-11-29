/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.deltaspike.data.impl {
    requires transitive com.blazebit.persistence.integration.deltaspike.data;
    requires deltaspike.partial.bean.module.impl;
    requires deltaspike.core.api;
    requires com.blazebit.persistence.integration.deltaspike.data.base;
    requires deltaspike.jpa.module.api;
    requires com.blazebit.persistence.view;
    requires com.blazebit.common.utils;
    requires jakarta.cdi;
    requires deltaspike.jpa.module.impl;
    requires com.blazebit.persistence.core.parser;
    requires deltaspike.data.module.api;
    exports com.blazebit.persistence.deltaspike.data.impl;
    provides jakarta.enterprise.inject.spi.Extension with com.blazebit.persistence.deltaspike.data.impl.CustomPartialBeanBindingExtension;
}