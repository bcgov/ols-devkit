package com.revolsys.collection.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class AbstractDelegatingList<T> implements List<T> {
  @Override
  public void add(final int index, final T element) {
    final List<T> list = getList();
    list.add(index, element);
  }

  @Override
  public boolean add(final T e) {
    final List<T> list = getList();
    return list.add(e);
  }

  @Override
  public boolean addAll(final Collection<? extends T> c) {
    final List<T> list = getList();
    return list.addAll(c);
  }

  @Override
  public boolean addAll(final int index, final Collection<? extends T> c) {
    final List<T> list = getList();
    return list.addAll(c);
  }

  @Override
  public void clear() {
    final List<T> list = getList();
    list.clear();
  }

  @Override
  public boolean contains(final Object o) {
    final List<T> list = getList();
    return list.contains(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    final List<T> list = getList();
    return list.containsAll(c);
  }

  @Override
  public boolean equals(final Object obj) {
    final List<T> list = getList();
    return list.equals(obj);
  }

  @Override
  public T get(final int index) {
    final List<T> list = getList();
    return list.get(index);
  }

  protected abstract List<T> getList();

  @Override
  public int hashCode() {
    final List<T> list = getList();
    return list.hashCode();
  }

  @Override
  public int indexOf(final Object o) {
    final List<T> list = getList();
    return list.indexOf(o);
  }

  @Override
  public boolean isEmpty() {
    final List<T> list = getList();
    return list.isEmpty();
  }

  @Override
  public Iterator<T> iterator() {
    final List<T> list = getList();
    return list.iterator();
  }

  @Override
  public int lastIndexOf(final Object o) {
    final List<T> list = getList();
    return list.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    final List<T> list = getList();
    return list.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(final int index) {
    final List<T> list = getList();
    return list.listIterator(index);
  }

  @Override
  public T remove(final int index) {
    final List<T> list = getList();
    return list.remove(index);
  }

  @Override
  public boolean remove(final Object o) {
    final List<T> list = getList();
    return list.remove(o);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    final List<T> list = getList();
    return list.removeAll(c);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    final List<T> list = getList();
    return list.retainAll(c);
  }

  @Override
  public T set(final int index, final T element) {
    final List<T> list = getList();
    return list.set(index, element);
  }

  @Override
  public int size() {
    final List<T> list = getList();
    return list.size();
  }

  @Override
  public List<T> subList(final int fromIndex, final int toIndex) {
    final List<T> list = getList();
    return list.subList(fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    final List<T> list = getList();
    return list.toArray();
  }

  @Override
  public <V> V[] toArray(final V[] a) {
    final List<T> list = getList();
    return list.toArray(a);
  }

  @Override
  public String toString() {
    final List<T> list = getList();
    return list.toString();
  }

}
