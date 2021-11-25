package org.jeometry.common.data.type;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jeometry.common.function.Function3;

public class FunctionDataType extends AbstractDataType {

  @SuppressWarnings("unchecked")
  public static BiFunction<? extends Object, ? extends Object, Boolean> MAP_EQUALS = (object1,
    object2) -> {
    final Map<Object, Object> map1 = (Map<Object, Object>)object1;
    final Map<Object, Object> map2 = (Map<Object, Object>)object2;
    if (map1.size() == map2.size()) {
      final Set<Object> keys1 = map1.keySet();
      final Set<Object> keys2 = map2.keySet();
      if (keys1.equals(keys2)) {
        for (final Object key : keys1) {
          final Object value1 = map1.get(key);
          final Object value2 = map2.get(key);
          if (!DataType.equal(value1, value2)) {
            return false;
          }
        }
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  };

  @SuppressWarnings("unchecked")
  public static final Function3<Object, Object, Collection<? extends CharSequence>, Boolean> MAP_EQUALS_EXCLUDES = (
    object1, object2, exclude) -> {
    final Map<Object, Object> map1 = (Map<Object, Object>)object1;
    final Map<Object, Object> map2 = (Map<Object, Object>)object2;
    final Set<Object> keys = new TreeSet<>();
    keys.addAll(map1.keySet());
    keys.addAll(map2.keySet());
    keys.removeAll(exclude);

    for (final Object key : keys) {
      final Object value1 = map1.get(key);
      final Object value2 = map2.get(key);
      if (!DataType.equal(value1, value2, exclude)) {
        return false;
      }
    }
    return true;
  };

  public static FunctionDataType newToObjectEquals(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction,
    final BiFunction<? extends Object, ? extends Object, Boolean> equalsFunction) {
    return new FunctionDataType(name, javaClass, true, toObjectFunction, null, equalsFunction,
      null);
  }

  private final Function<Object, ?> toObjectFunction;

  private final Function<Object, String> toStringFunction;

  private final BiFunction<Object, Object, Boolean> equalsFunction;

  private final Function3<Object, Object, Collection<? extends CharSequence>, Boolean> equalsExcludesFunction;

  public FunctionDataType(final String name, final Class<?> javaClass, final boolean requiresQuotes,
    final Function<Object, ?> function) {
    this(name, javaClass, requiresQuotes, function, null, null, null);
  }

  public FunctionDataType(final String name, final Class<?> javaClass, final boolean requireQuotes,
    final Function<Object, ?> toObjectFunction,
    final BiFunction<? extends Object, ? extends Object, Boolean> equalsFunction) {
    this(name, javaClass, requireQuotes, toObjectFunction, null, equalsFunction, null);
  }

  public FunctionDataType(final String name, final Class<?> javaClass, final boolean requiresQuotes,
    final Function<Object, ?> toObjectFunction, final Function<Object, String> toStringFunction) {
    this(name, javaClass, requiresQuotes, toObjectFunction, toStringFunction, null, null);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public FunctionDataType(final String name, final Class<?> javaClass, final boolean requiresQuotes,
    final Function<Object, ?> toObjectFunction, final Function<Object, String> toStringFunction,
    final BiFunction<?, ?, Boolean> equalsFunction,
    final Function3<Object, Object, Collection<? extends CharSequence>, Boolean> equalsExcludesFunction) {
    super(name, javaClass, requiresQuotes);
    this.toObjectFunction = toObjectFunction;
    if (toStringFunction == null) {
      this.toStringFunction = Object::toString;
    } else {
      this.toStringFunction = toStringFunction;
    }
    if (equalsFunction == null) {
      if (equalsExcludesFunction == null) {
        this.equalsFunction = Object::equals;
      } else {
        this.equalsFunction = (value1, value2) -> {
          return equalsNotNull(value1, value2, Collections.emptySet());
        };
      }
    } else {
      this.equalsFunction = (BiFunction)equalsFunction;
    }
    if (equalsExcludesFunction == null) {
      if (equalsFunction == null) {
        this.equalsExcludesFunction = (value1, value2, excludeFieldNames) -> {
          return value1.equals(value2);
        };
      } else {
        this.equalsExcludesFunction = (value1, value2, excludeFieldNames) -> {
          return this.equalsFunction.apply(value1, value2);
        };
      }
    } else {
      this.equalsExcludesFunction = equalsExcludesFunction;
    }
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> function) {
    this(name, javaClass, true, function);
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction,
    final BiFunction<? extends Object, ? extends Object, Boolean> equalsFunction,
    final Function3<Object, Object, Collection<? extends CharSequence>, Boolean> equalsExcludesFunction) {
    this(name, javaClass, true, toObjectFunction, null, equalsFunction, equalsExcludesFunction);
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction, final Function<Object, String> toStringFunction) {
    this(name, javaClass, true, toObjectFunction, toStringFunction);
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction, final Function<Object, String> toStringFunction,
    final BiFunction<?, ?, Boolean> equalsFunction) {
    this(name, javaClass, true, toObjectFunction, toStringFunction, equalsFunction, null);
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction,
    final Function3<Object, Object, Collection<? extends CharSequence>, Boolean> equalsExcludesFunction) {
    this(name, javaClass, true, toObjectFunction, null, null, equalsExcludesFunction);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return this.equalsFunction.apply(value1, value2);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    return this.equalsExcludesFunction.apply(value1, value2, excludeFieldNames);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    return this.toObjectFunction.apply(value);
  }

  @Override
  public String toStringDo(final Object value) {
    return this.toStringFunction.apply(value);
  }
}
