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

package com.blazebit.persistence.view.spi;

import com.blazebit.persistence.spi.JpqlMacro;

/**
 * Interface implemented by the entity view provider.
 *
 * Represents a embedding view that gives access to the embedding view.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface EmbeddingViewJpqlMacro extends JpqlMacro {

    /**
     * Returns whether the macro was used so far.
     *
     * @return whether the macro was used so far
     */
    public boolean usesEmbeddingView();

    /**
     * Returns the current embedding view path.
     *
     * @return the current embedding view path
     */
    public String getEmbeddingViewPath();

    /**
     * Sets the current embedding view path.
     *
     * @param embeddingViewPath The new embedding view path
     */
    public void setEmbeddingViewPath(String embeddingViewPath);
}
