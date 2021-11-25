package com.revolsys.collection.bplus;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.collection.map.MapKeySetEntrySet;
import com.revolsys.comparator.Comparators;
import com.revolsys.io.FileUtil;
import com.revolsys.io.page.FileMappedPageManager;
import com.revolsys.io.page.FilePageManager;
import com.revolsys.io.page.MemoryPageManager;
import com.revolsys.io.page.MethodPageValueManager;
import com.revolsys.io.page.Page;
import com.revolsys.io.page.PageManager;
import com.revolsys.io.page.PageValueManager;
import com.revolsys.io.page.SerializablePageValueManager;

public class BPlusTreeMap<K, V> extends AbstractMap<K, V> {

  private class PutResult {
    private boolean hasOldValue;

    private byte[] newKeyBytes;

    private byte[] newPageIndexBytes;

    private V oldValue;

    public void clear() {
      this.newKeyBytes = null;
      this.newPageIndexBytes = null;
    }

    public boolean wasSplit() {
      return this.newKeyBytes != null;
    }
  }

  private class RemoveResult {
    private boolean hasOldValue;

    private V oldValue;
  }

  public static final byte DATA = 2;

  public static final byte EXTENDED = -128;

  public static final byte INTERIOR = 0;

  public static final byte LEAF = 1;

  public static <K, V> Map<K, V> newInMemory(final Comparator<K> comparator,
    final PageValueManager<K> keyManager, final PageValueManager<V> valueManager) {
    final MemoryPageManager pages = new MemoryPageManager();
    return new BPlusTreeMap<>(pages, comparator, keyManager, valueManager);
  }

  public static <K extends Comparable<K>, V> Map<K, V> newInMemory(
    final PageValueManager<K> keyManager, final PageValueManager<V> valueManager) {
    final MemoryPageManager pages = new MemoryPageManager();
    final Comparator<K> comparator = Comparators.newComparator();
    return new BPlusTreeMap<>(pages, comparator, keyManager, valueManager);
  }

  public static <V> Map<Integer, V> newIntSeralizableTempDisk() {
    final File file = FileUtil.newTempFile("int", ".btree");
    final PageManager pageManager = new FilePageManager(file);
    final PageValueManager<Integer> keyManager = PageValueManager.INT;
    final SerializablePageValueManager<V> valueSerializer = new SerializablePageValueManager<>();
    final PageValueManager<V> valueManager = BPlusTreePageValueManager
      .newPageValueManager(pageManager, valueSerializer);
    final Comparator<Integer> comparator = Comparators.newComparator();
    return new BPlusTreeMap<>(pageManager, comparator, keyManager, valueManager);
  }

  public static <V> Map<Integer, V> newIntSeralizableTempDisk(final Map<Integer, V> values) {
    final File file = FileUtil.newTempFile("int", ".btree");
    final PageManager pageManager = new FilePageManager(file);
    final PageValueManager<Integer> keyManager = PageValueManager.INT;
    final SerializablePageValueManager<V> valueSerializer = new SerializablePageValueManager<>();
    final PageValueManager<V> valueManager = BPlusTreePageValueManager
      .newPageValueManager(pageManager, valueSerializer);
    final Comparator<Integer> comparator = Comparators.newComparator();
    final BPlusTreeMap<Integer, V> map = new BPlusTreeMap<>(pageManager, comparator, keyManager,
      valueManager);
    map.putAll(values);
    return map;
  }

  public static <K, V> Map<K, V> newMap(final PageManager pages, final Comparator<K> comparator,
    final PageValueManager<K> keyManager, final PageValueManager<V> valueManager) {
    return new BPlusTreeMap<>(pages, comparator, keyManager, valueManager);
  }

  public static <K extends Comparable<K>, V> Map<K, V> newMap(final PageManager pages,
    final PageValueManager<K> keyManager, final PageValueManager<V> valueManager) {
    final Comparator<K> comparator = Comparators.newComparator();
    return new BPlusTreeMap<>(pages, comparator, keyManager, valueManager);
  }

  public static <K extends Comparable<?>, V> Map<K, V> newTempDisk(final Map<K, V> values,
    PageValueManager<K> keyManager, PageValueManager<V> valueManager) {
    final File file = FileUtil.newTempFile("temp", ".bplustree");
    final PageManager pageManager = new FilePageManager(file);

    if (keyManager instanceof SerializablePageValueManager) {
      final SerializablePageValueManager<K> serializeableManager = (SerializablePageValueManager<K>)keyManager;
      keyManager = BPlusTreePageValueManager.newPageValueManager(pageManager, serializeableManager);
    }

    if (valueManager instanceof SerializablePageValueManager) {
      final SerializablePageValueManager<V> serializeableManager = (SerializablePageValueManager<V>)valueManager;
      valueManager = BPlusTreePageValueManager.newPageValueManager(pageManager,
        serializeableManager);
    }

    final Comparator<K> comparator = (o1, o2) -> ((Comparable<Object>)o1).compareTo(o2);
    final BPlusTreeMap<K, V> map = new BPlusTreeMap<>(pageManager, comparator, keyManager,
      valueManager);
    map.putAll(values);
    return map;
  }

  public static <K extends Comparable<K>, V> Map<K, V> newTempDisk(PageValueManager<K> keyManager,
    PageValueManager<V> valueManager) {
    final File file = FileUtil.newTempFile("temp", ".bplustree");
    final PageManager pageManager = new FileMappedPageManager(file);

    if (keyManager instanceof SerializablePageValueManager) {
      final SerializablePageValueManager<K> serializeableManager = (SerializablePageValueManager<K>)keyManager;
      keyManager = BPlusTreePageValueManager.newPageValueManager(pageManager, serializeableManager);
    }

    if (valueManager instanceof SerializablePageValueManager) {
      final SerializablePageValueManager<V> serializeableManager = (SerializablePageValueManager<V>)valueManager;
      valueManager = BPlusTreePageValueManager.newPageValueManager(pageManager,
        serializeableManager);
    }

    final Comparator<K> comparator = Comparators.newComparator();
    final BPlusTreeMap<K, V> map = new BPlusTreeMap<>(pageManager, comparator, keyManager,
      valueManager);
    return map;
  }

  protected static void setNumBytes(final Page page) {
    final int offset = page.getOffset();
    page.setOffset(1);
    page.writeShort((short)offset);
    page.setOffset(offset);
  }

  protected static void skipHeader(final Page page) {
    page.setOffset(3);
  }

  protected static void writeLeafHeader(final Page page, final byte pageType,
    final int nextPageIndex) {
    page.writeByte(pageType);
    page.writeShort((short)7);
    page.writeInt(nextPageIndex);
  }

  protected static void writePageHeader(final Page page, final byte pageType) {
    page.writeByte(pageType);
    page.writeShort((short)3);
  }

  private final Comparator<K> comparator;

  private final double fillFactor = 0.5;

  private final int headerSize = 3;

  private final PageValueManager<K> keyManager;

  private final int leafHeaderSize = 7;

  private final int minSize;

  private volatile transient int modCount;

  private final PageManager pages;

  private final int rootPageIndex = 0;

  private int size = 0;

  private final PageValueManager<V> valueManager;

  public BPlusTreeMap(final PageManager pages, final Comparator<K> comparator,
    final PageValueManager<K> keyManager, final PageValueManager<V> valueManager) {
    this.pages = pages;
    this.comparator = comparator;
    this.keyManager = keyManager;
    this.valueManager = valueManager;
    this.minSize = (int)(this.fillFactor * pages.getPageSize());
    if (pages.getNumPages() == 0) {
      final Page rootPage = pages.newPage();
      writeLeafHeader(rootPage, LEAF, -1);
      pages.releasePage(rootPage);
    }
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new MapKeySetEntrySet<>(this);
  }

  protected V get(final int pageIndex, final K key) {
    V result;
    final Page page = this.pages.getPage(pageIndex);
    final byte pageType = page.readByte();
    if (pageType == INTERIOR) {
      result = getInterior(page, key);
    } else if (pageType == LEAF) {
      result = getLeaf(page, key);
    } else {
      throw new IllegalArgumentException("Unknown page type " + pageType);
    }
    this.pages.releasePage(page);
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public V get(final Object key) {
    return get(this.rootPageIndex, (K)key);
  }

  private V getInterior(final Page page, final K key) {
    final int numBytes = page.readShort();
    final int pageIndex = page.readInt();
    int previousPageIndex = pageIndex;
    while (page.getOffset() < numBytes) {
      final K currentKey = this.keyManager.readFromPage(page);
      final int nextPageIndex = page.readInt();
      final int compare = this.comparator.compare(currentKey, key);
      if (compare > 0) {
        return get(previousPageIndex, key);
      }
      previousPageIndex = nextPageIndex;
    }
    return get(previousPageIndex, key);
  }

  private V getLeaf(final Page page, final K key) {
    final int numBytes = page.readShort();
    page.setOffset(this.leafHeaderSize);
    while (page.getOffset() < numBytes) {
      final K currentKey = this.keyManager.readFromPage(page);
      final V currentValue = this.valueManager.readFromPage(page);
      final int compare = this.comparator.compare(currentKey, key);
      if (compare == 0) {
        return currentValue;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  <T> int getLeafValues(final List<T> values, int pageIndex, final boolean key) {
    values.clear();
    final Page page = this.pages.getPage(pageIndex);

    final byte pageType = page.readByte();
    while (pageType == INTERIOR) {
      page.readShort(); // skip num bytes
      pageIndex = page.readInt();
      this.pages.releasePage(page);
    }

    if (pageType != LEAF) {
      throw new IllegalArgumentException("Unknown page type " + pageType);
    }

    // TODO traverse to leaf
    try {
      final int numBytes = page.readShort();
      final int nextPageId = page.readInt();
      while (page.getOffset() < numBytes) {
        final K currentKey = this.keyManager.readFromPage(page);
        final V currentValue = this.valueManager.readFromPage(page);
        if (key) {
          values.add((T)currentKey);
        } else {
          values.add((T)currentValue);
        }
      }
      return nextPageId;
    } finally {
      this.pages.releasePage(page);
    }
  }

  public int getModCount() {
    return this.modCount;
  }

  @Override
  public Set<K> keySet() {
    return new BPlusTreeLeafSet<>(this, true);
  }

  public void print() {
    printPage(this.rootPageIndex);
  }

  private void printPage(final int pageIndex) {
    final Page page = this.pages.getPage(pageIndex);
    try {
      final List<Integer> pageIndexes = new ArrayList<>();
      final int offset = page.getOffset();
      page.setOffset(0);
      final byte pageType = page.readByte();
      final int numBytes = page.readShort();
      if (pageType == INTERIOR) {
        final int pageIndex1 = page.readInt();
        int childPageIndex = pageIndex1;
        pageIndexes.add(childPageIndex);
        System.out.print("I");
        System.out.print(page.getIndex());
        System.out.print("\t");
        System.out.print(numBytes);
        System.out.print("\t");
        System.out.print(pageIndex1);
        while (page.getOffset() < numBytes) {
          final K value = this.keyManager.readFromPage(page);
          final int pageIndex2 = page.readInt();
          childPageIndex = pageIndex2;
          pageIndexes.add(childPageIndex);
          System.out.print("<-");
          System.out.print(value);
          System.out.print("->");
          System.out.print(childPageIndex);
        }
      } else if (pageType == LEAF) {
        System.out.print("L");
        System.out.print(page.getIndex());
        System.out.print("\t");
        System.out.print(numBytes);
        System.out.print("\t");
        boolean first = true;
        while (page.getOffset() < numBytes) {
          if (first) {
            first = false;
          } else {
            System.out.print(",");
          }
          final K key = this.keyManager.readFromPage(page);
          final V value = this.valueManager.readFromPage(page);
          System.out.print(key);
          System.out.print("=");
          System.out.print(value);
        }
      }
      page.setOffset(offset);
      for (final Integer childPageIndex : pageIndexes) {
        printPage(childPageIndex);
      }
    } finally {
      this.pages.releasePage(page);
    }
  }

  protected PutResult put(final int pageIndex, final Integer nextPageIndex, final K key,
    final V value) {
    PutResult result;
    final Page page = this.pages.getPage(pageIndex);
    final byte pageType = page.readByte();
    if (pageType == INTERIOR) {
      result = putInterior(page, key, value);
    } else if (pageType == LEAF) {
      result = putLeaf(page, nextPageIndex, key, value);
    } else {
      throw new IllegalArgumentException("Unknown page type " + pageType);
    }
    this.pages.releasePage(page);
    return result;
  }

  @Override
  public V put(final K key, final V value) {
    this.modCount++;
    final PutResult result = put(this.rootPageIndex, -1, key, value);
    if (result.wasSplit()) {
      final Page rootPage = this.pages.getPage(this.rootPageIndex);
      final Page leftPage = this.pages.newPage();
      leftPage.setContent(rootPage);

      rootPage.clear();
      writePageHeader(rootPage, INTERIOR);

      final int firstChildPageIndex = leftPage.getIndex();
      rootPage.writeInt(firstChildPageIndex);

      final byte[] keyBytes = result.newKeyBytes;
      rootPage.writeBytes(keyBytes);

      rootPage.writeBytes(result.newPageIndexBytes);
      setNumBytes(rootPage);
      this.pages.releasePage(rootPage);
      this.pages.releasePage(leftPage);
    }
    if (!result.hasOldValue) {
      this.size++;
    }
    return result.oldValue;
  }

  private PutResult putInterior(final Page page, final K key, final V value) {
    PutResult result = null;
    final List<byte[]> pageIndexesBytes = new ArrayList<>();
    final List<byte[]> keysBytes = new ArrayList<>();
    final int numBytes = page.readShort();
    final byte[] pageIndexBytes = MethodPageValueManager.getIntBytes(page);
    byte[] previousPageIndexBytes = pageIndexBytes;
    pageIndexesBytes.add(previousPageIndexBytes);
    while (page.getOffset() < numBytes) {
      final byte[] currentKeyBytes = this.keyManager.getBytes(page);
      final K currentKey = this.keyManager.getValue(currentKeyBytes);
      final byte[] nextPageIndexBytes = MethodPageValueManager.getIntBytes(page);
      if (result == null) {
        final int compare = this.comparator.compare(currentKey, key);
        if (compare > 0) {
          final int previousPageIndex = MethodPageValueManager.getIntValue(previousPageIndexBytes);
          final int nextPageIndex = MethodPageValueManager.getIntValue(nextPageIndexBytes);
          result = put(previousPageIndex, nextPageIndex, key, value);
          if (result.wasSplit()) {
            pageIndexesBytes.add(result.newPageIndexBytes);
            keysBytes.add(result.newKeyBytes);
          } else {
            return result;
          }
        }
      }
      keysBytes.add(currentKeyBytes);
      pageIndexesBytes.add(nextPageIndexBytes);
      previousPageIndexBytes = nextPageIndexBytes;
    }
    if (result == null) {
      final int previousPageIndex = MethodPageValueManager.getIntValue(previousPageIndexBytes);
      result = put(previousPageIndex, 0, key, value);
      if (result.wasSplit()) {
        pageIndexesBytes.add(result.newPageIndexBytes);
        keysBytes.add(result.newKeyBytes);
      } else {
        return result;
      }
    }
    updateOrSplitInteriorPage(result, page, keysBytes, pageIndexesBytes);
    return result;
  }

  private PutResult putLeaf(final Page page, final int nextPageIndex, final K key, final V value) {
    final PutResult result = new PutResult();
    final byte[] keyBytes = this.keyManager.getBytes(key);
    final List<byte[]> keysBytes = new ArrayList<>();
    final List<byte[]> valuesBytes = new ArrayList<>();
    final byte[] valueBytes = this.valueManager.getBytes(value);

    boolean newValueWritten = false;
    final int numBytes = page.readShort();
    page.readInt();
    while (page.getOffset() < numBytes) {
      final byte[] currentKeyBytes = this.keyManager.getBytes(page);
      final K currentKey = this.keyManager.getValue(currentKeyBytes);
      final byte[] currentValueBytes = this.valueManager.getBytes(page);
      final int compare = this.comparator.compare(currentKey, key);

      if (compare >= 0) {
        keysBytes.add(keyBytes);
        valuesBytes.add(valueBytes);
        newValueWritten = true;
        result.hasOldValue = true;
      }
      if (compare == 0) {
        result.oldValue = this.valueManager.getValue(currentValueBytes);
      } else {
        keysBytes.add(currentKeyBytes);
        valuesBytes.add(currentValueBytes);
      }

    }
    if (!newValueWritten) {
      keysBytes.add(keyBytes);
      valuesBytes.add(valueBytes);
    }
    updateOrSplitLeafPage(result, page, numBytes, keysBytes, valuesBytes, nextPageIndex);
    return result;
  }

  private RemoveResult remove(final int pageIndex, final K key) {
    final Page page = this.pages.getPage(pageIndex);
    try {
      final byte pageType = page.readByte();
      if (pageType == INTERIOR) {
        return removeInterior(page, key);
      } else if (pageType == LEAF) {
        return removeLeaf(page, key);
      } else {
        throw new IllegalArgumentException("Unknown page type " + pageType);
      }
    } finally {
      this.pages.releasePage(page);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public V remove(final Object key) {
    this.modCount++;
    final RemoveResult result = remove(this.rootPageIndex, (K)key);
    // TODO merge if required
    if (result.hasOldValue) {
      this.size--;
    }
    return result.oldValue;
  }

  private RemoveResult removeInterior(final Page page, final K key) {
    final int numBytes = page.readShort();
    final int pageIndex = page.readInt();
    int previousPageIndex = pageIndex;
    while (page.getOffset() < numBytes) {
      final K currentKey = this.keyManager.readFromPage(page);
      final int nextPageIndex = page.readInt();
      final int compare = this.comparator.compare(currentKey, key);
      if (compare > 0) {
        return remove(previousPageIndex, key);
      }
      previousPageIndex = nextPageIndex;
    }
    return remove(previousPageIndex, key);
  }

  private RemoveResult removeLeaf(final Page page, final K key) {
    final RemoveResult result = new RemoveResult();
    final List<byte[]> keysBytes = new ArrayList<>();
    final List<byte[]> valuesBytes = new ArrayList<>();

    final int numBytes = page.readShort();
    final int nextPageIndex = page.readInt();
    while (page.getOffset() < numBytes) {
      final byte[] keyBytes = this.keyManager.getBytes(page);
      final byte[] valueBytes = this.valueManager.getBytes(page);
      if (result.oldValue == null) {
        final K currentKey = this.keyManager.getValue(keyBytes);
        final int compare = this.comparator.compare(currentKey, key);
        if (compare == 0) {
          result.oldValue = this.valueManager.getValue(valueBytes);
          result.hasOldValue = true;
        } else {
          keysBytes.add(keyBytes);
          valuesBytes.add(valueBytes);
        }
      } else {
        keysBytes.add(keyBytes);
        valuesBytes.add(valueBytes);
      }
    }
    if (result.oldValue != null) {
      setLeafKeyAndValueBytes(page, keysBytes, valuesBytes, 0, keysBytes.size(), nextPageIndex);
    }
    // TODO size
    return result;
  }

  private void setInteriorKeyAndValueBytes(final Page page, final List<byte[]> keysBytes,
    final List<byte[]> pageIndexesBytes, final int startIndex, final int endIndex) {
    page.setOffset(0);
    page.writeByte(INTERIOR);
    page.writeShort((short)0);
    int i = startIndex;
    writeBytes(page, pageIndexesBytes, i);
    for (; i < endIndex; i++) {
      writeBytes(page, keysBytes, i);
      writeBytes(page, pageIndexesBytes, i + 1);
    }
    setNumBytes(page);
    page.clearBytes(page.getOffset());
  }

  private void setLeafKeyAndValueBytes(final Page page, final List<byte[]> keysBytes,
    final List<byte[]> valuesBytes, final int startIndex, final int endIndex,
    final int nextPageIndex) {
    page.setOffset(0);
    writeLeafHeader(page, LEAF, nextPageIndex);
    int i = startIndex;
    for (; i < endIndex; i++) {
      writeBytes(page, keysBytes, i);
      writeBytes(page, valuesBytes, i);
    }
    setNumBytes(page);
    page.clearBytes(page.getOffset());
  }

  @Override
  public int size() {
    return this.size;
  }

  private void updateOrSplitInteriorPage(final PutResult result, final Page page,
    final List<byte[]> keysBytes, final List<byte[]> pageIndexBytes) {
    result.clear();
    int numBytes = this.headerSize;
    int splitIndex = -1;
    int i = 0;
    numBytes += pageIndexBytes.get(0).length;
    while (i < keysBytes.size()) {
      numBytes += keysBytes.get(i).length;
      numBytes += pageIndexBytes.get(i + 1).length;

      i++;
      if (splitIndex == -1 && numBytes > this.minSize) {
        splitIndex = i;
      }
    }

    if (numBytes < page.getSize()) {
      setInteriorKeyAndValueBytes(page, keysBytes, pageIndexBytes, 0, keysBytes.size());
    } else {
      setInteriorKeyAndValueBytes(page, keysBytes, pageIndexBytes, 0, splitIndex);
      final Page rightPage = this.pages.newPage();
      setInteriorKeyAndValueBytes(rightPage, keysBytes, pageIndexBytes, splitIndex,
        keysBytes.size());

      result.newPageIndexBytes = MethodPageValueManager.getValueIntBytes(rightPage.getIndex());
      result.newKeyBytes = keysBytes.get(splitIndex);
      this.pages.releasePage(rightPage);
    }
  }

  private void updateOrSplitLeafPage(final PutResult result, final Page page, final int oldNumBytes,
    final List<byte[]> keysBytes, final List<byte[]> valuesBytes, final int nextPageIndex) {
    int numBytes = this.leafHeaderSize;
    int splitIndex = -1;
    int i = 0;
    while (i < keysBytes.size()) {
      final byte[] keyBytes = keysBytes.get(i);
      numBytes += keyBytes.length;

      final byte[] valueBytes = valuesBytes.get(i);
      numBytes += valueBytes.length;

      i++;
      if (splitIndex == -1 && numBytes > this.minSize) {
        splitIndex = i;
      }
    }
    if (numBytes < page.getSize()) {
      setLeafKeyAndValueBytes(page, keysBytes, valuesBytes, 0, keysBytes.size(), nextPageIndex);
    } else {
      final Page rightPage = this.pages.newPage();
      final int rightPageIndex = rightPage.getIndex();
      setLeafKeyAndValueBytes(page, keysBytes, valuesBytes, 0, splitIndex, rightPageIndex);
      setLeafKeyAndValueBytes(rightPage, keysBytes, valuesBytes, splitIndex, keysBytes.size(),
        nextPageIndex);

      result.newPageIndexBytes = MethodPageValueManager.getValueIntBytes(rightPageIndex);
      result.newKeyBytes = keysBytes.get(splitIndex);
      this.pages.releasePage(rightPage);
    }
  }

  @Override
  public Collection<V> values() {
    return new BPlusTreeLeafSet<>(this, false);
  }

  private void writeBytes(final Page page, final List<byte[]> bytesList, final int i) {
    final byte[] pageIndexBytes = bytesList.get(i);
    page.writeBytes(pageIndexBytes);
  }
}
