/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.base.bean;

import jakarta.persistence.EntityManagerFactory;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityManagerFactoryHolder {

    EntityManagerFactory getEntityManagerFactory();

}
