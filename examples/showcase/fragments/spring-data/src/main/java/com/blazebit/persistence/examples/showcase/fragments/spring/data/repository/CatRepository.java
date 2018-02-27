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

package com.blazebit.persistence.examples.showcase.fragments.spring.data.repository;

import com.blazebit.persistence.examples.showcase.fragments.spring.data.view.CatView;
import com.blazebit.persistence.spring.data.repository.EntityViewRepository;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface CatRepository extends EntityViewRepository<CatView, Integer> {

    List<CatView> findByName(String lastname);

}
