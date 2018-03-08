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

package com.blazebit.persistence.impl.keyset;

import java.util.Map;

import com.blazebit.persistence.impl.BuilderChainingException;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class AbstractKeysetBuilderEndedListener implements KeysetBuilderEndedListener {

    private KeysetBuilderImpl<?> currentBuilder;
    private KeysetLink keysetLink;

    public void verifyBuilderEnded() {
        if (currentBuilder != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
    }

    public <T> KeysetBuilderImpl<T> startBuilder(KeysetBuilderImpl<T> builder) {
        if (currentBuilder != null) {
            throw new BuilderChainingException("There was an attempt to start a builder but a previous builder was not ended.");
        }

        currentBuilder = builder;
        return builder;
    }

    @Override
    public void onBuilderEnded(KeysetBuilderImpl<?> builder) {
        if (currentBuilder == null) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }
        keysetLink = createLink(builder);
        currentBuilder = null;
    }

    private KeysetLink createLink(KeysetBuilderImpl<?> builder) {
        Map<String, Object> keysetValues = builder.getKeysetValues();
        KeysetMode mode = builder.getMode();

        return new LazyKeysetLink(keysetValues, mode);
    }

    public KeysetLink getKeysetLink() {
        return keysetLink;
    }

    public void setKeysetLink(KeysetLink keysetLink) {
        this.keysetLink = keysetLink;
    }

}
