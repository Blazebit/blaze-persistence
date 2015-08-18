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
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public final class OrPredicate extends MultinaryPredicate {

    public OrPredicate() {
    }

    public OrPredicate(Predicate... children) {
        super(children);
    }

    private OrPredicate(List<Predicate> children) {
        super(children);
    }

    @Override
    public OrPredicate clone() {
        int size = children.size();
        List<Predicate> newChildren = new ArrayList<Predicate>(size);

        for (int i = 0; i < size; i++) {
            newChildren.add(children.get(i).clone());
        }

        return new OrPredicate(newChildren);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
