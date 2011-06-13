package de.javakaffee.validation;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Martin Grotzke
 * @since 1.0
 * @version 1.0.0
 */
public class UniqueKeyValidator implements ConstraintValidator<UniqueKey, Serializable>,
		EntityManagerAwareValidator {

	private EntityManager entityManager;
	private UniqueKey constraintAnnotation;

	public UniqueKeyValidator() {
	}

	public UniqueKeyValidator(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void setEntityManager(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	@Override
	public void initialize(final UniqueKey constraintAnnotation) {
		this.constraintAnnotation = constraintAnnotation;

	}

	@Override
	public boolean isValid(final Serializable target, final ConstraintValidatorContext context) {

		if (this.entityManager == null) {
			// eclipselink may be configured with a BeanValidationListener that
			// validates an entity on prePersist
			// In this case we don't want to and we cannot check anything (the
			// entityManager is not set)
			//
			// Alternatively, you can disalbe bean validation during jpa
			// operations
			// by adding the property "javax.persistence.validation.mode" with
			// value "NONE" to persistence.xml
			return true;
		}

		final Class<?> entityClass = target.getClass();

		final CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();

		final CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();

		final Root<?> root = criteriaQuery.from(entityClass);

		try {
			final Object propertyValue = getPropertyValue(target, this.constraintAnnotation.property());
			final Predicate uniquePropertyPredicate =
					criteriaBuilder.equal(root.get(this.constraintAnnotation.property()), propertyValue);

			final Field idField = getIdField(entityClass);
			final String idProperty = idField.getName();
			final Object idValue = getPropertyValue(target, idProperty);

			if (idValue != null) {
				final Predicate idNotEqualsPredicate =
						criteriaBuilder.notEqual(root.get(idProperty), idValue);
				criteriaQuery.where(uniquePropertyPredicate, idNotEqualsPredicate);
			} else {
				criteriaQuery.where(uniquePropertyPredicate);
			}

		} catch (final Exception e) {
			throw new RuntimeException(
					"An error occurred when trying to create the jpa predicate for the @UniqueKey '"
							+ this.constraintAnnotation.property() + "' on bean " + entityClass + ".", e);
		}

		final List<Object> resultSet = this.entityManager.createQuery(criteriaQuery).getResultList();

		if (!resultSet.isEmpty()) {
			context.buildConstraintViolationWithTemplate(this.constraintAnnotation.message()).addNode(
					this.constraintAnnotation.property()).addConstraintViolation().disableDefaultConstraintViolation();
			return false;
		}

		return true;
	}

	private Field getIdField(final Class<?> entityClass) {
		final Field[] fields = entityClass.getDeclaredFields();
		for (final Field field : fields) {
			final Id annotation = field.getAnnotation(Id.class);
			if (annotation != null) {
				return field;
			}
		}
		if (entityClass.getSuperclass() != Object.class) {
			return getIdField(entityClass.getSuperclass());
		}
		throw new IllegalArgumentException("No id field found on class " + entityClass);
	}

	/**
	 * Read the value of the specified propertyName from the given object. This
	 * implementation allows properties/fields with only getters (immutable
	 * beans) and does not rely on java beans standard (PropertyDescriptor)
	 * which would require also a setter method (the alternative would be s.th.
	 * like
	 * <code>new PropertyDescriptor(propertyName, entityClass).getReadMethod().invoke(target);</code>
	 * ).
	 */
	@CheckForNull
	protected Object getPropertyValue(@Nonnull final Object object, @Nonnull final String propertyName)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		final Method method = getGetter(object.getClass(), propertyName);
		return method.invoke(object, (Object[])null);
	}

	@Nonnull
	private String toMethodName(@Nonnull final String methodPrefix, @Nonnull final String propertyName) {
		return methodPrefix + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH)
				+ propertyName.substring(1);
	}

	@Nonnull
	private Method getGetter(@Nonnull final Class<?> clazz, @Nonnull final String property) {
		final String get = toMethodName("get", property);
		final String is = toMethodName("is", property);
		Method getter;
		try {
			getter = clazz.getMethod(get);
		} catch (final NoSuchMethodException noSuchMethodException) {
			try {
				getter = clazz.getMethod(is);
			} catch (final NoSuchMethodException noSuchMethodException2) {
				throw new RuntimeException("Getter for '" + clazz.getSimpleName() + '#' + property
						+ "' not found.", noSuchMethodException2);
			} catch (final SecurityException securityException) {
				throw new RuntimeException("Method '" + clazz.getSimpleName() + '#' + is
						+ "()' is protected by the security manager.");
			}
		} catch (final SecurityException securityException) {
			try {
				getter = clazz.getMethod(is);
			} catch (final NoSuchMethodException noSuchMethodException) {
				throw new RuntimeException("Method '" + clazz.getSimpleName() + '#' + get
						+ "()' is protected by the security manager.");
			} catch (final SecurityException securityException2) {
				throw new RuntimeException("Methods '" + clazz.getSimpleName() + '#' + get + "()' and "
						+ clazz.getSimpleName() + '#' + is + "()' are protected by the security manager.");
			}
		}
		return getter;
	}

}
