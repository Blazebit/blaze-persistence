/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.tx;


import javax.persistence.EntityManager;

public abstract class TxWork<V> {

    public abstract V work(EntityManager em) throws Exception;
}
