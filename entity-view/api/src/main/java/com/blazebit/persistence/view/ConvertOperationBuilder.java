/*
 * Copyright 2014 - 2022 Blazebit.
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

/**
 * A builder for defining flush related configuration.
 *
 * @param <T> The type of the entity view class
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface ConvertOperationBuilder<T> {

    /**
     * Converts the source object as defined and returns the result.
     *
     * @return The converted view
     */
    public T convert();

    /**
     * Specifies that the given attribute should not be converted into the target view.
     *
     * @param attributePath The attribute path to skip during conversion
     * @return this for chaining
     */
    public ConvertOperationBuilder<T> excludeAttribute(String attributePath);

    /**
     * Specifies that the given attributes should not be converted into the target view.
     *
     * @param attributePaths The attribute paths to skip during conversion
     * @return this for chaining
     */
    public ConvertOperationBuilder<T> excludeAttributes(String... attributePaths);

    /**
     * Specifies that the attribute object should use the given view type class and convert options.
     *
     * @param attributePath The attribute path for which to override the view class and convert options
     * @param attributeViewClass The entity view class to convert the attribute object to
     * @param convertOptions The convert options to use
     * @return this for chaining
     */
    public ConvertOperationBuilder<T> convertAttribute(String attributePath, Class<?> attributeViewClass, ConvertOption... convertOptions);

    /**
     * Specifies that the attribute object should use the given view type class and convert options.
     *
     * @param attributePath The attribute path for which to override the view class and convert options
     * @param attributeViewClass The entity view class to convert the attribute object to
     * @param constructorName The name of the entity view constructor to use
     * @param convertOptions The convert options to use
     * @return this for chaining
     * @since 1.5.0
     */
    public ConvertOperationBuilder<T> convertAttribute(String attributePath, Class<?> attributeViewClass, String constructorName, ConvertOption... convertOptions);
}
