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

package com.blazebit.persistence.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * A group of {@link JpqlFunction}s for various DBMSes under a single function name.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class JpqlFunctionGroup {

    private final String name;
    private final boolean aggregate;
    private final Map<String, JpqlFunction> rdbmsFunctions;

    /**
     * Constructs a non-aggregate function group with the given name.
     *
     * @param name The function name
     */
    public JpqlFunctionGroup(String name) {
        this(name, false);
    }

    /**
     * Constructs a non-aggregate function group with the given name and given default function.
     *
     * @param name The function name
     * @param defaultFunction The default function to use when no specific function for a DBMS is available
     */
    public JpqlFunctionGroup(String name, JpqlFunction defaultFunction) {
        this(name, false);
        add(null, defaultFunction);
    }

    /**
     * Constructs a function group with the given name.
     *
     * @param name The function name
     * @param aggregate True if the function is an aggregate function, false otherwise
     */
    public JpqlFunctionGroup(String name, boolean aggregate) {
        this(name, aggregate, new HashMap<String, JpqlFunction>());
    }

    /**
     * Constructs a function group with the given name and given function mappings.
     *
     * @param name The function name
     * @param aggregate True if the function is an aggregate function, false otherwise
     * @param rdbmsFunctions The RDBMS functions in a map
     */
    public JpqlFunctionGroup(String name, boolean aggregate, Map<String, JpqlFunction> rdbmsFunctions) {
        this.name = name;
        this.aggregate = aggregate;
        this.rdbmsFunctions = new HashMap<String, JpqlFunction>(rdbmsFunctions);
    }

    /**
     * The name of the function.
     *
     * @return The function name
     */
    public String getName() {
        return name;
    }

    /**
     * Whether the function is an aggregate.
     *
     * @return True if this is an aggregate function, false otherwise
     */
    public boolean isAggregate() {
        return aggregate;
    }

    /**
     * Returns the {@link JpqlFunction} for the given RDBMS name.
     *
     * @param rdbms The RDBMS name
     * @return The {@link JpqlFunction} or null
     */
    public JpqlFunction get(String rdbms) {
        return rdbmsFunctions.get(rdbms);
    }

    /**
     * Whether a {@link JpqlFunction} for the given RDBMS name exists.
     *
     * @param rdbms The RDBMS name
     * @return True if a function for the RDBMS was registered, false otherwise
     */
    public boolean contains(String rdbms) {
        return rdbmsFunctions.containsKey(rdbms);
    }

    /**
     * Adds the given {@link JpqlFunction} for the given RDBMS to the group.
     *
     * @param rdbms The RDBMS name for which to register the function or null to register the default function
     * @param function The {@link JpqlFunction} to register
     */
    public void add(String rdbms, JpqlFunction function) {
        rdbmsFunctions.put(rdbms, function);
    }
}
