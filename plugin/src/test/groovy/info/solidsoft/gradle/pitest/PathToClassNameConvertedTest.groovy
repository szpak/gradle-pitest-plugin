package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.scm.PathToClassNameConverter
import spock.lang.Shared
import spock.lang.Specification

class PathToClassNameConvertedTest extends Specification {
    @Shared
    def sourceRoot = "/some/body/once/told/me"
    def converter = new PathToClassNameConverter(Collections.singletonList(sourceRoot))

    def "should return empty collection on fileNames not starting with root" () {
        given:
            def words = ["hello.com","world.com","how.net","are.java","you.groovy"]
        when:
            def result = converter.convertPathNamesToClassName(words)
        then:
            result.isEmpty()
    }

    def "should return empty collection on file names without extension" () {
        given:
            def words = ["$sourceRoot/NoExtension","$sourceRoot/NoSuffix","$sourceRoot/SuffixLess"]
        when:
            def result = converter.convertPathNamesToClassName(words)
        then:
            result.isEmpty()
    }

    def "should convert correctly ('#expectedResult')" () {
        when:
            def result = converter.convertPathNamesToClassName(words)
        then:
            result == expectedResult
        where:
            words | expectedResult
            ["$sourceRoot/org/sample/SampleCalculator.java"] | ["org.sample.SampleCalculator"]
            ["$sourceRoot/oh/mein/gott/KarelGott.groovy",
            "$sourceRoot/the/lurd/of/the/rings/Gandalf.java"] | ["oh.mein.gott.KarelGott","the.lurd.of.the.rings.Gandalf"]
    }
}
