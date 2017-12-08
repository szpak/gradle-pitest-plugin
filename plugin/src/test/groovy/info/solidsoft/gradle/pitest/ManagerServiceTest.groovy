package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.scm.ManagerService
import org.apache.commons.lang.RandomStringUtils
import org.apache.maven.scm.manager.ScmManager
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.exporter.ZipExporter
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ManagerServiceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test
    void returnsCorrectImplementationTest() {
        def name = "test-archive-${RandomStringUtils.randomAlphanumeric(7)}.jar"
        def file = temporaryFolder.newFile(name)
        def archive = ShrinkWrap.create(JavaArchive.class, name).addAsServiceProvider(ScmManager.class, CustomScmManager.class)
        archive.as(ZipExporter.class).exportTo(file, true)
        ManagerService service = ManagerService.getInstance(new URLClassLoader(file.toURI().toURL()))
        Assert.assertEquals(CustomScmManager.class, service.getManager().class)
    }

    @Test
    void shouldReturnDefaultIfNotSpecified() {
        ManagerService service = ManagerService.getInstance(this.class.classLoader)
        Assert.assertEquals(service.getDefaultManager().class, service.getManager().class)
    }

    @Test
    void shouldReturnDefaultIfNotOnClasspath() {
        def archiveName = "test-archive-${RandomStringUtils.randomAlphanumeric(7)}.jar"
        def file = temporaryFolder.newFile(archiveName)
        ShrinkWrap.create(JavaArchive.class, archiveName)
            .addAsServiceProvider(ScmManager.class, CustomScmManager.class)
            .as(ZipExporter.class)
            .exportTo(file, true)
        ManagerService service = ManagerService.getInstance(new URLClassLoader(file.toURI().toURL()))
        Assert.assertEquals(CustomScmManager.class, service.getManager().class)
    }

}
