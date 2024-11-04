/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.examples.quarkus.testsuite.base;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@QuarkusTestResource(H2DatabaseTestResource.class)
public class TestResources {
}
