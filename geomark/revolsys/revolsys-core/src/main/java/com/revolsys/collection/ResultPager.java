/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.collection;

import java.io.Closeable;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.record.schema.RecordDefinition;

/**
 * <p>
 * The ResultPager defines an interface for paging through the results of a
 * query. Methods are provided to set the number of objects to display per page
 * and the current page number. When these values change the implementation will
 * update the information about the number of pages and if the current page is
 * the first/last page or has previous/next pages that allow user interfaces to
 * display the appropriate navigation controls. The {@link #getList()} method is
 * used to return the List of objects in the current page.
 * </p>
 *
 * @author Paul Austin
 * @param <T> The type of object to page.
 */
public interface ResultPager<T> extends Closeable {
  @Override
  void close();

  default void forEachInPage(final Consumer<T> action) {
    final List<T> list = getList();
    list.forEach(action);
  }

  /**
   * Get the index of the last object in the current page.
   *
   * @return The index of the last object in the current page.
   */
  int getEndIndex();

  /**
   * Get the list of objects in the current page.
   *
   * @return The list of objects in the current page.
   */
  List<T> getList();

  /**
   * Get the page number of the next page.
   *
   * @return Thepage number of the next page.
   */
  int getNextPageNumber();

  /**
   * Get the number of pages.
   *
   * @return The number of pages.
   */
  int getNumPages();

  /**
   * Get the total number of results returned.
   *
   * @return The total number of results returned.
   */
  int getNumResults();

  /**
   * Get the page number of the current page.
   *
   * @return Thepage number of the current page.
   */
  int getPageNumber();

  /**
   * Get the number of objects to display in a page.
   *
   * @return The number of objects to display in a page.
   */
  int getPageSize();

  /**
   * Get the page number of the previous page.
   *
   * @return Thepage number of the previous page.
   */
  int getPreviousPageNumber();

  RecordDefinition getRecordDefinition();

  /**
   * Get the index of the first object in the current page.
   *
   * @return The index of the first object in the current page.
   */
  int getStartIndex();

  /**
   * Check to see if there is a next page.
   *
   * @return True if there is a next page.
   */
  boolean hasNextPage();

  /**
   * Check to see if there is a previous page.
   *
   * @return True if there is a previous page.
   */
  boolean hasPreviousPage();

  /**
   * Check to see if this is the first page.
   *
   * @return True if this is the first page.
   */
  boolean isFirstPage();

  /**
   * Check to see if this is the last page.
   *
   * @return True if this is the last page.
   */
  boolean isLastPage();

  /**
   * Set the current page number.
   *
   * @param pageNumber The current page number.
   */
  void setPageNumber(int pageNumber);

  default void setPageNumberAndSize(final int pageSize, final int pageNumber) {
    setPageSize(pageSize);
    setPageNumber(pageNumber);
  }

  /**
   * Set the number of objects per page.
   *
   * @param pageSize The number of objects per page.
   */
  void setPageSize(int pageSize);
}
