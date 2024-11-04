/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.treat.model;

import javax.persistence.Entity;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Entity
public class ContainerItem1 extends BaseContainerItem {
    public ContainerItem1() {
    }

    public ContainerItem1(Long id) {
        super(id);
    }
}
