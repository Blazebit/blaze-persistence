package com.blazebit.persistence.view.impl.spring;

import java.util.Set;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 12.10.2016.
 */
public class EntityViewClassesHolder {

    private final Set<Class<?>> entityViewClasses;

    public EntityViewClassesHolder(Set<Class<?>> entityViewClasses) {
        this.entityViewClasses = entityViewClasses;
    }

    public Set<Class<?>> getEntityViewClasses() {
        return entityViewClasses;
    }
}
