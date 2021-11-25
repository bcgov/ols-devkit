package com.revolsys.elevation.cloud.las.zip.context;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.StreamingMedian5;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipContextPoint14 {

  public boolean unused;

  public LasPoint lastPoint;

  public int legacy_return_number;

  public int legacy_number_of_returns;

  public int[] last_intensity = new int[8];

  public boolean gps_time_change;

  public StreamingMedian5[] last_X_diff_median5 = StreamingMedian5.newStreamingMedian5(12);

  public StreamingMedian5[] last_Y_diff_median5 = StreamingMedian5.newStreamingMedian5(12);

  public int[] last_Z = new int[8];

  public ArithmeticModel[] m_changed_values = new ArithmeticModel[8];

  public ArithmeticModel m_scanner_channel;

  public ArithmeticModel[] m_number_of_returns = new ArithmeticModel[16];

  public ArithmeticModel m_return_number_gps_same;

  public ArithmeticModel[] m_return_number = new ArithmeticModel[16];

  public ArithmeticCodingInteger ic_dX;

  public ArithmeticCodingInteger ic_dY;

  public ArithmeticCodingInteger ic_Z;

  public ArithmeticModel[] m_classification = new ArithmeticModel[64];

  public ArithmeticModel[] m_flags = new ArithmeticModel[64];

  public ArithmeticModel[] m_user_data = new ArithmeticModel[64];

  public ArithmeticCodingInteger ic_intensity;

  public ArithmeticCodingInteger ic_scan_angle;

  public ArithmeticCodingInteger ic_point_source_ID;

  // GPS time stuff
  public int last;

  public int next;

  public long[] last_gpstime = new long[4];

  public int[] last_gpstime_diff = new int[4];

  public int[] multi_extreme_counter = new int[4];

  public ArithmeticModel m_gpstime_multi;

  public ArithmeticModel m_gpstime_0diff;

  public ArithmeticCodingInteger ic_gpstime;

  public int classification_flags;

}
