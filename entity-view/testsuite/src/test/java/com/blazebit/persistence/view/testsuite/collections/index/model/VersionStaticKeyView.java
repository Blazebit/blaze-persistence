/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.index.model;

import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;

import java.util.Objects;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(Version.class)
public interface VersionStaticKeyView {

    // Good job Datanucleus.. https://github.com/datanucleus/datanucleus-core/issues/356
    @Mapping("versionIdx - versionIdx")
    public Integer getIdx();

    public static VersionStaticKeyView of(Integer idx) {
        return new VersionStaticKeyView() {
            @Override
            public Integer getIdx() {
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
                return obj instanceof VersionStaticKeyView && Objects.equals(idx, ((VersionStaticKeyView) obj).getIdx());
            }
        };
    }
}
