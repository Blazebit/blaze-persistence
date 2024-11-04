/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.eclipselink.platform;

import java.io.IOException;
import java.io.Writer;

/**
 * The {@link org.eclipse.persistence.platform.database.PostgreSQLPlatform} renders '1' and '0' literals for true
 * and false, respectively. This causes problems since eclipselink reads such values as String from the result set.
 * This platform overwrites this behavior to use standard true and false literals.
 *
 * To use this platform, create a file org/eclipse/persistence/internal/helper/VendorNameToPlatformMapping.properties
 * and add the following line:
 * PostgreSQL.*=com.blazebit.persistence.impl.eclipselink.platform.PostgreSQLPlatform
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class PostgreSQLPlatform extends org.eclipse.persistence.platform.database.PostgreSQLPlatform {

    @Override
    protected void appendBoolean(Boolean bool, Writer writer) throws IOException {
        if (bool.booleanValue()) {
            writer.write("true");
        } else {
            writer.write("false");
        }
    }
}
