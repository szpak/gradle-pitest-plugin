package info.solidsoft.gradle.pitest.scm

class PathToClassNameConverter {

    Set<String> sourceSetsPaths

    PathToClassNameConverter(Collection<String> sourceSets) {
        this.sourceSetsPaths = new HashSet<>(sourceSets)
    }

    List<String> convertPathNamesToClassName(List<String> pathNames) {
        def result = []
        pathNames.each {
            pathName ->
                def sourceRoot = sourceSetsPaths.find { pathName.contains(it)}
                if (sourceRoot && pathName.indexOf(".") != -1) {
                    result.add(transformToCanonicalName(sourceRoot, pathName))
                }
        }
        return result
    }

    private static String transformToCanonicalName(String sourceSet, String path) {
        def pathWithoutSourceRootPrefix = path.substring(sourceSet.length() + 1, path.length())
        def pathWithoutExtension = pathWithoutSourceRootPrefix.substring(0, pathWithoutSourceRootPrefix.lastIndexOf("."))
        return pathWithoutExtension.replaceAll("/",".")
    }
}
