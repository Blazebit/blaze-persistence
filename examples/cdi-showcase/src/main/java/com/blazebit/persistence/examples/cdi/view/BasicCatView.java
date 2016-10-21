package com.blazebit.persistence.examples.cdi.view;

import com.blazebit.persistence.examples.cdi.model.Cat;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
@EntityView(Cat.class)
public abstract class BasicCatView implements IdHolderView<Integer> {

    public abstract String getName();

    @Override
    public String toString() {
        return "Cat{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                '}';
    }
}
