package de.lmu.ifi.dbs.elki.algorithm.statistics;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2012
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.Collection;

import de.lmu.ifi.dbs.elki.algorithm.AbstractDistanceBasedAlgorithm;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.CombinedTypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.query.DistanceResultPair;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNResult;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.NumberDistance;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.logging.progress.FiniteProgress;
import de.lmu.ifi.dbs.elki.math.MeanVariance;
import de.lmu.ifi.dbs.elki.result.CollectionResult;
import de.lmu.ifi.dbs.elki.result.HistogramResult;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.GreaterEqualConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;

/**
 * Evaluate a distance functions performance by computing the average precision
 * at k, when ranking the objects by distance.
 * 
 * @author Erich Schubert
 * @param <V> Vector type
 * @param <D> Distance type
 */
public class AveragePrecisionAtK<V extends NumberVector<V, ?>, D extends NumberDistance<D, ?>> extends AbstractDistanceBasedAlgorithm<V, D, CollectionResult<DoubleVector>> {
  /**
   * The logger for this class.
   */
  private static final Logging logger = Logging.getLogger(AveragePrecisionAtK.class);

  /**
   * The parameter k - the number of neighbors to retrieve.
   */
  private int k;

  /**
   * Constructor.
   * 
   * @param distanceFunction
   */
  public AveragePrecisionAtK(DistanceFunction<? super V, D> distanceFunction, int k) {
    super(distanceFunction);
    this.k = k;
  }

  /**
   * Run the algorithm.
   */
  @Override
  public HistogramResult<DoubleVector> run(Database database) throws IllegalStateException {
    final Relation<V> relation = database.getRelation(getInputTypeRestriction()[0]);
    final Relation<Object> lrelation = database.getRelation(getInputTypeRestriction()[1]);
    final DistanceQuery<V, D> distQuery = database.getDistanceQuery(relation, getDistanceFunction());
    final KNNQuery<V, D> knnQuery = database.getKNNQuery(distQuery, k);

    if(logger.isVerbose()) {
      logger.verbose("Preprocessing clusters...");
    }

    MeanVariance mv = new MeanVariance();
    if(logger.isVerbose()) {
      logger.verbose("Processing points...");
    }
    FiniteProgress objloop = logger.isVerbose() ? new FiniteProgress("Computing nearest neighbors", relation.size(), logger) : null;

    // sort neighbors
    for(DBID id : relation.iterDBIDs()) {
      KNNResult<D> knn = knnQuery.getKNNForDBID(id, k);
      Object label = lrelation.get(id);

      int positive = 0;
      for(DistanceResultPair<D> res : knn) {
        Object olabel = lrelation.get(res.getDBID());
        if(label == null) {
          if(olabel == null) {
            positive += 1;
          }
        }
        else {
          if(label.equals(olabel)) {
            positive += 1;
          }
        }
      }
      final double precision = positive / (double) knn.size();
      mv.put(precision);
      if(objloop != null) {
        objloop.incrementProcessed(logger);
      }
    }
    if(objloop != null) {
      objloop.ensureCompleted(logger);
    }
    // Collections.sort(results);

    // Transform Histogram into a Double Vector array.
    Collection<DoubleVector> res = new ArrayList<DoubleVector>(1);
    DoubleVector row = new DoubleVector(new double[] { mv.getMean(), mv.getSampleStddev() });
    res.add(row);
    return new HistogramResult<DoubleVector>("Average Precision", "average-precision", res);
  }

  @Override
  public TypeInformation[] getInputTypeRestriction() {
    return TypeUtil.array(new CombinedTypeInformation(getDistanceFunction().getInputTypeRestriction(), TypeUtil.NUMBER_VECTOR_FIELD), TypeUtil.GUESSED_LABEL);
  }

  @Override
  protected Logging getLogger() {
    return logger;
  }

  /**
   * Parameterization class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  public static class Parameterizer<V extends NumberVector<V, ?>, D extends NumberDistance<D, ?>> extends AbstractDistanceBasedAlgorithm.Parameterizer<V, D> {
    /**
     * Parameter k to compute the average precision at.
     */
    private static final OptionID K_ID = OptionID.getOrCreateOptionID("avep.k", "K to compute the average precision at.");

    protected int k = 20;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      final IntParameter param = new IntParameter(K_ID, new GreaterEqualConstraint(2));
      if(config.grab(param)) {
        k = param.getValue();
      }
    }

    @Override
    protected AveragePrecisionAtK<V, D> makeInstance() {
      return new AveragePrecisionAtK<V, D>(distanceFunction, k);
    }
  }
}