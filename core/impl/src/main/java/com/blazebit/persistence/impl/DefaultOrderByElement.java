package com.blazebit.persistence.impl;

import com.blazebit.persistence.spi.OrderByElement;


public class DefaultOrderByElement implements OrderByElement {

    private final String name;
    private final int position;
    private final boolean ascending;
    private final boolean nullsFirst;
    
    public DefaultOrderByElement(String name, int position, boolean ascending, boolean nullsFirst) {
        this.name = name;
        this.position = position;
        this.ascending = ascending;
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
        
        if (nullsFirst) {
            sb.append(" NULLS FIRST");
        } else {
            sb.append(" NULLS LAST");
        }
        
        return sb.toString();
    }
    
    public static OrderByElement fromString(String string) {
        return fromString(string, 0, string.length() - 1);
    }
    
    public static OrderByElement fromString(String string, int startIndex, int endIndex) {
        int spaceIndex = string.indexOf(' ', startIndex);
        int pos = Integer.parseInt(string.substring(startIndex, spaceIndex));
        boolean asc = string.charAt(spaceIndex + 1) == 'A';
        spaceIndex = string.lastIndexOf(' ', endIndex);
        boolean nullFirst = string.charAt(spaceIndex + 1) == 'F';
        return new DefaultOrderByElement(null, pos, asc, nullFirst);
    }
}
