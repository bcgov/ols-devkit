package com.revolsys.io.map;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;

public class InvokeMethodMapObjectFactory extends AbstractMapObjectFactory
  implements MapSerializer {
  private final String methodName;

  private final Class<?> typeClass;

  public InvokeMethodMapObjectFactory(final String typeName, final String description,
    final Class<?> typeClass, final String methodName) {
    super(typeName, description);
    this.typeClass = typeClass;
    this.methodName = methodName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V mapToObject(final Map<String, ? extends Object> properties) {
    try {
      final Class<?> clazz = this.typeClass;
      return (V)MethodUtils.invokeStaticMethod(clazz, this.methodName, properties);
    } catch (final NoSuchMethodException e) {
      return Exceptions.throwUncheckedException(e);
    } catch (final IllegalAccessException e) {
      return Exceptions.throwUncheckedException(e);
    } catch (final InvocationTargetException e) {
      return Exceptions.throwCauseException(e);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    map.put("typeName", getTypeName());
    map.put("description", getDescription());
    map.put("typeClass", this.typeClass);
    map.put("methodName", this.methodName);
    return map;
  }
}
