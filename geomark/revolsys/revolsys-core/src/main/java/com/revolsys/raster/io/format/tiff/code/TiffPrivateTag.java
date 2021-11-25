package com.revolsys.raster.io.format.tiff.code;

public enum TiffPrivateTag implements TiffTag {
  WangAnnotation(32932, "Annotation data, as used in 'Imaging for Windows'"), //
  MDFileTag(33445,
    "Specifies the pixel data format encoding in the Molecular Dynamics GEL file format"), //
  MDScalePixel(33446, "Specifies a scale factor in the Molecular Dynamics GEL file format"), //
  MDColorTable(33447,
    "Used to specify the conversion from 16bit to 8bit in the Molecular Dynamics GEL file format"), //
  MDLabName(33448,
    "Name of the lab that scanned this file, as used in the Molecular Dynamics GEL file format"), //
  MDSampleInfo(33449,
    "Information about the sample, as used in the Molecular Dynamics GEL file format"), //
  MDPrepDate(33450,
    "Date the sample was prepared, as used in the Molecular Dynamics GEL file format"), //
  MDPrepTime(33451,
    "Time the sample was prepared, as used in the Molecular Dynamics GEL file format"), //
  MDFileUnits(33452,
    "Units for data in this file, as used in the Molecular Dynamics GEL file format"), //
  ModelPixelScaleTag(33550, "Used in interchangeable GeoTIFF files", true), //
  IPTC(33723, "IPTC (International Press Telecommunications Council) metadata"), //
  INGRPacketDataTag(33918, "Intergraph Application specific storage"), //
  INGRFlagRegisters(33919, "Intergraph Application specific flags"), //
  IrasBTransformationMatrix(33920,
    "Originally part of Intergraph's GeoTIFF tags, but likely understood by IrasB only"), //
  ModelTiepointTag(33922,
    "Originally part of Intergraph's GeoTIFF tags, but now used in interchangeable GeoTIFF files",
    true), //
  ModelTransformationTag(34264, "Used in interchangeable GeoTIFF files"), //
  Photoshop(34377, "Collection of Photoshop 'Image Resource Blocks'"), //
  ExifIFD(34665, "A pointer to the Exif IFD"), //
  ICCProfile(34675, "ICC profile data"), //
  GeoKeyDirectoryTag(34735, "Used in interchangeable GeoTIFF files", true), //
  GeoDoubleParamsTag(34736, "Used in interchangeable GeoTIFF files", true), //
  GeoAsciiParamsTag(34737, "Used in interchangeable GeoTIFF files"), //
  GPSIFD(34853, "A pointer to the Exif-related GPS Info IFD"), //
  HylaFAXFaxRecvParams(34908, "Used by HylaFAX"), //
  HylaFAXFaxSubAddress(34909, "Used by HylaFAX"), //
  HylaFAXFaxRecvTime(34910, "Used by HylaFAX"), //
  ImageSourceData(37724, "Used by Adobe Photoshop"), //
  InteroperabilityIFD(40965, "A pointer to the Exif-related Interoperability IFD"), //
  GDAL_METADATA(42112,
    "Used by the GDAL library, holds an XML list of name=value 'metadata' values about the image as a whole, and about specific samples"), //
  GDAL_NODATA(42113,
    "Used by the GDAL library, contains an ASCII encoded nodata or background pixel value"), //
  OceScanjobDescription(50215, "Used in the Oce scanning process"), //
  OceApplicationSelector(50216, "Used in the Oce scanning process"), //
  OceIdentificationNumber(50217, "Used in the Oce scanning process"), //
  OceImageLogicCharacteristics(50218, "Used in the Oce scanning process"), //
  DNGVersion(50706, "Used in IFD 0 of DNG files"), //
  DNGBackwardVersion(50707, "Used in IFD 0 of DNG files"), //
  UniqueCameraModel(50708, "Used in IFD 0 of DNG files"), //
  LocalizedCameraModel(50709, "Used in IFD 0 of DNG files"), //
  CFAPlaneColor(50710, "Used in Raw IFD of DNG files"), //
  CFALayout(50711, "Used in Raw IFD of DNG files"), //
  LinearizationTable(50712, "Used in Raw IFD of DNG files"), //
  BlackLevelRepeatDim(50713, "Used in Raw IFD of DNG files"), //
  BlackLevel(50714, "Used in Raw IFD of DNG files"), //
  BlackLevelDeltaH(50715, "Used in Raw IFD of DNG files"), //
  BlackLevelDeltaV(50716, "Used in Raw IFD of DNG files"), //
  WhiteLevel(50717, "Used in Raw IFD of DNG files"), //
  DefaultScale(50718, "Used in Raw IFD of DNG files"), //
  DefaultCropOrigin(50719, "Used in Raw IFD of DNG files"), //
  DefaultCropSize(50720, "Used in Raw IFD of DNG files"), //
  ColorMatrix1(50721, "Used in IFD 0 of DNG files"), //
  ColorMatrix2(50722, "Used in IFD 0 of DNG files"), //
  CameraCalibration1(50723, "Used in IFD 0 of DNG files"), //
  CameraCalibration2(50724, "Used in IFD 0 of DNG files"), //
  ReductionMatrix1(50725, "Used in IFD 0 of DNG files"), //
  ReductionMatrix2(50726, "Used in IFD 0 of DNG files"), //
  AnalogBalance(50727, "Used in IFD 0 of DNG files"), //
  AsShotNeutral(50728, "Used in IFD 0 of DNG files"), //
  AsShotWhiteXY(50729, "Used in IFD 0 of DNG files"), //
  BaselineExposure(50730, "Used in IFD 0 of DNG files"), //
  BaselineNoise(50731, "Used in IFD 0 of DNG files"), //
  BaselineSharpness(50732, "Used in IFD 0 of DNG files"), //
  BayerGreenSplit(50733, "Used in Raw IFD of DNG files"), //
  LinearResponseLimit(50734, "Used in IFD 0 of DNG files"), //
  CameraSerialNumber(50735, "Used in IFD 0 of DNG files"), //
  LensInfo(50736, "Used in IFD 0 of DNG files"), //
  ChromaBlurRadius(50737, "Used in Raw IFD of DNG files"), //
  AntiAliasStrength(50738, "Used in Raw IFD of DNG files"), //
  DNGPrivateData(50740, "Used in IFD 0 of DNG files"), //
  MakerNoteSafety(50741, "Used in IFD 0 of DNG files"), //
  CalibrationIlluminant1(50778, "Used in IFD 0 of DNG files"), //
  CalibrationIlluminant2(50779, "Used in IFD 0 of DNG files"), //
  BestQualityScale(50780, "Used in Raw IFD of DNG files"), //
  AliasLayerMetadata(50784, "Alias Sketchbook Pro layer usage description"), //
  TIFF_RSID(50908, "This private id is used in a GEOTIFF standard by DGIWG"), //
  GEO_METADATA(50909, "This private id is used in a GEOTIFF standard by DGIWG") //
  ;

  private boolean array;

  private String description;

  private int id;

  private TiffPrivateTag(final int id, final String description) {
    this(id, description, false);
  }

  private TiffPrivateTag(final int id, final String description, final boolean array) {
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
