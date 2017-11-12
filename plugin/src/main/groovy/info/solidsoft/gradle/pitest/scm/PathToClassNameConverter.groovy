package info.solidsoft.gradle.pitest.scm

import java.util.logging.Logger

class PathToClassNameConverter {
    Set<String> sourceSets

    PathToClassNameConverter(Collection<String> sourceSets) {
        this.sourceSets = new HashSet<>(sourceSets)
    }

    List<String> convertPathNamesToClassName(List<String> pathNames) {
        def result = []
        pathNames.each {
            pathName ->
                def sourceRoot = sourceSets.find { pathName.contains(it)}
                if (sourceRoot && pathName.indexOf(".") != -1) {
                    result.add(transformToCanonicalName(sourceRoot, pathName))
                }
        }
        Logger.getLogger(PathToClassNameConverter.getClass().getName()).info("@@@@@@@@RESULT: $result")
        Logger.getLogger("$pathNames")
        return result
    }

    private String transformToCanonicalName(String sourceSet, String path) {
        def pathWithoutSourceRootPrefix = path.substring(sourceSet.length() + 1, path.length())
        def pathWithoutExtension = pathWithoutSourceRootPrefix.substring(0, pathWithoutSourceRootPrefix.lastIndexOf("."))
        return pathWithoutExtension.replaceAll("/",".")
    }
}
