# ABACUS PLUGIN

A gradle plugin that provides tasks to count and plot the number of files, filtered by extension or by class/interface inheritance.

## Usage
Take a look at the [demo project](https://github.com/alecarnevale/abacus-plugin-demo) for a sample.

### Configuration
- if you want to count how many java or kotlin class extends or implements a file, set the `supertypes`;
- if you want to count the number of files for a given extension, set `fileExtensions` property;
    - between these, if only file inside specific folder must be count, set `fileFolders` property.

```
abacus {
  val cls = listOf(<class or interface name>)
  val exts = listOf(<file extension>)
  val folders = listOf(<folder's name>)
  supertypes.set(cls)
  fileExtensions.set(exts)
  fileFolders.set(folders)
}
```

### Tasks
_abacus_ plugin registers the task `abacus` at root project level.

You can:
1. run it to start counting only for the current source code
2. instruct abacus plugin to switch between tags and count for each of them

To start counting for each tag:
- provide a list of tags in a file (one per row) and set the path for `tagsFilePath` property;
- otherwise, abacus plugin provides you a task `abacusTags` that lists every tag of the project inside a file in `build/abacus/tags.txt`.

Note:
- `build/abacus/tags.txt` is also used as fallback, if no `tagsFilePath`;
- if empty, `abacus` task start count only for the current source code.

## Outputs
- task `abacusTags` generates a list of ordered tags inside `build/abacus/tags.txt`;
- task `abacus` generates a csv file `build/abacus/output.csv`;
- if more tags has been analyzed (`tags.txt` not empty) it also plots a graph and a table in `build/abacus/plot.html`.

## Installation
Abacus plugin is available in the [gradle plugin portal](https://plugins.gradle.org/plugin/io.github.alecarnevale.abacus).

```
[file: build.gradle.kts]

plugins {
  id("io.github.alecarnevale.abacus") version "1.0.0"
}
```

### Dependencies
- add https://jitpack.io maven repository;
- add `com.github.kotlinx:ast:0.1.0` in the classpath
```
[file: build.gradle.kts]

buildscript {
  repositories {
    maven("https://jitpack.io")
  }
  dependencies {
    classpath("com.github.kotlinx:ast:0.1.0")
  }
}
```

## Thanks to
- kotlinx.ast https://github.com/kotlinx/ast
- JavaParser https://github.com/javaparser/javaparser
- Simple Git Gradle Plugin https://github.com/rcw3bb/simple-git
- Plotly.kt https://github.com/SciProgCentre/plotly.kt