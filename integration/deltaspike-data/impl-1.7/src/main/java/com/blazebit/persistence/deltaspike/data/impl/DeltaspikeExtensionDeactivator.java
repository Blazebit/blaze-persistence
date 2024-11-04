/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl;

import org.apache.deltaspike.core.spi.activation.ClassDeactivator;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.data.impl.RepositoryExtension;
import org.apache.deltaspike.partialbean.impl.PartialBeanBindingExtension;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DeltaspikeExtensionDeactivator implements ClassDeactivator {
    @Override
    public Boolean isActivated(Class<? extends Deactivatable> aClass) {
        return !(aClass.equals(RepositoryExtension.class) || aClass.equals(PartialBeanBindingExtension.class));
    }
}