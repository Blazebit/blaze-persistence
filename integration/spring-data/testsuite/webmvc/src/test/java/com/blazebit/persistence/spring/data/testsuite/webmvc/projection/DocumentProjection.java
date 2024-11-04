/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.spring.data.testsuite.webmvc.projection;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
public interface DocumentProjection extends DocumentIdProjection {

    @Value("#{target.name + ' ' + target.age}")
    String getNameAndAge();
}
