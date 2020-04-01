/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.spring.data.webflux.impl.json;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.DecoderHttpMessageReader;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class EntityViewTypedDecoderHttpMessageReader<T> extends DecoderHttpMessageReader<T> {

    public EntityViewTypedDecoderHttpMessageReader(Decoder<T> decoder) {
        super(decoder);
    }

    @Override
    public boolean canRead(ResolvableType elementType, MediaType mediaType) {
        // By returning false for type Object, this reader will be regarded as typed reader
        // by Webflux and will receive precedence over object readers.
        if (elementType.isAssignableFrom(Object.class)) {
            return false;
        }
        return super.canRead(elementType, mediaType);
    }
}
