/*
 * Copyright 2014 - 2022 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    @Mapping("idx - idx")
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
