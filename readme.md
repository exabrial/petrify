# Petrify

ONNX ML models -> JVM bytecode compiler. Models go in, fossils (bytecode) come out.

## Overview

### Theory of operation

Petrify reads a model representation (a `Grove`) and emits a compiled class (a `Fossil`):

```java
// Load a grove; 
final Grove = arborist.toGrove(..., "/path/to/model.onnx");

// Compile your model as bytecode:
final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

// Now make as many predictions as you like
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
| CatBoost | `CatBoostRegressor` | Regression | |
| scikit-learn | `RandomForestRegressor` | Regression | |
| scikit-learn | `ExtraTreesRegressor` | Regression | |
| scikit-learn | `HistGradientBoostingClassifier` | Classification | |
| scikit-learn | `HistGradientBoostingRegressor` | Regression | |

## Usage

### Maven Lib Coordinates

- coming soon

### Compiling a model at runtime

1. Use `Arborist` to load your ONNX model into a grove
2. Call `petrify.fossilize(MethodHandles.lookup(), grove)` to compile it to bytecode
3. Invoke your model with `fossil.predict(features)`

#### Tree ensemble (XGBoost, LightGBM, CatBoost, scikit-learn trees)

```java
final Arborist arborist = new Arborist();
final ClassifierGrove grove = arborist.toGrove(ClassifierGrove.class, "/models/xgboostSimple.onnx");

final Petrify petrify = new Petrify();
final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

assertEquals(1, fossil.predict(new float[] { 1.0f, 2.0f, 3.0f, 4.0f }));
```

#### Linear classifier (scikit-learn LogisticRegression)

```java
final Arborist arborist = new Arborist();
final LinearClassifierGrove grove = arborist.toGrove(LinearClassifierGrove.class, "/models/logistic.onnx");

final Petrify petrify = new Petrify();
final ClassifierFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

assertEquals(0, fossil.predict(new float[] { 1.6812f, 25.0f, 4.1922f, 1.0223f, 1392.0f, 3.8774f, 36.06f, -119.01f }));
```

#### Tree regression

```java
final Arborist arborist = new Arborist();
final RegressorGrove grove = arborist.toGrove(RegressorGrove.class, "/models/regression.onnx");

final Petrify petrify = new Petrify();
final RegressionFossil fossil = petrify.fossilize(MethodHandles.lookup(), grove);

final float prediction = fossil.predict(new float[] { 8.3252f, 41.0f, 6.9841f, 1.0238f, 322.0f, 2.5556f, 37.88f, -122.23f });
```

### Pre-Compiling ONNX to JVM Classes at buildtime using Maven

- coming soon

## Bootnotes

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
