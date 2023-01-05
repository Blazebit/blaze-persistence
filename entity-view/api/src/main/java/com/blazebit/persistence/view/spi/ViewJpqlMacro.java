/*
 * Copyright 2014 - 2023 Blazebit.
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
 * Represents the current view that is accessible through the expression <code>VIEW()</code>.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface ViewJpqlMacro extends JpqlMacro {

    /**
     * Returns the current view path.
     *
     * @return the current view path
     */
    public String getViewPath();

    /**
     * Sets the current view path.
     *
     * @param viewPath The new view path
     */
    public void setViewPath(String viewPath);
}
