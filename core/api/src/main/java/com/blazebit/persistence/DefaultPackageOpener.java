/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import com.blazebit.persistence.spi.PackageOpener;

/**
 * A no-op package opener for pre-Java 9.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
class DefaultPackageOpener implements PackageOpener {

    static final PackageOpener INSTANCE = new DefaultPackageOpener();

    private DefaultPackageOpener() {
    }

    @Override
    public void openPackageIfNeeded(Class<?> targetClass, String targetPackage, Class<?> implementationClass) {
    }
}
