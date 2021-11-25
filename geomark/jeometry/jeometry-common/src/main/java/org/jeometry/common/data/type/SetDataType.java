package org.jeometry.common.data.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class SetDataType extends SimpleDataType {

  private final DataType contentType;

  public SetDataType(final Class<?> javaClass, final DataType contentType) {
    super("Set", javaClass);
    this.contentType = contentType;
  }

  public DataType getContentType() {
    return this.contentType;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  protected Object toObjectDo(final Object value) {
    if (value instanceof Collection) {
      try {
        final Collection<?> collection = (Collection<?>)value;
        final Class<?> javaClass = getJavaClass();
        final Set<Object> newCollection;
        if (Set.class == javaClass) {
          newCollection = new LinkedHashSet();
        } else {
          final Constructor<?> declaredConstructor = javaClass.getDeclaredConstructor();
          newCollection = (Set)declaredConstructor.newInstance();
        }
        newCollection.addAll(collection);
        return newCollection;
      } catch (InvocationTargetException | NoSuchMethodException | InstantiationException
          | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    } else {
      return super.toObjectDo(value);
    }
  }
}
