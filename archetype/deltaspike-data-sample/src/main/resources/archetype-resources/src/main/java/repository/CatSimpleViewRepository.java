/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.repository;

import com.blazebit.persistence.*;
import com.blazebit.persistence.view.*;

import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;
import com.blazebit.persistence.deltaspike.data.EntityViewRepository;

import java.util.List;

import ${package}.model.*;
import ${package}.view.*;

@Repository
public interface CatSimpleViewRepository extends EntityViewRepository<Cat, CatSimpleView, Long>, CriteriaSupport<Cat> {

    public default List<CatWithOwnerView> getWithOwnerView() {
        return criteria().select(CatWithOwnerView.class).orderAsc(Cat_.id).getResultList();
    }

}
