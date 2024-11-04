/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.util.List;

/**
 * A listener that adds the built entity view to a list.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ListEntityViewBuilderListener implements EntityViewBuilderListener {

    private final List<Object> list;
    private final int index;

    /**
     * Creates the listener.
     *
     * @param list The list to add a built entity view to
     * @param index The index to which to add the entity view to
     */
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
