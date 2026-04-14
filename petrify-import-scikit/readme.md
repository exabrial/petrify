# petrify-import-scikit

Import scikit-learn linear models into Petrify via JSON. Supports `LinearRegression` and `LogisticRegression` (binary and multiclass).

ONNX only supports f32 (Float, in Java). This importer will maintain f64 (Double) precision. The JVM and most Python runtimes
have some slight differences how they handle Floating Point arithmetic;  My testing shows it's with about decimal place, but
definitely implement some rounding in your code if you need extremely precision that's appropriate for your application.

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
        "coefficients": model.coef_.tolist()
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
        "coefficients": model.coef_.tolist()
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
        "coefficients": [model.coef_.tolist()]
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
    ]
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `type` | `string` | yes | `"classifier"` or `"regressor"` |
| `post_transform` | `string` | no | `"none"`, `"softmax"`, `"logistic"`, `"probit"`. Defaults to `"none"` if omitted. |
| `class_labels` | `long[]` | classifier only | Class label values, e.g. `[0, 1, 2]` |
| `intercepts` | `double[]` | yes | Bias terms. One per class (classifier) or one per target (regressor). |
| `coefficients` | `double[][]` | yes | Coefficient matrix. One row per class (classifier) or per target (regressor). For binary OVR classifiers, scikit stores a single row. |


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
