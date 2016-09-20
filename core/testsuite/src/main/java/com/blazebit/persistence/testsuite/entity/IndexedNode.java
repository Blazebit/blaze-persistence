package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity(name = "IndexedNode")
public class IndexedNode {

    @Id
    private Integer id;
    @ManyToOne
    private Root parent;
    @Column(name = "list_index")
    private Integer index;
}
