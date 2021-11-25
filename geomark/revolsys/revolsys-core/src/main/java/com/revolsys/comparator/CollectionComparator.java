package com.revolsys.comparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Compare the values of two collections, each element is compared in turn, the
 * first compare that is not 0 will cause the method to return. If all values
 * are the same then the shortest collection will be less than a longer
 * collection.
 *
 * @param <T>
 */
public class CollectionComparator<T extends Comparable<T>>
  implements Comparator<Collection<? extends T>> {

  @Override
  public int compare(final Collection<? extends T> collection1,
    final Collection<? extends T> collection2) {
    if (collection1 == collection2) {
      return 0;
    } else {
      final Iterator<? extends T> iterator1 = collection1.iterator();
      final Iterator<? extends T> iterator2 = collection2.iterator();
      while (iterator1.hasNext() && iterator2.hasNext()) {
        final Comparable<T> value1 = iterator1.next();
        final T value2 = iterator2.next();
        final int compare = value1.compareTo(value2);
        if (compare != 0) {
          return compare;
        }
      }
      if (iterator1.hasNext()) {
        return 1;
      } else if (iterator2.hasNext()) {
        return -1;
      } else {
        return 0;
      }
    }
  }
}
