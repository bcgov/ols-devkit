package com.revolsys.collection.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.util.BaseCloneable;
import com.revolsys.util.Property;

public interface Maps {
  public static final Supplier<Map<?, ?>> FACTORY_TREE = () -> {
    return new TreeMap<>();
  };

  public static final Supplier<Map<?, ?>> FACTORY_LINKED_HASH = () -> {
    return new LinkedHashMap<>();
  };

  public static final Supplier<Map<?, ?>> FACTORY_HASH = () -> {
    return new HashMap<>();
  };

  static <K1, V> boolean addAllToSet(final Map<K1, Set<V>> map, final K1 key1,
    final Collection<? extends V> values) {
    if (Property.hasValue(values)) {
      final Set<V> set = getSet(map, key1);
      return set.addAll(values);
    } else {
      return true;
    }
  }

  static <K> Integer addCount(final Map<K, Integer> counts, final K key) {
    Integer count = counts.get(key);
    if (count == null) {
      count = 1;
    } else {
      count++;
    }
    counts.put(key, count);
    return count;
  }

  static <K, V, C extends Collection<V>> boolean addToCollection(final Supplier<C> supplier,
    final Map<K, C> map, final K key, final V value) {
    final C values = get(map, key, supplier);
    return values.add(value);
  }

  static <K1, K2, C extends Collection<V>, V> boolean addToCollection(final Supplier<C> factory,
    final Map<K1, Map<K2, C>> map, final K1 key1, final K2 key2, final V value) {
    final C values = getCollection(factory, map, key1, key2);
    return values.add(value);
  }

  static <K1, V> boolean addToList(final Map<K1, List<V>> map, final K1 key1, final V value) {
    if (map != null && key1 != null) {
      final List<V> values = getList(map, key1);
      return values.add(value);
    } else {
      return false;
    }
  }

  static <K1, K2, V> boolean addToList(final Map<K1, Map<K2, List<V>>> map, final K1 key1,
    final K2 key2, final V value) {
    final List<V> values = getList(map, key1, key2);
    return values.add(value);
  }

  static <K1, K2, V> boolean addToList(final Supplier<Map<K2, List<V>>> supplier,
    final Map<K1, Map<K2, List<V>>> map, final K1 key1, final K2 key2, final V value) {
    final List<V> values = getList(supplier, map, key1, key2);
    return values.add(value);
  }

  static <K1, K2, V> V addToMap(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2,
    final V value) {
    final Map<K2, V> mapValue = getMap(map, key1);
    return mapValue.put(key2, value);
  }

  static <K1, K2, V> V addToMap(final Supplier<Map<K2, V>> supplier, final Map<K1, Map<K2, V>> map,
    final K1 key1, final K2 key2, final V value) {
    final Map<K2, V> mapValue = getMap(supplier, map, key1);
    return mapValue.put(key2, value);
  }

  static <K1, V> boolean addToSet(final Map<K1, Set<V>> map, final K1 key1, final V value) {
    final Set<V> values = getSet(map, key1);
    return values.add(value);
  }

  static <K1, V> boolean addToTreeSet(final Map<K1, Set<V>> map, final Comparator<V> comparator,
    final K1 key1, final V value) {
    final Set<V> values = getTreeSet(map, comparator, key1);
    return values.add(value);
  }

  static <K1, V> boolean addToTreeSet(final Map<K1, Set<V>> map, final K1 key1, final V value) {
    final Set<V> values = getTreeSet(map, key1);
    if (values == null) {
      return false;
    } else {
      return values.add(value);
    }
  }

  static <K, V> MapBuilder<K, V> buildHash() {
    final Map<K, V> map = newHash();
    return new MapBuilder<>(map);
  }

  static <K, V> MapBuilder<K, V> buildHashEx() {
    final Map<K, V> map = newHash();
    return new MapBuilder<>(map);
  }

  static <K, V> MapBuilder<K, V> buildLinkedHash() {
    final Map<K, V> map = newLinkedHash();
    return new MapBuilder<>(map);
  }

  static <K, V> MapBuilder<K, V> buildLinkedHash(final Map<K, V> values) {
    final Map<K, V> map = newLinkedHash(values);
    return new MapBuilder<>(map);
  }

  static <K, V> MapBuilder<K, V> buildTree() {
    final Map<K, V> map = newTree();
    return new MapBuilder<>(map);
  }

  static <K, V> boolean collectionContains(final Map<K, ? extends Collection<? extends V>> map,
    final K key, final V value) {
    if (map == null || key == null) {
      return false;
    } else {
      final Collection<? extends V> collection = map.get(key);
      if (collection == null) {
        return false;
      } else {
        return collection.contains(value);
      }
    }
  }

  static <K1, V> boolean containsInCollection(final Map<K1, ? extends Collection<V>> map,
    final K1 key, final V value) {
    final Collection<V> collection = map.get(key);
    if (collection == null) {
      return false;
    } else {
      return collection.contains(value);
    }
  }

  static <K1, K2, V> boolean containsKey(final Map<K1, Map<K2, V>> map, final K1 key1,
    final K2 key2) {
    final Map<K2, V> mapValue = getMap(map, key1);
    return mapValue.containsKey(key2);
  }

  static <T> Integer decrementCount(final Map<T, Integer> counts, final T key) {
    Integer count = counts.get(key);
    if (count == null) {
      return 0;
    } else {
      count--;
      if (count <= 0) {
        counts.remove(key);
      } else {
        counts.put(key, count);
      }
      return count;
    }
  }

  static boolean equalMap1Keys(final Map<String, Object> map1, final Map<String, Object> map2) {
    if (map1 == null) {
      return false;
    } else if (map2 == null) {
      return false;
    } else {
      for (final String key : map1.keySet()) {
        final boolean equals = equals(map1, map2, key);
        if (!equals) {
          return false;
        }
      }

      return true;
    }
  }

  static boolean equals(final Map<String, Object> map1, final Map<String, Object> map2,
    final String key) {
    final Object value1 = map1.get(key);
    final Object value2 = map2.get(key);
    final boolean equals = DataType.equal(value1, value2);
    return equals;
  }

  static boolean equalsNotNull(final Map<Object, Object> map1, final Map<Object, Object> map2) {
    final Set<Object> keys = new TreeSet<>();
    keys.addAll(map1.keySet());
    keys.addAll(map2.keySet());

    for (final Object key : keys) {
      final Object value1 = map1.get(key);
      final Object value2 = map2.get(key);
      if (!DataType.equal(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  static boolean equalsNotNull(final Map<Object, Object> map1, final Map<Object, Object> map2,
    final Collection<? extends CharSequence> exclude) {
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
  }

  @SuppressWarnings("unchecked")
  static boolean equalsNotNull(final Object map1, final Object map2) {
    return equalsNotNull((Map<Object, Object>)map1, (Map<Object, Object>)map2);
  }

  @SuppressWarnings("unchecked")
  static boolean equalsNotNull(final Object map1, final Object map2,
    final Collection<? extends CharSequence> exclude) {
    return equalsNotNull((Map<Object, Object>)map1, (Map<Object, Object>)map2, exclude);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static <K, V> Supplier<Map<K, V>> factoryHash() {
    return (Supplier)FACTORY_HASH;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static <K, V> Supplier<Map<K, V>> factoryLinkedHash() {
    return (Supplier)FACTORY_LINKED_HASH;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static <K, V> Supplier<Map<K, V>> factoryTree() {
    return (Supplier)FACTORY_TREE;
  }

  static <V> V first(final Map<?, V> map) {
    if (Property.hasValue(map)) {
      return map.values().iterator().next();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  static <K, V> V get(final Map<K, ? extends Object> map, final K key) {
    if (map == null) {
      return null;
    } else {
      return (V)map.get(key);
    }
  }

  /**
   * Get the value for the key from the map. If the value was null return
   * default Value instead. The default value will be added to the map.
   *
   * @param map The map.
   * @param key The key to return the value for.
   * @param defaultValue The default value.
   * @return The value.
   */
  static <K, V> V get(final Map<K, ? super V> map, final K key, final V defaultValue) {
    if (map == null) {
      return defaultValue;
    } else {
      @SuppressWarnings("unchecked")
      final V value = (V)map.get(key);
      if (value == null) {
        map.put(key, defaultValue);
        return defaultValue;
      } else {
        return value;
      }
    }
  }

  static <K, V> V get(final Map<K, V> map, final K key, final Function<K, V> defaultFactory) {
    V value = map.get(key);
    if (value == null) {
      value = defaultFactory.apply(key);
      map.put(key, value);
    }
    return value;
  }

  static <K, V> V get(final Map<K, V> map, final K key, final Supplier<V> defaultFactory) {
    V value = map.get(key);
    if (value == null) {
      value = defaultFactory.get();
      map.put(key, value);
    }
    return value;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static <K, V> V get(final Supplier<V> supplier, final Map<K, ? extends Object> map, final K key) {
    V value = (V)map.get(key);
    if (value == null) {
      value = supplier.get();
      ((Map)map).put(key, value);
    }
    return value;
  }

  static boolean getBool(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return false;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.parseBoolean(value.toString());
    }
  }

  static boolean getBool(final Map<String, ? extends Object> map, final String name,
    final boolean defaultValue) {
    final Object value = get(map, name);
    if (value == null) {
      return defaultValue;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.parseBoolean(value.toString());
    }
  }

  static Boolean getBoolean(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.valueOf(value.toString());
    }
  }

  static <K, C extends Collection<V>, V> C getCollection(final Supplier<C> factory,
    final Map<K, C> map, final K key) {
    C collection = map.get(key);
    if (collection == null && factory != null) {
      collection = factory.get();
      map.put(key, collection);
    }
    return collection;
  }

  static <K1, K2, C extends Collection<V>, V> C getCollection(final Supplier<C> factory,
    final Map<K1, Map<K2, C>> map, final K1 key1, final K2 key2) {
    final Map<K2, C> map2 = getMap(map, key1);
    final C collecion = getCollection(factory, map2, key2);
    return collecion;
  }

  static <T> Integer getCount(final Map<T, Integer> counts, final T key) {
    final Integer count = counts.get(key);
    if (count == null) {
      return 0;
    } else {
      return count;
    }
  }

  static <K> Double getDouble(final Map<K, ? extends Object> map, final K name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else {
      final String stringValue = value.toString();
      if (Property.hasValue(stringValue)) {
        try {
          return Double.valueOf(stringValue);
        } catch (final NumberFormatException e) {
          return null;
        }
      } else {
        return null;
      }
    }
  }

  static <K> double getDouble(final Map<K, ? extends Object> object, final K name,
    final double defaultValue) {
    final Double value = getDouble(object, name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  static Double getDoubleValue(final Map<String, ? extends Object> map, final String name) {
    final Number value = (Number)get(map, name);
    if (value == null) {
      return null;
    } else {
      return value.doubleValue();
    }
  }

  static <K> K getFirstKey(final Map<K, ?> map) {
    return map.keySet().iterator().next();
  }

  static <V> V getFirstValue(final Map<?, V> map) {
    return map.values().iterator().next();
  }

  static <K> Integer getInteger(final Map<K, ? extends Object> map, final K name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      final String stringValue = value.toString();
      if (Property.hasValue(stringValue)) {
        try {
          return Integer.valueOf(stringValue);
        } catch (final NumberFormatException e) {
          return null;
        }
      } else {
        return null;
      }
    }
  }

  static <K> int getInteger(final Map<K, ? extends Object> object, final K name,
    final int defaultValue) {
    final Integer value = getInteger(object, name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  static <K, V> List<V> getList(final Map<K, List<V>> map, final K key) {
    List<V> list = map.get(key);
    if (list == null) {
      list = new ArrayList<>();
      map.put(key, list);
    }
    return list;
  }

  static <K1, K2, V> List<V> getList(final Map<K1, Map<K2, List<V>>> map, final K1 key1,
    final K2 key2) {
    final Map<K2, List<V>> map2 = getMap(map, key1);
    final List<V> list = getList(map2, key2);
    return list;
  }

  static <K1, K2, V> List<V> getList(final Supplier<Map<K2, List<V>>> supplier,
    final Map<K1, Map<K2, List<V>>> map, final K1 key1, final K2 key2) {
    final Map<K2, List<V>> map2 = getMap(supplier, map, key1);
    final List<V> list = getList(map2, key2);
    return list;
  }

  static Long getLong(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      final String stringValue = value.toString();
      if (Property.hasValue(stringValue)) {
        try {
          return Long.valueOf(stringValue);
        } catch (final NumberFormatException e) {
          return null;
        }
      } else {
        return null;
      }
    }
  }

  static long getLong(final Map<String, ? extends Object> map, final String name,
    final long defaultValue) {
    final Object value = get(map, name);
    if (value == null) {
      return defaultValue;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      final String stringValue = value.toString();
      if (Property.hasValue(stringValue)) {
        try {
          return Long.valueOf(stringValue);
        } catch (final NumberFormatException e) {
          throw new IllegalArgumentException(value + " is not a valid long");
        }
      } else {
        return defaultValue;
      }
    }
  }

  static <K1, K2, V> Map<K2, V> getMap(final Map<K1, Map<K2, V>> map, final K1 key) {
    Map<K2, V> value = map.get(key);
    if (value == null) {
      value = newLinkedHash();
      map.put(key, value);
    }
    return value;
  }

  static <K1, K2, V> V getMap(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2) {
    final Map<K2, V> values = getMap(map, key1);
    return values.get(key2);
  }

  static <K1, K2, V> V getMap(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2,
    final Supplier<V> supplier) {
    final Map<K2, V> values = getMap(map, key1);
    return get(supplier, values, key2);
  }

  static <K1, K2, V> V getMap(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2,
    final V defaultValue) {
    final Map<K2, V> values = getMap(map, key1);
    final V value = values.get(key2);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  static <K1, K2, V> Map<K2, V> getMap(final Supplier<Map<K2, V>> supplier,
    final Map<K1, Map<K2, V>> map, final K1 key) {
    Map<K2, V> value = map.get(key);
    if (value == null) {
      value = supplier.get();
      map.put(key, value);
    }
    return value;
  }

  static <K, V> List<V> getNotNull(final Map<K, V> map, final Collection<K> keys) {
    final List<V> values = new ArrayList<>();
    if (keys != null) {
      for (final K key : keys) {
        final V value = map.get(key);
        if (value != null) {
          values.add(value);
        }
      }
    }
    return values;
  }

  static <K, V> Set<V> getSet(final Map<K, Set<V>> map, final K key) {
    Set<V> value = map.get(key);
    if (value == null) {
      value = new LinkedHashSet<>();
      map.put(key, value);
    }
    return value;
  }

  static String getString(final Map<String, ? extends Object> map, final String name) {
    final Object value = get(map, name);
    if (value == null) {
      return null;
    } else {
      return DataTypes.toString(value);
    }
  }

  static String getString(final Map<String, ? extends Object> map, final String name,
    final String defaultValue) {
    final Object value = get(map, name);
    if (value == null) {
      return defaultValue;
    } else {
      return DataTypes.toString(value);
    }
  }

  static <K1, K2, V> Map<K2, V> getTreeMap(final Map<K1, Map<K2, V>> map, final K1 key) {
    Map<K2, V> value = map.get(key);
    if (value == null) {
      value = newTree();
      map.put(key, value);
    }
    return value;
  }

  static <K, V> Set<V> getTreeSet(final Map<K, Set<V>> map, final Comparator<V> comparator,
    final K key) {
    Set<V> value = map.get(key);
    if (value == null) {
      value = new TreeSet<>(comparator);
      map.put(key, value);
    }
    return value;
  }

  static <K, V> Set<V> getTreeSet(final Map<K, Set<V>> map, final K key) {
    if (key == null) {
      return null;
    } else {
      Set<V> value = map.get(key);
      if (value == null) {
        value = new TreeSet<>();
        map.put(key, value);
      }
      return value;
    }
  }

  static <K1, K2, V> V getValue(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2) {
    final Map<K2, V> map2 = getMap(map, key1);
    return map2.get(key2);
  }

  static <K> boolean hasValue(final Map<K, ?> map, final K key) {
    if (map == null || key == null) {
      return false;
    } else {
      final Object value = map.get(key);
      return Property.hasValue(value);
    }
  }

  static boolean isNotNullAndNotZero(final Map<String, Object> object, final String name) {
    final Integer value = getInteger(object, name);
    if (value == null || value == 0) {
      return false;
    } else {
      return true;
    }
  }

  static <K, V> void mergeCollection(final Map<K, Collection<V>> map,
    final Map<K, Collection<V>> otherMap) {
    for (final Entry<K, Collection<V>> entry : otherMap.entrySet()) {
      final K key = entry.getKey();
      Collection<V> collection = map.get(key);
      final Collection<V> otherCollection = otherMap.get(key);
      if (collection == null) {
        collection = BaseCloneable.clone(otherCollection);
        map.put(key, collection);
      } else {
        for (final V value : otherCollection) {
          if (!collection.contains(value)) {
            collection.add(value);
          }
        }
      }
    }
  }

  static <V, K> HashMap<K, V> newHash() {
    return new HashMap<>();
  }

  static <K, V> Map<K, V> newHash(final K key, final V value) {
    final Map<K, V> map = newHash();
    map.put(key, value);
    return map;
  }

  static <K, V> Map<K, V> newHash(final Map<K, ? extends V> map) {
    final Map<K, V> copy = newHash();
    if (map != null) {
      copy.putAll(map);
    }
    return copy;
  }

  static <K, V> LinkedHashMap<K, V> newLinkedHash() {
    return new LinkedHashMap<>();
  }

  static <K, V> Map<K, V> newLinkedHash(final K key, final V value) {
    final Map<K, V> map = newLinkedHash();
    map.put(key, value);
    return map;
  }

  static <T1, T2> Map<T1, T2> newLinkedHash(final List<T1> sourceValues,
    final List<T2> targetValues) {
    final Map<T1, T2> map = newLinkedHash();
    for (int i = 0; i < sourceValues.size() && i < targetValues.size(); i++) {
      final T1 sourceValue = sourceValues.get(i);
      final T2 targetValue = targetValues.get(i);
      map.put(sourceValue, targetValue);
    }
    return map;
  }

  static <K, V> Map<K, V> newLinkedHash(final Map<K, ? extends V> map) {
    final Map<K, V> copy = newLinkedHash();
    if (map != null) {
      copy.putAll(map);
    }
    return copy;
  }

  static MapEx newLinkedHashEx(final Map<String, ? extends Object> map) {
    final MapEx copy = new LinkedHashMapEx();
    if (map != null) {
      copy.putAll(map);
    }
    return copy;
  }

  static <K, V> Map<K, V> newTree() {
    return new TreeMap<>();
  }

  static <K extends Comparable<K>, V extends Comparable<V>> TreeMap<K, V> newTree(
    final Comparator<K> comparator) {
    return new TreeMap<>(comparator);
  }

  static <K, V> Map<K, V> newTree(final Comparator<K> comparator, final Map<K, ? extends V> map) {
    final Map<K, V> newMap = newTree();
    if (map != null) {
      newMap.putAll(map);
    }
    return newMap;
  }

  static <K, V> Map<K, V> newTree(final K key, final V value) {
    final Map<K, V> map = newTree();
    map.put(key, value);
    return map;
  }

  static <K, V> Map<K, V> newTree(final Map<K, ? extends V> map) {
    final Map<K, V> newMap = newTree();
    if (map != null) {
      newMap.putAll(map);
    }
    return newMap;
  }

  static <K1, K2, V> V put(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2,
    final V value) {
    final Map<K2, V> values = getMap(map, key1);
    return values.put(key2, value);
  }

  static <K1, K2, V> V put(final Supplier<Map<K2, V>> factory, final Map<K1, Map<K2, V>> map,
    final K1 key1, final K2 key2, final V value) {
    final Map<K2, V> values = getMap(factory, map, key1);
    return values.put(key2, value);
  }

  static void putAll(final Map<String, Object> map,
    final Map<String, ? extends Object> properties) {
    if (map != null && properties != null) {
      for (final Entry<String, ? extends Object> entry : properties.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        map.put(key, value);
      }
    }
  }

  static void putAll(final Map<String, Object> map, final Properties properties) {
    if (map != null && properties != null) {
      for (final Entry<Object, Object> entry : properties.entrySet()) {
        final String key = (String)entry.getKey();
        final Object value = entry.getValue();
        map.put(key, value);
      }
    }
  }

  static <K, V extends Comparable<V>> void putIfGreaterThan(final Map<K, V> map, final K key,
    final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) > 1) {
        map.put(key, value);
      }
    }
  }

  static <K, V> boolean removeFromCollection(final Map<K, ? extends Collection<V>> map, final K key,
    final V value) {
    final Collection<V> values = map.get(key);
    if (values == null) {
      return false;
    } else {
      final boolean removed = values.remove(value);
      if (values.isEmpty()) {
        map.remove(key);
      }
      return removed;
    }
  }

  static <K1, K2, V> V removeFromMap(final Map<K1, Map<K2, V>> map, final K1 key1, final K2 key2) {
    final Map<K2, V> values = map.get(key1);
    if (values == null) {
      return null;
    } else {
      final V value = values.remove(key2);
      if (values.isEmpty()) {
        map.remove(key1);
      }
      return value;
    }
  }

  static <K, V> boolean removeFromSet(final Map<K, Set<V>> map, final K key, final V value) {
    final Set<V> values = map.get(key);
    if (values == null) {
      return false;
    } else {
      final boolean removed = values.remove(value);
      if (values.isEmpty()) {
        map.remove(key);
      }
      return removed;
    }
  }

  static <K, V extends Comparable<V>> void removeIfGreaterThanEqual(final Map<K, V> map,
    final K key, final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) >= 0) {
        map.remove(key);
      }
    }
  }

  static <K, V extends Comparable<V>> void removeIfLessThanEqual(final Map<K, V> map, final K key,
    final V value) {
    synchronized (map) {
      final V lastValue = map.get(key);
      if (lastValue == null || value.compareTo(lastValue) <= 0) {
        map.remove(key);
      }
    }
  }

  /**
   * Retain the value for the key in the map if the map's value is not equal to the value.
   * This can be used to create a set of keys values that need to be updated.
   *
   * @param map
   * @param key
   * @param value
   * @return True if the value was retained (not equal).
   */
  static <K, V> boolean retainIfNotEqual(final Map<K, V> map, final K key, final V value) {
    final Object currentValue = map.get(key);
    if (DataType.equal(currentValue, value)) {
      map.remove(key);
      return false;
    } else {
      return true;
    }
  }

  /**
   * Retain the value for the key in the map if the map's value is not equal to the value.
   * This can be used to create a set of keys values that need to be updated.
   *
   * @param map
   * @param key
   * @param value
   * @return True if the value was retained (not equal).
   */
  static <K, V> void retainIfNotEqual(final Map<K, V> map1, final Map<K, V> map2) {
    for (final Entry<K, V> entry : map2.entrySet()) {
      final K key = entry.getKey();
      final V value = entry.getValue();
      retainIfNotEqual(map1, key, value);
    }
  }

  static <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> sortByValues(
    final Map<K, V> map) {
    final MapValueComparator<K, V> comparator = new MapValueComparator<>(map);
    final Map<K, V> sortedMap = newTree(comparator);
    sortedMap.putAll(map);
    return newLinkedHash(sortedMap);
  }

  static Map<String, Object> toMap(final Preferences preferences) {
    try {
      final Map<String, Object> map = newHash();
      for (final String name : preferences.keys()) {
        final Object value = preferences.get(name, null);
        map.put(name, value);
      }
      return map;
    } catch (final BackingStoreException e) {
      throw new RuntimeException("Unable to get preferences " + e);
    }
  }

  static Map<String, String> toMap(final String string) {
    if (string == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, String> map = newLinkedHash();
      for (final String entry : string.split("\n")) {
        final String[] pair = entry.split("=");
        if (pair.length == 2) {
          final String name = pair[0];
          final String value = pair[1];
          map.put(name, value);
        } else {
          System.err.println("Invalid entry: " + entry);
        }
      }
      return map;
    }
  }
}
