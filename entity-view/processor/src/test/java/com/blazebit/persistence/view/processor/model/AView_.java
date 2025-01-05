package com.blazebit.persistence.view.processor.model;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.StaticMetamodel;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.MethodListAttribute;
import com.blazebit.persistence.view.metamodel.MethodMultiListAttribute;
import com.blazebit.persistence.view.metamodel.MethodSingularAttribute;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;

@Generated(value = "com.blazebit.persistence.view.processor.EntityViewAnnotationProcessor")
@StaticMetamodel(AView.class)
public abstract class AView_ {

    public static volatile MethodSingularAttribute<AView, Integer> age;
    public static volatile MethodSingularAttribute<AView, byte[]> bytes;
    public static volatile MethodSingularAttribute<AView, Integer> id;
    public static volatile MethodSingularAttribute<AView, List<Map<String, String>>> jsonMap;
    public static volatile MethodSingularAttribute<AView, List<Object>> listMappingParameter;
    public static volatile MethodSingularAttribute<AView, Map<String, String>> map;
    public static volatile MethodMultiListAttribute<AView, String, Set<String>> multiNames;
    public static volatile MethodSingularAttribute<AView, String> name;
    public static volatile MethodListAttribute<AView, String> names;
    public static volatile BViewRelation<AView, MethodSingularAttribute<AView, BView>> optionalValue;
    public static volatile MethodListAttribute<AView, Serializable> test;
    public static volatile MethodSingularAttribute<AView, Serializable> test2;
    public static volatile AttributeFilterMapping<AView, Integer> id_filter;
    public static volatile AttributeFilterMapping<AView, String> name_filter;

    public static final String AGE = "age";
    public static final String BYTES = "bytes";
    public static final String ID = "id";
    public static final String JSON_MAP = "jsonMap";
    public static final String LIST_MAPPING_PARAMETER = "listMappingParameter";
    public static final String MAP = "map";
    public static final String MULTI_NAMES = "multiNames";
    public static final String NAME = "name";
    public static final String NAMES = "names";
    public static final String OPTIONAL_VALUE = "optionalValue";
    public static final String TEST = "test";
    public static final String TEST2 = "test2";
    public static final String OPTIONAL_VALUEID = "optionalValue.id";
    public static final String OPTIONAL_VALUENAME = "optionalValue.name";
    public static final String OPTIONAL_VALUEPARENT = "optionalValue.parent";

    public static EntityViewSetting<AView, CriteriaBuilder<AView>> createSettingInit(List<Object> listMappingParameter) {
        return EntityViewSetting.create(AView.class, "init")
            .withOptionalParameter("listMappingParameter", listMappingParameter);
    }

    public static EntityViewSetting<AView, PaginatedCriteriaBuilder<AView>> createPaginatedSettingInit(int firstResult, int maxResults, List<Object> listMappingParameter) {
        return EntityViewSetting.create(AView.class, firstResult, maxResults, "init")
            .withOptionalParameter("listMappingParameter", listMappingParameter);
    }

    public static void applyTest(EntityViewSetting<AView, ?> setting, Object myParam) {
        setting.withViewFilter("test").withOptionalParameter("myParam", myParam);
    }

}
