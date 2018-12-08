#!/usr/bin/groovy

@NonCPS
def generateVersion(Map opts = [:]) {

    def version =  (readFile(opts.pom) =~ '<version>(.+)-SNAPSHOT</version>')[0][1]


    return version;
}