package de.lmu.ifi.dbs.data.synthetic;

import de.lmu.ifi.dbs.data.DoubleVector;
import de.lmu.ifi.dbs.linearalgebra.LinearEquation;
import de.lmu.ifi.dbs.linearalgebra.Matrix;
import de.lmu.ifi.dbs.utilities.Util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * TODO: comment
 *
 * @author Elke Achtert (<a href="mailto:achtert@dbs.ifi.lmu.de">achtert@dbs.ifi.lmu.de</a>)
 */
public class CorrelationGenerator {
  public static final NumberFormat NF = NumberFormat.getInstance(Locale.US);

  private static Random RANDOM = new Random(210571);

  private static double MIN = 0;
  private static double MAX = 1;

  private static double MAX_JITTER_PCT = 0.1;
  private static double JITTER_STANDARD_DEVIATION = MAX_JITTER_PCT * (MAX - MIN) / 100;

  public static void main(String[] args) {
    NF.setMaximumFractionDigits(4);
    NF.setMinimumFractionDigits(4);
    try {
      geradenKDDPaper();
//      classificationKDDPaper();
//      jitterKDDPaper();
//      dimKDDPaper();
    }

    catch (Exception e) {
      e.printStackTrace();
    }
  }

  static void geradenKDDPaper() throws FileNotFoundException {
    String dir = "p:/nfs/infdbs/WissProj/CorrelationClustering/DependencyDerivator/experiments/synthetic/geraden/";
    dir = "";
    int dim = 3;
    double maxDist = ((MAX - MIN) + MIN) * Math.sqrt(dim);
    JITTER_STANDARD_DEVIATION = MAX_JITTER_PCT * maxDist / 100;

    Matrix point = centroid(3);

    {// g1
      System.out.println("generate g1...");
      double[][] b = new double[3][1];
      b[0][0] = 1;
      b[1][0] = -0.5;
      b[2][0] = 1;
      Matrix basis = new Matrix(b);
      PrintStream outStream = new PrintStream(new FileOutputStream(dir + "g1.txt"));
      generateCorrelation(1000, point, basis, true, outStream, false);
      outStream.flush();
      outStream.close();
      if (true) return;
    }
    {// g2
      System.out.println("generate g2...");
      double[][] b = new double[3][1];
      b[0][0] = 1;
      b[1][0] = 1;
      b[2][0] = 1;
      Matrix basis = new Matrix(b);
      PrintStream outStream = new PrintStream(new FileOutputStream(dir + "g2.txt"));
      generateCorrelation(1000, point, basis, true, outStream, false);
      outStream.flush();
      outStream.close();
    }
    {// g3
      System.out.println("generate g3...");
      double[][] b = new double[3][1];
      b[0][0] = -1;
      b[1][0] = 1;
      b[2][0] = 1;
      Matrix basis = new Matrix(b);
      PrintStream outStream = new PrintStream(new FileOutputStream(dir + "g3.txt"));
      generateCorrelation(1000, point, basis, true, outStream, false);
      outStream.flush();
      outStream.close();
    }

    {// g4
      System.out.println("generate g4...");
      double[][] b = new double[3][1];
      b[0][0] = 1;
      b[1][0] = -1;
      b[2][0] = 1;
      Matrix basis = new Matrix(b);
      PrintStream outStream = new PrintStream(new FileOutputStream(dir + "g4.txt"));
      generateCorrelation(1000, point, basis, true, outStream, false);
      outStream.flush();
      outStream.close();
    }

    {// g5
      System.out.println("generate g5...");
      double[][] b = new double[3][1];
      b[0][0] = 1;
      b[1][0] = 1;
      b[2][0] = -1;
      Matrix basis = new Matrix(b);
      PrintStream outStream = new PrintStream(new FileOutputStream(dir + "g5.txt"));
      generateCorrelation(1000, point, basis, true, outStream, false);
      outStream.flush();
      outStream.close();
    }

  }

  static void jitterKDDPaper() throws FileNotFoundException {
    String dir = "P:/nfs/infdbs/WissProj/CorrelationClustering/DependencyDerivator/experiments/synthetic/jitter/";

    int dim = 3;
    double maxDist = ((MAX - MIN) + MIN) * Math.sqrt(dim);
    Matrix point = centroid(dim);

    double[][] b = new double[dim][2];
    b[0][0] = 1;
    b[1][0] = 1;
    b[2][0] = 1;

    b[0][1] = 1;
    b[1][1] = 2;
    b[2][1] = 0;
    Matrix basis = new Matrix(b);

    PrintStream outStream = new PrintStream(new FileOutputStream(dir + "dim_" + dim + "_jitter_" + 0 + ".txt"));
    GeneratorResult result = generateCorrelation(4000, point, basis, false, outStream, true);

    for (int j = 1; j <= 5; j += 1) {
      MAX_JITTER_PCT = j;
      JITTER_STANDARD_DEVIATION = MAX_JITTER_PCT * maxDist / 100;
      List<DoubleVector> doubleVectors = new ArrayList<DoubleVector>();
      for (DoubleVector doubleVector : result.doubleVectors) {
        doubleVectors.add(jitter(doubleVector, result.dependency.normalVectors));
      }

      double std = standardDeviation(doubleVectors, point, result.dependency.basisVectors);
      System.out.println("standard deviation " + std);
      outStream = new PrintStream(new FileOutputStream(dir + "dim_" + dim + "_jitter_" + j + ".txt"));
      output(outStream, doubleVectors, true, result.dependency.dependency, std, null);
    }
  }

  static void classificationKDDPaper() throws FileNotFoundException {
    String dir = "p:/nfs/infdbs/WissProj/CorrelationClustering/DependencyDerivator/experiments/synthetic/";

    int dim = 2;
    double maxDist = ((MAX - MIN) + MIN) * Math.sqrt(dim);
    JITTER_STANDARD_DEVIATION = MAX_JITTER_PCT * maxDist / 100;


    PrintStream outStream = new PrintStream(new FileOutputStream(dir + "geraden_100_jitter_0.75.txt", false));
    int numPoints = 100;

    {// g1
      System.out.println("generate g1...");
      double[][] p = new double[2][1];
      p[0][0] = 0.5;
      p[1][0] = 0.5;
      Matrix point = new Matrix(p);

      double[][] b = new double[2][1];
      b[0][0] = 1;
      b[1][0] = 1;
      Matrix basis = new Matrix(b);
      generateCorrelation(numPoints, point, basis, true, outStream, false, "g1");
    }
    {// g2
      System.out.println("generate g2...");
      double[][] p = new double[2][1];
      p[0][0] = 0.5;
      p[1][0] = 0.5;
      Matrix point = new Matrix(p);

      double[][] b = new double[2][1];
      b[0][0] = -1;
      b[1][0] = 2;
      Matrix basis = new Matrix(b);
      generateCorrelation(numPoints, point, basis, true, outStream, false, "g2");
    }
    {// g3
      System.out.println("generate g3...");
      double[][] p = new double[2][1];
      p[0][0] = 0.5;
      p[1][0] = 0.25;
      Matrix point = new Matrix(p);

      double[][] b = new double[2][1];
      b[0][0] = 1;
      b[1][0] = 2;
      Matrix basis = new Matrix(b);
      generateCorrelation(numPoints, point, basis, true, outStream, false, "g3");
    }

    {// g4
      System.out.println("generate g4...");
      double[][] p = new double[2][1];
      p[0][0] = 0.1;
      p[1][0] = 0.2;
      Matrix point = new Matrix(p);

      double[][] b = new double[2][1];
      b[0][0] = 1;
      b[1][0] = 0;
      Matrix basis = new Matrix(b);
      generateCorrelation(numPoints, point, basis, true, outStream, false, "g4");
    }

    {// g5
      System.out.println("generate g5...");
      double[][] p = new double[2][1];
      p[0][0] = 0.3;
      p[1][0] = 0;
      Matrix point = new Matrix(p);

      double[][] b = new double[2][1];
      b[0][0] = 1;
      b[1][0] = -1;
      Matrix basis = new Matrix(b);
      generateCorrelation(numPoints, point, basis, true, outStream, false, "g5");
      outStream.flush();
      outStream.close();
    }
  }

  private static void dimKDDPaper() throws FileNotFoundException {
//    String dir = "P:/nfs/infdbs/WissProj/CorrelationClustering/DependencyDerivator/experiments/synthetic/dim/";
    String dir = "";
    for (int dim = 5; dim <= 50; dim += 5) {
      System.out.println("");
      System.out.println("");
      System.out.println("dim " + dim);

      double maxDist = ((MAX - MIN) + MIN) * Math.sqrt(dim);
      JITTER_STANDARD_DEVIATION = MAX_JITTER_PCT * maxDist / 100;

      int corrDim = RANDOM.nextInt(dim - 1) + 1;
      Matrix point = centroid(dim);
      Matrix basis = correlationBasis(dim, corrDim);
//      System.out.println("basis " + basis);
      boolean jitter = true;
      PrintStream outStream = new PrintStream(new FileOutputStream(dir + "dim_" + dim + "_" + corrDim + "c.txt"));
      generateCorrelation(1000, point, basis, jitter, outStream, true);
      outStream.flush();
      outStream.close();
    }
  }

  static GeneratorResult generateCorrelation(int numberOfPoints, final Matrix point, final Matrix basis,
                                             boolean jitter, PrintStream outStream, boolean verbose) {
    return generateCorrelation(numberOfPoints, point, basis, jitter, outStream, verbose, null);
  }

  static GeneratorResult generateCorrelation(int numberOfPoints, final Matrix point, final Matrix basis,
                                             boolean jitter, PrintStream outStream, boolean verbose, String label) {

    if (point.getRowDimension() != basis.getRowDimension())
      throw new IllegalArgumentException("point.getRowDimension() != basis.getRowDimension()!");

    if (point.getColumnDimension() != 1)
      throw new IllegalArgumentException("point.getColumnDimension() != 1!");

    if (! inMinMax(point))
      throw new IllegalArgumentException("point not in min max!");

    Dependency dependency = determineDependency(point, basis);
    if (verbose) {
      System.out.println("Generated dependency");
      System.out.println(dependency.toString());
    }

    Matrix b = dependency.basisVectors;

    List<DoubleVector> featureVectors = new ArrayList<DoubleVector>(numberOfPoints);
    while (featureVectors.size() != numberOfPoints) {
      Matrix featureVector = generateCorrelation(point, b);
      double distance = distance(featureVector, point, b);
      if (distance > 1E-13)
      System.out.println("distance " + distance);
      if (jitter) {
        featureVector = jitter(featureVector, dependency.normalVectors);
      }
      if (inMinMax(featureVector)) {
        featureVectors.add(new DoubleVector(featureVector));
//        if (verbose) {
//          System.out.print("\r" + featureVectors.size());
//        }
      }
    }

    double std = standardDeviation(featureVectors, point, b);
    System.out.println("standard deviation " + std);
    output(outStream, featureVectors, jitter, dependency.dependency, std, label);

    return new GeneratorResult(featureVectors, dependency);
  }

  static Dependency determineDependency(final Matrix point, final Matrix basis) {
    // orthonormal basis of subvectorspace U
    Matrix orthonormalBasis_U = orthonormalize(basis);
    Matrix completeVectors = completeBasis(orthonormalBasis_U);
//    System.out.println("basis_U " + orthonormalBasis_U.toString(NF));

    // orthonormal basis of vectorspace V
    Matrix basis_V = appendColumn(orthonormalBasis_U, completeVectors);
    basis_V = orthonormalize(basis_V);
//    System.out.println("basis V " + basis_V.toString(NF));

    // normal vectors of U
    Matrix normalVectors_U = basis_V.getMatrix(0, basis_V.getRowDimension() - 1,
                                               basis.getColumnDimension(),
                                               basis.getRowDimension() - basis.getColumnDimension() + basis.getColumnDimension() - 1);
//    System.out.println("normal vector U " + normalVectors_U.toString(NF));
    Matrix transposedNormalVectors = normalVectors_U.transpose();
//    System.out.println("tNV " + transposedNormalVectors.toString(NF));
//    System.out.println("point " + point.toString(NF));

    // gauss jordan
    Matrix B = transposedNormalVectors.times(point);
//    System.out.println("B " + B.toString(NF));
    Matrix gaussJordan = new Matrix(transposedNormalVectors.getRowDimension(), transposedNormalVectors.getColumnDimension() + B.getColumnDimension());
    gaussJordan.setMatrix(0, transposedNormalVectors.getRowDimension() - 1, 0, transposedNormalVectors.getColumnDimension() - 1, transposedNormalVectors);
    gaussJordan.setMatrix(0, gaussJordan.getRowDimension() - 1, transposedNormalVectors.getColumnDimension(), gaussJordan.getColumnDimension() - 1, B);


    double[][] a = new double[transposedNormalVectors.getRowDimension()][transposedNormalVectors.getColumnDimension()];
    double[][] we = transposedNormalVectors.getArray();
    double[] b = B.getColumn(0).getRowPackedCopy();
    System.arraycopy(we, 0, a, 0, transposedNormalVectors.getRowDimension());

    System.out.println("a "+new Matrix(a).toString(NF));
    System.out.println("b "+Util.format(b, ",", 4));
//
    LinearEquation lq = new LinearEquation(a, b);
    lq.solveByTotalPivotSearch();
    Matrix solution = lq.getEquationMatrix();
//    System.out.println(lq.equationsToString());
    System.out.println("solution " + solution.toString(NF));

    Matrix solution2 = gaussJordan.gaussJordanElimination();
    System.out.println("solution2 " + solution2.toString(NF));

    Dependency dependency = new Dependency(orthonormalBasis_U, normalVectors_U, solution);
//    System.out.println("dep " + dependency);
    return dependency;
  }

  static Matrix generateCorrelation(Matrix point, Matrix basis) {
    Matrix featureVector = point.copy();
    for (int i = 0; i < basis.getColumnDimension(); i++) {
//      System.out.println("   d " + distance(featureVector, point, basis));
//      double lambda_i = RANDOM.nextDouble() * (0.5 * Math.sqrt(point.getRowDimension())) / point.getRowDimension();
      double lambda_i = RANDOM.nextDouble();
      if (RANDOM.nextBoolean()) lambda_i *= -1;
      Matrix b_i = basis.getColumn(i);
      featureVector = featureVector.plus(b_i.times(lambda_i));

    }
    return featureVector;
  }

  static DoubleVector jitter(DoubleVector doubleVector, Matrix normalVectors) {
    Matrix m = jitter(doubleVector.getColumnVector(), normalVectors);
    return new DoubleVector(m);
  }

  static Matrix jitter(Matrix featureVector, Matrix normalVectors) {
    for (int i = 0; i < normalVectors.getColumnDimension(); i++) {
      Matrix n_i = normalVectors.getColumn(i);
      n_i.normalizeCols();
      double distance = RANDOM.nextGaussian() * JITTER_STANDARD_DEVIATION;
      featureVector = n_i.times(distance).plus(featureVector);
    }
    return featureVector;

//    int index = RANDOM.nextInt(normalVectors.getColumnDimension());
//    Matrix normalVector = normalVectors.getColumn(index);
//    double distance = RANDOM.nextGaussian() * JITTER_STANDARD_DEVIATION;
//    return normalVector.times(distance).plus(featureVector);

//    for (int i = 0; i < featureVector.getRowDimension(); i++) {
//      double j = (RANDOM.nextDouble() * 2 - 1) * (MAX_JITTER_PCT * (MAX - MIN) / 100.0);
//      featureVector.set(i, 0, featureVector.get(i, 0) + j);
//    }
//    return featureVector;
  }

  public static boolean inMinMax(Matrix featureVector) {
    for (int i = 0; i < featureVector.getRowDimension(); i++) {
      for (int j = 0; j < featureVector.getColumnDimension(); j++) {
        double value = featureVector.get(i, j);
        if (value < MIN) return false;
        if (value > MAX) return false;
      }
    }
    return true;
  }

  static void output(PrintStream outStream, List<DoubleVector> featureVectors, boolean jitter, Matrix dependency, double std, String label) {
    outStream.println("########################################################");
    if (jitter) {
      outStream.println("### max Jitter " + MAX_JITTER_PCT + "%");
      outStream.println("### Randomized standard deviation " + JITTER_STANDARD_DEVIATION);
      outStream.println("### Real       standard deviation " + std);
      outStream.println("###");
    }
    double[][] dependencyArray = dependency.getArray();
    for (double[] d : dependencyArray) {
      outStream.println("### " + Util.format(d, " ", 4));
    }
    outStream.println("########################################################");


    for (DoubleVector featureVector : featureVectors) {
      if (label == null)
        outStream.println(featureVector);
      else {
        outStream.print(featureVector);
        outStream.println(" " + label);
      }
    }
  }

  static Matrix completeBasis(Matrix b) {
    Matrix e = Matrix.unitMatrix(b.getRowDimension());

    Matrix basis = b.copy();
    Matrix result = null;
    for (int i = 0; i < e.getColumnDimension(); i++) {
      Matrix e_i = e.getColumn(i);
      boolean li = basis.linearlyIndependent(e_i);
      if (li) {
        if (result == null) {
          result = e_i.copy();
        }
        else {
          result = appendColumn(result, e_i);
        }
        basis = appendColumn(basis, e_i);
      }
    }


    return result;
  }

  static Matrix appendColumn(Matrix m, Matrix column) {
    if (m.getRowDimension() != column.getRowDimension())
      throw new IllegalArgumentException("m.getRowDimension() != column.getRowDimension()");

    Matrix result = new Matrix(m.getRowDimension(), m.getColumnDimension() + column.getColumnDimension());
    for (int i = 0; i < result.getColumnDimension(); i++) {
      if (i < m.getColumnDimension()) {
        result.setColumn(i, m.getColumn(i));
      }
      else {
        result.setColumn(i, column.getColumn(i - m.getColumnDimension()));
      }
    }
    return result;
  }

  static Matrix orthonormalize(Matrix u) {
    Matrix v = u.getColumn(0).copy();

    for (int i = 1; i < u.getColumnDimension(); i++) {
      Matrix u_i = u.getColumn(i);
      Matrix sum = new Matrix(u.getRowDimension(), 1);
      for (int j = 0; j < i; j++) {
        Matrix v_j = v.getColumn(j);
        double scalar = u_i.scalarProduct(0, v_j, 0) / v_j.scalarProduct(0, v_j, 0);
        sum = sum.plus(v_j.times(scalar));
      }
      Matrix v_i = u_i.minus(sum);
      v = appendColumn(v, v_i);
    }

    v.normalizeCols();
    return v;
  }

  static Matrix centroid(int dim) {
    double[][] p = new double[dim][];
    for (int i = 0; i < p.length; i++) {
      p[i] = new double[]{(MAX - MIN) / 2};
    }
    return new Matrix(p);
  }

  static Matrix correlationBasis(int dim, int correlationDimensionality) {
    double[][] b = new double[dim][correlationDimensionality];
    for (int i = 0; i < b.length; i++) {
      if (i < correlationDimensionality) {
        b[i][i] = 1;
      }
      else {
        for (int j = 0; j < correlationDimensionality; j++) {
          b[i][j] = RANDOM.nextInt(10);
        }
      }
    }
    return new Matrix(b);
  }

  static double standardDeviation(List<DoubleVector> featureVectors, Matrix point, Matrix basis) {
//    Matrix b = basis.copy();
//    b.normalizeCols();
    double std_2 = 0;
    for (DoubleVector doubleVector : featureVectors) {
      double distance = distance(doubleVector.getColumnVector(), point, basis);
//      System.out.println("ddd " + distance);
      std_2 += distance * distance;
    }
    return Math.sqrt(std_2 / featureVectors.size());
  }

  static double distance(Matrix p, Matrix point, Matrix basis) {
    Matrix p_minus_a = p.minus(point);
    Matrix proj = p_minus_a.projection(basis);
    return p_minus_a.minus(proj).euclideanNorm(0);
  }

  private static class Dependency {
    Matrix basisVectors;
    Matrix normalVectors;
    Matrix dependency;

    public Dependency(Matrix basisVectors, Matrix normalvectors, Matrix dependency) {
      this.basisVectors = basisVectors;
      this.normalVectors = normalvectors;
      this.dependency = dependency;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString() {
      return "basisVectors : " + basisVectors.toString(NF) +
             "normalVectors: " + normalVectors.toString(NF) +
             "dependency   : " + dependency.toString(NF);
    }
  }

  private static class GeneratorResult {
    List<DoubleVector> doubleVectors;
    Dependency dependency;

    public GeneratorResult(List<DoubleVector> doubleVectors, Dependency dependency) {
      this.doubleVectors = doubleVectors;
      this.dependency = dependency;
    }
  }
}
