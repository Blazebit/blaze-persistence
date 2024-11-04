/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.ExecutableElement;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class OptionalParameterScanner {

    private OptionalParameterScanner() {
    }

    public static void scan(Map<String, String> optionalParameters, ExecutableElement executableElement, Context context) {
        try {
            JavacOptionalParameterScanner.scan(optionalParameters, executableElement, context);
        } catch (Exception ex) {
            // Ignore
        }
    }
}
