/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@SpringBootApplication
public class SpringBlazeApplication {
    private SpringBlazeApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBlazeApplication.class, args);
    }
}
