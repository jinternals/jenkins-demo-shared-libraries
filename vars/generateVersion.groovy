def call(Map opts = [:]) {
    //def version =  (readFile(opts.pom) =~ '<version>(.+)-SNAPSHOT</version>')[0][1]

    def version = '1.0.0'

        // if the repo has no tags this command will fail
        sh "git tag --sort version:refname | tail -1 > version.tmp"

        def tag = readFile 'version.tmp'

        if (tag == null || tag.size() == 0){
            echo "no existing tag found using version ${version}"
            return version
        }

        tag = tag.trim()

        echo "Testing to see if version ${tag} is semver compatible"

        def semver = tag =~ /(?i)\bv?(?<major>0|[1-9]\d*)(?:\.(?<minor>0|[1-9]\d*)(?:\.(?<patch>0|[1-9]\d*))?)?(?:-(?<prerelease>[\da-z\-]+(?:\.[\da-z\-]+)*))?(?:\+(?<build>[\da-z\-]+(?:\.[\da-z\-]+)*))?\b/

        if (semver.matches()) {
            echo "Version ${tag} is semver compatible"

            def majorVersion = semver.group('major') as int
            def minorVersion = (semver.group('minor') ?: 0) as int
            def patchVersion = ((semver.group('patch') ?: 0) as int) + 1

            def newVersion = "${majorVersion}.${minorVersion}.${patchVersion}"
            echo "New version is ${newVersion}"
            return newVersion
        } else {
            echo "Version is not semver compatible"

            // strip the v prefix from the tag so we can use in a maven version number
            def previousReleaseVersion = tag.substring(tag.lastIndexOf('v')+1)
            echo "Previous version found ${previousReleaseVersion}"

            // if there's an int as the version then turn it into a major.minor.micro version
            if (previousReleaseVersion.isNumber()){
                return previousReleaseVersion + '.0.1'
            } else {
                // if previous tag is not a number and doesnt have a '.' version seperator then error until we have one
                if (previousReleaseVersion.lastIndexOf('.') == 0){
                    error "found invalid latest tag [${previousReleaseVersion}] set to major.minor.micro to calculate next release version"
                }
                // increment the release number after the last seperator '.'
                def microVersion = previousReleaseVersion.substring(previousReleaseVersion.lastIndexOf('.')+1) as int
                return previousReleaseVersion.substring(0, previousReleaseVersion.lastIndexOf('.')+1) + (microVersion+1)
            }
        }
}
