package com.revolsys.elevation.cloud.las.zip;

public interface LasZipDecompressSelective {

  int ALL = 0xFFFFFFFF;

  int BYTE0 = 0x00010000;

  int BYTE1 = 0x00020000;

  int BYTE2 = 0x00040000;

  int BYTE3 = 0x00080000;

  int BYTE4 = 0x00100000;

  int BYTE5 = 0x00200000;

  int BYTE6 = 0x00400000;

  int BYTE7 = 0x00800000;

  int CHANNEL_RETURNS_XY = 0x00000000;

  int CLASSIFICATION = 0x00000002;

  int EXTRA_BYTES = 0xFFFF0000;

  int FLAGS = 0x00000004;

  int GPS_TIME = 0x00000080;

  int INTENSITY = 0x00000008;

  int NIR = 0x00000200;

  int POINT_SOURCE = 0x00000040;

  int RGB = 0x00000100;

  int SCAN_ANGLE = 0x00000010;

  int USER_DATA = 0x00000020;

  int WAVEPACKET = 0x00000400;

  int Z = 0x00000001;

}
