/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.impl.expression.modifier;

import com.blazebit.persistence.impl.expression.Expression;

import java.util.List;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 22.09.2016.
 */
public class ExpressionListModifier<E extends Expression> implements ExpressionModifier<E> {

    protected List<E> target;
    protected int modificationnIndex;

    public ExpressionListModifier() {
    }

    public ExpressionListModifier(List<E> target, int modificationnIndex) {
        this.target = target;
        this.modificationnIndex = modificationnIndex;
    }

    public ExpressionListModifier(ExpressionListModifier<E> original) {
        this.target = original.target;
        this.modificationnIndex = original.modificationnIndex;
    }

    public List<E> getTarget() {
        return target;
    }

    public void setTarget(List<E> target) {
        this.target = target;
    }

    public int getModificationnIndex() {
        return modificationnIndex;
    }

    public void setModificationnIndex(int modificationnIndex) {
        this.modificationnIndex = modificationnIndex;
    }

    @Override
    public void set(E expression) {
        target.set(modificationnIndex, expression);
    }

    @Override
    public Object clone() {
        return new ExpressionListModifier<E>(this);
    }
}
