/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
module com.blazebit.persistence.integration.jsonb {
    requires jakarta.json.bind;
    requires jakarta.json;
    requires com.blazebit.persistence.view.impl;
    requires com.blazebit.persistence.view;
    requires org.javassist;
    requires static org.eclipse.yasson;
    exports com.blazebit.persistence.integration.jsonb;
}