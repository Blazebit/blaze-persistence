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

package com.blazebit.persistence.spring.data.repository;

import com.blazebit.persistence.view.EntityViewSetting;

/**
 * @author Giovanni Lovato
 * @since 1.3.0
 */
public interface EntityViewSettingProcessor<T> {

    /**
     * Processes the {@link EntityViewSetting} to allow additional Entity View customization during query creation.
     *
     * @param setting the {@link EntityViewSetting} to be processed
     * @return the final {@link EntityViewSetting} to allow further processing
     */
    EntityViewSetting<T, ?> acceptEntityViewSetting(EntityViewSetting<T, ?> setting);
}
