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

package com.blazebit.persistence.spi;

import java.util.HashMap;
import java.util.Map;

public final class JpqlFunctionGroup {

    private final String name;
    private final boolean aggregate;
    private final Map<String, JpqlFunction> rdbmsFunctions;

    public JpqlFunctionGroup(String name) {
        this(name, false);
    }

    public JpqlFunctionGroup(String name, JpqlFunction defaultFunction) {
        this(name, false);
        add(null, defaultFunction);
    }

    public JpqlFunctionGroup(String name, boolean aggregate) {
        this(name, aggregate, new HashMap<String, JpqlFunction>());
    }

    public JpqlFunctionGroup(String name, boolean aggregate, Map<String, JpqlFunction> rdbmsFunctions) {
        this.name = name;
        this.aggregate = aggregate;
        this.rdbmsFunctions = new HashMap<String, JpqlFunction>(rdbmsFunctions);
    }

    public String getName() {
        return name;
    }

    public boolean isAggregate() {
        return aggregate;
    }

    public JpqlFunction get(String rdbms) {
        return rdbmsFunctions.get(rdbms);
    }

    public boolean contains(String rdbms) {
        return rdbmsFunctions.containsKey(rdbms);
    }

    public void add(String rdbms, JpqlFunction function) {
        rdbmsFunctions.put(rdbms, function);
    }
}
