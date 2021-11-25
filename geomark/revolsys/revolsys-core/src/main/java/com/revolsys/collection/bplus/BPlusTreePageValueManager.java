package com.revolsys.collection.bplus;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.page.MethodPageValueManager;
import com.revolsys.io.page.Page;
import com.revolsys.io.page.PageManager;
import com.revolsys.io.page.PageValueManager;

public class BPlusTreePageValueManager<T> implements PageValueManager<T> {

  public static <T> PageValueManager<T> newPageValueManager(final PageManager pageManager,
    final PageValueManager<T> valueSerializer) {
    return new BPlusTreePageValueManager<>(pageManager, valueSerializer);
  }

  private final PageManager pageManager;

  private final PageValueManager<T> valueSerializer;

  public BPlusTreePageValueManager(final PageManager pageManager,
    final PageValueManager<T> valueSerializer) {
    this.pageManager = pageManager;
    this.valueSerializer = valueSerializer;
  }

  @Override
  public void disposeBytes(final byte[] bytes) {
    final int pageIndex = MethodPageValueManager.getIntValue(bytes);
    Page dataPage = this.pageManager.getPage(pageIndex);
    dataPage.setOffset(0);
    byte pageType = dataPage.readByte();
    while (pageType == BPlusTreeMap.EXTENDED) {
      BPlusTreeMap.skipHeader(dataPage);
      final int nextPageIndex = dataPage.readInt();
      this.pageManager.removePage(dataPage);
      dataPage = this.pageManager.getPage(nextPageIndex);
      dataPage.setOffset(0);
      pageType = dataPage.readByte();
    }
    if (pageType == BPlusTreeMap.DATA) {
      this.pageManager.removePage(dataPage);
      this.pageManager.releasePage(dataPage);
    } else {
      throw new IllegalArgumentException(
        "Expecting a data page " + BPlusTreeMap.DATA + " not " + pageType);
    }
  }

  @Override
  public byte[] getBytes(final Page page) {
    return page.readBytes(4);
  }

  @Override
  public byte[] getBytes(final T value) {
    final byte[] valueBytes = this.valueSerializer.getBytes(value);

    int offset = 0;
    final int pageSize = this.pageManager.getPageSize();
    Page page = this.pageManager.newPage();
    try {
      final int pageIndex = page.getIndex();
      while (valueBytes.length + 3 > offset + pageSize) {
        final Page nextPage = this.pageManager.newPage();
        BPlusTreeMap.writePageHeader(page, BPlusTreeMap.EXTENDED);
        page.writeInt(nextPage.getIndex());
        page.writeBytes(valueBytes, offset, pageSize - 7);
        BPlusTreeMap.setNumBytes(page);
        this.pageManager.releasePage(page);
        page = nextPage;
        offset += pageSize - 7;
      }

      BPlusTreeMap.writePageHeader(page, BPlusTreeMap.DATA);
      page.writeBytes(valueBytes, offset, valueBytes.length - offset);
      BPlusTreeMap.setNumBytes(page);

      return PageValueManager.INT.getBytes(pageIndex);
    } finally {
      this.pageManager.releasePage(page);
    }
  }

  @Override
  public <V extends T> V getValue(final byte[] indexBytes) {
    final int pageIndex = MethodPageValueManager.getIntValue(indexBytes);
    Page dataPage = this.pageManager.getPage(pageIndex);
    try {
      dataPage.setOffset(0);
      byte pageType = dataPage.readByte();
      final List<byte[]> pageBytes = new ArrayList<>();
      int size = 0;
      while (pageType == BPlusTreeMap.EXTENDED) {
        final int numBytes = dataPage.readShort() - 7;
        final int nextPageIndex = dataPage.readInt();
        final byte[] bytes = dataPage.readBytes(numBytes);
        pageBytes.add(bytes);
        size += bytes.length;
        this.pageManager.releasePage(dataPage);
        dataPage = this.pageManager.getPage(nextPageIndex);
        dataPage.setOffset(0);
        pageType = dataPage.readByte();
      }
      if (pageType == BPlusTreeMap.DATA) {
        final int numBytes = dataPage.readShort() - 3;
        final byte[] bytes = dataPage.readBytes(numBytes);
        pageBytes.add(bytes);
        size += bytes.length;

      } else {
        throw new IllegalArgumentException(
          "Expecting a data page " + BPlusTreeMap.DATA + " not " + pageType);
      }
      final byte[] valueBytes = new byte[size];
      int offset = 0;
      for (final byte[] bytes : pageBytes) {
        System.arraycopy(bytes, 0, valueBytes, offset, bytes.length);
        offset += bytes.length;
      }
      return this.valueSerializer.getValue(valueBytes);
    } finally {
      this.pageManager.releasePage(dataPage);
    }
  }

  //
  // public <V extends T> V removeFromPage(Page page) {
  // final int pageIndex = page.readInt();
  // Page dataPage = pageManager.getPage(pageIndex);
  // dataPage.setOffset(0);
  // byte pageType = dataPage.readByte();
  // List<byte[]> pageBytes = new ArrayList<byte[]>();
  // int size = 0;
  // while (pageType == BPlusTreeMap.EXTENDED) {
  // int numBytes = dataPage.readShort() - 7;
  // int nextPageIndex = dataPage.readInt();
  // byte[] bytes = dataPage.readBytes(numBytes);
  // pageBytes.add(bytes);
  // size += bytes.length;
  // pageManager.removePage(dataPage);
  // dataPage = pageManager.getPage(nextPageIndex);
  // dataPage.setOffset(0);
  // pageType = dataPage.readByte();
  // }
  // if (pageType == BPlusTreeMap.DATA) {
  // final int numBytes = dataPage.readShort() - 3;
  // final byte[] bytes = dataPage.readBytes(numBytes);
  // pageBytes.add(bytes);
  // size += bytes.length;
  // pageManager.removePage(dataPage);
  // } else {
  // throw new IllegalArgumentException("Expecting a data page "
  // + BPlusTreeMap.DATA + " not " + pageType);
  // }
  // byte[] valueBytes = new byte[size];
  // int offset = 0;
  // for (byte[] bytes : pageBytes) {
  // System.arraycopy(bytes, 0, valueBytes, offset, bytes.length);
  // offset += bytes.length;
  // }
  // return valueSerializer.readFromByteArray(valueBytes);
  // }

  @Override
  public <V extends T> V readFromPage(final Page page) {
    final byte[] indexBytes = getBytes(page);
    return getValue(indexBytes);
  }

}
