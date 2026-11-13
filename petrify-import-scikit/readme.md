# petrify-import-scikit

Import scikit-learn linear models into Petrify via JSON. Supports `LinearRegression` and `LogisticRegression` (binary and multiclass).

ONNX only supports f32 (Float, in Java). This importer will maintain f64 (Double) precision. The JVM and most Python runtimes have some slight differences how they handle Floating Point arithmetic;  My testing shows it's pretty close (OSx, ARM64) but definitely implement some rounding in your code and don't count on exact values (which you really shouldn't be doing much anyway).

## Python Export

### Multiclass Classifier (LogisticRegression)

```python
import json
import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.datasets import fetch_california_housing
from sklearn.model_selection import train_test_split

data = fetch_california_housing()
y = np.digitize(data.target, bins=[1.5, 3.0])
X_train, X_test, y_train, y_test = train_test_split(data.data, y, test_size=0.2, random_state=42)

model = LogisticRegression(max_iter=1000, multi_class='multinomial', solver='lbfgs')
model.fit(X_train, y_train)

with open("logistic_housing_tier.json", "w") as f:
    json.dump({
        "type": "classifier",
        "post_transform": "softmax",
        "class_labels": model.classes_.tolist(),
        "intercepts": model.intercept_.tolist(),
        "coefficients": model.coef_.tolist(),
        "feature_names": data.feature_names,
        "model_family": "housing",
        "model_name": "logistic_housing_tier",
        "major_version": 1,
        "minor_version": 0
    }, f)
```

### Binary Classifier (LogisticRegression)

```python
model = LogisticRegression(max_iter=1000)
model.fit(X_train, y_train)

with open("logistic_binary.json", "w") as f:
    json.dump({
        "type": "classifier",
        "post_transform": "logistic",
        "class_labels": model.classes_.tolist(),
        "intercepts": model.intercept_.tolist(),
        "coefficients": model.coef_.tolist(),
        "feature_names": data.feature_names,
        "model_family": "housing",
        "model_name": "logistic_binary",
        "major_version": 1,
        "minor_version": 0
    }, f)
```

### Regressor (LinearRegression)

```python
from sklearn.linear_model import LinearRegression

model = LinearRegression()
model.fit(X_train, y_train)

with open("linear_regressor_housing.json", "w") as f:
    json.dump({
        "type": "regressor",
        "post_transform": "none",
        "intercepts": [model.intercept_] if isinstance(model.intercept_, float) else model.intercept_.tolist(),
        "coefficients": [model.coef_.tolist()],
        "feature_names": data.feature_names,
        "model_family": "housing",
        "model_name": "linear_regressor_housing",
        "major_version": 1,
        "minor_version": 0
    }, f)
```

## JSON Format

```json
{
    "type": "classifier",
    "post_transform": "softmax",
    "class_labels": [0, 1, 2],
    "intercepts": [0.08547, -0.01337, -0.07210],
    "coefficients": [
        [0.4398, -0.0089, -0.0146, -0.0273, -0.0000, -0.0042, -0.0201, -0.0080],
        [-0.1273, 0.0014, 0.0001, 0.0070, 0.0000, 0.0004, -0.0175, 0.0190],
        [-0.3125, 0.0075, 0.0145, 0.0203, 0.0000, 0.0038, 0.0376, -0.0110]
    ],
    "feature_names": ["MedInc", "HouseAge", "AveRooms", "AveBedrms", "Population", "AveOccup", "Latitude", "Longitude"],
    "pre_transformers": {
        "4": [
            {"transformer": "fill", "arg": 1000},
            {"transformer": "max", "arg": 20000},
            {"transformer": "min", "arg": 100},
            {"transformer": "log1p"}
        ]
    },
    "model_family": "housing",
    "model_name": "logistic_housing_tier",
    "major_version": 1,
    "minor_version": 0
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `type` | `string` | yes | `"classifier"` or `"regressor"` |
| `post_transform` | `string` | no | `"none"`, `"softmax"`, `"logistic"`, `"probit"`. Defaults to `"none"` if omitted. |
| `class_labels` | `long[]` | classifier only | Class label values, e.g. `[0, 1, 2]` |
| `intercepts` | `double[]` | yes | Bias terms. One per class (classifier) or one per target (regressor). |
| `coefficients` | `double[][]` | yes | Coefficient matrix. One row per class (classifier) or per target (regressor). For binary OVR classifiers, scikit stores a single row. |
| `feature_names` | `string[]` | no | Feature names in column order. Enables `FeatureMapper` for named-feature-to-array conversion. |
| `pre_transformers` | `object` | no | Transformers to apply to features before the model runs. See [Pre-transformers](#pre-transformers). |
| `model_family` | `string` | no | Model family metadata. Stored on the compiled `Fossil`. |
| `model_name` | `string` | no | Model name metadata. Stored on the compiled `Fossil`. |
| `major_version` | `int` | no | Major version metadata. Stored on the compiled `Fossil`. |
| `minor_version` | `int` | no | Minor version metadata. Stored on the compiled `Fossil`. With `major_version`, `Fossil.getModelVersion()` returns `major_version.minor_version`. |


## Pre-transformers

`pre_transformers` maps a feature index to a list of transformers. The key is the 0-based column index, written as a string (JSON keys must be strings; Python `json.dump` converts `int` keys). It is the same position as in `coefficients` and `feature_names`. List only the features that need transformers. A feature with no entry, or with an empty list, gets `noop`.

The transformers in a list run in order. Each transformer receives the output of the one before it. Put `fill` first: `max`, `min`, and `log1p` return `NaN` when the input is `NaN`.

| `transformer` | `arg` | Result |
|---|---|---|
| `noop` | not allowed | The input, unchanged. Not really needed for anything, but here for completeness.|
| `fill` | required | `arg` if the input is `NaN` (missing), otherwise the input. Same as `SimpleImputer(fill_value=...)`. |
| `max` | required | `min(input, arg)`. Clips to an upper bound. Same as `a_max` in `np.clip`. |
| `min` | required | `max(input, arg)`. Clips to a lower bound. Same as `a_min` in `np.clip`. |
| `log1p` | not allowed | `ln(1 + input)`. Same as `np.log1p`. |

In the example above, feature `4` (`Population`) changes like this: `NaN` becomes `1000`, then the value is clipped to the range `100` to `20000`, then `log1p` is applied.

Apply the same transformers to the training data before `fit`. If you do not, the model trains on different values than the `Fossil` predicts on.

## Java Usage

```java
final Vintner vintner = new ScikitVintner();

// Classifier
final ClassifierVine classifierVine = vintner.toVine("/models/logistic_housing_tier.json");
final Petrify petrify = new Petrify();
final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), classifierVine);
final int predictedClass = fossil.predict(new double[] { 8.3252, 41.0, 6.984, 1.024, 322.0, 2.556, 37.88, -122.23 });

// Regressor
final RegressorVine regressorVine = vintner.toVine("/models/linear_regressor_housing.json");
final RegressionFossil rFossil = petrify.fossilize(MethodHandles.lookup(), regressorVine);
final double predicted = rFossil.predict(new double[] { 8.3252, 41.0, 6.984, 1.024, 322.0, 2.556, 37.88, -122.23 });
```

## Precision

Models are imported at f64 (double) precision. Scikit-learn trains in f64 internally, and `json.dump` preserves full double-precision. This avoids the f32 narrowing that occurs when exporting through ONNX's `FloatTensorType`.
