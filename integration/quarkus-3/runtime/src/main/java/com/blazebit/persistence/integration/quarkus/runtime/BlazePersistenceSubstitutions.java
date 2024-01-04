/*
 * Copyright 2014 - 2024 Blazebit.
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
