def call(Map pipelineParams) {

    def label = "jenkins-pull-request-slave-${UUID.randomUUID().toString()}"

    podTemplate(
            label: label,
            cloud: 'jenkins',
            containers: [
                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
                    containerTemplate(name: 'docker', image: 'docker:18.02', ttyEnabled: true, command: 'cat'),
                    containerTemplate(name: 'maven', image: 'maven:3.5-jdk-8', ttyEnabled: true, command: 'cat'),
                    containerTemplate(name: 'git', image: 'alpine/git', ttyEnabled: true, command: 'cat')

            ],
            volumes: [
                    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                    hostPathVolume(hostPath: '/root/.m2', mountPath: '/root/.m2'),
                    hostPathVolume(hostPath: '/root/.sonar', mountPath: '/root/.sonar'),

               
            ]) {

        node(label) {

            stage('Checkout Pull Request') {
                try {
                    currentBuild.displayName = "# PR ${ghprbPullId}"

                    checkout([
                            $class                           : 'GitSCM',
                            branches                         : [[name: "${sha1}"]],
                            doGenerateSubmoduleConfigurations: false,
                            extensions                       : [[$class: 'CloneOption', noTags: false, shallow: false, depth: 0, reference: '']],
                            userRemoteConfigs                : [[refspec      : "+refs/pull/*:refs/remotes/origin/pr/*",
                                                                 credentialsId: "${pipelineParams.gitCredentialId}",
                                                                 url          : "${pipelineParams.gitRepository}"]]
                    ])

                } catch (e) {
                    throw e;
                }
            }

            stage('Build & Test source code') {
                try {
                    container('maven') {
                        sh "mvn clean install"
                    }
                } catch (e) {
                    throw e;
                }
            }

            stage("SonarQube analysis") {
                container('maven') {
                    withCredentials([string(credentialsId: 'sonar', variable: 'TOKEN')]) {
                        withSonarQubeEnv('sonar') {
                            sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.3.0.603:sonar " +
                                    "-f pom.xml " +
                                    "-Dsonar.organization=${pipelineParams.sonarOrganization} " +
                                    "-Dsonar.projectKey=${pipelineParams.sonarProjectKey} " +
                                    "-Dsonar.login=$TOKEN"
                        }
                    }
                }
            }

            stage("Quality Gate") {
                timeout(time: 1, unit: 'HOURS') {
                    def qg = waitForQualityGate()
                    if (qg.status != 'OK') {
                        error "Pipeline aborted due to quality gate failure: ${qg.status}"
                    }
                }
            }


        }
    }

}
