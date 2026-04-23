# 🪨 Petrify 

🪵 -> 🪨 ML models -> ultralight JVM bytecode compiler. Models go in, fossils (bytecode) come out!

⭐ Before you leave, ⭐ Leave a star! ⭐ Thanks! :) ⭐

## Overview

### Theory of operation

Petrify is a ultralight machine learning model compiler for the the JVM. It reads your model from an ONNX, LightGBM native, or scikit-learn JSON format, walks the Trees or Linear models, and encodes the model in equivalent JVM bytecode as a stateless class you can invoke.

This differs from every other ONNX Runtime that I know of, which are essentially interpreters and are reading data from the JVM Heap. Petrify encodes your model as bytecode and stores your splits/weights in the constant pool.

Petrify has exactly one heap allocation per invocation when executing trees (the scores accumulator array). This is required to make the compiled model thread-safe. Future iterations might have an option where thread safety is not a concern, making a model invocation create 0 Garbage Collection pressure.


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
  // proceed with ruthless efficiency and almost fanatical dedication
} else {
  throw new UnexpectedInquistionException();
}
```

No interpretation, no JNI, no giant runtime, no massive list of dependencies, no array traversal, no pointer chasing; just raw comparisons, conditional jumps, and arithmetic petrified as bytecode. Your model executes as native JVM instructions.

Once your ONNX models are compiled, the only runtime dependency is the `Fossil` interface from the `petrify-model` submodule (ASL2-licensed, safe for business). This interface provides the entry point into your model. Once your model is compiled, no execution runtime is needed, making Petrify the ultimate lightweight champ to run your models.


## Model Coverage

### Importers

| Module | Format | Precision | Model Types |
|---|---|---|---|
| `petrify-import-onnx` | ONNX (`.onnx`) | F32 | Tree ensembles, linear models |
| `petrify-import-lightgbm` | LightGBM native text (`.txt`) | F64 | LightGBM tree ensembles |
| `petrify-import-scikit` | scikit-learn JSON (`.json`) | F64 | Linear/logistic regression |

### Precision modes

Each `Grove` and `Vine` carries a `PrecisionMode` (`F32` or `F64`) that controls whether the compiled bytecode performs arithmetic in 32-bit float or 64-bit double precision. ONNX imports default to `F32` to match the ONNX specification; LightGBM native and scikit-learn JSON imports default to `F64`. You can override precision before fossilizing by setting `grove.precisionMode = PrecisionMode.F64`. Both `predict(float[])` and `predict(double[])` overloads are available on the compiled fossil regardless of precision mode.

The LightGBM native text importer (`petrify-import-lightgbm`) in F64 mode produces **bit-identical** IEEE 754 output to the official LightGBM C/C++ runtime (at least on  my machine). To achieve this, use `double[]` features so that feature values enter the F64 computation at full precision; using `float[]` introduces F32 quantization on the inputs and will produce small deltas (~1e-06).

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

| Framework | Model Type | Task | Importer | Test Included |
|-----------|-----------|------|----------|--------|
| XGBoost | `XGBClassifier` | Binary classification | ONNX | ✅ |
| XGBoost | `XGBClassifier` | Multiclass classification | ONNX | ✅ |
| LightGBM | `LGBMClassifier` | Multiclass classification | ONNX, Native | ✅ |
| LightGBM | `LGBMRegressor` | Regression | ONNX, Native | ✅ |
| CatBoost | `CatBoostClassifier` | Multiclass classification | ONNX | ✅ |
| CatBoost | `CatBoostRegressor` | Regression | ONNX | ✅ |
| scikit-learn | `DecisionTreeClassifier` | Binary classification | ONNX | ✅ |
| scikit-learn | `RandomForestClassifier` | Multiclass classification | ONNX | ✅ |
| scikit-learn | `RandomForestRegressor` | Regression | ONNX | ✅ |
| scikit-learn | `ExtraTreesClassifier` | Multiclass classification | ONNX | ✅ |
| scikit-learn | `ExtraTreesRegressor` | Regression | ONNX | ✅ |
| scikit-learn | `GradientBoostingClassifier` | Multiclass classification | ONNX | ✅ |
| scikit-learn | `GradientBoostingRegressor` | Regression | ONNX | ✅ |
| scikit-learn | `LogisticRegression` | Multiclass classification | ONNX, JSON | ✅ |
| scikit-learn | `LinearRegression` | Regression | ONNX, JSON | ✅ |
| XGBoost | `XGBRegressor` | Regression | ONNX | ✅ |
| scikit-learn | `HistGradientBoostingClassifier` | Classification | ONNX | |
| scikit-learn | `HistGradientBoostingRegressor` | Regression | ONNX | |

## Requirements

* Compiler: JDK25
* Runtime: JDK17

Because Petrify uses the new Bytecode/Class-File API that was introduced in JEP-484, **JDK 25 is required** to run the Petrify compiler (including the Maven plugin). However, compiled fossils target JDK 17 bytecode and can run on JDK 17+.

As such, the `petrify-model` module (containing the `Fossil` interfaces) is JDK 17 compatible and is the only runtime dependency your application needs.

## Usage

### Maven Coordinates

```xml
<!-- Fossil interfaces (ASL2 licensed) -->
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify-model</artifactId>
  <version>1.2.0</version>
  <scope>compile</scope>
</dependency>

<!-- Compiler and libs -->
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify</artifactId>
  <version>1.2.0</version>
  <scope>compile</scope>
</dependency>
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify-compiler-model</artifactId>
  <version>1.2.0</version>
  <scope>compile</scope>
</dependency>

<!-- ONNX importer (f32 "float" precision) -->
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify-import-onnx</artifactId>
  <version>1.2.0</version>
  <scope>compile</scope>
</dependency>

<!-- LightGBM native text importer (f64 "double" precision) -->
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify-import-lightgbm</artifactId>
  <version>1.2.0</version>
  <scope>compile</scope>
</dependency>

<!-- scikit-learn JSON importer (f64 "double" precision) -->
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify-import-scikit</artifactId>
  <version>1.2.0</version>
  <scope>compile</scope>
</dependency>
```

### Compiling a model at runtime

Tree ensemble models (XGBoost, LightGBM, CatBoost, scikit-learn trees) use `OnnxArborist` to produce a `Grove`. Linear models (LogisticRegression, LinearRegression) use `OnnxVintner` to produce a `Vine`. Both are then compiled to bytecode with `Petrify.fossilize()`.

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

### Pre-compiling models at build time with the Maven plugin

The `petrify-maven-plugin` compiles ML models to JVM bytecode during your build. The compiled `.class` files are written to your project's output directory and packaged into your jar automatically.

**Important:** The Maven plugin runs in-process and loads JDK 25 classes. Your Maven process must be running on JDK 25, even if your project targets JDK 17.

#### Plugin configuration

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.exabrial</groupId>
      <artifactId>petrify-maven-plugin</artifactId>
      <version>1.2.0</version>
      <executions>
        <execution>
          <goals>
            <goal>fossilize</goal>
          </goals>
          <configuration>
            <fossils>
              <fossil>
                <modelFile>volcanicRiskClassifier.onnx</modelFile>
                <importer>onnx</importer>
                <modelType>classifier</modelType>
                <targetPackageName>com.example.models</targetPackageName>
              </fossil>
            </fossils>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

Model files are resolved from `src/main/models/` by default.

#### Fossil configuration reference

| Parameter | Required | Description |
|---|---|---|
| `modelFile` | Yes | Filename of the model relative to the model directory |
| `importer` | Yes | `onnx`, `lightgbm`, or `scikit` |
| `modelType` | Yes | `classifier` or `regressor` |
| `targetPackageName` | Yes | Java package for the generated class |
| `targetClassName` | No | Class name stem (defaults to the model filename, PascalCased). `Fossil` is always appended. |
| `modelDirectory` | No | Override the model directory (defaults to `src/main/models/`) |
| `featureNames` | No | Comma-separated feature names in column order. Enables `FeatureMapper` on the compiled `Fossil`. |
| `ignoreFeatureNamesFromModel` | No | If `true`, discard any feature names embedded in the model file. Defaults to `false`. |
| `modelName` | No | Model name metadata. Stored on the compiled `Fossil`. |
| `modelVersion` | No | Model version metadata. Stored on the compiled `Fossil`. |
| `disableEclipseIntegration` | No | Plugin-level parameter (not per-fossil). When `true`, skips the m2e `.classpath` edit and `target/petrify-classes/` dual-write. Defaults to `false`. No effect outside m2e. Also settable as `-Dpetrify.disableEclipseIntegration=true`. |


#### Skipping execution

```bash
mvn compile -Dpetrify.skip=true
```

#### Using the compiled fossil

Your project only needs `petrify-model` as a runtime dependency:

```xml
<dependency>
  <groupId>com.github.exabrial</groupId>
  <artifactId>petrify-model</artifactId>
  <version>1.2.0</version>
  <scope>compile</scope>
</dependency>
```

The generated class is available on the classpath like any other compiled class:

```java
final ClassifierFossil fossil = new VolcanicRiskFossil();
final int prediction = fossil.predict(new float[] { 1.0f, 2.0f, 3.0f, 4.0f });
```

### FeatureMapper

If your model's `Fossil` contains feature name metadata, you can use `FeatureMapper` to convert a `Map<String, Object>` to the positional primitive array your model expects. This avoids hand-maintaining index-aligned arrays that must stay synchronized with the model's training feature order.

```java
// ONNX does not carry feature name metadata, so set it yourself before fossilizing:
grove.metadata = new ModelMetadata();
grove.metadata.featureNames = new String[] { "MedInc", "HouseAge", "AveRooms", "AveBedrms", "Population", "AveOccup", "Latitude", "Longitude" };

// Other importers (LightGBM native, scikit-learn JSON) read feature names from the model file by default.
```

```java
final FeatureMapper mapper = new FeatureMapper(fossil);

final Map<String, Object> features = Map.of(
    "MedInc", 8.3252,
    "HouseAge", 41.0,
    "AveRooms", 6.984,
    "AveBedrms", 1.024,
    "Population", 322.0,
    "AveOccup", 2.556,
    "Latitude", 37.88,
    "Longitude", -122.23
);

final float[] f32 = mapper.mapToF32(features);
final double[] f64 = mapper.mapToF64(features);
```

Null map values are mapped to `NaN` (the ONNX missing-value sentinel), `Number` subtypes are narrowed to the target precision, and `Boolean` values are mapped to `1.0`/`0.0`. Unsupported types throw `FossilUnconformity`.

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

All release artifacts are signed with Jonathan's GPG key (at this time; more committers would be awesome). You can verify signatures using the following public key:

```
Fingerprint: 871638A21A7F2C38066471420306A354336B4F0D
```

To import the key and verify artifacts in your local Maven repository:

```bash
gpg --keyserver keyserver.ubuntu.com --recv-keys 871638A21A7F2C38066471420306A354336B4F0D

find ~/.m2/repository/com/github/exabrial/petrify* -name '*.asc' -exec gpg --verify {} \;
```
