/*
 * Copyright 2014 - 2023 Blazebit.
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

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
@Target({ TYPE, FIELD, PARAMETER, PACKAGE })
@Retention(RUNTIME)
@Documented
@Qualifier
@Repeatable(BlazePersistenceInstance.List.class)
public @interface BlazePersistenceInstance {

    String DEFAULT = BlazePersistenceInstanceUtil.DEFAULT_BLAZE_PERSISTENCE_NAME;

    String value();

    /**
     * @author Moritz Becker
     * @since 1.6.0
     */
    class BlazePersistenceInstanceLiteral extends AnnotationLiteral<BlazePersistenceInstance> implements BlazePersistenceInstance {

        private String name;

        public BlazePersistenceInstanceLiteral(String name) {
            this.name = name;
        }

        @Override
        public String value() {
            return name;
        }
    }

    /**
     * @author Moritz Becker
     * @since 1.6.0
     */
    @Target(PACKAGE)
    @Retention(RUNTIME)
    @Documented
    @interface List {

        BlazePersistenceInstance[] value();
    }
}
