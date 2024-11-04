/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.spi.OrderByElement;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultOrderByElement implements OrderByElement {

    private final String name;
    private final int position;
    private final boolean ascending;
    private final boolean nullable;
    private final boolean nullsFirst;
    
    public DefaultOrderByElement(String name, int position, boolean ascending, boolean nullable, boolean nullsFirst) {
        this.name = name;
        this.position = position;
        this.ascending = ascending;
        this.nullable = nullable;
        this.nullsFirst = nullsFirst;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public boolean isAscending() {
        return ascending;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public boolean isNullsFirst() {
        return nullsFirst;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(20);
        sb.append(position);
        
        if (ascending) {
            sb.append(" ASC");
        } else {
            sb.append(" DESC");
        }

        if (nullable) {
            if (nullsFirst) {
                sb.append(" NULLS FIRST");
            } else {
                sb.append(" NULLS LAST");
            }
        }
        
        return sb.toString();
    }
    
    public static OrderByElement fromString(String string, int startIndex, int endIndex) {
        int spaceIndex = string.indexOf(' ', startIndex);
        int pos = Integer.parseInt(string.substring(startIndex, spaceIndex));
        boolean asc = string.charAt(spaceIndex + 1) == 'A';
        int lastSpaceIndex = string.lastIndexOf(' ', endIndex);
        if (spaceIndex == lastSpaceIndex) {
            return new DefaultOrderByElement(null, pos, asc, false, false);
        } else {
            boolean nullFirst = string.charAt(lastSpaceIndex + 1) == 'F';
            return new DefaultOrderByElement(null, pos, asc, true, nullFirst);
        }
    }
}
