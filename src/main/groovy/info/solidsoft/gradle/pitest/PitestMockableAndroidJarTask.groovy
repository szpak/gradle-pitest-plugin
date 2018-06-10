package info.solidsoft.gradle.pitest

import com.android.builder.testing.MockableJarGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class PitestMockableAndroidJarTask extends DefaultTask {
    @InputFile
    File inputJar = new File("${project.android.sdkDirectory}/platforms/${project.android.compileSdkVersion}/android.jar")

    @OutputFile
    File getOutputJar() {
        def suffix = project.android.testOptions.unitTests.returnDefaultValues ? "-default-values" : ""
        def outputJarFilename = "pitest-${project.android.compileSdkVersion}${suffix}.jar"
        return new File(project.buildDir, outputJarFilename)
    }

    @TaskAction
    protected void createMockableAndroidJar() {
        if (!outputJar.parentFile.mkdirs() && !outputJar.parentFile.isDirectory()) {
            throw new IOException("Could not create directory at ${outputJar.parentFile}")
        }

        if (outputJar.isFile()) {
            outputJar.delete()
        }

        def returnDefaultValues = project.android.testOptions.unitTests.returnDefaultValues
        MockableJarGenerator generator = new MockableJarGenerator(returnDefaultValues)
        generator.createMockableJar(inputJar, outputJar)
    }
}
