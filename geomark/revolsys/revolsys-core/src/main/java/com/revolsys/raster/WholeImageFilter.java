/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.revolsys.raster;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * A filter which acts as a superclass for filters which need to have the whole image in memory
 * to do their stuff.
 */
public abstract class WholeImageFilter extends AbstractBufferedImageOp
  implements java.io.Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  protected Rectangle originalSpace;

  protected Rectangle transformedSpace;

  /**
   * Construct a WholeImageFilter
   */
  public WholeImageFilter() {
  }

  @Override
  public BufferedImage filter(final BufferedImage src, BufferedImage dst) {
    final int width = src.getWidth();
    final int height = src.getHeight();
    final int type = src.getType();
    final WritableRaster srcRaster = src.getRaster();

    this.originalSpace = new Rectangle(0, 0, width, height);
    this.transformedSpace = new Rectangle(0, 0, width, height);
    transformSpace(this.transformedSpace);

    if (dst == null) {
      final ColorModel dstCM = src.getColorModel();
      dst = new BufferedImage(dstCM, dstCM
        .createCompatibleWritableRaster(this.transformedSpace.width, this.transformedSpace.height),
        dstCM.isAlphaPremultiplied(), null);
    }
    final WritableRaster dstRaster = dst.getRaster();

    int[] inPixels = getRGB(src, 0, 0, width, height, null);
    inPixels = filterPixels(width, height, inPixels, this.transformedSpace);
    setRGB(dst, 0, 0, this.transformedSpace.width, this.transformedSpace.height, inPixels);

    return dst;
  }

  protected abstract int[] filterPixels(int width, int height, int[] inPixels,
    Rectangle transformedSpace);

  protected void transformSpace(final Rectangle rect) {
  }
}
