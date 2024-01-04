/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.criteria;

/**
 * An {@link javax.persistence.criteria.Expression} for a window enabled function.
 *
 * @param <X> The target type
 * @author Christian Beikov
 * @since 1.6.4
 */
public interface BlazeWindowFunctionExpression<X> extends BlazeFunctionExpression<X> {

    /**
     * Returns the window for this window function.
     *
     * @return the window
     */
    public BlazeWindow getWindow();

    /**
     * Sets the window for this window function.
     *
     * @param window The window to set
     * @return <code>this</code> for method chaining
     */
    public BlazeWindowFunctionExpression<X> window(BlazeWindow window);
}
