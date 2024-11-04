/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.hateoas.webmvc.tx;

import com.blazebit.persistence.view.EntityViewManager;

import jakarta.persistence.EntityManager;

/**
 * @author Moritz Becker
 * @author Eugen Mayer
 * @since 1.6.9
 */
public interface TxWork<V> {

    V work(EntityManager em, EntityViewManager evm);
}
