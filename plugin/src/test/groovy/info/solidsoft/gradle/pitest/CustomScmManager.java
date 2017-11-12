package info.solidsoft.gradle.pitest;

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.AbstractScmManager;

public class CustomScmManager extends AbstractScmManager {
    @Override
    protected ScmLogger getScmLogger() {
        return null;
    }
}
