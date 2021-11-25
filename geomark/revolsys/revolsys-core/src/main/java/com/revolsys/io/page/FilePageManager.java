package com.revolsys.io.page;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.collection.map.WeakKeyValueMap;
import com.revolsys.io.FileUtil;

public class FilePageManager implements PageManager {
  private final Set<Integer> freePageIndexes = new TreeSet<>();

  // TODO
  private final Map<Integer, Page> pages = new WeakKeyValueMap<>();

  private final Set<Page> pagesInUse = new HashSet<>();

  int pageSize = 64;

  private RandomAccessFile randomAccessFile;

  public FilePageManager() {
    this(FileUtil.newTempFile("pages", ".pf"));
  }

  public FilePageManager(final File file) {
    try {
      this.randomAccessFile = new RandomAccessFile(file, "rw");
    } catch (final FileNotFoundException e) {
      throw new IllegalArgumentException("Unable to open file " + file.getAbsolutePath(), e);
    }
  }

  @Override
  public int getNumPages() {
    return this.pages.size();
  }

  @Override
  public synchronized Page getPage(final int index) {
    synchronized (this.pages) {
      if (this.freePageIndexes.contains(index)) {
        throw new IllegalArgumentException("Page does not exist " + index);
      } else {
        Page page = this.pages.get(index);
        if (page == null) {
          page = loadPage(index);
        }
        if (this.pagesInUse.contains(page)) {
          throw new IllegalArgumentException("Page is currently being used " + index);
        } else {
          this.pagesInUse.add(page);
          page.setOffset(0);
          return page;
        }
      }
    }
  }

  @Override
  public int getPageSize() {
    return this.pageSize;
  }

  private Page loadPage(final int index) {
    try {
      final Page page = new ByteArrayPage(this, index, this.pageSize);
      this.randomAccessFile.seek((long)index * this.pageSize);
      final byte[] content = page.getContent();
      this.randomAccessFile.read(content);
      this.pages.put(index, page);
      return page;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized Page newPage() {
    synchronized (this.pages) {
      Page page;
      if (this.freePageIndexes.isEmpty()) {
        try {
          final int index = (int)(this.randomAccessFile.length() / this.pageSize);
          page = new ByteArrayPage(this, index, this.pageSize);
          this.pages.put(page.getIndex(), page);
          write(page);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        final Iterator<Integer> iterator = this.freePageIndexes.iterator();
        final Integer pageIndex = iterator.next();
        iterator.remove();
        page = loadPage(pageIndex);
      }
      this.pagesInUse.add(page);
      return page;
    }
  }

  @Override
  public Page newTempPage() {
    return new ByteArrayPage(this, -1, this.pageSize);
  }

  @Override
  public synchronized void releasePage(final Page page) {
    write(page);
    this.pagesInUse.remove(page);
  }

  @Override
  public synchronized void removePage(final Page page) {
    synchronized (this.pages) {
      page.clear();
      write(page);
      this.freePageIndexes.add(page.getIndex());
    }
  }

  @Override
  public synchronized void write(final Page page) {
    if (page.getPageManager() == this) {
      synchronized (this.randomAccessFile) {
        try {
          final long index = page.getIndex();
          if (index >= 0) {
            this.randomAccessFile.seek(index * this.pageSize);
            final byte[] content = page.getContent();
            this.randomAccessFile.write(content);
          }
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

}
