/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@AssociationOverrides({
    @AssociationOverride(name = "base", joinColumns = @JoinColumn(name = "base_sub_2"))
})
public class PolymorphicPropertySub2 extends PolymorphicPropertyMapBase<PolymorphicSub2> {
    private static final long serialVersionUID = 1L;

    public PolymorphicPropertySub2() {
    }
}
