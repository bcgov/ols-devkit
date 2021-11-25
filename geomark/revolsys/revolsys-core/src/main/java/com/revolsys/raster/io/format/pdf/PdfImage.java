package com.revolsys.raster.io.format.pdf;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.AbstractGeoreferencedImage;
import com.revolsys.spring.resource.Resource;

public class PdfImage extends AbstractGeoreferencedImage {

  public PdfImage(final Resource imageResource) {
    super("pfw");
    setImageResource(imageResource);
    setRenderedImage(newBufferedImage());
    if (!hasGeometryFactory()) {
      loadProjectionFile();
    }
    if (!hasBoundingBox()) {
      loadWorldFile();
    }
  }

  @Override
  public void cancelChanges() {
    if (getImageResource() != null) {
      loadImageMetaData();
      setHasChanges(false);
    }
  }

  protected BufferedImage newBufferedImage() {
    final Resource imageResource = getImageResource();
    try {
      final File file = Resource.getOrDownloadFile(imageResource);
      // TODO password support
      final PDDocument document = PDDocument.load(file);

      final PDPageTree pages = document.getDocumentCatalog().getPages();
      final int pageCount = pages.getCount();
      if (pageCount == 0) {
        throw new RuntimeException("PDF file " + imageResource + " doesn't contain any pages");
      } else {
        if (pageCount > 1) {
          Logs.warn(this, "PDF file " + imageResource + " doesn't contais more than 1 page");
        }
        final PDPage page = pages.get(0);
        final COSDictionary pageDictionary = page.getCOSObject();
        final Rectangle2D mediaBox = PdfUtil.findRectangle(pageDictionary, COSName.MEDIA_BOX);
        final int resolution = 72;
        final PDFRenderer pdfRenderer = new PDFRenderer(document);

        BufferedImage image = pdfRenderer.renderImageWithDPI(0, resolution, ImageType.ARGB);
        final COSDictionary viewport = PdfUtil.getPageViewport(pageDictionary);
        if (viewport != null) {
          final Rectangle2D bbox = PdfUtil.findRectangle(viewport, COSName.BBOX);
          if (bbox != null) {
            final double boxX = bbox.getX();
            final double boxY = bbox.getY();
            final int boxWidth = (int)bbox.getWidth();
            final int boxHeight = (int)bbox.getHeight();
            final BufferedImage viewportImage = new BufferedImage(boxWidth, boxHeight,
              BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = (Graphics2D)viewportImage.getGraphics();
            final double translateY = -(mediaBox.getHeight() - (boxHeight + boxY));
            graphics.translate(-boxX, translateY);
            graphics.scale(1, 1);
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            image = viewportImage;
          }
          final BoundingBox boundingBox = PdfUtil.getViewportBoundingBox(viewport);
          setBoundingBox(boundingBox);
          setResolutionX(boundingBox.getWidth() / image.getWidth());
          setResolutionY(boundingBox.getHeight() / image.getHeight());
        }
        return image;
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error loading PDF file " + imageResource, e);
    }
  }
}
