package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.StaticMetamodel;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.MethodSingularAttribute;

import javax.annotation.processing.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
@StaticMetamodel(BView.class)
public abstract class BView_ {

    public static volatile MethodSingularAttribute<BView, Integer> id;
    public static volatile MethodSingularAttribute<BView, String> name;
    public static volatile MethodSingularAttribute<BView, Integer> parent;
    public static volatile AttributeFilterMapping<BView, Integer> id_filter;

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PARENT = "parent";

    public static EntityViewSetting<BView, CriteriaBuilder<BView>> createSettingCreate() {
        return EntityViewSetting.create(BView.class, "create");
    }

    public static EntityViewSetting<BView, PaginatedCriteriaBuilder<BView>> createPaginatedSettingCreate(int firstResult, int maxResults) {
        return EntityViewSetting.create(BView.class, firstResult, maxResults, "create");
    }

    public static EntityViewSetting<BView, CriteriaBuilder<BView>> createSettingInit() {
        return EntityViewSetting.create(BView.class, "init");
    }

    public static EntityViewSetting<BView, PaginatedCriteriaBuilder<BView>> createPaginatedSettingInit(int firstResult, int maxResults) {
        return EntityViewSetting.create(BView.class, firstResult, maxResults, "init");
    }

}
