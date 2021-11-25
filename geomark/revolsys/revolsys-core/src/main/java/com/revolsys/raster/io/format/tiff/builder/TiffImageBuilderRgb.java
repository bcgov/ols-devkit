package com.revolsys.raster.io.format.tiff.builder;

import java.util.function.Function;

import com.revolsys.raster.io.format.tiff.TiffDirectoryBuilder;
import com.revolsys.raster.io.format.tiff.TiffFileBuilder;
import com.revolsys.raster.io.format.tiff.code.TiffPhotogrametricInterpretation;

public class TiffImageBuilderRgb extends TiffDirectoryBuilder {

  public static Function<TiffFileBuilder, TiffImageBuilderRgb> newBits(final int bitsPerSample) {
    return fileBuilder -> new TiffImageBuilderRgb(fileBuilder, bitsPerSample);
  }

  public TiffImageBuilderRgb(final TiffFileBuilder fileBuilder, final int bitsPerSample) {
    super(fileBuilder, TiffPhotogrametricInterpretation.RGB);
    setBitsPerSample(bitsPerSample, bitsPerSample, bitsPerSample);
  }

}
