package com.revolsys.math.matrix;

/** Cholesky Decomposition.
<P>
For a symmetric, positive definite matrix A, the Cholesky decomposition
is an lower triangular matrix L so that A = L*L'.
<P>
If the matrix is not symmetric or positive definite, the constructor
returns a partial decomposition and sets an internal flag that may
be queried by the isSPD() method.
*/

public class CholeskyDecomposition implements java.io.Serializable {

  /*
   * ------------------------ Class variables ------------------------
   */

  private static final long serialVersionUID = 1;

  /** Symmetric and positive definite flag.
  @serial is symmetric and positive definite flag.
  */
  private boolean isspd;

  /** Array for internal storage of decomposition.
  @serial internal array storage.
  */
  private final double[][] L;

  /*
   * ------------------------ Constructor ------------------------
   */

  /** Row and column dimension (square matrix).
  @serial matrix dimension.
  */
  private final int n;

  /*
   * ------------------------ Temporary, experimental code.
   * ------------------------ *\ \** Right Triangular Cholesky Decomposition.
   * <P> For a symmetric, positive definite matrix A, the Right Cholesky
   * decomposition is an upper triangular matrix R so that A = R'*R. This
   * constructor computes R with the Fortran inspired column oriented algorithm
   * used in LINPACK and MATLAB. In Java, we suspect a row oriented, lower
   * triangular decomposition is faster. We have temporarily included this
   * constructor here until timing experiments confirm this suspicion.\ \**
   * Array for internal storage of right triangular decomposition. **\ private
   * transient double[][] R; \** Cholesky algorithm for symmetric and positive
   * definite matrix.
   * @param A Square, symmetric matrix.
   * @param rightflag Actual value ignored.
   * @return Structure to access R and isspd flag.\ public CholeskyDecomposition
   * (Matrix Arg, int rightflag) { // Initialize. double[][] A = Arg.getArray();
   * n = Arg.getColumnDimension(); R = new double[n][n]; isspd =
   * (Arg.getColumnDimension() == n); // Main loop. for (int j = 0; j < n; j++)
   * { double d = 0.0; for (int k = 0; k < j; k++) { double s = A[k][j]; for
   * (int i = 0; i < k; i++) { s = s - R[i][k]*R[i][j]; } R[k][j] = s =
   * s/R[k][k]; d = d + s*s; isspd = isspd & (A[k][j] == A[j][k]); } d = A[j][j]
   * - d; isspd = isspd & (d > 0.0); R[j][j] = Math.sqrt(Math.max(d,0.0)); for
   * (int k = j+1; k < n; k++) { R[k][j] = 0.0; } } } \** Return upper
   * triangular factor.
   * @return R\ public Matrix getR () { return new Matrix(R,n,n); } \*
   * ------------------------ End of temporary code. ------------------------
   */

  /*
   * ------------------------ Public Methods ------------------------
   */

  /** Cholesky algorithm for symmetric and positive definite matrix.
      Structure to access L and isspd flag.
  @param  Arg   Square, symmetric matrix.
  */

  public CholeskyDecomposition(final Matrix Arg) {

    // Initialize.
    final double[][] A = Arg.getArray();
    this.n = Arg.getRowCount();
    this.L = new double[this.n][this.n];
    this.isspd = Arg.getColumnCount() == this.n;
    // Main loop.
    for (int j = 0; j < this.n; j++) {
      final double[] Lrowj = this.L[j];
      double d = 0.0;
      for (int k = 0; k < j; k++) {
        final double[] Lrowk = this.L[k];
        double s = 0.0;
        for (int i = 0; i < k; i++) {
          s += Lrowk[i] * Lrowj[i];
        }
        Lrowj[k] = s = (A[j][k] - s) / this.L[k][k];
        d = d + s * s;
        this.isspd = this.isspd & A[k][j] == A[j][k];
      }
      d = A[j][j] - d;
      this.isspd = this.isspd & d > 0.0;
      this.L[j][j] = Math.sqrt(Math.max(d, 0.0));
      for (int k = j + 1; k < this.n; k++) {
        this.L[j][k] = 0.0;
      }
    }
  }

  /** Return triangular factor.
  @return     L
  */

  public Matrix getL() {
    return new Matrix(this.L, this.n, this.n);
  }

  /** Is the matrix symmetric and positive definite?
  @return     true if A is symmetric and positive definite.
  */

  public boolean isSPD() {
    return this.isspd;
  }

  /** Solve A*X = B
   @param  B   A Matrix with as many rows as A and any number of columns.
   @return     X so that L*L'*X = B
   @exception  IllegalArgumentException  Matrix row dimensions must agree.
   @exception  RuntimeException  Matrix is not symmetric positive definite.
   */

  public Matrix solve(final Matrix B) {
    if (B.getRowCount() != this.n) {
      throw new IllegalArgumentException("Matrix row dimensions must agree.");
    }
    if (!this.isspd) {
      throw new RuntimeException("Matrix is not symmetric positive definite.");
    }

    // Copy right hand side.
    final double[][] X = B.getArrayCopy();
    final int nx = B.getColumnCount();

    // Solve L*Y = B;
    for (int k = 0; k < this.n; k++) {
      for (int j = 0; j < nx; j++) {
        for (int i = 0; i < k; i++) {
          X[k][j] -= X[i][j] * this.L[k][i];
        }
        X[k][j] /= this.L[k][k];
      }
    }

    // Solve L'*X = Y;
    for (int k = this.n - 1; k >= 0; k--) {
      for (int j = 0; j < nx; j++) {
        for (int i = k + 1; i < this.n; i++) {
          X[k][j] -= X[i][j] * this.L[i][k];
        }
        X[k][j] /= this.L[k][k];
      }
    }

    return new Matrix(X, this.n, nx);
  }

}
