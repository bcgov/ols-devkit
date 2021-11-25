package com.revolsys.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.revolsys.record.schema.RecordDefinition;

public class ListResultPager<T> implements ResultPager<T> {
  private final List<T> list = new ArrayList<>();

  /** The current page number. */
  private int pageNumber = -1;

  /** The number of objects in a page. */
  private int pageSize = 10;

  private final RecordDefinition recordDefinition;

  public ListResultPager(final RecordDefinition recordDefinition,
    final Collection<? extends T> list) {
    this.recordDefinition = recordDefinition;
    this.list.addAll(list);
  }

  @Override
  public void close() {
  }

  /**
   * Get the index of the last object in the current page.
   *
   * @return The index of the last object in the current page.
   */
  @Override
  public int getEndIndex() {
    final int numPages = getNumPages();
    if (numPages == 0) {
      return 0;
    } else if (this.pageNumber < numPages - 1) {
      return (this.pageNumber + 1) * this.pageSize;
    } else {
      return this.list.size();
    }
  }

  /**
   * Get the list of objects in the current page.
   *
   * @return The list of objects in the current page.
   */
  @Override
  public List<T> getList() {
    if (getNumResults() == 0) {
      return Collections.emptyList();
    } else {
      final int startIndex = getStartIndex() - 1;
      final int endIndex = getEndIndex();
      return this.list.subList(startIndex, endIndex);
    }
  }

  /**
   * Get the page number of the next page.
   *
   * @return Thepage number of the next page.
   */
  @Override
  public int getNextPageNumber() {
    return this.pageNumber + 2;
  }

  /**
   * Get the number of pages.
   *
   * @return The number of pages.
   */
  @Override
  public int getNumPages() {
    return (int)Math.ceil((double)this.list.size() / getPageSize());
  }

  /**
   * Get the total number of results returned.
   *
   * @return The total number of results returned.
   */
  @Override
  public int getNumResults() {
    return this.list.size();
  }

  /**
   * Get the page number of the current page.
   *
   * @return Thepage number of the current page.
   */
  @Override
  public int getPageNumber() {
    return this.pageNumber + 1;
  }

  /**
   * Get the number of objects to display in a page.
   *
   * @return The number of objects to display in a page.
   */
  @Override
  public int getPageSize() {
    return this.pageSize;
  }

  /**
   * Get the page number of the previous page.
   *
   * @return Thepage number of the previous page.
   */
  @Override
  public int getPreviousPageNumber() {
    return this.pageNumber;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  /**
   * Get the index of the first object in the current page.
   *
   * @return The index of the first object in the current page.
   */
  @Override
  public int getStartIndex() {
    final int numPages = getNumPages();
    if (numPages == 0) {
      return 0;
    } else if (this.pageNumber < numPages - 1) {
      return this.pageNumber * this.pageSize + 1;
    } else {
      return (numPages - 1) * this.pageSize + 1;
    }
  }

  /**
   * Check to see if there is a next page.
   *
   * @return True if there is a next page.
   */
  @Override
  public boolean hasNextPage() {
    return this.pageNumber < getNumPages();
  }

  /**
   * Check to see if there is a previous page.
   *
   * @return True if there is a previous page.
   */
  @Override
  public boolean hasPreviousPage() {
    return this.pageNumber > 0;
  }

  /**
   * Check to see if this is the first page.
   *
   * @return True if this is the first page.
   */
  @Override
  public boolean isFirstPage() {
    return this.pageNumber == 0;
  }

  /**
   * Check to see if this is the last page.
   *
   * @return True if this is the last page.
   */
  @Override
  public boolean isLastPage() {
    return this.pageNumber == getNumPages();
  }

  /**
   * Set the current page number.
   *
   * @param pageNumber The current page number.
   */
  @Override
  public void setPageNumber(final int pageNumber) {
    if (pageNumber - 1 > getNumPages()) {
      this.pageNumber = getNumPages();
    } else if (pageNumber <= 0) {
      this.pageNumber = 0;
    } else {
      this.pageNumber = pageNumber - 1;
    }
  }

  /**
   * Set the number of objects per page.
   *
   * @param pageSize The number of objects per page.
   */
  @Override
  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
  }
}
