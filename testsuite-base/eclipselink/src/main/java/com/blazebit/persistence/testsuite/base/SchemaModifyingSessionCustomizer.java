/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.testsuite.base;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class SchemaModifyingSessionCustomizer implements SessionCustomizer {

    private static String schemaName;

    public static void setSchemaName(String schemaName) {
        SchemaModifyingSessionCustomizer.schemaName = schemaName;
    }

    @Override
    public void customize(Session session) throws Exception {
        if (schemaName != null) {
            session.getLogin().setTableQualifier(this.schemaName);
        }
    }
}
