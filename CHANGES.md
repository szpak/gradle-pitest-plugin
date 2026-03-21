# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [1.20.0] - 2026-03-21

Gradle 9.x and JDK 25 Compatibility

### Added
- Gradle 9.4.1 support with zero deprecation warnings
- JDK 25 (class file version 69) compatibility
- Gradle 9.0.0–9.4.1 to functional test matrix (`GRADLE9_VERSIONS`)
- JDK 25 entry in `MINIMAL_GRADLE_VERSION_FOR_JAVA_VERSION` map
- `gradlePlugin.testSourceSets` registration for funcTest source set (Gradle 9.4 `compileOnlyApi` change)
- Explicit `testImplementation gradleApi()` for unit tests (Gradle 9.4 scope change)
- `junit:junit:4.13.2` explicit dependency for nebula-test 12.x compatibility
- `ignoreDeprecations` system property for funcTest (third-party plugin warnings)

### Changed
- Gradle wrapper 8.14.3 → 9.4.1
- `sourceCompatibility` 1.8 → 17 (aligned with minimum Java requirement)
- `PitestTask` class is now `abstract` (required by Groovy 4 for JavaExec's abstract `@Inject` methods)
- `setupReportDirInExtensionWithProblematicTypeForGradle5()` renamed to `setupDefaultReportDir()` — uses lazy `baseDirectory.dir()` instead of eager `.asFile.get()`
- `PitestAggregatorPlugin.getReportBaseDirectory()` returns `Provider<Directory>` instead of `File` — fully lazy evaluation
- `launchClasspath.setFrom` now calls `.get()` on `NamedDomainObjectProvider` (Groovy 4 stricter type coercion)
- PIT version string comparison uses `GradleVersion.version()` comparator instead of lexicographic ordering
- `afterSuite` Closure replaced with `TestListener` interface (Closure API deprecated in Gradle 9.4)
- `GRADLE8_VERSIONS` decoupled from `LATEST_KNOWN_GRADLE_VERSION` (was incorrectly placing 9.x in 8.x list)
- `GRADLE9_VERSIONS` added to `"full"` regression test mode
- Deduplicated `GRADLE7_VERSIONS` list
- Default PIT version 1.22.0 → 1.23.0 (`DEFAULT_PITEST_VERSION` + `pitestAggregatorVersion`)
- `.editorconfig` extended with Groovy/Java max line length, YAML/XML indent, Kotlin, Markdown, Makefile rules

### Changed — Dependencies
- `org.spockframework:spock-core` 2.4-groovy-3.0 → 2.4-groovy-4.0
- `com.netflix.nebula:nebula-test` 10.6.2 → 12.0.0 (Gradle 9 baseline)
- `com.gradle.publish:plugin-publish-plugin` 2.0.0 → 2.1.1
- `net.bytebuddy:byte-buddy` 1.18.4 → 1.18.7
- `org.junit.platform:junit-platform-launcher` (managed) → 6.0.3
- Groovy exclude group `org.codehaus.groovy` → `org.apache.groovy` (Groovy 4 package rename)
- `gradlePluginPortal()` added to repositories (nebula-test 12.x)

### Changed — Test Projects
- `junit5kotlin`: Kotlin 2.0.21 → 2.1.20, `sourceCompatibility` 1.8 → 17, `jvmTarget` 1.8 → 17
- `junit5spock2`: `spock-core` 2.4-M6-groovy-3.0 → 2.4-groovy-4.0
- `multiproject`: removed deprecated cross-project `mutableCodeBase.extendsFrom()` configuration access

### Removed
- `Configuration.visible = false` from `PitestPlugin` and `PitestAggregatorPlugin` (deprecated in Gradle 9.1, no effect since 9.0)
- `@CompileDynamic` annotation from production code (`PitestPlugin.setupDefaultReportDir`)
- `import groovy.transform.CompileDynamic` from `PitestPlugin`
- Unused `forceTaskCreation()` method from `PitestPluginTest`
- Unused `GradleException` import from `PitestPluginTest`

### Fixed
- `PitestPluginTest` buildscript configuration test adapted for Gradle 9 (immutable `buildscript.configurations`)
- Functional tests skip PIT versions < 1.19.0 on JDK 25+ (ASM 9.7 does not support class file version 69)
- `RegularFileProperty` functional test skipped on JDK 25+ (PIT internal error with `historyInputLocation`)
- `InvalidUserCodeException` functional test uses current Gradle version instead of hardcoded 8.14.1 (incompatible with JDK 25)

### Compatibility Notes
- **Minimum Java:** 17 (was 8 — aligned with Gradle 9 requirement and plugin's existing runtime minimum)
- **Minimum Gradle:** 8.4 (unchanged)
- **Tested Gradle:** 6.5 through 9.4.1
- **Groovy 4:** Plugin compiled with Gradle 9 (Groovy 4.0.29) requires Gradle ≥ 7.0 at runtime (hard constraint from Groovy 4 bytecode)
- **nebula-test 12.0.0:** Not yet published to Maven Central — must be built from source with Spock 2.x `testMethodName` patch for functional tests

[Unreleased]: https://github.com/dantte-lp/gradle-pitest-plugin/compare/v1.20.0...HEAD
[1.20.0]: https://github.com/dantte-lp/gradle-pitest-plugin/releases/tag/v1.20.0
