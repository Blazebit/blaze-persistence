package com.blazebit.persistence.spi;

import java.util.HashMap;
import java.util.Map;

public final class JpqlFunctionGroup {

    private final String name;
    private final boolean aggregate;
    private final Map<String, JpqlFunction> rdbmsFunctions;

    public JpqlFunctionGroup(String name) {
        this(name, false);
    }

    public JpqlFunctionGroup(String name, JpqlFunction defaultFunction) {
        this(name, false);
        add(null, defaultFunction);
    }

    public JpqlFunctionGroup(String name, boolean aggregate) {
        this(name, aggregate, new HashMap<String, JpqlFunction>());
    }

    public JpqlFunctionGroup(String name, boolean aggregate, Map<String, JpqlFunction> rdbmsFunctions) {
        this.name = name;
        this.aggregate = aggregate;
        this.rdbmsFunctions = new HashMap<String, JpqlFunction>(rdbmsFunctions);
    }

    public String getName() {
        return name;
    }

    public boolean isAggregate() {
        return aggregate;
    }

    public JpqlFunction get(String rdbms) {
        return rdbmsFunctions.get(rdbms);
    }

    public boolean contains(String rdbms) {
        return rdbmsFunctions.containsKey(rdbms);
    }

    public void add(String rdbms, JpqlFunction function) {
        rdbmsFunctions.put(rdbms, function);
    }
}
