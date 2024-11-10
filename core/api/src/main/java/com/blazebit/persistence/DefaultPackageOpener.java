/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import com.blazebit.persistence.spi.PackageOpener;

/**
 * A package opener that works with Java 9 modules.
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
        Module targetModule = targetClass.getModule();
        if (!targetModule.isOpen(targetPackage, implementationClass.getModule())) {
            targetModule.addOpens(targetPackage, implementationClass.getModule());
        }
    }
}
