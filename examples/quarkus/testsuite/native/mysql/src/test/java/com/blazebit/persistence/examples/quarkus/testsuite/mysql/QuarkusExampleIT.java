/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.examples.quarkus.testsuite.mysql;

import com.blazebit.persistence.examples.quarkus.testsuite.base.AbstractQuarkusExampleTest;
import io.quarkus.test.junit.NativeImageTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.matchesPattern;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@NativeImageTest
public class QuarkusExampleIT extends AbstractQuarkusExampleTest {
}
