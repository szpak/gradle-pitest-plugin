package info.solidsoft.gradle.pitest

import org.gradle.api.Project

class ScmPitestPluginExtension extends PitestPluginExtension {

    private static final NAME = "scmPitest"

    ScmPitestPluginExtension(Project project) {
        super(project)
    }

    @Override
    static String getName() {
        return NAME
    }
}
