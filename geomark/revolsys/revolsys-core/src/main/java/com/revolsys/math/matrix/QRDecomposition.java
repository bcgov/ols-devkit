package com.revolsys.math.matrix;

import org.jeometry.common.number.Doubles;

/** QR Decomposition.
<P>
   For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
   orthogonal matrix Q and an n-by-n upper triangular matrix R so that
   A = Q*R.
<P>
   The QR decompostion always exists, even if the matrix does not have
   full rank, so the constructor will never fail.  The primary use of the
   QR decomposition is in the least squares solution of nonsquare systems
   of simultaneous linear equations.  This will fail if isFullRank()
   returns false.
*/

public class QRDecomposition implements java.io.Serializable {

  /*
   * ------------------------ Class variables ------------------------
   */

  private static final long serialVersionUID = 1;

  /** Row and column dimensions.
  @serial column dimension.
  @serial row dimension.
  */
  private final int m, n;

  /** Array for internal storage of decomposition.
  @serial internal array storage.
  */
  private final double[][] QR;

  /*
   * ------------------------ Constructor ------------------------
   */

  /** Array for internal storage of diagonal of R.
  @serial diagonal of R.
  */
  private final double[] Rdiag;

  /*
   * ------------------------ Public Methods ------------------------
   */

  /** QR Decomposition, computed by Householder reflections.
      Structure to access R and the Householder vectors and compute Q.
  @param A    Rectangular matrix
  */

  public QRDecomposition(final Matrix A) {
    // Initialize.
    this.QR = A.getArrayCopy();
    this.m = A.getRowCount();
    this.n = A.getColumnCount();
    this.Rdiag = new double[this.n];

    // Main loop.
    for (int k = 0; k < this.n; k++) {
      // Compute 2-norm of k-th column without under/overflow.
      double nrm = 0;
      for (int i = k; i < this.m; i++) {
        nrm = Doubles.hypot(nrm, this.QR[i][k]);
      }

      if (nrm != 0.0) {
        // Form k-th Householder vector.
        if (this.QR[k][k] < 0) {
          nrm = -nrm;
        }
        for (int i = k; i < this.m; i++) {
          this.QR[i][k] /= nrm;
        }
        this.QR[k][k] += 1.0;

        // Apply transformation to remaining columns.
        for (int j = k + 1; j < this.n; j++) {
          double s = 0.0;
          for (int i = k; i < this.m; i++) {
            s += this.QR[i][k] * this.QR[i][j];
          }
          s = -s / this.QR[k][k];
          for (int i = k; i < this.m; i++) {
            this.QR[i][j] += s * this.QR[i][k];
          }
        }
      }
      this.Rdiag[k] = -nrm;
    }
  }

  /** Return the Householder vectors
  @return     Lower trapezoidal matrix whose columns define the reflections
  */

  public Matrix getH() {
    final Matrix X = new Matrix(this.m, this.n);
    final double[][] H = X.getArray();
    for (int i = 0; i < this.m; i++) {
      for (int j = 0; j < this.n; j++) {
        if (i >= j) {
          H[i][j] = this.QR[i][j];
        } else {
          H[i][j] = 0.0;
        }
      }
    }
    return X;
  }

  /** Generate and return the (economy-sized) orthogonal factor
  @return     Q
  */

  public Matrix getQ() {
    final Matrix X = new Matrix(this.m, this.n);
    final double[][] Q = X.getArray();
    for (int k = this.n - 1; k >= 0; k--) {
      for (int i = 0; i < this.m; i++) {
        Q[i][k] = 0.0;
      }
      Q[k][k] = 1.0;
      for (int j = k; j < this.n; j++) {
        if (this.QR[k][k] != 0) {
          double s = 0.0;
          for (int i = k; i < this.m; i++) {
            s += this.QR[i][k] * Q[i][j];
          }
          s = -s / this.QR[k][k];
          for (int i = k; i < this.m; i++) {
            Q[i][j] += s * this.QR[i][k];
          }
        }
      }
    }
    return X;
  }

  /** Return the upper triangular factor
  @return     R
  */

  public Matrix getR() {
    final Matrix X = new Matrix(this.n, this.n);
    final double[][] R = X.getArray();
    for (int i = 0; i < this.n; i++) {
      for (int j = 0; j < this.n; j++) {
        if (i < j) {
          R[i][j] = this.QR[i][j];
        } else if (i == j) {
          R[i][j] = this.Rdiag[i];
        } else {
          R[i][j] = 0.0;
        }
      }
    }
    return X;
  }

  /** Is the matrix full rank?
  @return     true if R, and hence A, has full rank.
  */

  public boolean isFullRank() {
    for (int j = 0; j < this.n; j++) {
      if (this.Rdiag[j] == 0) {
        return false;
      }
    }
    return true;
  }

  /** Least squares solution of A*X = B
   @param B    A Matrix with as many rows as A and any number of columns.
   @return     X that minimizes the two norm of Q*R*X-B.
   @exception  IllegalArgumentException  Matrix row dimensions must agree.
   @exception  RuntimeException  Matrix is rank deficient.
   */

  public Matrix solve(final Matrix B) {
    if (B.getRowCount() != this.m) {
      throw new IllegalArgumentException("Matrix row dimensions must agree.");
    }
    if (!this.isFullRank()) {
      throw new RuntimeException("Matrix is rank deficient.");
    }

    // Copy right hand side
    final int nx = B.getColumnCount();
    final double[][] X = B.getArrayCopy();

    // Compute Y = transpose(Q)*B
    for (int k = 0; k < this.n; k++) {
      for (int j = 0; j < nx; j++) {
        double s = 0.0;
        for (int i = k; i < this.m; i++) {
          s += this.QR[i][k] * X[i][j];
        }
        s = -s / this.QR[k][k];
        for (int i = k; i < this.m; i++) {
          X[i][j] += s * this.QR[i][k];
        }
      }
    }
    // Solve R*X = Y;
    for (int k = this.n - 1; k >= 0; k--) {
      for (int j = 0; j < nx; j++) {
        X[k][j] /= this.Rdiag[k];
      }
      for (int i = 0; i < k; i++) {
        for (int j = 0; j < nx; j++) {
          X[i][j] -= X[k][j] * this.QR[i][k];
        }
      }
    }
    return new Matrix(X, this.n, nx).getMatrix(0, this.n - 1, 0, nx - 1);
  }
}
