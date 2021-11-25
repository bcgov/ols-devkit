package com.revolsys.raster.io.format.tiff.cmd;

import java.io.PrintStream;

import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.TiffDirectoryIterator;

public class TiffDump {
  public static void dump(final Object source, final PrintStream out) {
    try (
      TiffDirectoryIterator directoryIterator = TiffDirectoryIterator.newIterator(source)) {
      for (final TiffDirectory directory : directoryIterator) {
        directory.dump(out);
        out.println();
      }
    }
  }

  public static void main(final String[] args) {
    final String file = args[0];
    final PrintStream out = System.out;
    dump(file, out);

  }
}
