/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webflux.tx;

import com.blazebit.persistence.view.EntityViewManager;

import javax.persistence.EntityManager;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public interface TxVoidWork {

    void work(EntityManager em, EntityViewManager evm);
}
