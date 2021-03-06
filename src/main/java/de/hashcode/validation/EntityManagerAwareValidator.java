/*
 * Copyright 2011 Martin Grotzke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hashcode.validation;

import javax.persistence.EntityManager;

/**
 * Interface for validators that are interested in an {@link EntityManager},
 * when the {@link ConstraintValidatorFactoryEMFImpl} is used.
 *
 * @author <a href="mailto:martin.grotzke@googlemail.com">Martin Grotzke</a>
 */
public interface EntityManagerAwareValidator {

    /**
     * Set the {@link EntityManager}.
     */
    void setEntityManager(EntityManager entityManager);

}
