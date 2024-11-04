/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.builder.expression;

import com.blazebit.persistence.EscapeBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class EscapeBuilderImpl<T> implements EscapeBuilder<T> {

    private final EscapeBuilderImplEndedListener listener;
    private final T result;
    private Character escapeCharacter;

    public EscapeBuilderImpl(EscapeBuilderImplEndedListener listener, T result) {
        this.listener = listener;
        this.result = result;
    }

    @Override
    public T escape(char c) {
        escapeCharacter = c;
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public T noEscape() {
        listener.onBuilderEnded(this);
        return result;
    }

    public Character getEscapeCharacter() {
        return escapeCharacter;
    }

    /**
     * @author Moritz Becker
     * @since 1.0.0
     */
    public static class EscapeBuilderImplEndedListener {

        private EscapeBuilderImpl<?> currentBuilder;

        public void verifyBuilderEnded() {
            if (currentBuilder != null) {
                throw new BuilderChainingException("A builder was not ended properly.");
            }
        }

        public <T extends EscapeBuilderImpl<?>> T startBuilder(T builder) {
            if (currentBuilder != null) {
                throw new BuilderChainingException("There was an attempt to start a builder but a previous builder was not ended.");
            }

            currentBuilder = builder;
            return builder;
        }

        public void onBuilderEnded(EscapeBuilderImpl<?> builder) {
            if (currentBuilder == null) {
                throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
            }
            currentBuilder = null;
        }
    }
}
