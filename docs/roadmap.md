# Petrify Roadmap: Beyond Tree Ensembles

## Float64 support
Need to figure out how to have grove support Double or Float depending on the imported ONNX

## Known Limitations (Current Tree Implementation)

### `BRANCH_EQ` / `BRANCH_NEQ` NaN routing

For inequality modes (`BRANCH_LEQ`, `BRANCH_LT`, `BRANCH_GEQ`, `BRANCH_GT`), the `fcmpg` vs `fcmpl` trick controls where NaN routes — flipping between them steers NaN to the true or false branch as directed by `missingValueTracksTrue`.

For equality modes (`BRANCH_EQ`, `BRANCH_NEQ`), this trick **does not work**. Both `fcmpg` and `fcmpl` produce nonzero for NaN, so:

- `BRANCH_EQ` + `IFNE`: NaN always goes to false (NaN is never equal)
- `BRANCH_NEQ` + `IFEQ`: NaN always goes to true (NaN is never equal, so it's always "not equal")

If `missingValueTracksTrue=1` with `BRANCH_EQ`, or `missingValueTracksTrue=0` with `BRANCH_NEQ`, the current implementation **cannot honor the missing-value policy**. Fixing this requires emitting an explicit `Float.isNaN()` check before the comparison — a separate code path in `emitBranch()`.

In practice, `BRANCH_EQ`/`BRANCH_NEQ` are rare in gradient-boosted tree exports, and combining them with non-default `missingValueTracksTrue` is essentially theoretical. If it surfaces in a real model, implement the explicit NaN check path.

---

## New ONNX `ai.onnx.ml` Operators

### `LinearClassifier`

Computes `scores = X · coefficients + intercepts`, then applies post-transform, then argmax.

#### ONNX Attributes

| Attribute | Type | Description |
|---|---|---|
| `coefficients` | `float[]` | Flattened `[n_features × n_classes]` weight matrix |
| `intercepts` | `float[]` | One per class |
| `classlabels_ints` | `long[]` | Class label values (or `classlabels_strings` for string labels) |
| `multi_class` | `int` | `0` = one-vs-rest, `1` = multinomial |
| `post_transform` | `string` | `NONE`, `SOFTMAX`, `LOGISTIC`, `SOFTMAX_ZERO`, `PROBIT` |

#### Bytecode Emission Strategy

The generated `predict(float[])` method:

1. Allocate `float[] scores = new float[nClasses]`
2. For each class `c` in `[0, nClasses)`:
   - Emit `scores[c] = intercepts[c]`
   - For each feature `f` in `[0, nFeatures)`:
     - Emit `scores[c] += features[f] * coefficients[c * nFeatures + f]`
3. Delegate to `ClassifierFossil.classify(scores, postTransform, isBinarySingleScore)`
4. Emit class label lookup (reuse existing `emitClassLabelLookup()`)

All coefficients and intercepts become `ldc` constants — no arrays at prediction time.

#### New Classes Needed

- `LinearClassifierGrove` (extends some base, or standalone) — holds `coefficients`, `intercepts`, `classlabels`, `multiClass`, `postTransform`
- `Arborist` updates: new operator constant `OP_LINEAR_CLASSIFIER = "LinearClassifier"`, mapping method `mapToLinearClassifierGrove()`
- `Petrify` updates: new `fossilize(Lookup, LinearClassifierGrove)` overload, `emitLinearClassifierPredict()` method

---

### `LinearRegressor`

Computes `output = X · coefficients + intercepts`, then applies post-transform.

#### ONNX Attributes

| Attribute | Type | Description |
|---|---|---|
| `coefficients` | `float[]` | Flattened `[n_features × n_targets]` |
| `intercepts` | `float[]` | One per target |
| `targets` | `int` | Number of output targets (usually 1) |
| `post_transform` | `string` | `NONE`, `LOGISTIC`, `PROBIT` |

#### Bytecode Emission Strategy

1. Emit `float score = intercept`
2. For each feature `f`: emit `score += features[f] * coefficients[f]`
3. Delegate to `RegressionFossil.aggregate(score, postTransform)`

#### New Classes Needed

- `LinearRegressorGrove`
- `Arborist` mapping: `OP_LINEAR_REGRESSOR = "LinearRegressor"`, `mapToLinearRegressorGrove()`
- `Petrify`: `fossilize(Lookup, LinearRegressorGrove)`, `emitLinearRegressorPredict()`

---

### `SVMClassifier`

Kernel-based classification. The bytecode complexity depends heavily on kernel type.

#### ONNX Attributes

| Attribute | Type | Description |
|---|---|---|
| `kernel_type` | `string` | `LINEAR`, `POLY`, `RBF`, `SIGMOID` |
| `kernel_params` | `float[]` | `[gamma, coef0, degree]` |
| `support_vectors` | `float[]` | Flattened `[n_support_vectors × n_features]` |
| `vectors_per_class` | `int[]` | How many support vectors per class |
| `coefficients` | `float[]` | Dual coefficients, flattened `[n_classes-1 × n_support_vectors]` |
| `rho` | `float[]` | Decision function intercepts (one per class pair for OvO) |
| `prob_a` | `float[]` | Platt scaling parameter A (optional, for probability calibration) |
| `prob_b` | `float[]` | Platt scaling parameter B (optional) |
| `classlabels_ints` | `long[]` | Class labels |
| `post_transform` | `string` | `NONE`, `SOFTMAX`, `LOGISTIC`, `SOFTMAX_ZERO`, `PROBIT` |

#### Bytecode Emission Strategy

This is significantly more complex than linear models:

1. **LINEAR kernel**: Equivalent to `LinearClassifier` with support vectors as the weight matrix. Could share emission logic.
2. **RBF kernel**: For each support vector `sv`, compute `exp(-gamma * ||x - sv||²)`. This requires:
   - Per-feature subtraction and squaring loops
   - Summation
   - `Math.exp()` invocation
   - All baked as `ldc` constants for the support vector values
3. **POLY kernel**: `(gamma * dot(x, sv) + coef0) ^ degree`
   - `Math.pow()` invocation
4. **SIGMOID kernel**: `tanh(gamma * dot(x, sv) + coef0)`
   - `Math.tanh()` invocation

After kernel evaluation, apply dual coefficients and rho to get decision function values, then vote or compute probabilities.

**Recommendation**: Start with `LINEAR` kernel only (it's the most common for high-dimensional data). `RBF` is the next priority. `POLY` and `SIGMOID` are rarely exported to ONNX.

#### New Classes Needed

- `SVMClassifierGrove`
- `Arborist` mapping: `OP_SVM_CLASSIFIER = "SVMClassifier"`, `mapToSVMClassifierGrove()`
- `Petrify`: `fossilize(Lookup, SVMClassifierGrove)`, `emitSVMClassifierPredict()`
- Kernel-specific emission helpers: `emitLinearKernel()`, `emitRBFKernel()`, etc.

---

### `SVMRegressor`

Same kernel machinery as `SVMClassifier`, regression output.

#### ONNX Attributes

| Attribute | Type | Description |
|---|---|---|
| `kernel_type` | `string` | `LINEAR`, `POLY`, `RBF`, `SIGMOID` |
| `kernel_params` | `float[]` | `[gamma, coef0, degree]` |
| `support_vectors` | `float[]` | Flattened |
| `coefficients` | `float[]` | Dual coefficients |
| `rho` | `float[]` | Intercept |
| `n_supports` | `int` | Number of support vectors |
| `one_class` | `int` | `1` for one-class SVM (novelty detection) |
| `post_transform` | `string` | `NONE`, `LOGISTIC`, `PROBIT` |

#### New Classes Needed

- `SVMRegressorGrove`
- `Arborist` mapping: `OP_SVM_REGRESSOR = "SVMRegressor"`, `mapToSVMRegressorGrove()`
- `Petrify`: `fossilize(Lookup, SVMRegressorGrove)`, `emitSVMRegressorPredict()`

---

## Preprocessing Operators (Pipeline Support)

These operators appear upstream of the ML operator in sklearn/ONNX pipelines. Currently, `Arborist.findTreeEnsembleNode()` throws `UnexpectedPreservative` for anything outside `KNOWN_OP_TYPES`.

### Architecture Decision Required

**Option A — Compile the full pipeline**: Walk the ONNX graph in topological order. Each operator node gets compiled into bytecode that transforms the feature array in place (or produces a new one). The final ML operator reads from the transformed features. The generated class encapsulates the entire pipeline.

**Option B — Petrify only the ML operator**: Require the caller to handle preprocessing in Java before calling `fossil.predict()`. Simpler, but pushes responsibility to the consumer.

**Option C — Hybrid**: Compile preprocessing into a separate `Fossil`-like interface (`Preprocessor.transform(float[]) → float[]`), and let the caller chain them. Keeps Petrify modular.

### `ai.onnx.ml` Preprocessing Operators

| Operator | Description | Attributes |
|---|---|---|
| `Scaler` | Element-wise `(x - offset) * scale` | `offset: float[]`, `scale: float[]` |
| `Normalizer` | Row normalization | `norm: string` (`MAX`, `L1`, `L2`) |
| `Imputer` | Replace missing/NaN with constant | `imputed_value_floats: float[]`, `replaced_value_float: float` |
| `OneHotEncoder` | Categorical → binary vector | `cats_int64s: long[]` or `cats_strings`, `zeros: int` |
| `LabelEncoder` | Map input values → output values | `keys_*`, `values_*`, `default_*` |
| `Binarizer` | Threshold → 0/1 | `threshold: float` |

### Standard ONNX Math/Glue Operators

These appear in sklearn pipelines as the decomposed form of transformers like `StandardScaler` (`Sub` + `Div`).

| Operator | Description | Notes |
|---|---|---|
| `Sub` | Element-wise subtraction | `StandardScaler` mean subtraction |
| `Div` | Element-wise division | `StandardScaler` stddev division |
| `Add` | Element-wise addition | Bias terms |
| `Mul` | Element-wise multiplication | Scaling |
| `MatMul` | Matrix multiplication | Core of linear models in generic ONNX graphs |
| `Reshape` | Tensor shape manipulation | Pipeline plumbing |
| `Transpose` | Tensor transpose | Pipeline plumbing |
| `Concat` | Join tensors along an axis | Parallel preprocessing branch merging |
| `Gather` | Index-based selection | Feature subsetting |

### Already Handled (pass-through)

- `Cast` — type conversion, currently skipped by `Arborist`
- `ZipMap` — dict output formatting, currently skipped
- `Identity` — no-op pass-through, currently skipped

---

## Suggested Implementation Order

### Phase 1 — Linear models + preprocessing

Linear models are the simplest new ML operators — dot product + intercept — and they're the reason preprocessing operators exist in ONNX exports. sklearn users wrap `StandardScaler` + `LogisticRegression` in a `Pipeline` and export the whole thing. Tree ensembles don't need preprocessing (they're invariant to monotonic feature transforms), so none of the existing test models contain any preprocessing nodes.

Implement the ML operators first, then add preprocessing support as needed to handle real pipeline exports.

1. **`LinearClassifier`** — Dot product + intercept. Reuses existing `ClassifierFossil.classify()` and `emitClassLabelLookup()`. New `LinearClassifierGrove`, `Arborist` mapping, and `Petrify.emitLinearClassifierPredict()`.
2. **`LinearRegressor`** — Near-identical but simpler output path. Reuses `RegressionFossil.aggregate()`.
3. **`Scaler`** — Element-wise `(x - offset) * scale`. Six bytecode instructions per feature, compile-time unrolled. Covers the most common sklearn `StandardScaler` export. Introduces a `Preprocessor` fossil interface (`transform(float[]) → float[]`).
4. **`Sub` / `Div` / `Add` / `Mul`** — Element-wise arithmetic. Same emission pattern as `Scaler`. Covers the decomposed `StandardScaler` form (separate `Sub` and `Div` nodes in the ONNX graph).
5. **`Normalizer`** — Two passes: reduction (L1/L2/MAX) then element-wise division.
6. **`Imputer`** — NaN check (`fcmpg` self-compare) + conditional store of replacement value.
7. **`Binarizer`** — Single `fcmpg` + `IFGT` per feature → store `0.0f` or `1.0f`.
8. **`LabelEncoder`** — `tableswitch` or `lookupswitch` mapping input values to output values.
9. **`OneHotEncoder`** — Index matching + zero-fill. Output array size changes, so the fossil signature differs from simple `float[] → float[]`.

### Phase 2 — SVMs

10. **`SVMClassifier` (LINEAR kernel)** — Essentially the same math as `LinearClassifier` with support-vector indirection.
11. **`SVMRegressor` (LINEAR kernel)** — Same.
12. **`SVMClassifier` / `SVMRegressor` (RBF kernel)** — Per-support-vector distance computation + `Math.exp()` calls.
13. **`SVMClassifier` / `SVMRegressor` (POLY, SIGMOID kernels)** — `Math.pow()` and `Math.tanh()`. Rarely exported to ONNX.

### Phase 3 — Graph plumbing

14. **`Reshape` / `Transpose` / `Concat` / `Gather`** — Tensor shape manipulation for complex multi-branch pipelines.
15. **`MatMul`** — Matrix multiply. Core of linear models exported as generic ONNX graphs rather than `ai.onnx.ml` operators.
16. **Full graph walking** — Topological sort of the ONNX graph, chaining operator emissions into a single compiled class.
