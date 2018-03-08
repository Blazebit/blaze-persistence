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

package com.blazebit.persistence.impl;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class Version {

    public static final String PROJECT_NAME = "Blaze-Persistence";

    private Version() {
    }

    public static String getVersion() {
        return Injected.getVersion();
    }

    public static String printVersion() {
        return PROJECT_NAME + " '" + Injected.getCodename() + "' " + Injected.getVersion();
    }

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    static class Injected {

        static String getVersion() {
            // Will be replaced by the Maven Injection plugin
            return "0.0.0-SNAPSHOT";
        }

        static String getCodename() {
            // Will be replaced by the Maven Injection plugin
            return "";
        }
    }
}
