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
        if (this.name == null) {
            this.name = null;
        }
        if (this.parent == null) {
            this.parent = null;
        }
    }

    public BViewImpl(Integer id) {
        this.$$_kind = (byte) 1;
        this.id = id;
        if (this.name == null) {
            this.name = null;
        }
        if (this.parent == null) {
            this.parent = null;
        }
    }

    public BViewImpl(
            Integer id,
            String name,
            Integer parent
    ) {
        super();
        this.id = id;
        if (this.name == null) {
            this.name = name;
        }
        if (this.parent == null) {
            this.parent = parent;
        }
    }

    public BViewImpl(BViewImpl noop, int offset, Object[] tuple) {
        super();
        this.id = (Integer) tuple[offset + 0];
        if (this.name == null) {
            this.name = (String) tuple[offset + 1];
        }
        if (this.parent == null) {
            this.parent = (Integer) tuple[offset + 2];
        }
    }

    public BViewImpl(BViewImpl noop, int offset, int[] assignment, Object[] tuple) {
        super();
        this.id = (Integer) tuple[offset + assignment[0]];
        if (this.name == null) {
            this.name = (String) tuple[offset + assignment[1]];
        }
        if (this.parent == null) {
            this.parent = (Integer) tuple[offset + assignment[2]];
        }
    }

    public BViewImpl(
            Integer id,
            String name,
            Integer parent,
            BView self
    ) {
        super(
                self
        );
        this.id = id;
        if (this.name == null) {
            this.name = name;
        }
        if (this.parent == null) {
            this.parent = parent;
        }
    }

    public BViewImpl(
            BViewImpl noop,
            int offset,
            Object[] tuple,
            BView self
    ) {
        super(
                createSelf(
                        (Integer) tuple[offset + 0],
                        (String) tuple[offset + 1],
                        (Integer) tuple[offset + 2]
                )
        );
        this.id = (Integer) tuple[offset + 0];
        if (this.name == null) {
            this.name = (String) tuple[offset + 1];
        }
        if (this.parent == null) {
            this.parent = (Integer) tuple[offset + 2];
        }
    }

    public BViewImpl(
            BViewImpl noop,
            int offset,
            int[] assignment,
            Object[] tuple,
            BView self
    ) {
        super(
                createSelf(
                        (Integer) tuple[offset + assignment[0]],
                        (String) tuple[offset + assignment[1]],
                        (Integer) tuple[offset + assignment[2]]
                )
        );
        this.id = (Integer) tuple[offset + assignment[0]];
        if (this.name == null) {
            this.name = (String) tuple[offset + assignment[1]];
        }
        if (this.parent == null) {
            this.parent = (Integer) tuple[offset + assignment[2]];
        }
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

    private byte $$_kind;
    @Override
    public Class<?> $$_getJpaManagedClass() { return BView.class; }
    @Override
    public Class<?> $$_getJpaManagedBaseClass() { return BView.class; }
    @Override
    public Class<?> $$_getEntityViewClass() { return BView.class; }
    @Override
    public boolean $$_isNew() { return false; }
    @Override
    public boolean $$_isReference() {
        return $$_kind == (byte) 1;
    }
    @Override
    public void $$_setIsReference(boolean isReference) {
        if (isReference) {
            this.$$_kind = (byte) 1;
        } else {
            this.$$_kind = (byte) 0;
        }
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

    public static BView createSelf(
            Integer id,
            String name,
            Integer parent
    ) {
        try {
            BViewSer $ = (BViewSer) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(BViewSer.EMPTY_INSTANCE_BYTES)).readObject();
            $.id = id;
            $.name = name;
            $.parent = parent;
            return $;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    private static class BViewSer<X extends Serializable> extends BaseView_com_blazebit_persistence_view_processor_model_BView<java.lang.Integer, X> implements Serializable {

        private static final long serialVersionUID = 1L;
        private static final byte[] EMPTY_INSTANCE_BYTES = new byte[]{ (byte) 0xAC, (byte) 0xED, (byte) 0x00, (byte) 0x05, (byte) 0x73, (byte) 0x72, (byte) 0x00, (byte) 0x40, (byte) 0x63, (byte) 0x6F, (byte) 0x6D, (byte) 0x2E, (byte) 0x62, (byte) 0x6C, (byte) 0x61, (byte) 0x7A, (byte) 0x65, (byte) 0x62, (byte) 0x69, (byte) 0x74, (byte) 0x2E, (byte) 0x70, (byte) 0x65, (byte) 0x72, (byte) 0x73, (byte) 0x69, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x6E, (byte) 0x63, (byte) 0x65, (byte) 0x2E, (byte) 0x76, (byte) 0x69, (byte) 0x65, (byte) 0x77, (byte) 0x2E, (byte) 0x70, (byte) 0x72, (byte) 0x6F, (byte) 0x63, (byte) 0x65, (byte) 0x73, (byte) 0x73, (byte) 0x6F, (byte) 0x72, (byte) 0x2E, (byte) 0x6D, (byte) 0x6F, (byte) 0x64, (byte) 0x65, (byte) 0x6C, (byte) 0x2E, (byte) 0x42, (byte) 0x56, (byte) 0x69, (byte) 0x65, (byte) 0x77, (byte) 0x49, (byte) 0x6D, (byte) 0x70, (byte) 0x6C, (byte) 0x24, (byte) 0x42, (byte) 0x56, (byte) 0x69, (byte) 0x65, (byte) 0x77, (byte) 0x53, (byte) 0x65, (byte) 0x72, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x03, (byte) 0x4C, (byte) 0x00, (byte) 0x02, (byte) 0x69, (byte) 0x64, (byte) 0x74, (byte) 0x00, (byte) 0x16, (byte) 0x4C, (byte) 0x6A, (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2F, (byte) 0x69, (byte) 0x6F, (byte) 0x2F, (byte) 0x53, (byte) 0x65, (byte) 0x72, (byte) 0x69, (byte) 0x61, (byte) 0x6C, (byte) 0x69, (byte) 0x7A, (byte) 0x61, (byte) 0x62, (byte) 0x6C, (byte) 0x65, (byte) 0x3B, (byte) 0x4C, (byte) 0x00, (byte) 0x04, (byte) 0x6E, (byte) 0x61, (byte) 0x6D, (byte) 0x65, (byte) 0x74, (byte) 0x00, (byte) 0x12, (byte) 0x4C, (byte) 0x6A, (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2F, (byte) 0x6C, (byte) 0x61, (byte) 0x6E, (byte) 0x67, (byte) 0x2F, (byte) 0x53, (byte) 0x74, (byte) 0x72, (byte) 0x69, (byte) 0x6E, (byte) 0x67, (byte) 0x3B, (byte) 0x4C, (byte) 0x00, (byte) 0x06, (byte) 0x70, (byte) 0x61, (byte) 0x72, (byte) 0x65, (byte) 0x6E, (byte) 0x74, (byte) 0x74, (byte) 0x00, (byte) 0x16, (byte) 0x4C, (byte) 0x6A, (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2F, (byte) 0x69, (byte) 0x6F, (byte) 0x2F, (byte) 0x53, (byte) 0x65, (byte) 0x72, (byte) 0x69, (byte) 0x61, (byte) 0x6C, (byte) 0x69, (byte) 0x7A, (byte) 0x61, (byte) 0x62, (byte) 0x6C, (byte) 0x65, (byte) 0x3B, (byte) 0x78, (byte) 0x72, (byte) 0x00, (byte) 0x33, (byte) 0x63, (byte) 0x6F, (byte) 0x6D, (byte) 0x2E, (byte) 0x62, (byte) 0x6C, (byte) 0x61, (byte) 0x7A, (byte) 0x65, (byte) 0x62, (byte) 0x69, (byte) 0x74, (byte) 0x2E, (byte) 0x70, (byte) 0x65, (byte) 0x72, (byte) 0x73, (byte) 0x69, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x6E, (byte) 0x63, (byte) 0x65, (byte) 0x2E, (byte) 0x76, (byte) 0x69, (byte) 0x65, (byte) 0x77, (byte) 0x2E, (byte) 0x70, (byte) 0x72, (byte) 0x6F, (byte) 0x63, (byte) 0x65, (byte) 0x73, (byte) 0x73, (byte) 0x6F, (byte) 0x72, (byte) 0x2E, (byte) 0x6D, (byte) 0x6F, (byte) 0x64, (byte) 0x65, (byte) 0x6C, (byte) 0x2E, (byte) 0x42, (byte) 0x56, (byte) 0x69, (byte) 0x65, (byte) 0x77, (byte) 0xF4, (byte) 0x2F, (byte) 0x62, (byte) 0x48, (byte) 0xFA, (byte) 0x77, (byte) 0x2D, (byte) 0x22, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x4C, (byte) 0x00, (byte) 0x0C, (byte) 0x63, (byte) 0x61, (byte) 0x70, (byte) 0x74, (byte) 0x75, (byte) 0x72, (byte) 0x65, (byte) 0x64, (byte) 0x4E, (byte) 0x61, (byte) 0x6D, (byte) 0x65, (byte) 0x74, (byte) 0x00, (byte) 0x12, (byte) 0x4C, (byte) 0x6A, (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2F, (byte) 0x6C, (byte) 0x61, (byte) 0x6E, (byte) 0x67, (byte) 0x2F, (byte) 0x53, (byte) 0x74, (byte) 0x72, (byte) 0x69, (byte) 0x6E, (byte) 0x67, (byte) 0x3B, (byte) 0x78, (byte) 0x72, (byte) 0x00, (byte) 0x3A, (byte) 0x63, (byte) 0x6F, (byte) 0x6D, (byte) 0x2E, (byte) 0x62, (byte) 0x6C, (byte) 0x61, (byte) 0x7A, (byte) 0x65, (byte) 0x62, (byte) 0x69, (byte) 0x74, (byte) 0x2E, (byte) 0x70, (byte) 0x65, (byte) 0x72, (byte) 0x73, (byte) 0x69, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x6E, (byte) 0x63, (byte) 0x65, (byte) 0x2E, (byte) 0x76, (byte) 0x69, (byte) 0x65, (byte) 0x77, (byte) 0x2E, (byte) 0x70, (byte) 0x72, (byte) 0x6F, (byte) 0x63, (byte) 0x65, (byte) 0x73, (byte) 0x73, (byte) 0x6F, (byte) 0x72, (byte) 0x2E, (byte) 0x6D, (byte) 0x6F, (byte) 0x64, (byte) 0x65, (byte) 0x6C, (byte) 0x2E, (byte) 0x73, (byte) 0x75, (byte) 0x62, (byte) 0x2E, (byte) 0x42, (byte) 0x61, (byte) 0x73, (byte) 0x65, (byte) 0x56, (byte) 0x69, (byte) 0x65, (byte) 0x77, (byte) 0x45, (byte) 0x82, (byte) 0xA1, (byte) 0xAC, (byte) 0x40, (byte) 0x09, (byte) 0xDC, (byte) 0x93, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x78, (byte) 0x70, (byte) 0x70, (byte) 0x70, (byte) 0x70, (byte) 0x70};
        public Integer id;
        public String name;
        public Integer parent;

        private BViewSer() {
            super();
        }

        private BViewSer(BView self) {
            super(self);
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
    }
}
