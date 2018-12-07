/**
 * getCommitHash returns the current git commit hash.
 */
def call(Map opts = [:]) {
    sh "git rev-parse HEAD"
}