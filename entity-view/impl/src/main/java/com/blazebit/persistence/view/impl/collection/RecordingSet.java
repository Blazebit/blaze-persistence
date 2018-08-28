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

package com.blazebit.persistence.view.impl.collection;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RecordingSet<C extends Set<E>, E> extends RecordingCollection<C, E> implements Set<E> {

    protected RecordingSet(C delegate, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize, boolean hashBased, boolean ordered) {
        super(delegate, false, ordered, allowedSubtypes, updatable, optimize, hashBased);
    }

    public RecordingSet(C delegate, boolean ordered, Set<Class<?>> allowedSubtypes, boolean updatable, boolean optimize) {
        super(delegate, false, ordered, allowedSubtypes, updatable, optimize, true);
    }

    @Override
    protected boolean allowDuplicates() {
        return false;
    }

}
