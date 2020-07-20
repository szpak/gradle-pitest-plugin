package pl.droidsonroids.gradle.pitest

import com.android.builder.testing.MockableJarGenerator
import groovy.transform.CompileDynamic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CompileDynamic
class PitestMockableAndroidJarTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    File inputJar = new File("${project.android.sdkDirectory}/platforms/${project.android.compileSdkVersion}/android.jar")

    @OutputFile
    File getOutputJar() {
        String suffix = project.android.testOptions.unitTests.returnDefaultValues ? "-default-values" : ""
        String outputJarFilename = "pitest-${project.android.compileSdkVersion}${suffix}.jar"
        return new File(project.buildDir, outputJarFilename)
    }

    @TaskAction
    @SuppressWarnings("BuilderMethodWithSideEffects")
    protected void createMockableAndroidJar() {
        if (!outputJar.parentFile.mkdirs() && !outputJar.parentFile.isDirectory()) {
            throw new IOException("Could not create directory at ${outputJar.parentFile}")
        }

        if (outputJar.isFile()) {
            outputJar.delete()
        }

        boolean returnDefaultValues = project.android.testOptions.unitTests.returnDefaultValues
        MockableJarGenerator generator = new MockableJarGenerator(returnDefaultValues)
        generator.createMockableJar(inputJar, outputJar)
    }

}
