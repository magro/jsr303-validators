package de.javakaffee.validation;

import javax.persistence.EntityManager;

/**
 * @author Martin Grotzke
 * @since 1.0
 * @version 1.0.0
 */
public interface EntityManagerAwareValidator {

	void setEntityManager(EntityManager entityManager);

}
