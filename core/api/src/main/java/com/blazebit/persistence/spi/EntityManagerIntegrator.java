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
package com.blazebit.persistence.spi;

import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;

/**
 * Interface implemented by the criteria provider.
 *
 * It is used to integrate some features with the persistence provider.
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public interface EntityManagerIntegrator {

    /**
     * Registers the given functions under the given names on the given entity manager.
     * The dbmsFunctions map the function name to a map of dbms specific functions.
     * The dbms specific functions map uses a dbms identifier as key.
     *
     * @param entityManager The entity manager which should be enriched
     * @param dbmsFunctions The functions for various dbms
     * @return The enriched entity manager
     */
    public EntityManager registerFunctions(EntityManager entityManager, Map<String, JpqlFunctionGroup> dbmsFunctions);
    
    /**
     * Returns the names of all registered functions.
     * 
     * @param entityManager The entity manager which should be queried
     * @return The set of function names
     */
    public Set<String> getRegisteredFunctions(EntityManager entityManager);
}
