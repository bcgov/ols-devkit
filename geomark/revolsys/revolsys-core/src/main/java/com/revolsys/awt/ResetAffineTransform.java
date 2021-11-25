package com.revolsys.awt;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.revolsys.io.BaseCloseable;

public class ResetAffineTransform implements BaseCloseable {
  private final AffineTransform originalTransform;

  private final AffineTransform newTransform;

  private final Graphics2D graphics;

  public ResetAffineTransform(final Graphics2D graphics, final AffineTransform originalTransform,
    final AffineTransform newTransform) {

    this.graphics = graphics;
    this.originalTransform = originalTransform;
    this.newTransform = newTransform;
  }

  @Override
  public void close() {
    this.graphics.setTransform(this.originalTransform);
  }

  public ResetAffineTransform reset() {
    this.graphics.setTransform(this.newTransform);
    return this;
  }
}
