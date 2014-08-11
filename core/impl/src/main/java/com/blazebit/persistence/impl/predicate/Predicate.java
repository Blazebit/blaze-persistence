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

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public interface Predicate {

    public static interface Visitor {

        public void visit(AndPredicate predicate);

        public void visit(OrPredicate predicate);

        public void visit(NotPredicate predicate);

        public void visit(EqPredicate predicate);

        public void visit(IsNullPredicate predicate);

        public void visit(IsEmptyPredicate predicate);

        public void visit(IsMemberOfPredicate predicate);

        public void visit(LikePredicate predicate);

        public void visit(BetweenPredicate predicate);

        public void visit(InPredicate predicate);

        public void visit(NotInPredicate predicate);

        public void visit(GtPredicate predicate);

        public void visit(GePredicate predicate);

        public void visit(LtPredicate predicate);

        public void visit(LePredicate predicate);

        public void visit(ExistsPredicate predicate);
    }

    /**
     * The predicate tree is traversed in pre-order.
     *
     * @param visitor
     */
    public void accept(Visitor visitor);
}
