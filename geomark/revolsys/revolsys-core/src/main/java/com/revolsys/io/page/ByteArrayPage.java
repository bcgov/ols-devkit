package com.revolsys.io.page;

public class ByteArrayPage extends AbstractPage {
  private final byte[] content;

  private int offset = 0;

  public ByteArrayPage(final PageManager pageManager, final int index, final int size) {
    super(pageManager, index);
    this.content = new byte[size];
  }

  @Override
  public byte[] getContent() {
    return this.content;
  }

  @Override
  public int getOffset() {
    return this.offset;
  }

  @Override
  public int getSize() {
    return this.content.length;
  }

  @Override
  protected int readNextByte() {
    final byte b = this.content[this.offset];
    this.offset++;
    return b & 0xff;
  }

  @Override
  public void setContent(final Page page) {
    final byte[] copyContent = page.getContent();
    System.arraycopy(copyContent, 0, this.content, 0, copyContent.length);
  }

  @Override
  public void setOffset(final int offset) {
    if (offset > getSize()) {
      throw new IllegalArgumentException("Cannot set offset past end of file ");
    } else {
      this.offset = offset;
    }
  }

  @Override
  protected void writeByte(final int b) {
    this.content[this.offset++] = (byte)b;
  }
}
