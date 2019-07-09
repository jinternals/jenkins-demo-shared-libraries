/**
 * getCommitHash returns the current git commit hash.
 */
def call(Map opts = [:]) {
    //def version =  (readFile(opts.pom) =~ '<version>(.+)-SNAPSHOT</version>')[0][1]

     def version = '1.0.0'

     try {
       // if the repo has no tags this command will fail
         sh "git tag  -l --merged master --sort='-*authordate' | head -n1 > version.tmp"
         def previousVersion = readFile 'version.tmp'
         echo 'found previous tagged version ' + previousVersion
         
         def microVersion = previousVersion.substring(previousVersion.lastIndexOf('.')+1) as int
         version = previousVersion.substring(0, previousVersion.lastIndexOf('.')+1) + (microVersion+1)
      
         echo 'using new version ' +  version
     } catch (err) {
       echo "no existing tag found or issue understanding existing tags so using ${version}"
     }

    currentBuild.displayName = "# ${version}"

    return version

}
