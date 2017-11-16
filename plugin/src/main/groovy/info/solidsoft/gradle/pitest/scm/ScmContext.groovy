package info.solidsoft.gradle.pitest.scm

class ScmContext {

    File scmRoot
    String startVersion
    String startVersionType
    String endVersion
    String endVersionType

    private ScmContext(Builder builder) {
        this.scmRoot = builder.scmRoot
        this.startVersionType = builder.startVersionType
        this.startVersion = builder.startVersion
        this.endVersionType = builder.endVersionType
        this.endVersion = builder.endVersion
    }

    static class Builder {
        File scmRoot
        String startVersion
        String startVersionType
        String endVersion
        String endVersionType

        Builder scmRoot(String scmRootPath) {
            this.scmRoot = new File(scmRootPath)
            return this
        }

        Builder scmRoot(File scmRoot) {
            this.scmRoot = scmRoot
            return this
        }

        Builder startVersion(String startVersion) {
            this.startVersion = startVersion
            return this
        }

        Builder startVersionType(String startVersionType) {
            this.startVersionType = startVersionType
            return this
        }

        Builder endVersion(String endVersion) {
            this.endVersion = endVersion
            return this
        }

        Builder endVersionType(String endVersionType) {
            this.endVersionType = endVersionType
            return this
        }

        ScmContext build() {
            return new ScmContext(this)
        }
    }
}
