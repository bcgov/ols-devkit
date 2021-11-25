package org.jeometry.common.data.identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.Describable;

public interface Code extends Describable, Identifier {

  static <C extends Code> List<String> descriptions(final Class<C> enumClass) {
    final List<String> descriptions = new ArrayList<>();
    for (final C code : enumClass.getEnumConstants()) {
      final String description = code.getDescription();
      descriptions.add(description);
    }
    return descriptions;
  }

  static List<String> descriptions(final List<? extends Code> codes) {
    final List<String> descriptions = new ArrayList<>();
    for (final Code code : codes) {
      final String description = code.getDescription();
      descriptions.add(description);
    }
    return descriptions;
  }

  @SuppressWarnings("unchecked")
  static <C> C getCode(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Code) {
      final Code code = (Code)value;
      return code.getCode();
    } else {
      return (C)value;
    }
  }

  static <C extends Code> Map<String, C> getEnumCodeMap(final Class<C> enumClass) {
    final Map<String, C> code = new HashMap<>();
    for (final C enumValue : enumClass.getEnumConstants()) {
      final String codeId = enumValue.getCode();
      code.put(codeId, enumValue);
    }
    return Collections.unmodifiableMap(code);
  }

  @Override
  default boolean equals(final Identifier identifier) {
    return equals((Object)identifier);
  }

  default boolean equalsCode(final Object code) {
    if (code == null) {
      return false;
    } else if (code == this) {
      return true;
    } else {
      final Object codeThis = getCode();
      return codeThis.equals(code);
    }
  }

  <C> C getCode();

  @Override
  default <V> V getValue(final int index) {
    if (index == 0) {
      return getCode();
    } else {
      return null;
    }
  }

  @Override
  default List<Object> getValues() {
    final Object code = getCode();
    return Collections.singletonList(code);
  }

  @Override
  default String toIdString() {
    return getCode().toString();
  }
}
