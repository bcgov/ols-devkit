package com.revolsys.beans;

import org.apache.commons.beanutils.converters.AbstractConverter;

public final class EnumConverter extends AbstractConverter {
  public EnumConverter() {
    super();
  }

  public EnumConverter(final Object defaultValue) {
    super(defaultValue);
  }

  @Override
  protected Object convertToType(final Class type, final Object value) throws Throwable {
    if (type.isEnum()) {
      return Enum.valueOf(type, value.toString());
    } else {
      return value;
    }
  }

  @Override
  protected Class getDefaultType() {
    return Enum.class;
  }

}
