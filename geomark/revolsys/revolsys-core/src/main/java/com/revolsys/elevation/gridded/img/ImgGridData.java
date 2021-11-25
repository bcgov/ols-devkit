package com.revolsys.elevation.gridded.img;

class ImgGridData {

  private final int width;

  private final int height;

  private final int itemType;

  private final Object data;

  public ImgGridData(final int width, final int height, final int itemType, final Object data) {
    this.width = width;
    this.height = height;
    this.itemType = itemType;
    this.data = data;
  }

  public Object getData() {
    return this.data;
  }

  public int getHeight() {
    return this.height;
  }

  public int getItemType() {
    return this.itemType;
  }

  public int getWidth() {
    return this.width;
  }

}
