/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.deltaspike.data.base {
    requires transitive jakarta.inject;
    requires transitive deltaspike.core.api;
    requires transitive com.blazebit.persistence.view;
    requires transitive com.blazebit.persistence.criteria.impl;
    requires transitive deltaspike.data.module.api;
    requires transitive deltaspike.data.module.impl;
    requires transitive com.blazebit.persistence.integration.deltaspike.data;
    exports com.blazebit.persistence.deltaspike.data.base.builder;
    exports com.blazebit.persistence.deltaspike.data.base.builder.part;
    exports com.blazebit.persistence.deltaspike.data.base.builder.postprocessor;
    exports com.blazebit.persistence.deltaspike.data.base.criteria;
    exports com.blazebit.persistence.deltaspike.data.base.handler;
}