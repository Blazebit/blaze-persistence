/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface ImportContext {

    ImportContext NOOP = new ImportContext() {
        @Override
        public String importType(String fqcn) {
            return fqcn;
        }

        @Override
        public String generateImports() {
            return "";
        }
    };

    String importType(String fqcn);

    String generateImports();
}
