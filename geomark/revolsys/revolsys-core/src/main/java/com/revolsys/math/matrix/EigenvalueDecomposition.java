package com.revolsys.math.matrix;

import org.jeometry.common.number.Doubles;

/** Eigenvalues and eigenvectors of a real matrix.
<P>
    If A is symmetric, then A = V*D*V' where the eigenvalue matrix D is
    diagonal and the eigenvector matrix V is orthogonal.
    I.e. A = V.times(D.times(V.transpose())) and
    V.times(V.transpose()) equals the identity matrix.
<P>
    If A is not symmetric, then the eigenvalue matrix D is block diagonal
    with the real eigenvalues in 1-by-1 blocks and any complex eigenvalues,
    lambda + i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda].  The
    columns of V represent the eigenvectors in the sense that A*V = V*D,
    i.e. A.times(V) equals V.times(D).  The matrix V may be badly
    conditioned, or even singular, so the validity of the equation
    A = V*D*inverse(V) depends upon V.cond().
**/

public class EigenvalueDecomposition implements java.io.Serializable {

  /*
   * ------------------------ Class variables ------------------------
   */

  private static final long serialVersionUID = 1;

  private transient double cdivr, cdivi;

  /** Arrays for internal storage of eigenvalues.
  @serial internal storage of eigenvalues.
  */
  private final double[] d, e;

  /** Array for internal storage of nonsymmetric Hessenberg form.
  @serial internal storage of nonsymmetric Hessenberg form.
  */
  private double[][] H;

  /** Symmetry flag.
  @serial internal symmetry flag.
  */
  private boolean issymmetric;

  /** Row and column dimension (square matrix).
  @serial matrix dimension.
  */
  private final int n;

  /*
   * ------------------------ Private Methods ------------------------
   */

  // Symmetric Householder reduction to tridiagonal form.

  /** Working storage for nonsymmetric algorithm.
  @serial working storage for nonsymmetric algorithm.
  */
  private double[] ort;

  // Symmetric tridiagonal QL algorithm.

  /** Array for internal storage of eigenvectors.
  @serial internal storage of eigenvectors.
  */
  private final double[][] V;

  // Nonsymmetric reduction to Hessenberg form.

  /** Check for symmetry, then construct the eigenvalue decomposition
      Structure to access D and V.
  @param Arg    Square matrix
  */

  public EigenvalueDecomposition(final Matrix Arg) {
    final double[][] A = Arg.getArray();
    this.n = Arg.getColumnCount();
    this.V = new double[this.n][this.n];
    this.d = new double[this.n];
    this.e = new double[this.n];

    this.issymmetric = true;
    for (int j = 0; j < this.n & this.issymmetric; j++) {
      for (int i = 0; i < this.n & this.issymmetric; i++) {
        this.issymmetric = A[i][j] == A[j][i];
      }
    }

    if (this.issymmetric) {
      for (int i = 0; i < this.n; i++) {
        for (int j = 0; j < this.n; j++) {
          this.V[i][j] = A[i][j];
        }
      }

      // Tridiagonalize.
      tred2();

      // Diagonalize.
      tql2();

    } else {
      this.H = new double[this.n][this.n];
      this.ort = new double[this.n];

      for (int j = 0; j < this.n; j++) {
        for (int i = 0; i < this.n; i++) {
          this.H[i][j] = A[i][j];
        }
      }

      // Reduce to Hessenberg form.
      orthes();

      // Reduce Hessenberg to real Schur form.
      hqr2();
    }
  }

  // Complex scalar division.

  private void cdiv(final double xr, final double xi, final double yr, final double yi) {
    double r, d;
    if (Math.abs(yr) > Math.abs(yi)) {
      r = yi / yr;
      d = yr + r * yi;
      this.cdivr = (xr + r * xi) / d;
      this.cdivi = (xi - r * xr) / d;
    } else {
      r = yr / yi;
      d = yi + r * yr;
      this.cdivr = (r * xr + xi) / d;
      this.cdivi = (r * xi - xr) / d;
    }
  }

  /** Return the block diagonal eigenvalue matrix
  @return     D
  */

  public Matrix getD() {
    final Matrix X = new Matrix(this.n, this.n);
    final double[][] D = X.getArray();
    for (int i = 0; i < this.n; i++) {
      for (int j = 0; j < this.n; j++) {
        D[i][j] = 0.0;
      }
      D[i][i] = this.d[i];
      if (this.e[i] > 0) {
        D[i][i + 1] = this.e[i];
      } else if (this.e[i] < 0) {
        D[i][i - 1] = this.e[i];
      }
    }
    return X;
  }

  // Nonsymmetric reduction from Hessenberg to real Schur form.

  /** Return the imaginary parts of the eigenvalues
  @return     imag(diag(D))
  */

  public double[] getImagEigenvalues() {
    return this.e;
  }

  /*
   * ------------------------ Constructor ------------------------
   */

  /** Return the real parts of the eigenvalues
  @return     real(diag(D))
  */

  public double[] getRealEigenvalues() {
    return this.d;
  }

  /*
   * ------------------------ Public Methods ------------------------
   */

  /** Return the eigenvector matrix
  @return     V
  */

  public Matrix getV() {
    return new Matrix(this.V, this.n, this.n);
  }

  private void hqr2() {

    // This is derived from the Algol procedure hqr2,
    // by Martin and Wilkinson, Handbook for Auto. Comp.,
    // Vol.ii-Linear Algebra, and the corresponding
    // Fortran subroutine in EISPACK.

    // Initialize

    final int nn = this.n;
    int n = nn - 1;
    final int low = 0;
    final int high = nn - 1;
    final double eps = Math.pow(2.0, -52.0);
    double exshift = 0.0;
    double p = 0, q = 0, r = 0, s = 0, z = 0, t, w, x, y;

    // Store roots isolated by balanc and compute matrix norm

    double norm = 0.0;
    for (int i = 0; i < nn; i++) {
      if (i < low | i > high) {
        this.d[i] = this.H[i][i];
        this.e[i] = 0.0;
      }
      for (int j = Math.max(i - 1, 0); j < nn; j++) {
        norm = norm + Math.abs(this.H[i][j]);
      }
    }

    // Outer loop over eigenvalue index

    int iter = 0;
    while (n >= low) {

      // Look for single small sub-diagonal element

      int l = n;
      while (l > low) {
        s = Math.abs(this.H[l - 1][l - 1]) + Math.abs(this.H[l][l]);
        if (s == 0.0) {
          s = norm;
        }
        if (Math.abs(this.H[l][l - 1]) < eps * s) {
          break;
        }
        l--;
      }

      // Check for convergence
      // One root found

      if (l == n) {
        this.H[n][n] = this.H[n][n] + exshift;
        this.d[n] = this.H[n][n];
        this.e[n] = 0.0;
        n--;
        iter = 0;

        // Two roots found

      } else if (l == n - 1) {
        w = this.H[n][n - 1] * this.H[n - 1][n];
        p = (this.H[n - 1][n - 1] - this.H[n][n]) / 2.0;
        q = p * p + w;
        z = Math.sqrt(Math.abs(q));
        this.H[n][n] = this.H[n][n] + exshift;
        this.H[n - 1][n - 1] = this.H[n - 1][n - 1] + exshift;
        x = this.H[n][n];

        // Real pair

        if (q >= 0) {
          if (p >= 0) {
            z = p + z;
          } else {
            z = p - z;
          }
          this.d[n - 1] = x + z;
          this.d[n] = this.d[n - 1];
          if (z != 0.0) {
            this.d[n] = x - w / z;
          }
          this.e[n - 1] = 0.0;
          this.e[n] = 0.0;
          x = this.H[n][n - 1];
          s = Math.abs(x) + Math.abs(z);
          p = x / s;
          q = z / s;
          r = Math.sqrt(p * p + q * q);
          p = p / r;
          q = q / r;

          // Row modification

          for (int j = n - 1; j < nn; j++) {
            z = this.H[n - 1][j];
            this.H[n - 1][j] = q * z + p * this.H[n][j];
            this.H[n][j] = q * this.H[n][j] - p * z;
          }

          // Column modification

          for (int i = 0; i <= n; i++) {
            z = this.H[i][n - 1];
            this.H[i][n - 1] = q * z + p * this.H[i][n];
            this.H[i][n] = q * this.H[i][n] - p * z;
          }

          // Accumulate transformations

          for (int i = low; i <= high; i++) {
            z = this.V[i][n - 1];
            this.V[i][n - 1] = q * z + p * this.V[i][n];
            this.V[i][n] = q * this.V[i][n] - p * z;
          }

          // Complex pair

        } else {
          this.d[n - 1] = x + p;
          this.d[n] = x + p;
          this.e[n - 1] = z;
          this.e[n] = -z;
        }
        n = n - 2;
        iter = 0;

        // No convergence yet

      } else {

        // Form shift

        x = this.H[n][n];
        y = 0.0;
        w = 0.0;
        if (l < n) {
          y = this.H[n - 1][n - 1];
          w = this.H[n][n - 1] * this.H[n - 1][n];
        }

        // Wilkinson's original ad hoc shift

        if (iter == 10) {
          exshift += x;
          for (int i = low; i <= n; i++) {
            this.H[i][i] -= x;
          }
          s = Math.abs(this.H[n][n - 1]) + Math.abs(this.H[n - 1][n - 2]);
          x = y = 0.75 * s;
          w = -0.4375 * s * s;
        }

        // MATLAB's new ad hoc shift

        if (iter == 30) {
          s = (y - x) / 2.0;
          s = s * s + w;
          if (s > 0) {
            s = Math.sqrt(s);
            if (y < x) {
              s = -s;
            }
            s = x - w / ((y - x) / 2.0 + s);
            for (int i = low; i <= n; i++) {
              this.H[i][i] -= s;
            }
            exshift += s;
            x = y = w = 0.964;
          }
        }

        iter = iter + 1; // (Could check iteration count here.)

        // Look for two consecutive small sub-diagonal elements

        int m = n - 2;
        while (m >= l) {
          z = this.H[m][m];
          r = x - z;
          s = y - z;
          p = (r * s - w) / this.H[m + 1][m] + this.H[m][m + 1];
          q = this.H[m + 1][m + 1] - z - r - s;
          r = this.H[m + 2][m + 1];
          s = Math.abs(p) + Math.abs(q) + Math.abs(r);
          p = p / s;
          q = q / s;
          r = r / s;
          if (m == l) {
            break;
          }
          if (Math.abs(this.H[m][m - 1]) * (Math.abs(q) + Math.abs(r)) < eps * (Math.abs(p)
            * (Math.abs(this.H[m - 1][m - 1]) + Math.abs(z) + Math.abs(this.H[m + 1][m + 1])))) {
            break;
          }
          m--;
        }

        for (int i = m + 2; i <= n; i++) {
          this.H[i][i - 2] = 0.0;
          if (i > m + 2) {
            this.H[i][i - 3] = 0.0;
          }
        }

        // Double QR step involving rows l:n and columns m:n

        for (int k = m; k <= n - 1; k++) {
          final boolean notlast = k != n - 1;
          if (k != m) {
            p = this.H[k][k - 1];
            q = this.H[k + 1][k - 1];
            r = notlast ? this.H[k + 2][k - 1] : 0.0;
            x = Math.abs(p) + Math.abs(q) + Math.abs(r);
            if (x == 0.0) {
              continue;
            }
            p = p / x;
            q = q / x;
            r = r / x;
          }

          s = Math.sqrt(p * p + q * q + r * r);
          if (p < 0) {
            s = -s;
          }
          if (s != 0) {
            if (k != m) {
              this.H[k][k - 1] = -s * x;
            } else if (l != m) {
              this.H[k][k - 1] = -this.H[k][k - 1];
            }
            p = p + s;
            x = p / s;
            y = q / s;
            z = r / s;
            q = q / p;
            r = r / p;

            // Row modification

            for (int j = k; j < nn; j++) {
              p = this.H[k][j] + q * this.H[k + 1][j];
              if (notlast) {
                p = p + r * this.H[k + 2][j];
                this.H[k + 2][j] = this.H[k + 2][j] - p * z;
              }
              this.H[k][j] = this.H[k][j] - p * x;
              this.H[k + 1][j] = this.H[k + 1][j] - p * y;
            }

            // Column modification

            for (int i = 0; i <= Math.min(n, k + 3); i++) {
              p = x * this.H[i][k] + y * this.H[i][k + 1];
              if (notlast) {
                p = p + z * this.H[i][k + 2];
                this.H[i][k + 2] = this.H[i][k + 2] - p * r;
              }
              this.H[i][k] = this.H[i][k] - p;
              this.H[i][k + 1] = this.H[i][k + 1] - p * q;
            }

            // Accumulate transformations

            for (int i = low; i <= high; i++) {
              p = x * this.V[i][k] + y * this.V[i][k + 1];
              if (notlast) {
                p = p + z * this.V[i][k + 2];
                this.V[i][k + 2] = this.V[i][k + 2] - p * r;
              }
              this.V[i][k] = this.V[i][k] - p;
              this.V[i][k + 1] = this.V[i][k + 1] - p * q;
            }
          } // (s != 0)
        } // k loop
      } // check convergence
    } // while (n >= low)

    // Backsubstitute to find vectors of upper triangular form

    if (norm == 0.0) {
      return;
    }

    for (n = nn - 1; n >= 0; n--) {
      p = this.d[n];
      q = this.e[n];

      // Real vector

      if (q == 0) {
        int l = n;
        this.H[n][n] = 1.0;
        for (int i = n - 1; i >= 0; i--) {
          w = this.H[i][i] - p;
          r = 0.0;
          for (int j = l; j <= n; j++) {
            r = r + this.H[i][j] * this.H[j][n];
          }
          if (this.e[i] < 0.0) {
            z = w;
            s = r;
          } else {
            l = i;
            if (this.e[i] == 0.0) {
              if (w != 0.0) {
                this.H[i][n] = -r / w;
              } else {
                this.H[i][n] = -r / (eps * norm);
              }

              // Solve real equations

            } else {
              x = this.H[i][i + 1];
              y = this.H[i + 1][i];
              q = (this.d[i] - p) * (this.d[i] - p) + this.e[i] * this.e[i];
              t = (x * s - z * r) / q;
              this.H[i][n] = t;
              if (Math.abs(x) > Math.abs(z)) {
                this.H[i + 1][n] = (-r - w * t) / x;
              } else {
                this.H[i + 1][n] = (-s - y * t) / z;
              }
            }

            // Overflow control

            t = Math.abs(this.H[i][n]);
            if (eps * t * t > 1) {
              for (int j = i; j <= n; j++) {
                this.H[j][n] = this.H[j][n] / t;
              }
            }
          }
        }

        // Complex vector

      } else if (q < 0) {
        int l = n - 1;

        // Last vector component imaginary so matrix is triangular

        if (Math.abs(this.H[n][n - 1]) > Math.abs(this.H[n - 1][n])) {
          this.H[n - 1][n - 1] = q / this.H[n][n - 1];
          this.H[n - 1][n] = -(this.H[n][n] - p) / this.H[n][n - 1];
        } else {
          cdiv(0.0, -this.H[n - 1][n], this.H[n - 1][n - 1] - p, q);
          this.H[n - 1][n - 1] = this.cdivr;
          this.H[n - 1][n] = this.cdivi;
        }
        this.H[n][n - 1] = 0.0;
        this.H[n][n] = 1.0;
        for (int i = n - 2; i >= 0; i--) {
          double ra, sa, vr, vi;
          ra = 0.0;
          sa = 0.0;
          for (int j = l; j <= n; j++) {
            ra = ra + this.H[i][j] * this.H[j][n - 1];
            sa = sa + this.H[i][j] * this.H[j][n];
          }
          w = this.H[i][i] - p;

          if (this.e[i] < 0.0) {
            z = w;
            r = ra;
            s = sa;
          } else {
            l = i;
            if (this.e[i] == 0) {
              cdiv(-ra, -sa, w, q);
              this.H[i][n - 1] = this.cdivr;
              this.H[i][n] = this.cdivi;
            } else {

              // Solve complex equations

              x = this.H[i][i + 1];
              y = this.H[i + 1][i];
              vr = (this.d[i] - p) * (this.d[i] - p) + this.e[i] * this.e[i] - q * q;
              vi = (this.d[i] - p) * 2.0 * q;
              if (vr == 0.0 & vi == 0.0) {
                vr = eps * norm
                  * (Math.abs(w) + Math.abs(q) + Math.abs(x) + Math.abs(y) + Math.abs(z));
              }
              cdiv(x * r - z * ra + q * sa, x * s - z * sa - q * ra, vr, vi);
              this.H[i][n - 1] = this.cdivr;
              this.H[i][n] = this.cdivi;
              if (Math.abs(x) > Math.abs(z) + Math.abs(q)) {
                this.H[i + 1][n - 1] = (-ra - w * this.H[i][n - 1] + q * this.H[i][n]) / x;
                this.H[i + 1][n] = (-sa - w * this.H[i][n] - q * this.H[i][n - 1]) / x;
              } else {
                cdiv(-r - y * this.H[i][n - 1], -s - y * this.H[i][n], z, q);
                this.H[i + 1][n - 1] = this.cdivr;
                this.H[i + 1][n] = this.cdivi;
              }
            }

            // Overflow control

            t = Math.max(Math.abs(this.H[i][n - 1]), Math.abs(this.H[i][n]));
            if (eps * t * t > 1) {
              for (int j = i; j <= n; j++) {
                this.H[j][n - 1] = this.H[j][n - 1] / t;
                this.H[j][n] = this.H[j][n] / t;
              }
            }
          }
        }
      }
    }

    // Vectors of isolated roots

    for (int i = 0; i < nn; i++) {
      if (i < low | i > high) {
        for (int j = i; j < nn; j++) {
          this.V[i][j] = this.H[i][j];
        }
      }
    }

    // Back transformation to get eigenvectors of original matrix

    for (int j = nn - 1; j >= low; j--) {
      for (int i = low; i <= high; i++) {
        z = 0.0;
        for (int k = low; k <= Math.min(j, high); k++) {
          z = z + this.V[i][k] * this.H[k][j];
        }
        this.V[i][j] = z;
      }
    }
  }

  private void orthes() {

    // This is derived from the Algol procedures orthes and ortran,
    // by Martin and Wilkinson, Handbook for Auto. Comp.,
    // Vol.ii-Linear Algebra, and the corresponding
    // Fortran subroutines in EISPACK.

    final int low = 0;
    final int high = this.n - 1;

    for (int m = low + 1; m <= high - 1; m++) {

      // Scale column.

      double scale = 0.0;
      for (int i = m; i <= high; i++) {
        scale = scale + Math.abs(this.H[i][m - 1]);
      }
      if (scale != 0.0) {

        // Compute Householder transformation.

        double h = 0.0;
        for (int i = high; i >= m; i--) {
          this.ort[i] = this.H[i][m - 1] / scale;
          h += this.ort[i] * this.ort[i];
        }
        double g = Math.sqrt(h);
        if (this.ort[m] > 0) {
          g = -g;
        }
        h = h - this.ort[m] * g;
        this.ort[m] = this.ort[m] - g;

        // Apply Householder similarity transformation
        // H = (I-u*u'/h)*H*(I-u*u')/h)

        for (int j = m; j < this.n; j++) {
          double f = 0.0;
          for (int i = high; i >= m; i--) {
            f += this.ort[i] * this.H[i][j];
          }
          f = f / h;
          for (int i = m; i <= high; i++) {
            this.H[i][j] -= f * this.ort[i];
          }
        }

        for (int i = 0; i <= high; i++) {
          double f = 0.0;
          for (int j = high; j >= m; j--) {
            f += this.ort[j] * this.H[i][j];
          }
          f = f / h;
          for (int j = m; j <= high; j++) {
            this.H[i][j] -= f * this.ort[j];
          }
        }
        this.ort[m] = scale * this.ort[m];
        this.H[m][m - 1] = scale * g;
      }
    }

    // Accumulate transformations (Algol's ortran).

    for (int i = 0; i < this.n; i++) {
      for (int j = 0; j < this.n; j++) {
        this.V[i][j] = i == j ? 1.0 : 0.0;
      }
    }

    for (int m = high - 1; m >= low + 1; m--) {
      if (this.H[m][m - 1] != 0.0) {
        for (int i = m + 1; i <= high; i++) {
          this.ort[i] = this.H[i][m - 1];
        }
        for (int j = m; j <= high; j++) {
          double g = 0.0;
          for (int i = m; i <= high; i++) {
            g += this.ort[i] * this.V[i][j];
          }
          // Double division avoids possible underflow
          g = g / this.ort[m] / this.H[m][m - 1];
          for (int i = m; i <= high; i++) {
            this.V[i][j] += g * this.ort[i];
          }
        }
      }
    }
  }

  private void tql2() {

    // This is derived from the Algol procedures tql2, by
    // Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
    // Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
    // Fortran subroutine in EISPACK.

    for (int i = 1; i < this.n; i++) {
      this.e[i - 1] = this.e[i];
    }
    this.e[this.n - 1] = 0.0;

    double f = 0.0;
    double tst1 = 0.0;
    final double eps = Math.pow(2.0, -52.0);
    for (int l = 0; l < this.n; l++) {

      // Find small subdiagonal element

      tst1 = Math.max(tst1, Math.abs(this.d[l]) + Math.abs(this.e[l]));
      int m = l;
      while (m < this.n) {
        if (Math.abs(this.e[m]) <= eps * tst1) {
          break;
        }
        m++;
      }

      // If m == l, d[l] is an eigenvalue,
      // otherwise, iterate.

      if (m > l) {
        int iter = 0;
        do {
          iter = iter + 1; // (Could check iteration count here.)

          // Compute implicit shift

          double g = this.d[l];
          double p = (this.d[l + 1] - g) / (2.0 * this.e[l]);
          double r = Doubles.hypot(p, 1.0);
          if (p < 0) {
            r = -r;
          }
          this.d[l] = this.e[l] / (p + r);
          this.d[l + 1] = this.e[l] * (p + r);
          final double dl1 = this.d[l + 1];
          double h = g - this.d[l];
          for (int i = l + 2; i < this.n; i++) {
            this.d[i] -= h;
          }
          f = f + h;

          // Implicit QL transformation.

          p = this.d[m];
          double c = 1.0;
          double c2 = c;
          double c3 = c;
          final double el1 = this.e[l + 1];
          double s = 0.0;
          double s2 = 0.0;
          for (int i = m - 1; i >= l; i--) {
            c3 = c2;
            c2 = c;
            s2 = s;
            g = c * this.e[i];
            h = c * p;
            r = Doubles.hypot(p, this.e[i]);
            this.e[i + 1] = s * r;
            s = this.e[i] / r;
            c = p / r;
            p = c * this.d[i] - s * g;
            this.d[i + 1] = h + s * (c * g + s * this.d[i]);

            // Accumulate transformation.

            for (int k = 0; k < this.n; k++) {
              h = this.V[k][i + 1];
              this.V[k][i + 1] = s * this.V[k][i] + c * h;
              this.V[k][i] = c * this.V[k][i] - s * h;
            }
          }
          p = -s * s2 * c3 * el1 * this.e[l] / dl1;
          this.e[l] = s * p;
          this.d[l] = c * p;

          // Check for convergence.

        } while (Math.abs(this.e[l]) > eps * tst1);
      }
      this.d[l] = this.d[l] + f;
      this.e[l] = 0.0;
    }

    // Sort eigenvalues and corresponding vectors.

    for (int i = 0; i < this.n - 1; i++) {
      int k = i;
      double p = this.d[i];
      for (int j = i + 1; j < this.n; j++) {
        if (this.d[j] < p) {
          k = j;
          p = this.d[j];
        }
      }
      if (k != i) {
        this.d[k] = this.d[i];
        this.d[i] = p;
        for (int j = 0; j < this.n; j++) {
          p = this.V[j][i];
          this.V[j][i] = this.V[j][k];
          this.V[j][k] = p;
        }
      }
    }
  }

  private void tred2() {

    // This is derived from the Algol procedures tred2 by
    // Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
    // Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
    // Fortran subroutine in EISPACK.

    for (int j = 0; j < this.n; j++) {
      this.d[j] = this.V[this.n - 1][j];
    }

    // Householder reduction to tridiagonal form.

    for (int i = this.n - 1; i > 0; i--) {

      // Scale to avoid under/overflow.

      double scale = 0.0;
      double h = 0.0;
      for (int k = 0; k < i; k++) {
        scale = scale + Math.abs(this.d[k]);
      }
      if (scale == 0.0) {
        this.e[i] = this.d[i - 1];
        for (int j = 0; j < i; j++) {
          this.d[j] = this.V[i - 1][j];
          this.V[i][j] = 0.0;
          this.V[j][i] = 0.0;
        }
      } else {

        // Generate Householder vector.

        for (int k = 0; k < i; k++) {
          this.d[k] /= scale;
          h += this.d[k] * this.d[k];
        }
        double f = this.d[i - 1];
        double g = Math.sqrt(h);
        if (f > 0) {
          g = -g;
        }
        this.e[i] = scale * g;
        h = h - f * g;
        this.d[i - 1] = f - g;
        for (int j = 0; j < i; j++) {
          this.e[j] = 0.0;
        }

        // Apply similarity transformation to remaining columns.

        for (int j = 0; j < i; j++) {
          f = this.d[j];
          this.V[j][i] = f;
          g = this.e[j] + this.V[j][j] * f;
          for (int k = j + 1; k <= i - 1; k++) {
            g += this.V[k][j] * this.d[k];
            this.e[k] += this.V[k][j] * f;
          }
          this.e[j] = g;
        }
        f = 0.0;
        for (int j = 0; j < i; j++) {
          this.e[j] /= h;
          f += this.e[j] * this.d[j];
        }
        final double hh = f / (h + h);
        for (int j = 0; j < i; j++) {
          this.e[j] -= hh * this.d[j];
        }
        for (int j = 0; j < i; j++) {
          f = this.d[j];
          g = this.e[j];
          for (int k = j; k <= i - 1; k++) {
            this.V[k][j] -= f * this.e[k] + g * this.d[k];
          }
          this.d[j] = this.V[i - 1][j];
          this.V[i][j] = 0.0;
        }
      }
      this.d[i] = h;
    }

    // Accumulate transformations.

    for (int i = 0; i < this.n - 1; i++) {
      this.V[this.n - 1][i] = this.V[i][i];
      this.V[i][i] = 1.0;
      final double h = this.d[i + 1];
      if (h != 0.0) {
        for (int k = 0; k <= i; k++) {
          this.d[k] = this.V[k][i + 1] / h;
        }
        for (int j = 0; j <= i; j++) {
          double g = 0.0;
          for (int k = 0; k <= i; k++) {
            g += this.V[k][i + 1] * this.V[k][j];
          }
          for (int k = 0; k <= i; k++) {
            this.V[k][j] -= g * this.d[k];
          }
        }
      }
      for (int k = 0; k <= i; k++) {
        this.V[k][i + 1] = 0.0;
      }
    }
    for (int j = 0; j < this.n; j++) {
      this.d[j] = this.V[this.n - 1][j];
      this.V[this.n - 1][j] = 0.0;
    }
    this.V[this.n - 1][this.n - 1] = 1.0;
    this.e[0] = 0.0;
  }
}
