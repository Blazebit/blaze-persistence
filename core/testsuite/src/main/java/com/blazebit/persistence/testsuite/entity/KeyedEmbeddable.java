package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Embeddable;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Embeddable
public class KeyedEmbeddable {

    private String value;
    private String value2;

    public KeyedEmbeddable() {
    }

    public KeyedEmbeddable(String value, String value2) {
        this.value = value;
        this.value2 = value2;
    }
}
