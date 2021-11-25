package com.revolsys.record.io.format.kml;

import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;

public class KmlMapWriter extends AbstractMapWriter {

  /** The writer */
  private KmlXmlWriter out;

  public KmlMapWriter(final Writer out) {
    this.out = new KmlXmlWriter(out);

    writeHeader();
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        this.out.endTag();
        this.out.endTag();
        this.out.endDocument();
      } finally {
        FileUtil.closeSilent(this.out);
        this.out = null;
      }
    }
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    this.out.startTag(Kml22Constants.PLACEMARK);
    Geometry multiGeometry = null;
    this.out.startTag(Kml22Constants.EXTENDED_DATA);
    for (final Entry<String, ? extends Object> field : values.entrySet()) {
      final String key = field.getKey();
      final Object value = field.getValue();
      if (isWritable(value)) {
        if (value instanceof Geometry) {
          final Geometry geometry = (Geometry)value;
          if (multiGeometry == null) {
            multiGeometry = geometry;
          } else {
            multiGeometry = multiGeometry.union(geometry);
          }
        } else {
          this.out.writeData(key, value);
        }
      }
    }

    this.out.endTag();
    this.out.writeGeometry(multiGeometry, 2);
    this.out.endTag();
  }

  private void writeHeader() {
    this.out.startDocument("UTF-8", "1.0");
    this.out.startTag(Kml22Constants.KML);
    this.out.startTag(Kml22Constants.DOCUMENT);
  }
}
