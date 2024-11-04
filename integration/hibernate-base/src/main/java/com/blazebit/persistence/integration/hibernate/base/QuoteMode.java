/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

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
    },
    SINGLE {
        public QuoteMode onChar(char c) {
            if (c == '\'') {
                return NONE;
            }

            return SINGLE;
        }
    },
    DOUBLE {
        public QuoteMode onChar(char c) {
            if (c == '\"') {
                return NONE;
            }

            return DOUBLE;
        }
    },
    BACKTICK {
        public QuoteMode onChar(char c) {
            if (c == '`') {
                return NONE;
            }

            return BACKTICK;
        }
    },
    BRACKET {
        public QuoteMode onChar(char c) {
            if (c == ']') {
                return NONE;
            }

            return BRACKET;
        }
    };

    public abstract QuoteMode onChar(char c);
}
