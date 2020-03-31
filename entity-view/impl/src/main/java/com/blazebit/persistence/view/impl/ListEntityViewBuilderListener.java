/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.impl;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ListEntityViewBuilderListener implements EntityViewBuilderListener {

    private final List<Object> list;
    private final int index;

    public ListEntityViewBuilderListener(List<Object> list, int index) {
        this.list = list;
        this.index = index;
    }

    @Override
    public void onBuildComplete(Object object) {
        if (index > list.size()) {
            for (int i = list.size(); i < index; i++) {
                list.add(null);
            }
            list.add(object);
        } else if (index < list.size()) {
            list.set(index, object);
        } else {
            list.add(object);
        }
    }
}
