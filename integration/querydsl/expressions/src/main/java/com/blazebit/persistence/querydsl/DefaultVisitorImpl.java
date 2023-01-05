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

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.TemplateExpression;
import com.querydsl.core.types.Visitor;

/**
 * Default implementation for visitors that returns null for all methods.
 *
 * @param <R> Return type
 * @param <C> Context type
 * @since 1.5.0
 */
abstract class DefaultVisitorImpl<R, C> implements Visitor<R, C> {

    @Override
    public R visit(Constant<?> constant, C c) {
        return null;
    }

    @Override
    public R visit(FactoryExpression<?> factoryExpression, C c) {
        return null;
    }

    @Override
    public R visit(Operation<?> operation, C c) {
        return null;
    }

    @Override
    public R visit(ParamExpression<?> paramExpression, C c) {
        return null;
    }

    @Override
    public R visit(Path<?> path, C c) {
        return null;
    }

    @Override
    public R visit(SubQueryExpression<?> subQueryExpression, C c) {
        return null;
    }

    @Override
    public R visit(TemplateExpression<?> templateExpression, C c) {
        return null;
    }

}
