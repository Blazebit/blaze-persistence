/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.deltaspike.data.rest {
    requires transitive jakarta.ws.rs;
    requires transitive com.blazebit.persistence.integration.deltaspike.data;
    exports com.blazebit.persistence.deltaspike.data.rest;
}