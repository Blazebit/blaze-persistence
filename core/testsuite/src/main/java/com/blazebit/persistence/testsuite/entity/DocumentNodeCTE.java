package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.blazebit.persistence.CTE;

@CTE
@Entity
public class DocumentNodeCTE implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long parentId;
    
    @Id
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

}
