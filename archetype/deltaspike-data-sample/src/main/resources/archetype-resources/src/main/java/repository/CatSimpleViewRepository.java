/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ${package}.repository;

import com.blazebit.persistence.*;
import com.blazebit.persistence.view.*;

import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;
import com.blazebit.persistence.deltaspike.data.api.EntityViewRepository;

import java.util.List;

import ${package}.model.*;
import ${package}.view.*;

@Repository
public interface CatSimpleViewRepository extends EntityViewRepository<Cat, CatSimpleView, Long>, CriteriaSupport<Cat> {

    public default List<CatWithOwnerView> getWithOwnerView() {
        return criteria().select(CatWithOwnerView.class).orderAsc(Cat_.id).getResultList();
    }

}
