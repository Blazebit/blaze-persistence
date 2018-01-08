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
