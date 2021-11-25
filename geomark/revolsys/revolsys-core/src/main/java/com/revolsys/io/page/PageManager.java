package com.revolsys.io.page;

public interface PageManager {

  int getNumPages();

  Page getPage(int index);

  int getPageSize();

  Page newPage();

  Page newTempPage();

  void releasePage(Page page);

  void removePage(Page page);

  void write(Page page);
}
