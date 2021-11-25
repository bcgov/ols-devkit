package com.revolsys.record.filter;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import com.revolsys.record.Record;

public class RecordAccessor implements PropertyAccessor {

  @SuppressWarnings("serial")
  private static class RecordAccessException extends AccessException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String key;

    public RecordAccessException(final String key) {
      super(null);
      this.key = key;
    }

    @Override
    public String getMessage() {
      return "Record does not contain a value for key '" + this.key + "'";
    }
  }

  @Override
  public boolean canRead(final EvaluationContext context, final Object target, final String name)
    throws AccessException {
    final Record object = (Record)target;
    return object.hasField(name);
  }

  @Override
  public boolean canWrite(final EvaluationContext context, final Object target, final String name)
    throws AccessException {
    return true;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Class[] getSpecificTargetClasses() {
    return new Class[] {
      Record.class
    };
  }

  @Override
  public TypedValue read(final EvaluationContext context, final Object target, final String name)
    throws AccessException {
    final Record object = (Record)target;
    final Object value = object.getValue(name);
    if (value == null && !object.hasField(name)) {
      throw new RecordAccessException(name);
    }
    return new TypedValue(value);
  }

  @Override
  public void write(final EvaluationContext context, final Object target, final String name,
    final Object newValue) throws AccessException {
    final Record object = (Record)target;
    object.setValue(name, newValue);
  }

}
