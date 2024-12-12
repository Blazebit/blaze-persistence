/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.base;

import java.lang.reflect.Method;
import org.springframework.data.domain.Pageable;

public final class PageableUtil {

    private static final Pageable UNPAGED;

    static {
        Pageable unpaged = null;
        try {
            Method unpagedMethod = Class.forName("org.springframework.data.domain.Pageable").getMethod("unpaged");
            unpaged = (Pageable) unpagedMethod.invoke(null);
        } catch (Exception e) {
            // ignore
        }
        UNPAGED = unpaged;
    }

    private PageableUtil() {
    }

    public static boolean isUnpaged(Pageable pageable) {
        return pageable == null || pageable == UNPAGED;
    }
}
