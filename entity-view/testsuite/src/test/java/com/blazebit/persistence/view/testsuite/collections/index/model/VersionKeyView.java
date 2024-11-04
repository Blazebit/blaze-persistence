/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.index.model;

import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.EntityView;

import java.util.Objects;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(Version.class)
public interface VersionKeyView {
    
    public Integer getVersionIdx();

    public static VersionKeyView of(Integer idx) {
        return new VersionKeyView() {
            @Override
            public Integer getVersionIdx() {
                return idx;
            }

            @Override
            public int hashCode() {
                int hash = 3;
                hash = 83 * hash + Integer.hashCode(idx);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                return obj instanceof VersionKeyView && Objects.equals(idx, ((VersionKeyView) obj).getVersionIdx());
            }
        };
    }
}
