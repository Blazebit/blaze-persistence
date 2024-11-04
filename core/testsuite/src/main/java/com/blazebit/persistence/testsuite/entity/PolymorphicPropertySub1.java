/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@AssociationOverrides({
    @AssociationOverride(name = "base", joinColumns = @JoinColumn(name = "base_sub_1"))
})
public class PolymorphicPropertySub1 extends PolymorphicPropertyMapBase<PolymorphicSub1> {
    private static final long serialVersionUID = 1L;

    public PolymorphicPropertySub1() {
    }
}
