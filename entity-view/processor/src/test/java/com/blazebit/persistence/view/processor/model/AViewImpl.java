package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.SerializableEntityViewManager;
import com.blazebit.persistence.view.StaticImplementation;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
@StaticImplementation(AView.class)
public class AViewImpl<X extends Serializable> implements AView<X>, EntityViewProxy {

    public static volatile EntityViewManager ENTITY_VIEW_MANAGER;
    public static final SerializableEntityViewManager SERIALIZABLE_ENTITY_VIEW_MANAGER = new SerializableEntityViewManager(AViewImpl.class, ENTITY_VIEW_MANAGER);

    private final int age;
    private final byte[] bytes;
    private final Integer id;
    private final List<Set<String>> multiNames;
    private String name;
    private final List<String> names;
    private final List<X> test;

    public AViewImpl(AViewImpl noop, Map<String, Object> optionalParameters) {
        this.age = 0;
        this.bytes = null;
        this.id = null;
        this.multiNames = (List<Set<String>>) (java.util.List<?>) AView_.multiNames.getCollectionInstantiator().createCollection(0);
        this.name = null;
        this.names = (List<String>) (java.util.List<?>) AView_.names.getCollectionInstantiator().createCollection(0);
        this.test = (List<X>) (java.util.List<?>) AView_.test.getCollectionInstantiator().createCollection(0);
    }

    public AViewImpl(Integer id) {
        this.$$_kind = (byte) 1;
        this.age = 0;
        this.bytes = null;
        this.id = id;
        this.multiNames = (List<Set<String>>) (java.util.List<?>) AView_.multiNames.getCollectionInstantiator().createCollection(0);
        this.name = null;
        this.names = (List<String>) (java.util.List<?>) AView_.names.getCollectionInstantiator().createCollection(0);
        this.test = (List<X>) (java.util.List<?>) AView_.test.getCollectionInstantiator().createCollection(0);
    }

    public AViewImpl(Integer id, int age, byte[] bytes, List<Set<String>> multiNames, String name, List<String> names, List<X> test) {
        super();
        this.age = age;
        this.bytes = bytes;
        this.id = id;
        this.multiNames = multiNames;
        this.name = name;
        this.names = names;
        this.test = test;
    }

    public AViewImpl(AViewImpl noop, int offset, Object[] tuple) {
        super();
        this.age = (int) tuple[offset + 1];
        this.bytes = (byte[]) tuple[offset + 2];
        this.id = (Integer) tuple[offset + 0];
        this.multiNames = (List<Set<String>>) tuple[offset + 3];
        this.name = (String) tuple[offset + 4];
        this.names = (List<String>) tuple[offset + 5];
        this.test = (List<X>) tuple[offset + 6];
    }

    public AViewImpl(AViewImpl noop, int offset, int[] assignment, Object[] tuple) {
        super();
        this.age = (int) tuple[offset + assignment[1]];
        this.bytes = (byte[]) tuple[offset + assignment[2]];
        this.id = (Integer) tuple[offset + assignment[0]];
        this.multiNames = (List<Set<String>>) tuple[offset + assignment[3]];
        this.name = (String) tuple[offset + assignment[4]];
        this.names = (List<String>) tuple[offset + assignment[5]];
        this.test = (List<X>) tuple[offset + assignment[6]];
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public List<Set<String>> getMultiNames() {
        return multiNames;
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
    public List<String> getNames() {
        return names;
    }

    @Override
    public List<X> getTest() {
        return test;
    }

    @Override
    public EntityViewManager evm() {
        return SERIALIZABLE_ENTITY_VIEW_MANAGER;
    }

    private byte $$_kind;
    @Override
    public Class<?> $$_getJpaManagedClass() { return AEntity.class; }
    @Override
    public Class<?> $$_getJpaManagedBaseClass() { return AEntity.class; }
    @Override
    public Class<?> $$_getEntityViewClass() { return AView.class; }
    @Override
    public boolean $$_isNew() { return false; }
    @Override
    public boolean $$_isReference() {
        return $$_kind == (byte) 1;
    }
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
        if (obj instanceof AView) {
            AView other = (AView) obj;
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
        return "AView(id = " + this.id + ")";
    }
}
