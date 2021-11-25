package com.revolsys.io.page;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MemoryPageManager implements PageManager {
  private final Set<Page> freePages = new TreeSet<>();

  private final List<Page> pages = new ArrayList<>();

  private final Set<Page> pagesInUse = new HashSet<>();

  int pageSize = 64;

  @Override
  public synchronized int getNumPages() {
    return this.pages.size() - this.freePages.size();
  }

  @Override
  public synchronized Page getPage(final int index) {
    final Page page = this.pages.get(index);
    if (this.freePages.contains(page)) {
      throw new IllegalArgumentException("Page does not exist " + index);
    } else if (this.pagesInUse.contains(page)) {
      throw new IllegalArgumentException("Page is currently being used " + index);
    } else {
      page.setOffset(0);
      this.pagesInUse.add(page);
      return page;
    }
  }

  @Override
  public int getPageSize() {
    return this.pageSize;
  }

  @Override
  public synchronized Page newPage() {
    Page page;
    if (this.freePages.isEmpty()) {
      page = new ByteArrayPage(this, this.pages.size(), this.pageSize);
      this.pages.add(page);
    } else {
      final Iterator<Page> iterator = this.freePages.iterator();
      page = iterator.next();
      iterator.remove();
    }
    this.pagesInUse.add(page);
    return page;
  }

  @Override
  public Page newTempPage() {
    return new ByteArrayPage(this, -1, this.pageSize);
  }

  @Override
  public synchronized void releasePage(final Page page) {
    this.pagesInUse.remove(page);
  }

  @Override
  public synchronized void removePage(final Page page) {
    this.freePages.add(page);
    this.pagesInUse.remove(page);
  }

  @Override
  public void write(final Page page) {
  }
}
