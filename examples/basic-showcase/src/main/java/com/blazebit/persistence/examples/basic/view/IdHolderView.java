package com.blazebit.persistence.examples.basic.view;

import com.blazebit.persistence.view.IdMapping;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
public interface IdHolderView<T> {

    @IdMapping("id")
    T getId();

}
