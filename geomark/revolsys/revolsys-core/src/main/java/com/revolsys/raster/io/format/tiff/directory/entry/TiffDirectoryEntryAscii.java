package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;

public class TiffDirectoryEntryAscii extends AbstractTiffDirectoryEntry<String> {

  public TiffDirectoryEntryAscii() {
  }

  @Override
  public String getString() {
    return this.value;
  }

  @Override
  public TiffFieldType getType() {
    return TiffFieldType.ASCII;
  }

  @Override
  protected String loadValueDo(final DataReader in, final int count) {
    return in.getUsAsciiString(count);
  }

  @Override
  public void writeValueDo(final ChannelWriter out) {
    out.putString(this.value, this.value.length());
  }
}
