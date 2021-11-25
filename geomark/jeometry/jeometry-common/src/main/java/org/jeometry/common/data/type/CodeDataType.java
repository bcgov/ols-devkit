package org.jeometry.common.data.type;

import java.util.function.Function;

import org.jeometry.common.data.identifier.Code;

public class CodeDataType extends AbstractDataType {
  private Function<Object, Object> toObjectFunction;

  public CodeDataType() {
    super("code", Code.class, true);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V, C> CodeDataType(final String typeName, final Class<? extends Code> codeClass,
    final Function<V, C> toObjectFunction) {
    super(typeName, codeClass, true);
    this.toObjectFunction = (Function)toObjectFunction;
  }

  @Override
  public boolean equals(final Object value1, final Object value2) {
    final Object code1 = Code.getCode(value1);
    final Object code2 = Code.getCode(value2);
    if (code1 == null) {
      return code2 == null;
    } else {
      return code1.equals(code2);
    }
  }

  @Override
  protected Object toObjectDo(final Object value) {
    if (this.toObjectFunction == null) {
      return super.toObjectDo(value);
    } else {
      return this.toObjectFunction.apply(value);
    }
  }

  @Override
  protected String toStringDo(final Object value) {
    if (value instanceof Code) {
      final Code code = (Code)value;
      return code.getCode().toString();
    } else {
      return super.toStringDo(value);
    }
  }
}
