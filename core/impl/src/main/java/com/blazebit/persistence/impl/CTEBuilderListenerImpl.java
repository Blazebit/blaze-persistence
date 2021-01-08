/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.impl;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class CTEBuilderListenerImpl implements CTEBuilderListener {

    private Set<CTEInfoBuilder> currentCteBuilders = Collections.newSetFromMap(new IdentityHashMap<CTEInfoBuilder, Boolean>());
    
    public void onReplaceBuilder(CTEInfoBuilder oldBuilder, CTEInfoBuilder newBuilder) {
        if (!currentCteBuilders.remove(oldBuilder)) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }

        currentCteBuilders.add(newBuilder);
    }

    public void verifyBuilderEnded() {
        if (!currentCteBuilders.isEmpty()) {
            throw new BuilderChainingException("Some CTE builders were not ended properly.");
        }
    }

    @Override
    public void onBuilderEnded(CTEInfoBuilder builder) {
        if (!currentCteBuilders.remove(builder)) {
            throw new BuilderChainingException("There was an attempt to end a builder that was not started or already closed.");
        }
    }

    @Override
    public void onBuilderStarted(CTEInfoBuilder builder) {
        currentCteBuilders.add(builder);
    }
}
