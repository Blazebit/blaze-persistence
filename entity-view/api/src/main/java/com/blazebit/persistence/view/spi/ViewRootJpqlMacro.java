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

package com.blazebit.persistence.view.spi;

import com.blazebit.persistence.spi.JpqlMacro;

/**
 * Interface implemented by the entity view provider.
 *
 * Represents a view root macro that gives access to the view root.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ViewRootJpqlMacro extends JpqlMacro {

    /**
     * Returns the view root alias or <code>null</code> if not an alias.
     *
     * When using batched fetching, it can happen that a view root is represented as parameter. In that case <code>null</code> is returned.
     *
     * @return The view root or <code>null</code>
     */
    public String getViewRoot();

}
