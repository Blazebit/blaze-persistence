/*
 * Copyright 2014 - 2022 Blazebit.
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
package com.blazebit.persistence.examples.quarkus.testsuite.h2;

import com.blazebit.persistence.examples.quarkus.testsuite.base.AbstractQuarkusExampleTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.NativeImageTest;
import io.quarkus.test.h2.H2DatabaseTestResource;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@QuarkusTestResource(H2DatabaseTestResource.class)
@NativeImageTest
public class QuarkusExampleIT extends AbstractQuarkusExampleTest {
}
