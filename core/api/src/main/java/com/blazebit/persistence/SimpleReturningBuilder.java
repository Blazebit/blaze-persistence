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

package com.blazebit.persistence;

/**
 * A builder for the returning clause.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface SimpleReturningBuilder {

    /**
     * Adds the given entity attribute(<code>modificationQueryAttribute</code>) to the <code>RETURNING</code> clause.
     *
     * @param modificationQueryAttribute The attribute of the modification query entity which to return
     * @return This builder for chaining
     */
    public SimpleReturningBuilder returning(String modificationQueryAttribute);
    
}
