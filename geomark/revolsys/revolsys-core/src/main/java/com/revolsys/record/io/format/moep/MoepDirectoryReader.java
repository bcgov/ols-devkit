package com.revolsys.record.io.format.moep;

import java.io.File;
import java.io.IOException;
import java.sql.Date;

import org.jeometry.common.date.Dates;

import com.revolsys.io.FileUtil;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordDirectoryReader;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

public class MoepDirectoryReader extends RecordDirectoryReader implements RecordDefinitionFactory {

  private Date integrationDate;

  private String revisionKey;

  private String specificationsRelease;

  private Date submissionDate;

  public MoepDirectoryReader() {
    setFileExtensions("bin");
  }

  public MoepDirectoryReader(final File directory) throws IOException {
    setFileExtensions("bin");
    setDirectory(directory);
  }

  public Date getIntegrationDate() {
    return this.integrationDate;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    if (typePath.equals(MoepConstants.TYPE_NAME)) {
      return MoepConstants.RECORD_DEFINITION;
    } else {
      return null;
    }
  }

  public String getRevisionKey() {
    return this.revisionKey;
  }

  public String getSpecificationsRelease() {
    return this.specificationsRelease;
  }

  public Date getSubmissionDate() {
    return this.submissionDate;
  }

  /**
   * Construct a new new {@link MoepBinaryReader} to read the file.
   *
   * @param file The file to read.
   * @return The reader for the file.
   * @throws IOException If an I/O error occurs.
   */
  @Override
  protected RecordReader newReader(final Resource resource) {
    return new MoepBinaryReader(this, resource, ArrayRecord.FACTORY);
  }

  @Override
  public void setDirectory(final File directory) {
    super.setDirectory(directory);
    final String name = FileUtil.getFileName(directory);
    final File file = new File(directory, name + "s.bin");
    final RecordReader supDataReader = newReader(new PathResource(file));
    for (final Record supData : supDataReader) {
      final String featureCode = supData.getValue(MoepConstants.FEATURE_CODE);
      if (featureCode.equals("KN00020000")) {
        final String text = supData.getValue(MoepConstants.TEXT);
        final String[] versionFields = text.split(" ");

        final String dateString = versionFields[2];
        this.submissionDate = new Date(Dates.getDate("yyyyMMdd", dateString).getTime());
        this.revisionKey = versionFields[3];
        this.integrationDate = new Date(Dates.getDate("yyyyMMdd", versionFields[4]).getTime());
        this.specificationsRelease = versionFields[5];
      }
    }

  }
}
