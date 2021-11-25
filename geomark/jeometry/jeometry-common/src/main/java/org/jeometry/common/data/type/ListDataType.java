package org.jeometry.common.data.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListDataType extends SimpleDataType {

  private final DataType contentType;

  public ListDataType(final Class<?> javaClass, final DataType contentType) {
    this("List", javaClass, contentType);
  }

  public ListDataType(final String name, final Class<?> javaClass, final DataType contentType) {
    super(name, javaClass);
    this.contentType = contentType;
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    final List<?> list1 = (List<?>)value1;
    final List<?> list2 = (List<?>)value2;
    if (list1.size() != list2.size()) {
      return false;
    } else {
      for (int i = 0; i < list1.size(); i++) {
        final Object value11 = list1.get(i);
        final Object value21 = list2.get(i);
        if (!DataType.equal(value11, value21)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final List<?> list1 = (List<?>)value1;
    final List<?> list2 = (List<?>)value2;
    if (list1.size() != list2.size()) {
      return false;
    } else {
      for (int i = 0; i < list1.size(); i++) {
        final Object value11 = list1.get(i);
        final Object value21 = list2.get(i);
        if (!DataType.equal(value11, value21, excludeFieldNames)) {
          return false;
        }
      }
    }
    return true;
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
        final List<Object> newCollection;
        if (List.class == javaClass) {
          newCollection = new ArrayList<>();
        } else {
          final Constructor<?> declaredConstructor = javaClass.getDeclaredConstructor();
          newCollection = (List)declaredConstructor.newInstance();
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
