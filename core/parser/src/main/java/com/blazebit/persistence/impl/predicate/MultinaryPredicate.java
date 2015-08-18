/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blazebit.persistence.impl.expression.AbstractExpression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class MultinaryPredicate extends AbstractExpression implements Predicate {

    protected final List<Predicate> children;

    public MultinaryPredicate() {
        this.children = new ArrayList<Predicate>();
    }

    public MultinaryPredicate(Predicate... children) {
        this.children = new ArrayList<Predicate>();
        this.children.addAll(Arrays.asList(children));
    }

    protected MultinaryPredicate(List<Predicate> children) {
        this.children = children;
    }

    @Override
    public abstract MultinaryPredicate clone();

    public List<Predicate> getChildren() {
        return children;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.children != null ? this.children.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MultinaryPredicate other = (MultinaryPredicate) obj;
        if (this.children != other.children && (this.children == null || !this.children.equals(other.children))) {
            return false;
        }
        return true;
    }
}
