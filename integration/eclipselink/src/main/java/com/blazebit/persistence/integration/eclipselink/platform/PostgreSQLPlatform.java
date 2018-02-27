/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
