package com.github.exabrial.petrify.testing;

import com.github.exabrial.petrify.compiler.model.ClassifierVine;
import com.github.exabrial.petrify.compiler.model.PrecisionMode;
import com.github.exabrial.petrify.compiler.model.RegressorVine;
import com.github.exabrial.petrify.model.PetrifyConstants;

public class VineGenerator {
	public static final double INTERCEPT = 2.0;
	public static final double COEFFICIENT = 3.0;
	public static final int CLASS_A = 0;
	public static final int CLASS_B = 1;

	public final PrecisionMode precision;

	public VineGenerator(final PrecisionMode precision) {
		this.precision = precision;
	}

	public RegressorVine singleTermRegressorVine() {
		final RegressorVine vine = new RegressorVine();
		vine.precisionMode = precision;
		vine.postTransform = PetrifyConstants.POST_TRANSFORM_NONE;
		vine.nFeatures = 1;
		vine.nTargets = 1;
		// y = INTERCEPT:2.0 + (COEFFICIENT:3.0 * features[0]:x)
		vine.intercepts = new double[] { INTERCEPT };
		vine.coefficients = new double[] { COEFFICIENT };
		return vine;
	}

	public ClassifierVine singleTermClassifierVine() {
		final ClassifierVine vine = new ClassifierVine();
		vine.precisionMode = precision;
		vine.postTransform = PetrifyConstants.POST_TRANSFORM_NONE;
		vine.nFeatures = 1;
		vine.nClasses = 2;
		vine.multiClass = 1; // not binary-single-score; two independent linear models with argmax
		// scores[CLASS_A:0] = intercepts[0]:1.0 + (coefficients[0]:2.0 * features[0]:x)
		// scores[CLASS_B:1] = intercepts[1]:-1.0 + (coefficients[1]:-2.0 * features[0]:x)
		vine.intercepts = new double[] { 1.0, -1.0 };
		vine.coefficients = new double[] { 2.0, -2.0 };
		vine.classlabelsInts = new long[] { CLASS_A, CLASS_B }; // argmax(scores) -> CLASS_A:0 or CLASS_B:1
		return vine;
	}
}
