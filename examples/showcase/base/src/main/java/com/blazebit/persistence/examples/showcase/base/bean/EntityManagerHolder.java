/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.base.bean;

import javax.persistence.EntityManager;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface EntityManagerHolder {

    EntityManager getEntityManager();

}
