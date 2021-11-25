package com.revolsys.collection.list;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class DelegatingList<V> implements List<V> {

  protected final List<V> list;

  public DelegatingList(final List<V> list) {
    this.list = list;
  }

  @Override
  public void add(final int index, final V element) {
    this.list.add(index, element);
  }

  @Override
  public boolean add(final V e) {
    return this.list.add(e);
  }

  @Override
  public boolean addAll(final Collection<? extends V> c) {
    return this.list.addAll(c);
  }

  @Override
  public boolean addAll(final int index, final Collection<? extends V> c) {
    return this.list.addAll(index, c);
  }

  @Override
  public void clear() {
    this.list.clear();
  }

  @Override
  public boolean contains(final Object o) {
    return this.list.contains(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return this.list.containsAll(c);
  }

  @Override
  public boolean equals(final Object obj) {
    return this.list.equals(obj);
  }

  @Override
  public void forEach(final Consumer<? super V> action) {
    this.list.forEach(action);
  }

  @Override
  public V get(final int index) {
    return this.list.get(index);
  }

  public List<V> getList() {
    return this.list;
  }

  @Override
  public int hashCode() {
    return this.list.hashCode();
  }

  @Override
  public int indexOf(final Object o) {
    return this.list.indexOf(o);
  }

  @Override
  public boolean isEmpty() {
    return this.list.isEmpty();
  }

  @Override
  public Iterator<V> iterator() {
    return this.list.iterator();
  }

  @Override
  public int lastIndexOf(final Object o) {
    return this.list.lastIndexOf(o);
  }

  @Override
  public ListIterator<V> listIterator() {
    return this.list.listIterator();
  }

  @Override
  public ListIterator<V> listIterator(final int index) {
    return this.list.listIterator(index);
  }

  @Override
  public Stream<V> parallelStream() {
    return this.list.parallelStream();
  }

  @Override
  public V remove(final int index) {
    return this.list.remove(index);
  }

  @Override
  public boolean remove(final Object o) {
    return this.list.remove(o);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return this.list.removeAll(c);
  }

  @Override
  public boolean removeIf(final Predicate<? super V> filter) {
    return this.list.removeIf(filter);
  }

  @Override
  public void replaceAll(final UnaryOperator<V> operator) {
    this.list.replaceAll(operator);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return this.list.retainAll(c);
  }

  @Override
  public V set(final int index, final V element) {
    return this.list.set(index, element);
  }

  @Override
  public int size() {
    return this.list.size();
  }

  @Override
  public void sort(final Comparator<? super V> c) {
    this.list.sort(c);
  }

  @Override
  public Spliterator<V> spliterator() {
    return this.list.spliterator();
  }

  @Override
  public Stream<V> stream() {
    return this.list.stream();
  }

  @Override
  public List<V> subList(final int fromIndex, final int toIndex) {
    return this.list.subList(fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    return this.list.toArray();
  }

  @Override
  public <T> T[] toArray(final IntFunction<T[]> generator) {
    return this.list.toArray(generator);
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return this.list.toArray(a);
  }

  @Override
  public String toString() {
    return this.list.toString();
  }

}
