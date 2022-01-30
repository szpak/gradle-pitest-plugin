# 0.2.9 - 2022-01-30
- Update dependency versions
- Merge upstream changes 9efe3a585bc1307470102147d75fb9a0a29fae72

# 0.2.8 - 2021-08-22
- Update dependency versions
- Fix compatibility with Android Gradle Plugin 7 [#77](https://github.com/koral--/gradle-pitest-plugin/issue/77)

# 0.2.7 - 2021-03-09
- Update dependency versions
- Handle the case where unit test variant does not exist [#75](https://github.com/koral--/gradle-pitest-plugin/issue/75)

# 0.2.6 - 2021-01-31
- Update dependency versions
- Move pitest configuration from root project to subproject [#72](https://github.com/koral--/gradle-pitest-plugin/issue/72)
- Fix integration with Junit5 [pitest-junit5-plugin#53](https://github.com/pitest/pitest-junit5-plugin/issues/53)

# 0.2.5 - 2020-08-19
- Merge upstream changes 16873cb7a67b2b21287ba65c0ac89c7266f2d659
- Add compatibility with Android Gradle Plugin 4 [#64](https://github.com/koral--/gradle-pitest-plugin/issue/64)
- Fix report directory parameter handling [#63](https://github.com/koral--/gradle-pitest-plugin/issue/63)

# 0.2.4 - 2020-05-27
- Fix Gradle 6.4 execution break while trying to change mainClass [#60](https://github.com/koral--/gradle-pitest-plugin/issue/60)

# 0.2.3 - 2020-03-29
- Update resource classpath paths [#57](https://github.com/koral--/gradle-pitest-plugin/issue/57)
- Merge upstream changes a934028bb2efb474f441d818d3de2849de80a3a5
- Update Android Gradle Plugin to 3.6.1
- Update Gradle to 6.3

# 0.2.2 - 2019-09-30
- Merge upstream changes 42c9e3211da4b160695ee4004722a68a59243088 [#53](https://github.com/koral--/gradle-pitest-plugin/issues/53)

# 0.2.1 - 2019-05-04
- Add `excludeMockableAndroidJar` extension property
-- Fix compatibility with Robolectric [#44](https://github.com/koral--/gradle-pitest-plugin/issues/44)
-- Fix compatibility with UnMock [#49](https://github.com/koral--/gradle-pitest-plugin/issues/49)

# 0.2.0 - 2019-05-03
- Android Gradle Plugin dependency updated to 3.4.0
- Gradle updated to 5.4.1
- Fix compatibility with Android Gradle Plugin 3.4 and 3.5 [#48](https://github.com/koral--/gradle-pitest-plugin/issues/48)

# 0.1.9 - 2019-02-02
- Merged upstream changes 7ec26eefcbf6234be892e0a3d0fa88b6b36cec3b
- Android Gradle Plugin dependency updated to 3.3.0

# 0.1.8 - 2018-12-19
- Android Gradle Plugin dependency updated to 3.2.1
- Gradle updated to 5.0
- Change info.solidsoft  package name [#42](https://github.com/koral--/gradle-pitest-plugin/issues/42)
- Fixed missing Android Mockable JAR with AGP 3.2+ [#41](https://github.com/koral--/gradle-pitest-plugin/issues/41)

# 0.1.7 - 2018-08-25
- Merged upstream changes bbca1adac89e6ac00d12303ba2e87ac7068d5d47
- Replaced deprecated FileCollection#add() with #from() - [#37](https://github.com/koral--/gradle-pitest-plugin/issues/37)
- Changed mockable Android JAR classpath entry order - [#30](https://github.com/koral--/gradle-pitest-plugin/issues/30)
- Android Gradle Plugin dependency updated to 3.1.4

# 0.1.6 - 2018-07-22
- Android Gradle Plugin dependency updated to 3.1.2
- Pitest dependency updated to 1.4.0
- Kotlin dependency updated to 1.2.51
- Gradle updated to 4.9
- Merged upstream changes bbca1adac89e6ac00d12303ba2e87ac7068d5d47
- Fixed mockable Android JAR handling when using Android Gradle Plugin 3.2+ - [#29](https://github.com/koral--/gradle-pitest-plugin/issues/29)
- Fixed unit test classpath - [#27](https://github.com/koral--/gradle-pitest-plugin/issues/27)

# 0.1.5 - 2018-01-22
- Fixed classpath order - [#24](https://github.com/koral--/gradle-pitest-plugin/issues/24)

# 0.1.4 - 2017-11-25
- Fixed mockable Android JAR path when using Android Gradle Plugin 3+ - [#20](https://github.com/koral--/gradle-pitest-plugin/pull/20)
- Android Gradle Plugin dependency updated to 3.0.1
- Pitest dependency updated to 1.2.4
- Kotlin dependency updated to 1.1.60
- Gradle updated to 4.3.1

# 0.1.3 - 2017-10-29
- Android Gradle Plugin dependency updated to 3.0.0
- Added workaround for `project` dependency resolution

# 0.1.2 - 2017-10-23
- Fixed compatibility with Android Gradle Plugin 3-rc1
- Fixed compatibility with Kotlin - [#15](https://github.com/koral--/gradle-pitest-plugin/issues/15)

# 0.0.11 - 2017-09-11
- Merged upstream changes 491bf25072ee7ed6a8f10c0618e464586e5ee32a
- Fixed compatibility with Android Gradle Plugin 3+ - [#13](https://github.com/koral--/gradle-pitest-plugin/pull/13)

# 0.0.10 - 2017-09-03
- Fixed `NoSuchMethodError` on Gradle < 4.0 - [#11](https://github.com/koral--/gradle-pitest-plugin/issues/11)

# 0.0.9 - 2017-07-20
 - `CharMatcher` replaced with regular expression to be compatible with Android Gradle Plugin 3.0.0 - [#9](https://github.com/koral--/gradle-pitest-plugin/pull/9)

# 0.0.8 - 2017-05-11
 - Compatibility with Robolectric improved - [#5](https://github.com/koral--/gradle-pitest-plugin/pull/5)
 - Added flavoured app projects support - [#7](https://github.com/koral--/gradle-pitest-plugin/pull/7)

# 0.0.7 - 2017-05-04
 - Android Gradle plugin updated to 2.3.1
 - Robolectric dependency replaced with mockable Android JAR, fixes [#4](https://github.com/koral--/gradle-pitest-plugin/issues/4)

# 0.0.6 - 2017-04-01
 - merged upstream changes d71835784d5ad55b1dbd8eef61cadbd72ec75465
 - Android Gradle plugin updated to 2.3.0
 - Gradle updated to 3.4.1

# 0.0.5 - 2017-02-05
 - Robolectric dependency updated to 7.1.0_r7
 - merged upstream changes 14cf634c98590a9a9719a2284b52c7abef8f8f8f

# 0.0.4 - 2016-10-26
 - Dependencies updated
 - Removed limitation that Android pitest plugin has to be applied after Android Gradle plugin

# 0.0.3 - 2016-07-12
 - Robolectric annotation format error fixed

# 0.0.2 - 2016-07-06
 - Maven central integration added

# 0.0.1 - 2016-07-05
 - Initial release

[Changelog of source project](https://github.com/szpak/gradle-pitest-plugin/blob/master/CHANGELOG.md)
