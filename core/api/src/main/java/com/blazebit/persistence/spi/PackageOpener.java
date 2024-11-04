/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * A package opener can be used to propagate module visibilities.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface PackageOpener {

    /**
     * A no-op package opener.
     */
    public static final PackageOpener NOOP = new PackageOpener() {
        @Override
        public void openPackageIfNeeded(Class<?> targetClass, String targetPackage, Class<?> implementationClass) {
        }
    };

    /**
     * Opens the given package of the module of the target class to the module of the implementation class.
     *
     * @param targetClass The class by which to obtain the module that should be opened.
     * @param targetPackage The package of the module that should be opened.
     * @param implementationClass The class by which to obtain the module to which the given package should be opened.
     */
    public void openPackageIfNeeded(Class<?> targetClass, String targetPackage, Class<?> implementationClass);
}
