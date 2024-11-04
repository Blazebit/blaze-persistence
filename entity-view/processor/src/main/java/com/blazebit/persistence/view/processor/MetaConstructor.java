/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MetaConstructor {

    Comparator<MetaConstructor> NAME_COMPARATOR = new Comparator<MetaConstructor>() {
        @Override
        public int compare(MetaConstructor o1, MetaConstructor o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    MetaEntityView getHostingEntity();

    String getName();

    boolean isReal();

    boolean hasSelfParameter();

    List<MetaAttribute> getParameters();

    Map<String, String> getOptionalParameters();
}
