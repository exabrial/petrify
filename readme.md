# 🪨 Petrify 

🪵 -> 🪨 ML models -> JVM bytecode compiler. Models go in, fossils (bytecode) come out!

Before you leave :) Leave a star! Thanks!

## Overview

### Theory of operation

Petrify is a fully JVM native model compiler. It reads your model from an ONNX or other native model format, walks the Trees or Linear models, and encodes the model as equivalent JVM bytecode as a stateless class you can invoke.

This differs from every other ONNX Runtime that I know of, which are essentially interpreters.

A `Grove` or a `Vine` is the IR (intermediate representation) of your model. The resulting `Fossil` is the compiled equivelant of your model.

```java
// Load a grove:
final Grove = arborist.toGrove("/class/path/to/model.onnx");
// Or you can load ONNX from a byte[]

// Compile your model as bytecode:
final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

// Now make as many predictions as you like:
final int prediction = fossil.predict(new float[] { 1.0f, 2.0f, 3.0f, 4.0f }));

// Do something with your prediction:
if (prediction != SPANISH_INQUISITION) {
  // Profits!
} else {
  throw new UnexpectedInquistionException();
}
```

No interpretation, no array traversal, no pointer chasing; just raw comparisons, conditional jumps, and arithmetic baked into bytecode. Your model executes as native JVM instructions.

Once your ONNX models are compiled, the only runtime dependency is the `Fossil` interface from the `petrify-model` submodule (ASL2-licensed, safe for business). This interface provides the entry point into your model. Once your model is compiled, no execution runtime is needed, making Petrify the ultimate lightweight champ to run your models.


## Model Coverage

### Supported ONNX Operators


| ONNX Operator | Task | Status |
|---|---|---|
| `TreeEnsembleClassifier` | Classification (binary & multiclass) | ✅ Supported |
| `TreeEnsembleRegressor` | Regression | ✅ Supported |
| `TreeEnsemble` | Classification / Regression | ✅ Supported |
| `LinearClassifier` | Classification (binary & multiclass) | ✅ Supported |
| `LinearRegressor` | Regression | ✅ Supported |
| `SVMClassifier` | Classification | Planned |
| `SVMRegressor` | Regression | Planned |


Tree node modes supported:

* `BRANCH_LEQ`
* `BRANCH_LT`
* `BRANCH_GEQ`
* `BRANCH_GTE`
* `BRANCH_GT`
* `BRANCH_EQ`
* `BRANCH_NEQ`

Passthrough operators (safely ignored during import):

* `Cast`
* `ZipMap`
* `Normalizer`
* `Identity`

### Supported Frameworks

Any framework that exports to a supported ONNX operator should work. The table below lists known-compatible frameworks and model types.

| Framework | Model Type | Task | Test Included |
|-----------|-----------|------|--------|
| XGBoost | `XGBClassifier` | Binary classification | ✅ |
| XGBoost | `XGBClassifier` | Multiclass classification | ✅ |
| LightGBM | `LGBMClassifier` | Multiclass classification | ✅ |
| CatBoost | `CatBoostClassifier` | Multiclass classification | ✅ |
| scikit-learn | `DecisionTreeClassifier` | Binary classification | ✅ |
| scikit-learn | `RandomForestClassifier` | Multiclass classification | ✅ |
| scikit-learn | `ExtraTreesClassifier` | Multiclass classification | ✅ |
| scikit-learn | `GradientBoostingRegressor` | Regression | ✅ |
| scikit-learn | `GradientBoostingClassifier` | Multiclass classification | ✅ |
| scikit-learn | `LogisticRegression` | Multiclass classification | ✅ |
| scikit-learn | `LinearRegression` | Regression | ✅ |
| XGBoost | `XGBRegressor` | Regression | ✅ |
| LightGBM | `LGBMRegressor` | Regression | ✅ |
| CatBoost | `CatBoostRegressor` | Regression | ✅ |
| scikit-learn | `RandomForestRegressor` | Regression | ✅ |
| scikit-learn | `ExtraTreesRegressor` | Regression | ✅ |
| scikit-learn | `HistGradientBoostingClassifier` | Classification | |
| scikit-learn | `HistGradientBoostingRegressor` | Regression | |

## Usage

### Maven Coordinates

```xml
<!-- Fossil interfaces (ASL2 licensed) -->
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify-model</artifactId>
  <version>0.1.0</version>
  <scope>compile</scope>
</dependency>

<!-- Compiler and libs -->
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify</artifactId>
  <version>0.1.0</version>
  <scope>compile</scope>
</dependency>
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify-compiler-model</artifactId>
  <version>0.1.0</version>
  <scope>compile</scope>
</dependency>

<!-- ONNX importer and libs -->
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify-onnx-import</artifactId>
  <version>0.1.0</version>
  <scope>compile</scope>
</dependency>
```

### Compiling a model at runtime

Tree ensemble models (XGBoost, LightGBM, CatBoost, scikit-learn trees) use `OnnxArborist` to produce a `Grove`. Linear models (LogisticRegression, LinearRegression) use `OnnxVintner` to produce a `Vine`. Both are then compiled to bytecode with `Petrify.fossilize()`.

Each `Grove` and `Vine` carries a `PrecisionMode` (`F32` or `F64`) that controls whether the compiled bytecode performs arithmetic in 32-bit float or 64-bit double precision. ONNX imports always default to `F32` to match the ONNX specification. If your model was imported from a format that supports double precision, you can set `grove.precisionMode = PrecisionMode.F64` before fossilizing to get full double-precision arithmetic. Both `predict(float[])` and `predict(double[])` overloads are available on the compiled fossil regardless of precision mode.

#### Grove classifier (tree ensemble)

```java
final Arborist arborist = new OnnxArborist();
final ClassifierGrove grove = arborist.toGrove("/models/xgboostClassifier.onnx");

final Petrify petrify = new Petrify();
final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

final int prediction = fossil.predict(new float[] { 1.0f, 2.0f, 3.0f, 4.0f });
```

#### Grove regressor (tree ensemble)

```java
final Arborist arborist = new OnnxArborist();
final RegressorGrove grove = arborist.toGrove("/models/xgboostRegressor.onnx");

final Petrify petrify = new Petrify();
final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

final float prediction = fossil.predict(new float[] { 8.3252f, 41.0f, 6.9841f, 1.0238f, 322.0f, 2.5556f, 37.88f, -122.23f });
```

#### Vine classifier (linear model)

```java
final Vintner vintner = new OnnxVintner();
final ClassifierVine vine = vintner.toVine("/models/logisticRegression.onnx");

final Petrify petrify = new Petrify();
final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), vine);

final int prediction = fossil.predict(new float[] { 1.6812f, 25.0f, 4.1922f, 1.0223f, 1392.0f, 3.8774f, 36.06f, -119.01f });
```

#### Vine regressor (linear model)

```java
final Vintner vintner = new OnnxVintner();
final RegressorVine vine = vintner.toVine("/models/linearRegression.onnx");

final Petrify petrify = new Petrify();
final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(), vine);

final float prediction = fossil.predict(new float[] { 8.3252f, 41.0f, 6.9841f, 1.0238f, 322.0f, 2.5556f, 37.88f, -122.23f });
```

### Pre-Compiling ONNX to JVM Classes at buildtime using Maven

- coming soon

### Known Limitations

- **String class labels are not supported.** ONNX classifiers can store labels as either `classlabels_ints` or `classlabels_strings`. Petrify only supports integer labels at this time. Models trained on string targets (e.g., `["cat", "dog", "fish"]`) must be label-encoded to `integer`s before export.

## Bootnotes

### Why the Geology theme?

A tribute to my father; a Geologist. Although I never studied his area of science, growing up around it I learned a ton through osmosis.

And... like every other good project name is taken.

### Motivation (Why compile to native?)

We needed fast, lightweight tree ensemble inference on the JVM without dragging in a heavyweight runtime or relying on interpretation. The java source code route is also really messy, triggers many source code alarms, and is a sharp point in builds.

We looked at the existing options and weren't happy with any of them.

* ONNX Runtime (Java binding)
    - https://onnxruntime.ai
    - The reference ONNX inference engine
    - Interpretted mode; does not compile to native JVM bytecode.
    - Chases pointers across the JVM Heap
    - Requires a large native shared library (~150MB+ depending on platform) bundled via JNI
    - Brings significant transitive dependencies
    - No pure Java option; platform-specific binaries required
* Tribuo (Oracle)
    - https://github.com/oracle/tribuo
    - Just delegates to ONNX Runtime
* JPMML-Evaluator
    - https://github.com/jpmml/jpmml-evaluator
    - Mature PMML-based model evaluator
    - Interpretted mode; does not compile to native JVM bytecode
    - AGPL License (or commercial) may be less than ideal
* JPMML-Transpiler
    - https://github.com/jpmml/jpmml-transpiler
    - Compiles PMML models to Java source code
    - Operates on PMML (not ONNX) and generates `.java` source files rather than bytecode
    - License may be less than ideal
* m2cgen
    - https://github.com/BayesWitnesses/m2cgen
    - Transpiles trained Python model objects `.java` source file
    - Giant nest of `if/then/else`
    - Runs into 64kb method limits
    - Reads scikit-learn, XGBoost, LightGBM model objects directly, not ONNX
    - Last commit was 2022
    - No runtime dependencies in the generated code, hey nice!

Petrify takes a different approach: compile the tree ensemble directly to JVM bytecode. The result is a plain Java class with no runtime dependencies beyond the `Fossil` interface. No JNI, no native libraries, no interpretation loop, no Java source conversion step.


### License and other boring legal notes

- All files in this project are copyrighted
- All files in `petrify-model` are Apache Source Licensed (ASL2.0)
    - This is done so your models extend from and use ASL2.0 classes at runtime
- All files in `petrify-onnx-proto` are Apache Source Licensed (ASL2.0)
    - `onnx-ml.proto` copied from the main ONNX project. Their license and rights are maintained.
- All all other files in this project are licensed under EUPL-1.2
    - This license allows you to safely use unmodified/un-extended code in closed-source commercial projects, without revealing your company's proprietary application code in most cases.
    - However: Note that if you modify/extend Petrify, distribute it, and/or offer online access to apps through a modified/extended Petrify, it is required by law that the source code for your Petrify changeset be made available _first_, before offering said access to your app or distribution.
    - Again, this does not include your proprietary application source code, just the changeset to Petrify.
- ONNX, XGBoost, LightGBM, scikit-learn, and other names are trademarks; this project is not endorsed by nor affiliated with them.

### Verifying artifacts

All release artifacts are signed with my GPG key. You can verify signatures using the following public key:

```
Fingerprint: 871638A21A7F2C38066471420306A354336B4F0D
```

To import the key and verify artifacts in your local Maven repository:

```bash
gpg --keyserver keyserver.ubuntu.com --recv-keys 871638A21A7F2C38066471420306A354336B4F0D

find ~/.m2/repository/com/github/exabrial/petrify* -name '*.asc' -exec gpg --verify {} \;
```
