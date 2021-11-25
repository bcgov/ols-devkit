package com.revolsys.raster.io.format.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.XmpSerializer;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.AbstractWriter;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageWriter;
import com.revolsys.spring.resource.Resource;

public class PdfImageWriter extends AbstractWriter<GeoreferencedImage>
  implements GeoreferencedImageWriter {

  private final Resource resource;

  public PdfImageWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void write(final GeoreferencedImage image) {
    try {
      final PDDocument document = new PDDocument();

      BoundingBox boundingBox = image.getBoundingBox();
      final int width = image.getImageWidth();
      final int height = image.getImageHeight();

      final int srid = boundingBox.getHorizontalCoordinateSystemId();
      if (srid == 3857) {
        boundingBox = boundingBox.bboxEdit(editor -> editor
          .setGeometryFactory(image.getGeometryFactory().getGeographicGeometryFactory()));
      }
      final PDRectangle pageSize = new PDRectangle(width, height);
      final PDPage page = new PDPage(pageSize);

      final BufferedImage bufferedImage = image.getBufferedImage();
      final PDImageXObject pdfImage = LosslessFactory.createFromImage(document, bufferedImage);
      try (
        final PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
        contentStream.drawImage(pdfImage, 0f, 0f);
      }
      document.addPage(page);

      final PDDocumentCatalog catalog = document.getDocumentCatalog();
      final PDMetadata metadata = new PDMetadata(document);
      catalog.setMetadata(metadata);

      // jempbox version
      final XMPMetadata xmpMetadata = XMPMetadata.createXMPMetadata();
      final DublinCoreSchema dcSchema = xmpMetadata.createAndAddDublinCoreSchema();

      dcSchema.setAboutAsSimple("");

      final XmpSerializer serializer = new XmpSerializer();
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      serializer.serialize(xmpMetadata, baos, false);
      metadata.importXMPMetadata(baos.toByteArray());

      try (
        OutputStream out = this.resource.newBufferedOutputStream()) {
        document.save(out);
      }
    } catch (final Throwable e) {
      throw Exceptions.wrap("Unable to create PDF " + this.resource, e);
    }

  }
}
