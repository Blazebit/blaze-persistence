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

package com.blazebit.persistence.view;

import com.blazebit.persistence.view.filter.ContainsFilter;
import com.blazebit.persistence.view.filter.EndsWithFilter;
import com.blazebit.persistence.view.filter.ExactFilter;
import com.blazebit.persistence.view.filter.StartsWithFilter;

/**
 * TODO: javadoc
 *
 * @author Christian Beikov
 * @since 1.0
 */
public final class Filters {
    
    private Filters() {
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public static Filter contains(String value) {
        return new ContainsFilter(value);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public static Filter startsWith(String value) {
        return new StartsWithFilter(value);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public static Filter endsWith(String value) {
        return new EndsWithFilter(value);
    }
    
    /**
     * TODO: javadoc
     *
     * @return
     */
    public static Filter exact(String value) {
        return new ExactFilter(value);
    }
}
