package com.revolsys.math.matrix;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;

import org.jeometry.common.number.Doubles;

/**
   Jama = Java Matrix class.
<P>
   The Java Matrix Class provides the fundamental operations of numerical
   linear algebra.  Various constructors create Matrices from two dimensional
   arrays of double precision floating point numbers.  Various "gets" and
   "sets" provide access to submatrices and matrix elements.  Several methods
   implement basic matrix arithmetic, including matrix addition and
   multiplication, matrix norms, and element-by-element array operations.
   Methods for reading and printing matrices are also included.  All the
   operations in this version of the Matrix Class involve real matrices.
   Complex matrices may be handled in a future version.
<P>
   Five fundamental matrix decompositions, which consist of pairs or triples
   of matrices, permutation vectors, and the like, produce results in five
   decomposition classes.  These decompositions are accessed by the Matrix
   class to compute solutions of simultaneous linear equations, determinants,
   inverses and other matrix functions.  The five decompositions are:
<P><UL>
   <LI>Cholesky Decomposition of symmetric, positive definite matrices.
   <LI>LU Decomposition of rectangular matrices.
   <LI>QR Decomposition of rectangular matrices.
   <LI>Singular Value Decomposition of rectangular matrices.
   <LI>Eigenvalue Decomposition of both symmetric and nonsymmetric square matrices.
</UL>
<DL>
<DT><B>Example of use:</B></DT>
<P>
<DD>Solve a linear system A x = b and compute the residual norm, ||b - A x||.
<P><PRE>
      double[][] vals = {{1.,2.,3},{4.,5.,6.},{7.,8.,10.}};
      Matrix A = new Matrix(vals);
      Matrix b = Matrix.random(3,1);
      Matrix x = A.solve(b);
      Matrix r = A.times(x).minus(b);
      double rnorm = r.normInf();
</PRE></DD>
</DL>

@author The <a href="http://math.nist.gov/javanumerics/jama/">MathWorks, Inc. and the National Institute of Standards and Technology.</a>
@version 5 August 1998
*/

public class Matrix implements Cloneable, java.io.Serializable {

  /*
   * ------------------------ Class variables ------------------------
   */

  private static final long serialVersionUID = 1;

  /** Construct a matrix from a copy of a 2-D array.
  @param A    Two-dimensional array of doubles.
  @exception  IllegalArgumentException All rows must have the same length
  */

  public static Matrix constructWithCopy(final double[][] A) {
    final int m = A.length;
    final int n = A[0].length;
    final Matrix X = new Matrix(m, n);
    final double[][] C = X.getArray();
    for (int i = 0; i < m; i++) {
      if (A[i].length != n) {
        throw new IllegalArgumentException("All rows must have the same length.");
      }
      for (int j = 0; j < n; j++) {
        C[i][j] = A[i][j];
      }
    }
    return X;
  }

  /*
   * ------------------------ Constructors ------------------------
   */

  /** Generate identity matrix
  @param m    Number of rows.
  @param n    Number of colums.
  @return     An m-by-n matrix with ones on the diagonal and zeros elsewhere.
  */

  public static Matrix identity(final int m, final int n) {
    final Matrix A = new Matrix(m, n);
    final double[][] X = A.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        X[i][j] = i == j ? 1.0 : 0.0;
      }
    }
    return A;
  }

  /**
   * Solves a set of linear equations.  The input parameters "matrix1",
   * and "row_perm" come from luDecompostion and do not change
   * here.  The parameter "matrix2" is a set of column vectors assembled
   * into a nxn matrix of floating-point values.  The procedure takes each
   * column of "matrix2" in turn and treats it as the right-hand side of the
   * matrix equation Ax = LUx = b.  The solution vector replaces the
   * original column of the matrix.
   *
   * If "matrix2" is the identity matrix, the procedure replaces its contents
   * with the inverse of the matrix from which "matrix1" was originally
   * derived.
   */
  //
  // Reference: Press, Flannery, Teukolsky, Vetterling,
  // _Numerical_Recipes_in_C_, Cambridge University Press,
  // 1988, pp 44-45.
  //
  static void luBacksubstitution(final int dim, final double[] matrix1, final int[] row_perm,
    final double[] matrix2) {

    int i, ii, ip, j, k;
    int rp;
    int cv, rv, ri;
    double tt;

    // rp = row_perm;
    rp = 0;

    // For each column vector of matrix2 ...
    for (k = 0; k < dim; k++) {
      // cv = &(matrix2[0][k]);
      cv = k;
      ii = -1;

      // Forward substitution
      for (i = 0; i < dim; i++) {
        double sum;

        ip = row_perm[rp + i];
        sum = matrix2[cv + dim * ip];
        matrix2[cv + dim * ip] = matrix2[cv + dim * i];
        if (ii >= 0) {
          // rv = &(matrix1[i][0]);
          rv = i * dim;
          for (j = ii; j <= i - 1; j++) {
            sum -= matrix1[rv + j] * matrix2[cv + dim * j];
          }
        } else if (sum != 0.0) {
          ii = i;
        }
        matrix2[cv + dim * i] = sum;
      }

      // Backsubstitution
      for (i = 0; i < dim; i++) {
        ri = dim - 1 - i;
        rv = dim * ri;
        tt = 0.0;
        for (j = 1; j <= i; j++) {
          tt += matrix1[rv + dim - j] * matrix2[cv + dim * (dim - j)];
        }
        matrix2[cv + dim * ri] = (matrix2[cv + dim * ri] - tt) / matrix1[rv + ri];
      }
    }
  }

  /**
   * Given a nxn array "matrix0", this function replaces it with the
   * LU decomposition of a row-wise permutation of itself.  The input
   * parameters are "matrix0" and "dim".  The array "matrix0" is also
   * an output parameter.  The vector "row_perm[]" is an output
   * parameter that contains the row permutations resulting from partial
   * pivoting.  The output parameter "even_row_xchg" is 1 when the
   * number of row exchanges is even, or -1 otherwise.  Assumes data
   * type is always double.
   *
   * @return true if the matrix is nonsingular, or false otherwise.
   */
  //
  // Reference: Press, Flannery, Teukolsky, Vetterling,
  // _Numerical_Recipes_in_C_, Cambridge University Press,
  // 1988, pp 40-45.
  //
  static boolean luDecomposition(final int dim, final double[] matrix0, final int[] row_perm,
    final int[] even_row_xchg) {

    final double row_scale[] = new double[dim];

    // Determine implicit scaling information by looping over rows
    int i, j;
    int ptr, rs, mtx;
    double big, temp;

    ptr = 0;
    rs = 0;
    even_row_xchg[0] = 1;

    // For each row ...
    i = dim;
    while (i-- != 0) {
      big = 0.0;

      // For each column, find the largest element in the row
      j = dim;
      while (j-- != 0) {
        temp = matrix0[ptr++];
        temp = Math.abs(temp);
        if (temp > big) {
          big = temp;
        }
      }

      // Is the matrix singular?
      if (big == 0.0) {
        return false;
      }
      row_scale[rs++] = 1.0 / big;
    }

    // For all columns, execute Crout's method
    mtx = 0;
    for (j = 0; j < dim; j++) {
      int imax, k;
      int target, p1, p2;
      double sum;

      // Determine elements of upper diagonal matrix U
      for (i = 0; i < j; i++) {
        target = mtx + dim * i + j;
        sum = matrix0[target];
        k = i;
        p1 = mtx + dim * i;
        p2 = mtx + j;
        while (k-- != 0) {
          sum -= matrix0[p1] * matrix0[p2];
          p1++;
          p2 += dim;
        }
        matrix0[target] = sum;
      }

      // Search for largest pivot element and calculate
      // intermediate elements of lower diagonal matrix L.
      big = 0.0;
      imax = -1;
      for (i = j; i < dim; i++) {
        target = mtx + dim * i + j;
        sum = matrix0[target];
        k = j;
        p1 = mtx + dim * i;
        p2 = mtx + j;
        while (k-- != 0) {
          sum -= matrix0[p1] * matrix0[p2];
          p1++;
          p2 += dim;
        }
        matrix0[target] = sum;

        // Is this the best pivot so far?
        if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
          big = temp;
          imax = i;
        }
      }

      if (imax < 0) {
        throw new RuntimeException("???");
      }

      // Is a row exchange necessary?
      if (j != imax) {
        // Yes: exchange rows
        k = dim;
        p1 = mtx + dim * imax;
        p2 = mtx + dim * j;
        while (k-- != 0) {
          temp = matrix0[p1];
          matrix0[p1++] = matrix0[p2];
          matrix0[p2++] = temp;
        }

        // Record change in scale factor
        row_scale[imax] = row_scale[j];
        even_row_xchg[0] = -even_row_xchg[0]; // change exchange parity
      }

      // Record row permutation
      row_perm[j] = imax;

      // Is the matrix singular
      if (matrix0[mtx + dim * j + j] == 0.0) {
        return false;
      }

      // Divide elements of lower diagonal matrix L by pivot
      if (j != dim - 1) {
        temp = 1.0 / matrix0[mtx + dim * j + j];
        target = mtx + dim * (j + 1) + j;
        i = dim - 1 - j;
        while (i-- != 0) {
          matrix0[target] *= temp;
          target += dim;
        }
      }

    }

    return true;
  }

  /** Generate matrix with random elements
  @param m    Number of rows.
  @param n    Number of colums.
  @return     An m-by-n matrix with uniformly distributed random elements.
  */

  public static Matrix random(final int m, final int n) {
    final Matrix A = new Matrix(m, n);
    final double[][] X = A.getArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        X[i][j] = Math.random();
      }
    }
    return A;
  }

  /** Read a matrix from a stream.  The format is the same the print method,
    * so printed matrices can be read back in (provided they were printed using
    * US Locale).  Elements are separated by
    * whitespace, all the elements for each row appear on a single line,
    * the last row is followed by a blank line.
  @param input the input stream.
  */

  public static Matrix read(final BufferedReader input) throws java.io.IOException {
    final StreamTokenizer tokenizer = new StreamTokenizer(input);

    // Although StreamTokenizer will parse numbers, it doesn't recognize
    // scientific notation (E or D); however, Double.valueOf does.
    // The strategy here is to disable StreamTokenizer's number parsing.
    // We'll only get whitespace delimited words, EOL's and EOF's.
    // These words should all be numbers, for Double.valueOf to parse.

    tokenizer.resetSyntax();
    tokenizer.wordChars(0, 255);
    tokenizer.whitespaceChars(0, ' ');
    tokenizer.eolIsSignificant(true);
    final java.util.Vector<Double> vD = new java.util.Vector<>();

    // Ignore initial empty lines
    while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
      ;
    }
    if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
      throw new java.io.IOException("Unexpected EOF on matrix read.");
    }
    do {
      vD.addElement(Double.valueOf(tokenizer.sval)); // Read & store 1st row.
    } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);

    final int n = vD.size(); // Now we've got the number of columns!
    double row[] = new double[n];
    for (int j = 0; j < n; j++) {
      row[j] = vD.elementAt(j).doubleValue();
    }
    final java.util.Vector<double[]> v = new java.util.Vector<>();
    v.addElement(row); // Start storing rows instead of columns.
    while (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
      // While non-empty lines
      v.addElement(row = new double[n]);
      int j = 0;
      do {
        if (j >= n) {
          throw new java.io.IOException("Row " + v.size() + " is too long.");
        }
        row[j++] = Double.valueOf(tokenizer.sval).doubleValue();
      } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);
      if (j < n) {
        throw new java.io.IOException("Row " + v.size() + " is too short.");
      }
    }
    final int m = v.size(); // Now we've got the number of rows.
    final double[][] A = new double[m][];
    v.copyInto(A); // copy the rows out of the vector
    return new Matrix(A);
  }

  static String toString(final Matrix matrix) {
    final int numRow = matrix.getRowCount();
    final int numCol = matrix.getColumnCount();
    StringBuffer buffer = new StringBuffer();
    final String lineSeparator = "\n";
    final FieldPosition dummy = new FieldPosition(0);
    final NumberFormat format = NumberFormat.getNumberInstance();
    format.setGroupingUsed(false);
    format.setMinimumFractionDigits(6);
    format.setMaximumFractionDigits(6);
    for (int j = 0; j < numRow; ++j) {
      for (int i = 0; i < numCol; ++i) {
        final int position = buffer.length();
        buffer = format.format(matrix.get(j, i), buffer, dummy);
        buffer.insert(position, " ");
      }
      buffer.append(lineSeparator);
    }
    return buffer.toString();
  }

  /** column dimension.
  @serial column dimension.
  */
  private final int columnCount;

  /*
   * ------------------------ Public Methods ------------------------
   */

  /** Row dimension.
   @serial row dimension.
   */
  private final int rowCount;

  /** Array for internal storage of elements.
  @serial internal array storage.
  */
  private final double[][] values;

  /** Construct a matrix from a one-dimensional packed array
  @param vals One-dimensional array of doubles, packed by columns (ala Fortran).
  @param rowCount    Number of rows.
  @exception  IllegalArgumentException Array length must be a multiple of m.
  */

  public Matrix(final double vals[], final int rowCount) {
    this.rowCount = rowCount;
    this.columnCount = rowCount != 0 ? vals.length / rowCount : 0;
    if (rowCount * this.columnCount != vals.length) {
      throw new IllegalArgumentException("Array length must be a multiple of m.");
    }
    this.values = new double[rowCount][this.columnCount];
    for (int i = 0; i < rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        this.values[i][j] = vals[i + j * rowCount];
      }
    }
  }

  /** Construct a matrix from a 2-D array.
  @param A    Two-dimensional array of doubles.
  @exception  IllegalArgumentException All rows must have the same length
  @see        #constructWithCopy
  */

  public Matrix(final double[][] A) {
    this.rowCount = A.length;
    this.columnCount = A[0].length;
    for (int i = 0; i < this.rowCount; i++) {
      if (A[i].length != this.columnCount) {
        throw new IllegalArgumentException("All rows must have the same length.");
      }
    }
    this.values = A;
  }

  /** Construct a matrix quickly without checking arguments.
  @param A    Two-dimensional array of doubles.
  @param rowCount    Number of rows.
  @param columnCount    Number of colums.
  */

  public Matrix(final double[][] A, final int rowCount, final int columnCount) {
    this.values = A;
    this.rowCount = rowCount;
    this.columnCount = columnCount;
  }

  /** Construct an m-by-n matrix of zeros.
  @param m    Number of rows.
  @param n    Number of colums.
  */

  public Matrix(final int m, final int n) {
    this.rowCount = m;
    this.columnCount = n;
    this.values = new double[m][n];
  }

  /** Construct an m-by-n constant matrix.
  @param m    Number of rows.
  @param n    Number of colums.
  @param s    Fill the matrix with this scalar value.
  */

  public Matrix(final int m, final int n, final double s) {
    this.rowCount = m;
    this.columnCount = n;
    this.values = new double[m][n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        this.values[i][j] = s;
      }
    }
  }

  /** Element-by-element left division, C = A.\B
  @param B    another matrix
  @return     A.\B
  */

  public Matrix arrayLeftDivide(final Matrix B) {
    checkMatrixDimensions(B);
    final Matrix X = new Matrix(this.rowCount, this.columnCount);
    final double[][] C = X.getArray();
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        C[i][j] = B.values[i][j] / this.values[i][j];
      }
    }
    return X;
  }

  /** Element-by-element left division in place, A = A.\B
  @param B    another matrix
  @return     A.\B
  */

  public Matrix arrayLeftDivideEquals(final Matrix B) {
    checkMatrixDimensions(B);
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        this.values[i][j] = B.values[i][j] / this.values[i][j];
      }
    }
    return this;
  }

  /** Element-by-element right division, C = A./B
  @param B    another matrix
  @return     A./B
  */

  public Matrix arrayRightDivide(final Matrix B) {
    checkMatrixDimensions(B);
    final Matrix X = new Matrix(this.rowCount, this.columnCount);
    final double[][] C = X.getArray();
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        C[i][j] = this.values[i][j] / B.values[i][j];
      }
    }
    return X;
  }

  /** Element-by-element right division in place, A = A./B
  @param B    another matrix
  @return     A./B
  */

  public Matrix arrayRightDivideEquals(final Matrix B) {
    checkMatrixDimensions(B);
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        this.values[i][j] = this.values[i][j] / B.values[i][j];
      }
    }
    return this;
  }

  /** Element-by-element multiplication, C = A.*B
  @param B    another matrix
  @return     A.*B
  */

  public Matrix arrayTimes(final Matrix B) {
    checkMatrixDimensions(B);
    final Matrix X = new Matrix(this.rowCount, this.columnCount);
    final double[][] C = X.getArray();
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        C[i][j] = this.values[i][j] * B.values[i][j];
      }
    }
    return X;
  }

  /** Element-by-element multiplication in place, A = A.*B
  @param B    another matrix
  @return     A.*B
  */

  public Matrix arrayTimesEquals(final Matrix B) {
    checkMatrixDimensions(B);
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        this.values[i][j] = this.values[i][j] * B.values[i][j];
      }
    }
    return this;
  }

  /** Check if size(A) == size(B) **/

  private void checkMatrixDimensions(final Matrix B) {
    if (B.rowCount != this.rowCount || B.columnCount != this.columnCount) {
      throw new IllegalArgumentException("Matrix dimensions must agree.");
    }
  }

  /** Cholesky Decomposition
  @return     CholeskyDecomposition
  @see CholeskyDecomposition
  */

  public CholeskyDecomposition chol() {
    return new CholeskyDecomposition(this);
  }

  /** Clone the Matrix object.
  */

  @Override
  public Object clone() {
    return this.copy();
  }

  /** Matrix condition (2 norm)
  @return     ratio of largest to smallest singular value.
  */

  public double cond() {
    return new SingularValueDecomposition(this).cond();
  }

  /** Make a deep copy of a matrix
  */

  public Matrix copy() {
    final Matrix X = new Matrix(this.rowCount, this.columnCount);
    final double[][] C = X.getArray();
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        C[i][j] = this.values[i][j];
      }
    }
    return X;
  }

  /** Matrix determinant
  @return     determinant
  */

  public double det() {
    return new LUDecomposition(this).det();
  }

  /** Eigenvalue Decomposition
  @return     EigenvalueDecomposition
  @see EigenvalueDecomposition
  */

  public EigenvalueDecomposition eig() {
    return new EigenvalueDecomposition(this);
  }

  /** Get a single element.
  @param i    Row index.
  @param j    Column index.
  @return     A(i,j)
  @exception  ArrayIndexOutOfBoundsException
  */

  public double get(final int i, final int j) {
    return this.values[i][j];
  }

  public double[][] getArray() {
    return this.values;
  }

  /** Copy the internal two-dimensional array.
  @return     Two-dimensional array copy of matrix elements.
  */

  public double[][] getArrayCopy() {
    final double[][] C = new double[this.rowCount][this.columnCount];
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        C[i][j] = this.values[i][j];
      }
    }
    return C;
  }

  /** Get column dimension.
  @return     n, the number of columns.
  */

  public int getColumnCount() {
    return this.columnCount;
  }

  public double[] getColumnPackedCopy() {
    final double[] vals = new double[this.rowCount * this.columnCount];
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        vals[i + j * this.rowCount] = this.values[i][j];
      }
    }
    return vals;
  }

  /** Get a submatrix.
  @param i0   Initial row index
  @param i1   Final row index
  @param j0   Initial column index
  @param j1   Final column index
  @return     A(i0:i1,j0:j1)
  @exception  ArrayIndexOutOfBoundsException Submatrix indices
  */

  public Matrix getMatrix(final int i0, final int i1, final int j0, final int j1) {
    final Matrix X = new Matrix(i1 - i0 + 1, j1 - j0 + 1);
    final double[][] B = X.getArray();
    try {
      for (int i = i0; i <= i1; i++) {
        for (int j = j0; j <= j1; j++) {
          B[i - i0][j - j0] = this.values[i][j];
        }
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
    return X;
  }

  /** Get a submatrix.
  @param i0   Initial row index
  @param i1   Final row index
  @param c    Array of column indices.
  @return     A(i0:i1,c(:))
  @exception  ArrayIndexOutOfBoundsException Submatrix indices
  */

  public Matrix getMatrix(final int i0, final int i1, final int[] c) {
    final Matrix X = new Matrix(i1 - i0 + 1, c.length);
    final double[][] B = X.getArray();
    try {
      for (int i = i0; i <= i1; i++) {
        for (int j = 0; j < c.length; j++) {
          B[i - i0][j] = this.values[i][c[j]];
        }
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
    return X;
  }

  /** Get a submatrix.
  @param r    Array of row indices.
  @param j0   Initial column index
  @param j1   Final column index
  @return     A(r(:),j0:j1)
  @exception  ArrayIndexOutOfBoundsException Submatrix indices
  */

  public Matrix getMatrix(final int[] r, final int j0, final int j1) {
    final Matrix X = new Matrix(r.length, j1 - j0 + 1);
    final double[][] B = X.getArray();
    try {
      for (int i = 0; i < r.length; i++) {
        for (int j = j0; j <= j1; j++) {
          B[i][j - j0] = this.values[r[i]][j];
        }
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
    return X;
  }

  /** Get a submatrix.
  @param r    Array of row indices.
  @param c    Array of column indices.
  @return     A(r(:),c(:))
  @exception  ArrayIndexOutOfBoundsException Submatrix indices
  */

  public Matrix getMatrix(final int[] r, final int[] c) {
    final Matrix X = new Matrix(r.length, c.length);
    final double[][] B = X.getArray();
    try {
      for (int i = 0; i < r.length; i++) {
        for (int j = 0; j < c.length; j++) {
          B[i][j] = this.values[r[i]][c[j]];
        }
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
    return X;
  }

  public double[] getRow(final int rowIndex) {
    final double[] row = this.values[rowIndex];
    return row.clone();
  }

  /** Get row dimension.
  *@return     m, the number of rows.
  */
  public int getRowCount() {
    return this.rowCount;
  }

  /** Make a one-dimensional row packed copy of the internal array.
  @return     Matrix elements packed in a one-dimensional array by rows.
  */
  public double[] getRowPackedCopy() {
    final double[] vals = new double[this.rowCount * this.columnCount];
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        vals[i * this.columnCount + j] = this.values[i][j];
      }
    }
    return vals;
  }

  /** Matrix inverse or pseudoinverse
  @return     inverse(A) if A is square, pseudoinverse otherwise.
  */

  public Matrix inverse() {
    return solve(identity(this.rowCount, this.rowCount));
  }

  /**
   * Inverts this matrix in place.
   */
  public final void invert() {
    invertGeneral(this);
  }

  /**
   * Inverts matrix m1 and places the new values into this matrix.  Matrix
   * m1 is not modified.
   * @param m1   the matrix to be inverted
   */
  public final void invert(final Matrix m1) {
    invertGeneral(m1);
  }

  /**
   * General invert routine.  Inverts m1 and places the result in "this".
   * Note that this routine handles both the "this" version and the
   * non-"this" version.
   *
   * Also note that since this routine is slow anyway, we won't worry
   * about allocating a little bit of garbage.
   */
  final void invertGeneral(final Matrix m1) {
    final int size = m1.rowCount * m1.columnCount;
    final double temp[] = new double[size];
    final double result[] = new double[size];
    final int row_perm[] = new int[m1.rowCount];
    final int[] even_row_exchange = new int[1];

    // Use LU decomposition and backsubstitution code specifically
    // for floating-point nxn matrices.
    if (m1.rowCount != m1.columnCount) {
      // Matrix is either under or over determined
      throw new IllegalArgumentException(
        "Row " + m1.rowCount + "and" + m1.columnCount + " column count must be the same");
    }

    // Copy source matrix to temp
    for (int rowIndex = 0; rowIndex < this.rowCount; rowIndex++) {
      for (int columnIndex = 0; columnIndex < this.columnCount; columnIndex++) {
        temp[rowIndex * this.columnCount + columnIndex] = m1.values[rowIndex][columnIndex];
      }
    }

    // Calculate LU decomposition: Is the matrix singular?
    if (!luDecomposition(m1.rowCount, temp, row_perm, even_row_exchange)) {
      // Matrix has no inverse
      throw new RuntimeException("No inverse");
    }

    // Perform back substitution on the identity matrix
    for (int rowIndex = 0; rowIndex < size; rowIndex++) {
      result[rowIndex] = 0.0;
    }

    for (int rowIndex = 0; rowIndex < this.columnCount; rowIndex++) {
      result[rowIndex + rowIndex * this.columnCount] = 1.0;
    }

    luBacksubstitution(m1.rowCount, temp, row_perm, result);

    for (int rowIndex = 0; rowIndex < this.rowCount; rowIndex++) {
      for (int columnIndex = 0; columnIndex < this.columnCount; columnIndex++) {
        this.values[rowIndex][columnIndex] = result[rowIndex * this.columnCount + columnIndex];
      }
    }
  }

  /** LU Decomposition
  @return     LUDecomposition
  @see LUDecomposition
  */

  public LUDecomposition lu() {
    return new LUDecomposition(this);
  }

  /** C = A - B
  @param B    another matrix
  @return     A - B
  */

  public Matrix minus(final Matrix B) {
    checkMatrixDimensions(B);
    final Matrix X = new Matrix(this.rowCount, this.columnCount);
    final double[][] C = X.getArray();
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        C[i][j] = this.values[i][j] - B.values[i][j];
      }
    }
    return X;
  }

  /** A = A - B
  @param B    another matrix
  @return     A - B
  */

  public Matrix minusEquals(final Matrix B) {
    checkMatrixDimensions(B);
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        this.values[i][j] = this.values[i][j] - B.values[i][j];
      }
    }
    return this;
  }

  /** One norm
  @return    maximum column sum.
  */

  public double norm1() {
    double f = 0;
    for (int j = 0; j < this.columnCount; j++) {
      double s = 0;
      for (int i = 0; i < this.rowCount; i++) {
        s += Math.abs(this.values[i][j]);
      }
      f = Math.max(f, s);
    }
    return f;
  }

  /** Two norm
  @return    maximum singular value.
  */

  public double norm2() {
    return new SingularValueDecomposition(this).norm2();
  }

  /** Frobenius norm
  @return    sqrt of sum of squares of all elements.
  */

  public double normF() {
    double f = 0;
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        f = Doubles.hypot(f, this.values[i][j]);
      }
    }
    return f;
  }

  /** Infinity norm
  @return    maximum row sum.
  */

  public double normInf() {
    double f = 0;
    for (int i = 0; i < this.rowCount; i++) {
      double s = 0;
      for (int j = 0; j < this.columnCount; j++) {
        s += Math.abs(this.values[i][j]);
      }
      f = Math.max(f, s);
    }
    return f;
  }

  /** C = A + B
  @param B    another matrix
  @return     A + B
  */

  public Matrix plus(final Matrix B) {
    checkMatrixDimensions(B);
    final Matrix X = new Matrix(this.rowCount, this.columnCount);
    final double[][] C = X.getArray();
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        C[i][j] = this.values[i][j] + B.values[i][j];
      }
    }
    return X;
  }

  /** A = A + B
  @param B    another matrix
  @return     A + B
  */

  public Matrix plusEquals(final Matrix B) {
    checkMatrixDimensions(B);
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        this.values[i][j] = this.values[i][j] + B.values[i][j];
      }
    }
    return this;
  }

  /** Print the matrix to stdout.   Line the elements up in columns
    * with a Fortran-like 'Fw.d' style format.
  @param w    Column width.
  @param d    Number of digits after the decimal.
  */

  public void print(final int w, final int d) {
    print(new PrintWriter(System.out, true), w, d);
  }

  /** Print the matrix to stdout.  Line the elements up in columns.
    * Use the format object, and right justify within columns of width
    * characters.
    * Note that is the matrix is to be read back in, you probably will want
    * to use a NumberFormat that is set to US Locale.
  @param format A  Formatting object for individual elements.
  @param width     Field width for each column.
  @see java.text.DecimalFormat#setDecimalFormatSymbols
  */

  public void print(final NumberFormat format, final int width) {
    print(new PrintWriter(System.out, true), format, width);
  }

  /** Print the matrix to the output stream.   Line the elements up in
    * columns with a Fortran-like 'Fw.d' style format.
  @param output Output stream.
  @param w      Column width.
  @param d      Number of digits after the decimal.
  */

  public void print(final PrintWriter output, final int w, final int d) {
    final DecimalFormat format = new DecimalFormat();
    format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
    format.setMinimumIntegerDigits(1);
    format.setMaximumFractionDigits(d);
    format.setMinimumFractionDigits(d);
    format.setGroupingUsed(false);
    print(output, format, w + 2);
  }

  /** Print the matrix to the output stream.  Line the elements up in columns.
    * Use the format object, and right justify within columns of width
    * characters.
    * Note that is the matrix is to be read back in, you probably will want
    * to use a NumberFormat that is set to US Locale.
  @param output the output stream.
  @param format A formatting object to format the matrix elements
  @param width  Column width.
  @see java.text.DecimalFormat#setDecimalFormatSymbols
  */

  public void print(final PrintWriter output, final NumberFormat format, final int width) {
    output.println(); // start on new line.
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        final String s = format.format(this.values[i][j]); // format the number
        final int padding = Math.max(1, width - s.length()); // At _least_ 1
                                                             // space
        for (int k = 0; k < padding; k++) {
          output.print(' ');
        }
        output.print(s);
      }
      output.println();
    }
    output.println(); // end with blank line.
  }

  /** QR Decomposition
  @return     QRDecomposition
  @see QRDecomposition
  */

  public QRDecomposition qr() {
    return new QRDecomposition(this);
  }

  /** Matrix rank
  @return     effective numerical rank, obtained from SVD.
  */

  public int rank() {
    return new SingularValueDecomposition(this).rank();
  }

  /** Set a single element.
  @param rowIndex    Row index.
  @param columnIndex    Column index.
  @param value    A(i,j).
  @exception  ArrayIndexOutOfBoundsException
  */

  public void set(final int rowIndex, final int columnIndex, final double value) {
    this.values[rowIndex][columnIndex] = value;
  }

  /** Set a submatrix.
  @param i0   Initial row index
  @param i1   Final row index
  @param j0   Initial column index
  @param j1   Final column index
  @param X    A(i0:i1,j0:j1)
  @exception  ArrayIndexOutOfBoundsException Submatrix indices
  */

  public void setMatrix(final int i0, final int i1, final int j0, final int j1, final Matrix X) {
    try {
      for (int i = i0; i <= i1; i++) {
        for (int j = j0; j <= j1; j++) {
          this.values[i][j] = X.get(i - i0, j - j0);
        }
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
  }

  /** Set a submatrix.
  @param i0   Initial row index
  @param i1   Final row index
  @param c    Array of column indices.
  @param X    A(i0:i1,c(:))
  @exception  ArrayIndexOutOfBoundsException Submatrix indices
  */

  public void setMatrix(final int i0, final int i1, final int[] c, final Matrix X) {
    try {
      for (int i = i0; i <= i1; i++) {
        for (int j = 0; j < c.length; j++) {
          this.values[i][c[j]] = X.get(i - i0, j);
        }
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
  }

  /** Set a submatrix.
  @param r    Array of row indices.
  @param j0   Initial column index
  @param j1   Final column index
  @param X    A(r(:),j0:j1)
  @exception  ArrayIndexOutOfBoundsException Submatrix indices
  */

  public void setMatrix(final int[] r, final int j0, final int j1, final Matrix X) {
    try {
      for (int i = 0; i < r.length; i++) {
        for (int j = j0; j <= j1; j++) {
          this.values[r[i]][j] = X.get(i, j - j0);
        }
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
  }

  /** Set a submatrix.
  @param r    Array of row indices.
  @param c    Array of column indices.
  @param X    A(r(:),c(:))
  @exception  ArrayIndexOutOfBoundsException Submatrix indices
  */

  public void setMatrix(final int[] r, final int[] c, final Matrix X) {
    try {
      for (int i = 0; i < r.length; i++) {
        for (int j = 0; j < c.length; j++) {
          this.values[r[i]][c[j]] = X.get(i, j);
        }
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("Submatrix indices");
    }
  }

  public final void setRow(final int rowIndex, final double... values) {
    for (int i = 0; i < this.columnCount; i++) {
      this.values[rowIndex][i] = values[i];
    }
  }

  /** Solve A*X = B
  @param B    right hand side
  @return     solution if A is square, least squares solution otherwise
  */

  public Matrix solve(final Matrix B) {
    return this.rowCount == this.columnCount ? new LUDecomposition(this).solve(B)
      : new QRDecomposition(this).solve(B);
  }

  /** Solve X*A = B, which is also A'*X' = B'
  @param B    right hand side
  @return     solution if A is square, least squares solution otherwise.
  */

  public Matrix solveTranspose(final Matrix B) {
    return transpose().solve(B.transpose());
  }

  /** Singular Value Decomposition
  @return     SingularValueDecomposition
  @see SingularValueDecomposition
  */

  public SingularValueDecomposition svd() {
    return new SingularValueDecomposition(this);
  }

  /** Multiply a matrix by a scalar, C = s*A
  @param s    scalar
  @return     s*A
  */

  public Matrix times(final double s) {
    final Matrix X = new Matrix(this.rowCount, this.columnCount);
    final double[][] C = X.getArray();
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        C[i][j] = s * this.values[i][j];
      }
    }
    return X;
  }

  /** Linear algebraic matrix multiplication, A * B
  @param B    another matrix
  @return     Matrix product, A * B
  @exception  IllegalArgumentException Matrix inner dimensions must agree.
  */

  public Matrix times(final Matrix B) {
    if (B.rowCount != this.columnCount) {
      throw new IllegalArgumentException("Matrix inner dimensions must agree.");
    }
    final Matrix X = new Matrix(this.rowCount, B.columnCount);
    final double[][] C = X.getArray();
    final double[] Bcolj = new double[this.columnCount];
    for (int j = 0; j < B.columnCount; j++) {
      for (int k = 0; k < this.columnCount; k++) {
        Bcolj[k] = B.values[k][j];
      }
      for (int i = 0; i < this.rowCount; i++) {
        final double[] Arowi = this.values[i];
        double s = 0;
        for (int k = 0; k < this.columnCount; k++) {
          s += Arowi[k] * Bcolj[k];
        }
        C[i][j] = s;
      }
    }
    return X;
  }

  /** Access the internal two-dimensional array.
  @return     Pointer to the two-dimensional array of matrix elements.
  */

  public void times(final Matrix matrix1, final Matrix matrix2) {
    if (matrix1.columnCount != matrix2.rowCount || this.rowCount != matrix1.rowCount
      || this.columnCount != matrix2.columnCount) {
      new IllegalArgumentException("Matrix must be the same size");
    }

    for (int rowIndex1 = 0; rowIndex1 < matrix1.rowCount; rowIndex1++) {
      for (int columnIndex2 = 0; columnIndex2 < matrix2.columnCount; columnIndex2++) {
        this.values[rowIndex1][columnIndex2] = 0.0;
        for (int columnIndex1 = 0; columnIndex1 < matrix1.columnCount; columnIndex1++) {
          this.values[rowIndex1][columnIndex2] += matrix1.values[rowIndex1][columnIndex1]
            * matrix2.values[columnIndex1][columnIndex2];
        }
      }
    }
  }

  // DecimalFormat is a little disappointing coming from Fortran or C's printf.
  // Since it doesn't pad on the left, the elements will come out different
  // widths. Consequently, we'll pass the desired column width in as an
  // argument and do the extra padding ourselves.

  /** Multiply a matrix by a scalar in place, A = s*A
  @param s    scalar
  @return     replace A by s*A
  */

  public Matrix timesEquals(final double s) {
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        this.values[i][j] = s * this.values[i][j];
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return toString(this);
  }

  /*
   * ------------------------ Private Methods ------------------------
   */

  /** Matrix trace.
  @return     sum of the diagonal elements.
  */

  public double trace() {
    double t = 0;
    for (int i = 0; i < Math.min(this.rowCount, this.columnCount); i++) {
      t += this.values[i][i];
    }
    return t;
  }

  /** Matrix transpose.
  @return    A'
  */

  public Matrix transpose() {
    final Matrix X = new Matrix(this.columnCount, this.rowCount);
    final double[][] C = X.getArray();
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        C[j][i] = this.values[i][j];
      }
    }
    return X;
  }

  /**  Unary minus
   @return    -A
   */

  public Matrix uminus() {
    final Matrix X = new Matrix(this.rowCount, this.columnCount);
    final double[][] C = X.getArray();
    for (int i = 0; i < this.rowCount; i++) {
      for (int j = 0; j < this.columnCount; j++) {
        C[i][j] = -this.values[i][j];
      }
    }
    return X;
  }
}
