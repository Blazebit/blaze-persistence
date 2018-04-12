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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PostFlushViewToEntityMapperDeleter implements PostFlushDeleter {

    private final ViewToEntityMapper viewToEntityMapper;
    private final Object object;

    public PostFlushViewToEntityMapperDeleter(ViewToEntityMapper viewToEntityMapper, Object object) {
        this.viewToEntityMapper = viewToEntityMapper;
        this.object = object;
    }

    @Override
    public void execute(UpdateContext context) {
        viewToEntityMapper.remove(context, object);
    }
}
