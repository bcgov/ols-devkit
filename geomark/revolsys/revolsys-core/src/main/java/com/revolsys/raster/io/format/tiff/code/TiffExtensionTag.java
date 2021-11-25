package com.revolsys.raster.io.format.tiff.code;

public enum TiffExtensionTag implements TiffTag {
  DocumentName(269, "The name of the document from which this image was scanned"), //
  PageName(285, "The name of the page from which this image was scanned"), //
  XPosition(286, "X position of the image"), //
  YPosition(287, "Y position of the image"), //
  T4Options(292, "Options for Group 3 Fax compression"), //
  T6Options(293, "Options for Group 4 Fax compression"), //
  PageNumber(297, "The page number of the page from which this image was scanned", true), //
  TransferFunction(301, "Describes a transfer function for the image in tabular style", true), //
  Predictor(317,
    "A mathematical operator that is applied to the image data before an encoding scheme is applied"), //
  WhitePoint(318, "The chromaticity of the white point of the image", true), //
  PrimaryChromaticities(319, "The chromaticities of the primaries of the image", true), //
  HalftoneHints(321,
    "Conveys to the halftone function the range of gray levels within a colorimetrically-specified image that should retain tonal detail",
    true), //
  TileWidth(322, "The tile width in pixels. This is the number of columns in each tile"), //
  TileLength(323, "The tile length (height) in pixels. This is the number of rows in each tile"), //
  TileOffsets(324, "For each tile, the byte offset of that tile, as compressed and stored on disk",
    true), //
  TileByteCounts(325, "For each tile, the number of (compressed) bytes in that tile", true), //
  BadFaxLines(326,
    "Used in the TIFF-F standard, denotes the number of 'bad' scan lines encountered by the facsimile device"), //
  CleanFaxData(327,
    "Used in the TIFF-F standard, indicates if 'bad' lines encountered during reception are stored in the data, or if 'bad' lines have been replaced by the receiver"), //
  ConsecutiveBadFaxLines(328,
    "Used in the TIFF-F standard, denotes the maximum number of consecutive 'bad' scanlines received"), //
  SubIFDs(330, "Offset to child IFDs"), //
  InkSet(332, "The set of inks used in a separated (PhotometricInterpretation=5) image"), //
  InkNames(333, "The name of each ink used in a separated image", true), //
  NumberOfInks(334, "The number of inks"), //
  DotRange(336, "The component values that correspond to a 0% dot and 100% dot", true), //
  TargetPrinter(337,
    "A description of the printing environment for which this separation is intended", true), //
  SampleFormat(339, "Specifies how to interpret each data sample in a pixel", true), //
  SMinSampleValue(340, "Specifies the minimum sample value", true), //
  SMaxSampleValue(341, "Specifies the maximum sample value", true), //
  TransferRange(342, "Expands the range of the TransferFunction", true), //
  ClipPath(343, "Mirrors the essentials of PostScript's path creation functionality"), //
  XClipPathUnits(344,
    "The number of units that span the width of the image, in terms of integer ClipPath coordinates"), //
  YClipPathUnits(345,
    "The number of units that span the height of the image, in terms of integer ClipPath coordinates"), //
  Indexed(346,
    "Aims to broaden the support for indexed images to include support for any color space"), //
  JPEGTables(347, "JPEG quantization and/or Huffman tables", true), //
  OPIProxy(351, "OPI-related"), //
  GlobalParametersIFD(400,
    "Used in the TIFF-FX standard to point to an IFD containing tags that are globally applicable to the complete TIFF file"), //
  ProfileType(401,
    "Used in the TIFF-FX standard, denotes the type of data stored in this file or IFD"), //
  FaxProfile(402, "Used in the TIFF-FX standard, denotes the 'profile' that applies to this file"), //
  CodingMethods(403,
    "Used in the TIFF-FX standard, indicates which coding methods are used in the file"), //
  VersionYear(404,
    "Used in the TIFF-FX standard, denotes the year of the standard specified by the FaxProfile field"), //
  ModeNumber(405,
    "Used in the TIFF-FX standard, denotes the mode of the standard specified by the FaxProfile field"), //
  Decode(433,
    "Used in the TIFF-F and TIFF-FX standards, holds information about the ITULAB (PhotometricInterpretation = 10) encoding"), //
  DefaultImageColor(434,
    "Defined in the Mixed Raster Content part of RFC 2301, is the default color needed in areas where no image is available"), //
  JPEGProc(512,
    "Old-style JPEG compression field. TechNote2 invalidates this part of the specification"), //
  JPEGInterchangeFormat(513,
    "Old-style JPEG compression field. TechNote2 invalidates this part of the specification"), //
  JPEGInterchangeFormatLength(514,
    "Old-style JPEG compression field. TechNote2 invalidates this part of the specification"), //
  JPEGRestartInterval(515,
    "Old-style JPEG compression field. TechNote2 invalidates this part of the specification"), //
  JPEGLosslessPredictors(517,
    "Old-style JPEG compression field. TechNote2 invalidates this part of the specification", true), //
  JPEGPointTransforms(518,
    "Old-style JPEG compression field. TechNote2 invalidates this part of the specification", true), //
  JPEGQTables(519,
    "Old-style JPEG compression field. TechNote2 invalidates this part of the specification", true), //
  JPEGDCTables(520,
    "Old-style JPEG compression field. TechNote2 invalidates this part of the specification", true), //
  JPEGACTables(521,
    "Old-style JPEG compression field. TechNote2 invalidates this part of the specification", true), //
  YCbCrCoefficients(529, "The transformation from RGB to YCBCR image data", true), //
  YCbCrSubSampling(530,
    "Specifies the subsampling factors used for the chrominance components of a YCBCR image", true), //
  YCbCrPositioning(531,
    "Specifies the positioning of subsampled chrominance components relative to luminance samples"), //
  ReferenceBlackWhite(532,
    "Specifies a pair of headroom and footroom image data values (codes) for each pixel component",
    true), //
  StripRowCounts(559,
    "Defined in the Mixed Raster Content part of RFC 2301, used to replace RowsPerStrip for IFDs with variable-sized strips"), //
  XMP(700, "XML packet containing XMP metadata"), //
  ImageID(32781, "OPI-related"), //
  ImageLayer(34732,
    "Defined in the Mixed Raster Content part of RFC 2301, used to denote the particular function of this Image in the mixed raster scheme") //
  ;

  private boolean array;

  private int id;

  private String description;

  private TiffExtensionTag(final int id, final String description) {
    this.id = id;
    this.description = description;
  }

  private TiffExtensionTag(final int id, final String description, final boolean array) {
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
