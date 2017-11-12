package pitest.test.scm;

import org.apache.maven.scm.*;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.AbstractScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class LastCommitManager extends AbstractScmManager {

    private static final String FILENAME = "src/main/java/pitest/test/scm/SampleClass.java";

    @Override
    public ChangeLogScmResult changeLog(ChangeLogScmRequest scmRequest) throws ScmException {
        ChangeFile changedFile = new ChangeFile(FILENAME);
        changedFile.setAction(ScmFileStatus.ADDED);
        ChangeSet changeSet = new ChangeSet();
        changeSet.setFiles(Collections.singletonList(changedFile));
        ChangeLogSet changeLogSet = new ChangeLogSet(Collections.singletonList(changeSet), new Date(), new Date());
        return new ChangeLogScmResult(changeLogSet, new ScmResult(null,null,null,true));
    }

    @Override
    public List<String> validateScmRepository(String scmUrl) {
        return Collections.emptyList();
    }

    @Override
    public ScmRepository makeScmRepository(String scmUrl) throws ScmRepositoryException, NoSuchScmProviderException {
        Logger.getLogger(LastCommitManager.class.getName()).info("@@@@@@@MAKING_REPO@@@@@@@@@");
        return null;
    }

    @Override
    protected ScmLogger getScmLogger() {
        return new DefaultLog();
    }
}
