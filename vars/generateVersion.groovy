/**
 * getCommitHash returns the current git commit hash.
 */
def call(Map opts = [:]) {
    def version =  (readFile(opts.pom) =~ '<version>(.+)-SNAPSHOT</version>')[0][1]

    currentBuild.displayName = "# ${version}"

    return version;
}