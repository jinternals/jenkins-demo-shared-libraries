def call(Map pipelineParams) {

    def label = "jenkins-slave-${UUID.randomUUID().toString()}"

    podTemplate(label: label, containers: [
            containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
            containerTemplate(name: 'maven', image: 'maven:3.5-jdk-8', ttyEnabled: true, command: 'cat')], 
    volumes:[
        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
        hostPathVolume(hostPath: '/root/.m2', mountPath: '/root/.m2')
    ]) {

        node(label) {
             stage('Checkout') {
                git credentialsId: 'github', url: "${pipelineParams.gitUrl}"
             } 
            stage('Determine Version') {
                versionNumber = generateVersion(pom: 'pom.xml')
                 
                echo "Generated new tag ${versionNumber}"
                
                 try {
                      withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'github', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
                        sh "git config credential.username ${env.GIT_USERNAME}"
                        sh "git config credential.helper '!f() { echo password=\$GIT_PASSWORD; }; f'"
                        sh "git tag ${versionNumber}"
                        sh "GIT_ASKPASS=true git push origin ${versionNumber}"
                      }
                 } 
                 catch (Exception err) {
                    currentBuild.result = 'FAILURE'
                 }
                 finally {
                    sh "git config --unset credential.username"
                    sh "git config --unset credential.helper"
                 }
                 
                container('maven') {
                    stage('Build a Maven project') {
                        sh 'mvn -B clean install'
                    }
                }
            }
        }
    }

}
