package com.revolsys.math.matrix;

/** LU Decomposition.
<P>
For an m-by-n matrix A with m >= n, the LU decomposition is an m-by-n
unit lower triangular matrix L, an n-by-n upper triangular matrix U,
and a permutation vector piv of length m so that A(piv,:) = L*U.
If m < n, then L is m-by-m and U is m-by-n.
<P>
The LU decompostion with pivoting always exists, even if the matrix is
singular, so the constructor will never fail.  The primary use of the
LU decomposition is in the solution of square systems of simultaneous
linear equations.  This will fail if isNonsingular() returns false.
*/

public class LUDecomposition implements java.io.Serializable {

  /*
   * ------------------------ Class variables ------------------------
   */

  private static final long serialVersionUID = 1;

  /** Array for internal storage of decomposition.
  @serial internal array storage.
  */
  private final double[][] LU;

  /** Row and column dimensions, and pivot sign.
  @serial column dimension.
  @serial row dimension.
  @serial pivot sign.
  */
  private final int m, n;

  /** Internal storage of pivot vector.
  @serial pivot vector.
  */
  private final int[] piv;

  /*
   * ------------------------ Constructor ------------------------
   */

  private int pivsign;

  /*
   * ------------------------ Temporary, experimental code.
   * ------------------------ *\ \** LU Decomposition, computed by Gaussian
   * elimination. <P> This constructor computes L and U with the "daxpy"-based
   * elimination algorithm used in LINPACK and MATLAB. In Java, we suspect the
   * dot-product, Crout algorithm will be faster. We have temporarily included
   * this constructor until timing experiments confirm this suspicion. <P>
   * @param A Rectangular matrix
   * @param linpackflag Use Gaussian elimination. Actual value ignored.
   * @return Structure to access L, U and piv.\ public LUDecomposition (Matrix
   * A, int linpackflag) { // Initialize. LU = A.getArrayCopy(); m =
   * A.getRowDimension(); n = A.getColumnDimension(); piv = new int[m]; for (int
   * i = 0; i < m; i++) { piv[i] = i; } pivsign = 1; // Main loop. for (int k =
   * 0; k < n; k++) { // Find pivot. int p = k; for (int i = k+1; i < m; i++) {
   * if (Math.abs(LU[i][k]) > Math.abs(LU[p][k])) { p = i; } } // Exchange if
   * necessary. if (p != k) { for (int j = 0; j < n; j++) { double t = LU[p][j];
   * LU[p][j] = LU[k][j]; LU[k][j] = t; } int t = piv[p]; piv[p] = piv[k];
   * piv[k] = t; pivsign = -pivsign; } // Compute multipliers and eliminate k-th
   * column. if (LU[k][k] != 0.0) { for (int i = k+1; i < m; i++) { LU[i][k] /=
   * LU[k][k]; for (int j = k+1; j < n; j++) { LU[i][j] -= LU[i][k]*LU[k][j]; }
   * } } } } \* ------------------------ End of temporary code.
   * ------------------------
   */

  /*
   * ------------------------ Public Methods ------------------------
   */

  /** LU Decomposition
      Structure to access L, U and piv.
  @param  A Rectangular matrix
  */

  public LUDecomposition(final Matrix A) {

    // Use a "left-looking", dot-product, Crout/Doolittle algorithm.

    this.LU = A.getArrayCopy();
    this.m = A.getRowCount();
    this.n = A.getColumnCount();
    this.piv = new int[this.m];
    for (int i = 0; i < this.m; i++) {
      this.piv[i] = i;
    }
    this.pivsign = 1;
    double[] LUrowi;
    final double[] LUcolj = new double[this.m];

    // Outer loop.

    for (int j = 0; j < this.n; j++) {

      // Make a copy of the j-th column to localize references.

      for (int i = 0; i < this.m; i++) {
        LUcolj[i] = this.LU[i][j];
      }

      // Apply previous transformations.

      for (int i = 0; i < this.m; i++) {
        LUrowi = this.LU[i];

        // Most of the time is spent in the following dot product.

        final int kmax = Math.min(i, j);
        double s = 0.0;
        for (int k = 0; k < kmax; k++) {
          s += LUrowi[k] * LUcolj[k];
        }

        LUrowi[j] = LUcolj[i] -= s;
      }

      // Find pivot and exchange if necessary.

      int p = j;
      for (int i = j + 1; i < this.m; i++) {
        if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
          p = i;
        }
      }
      if (p != j) {
        for (int k = 0; k < this.n; k++) {
          final double t = this.LU[p][k];
          this.LU[p][k] = this.LU[j][k];
          this.LU[j][k] = t;
        }
        final int k = this.piv[p];
        this.piv[p] = this.piv[j];
        this.piv[j] = k;
        this.pivsign = -this.pivsign;
      }

      // Compute multipliers.

      if (j < this.m & this.LU[j][j] != 0.0) {
        for (int i = j + 1; i < this.m; i++) {
          this.LU[i][j] /= this.LU[j][j];
        }
      }
    }
  }

  /** Determinant
  @return     det(A)
  @exception  IllegalArgumentException  Matrix must be square
  */

  public double det() {
    if (this.m != this.n) {
      throw new IllegalArgumentException("Matrix must be square.");
    }
    double d = this.pivsign;
    for (int j = 0; j < this.n; j++) {
      d *= this.LU[j][j];
    }
    return d;
  }

  /** Return pivot permutation vector as a one-dimensional double array
  @return     (double) piv
  */

  public double[] getDoublePivot() {
    final double[] vals = new double[this.m];
    for (int i = 0; i < this.m; i++) {
      vals[i] = this.piv[i];
    }
    return vals;
  }

  /** Return lower triangular factor
  @return     L
  */

  public Matrix getL() {
    final Matrix X = new Matrix(this.m, this.n);
    final double[][] L = X.getArray();
    for (int i = 0; i < this.m; i++) {
      for (int j = 0; j < this.n; j++) {
        if (i > j) {
          L[i][j] = this.LU[i][j];
        } else if (i == j) {
          L[i][j] = 1.0;
        } else {
          L[i][j] = 0.0;
        }
      }
    }
    return X;
  }

  /** Return pivot permutation vector
  @return     piv
  */

  public int[] getPivot() {
    final int[] p = new int[this.m];
    for (int i = 0; i < this.m; i++) {
      p[i] = this.piv[i];
    }
    return p;
  }

  /** Return upper triangular factor
  @return     U
  */

  public Matrix getU() {
    final Matrix X = new Matrix(this.n, this.n);
    final double[][] U = X.getArray();
    for (int i = 0; i < this.n; i++) {
      for (int j = 0; j < this.n; j++) {
        if (i <= j) {
          U[i][j] = this.LU[i][j];
        } else {
          U[i][j] = 0.0;
        }
      }
    }
    return X;
  }

  /** Is the matrix nonsingular?
  @return     true if U, and hence A, is nonsingular.
  */

  public boolean isNonsingular() {
    for (int j = 0; j < this.n; j++) {
      if (this.LU[j][j] == 0) {
        return false;
      }
    }
    return true;
  }

  /** Solve A*X = B
  @param  B   A Matrix with as many rows as A and any number of columns.
  @return     X so that L*U*X = B(piv,:)
  @exception  IllegalArgumentException Matrix row dimensions must agree.
  @exception  RuntimeException  Matrix is singular.
  */

  public Matrix solve(final Matrix B) {
    if (B.getRowCount() != this.m) {
      throw new IllegalArgumentException("Matrix row dimensions must agree.");
    }
    if (!this.isNonsingular()) {
      throw new RuntimeException("Matrix is singular.");
    }

    // Copy right hand side with pivoting
    final int nx = B.getColumnCount();
    final Matrix Xmat = B.getMatrix(this.piv, 0, nx - 1);
    final double[][] X = Xmat.getArray();

    // Solve L*Y = B(piv,:)
    for (int k = 0; k < this.n; k++) {
      for (int i = k + 1; i < this.n; i++) {
        for (int j = 0; j < nx; j++) {
          X[i][j] -= X[k][j] * this.LU[i][k];
        }
      }
    }
    // Solve U*X = Y;
    for (int k = this.n - 1; k >= 0; k--) {
      for (int j = 0; j < nx; j++) {
        X[k][j] /= this.LU[k][k];
      }
      for (int i = 0; i < k; i++) {
        for (int j = 0; j < nx; j++) {
          X[i][j] -= X[k][j] * this.LU[i][k];
        }
      }
    }
    return Xmat;
  }
}
