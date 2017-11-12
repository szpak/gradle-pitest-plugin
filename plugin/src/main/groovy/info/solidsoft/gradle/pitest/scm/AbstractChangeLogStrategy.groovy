package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.manager.NoSuchScmProviderException
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepository
import org.apache.maven.scm.repository.ScmRepositoryException

abstract class AbstractChangeLogStrategy implements ChangeLogStrategy {

    protected ScmFileSet fileSet

    protected void validateUrl(ScmManager manager, String url) {
        def errorMessages = manager.validateScmRepository(url)
        if (!errorMessages.isEmpty()) {
            throw new ChangeLogException("Error when validating url: $url, $errorMessages")
        }
    }

    protected boolean containsIgnoreCase(Collection<String> elements, String item) {
        return elements.any {
            element -> element.equalsIgnoreCase(item)
        }
    }
}
