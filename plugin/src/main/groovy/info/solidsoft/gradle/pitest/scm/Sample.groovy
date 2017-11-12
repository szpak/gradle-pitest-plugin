package info.solidsoft.gradle.pitest.scm

import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.manager.BasicScmManager
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider


def manager = new BasicScmManager()
def root = new File("/home/spriadka/Documents/School/gradle-pitest-plugin/")
manager.setScmProvider("git", new GitExeScmProvider())
def scmRoot = new ScmFileSet(root)
def sourcesRoot = "/home/spriadka/Documents/School/gradle-pitest-plugin/plugin/src/main/groovy"
def repository = manager.makeScmRepository("scm:git:git@github.com/szpak/gradle-pitest-plugin")
def result = manager.status(repository, scmRoot)
result.changedFiles.each {
    file ->
        def modified = "$scmRoot.basedir.absolutePath/$file.path"
        def withExtension =  modified.substring(sourcesRoot.length() + 1, modified.length())
        println withExtension.substring(0, withExtension.lastIndexOf(".")).replaceAll("/",".")
}
