/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.hateoas.webmvc.tx;

import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

/**
 * @author Moritz Becker
 * @author Eugen Mayer
 * @since 1.6.9
 */
@Service
@Transactional
public class TransactionalWorkService {

    @Autowired
    private EntityManager em;
    @Autowired
    private EntityViewManager evm;

    public <V> V txGet(TxWork<V> work) {
        return work.work(em, evm);
    }

    public void doTxWork(TxVoidWork work) {
        work.work(em, evm);
    }

}
