/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.VersionBasicUserType;
import com.blazebit.persistence.view.spi.type.ImmutableBasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class LongBasicUserType extends ImmutableBasicUserType<Long> implements VersionBasicUserType<Long> {

    public static final BasicUserType<Long> INSTANCE = new LongBasicUserType();
    private static final Long ZERO = 0L;

    @Override
    public Long nextValue(Long current) {
        if (current == null) {
            return ZERO;
        }
        return current + 1L;
    }
}
