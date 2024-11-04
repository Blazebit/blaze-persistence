/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
