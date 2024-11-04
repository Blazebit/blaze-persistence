/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.repository;

import com.blazebit.persistence.view.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import ${package}.view.*;

@Repository
@Transactional(readOnly = true)
public interface CatSimpleViewRepository extends EntityViewRepository<CatSimpleView, Long> {
}
