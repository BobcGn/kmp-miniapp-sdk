---
translation-of: submodule-gradle-toolchain
language: en
status: draft
title: gradle · Wrapper, versions and dependency catalog
---

# Wrapper, versions and dependency catalog

[中文](SPEC-ch.md)

## Responsibility and input surface

`wrapper/gradle-wrapper.properties` and the checked-in Wrapper pin the root Gradle distribution. `libs.versions.toml` centralizes Kotlin, coroutines, publishing plugins and the shared miniapp version. These executable build inputs are not replaced by IDE state or a roadmap.

The SDK version constant and plugin runtime coordinate are generated from the catalog to avoid duplicated literals. The consumer plugin compiles against KGP with compileOnly, supplied by consumers at runtime, not an SDK runtime dependency.

## Why version inputs have one source

[libs.versions.toml](libs.versions.toml) drives publication version, runtime's generated constant and the plugin's runtime coordinate. Separate literals could compile yet give consumers mismatched dependencies/reports; generated relationships constrain drift better than manual synchronization.

The Wrapper fixes a build entry, not every consumer's project version. Independent consumers can pin experiment/released-dependency inputs; unifying them for tidy root numbers would destroy evidence identity.

## Boundary and version scope

The root build pins no JDK toolchain. A documented verification JDK is not a new hard toolchain requirement. Use `./gradlew` or `gradlew.bat`, not system Gradle as authority.

Independent demos and the released consumer have their own Wrappers/catalogs; reproducibility pins do not automatically upgrade with the root catalog. Matching versions do not make composite-build and public-dependency evidence interchangeable.

## Verification

`./gradlew --version` identifies the actual Wrapper; `./gradlew projects` identifies the root model. Version changes also require relevant compilation/tests/publication-shape checks; declarations or directories alone prove no compatibility.
