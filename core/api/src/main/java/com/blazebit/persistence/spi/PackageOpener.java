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
