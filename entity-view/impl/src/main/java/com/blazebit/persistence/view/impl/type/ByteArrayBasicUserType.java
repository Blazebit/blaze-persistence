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

import com.blazebit.persistence.view.spi.type.AbstractMutableBasicUserType;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.Arrays;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ByteArrayBasicUserType extends AbstractMutableBasicUserType<Byte[]> implements BasicUserType<Byte[]> {

    public static final BasicUserType<?> INSTANCE = new ByteArrayBasicUserType();

    @Override
    public boolean isDeepEqual(Byte[] object1, Byte[] object2) {
        return Arrays.equals(object1, object2);
    }

    @Override
    public int hashCode(Byte[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public Byte[] deepClone(Byte[] object) {
        return Arrays.copyOf(object, object.length);
    }
}
