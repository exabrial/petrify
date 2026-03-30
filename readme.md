# Petrify

ONNX tree ensemble -> JVM bytecode compiler. Trees go in, fossils (bytecode) come out.

## Overview

Petrify reads a `Grove` (the ONNX-faithful parallel array representation of a tree ensemble) and emits a class at runtime that implements `Fossil.predict(float[])`. 

No interpretation, no array traversal, no pointer chasing, just raw comparisons and conditional jumps. Your Decision Tree is encoded as JVM Bytecode and executes lightning fast.

Once your ONNX models are compiled, the only dependency is the `Fossil` `interfaces` from the `petrify-model` submodule. This provides an entry point into your model.

## Motivation

We needed fast, lightweight tree ensemble inference on the JVM without dragging in a heavyweight runtime or relying on interpretation.

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
    - Giant nest `if/then/else`
    - Runs into 64kb method limits
    - Reads scikit-learn, XGBoost, LightGBM model objects directly, not ONNX
    - Last commit was 2022
    - No runtime dependencies in the generated code, hey nice!

Petrify takes a different approach: compile the tree ensemble directly to JVM bytecode. The result is a plain Java class with no runtime dependencies beyond the `Fossil` interface. No JNI, no native libraries, no interpretation loop, no PMML conversion step.


### License and other boring legal notes

- All files in this project are copyrighted
- The files in `petrify-model` are Apache Source Licensed (ASL2.0)
    - This is done so your models extend from and use ASL2.0 classes at runtime
- All all other files in this project are licensed under EUPL-1.2
    - This license allows you to safely use unmodified/un-extended code in closed-source commercial projects, without revealing your company's proprietary application code in most cases.
    - However: Note that if you modify/extend Petrify, distrute it, and/or offer online access to apps through a modified/extended Petrify, it is required by law that the source code for your Petrify changeset be made available _first_, before offering said access to your app or distribution.
    - Again, this does not include your proprietary application source code, just the changeset to Petrify
- ONNX, XGBoost, LightGBM, scikit-learn, and other names are trademarks; this project is not endorsed by nor affiliated with them
