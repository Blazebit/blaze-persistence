package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.SerializableEntityViewManager;
import com.blazebit.persistence.view.StaticImplementation;
import com.blazebit.persistence.view.processor.model.sub.BaseView_com_blazebit_persistence_view_processor_model_BView;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
@StaticImplementation(BView.class)
public class BViewImpl<X extends Serializable> extends BaseView_com_blazebit_persistence_view_processor_model_BView<java.lang.Integer, X> implements EntityViewProxy {

    public static volatile EntityViewManager ENTITY_VIEW_MANAGER;
    public static final SerializableEntityViewManager SERIALIZABLE_ENTITY_VIEW_MANAGER = new SerializableEntityViewManager(BViewImpl.class, ENTITY_VIEW_MANAGER);

    private final Integer id;
    private String name;
    private Integer parent;

    public BViewImpl(BViewImpl noop, Map<String, Object> optionalParameters) {
        this.id = null;
        this.name = null;
        this.parent = null;
    }

    public BViewImpl(Integer id) {
        this.id = id;
        this.name = null;
        this.parent = null;
    }

    public BViewImpl(Integer id, String name, Integer parent) {
        super();
        this.id = id;
        this.name = name;
        this.parent = parent;
    }

    public BViewImpl(BViewImpl noop, int offset, Object[] tuple) {
        super();
        this.id = (Integer) tuple[offset + 0];
        this.name = (String) tuple[offset + 1];
        this.parent = (Integer) tuple[offset + 2];
    }

    public BViewImpl(BViewImpl noop, int offset, int[] assignment, Object[] tuple) {
        super();
        this.id = (Integer) tuple[offset + assignment[0]];
        this.name = (String) tuple[offset + assignment[1]];
        this.parent = (Integer) tuple[offset + assignment[2]];
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getParent() {
        return parent;
    }

    @Override
    public void setParent(Integer parent) {
        this.parent = parent;
    }

    @Override
    public Class<?> $$_getJpaManagedClass() { return BView.class; }
    @Override
    public Class<?> $$_getJpaManagedBaseClass() { return BView.class; }
    @Override
    public Class<?> $$_getEntityViewClass() { return BView.class; }
    @Override
    public boolean $$_isNew() { return false; }
    @Override
    public Object $$_getId() { return id; }
    @Override
    public Object $$_getVersion() { return null; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.$$_getId() == null) {
            return false;
        }
        if (obj instanceof EntityViewProxy) {
            EntityViewProxy other = (EntityViewProxy) obj;
            if (this.$$_getJpaManagedBaseClass() == other.$$_getJpaManagedBaseClass() && this.$$_getId().equals(other.$$_getId())) {
                return true;
            } else {
                return false;
            }
        }
        if (obj instanceof BView) {
            BView other = (BView) obj;
            if (!Objects.equals(this.id, other.getId())) {
                return false;
            }
            return true;
        }
        return false;
    }
    @Override
    public int hashCode() {
        long bits;
        int hash = 3;
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    @Override
    public String toString() {
        return "BView(id = " + this.id + ")";
    }
}
