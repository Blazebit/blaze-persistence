package com.blazebit.persistence.tx;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;


public class TxSupport {

    public static void transactional(EntityManager em, TxVoidWork r) {
        EntityTransaction tx = em.getTransaction();
        boolean success = false;
        try {
            tx.begin();
            r.work();
            tx.commit();
            success = true;
        } finally {
            if (!success) {
                tx.rollback();
            }
        }
    }

    public static <V> V transactional(EntityManager em, TxWork<V> c) {
        EntityTransaction tx = em.getTransaction();
        boolean success = false;
        try {
            tx.begin();
            V result = c.work();
            tx.commit();
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
