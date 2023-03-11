/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public interface ExpressionCopyContext {

    public static final ExpressionCopyContext EMPTY = new ExpressionCopyContext() {
        @Override
        public String getNewParameterName(String oldParameterName) {
            return oldParameterName;
        }

        @Override
        public Expression getExpressionForAlias(String alias) {
            return null;
        }

        @Override
        public boolean isCopyResolved() {
            return false;
        }
    };

    public static final ExpressionCopyContext CLONE = new ExpressionCopyContext() {
        @Override
        public String getNewParameterName(String oldParameterName) {
            return oldParameterName;
        }

        @Override
        public Expression getExpressionForAlias(String alias) {
            return null;
        }

        @Override
        public boolean isCopyResolved() {
            return true;
        }
    };

    public String getNewParameterName(String oldParameterName);

    public Expression getExpressionForAlias(String alias);

    public boolean isCopyResolved();
}
