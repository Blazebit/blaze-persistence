package com.blazebit.persistence.impl.function.entity;

import com.blazebit.persistence.CTE;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
@CTE
public class ValuesEntity implements Serializable {

    private String value;

    @Id
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
