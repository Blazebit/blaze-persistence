package com.blazebit.persistence.impl;

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
