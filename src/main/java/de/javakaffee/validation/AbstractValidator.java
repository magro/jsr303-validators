package de.javakaffee.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.validation.ConstraintValidator;

/**
 * Abstract base class for annotation based validators.
 * 
 * @author Martin Grotzke
 * @since 1.0
 * @version 1.0.0
 */
public abstract class AbstractValidator<T extends Annotation> implements ConstraintValidator<T, Object> {

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
