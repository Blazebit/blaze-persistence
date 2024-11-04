/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.tx;


import javax.persistence.EntityManager;

public abstract class TxVoidWork {

    public abstract void work(EntityManager em);
}
