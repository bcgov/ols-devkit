package com.revolsys.collection.map;

/*
 * $Id: ArrayListStack.java 4448 2006-02-14 20:54:57Z jonathanlocke $ $Revision:
 * 4448 $ $Date: 2006-02-14 21:54:57 +0100 (di, 14 feb 2006) $
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.revolsys.collection.ArrayUtil;

/**
 * This is a long hashmap that has the exact same features and interface as a
 * normal Map except that the key is directly an long. So no hash is
 * calculated or key object is stored.
 *
 * @author jcompagner
 */
public class LongHashMap<T> implements Map<Long, T>, Cloneable, Serializable {
  /**
   * @author jcompagner
   */
  public static class Entry<T> implements Map.Entry<Long, T> {
    final long key;

    Entry<T> next;

    T value;

    Entry(final long k, final T v, final Entry<T> n) {
      this.value = v;
      this.next = n;
      this.key = k;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }
      @SuppressWarnings("unchecked")
      final Entry<T> e = (Entry<T>)o;
      final long k1 = getLongKey();
      final long k2 = e.getLongKey();
      if (k1 == k2) {
        final Object v1 = getValue();
        final Object v2 = e.getValue();
        if (v1 == v2 || v1 != null && v1.equals(v2)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Long getKey() {
      return this.key;
    }

    /**
     * @return The long key of this entry
     */
    public long getLongKey() {
      return this.key;
    }

    /**
     * @return Gets the value object of this entry
     */
    @Override
    public T getValue() {
      return this.value;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return (int)this.key ^ (this.value == null ? 0 : this.value.hashCode());
    }

    /**
     * @param newValue
     * @return The previous value
     */
    @Override
    public T setValue(final T newValue) {
      final T oldValue = this.value;
      this.value = newValue;
      return oldValue;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return getLongKey() + "=" + getValue(); //$NON-NLS-1$
    }
  }

  private class EntryIterator extends HashIterator<Entry<T>> {
    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public Entry<T> next() {
      return nextEntry();
    }
  }

  private class EntrySet extends AbstractSet<Entry<T>> {
    /**
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
      LongHashMap.this.clear();
    }

    /**
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(final Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }
      @SuppressWarnings("unchecked")
      final Entry<T> e = (Entry<T>)o;
      final Entry<T> candidate = getEntry(e.getLongKey());
      return candidate != null && candidate.equals(e);
    }

    /**
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<Entry<T>> iterator() {
      return newEntryIterator();
    }

    /**
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(final Object o) {
      return removeMapping(o) != null;
    }

    /**
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
      return LongHashMap.this.size;
    }
  }

  private abstract class HashIterator<V> implements Iterator<V> {
    Entry<T> current; // current entry

    int expectedModCount; // For fast-fail

    int index; // current slot

    Entry<T> next; // next entry to return

    HashIterator() {
      this.expectedModCount = LongHashMap.this.modCount;
      final Entry<T>[] t = LongHashMap.this.table;
      int i = t.length;
      Entry<T> n = null;
      if (LongHashMap.this.size != 0) { // advance to first entry
        while (i > 0 && (n = t[--i]) == null) {
          /* NoOp */;
        }
      }
      this.next = n;
      this.index = i;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
      return this.next != null;
    }

    Entry<T> nextEntry() {
      if (LongHashMap.this.modCount != this.expectedModCount) {
        throw new ConcurrentModificationException();
      }
      final Entry<T> e = this.next;
      if (e == null) {
        throw new NoSuchElementException();
      }

      Entry<T> n = e.next;
      final Entry<T>[] t = LongHashMap.this.table;
      int i = this.index;
      while (n == null && i > 0) {
        n = t[--i];
      }
      this.index = i;
      this.next = n;
      return this.current = e;
    }

    /**
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
      if (this.current == null) {
        throw new IllegalStateException();
      }
      if (LongHashMap.this.modCount != this.expectedModCount) {
        throw new ConcurrentModificationException();
      }
      final long k = this.current.key;
      this.current = null;
      LongHashMap.this.removeEntryForKey(k);
      this.expectedModCount = LongHashMap.this.modCount;
    }

  }

  private class KeyIterator extends HashIterator<Long> {
    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public Long next() {
      return new Long(nextEntry().getLongKey());
    }
  }

  private class KeySet extends AbstractSet<Long> {
    /**
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
      LongHashMap.this.clear();
    }

    /**
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(final Object o) {
      if (o instanceof Number) {
        return containsKey(((Number)o).longValue());
      }
      return false;
    }

    /**
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<Long> iterator() {
      return newKeyIterator();
    }

    /**
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(final Object o) {
      if (o instanceof Number) {
        return LongHashMap.this.removeEntryForKey(((Number)o).intValue()) != null;
      }
      return false;
    }

    /**
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
      return LongHashMap.this.size;
    }
  }

  private class ValueIterator extends HashIterator<T> {
    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public T next() {
      return nextEntry().value;
    }
  }

  private class Values extends AbstractCollection<T> {
    /**
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
      LongHashMap.this.clear();
    }

    /**
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(final Object o) {
      return containsValue(o);
    }

    /**
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<T> iterator() {
      return newValueIterator();
    }

    /**
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
      return LongHashMap.this.size;
    }
  }

  /**
   * The default initial capacity - MUST be a power of two.
   */
  static final int DEFAULT_INITIAL_CAPACITY = 16;

  /**
   * The load factor used when none specified in constructor.
   */
  static final float DEFAULT_LOAD_FACTOR = 0.75f;

  /**
   * The maximum capacity, used if a higher value is implicitly specified by
   * either of the constructors with arguments. MUST be a power of two <= 1<<30.
   */
  static final int MAXIMUM_CAPACITY = 1 << 30;

  private static final long serialVersionUID = 362498820763181265L;

  /**
   * Returns index for hash code h.
   *
   * @param h
   * @param length
   * @return The index for the hash long for the given length
   */
  static int indexFor(final int h, final int length) {
    return h & length - 1;
  }

  // internal utilities

  private transient Set<Entry<T>> entrySet = null;

  transient volatile Set<Long> keySet = null;

  /**
   * The load factor for the hash table.
   *
   * @serial
   */
  final float loadFactor;

  /**
   * The number of times this HashMap has been structurally modified Structural
   * modifications are those that change the number of mappings in the HashMap
   * or otherwise modify its internal structure (e.g., rehash). This field is
   * used to make iterators on Collection-views of the HashMap fail-fast. (See
   * ConcurrentModificationException).
   */
  transient volatile int modCount;

  /**
   * The number of key-value mappings contained in this identity hash map.
   */
  transient int size;

  /**
   * The table, resized as necessary. Length MUST Always be a power of two.
   */
  transient Entry<T>[] table;

  /**
   * The next size value at which to resize (capacity * load factor).
   *
   * @serial
   */
  int threshold;

  transient volatile Collection<T> values = null;

  /**
   * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16)
   * and the default load factor (0.75).
   */
  @SuppressWarnings("unchecked")
  public LongHashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
    this.threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
    this.table = ArrayUtil.newArray(Entry.class, DEFAULT_INITIAL_CAPACITY);
    init();
  }

  /**
   * Constructs an empty <tt>HashMap</tt> with the specified initial capacity
   * and the default load factor (0.75).
   *
   * @param initialCapacity the initial capacity.
   * @throws IllegalArgumentException if the initial capacity is negative.
   */
  public LongHashMap(final int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Constructs an empty <tt>HashMap</tt> with the specified initial capacity
   * and load factor.
   *
   * @param initialCapacity The initial capacity.
   * @param loadFactor The load factor.
   * @throws IllegalArgumentException if the initial capacity is negative or the
   *           load factor is nonpositive.
   */
  @SuppressWarnings("unchecked")
  public LongHashMap(int initialCapacity, final float loadFactor) {
    if (initialCapacity < 0) {
      throw new IllegalArgumentException("Illegal initial capacity: " + //$NON-NLS-1$
        initialCapacity);
    }
    if (initialCapacity > MAXIMUM_CAPACITY) {
      initialCapacity = MAXIMUM_CAPACITY;
    }
    if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
      throw new IllegalArgumentException("Illegal load factor: " + //$NON-NLS-1$
        loadFactor);
    }

    // Find a power of 2 >= initialCapacity
    int capacity = 1;
    while (capacity < initialCapacity) {
      capacity <<= 1;
    }

    this.loadFactor = loadFactor;
    this.threshold = (int)(capacity * loadFactor);
    this.table = ArrayUtil.newArray(Entry.class, this.size);
    init();
  }

  /**
   * Add a new entry with the specified key, value and hash code to the
   * specified bucket. It is the responsibility of this method to resize the
   * table if appropriate. Subclass overrides this to alter the behavior of put
   * method.
   *
   * @param key
   * @param value
   * @param bucketIndex
   */
  void addEntry(final int key, final T value, final int bucketIndex) {
    this.table[bucketIndex] = new Entry<>(key, value, this.table[bucketIndex]);
    if (this.size++ >= this.threshold) {
      resize(2 * this.table.length);
    }
  }

  // These methods are used when serializing HashSets
  int capacity() {
    return this.table.length;
  }

  /**
   * Removes all mappings from this map.
   */
  @Override
  public void clear() {
    this.modCount++;
    final Entry<T> tab[] = this.table;
    for (int i = 0; i < tab.length; i++) {
      tab[i] = null;
    }
    this.size = 0;
  }

  /**
   * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and
   * values themselves are not cloned.
   *
   * @return a shallow copy of this map.
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object clone() throws CloneNotSupportedException {
    LongHashMap<T> result = null;
    try {
      result = (LongHashMap<T>)super.clone();
      result.table = ArrayUtil.newArray(Entry.class, this.table.length);
      result.entrySet = null;
      result.modCount = 0;
      result.size = 0;
      result.init();
      result.putAllForCreate(this);
    } catch (final CloneNotSupportedException e) {
      // assert false;
    }
    return result;
  }

  /**
   * Returns <tt>true</tt> if this map contains a mapping for the specified key.
   *
   * @param key The key whose presence in this map is to be tested
   * @return <tt>true</tt> if this map contains a mapping for the specified key.
   */
  public boolean containsKey(final int key) {
    final int i = indexFor(key, this.table.length);
    Entry<T> e = this.table[i];
    while (e != null) {
      if (key == e.key) {
        return true;
      }
      e = e.next;
    }
    return false;
  }

  @Override
  public boolean containsKey(final Object obj) {
    if (obj instanceof Long) {
      final Long number = (Long)obj;
      return get(number) != null;
    } else {
      return false;
    }
  }

  /**
   * Special-case code for containsValue with null argument
   *
   * @return boolean true if there is a null value in this map
   */
  private boolean containsNullValue() {
    final Entry<T> tab[] = this.table;
    for (final Entry<T> element : tab) {
      for (Entry<T> e = element; e != null; e = e.next) {
        if (e.value == null) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns <tt>true</tt> if this map maps one or more keys to the specified
   * value.
   *
   * @param value value whose presence in this map is to be tested.
   * @return <tt>true</tt> if this map maps one or more keys to the specified
   *         value.
   */
  @Override
  public boolean containsValue(final Object value) {
    if (value == null) {
      return containsNullValue();
    }

    final Entry<T> tab[] = this.table;
    for (final Entry<T> element : tab) {
      for (Entry<T> e = element; e != null; e = e.next) {
        if (value.equals(e.value)) {
          return true;
        }
      }
    }
    return false;
  }

  public Set<Entry<T>> entryIntSet() {
    if (this.entrySet == null) {
      this.entrySet = new EntrySet();
    }
    return this.entrySet;
  }

  /**
   * Returns a collection view of the mappings contained in this map. Each
   * element in the returned collection is a <tt>Map.Entry</tt>. The collection
   * is backed by the map, so changes to the map are reflected in the
   * collection, and vice-versa. The collection supports element removal, which
   * removes the corresponding mapping from the map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>,
   * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support the
   * <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the mappings contained in this map.
   * @see Map.Entry
   */
  @Override
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public Set<Map.Entry<Long, T>> entrySet() {
    if (this.entrySet == null) {
      this.entrySet = new EntrySet();
    }
    return (Set)this.entrySet;
  }

  /**
   * Returns the value to which the specified key is mapped in this identity
   * hash map, or <tt>null</tt> if the map contains no mapping for this key. A
   * return value of <tt>null</tt> does not <i>necessarily</i> indicate that the
   * map contains no mapping for the key; it is also possible that the map
   * explicitly maps the key to <tt>null</tt>. The <tt>containsKey</tt> method
   * may be used to distinguish these two cases.
   *
   * @param key the key whose associated value is to be returned.
   * @return the value to which this map maps the specified key, or
   *         <tt>null</tt> if the map contains no mapping for this key.
   * @see #put(int, Object)
   */
  public T get(final long key) {
    final int i = indexFor((int)key, this.table.length);
    Entry<T> e = this.table[i];
    while (true) {
      if (e == null) {
        return null;
      }
      if (key == e.key) {
        return e.value;
      }
      e = e.next;
    }
  }

  @Override
  public T get(final Object obj) {
    if (obj instanceof Number) {
      final long number = ((Long)obj).longValue();
      return get(number);
    } else {
      return null;
    }
  }

  /**
   * Returns the entry associated with the specified key in the HashMap. Returns
   * null if the HashMap contains no mapping for this key.
   *
   * @param key
   * @return The MapKeyEntry<T> object for the given hash key
   */
  Entry<T> getEntry(final long key) {
    final int i = indexFor((int)key, this.table.length);
    Entry<T> e = this.table[i];
    while (e != null && !(key == e.key)) {
      e = e.next;
    }
    return e;
  }

  /**
   * Initialization hook for subclasses. This method is called in all
   * constructors and pseudo-constructors (clone, readObject) after HashMap has
   * been initialized but before any entries have been inserted. (In the absence
   * of this method, readObject would require explicit knowledge of subclasses.)
   */
  void init() {
  }

  /**
   * Returns <tt>true</tt> if this map contains no key-value mappings.
   *
   * @return <tt>true</tt> if this map contains no key-value mappings.
   */
  @Override
  public boolean isEmpty() {
    return this.size == 0;
  }

  /**
   * Returns a set view of the keys contained in this map. The set is backed by
   * the map, so changes to the map are reflected in the set, and vice-versa.
   * The set supports element removal, which removes the corresponding mapping
   * from this map, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It
   * does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a set view of the keys contained in this map.
   */
  @Override
  public Set<Long> keySet() {
    final Set<Long> ks = this.keySet;
    return ks != null ? ks : (this.keySet = new KeySet());
  }

  float loadFactor() {
    return this.loadFactor;
  }

  /**
   * Like addEntry<T> except that this version is used when creating entries as
   * part of Map construction or "pseudo-construction" (cloning,
   * deserialization). This version needn't worry about resizing the table.
   * Subclass overrides this to alter the behavior of HashMap(Map), clone, and
   * readObject.
   *
   * @param key
   * @param value
   * @param bucketIndex
   */
  void newEntry(final long key, final T value, final int bucketIndex) {
    this.table[bucketIndex] = new Entry<>(key, value, this.table[bucketIndex]);
    this.size++;
  }

  Iterator<Entry<T>> newEntryIterator() {
    return new EntryIterator();
  }

  // Subclass overrides these to alter behavior of views' iterator() method
  Iterator<Long> newKeyIterator() {
    return new KeyIterator();
  }

  // Views

  Iterator<T> newValueIterator() {
    return new ValueIterator();
  }

  @Override
  public T put(final Long key, final T value) {
    return putInt(key.intValue(), value);
  }

  /**
   * Copies all of the mappings from the specified map to this map These
   * mappings will replace any mappings that this map had for any of the keys
   * currently in the specified map.
   *
   * @param m mappings to be stored in this map.
   * @throws NullPointerException if the specified map is null.
   */
  public void putAll(final LongHashMap<T> m) {
    final int numKeysToBeAdded = m.size();
    if (numKeysToBeAdded == 0) {
      return;
    }

    /*
     * Expand the map if the map if the number of mappings to be added is
     * greater than or equal to threshold. This is conservative; the obvious
     * condition is (m.size() + size) >= threshold, but this condition could
     * result in a map with twice the appropriate capacity, if the keys to be
     * added overlap with the keys already in this map. By using the
     * conservative calculation, we subject ourself to at most one extra resize.
     */
    if (numKeysToBeAdded > this.threshold) {
      int targetCapacity = (int)(numKeysToBeAdded / this.loadFactor + 1);
      if (targetCapacity > MAXIMUM_CAPACITY) {
        targetCapacity = MAXIMUM_CAPACITY;
      }
      int newCapacity = this.table.length;
      while (newCapacity < targetCapacity) {
        newCapacity <<= 1;
      }
      if (newCapacity > this.table.length) {
        resize(newCapacity);
      }
    }

    for (final Entry<T> e : m.entryIntSet()) {
      put(e.getLongKey(), e.getValue());
    }
  }

  @Override
  public void putAll(final Map<? extends Long, ? extends T> map) {
    final int numKeysToBeAdded = map.size();
    if (numKeysToBeAdded > 0) {
      if (numKeysToBeAdded > this.threshold) {
        int targetCapacity = (int)(numKeysToBeAdded / this.loadFactor + 1);
        if (targetCapacity > MAXIMUM_CAPACITY) {
          targetCapacity = MAXIMUM_CAPACITY;
        }
        int newCapacity = this.table.length;
        while (newCapacity < targetCapacity) {
          newCapacity <<= 1;
        }
        if (newCapacity > this.table.length) {
          resize(newCapacity);
        }
      }

      for (final Map.Entry<? extends Long, ? extends T> e : map.entrySet()) {
        final Long key = e.getKey();
        final T value = e.getValue();
        put(key, value);
      }
    }
  }

  void putAllForCreate(final LongHashMap<T> m) {
    for (final Entry<T> e : m.entryIntSet()) {
      putForCreate(e.getLongKey(), e.getValue());
    }
  }

  /**
   * This method is used instead of put by constructors and pseudoconstructors
   * (clone, readObject). It does not resize the table, check for
   * comodification, etc. It calls  newEntry<T> rather than addEntry.
   *
   * @param key
   * @param value
   */
  private void putForCreate(final long key, final T value) {
    final int i = indexFor((int)key, this.table.length);

    /**
     * Look for preexisting entry for key. This will never happen for clone or
     * deserialize. It will only happen for construction if the input Map is a
     * sorted map whose ordering is inconsistent w/ equals.
     */
    for (Entry<T> e = this.table[i]; e != null; e = e.next) {
      if (key == e.key) {
        e.value = value;
        return;
      }
    }

    newEntry(key, value, i);
  }

  /**
   * Associates the specified value with the specified key in this map. If the
   * map previously contained a mapping for this key, the old value is replaced.
   *
   * @param key key with which the specified value is to be associated.
   * @param value value to be associated with the specified key.
   * @return previous value associated with specified key, or <tt>null</tt> if
   *         there was no mapping for key. A <tt>null</tt> return can also
   *         indicate that the HashMap previously associated <tt>null</tt> with
   *         the specified key.
   */
  public T putInt(final int key, final T value) {
    final int i = indexFor(key, this.table.length);

    for (Entry<T> e = this.table[i]; e != null; e = e.next) {
      if (key == e.key) {
        final T oldValue = e.value;
        e.value = value;
        return oldValue;
      }
    }

    this.modCount++;
    addEntry(key, value, i);
    return null;
  }

  /**
   * Reconstitute the <tt>HashMap</tt> instance from a stream (i.e., deserialize
   * it).
   *
   * @param s
   * @throws IOException
   * @throws ClassNotFoundException
   */
  @SuppressWarnings("unchecked")
  private void readObject(final java.io.ObjectInputStream s)
    throws IOException, ClassNotFoundException {
    // Read in the threshold, loadfactor, and any hidden stuff
    s.defaultReadObject();

    // Read in number of buckets and allocate the bucket array;
    final int numBuckets = s.readInt();
    this.table = ArrayUtil.newArray(Entry.class, numBuckets);

    init(); // Give subclass a chance to do its thing.

    // Read in size (number of Mappings)
    final int size = s.readInt();

    // Read the keys and values, and put the mappings in the HashMap
    for (int i = 0; i < size; i++) {
      final int key = s.readInt();
      final T value = (T)s.readObject();
      putForCreate(key, value);
    }
  }

  /**
   * Removes the mapping for this key from this map if present.
   *
   * @param key key whose mapping is to be removed from the map.
   * @return previous value associated with specified key, or <tt>null</tt> if
   *         there was no mapping for key. A <tt>null</tt> return can also
   *         indicate that the map previously associated <tt>null</tt> with the
   *         specified key.
   */
  public T remove(final long key) {
    final Entry<T> e = removeEntryForKey(key);
    if (e == null) {
      return null;
    } else {
      return e.value;
    }
  }

  @Override
  public T remove(final Object obj) {
    if (obj instanceof Long) {
      final Long number = (Long)obj;
      return remove(number.intValue());
    } else {
      return null;
    }
  }

  /**
   * Removes and returns the entry associated with the specified key in the
   * HashMap. Returns null if the HashMap contains no mapping for this key.
   *
   * @param key
   * @return The MapKeyEntry<T> object that was removed
   */
  Entry<T> removeEntryForKey(final long key) {
    final int i = indexFor((int)key, this.table.length);
    Entry<T> prev = this.table[i];
    Entry<T> e = prev;

    while (e != null) {
      final Entry<T> next = e.next;
      if (key == e.key) {
        this.modCount++;
        this.size--;
        if (prev == e) {
          this.table[i] = next;
        } else {
          prev.next = next;
        }
        return e;
      }
      prev = e;
      e = next;
    }

    return e;
  }

  /**
   * Special version of remove for MapKeySetEntrySet.
   *
   * @param o
   * @return The entry that was removed
   */
  Entry<T> removeMapping(final Object o) {
    if (!(o instanceof Entry)) {
      return null;
    }

    @SuppressWarnings("unchecked")
    final Entry<T> entry = (Entry<T>)o;
    final long key = entry.getLongKey();
    final int i = indexFor((int)key, this.table.length);
    Entry<T> prev = this.table[i];
    Entry<T> e = prev;

    while (e != null) {
      final Entry<T> next = e.next;
      if (e.key == key && e.equals(entry)) {
        this.modCount++;
        this.size--;
        if (prev == e) {
          this.table[i] = next;
        } else {
          prev.next = next;
        }
        return e;
      }
      prev = e;
      e = next;
    }

    return e;
  }

  /**
   * Rehashes the contents of this map into a new array with a larger capacity.
   * This method is called automatically when the number of keys in this map
   * reaches its threshold. If current capacity is MAXIMUM_CAPACITY, this method
   * does not resize the map, but but sets threshold to Long.MAX_VALUE. This
   * has the effect of preventing future calls.
   *
   * @param newCapacity the new capacity, MUST be a power of two; must be
   *          greater than current capacity unless current capacity is
   *          MAXIMUM_CAPACITY (in which case value is irrelevant).
   */
  void resize(final int newCapacity) {
    final Entry<T>[] oldTable = this.table;
    final int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
      this.threshold = Integer.MAX_VALUE;
      return;
    }

    @SuppressWarnings("unchecked")
    final Entry<T>[] newTable = ArrayUtil.newArray(Entry.class, newCapacity);
    transfer(newTable);
    this.table = newTable;
    this.threshold = (int)(newCapacity * this.loadFactor);
  }

  /**
   * Returns the number of key-value mappings in this map.
   *
   * @return the number of key-value mappings in this map.
   */
  @Override
  public int size() {
    return this.size;
  }

  /**
   * Transfer all entries from current table to newTable.
   *
   * @param newTable
   */
  void transfer(final Entry<T>[] newTable) {
    final Entry<T>[] src = this.table;
    final int newCapacity = newTable.length;
    for (int j = 0; j < src.length; j++) {
      Entry<T> e = src[j];
      if (e != null) {
        src[j] = null;
        do {
          final Entry<T> next = e.next;
          final int i = indexFor((int)e.key, newCapacity);
          e.next = newTable[i];
          newTable[i] = e;
          e = next;
        } while (e != null);
      }
    }
  }

  /**
   * Returns a collection view of the values contained in this map. The
   * collection is backed by the map, so changes to the map are reflected in the
   * collection, and vice-versa. The collection supports element removal, which
   * removes the corresponding mapping from this map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>,
   * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support the
   * <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the values contained in this map.
   */
  @Override
  public Collection<T> values() {
    final Collection<T> vs = this.values;
    return vs != null ? vs : (this.values = new Values());
  }

  /**
   * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
   * serialize it).
   *
   * @param s The ObjectOutputStream
   * @throws IOException
   * @serialData The <i>capacity</i> of the HashMap (the length of the bucket
   *             array) is emitted (int), followed by the <i>size</i> of the
   *             HashMap (the number of key-value mappings), followed by the key
   *             (Object) and value (Object) for each key-value mapping
   *             represented by the HashMap The key-value mappings are emitted
   *             in the order that they are returned by
   *             <tt>entrySet().iterator()</tt>.
   */
  private void writeObject(final java.io.ObjectOutputStream s) throws IOException {
    // Write out the threshold, loadfactor, and any hidden stuff
    s.defaultWriteObject();

    // Write out number of buckets
    s.writeInt(this.table.length);

    // Write out size (number of Mappings)
    s.writeInt(this.size);

    // Write out keys and values (alternating)
    for (final Entry<T> e : entryIntSet()) {
      s.writeLong(e.getLongKey());
      s.writeObject(e.getValue());
    }
  }
}
