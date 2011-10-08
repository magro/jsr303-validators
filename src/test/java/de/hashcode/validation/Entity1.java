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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Size;

import de.hashcode.validation.UniqueKey;

/**
 * An entity with a unique key and another validated property.
 *
 * @author <a href="mailto:martin.grotzke@googlemail.com">Martin Grotzke</a>
 */
@SuppressWarnings("serial")
@Entity
@UniqueKey(property = "prop1")
class Entity1 implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column
    private String prop1;
    @Column
    @Size(min = 5, max = 10)
    private String prop2;

    public Entity1() {
    }

    public Entity1(final String prop1) {
        this.prop1 = prop1;
    }

    public Entity1(final String prop1, final String prop2) {
        this.prop1 = prop1;
        this.prop2 = prop2;
    }

    public long getId() {
        return id;
    }

    public String getProp1() {
        return prop1;
    }

    public String getProp2() {
        return prop2;
    }

}