package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.manager.BasicScmManager
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider

import java.util.logging.Logger


class ManagerService {
    private static final Logger LOG = Logger.getLogger(ManagerService.class.name)
    private static ServiceLoader<ScmManager> loader

    private ManagerService(ClassLoader classLoader) {
        loader = ServiceLoader.load(ScmManager, classLoader)
    }

    static ManagerService getInstance(ClassLoader classLoader) {
        return new ManagerService(classLoader)
    }

    ScmManager getManager() {
        for (ScmManager manager : loader) {
            LOG.info("Got: ${manager.getClass().getName()}")
            return manager
        }
        return getDefaultManager()
    }

    ScmManager getDefaultManager() {
        ScmManager manager = new BasicScmManager()
        manager.setScmProvider("git", new GitExeScmProvider())
        return manager
    }
}
