/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
