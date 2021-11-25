package com.revolsys.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.measure.Quantity;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.FunctionDataType;
import org.jeometry.common.io.FileProxy;

import com.revolsys.io.FileUtil;

public class RsCoreDataTypes {

  public static final DataType FILE = new FunctionDataType("File", File.class, value -> {
    if (value == null) {
      return null;
    } else {
      File file = null;
      if (value instanceof File) {
        file = (File)value;
      } else if (value instanceof URL) {
        return FileUtil.getFile((URL)value);
      } else if (value instanceof URI) {
        return FileUtil.getFile((URI)value);
      } else if (value instanceof FileProxy) {
        final FileProxy proxy = (FileProxy)value;
        file = proxy.getOrDownloadFile();
      } else {
        // final String string = DataTypes.toString(value);
        // return getFile(string);
        file = null;
      }
      if (file == null) {
        return file;
      } else {
        try {
          return file.getCanonicalFile();
        } catch (final IOException e) {
          return file.getAbsoluteFile();
        }
      }
    }
  });

  public static final DataType MEASURE = new FunctionDataType("measure", Quantity.class,
    QuantityType::newQuantity, QuantityType::toString);

}
