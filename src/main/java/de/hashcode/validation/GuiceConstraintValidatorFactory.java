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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * A {@link ConstraintValidatorFactory} that relies on guice for creating
 * validators.
 *
 * @author <a href="mailto:martin.grotzke@googlemail.com">Martin Grotzke</a>
 */
public class GuiceConstraintValidatorFactory implements ConstraintValidatorFactory {

    private final Injector injector;

    @Inject
    public GuiceConstraintValidatorFactory(final Injector injector) {
        this.injector = injector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
        return injector.getInstance(key);
    }

}
