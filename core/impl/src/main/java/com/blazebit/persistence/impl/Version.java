/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class Version {

    public static final String PROJECT_NAME = "Blaze-Persistence";
    private static final String VERSION;
    private static final String CODENAME;

    static {
        Properties properties = new Properties();
        InputStream propertiesStream = Version.class.getClassLoader().getResourceAsStream("META-INF/blaze-persistence.properties");
        if (propertiesStream == null) {
            VERSION = "0.0.0-SNAPSHOT";
            CODENAME = "";
        } else {
            try {
                properties.load(propertiesStream);
                VERSION = properties.getProperty("version");
                CODENAME = properties.getProperty("codename");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Version() {
    }

    public static String getVersion() {
        return VERSION;
    }

    public static String printVersion() {
        return PROJECT_NAME + " '" + CODENAME + "' " + VERSION;
    }
}
