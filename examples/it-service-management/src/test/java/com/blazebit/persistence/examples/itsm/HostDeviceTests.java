/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm;

import com.blazebit.persistence.examples.itsm.model.host.view.HostDeviceWithItems;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import com.blazebit.persistence.view.EntityViewManager;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@DataJpaTest(showSql = false)
@ContextConfiguration(classes = BlazeConfiguration.class)
public class HostDeviceTests {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EntityViewManager evm;

    @Test
    public void lastValueProviderTest() {
        this.evm.find(this.em.getEntityManager(), HostDeviceWithItems.class, 123L);
    }

}
