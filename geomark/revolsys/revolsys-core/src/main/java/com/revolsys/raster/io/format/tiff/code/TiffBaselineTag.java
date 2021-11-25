package com.revolsys.raster.io.format.tiff.code;

public enum TiffBaselineTag implements TiffTag {
  NewSubfileType(254, "A general indication of the kind of data contained in this subfile"), //
  SubfileType(255, "A general indication of the kind of data contained in this subfile"), //
  ImageWidth(256, "The number of columns in the image, i.e., the number of pixels per row"), //
  ImageLength(257, "The number of rows of pixels in the image"), //
  BitsPerSample(258, "Number of bits per component", true), //
  Compression(259, "Compression scheme used on the image data"), //
  PhotometricInterpretation(262, "The color space of the image data"), //
  Threshholding(263,
    "For black and white TIFF files that represent shades of gray, the technique used to convert from gray to black and white pixels",
    false), //
  CellWidth(264,
    "The width of the dithering or halftoning matrix used to create a dithered or halftoned bilevel file",
    false), //
  CellLength(265,
    "The length of the dithering or halftoning matrix used to create a dithered or halftoned bilevel file",
    false), //
  FillOrder(266, "The logical order of bits within a byte"), //
  ImageDescription(270, "A string that describes the subject of the image"), //
  Make(271, "The scanner manufacturer"), //
  Model(272, "The scanner model name or number"), //
  StripOffsets(273, "For each strip, the byte offset of that strip", true), //
  Orientation(274, "The orientation of the image with respect to the rows and columns"), //
  SamplesPerPixel(277, "The number of components per pixel"), //
  RowsPerStrip(278, "The number of rows per strip"), //
  StripByteCounts(279, "For each strip, the number of bytes in the strip after compression", true), //
  MinSampleValue(280, "The minimum component value used", true), //
  MaxSampleValue(281, "The maximum component value used", true), //
  XResolution(282, "The number of pixels per ResolutionUnit in the ImageWidth direction"), //
  YResolution(283, "The number of pixels per ResolutionUnit in the ImageLength direction"), //
  PlanarConfiguration(284, "How the components of each pixel are stored"), //
  FreeOffsets(288,
    "For each string of contiguous unused bytes in a TIFF file, the byte offset of the string",
    true), //
  FreeByteCounts(289,
    "For each string of contiguous unused bytes in a TIFF file, the number of bytes in the string",
    true), //
  GrayResponseUnit(290, "The precision of the information contained in the GrayResponseCurve",
    false), //
  GrayResponseCurve(291, "For grayscale data, the optical density of each possible pixel value",
    true), //
  ResolutionUnit(296, "The unit of measurement for XResolution and YResolution"), //
  Software(305, "Name and version number of the software package(s) used to create the image",
    false), //
  DateTime(306, "Date and time of image creation"), //
  Artist(315, "Person who created the image"), //
  HostComputer(316, "The computer and/or operating system in use at the time of image creation",
    false), //
  ColorMap(320, "A color map for palette color images", true), //
  ExtraSamples(338, "Description of extra components", true), //
  Copyright(33432, "Copyright notice") //
  ;

  private boolean array;

  private String description;

  private int id;

  private TiffBaselineTag(final int id, final String description) {
    this(id, description, false);
  }

  private TiffBaselineTag(final int id, final String description, final boolean array) {
    this.id = id;
    this.description = description;
    this.array = array;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public boolean isArray() {
    return this.array;
  }
}
