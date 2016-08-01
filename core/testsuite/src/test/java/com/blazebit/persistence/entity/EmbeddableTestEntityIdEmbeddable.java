package com.blazebit.persistence.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class EmbeddableTestEntityIdEmbeddable implements Serializable {

    private static final long serialVersionUID = 1L;

    private String someValue;

    public EmbeddableTestEntityIdEmbeddable() {
    }

    public EmbeddableTestEntityIdEmbeddable(String someValue) {
        this.someValue = someValue;
    }

    @Column(nullable = false, length = 10)
    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((someValue == null) ? 0 : someValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EmbeddableTestEntityIdEmbeddable other = (EmbeddableTestEntityIdEmbeddable) obj;
        if (someValue == null) {
            if (other.someValue != null)
                return false;
        } else if (!someValue.equals(other.someValue))
            return false;
        return true;
    }

}
