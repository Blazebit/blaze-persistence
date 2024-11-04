/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.runtime;

import com.blazebit.persistence.view.EntityViewManager;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import javassist.CannotCompileException;
import javassist.CtClass;

import java.io.IOException;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@TargetClass(className = "com.blazebit.persistence.view.impl.proxy.ProxyFactory")
final class ProxyFactory {

    @Substitute
    private <T> Class<? extends T> defineOrGetClass(EntityViewManager entityViewManager, boolean unsafe, Class<?> clazz, Class<?> neighbourClazz, CtClass cc) throws IOException, IllegalAccessException, NoSuchFieldException, CannotCompileException {
        throw new RuntimeException("Unsupported in the native compiler.");
    }

}
