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

import com.blazebit.persistence.impl.SimpleQueryGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public final class AndPredicate extends MultinaryPredicate {
    
    public AndPredicate() {
    }
    
    public AndPredicate(Predicate... children) {
        super(children);
    }
    
    private AndPredicate(List<Predicate> children) {
        super(children);
    }

    @Override
    public AndPredicate clone() {
        int size = children.size();
        List<Predicate> newChildren = new ArrayList<Predicate>(size);
        
        for (int i = 0; i < size; i++) {
            newChildren.add(children.get(i).clone());
        }
        
        return new AndPredicate(newChildren);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        SimpleQueryGenerator generator = new SimpleQueryGenerator();
        generator.setQueryBuffer(sb);
        generator.visit(this);
        return sb.toString();
    }
}
