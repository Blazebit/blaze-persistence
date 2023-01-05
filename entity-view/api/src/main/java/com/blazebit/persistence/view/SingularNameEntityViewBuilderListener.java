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

package com.blazebit.persistence.view;

/**
 * A listener that sets the built entity view on the given builder for the given attribute name.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class SingularNameEntityViewBuilderListener implements EntityViewBuilderListener {

    private final EntityViewBuilderBase<?, ?> builder;
    private final String attributeName;

    /**
     * Creates a new listener.
     *
     * @param builder The builder to set the built entity view on
     * @param attributeName The attribute name to set
     */
    public SingularNameEntityViewBuilderListener(EntityViewBuilderBase<?, ?> builder, String attributeName) {
        this.builder = builder;
        this.attributeName = attributeName;
    }

    @Override
    public void onBuildComplete(Object object) {
        builder.with(attributeName, object);
    }
}
