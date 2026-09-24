/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jackson;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SomeEntity {

    @Id
    Long id;
    String name;
}
