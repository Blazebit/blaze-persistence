/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.tx;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;


public class TxSupport {

    private TxSupport() {
    }

    public static void transactional(EntityManager em, TxVoidWork r) {
        EntityTransaction tx = em.getTransaction();
        boolean success = false;
        try {
            tx.begin();
            r.work(em);
            if (tx.getRollbackOnly()) {
                tx.rollback();
            } else {
                tx.commit();
            }
            success = true;
        } finally {
            if (!success) {
                try {
                    tx.rollback();
                } catch (RuntimeException ex) {
                    // Ignore
                }
            }
        }
    }

    public static <V> V transactional(EntityManager em, TxWork<V> c) {
        EntityTransaction tx = em.getTransaction();
        boolean success = false;
        try {
            tx.begin();
            V result = c.work(em);
            if (tx.getRollbackOnly()) {
                tx.rollback();
            } else {
                tx.commit();
            }
            success = true;
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (!success) {
                tx.rollback();
            }
        }
    }
}
