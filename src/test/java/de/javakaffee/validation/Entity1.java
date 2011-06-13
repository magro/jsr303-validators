package de.javakaffee.validation;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Size;

/**
 * An entity with a unique key and another validated property.
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
		return this.id;
	}

	public String getProp1() {
		return this.prop1;
	}

	public String getProp2() {
		return this.prop2;
	}

}