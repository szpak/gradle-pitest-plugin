package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.manager.NoSuchScmProviderException
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepository
import org.apache.maven.scm.repository.ScmRepositoryException

abstract class AbstractChangeLogStrategy implements ChangeLogStrategy {

    protected ScmFileSet fileSet

    protected ScmRepository getRepository(ScmManager manager, String url) {
        try {
            ScmRepository repository = manager.makeScmRepository(url)
            return repository
        } catch (ScmRepositoryException | NoSuchScmProviderException e) {
            throw new ChangeLogException("An error occurred with repository configuration", e)
        }
    }

    protected boolean containsIgnoreCase(Collection<String> elements, String item) {
        return elements.any {
            element -> element.equalsIgnoreCase(item)
        }
    }
}
