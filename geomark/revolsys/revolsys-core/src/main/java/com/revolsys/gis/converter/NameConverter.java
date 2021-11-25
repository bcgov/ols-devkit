package com.revolsys.gis.converter;

import org.springframework.core.convert.converter.Converter;

public interface NameConverter extends Converter<String, String> {
  @Override
  String convert(String name);
}
