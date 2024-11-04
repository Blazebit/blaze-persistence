/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.util;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum QuoteMode {
    NONE {
        public QuoteMode onChar(char c) {
            if (c == '\'') {
                return SINGLE;
            } else if (c == '\"') {
                return DOUBLE;
            } else if (c == '`') {
                return BACKTICK;
            } else if (c == '[') {
                return BRACKET;
            }

            return NONE;
        }

        public QuoteMode onCharBackwards(char c) {
            if (c == '\'') {
                return SINGLE;
            } else if (c == '\"') {
                return DOUBLE;
            } else if (c == '`') {
                return BACKTICK;
            } else if (c == ']') {
                return BRACKET;
            }

            return NONE;
        }
    },
    SINGLE {
        public QuoteMode onChar(char c) {
            if (c == '\'') {
                return NONE;
            }

            return SINGLE;
        }

        public QuoteMode onCharBackwards(char c) {
            return onChar(c);
        }
    },
    DOUBLE {
        public QuoteMode onChar(char c) {
            if (c == '\"') {
                return NONE;
            }

            return DOUBLE;
        }

        public QuoteMode onCharBackwards(char c) {
            return onChar(c);
        }
    },
    BACKTICK {
        public QuoteMode onChar(char c) {
            if (c == '`') {
                return NONE;
            }

            return BACKTICK;
        }

        public QuoteMode onCharBackwards(char c) {
            return onChar(c);
        }
    },
    BRACKET {
        public QuoteMode onChar(char c) {
            if (c == ']') {
                return NONE;
            }

            return BRACKET;
        }

        public QuoteMode onCharBackwards(char c) {
            if (c == '[') {
                return NONE;
            }

            return BRACKET;
        }
    };

    public abstract QuoteMode onChar(char c);

    public abstract QuoteMode onCharBackwards(char c);
}
