package com.blazebit.persistence.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class EmbeddableTestEntityContainer implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Set<EmbeddableTestEntity> embeddableTestEntities = new HashSet<EmbeddableTestEntity>();
	
	@Id
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@OneToMany(fetch = FetchType.LAZY)
	public Set<EmbeddableTestEntity> getEmbeddableTestEntities() {
		return embeddableTestEntities;
	}
	public void setEmbeddableTestEntities(Set<EmbeddableTestEntity> embeddableTestEntities) {
		this.embeddableTestEntities = embeddableTestEntities;
	}
	
}
