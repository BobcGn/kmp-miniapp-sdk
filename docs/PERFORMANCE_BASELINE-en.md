# Kotlin/JS Bundle Size Baseline

[中文](PERFORMANCE_BASELINE-ch.md)

This document records what the Mini App SDK costs a mini program, how that is measured, and what
would have to change for the measurement to fail a build. It is a baseline, not a performance
guarantee: one machine, one compiler pair, one moment in time.

## What is measured

| Measured | Command | Why |
| --- | --- | --- |
| The SDK's production library distribution, `sdk/build/dist/js/productionLibrary` | `./gradlew verifyMiniAppBundleSize` | This is the SDK's own shipping artifact: the plugin republishes it, and its size is the part of a consumer's bundle the SDK owns |
| The WeChat host distribution, `examples/wechat-miniprogram/miniprogram/libs` | `./gradlew verifyMiniAppBundleSize -PminiappSizeDirectory=examples/wechat-miniprogram/miniprogram/libs` | What today's example host actually loads: the same library plus the hand-written CommonJS normalization |
| A consumer bundle, for example `fixtures/miniapp-consumer/build/miniapp/bundle` | the same command with that path | Shows the SDK's share next to application code; requires the fixture to have been built first |

Nothing else is measured. A repository checkout is not a bundle: `docs/`, `poc/`, `examples/`, the
Gradle scripts and the source tree are not part of what a host loads, and none of them appear in a
report.

## Categories

Every measured file is classified by name, so the same rules work on the SDK's distribution and on a
consumer's bundle.

| Category | Rule | Meaning |
| --- | --- | --- |
| `sdk` | `kmp-miniapp-sdk-*` | The SDK's compiled module and its hand-written CommonJS normalization |
| `runtime` | `kotlin-*`, `kotlinx-*`, `kotlin_*` | The Kotlin, coroutines, atomicfu and DOM-compatibility runtimes the compilation resolved |
| `consumer` | any other `.js` | Application code a consumer compiled |
| `development` | `*.map`, `*.d.ts`, `package.json` | Auxiliary source maps, declarations and module metadata included in the assembled distribution; they are not runtime code, but no packaging exclusion is assumed |

A distribution containing `.wxml`, `.wxss`, `.axml`, `.acss` or `.html` is refused rather than
measured: host markup belongs to the host UI, and counting it as SDK runtime size would be wrong.

## Current baseline

Recorded by `updateMiniAppSizeBaseline`. The machine-readable record is
[`performance-baseline.json`](performance-baseline.json), and it is the authority the task compares
against; the tables below reproduce it.

Measured distribution: `sdk/build/dist/js/productionLibrary`.

| File | Category | Raw bytes | Gzip bytes |
| --- | --- | ---: | ---: |
| `kmp-miniapp-sdk-kotlin.js` | sdk | 396,978 | 49,023 |
| `kmp-miniapp-sdk-kotlin.d.ts` | development | 13,379 | 1,897 |
| `kmp-miniapp-sdk-kotlin.js.map` | development | 154,020 | 42,220 |
| `kotlin-kotlin-stdlib.js` | runtime | 255,917 | 42,144 |
| `kotlin-kotlin-stdlib.js.map` | development | 154,611 | 20,943 |
| `kotlinx-coroutines-core.js` | runtime | 380,535 | 58,655 |
| `kotlinx-coroutines-core.js.map` | development | 167,944 | 61,065 |
| `kotlinx-atomicfu.js` | runtime | 9,372 | 1,431 |
| `kotlinx-atomicfu.js.map` | development | 4,849 | 1,532 |
| `kotlin_org_jetbrains_kotlin_kotlin_dom_api_compat.js` | runtime | 191 | 168 |
| `kotlin_org_jetbrains_kotlin_kotlin_dom_api_compat.js.map` | development | 151 | 133 |
| `package.json` | development | 311 | 175 |

| Category | Files | Raw bytes | Gzip bytes |
| --- | ---: | ---: | ---: |
| sdk | 1 | 396,978 | 49,023 |
| runtime | 4 | 646,015 | 102,398 |
| consumer | 0 | 0 | 0 |
| development | 7 | 495,265 | 127,965 |
| **total** | **12** | **1,538,258** | **279,386** |

What the numbers say, stated plainly:

- The SDK's own code is 397 KB raw and 49 KB gzip. The Kotlin and coroutines runtimes are 646 KB raw
  and 102 KB gzip — **the runtime is about 62% of the bytes a host must load**, and coroutines alone
  is larger than the SDK.
- Source maps and declarations are 495 KB raw, a third of the directory, and none of them ship to a
  host.
- The WeChat host distribution measured the same day: 11 files, 1,581,342 raw / 291,071 gzip — sdk
  408,958 raw / 51,734 gzip (the module plus the hand-written wrapper), runtime 645,824 raw / 102,237
  gzip, development 526,560 raw / 137,100 gzip. It is larger than the library distribution because it
  also carries the wrapper and the wrapper's declaration, and smaller in the runtime direction because
  that path copies only the files the host loads.

## The gzip rule

The gzip figures are a comparison metric, not a delivery size. A real host serves these files through
its own transport, at its own level, with its own options, and possibly after its own transformation.

| Rule | Value |
| --- | --- |
| Algorithm | `java.util.zip.GZIPOutputStream` |
| Level | `Deflater.DEFAULT_COMPRESSION` (`-1`, zlib's default), recorded in the baseline as `gzipLevel` |
| Timestamp | None: the JDK's gzip header carries no modification time, so the same bytes always compress to the same length |
| Aggregation | Each file is compressed on its own and the sizes are summed; a file is never compressed together with another |
| Ordering | Files are sorted by relative path |
| Paths | Relative to the measured directory; no absolute path appears in a report |

The same bytes therefore always produce the same number, and the unit tests assert it. What is *not*
stable is the compiler's output itself — see the limitations below.

## Thresholds

`verifyMiniAppBundleSize` compares a fresh measurement with the committed baseline and fails only
when **both** of these are exceeded, for any category or for the complete assembled distribution:

| Threshold | Value | Basis |
| --- | --- | --- |
| Absolute floor | 4,096 bytes | A few hundred bytes are not a decision anyone can act on in a mini program whose package limit is measured in megabytes; 4 KB is about 0.2% of WeChat's 2 MB per-package limit |
| Relative share | 5% | The smallest shipped category (atomicfu, 9 KB) can double without mattering to a host, so a share alone would fire on noise; the largest (coroutines, 380 KB) can absorb 19 KB before the share trips |

Both must be crossed, so a small file growing a lot and a large file growing a little are both
reported and neither fails. Only growth counts: a smaller bundle never fails. Auxiliary
`development` files participate because they are present in the assembled distribution. The total
is guarded separately so material growth spread across several categories cannot evade the gate.

The rule is deliberately soft. A single measurement on one machine is not a performance guarantee,
and a baseline taken before any optimisation exists cannot honestly claim that 4 KB is the boundary
between acceptable and unacceptable.

**Accepting intentional growth.** When growth is understood and wanted, rewrite the baseline with the
measurement that produced it and review the diff like any other change:

```shell
./gradlew updateMiniAppSizeBaseline
```

The baseline records the commit, the date, the Gradle, Kotlin and SDK versions, and the gzip level, so
a later reader can tell what it describes. A baseline recorded at another gzip level, or for another
directory, is not compared — the task says so instead of producing a meaningless diff.

## Reproducing the report

```shell
./gradlew verifyMiniAppBundleSize
```

It writes `build/reports/miniapp-size/bundle-size.json` and prints every file, every category total
and the comparison. The measurement does not touch the products, needs no network, and reuses the
configuration cache.

## First-load observation

The repository cannot measure how long a mini program takes to start, and nothing here should be read
as if it could. What follows is an observation a person makes once, with WeChat Developer Tools'
own figures.

1. Build the example's distribution: `./gradlew buildMiniAppSdk`.
2. Open `examples/wechat-miniprogram` in WeChat Developer Tools and compile it.
3. Record the debug base library version shown in the tool.
4. Open **Details → Performance** and read **Launch Time** for a cold start (close and reopen the
   simulator between runs, so each figure starts from a cold load).
5. Repeat at least three times and record every value and the median.
6. If the tool offers a package analysis, capture each file's size as it reports them.
7. Record the machine and the base library version next to the numbers.

### Acceptance observation on 2026-09-18

- WeChat Developer Tools: `2.01.2510290 darwin-arm64`
- Debug base library: `3.17.2`
- SDK: `0.1.0-SNAPSHOT`
- Method: automatic hot reload disabled; data/file caches cleared and the project recompiled for
  every run, with no source change or Gradle rebuild between runs
- Three Launch Time readings: `784 ms`, `851 ms`, and `807 ms`
- Median: `807 ms`; mean: approximately `814 ms`; range: `67 ms`
- Result: all three runs printed the SDK version and runtime detection `PASS`; session `Invalid`
  after clearing caches is expected

This is a host cold-start observation in Developer Tools, not isolated SDK time and not a release
environment performance guarantee.

**What the figure means.** WeChat's Launch Time covers the whole mini program: the WeChat runtime
itself, the page's own initialisation, the example's startup calls (storage, login, a network
request, capability queries), its WXML rendering, and only then the SDK's share. It is not an SDK
cost, and subtracting another project's figure from it would not produce one either. The size report
above is the SDK's cost; Launch Time is context.

## Known limitations

- **The compiler's output is not byte-stable.** Rebuilding `jsNodeProductionLibraryDistribution`
  re-emits `kotlinx-coroutines-core.js` and `kotlin-kotlin-stdlib.js` with a few bytes of difference:
  across four consecutive rebuilds the observed spread was 7 bytes on the total gzip figure and up to
  11 bytes on a single file, with raw sizes moving by at most 1 byte. The measurement is deterministic
  for the files it reads — the same directory always produces the same report — but a rebuild moves
  the input slightly. The threshold rule's floors exist partly for this reason.
- The numbers describe one machine with the recorded Kotlin and Gradle versions. Another compiler
  version can move them by more than the drift above.
- Gzip sizes are a comparison metric, not a network transfer size.
- The baseline covers the SDK's distribution, and the host example's is measured alongside it. A
  consumer's bundle is measurable with the same command, but no consumer bundle is committed, so none
  is baselined.
- There is no trend history: the baseline is a single record, replaced in place when it is updated.
- This document makes no claim about runtime performance, memory, or startup cost.
