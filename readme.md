# Petrify

ONNX tree ensemble -> JVM bytecode compiler. Trees go in, fossils come out.

Petrify reads a `Grove` (the ONNX-faithful parallel array representation of a tree ensemble) and emits a class at runtime that implements `Fossil.predict(float[])` — no interpretation, no array traversal, no pointer chasing, just raw comparisons and conditional jumps.

## Glossary

See [docs/glossary.md](docs/glossary.md).

### License and other boring legal notes

- All files in this project are copyrighted
- All files in this project are licensed under EUPL-1.2
    - This license allows you to safely use this code in closed-source commercial projects, without ever having to reveal your company's proprietary application code
    - However: note that if you modify/extend Petrify, and offer online access to apps through a modified/extended Petrify, it is required by law that the source code for your Petrify changeset be made available _first_, before offering said access to your app
    - Again, this does not include your proprietary application source code, just the changeset to Petrify
- ONNX, XGBoost, LightGBM, scikit-learn, and other names are trademarks; this project is not endorsed by nor affiliated with them
