package org.jeometry.common.data.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

public class CollectionDataType extends SimpleDataType {

  private final DataType contentType;

  public CollectionDataType(final String name, final Class<?> javaClass,
    final DataType contentType) {
    super(name, javaClass);
    this.contentType = contentType;
  }

  public DataType getContentType() {
    return this.contentType;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  protected Object toObjectDo(final Object value) {
    if (value instanceof Collection) {
      try {
        final Collection<?> collection = (Collection<?>)value;
        final Class<?> javaClass = getJavaClass();
        final Collection<Object> newCollection;
        if (Collection.class == javaClass) {
          newCollection = new ArrayList<>();
        } else {
          final Constructor<?> declaredConstructor = javaClass.getDeclaredConstructor();
          newCollection = (Collection)declaredConstructor.newInstance();
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
