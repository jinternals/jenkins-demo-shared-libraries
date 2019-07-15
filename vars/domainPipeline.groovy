def call(Map pipelineParams) {

    def label = "jenkins-domain-build-${UUID.randomUUID().toString()}"

    podTemplate(
            label: label,
            cloud: 'jenkins',
            containers: [
                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
                    containerTemplate(name: 'docker', image: 'docker:18.02', ttyEnabled: true, command: 'cat'),
                    containerTemplate(name: 'maven', image: 'maven:3.5-jdk-8', ttyEnabled: true, command: 'cat')
            ],
            volumes: [
                    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
                    hostPathVolume(hostPath: '/root/.m2', mountPath: '/root/.m2')
            ]) {

        node(label) {

            stage('Checkout') {
                try {
                    git branch:  "${pipelineParams.gitBranch}", credentialsId: "${pipelineParams.gitCredentialId}", url: "${pipelineParams.gitRepository}"
                } catch (e) {
                    throw e;
                }
            }

            stage('Determine Version') {
                try {
                    versionNumber = generateVersion(pom: 'pom.xml')
                    currentBuild.displayName = "# ${versionNumber}"
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "${pipelineParams.gitCredentialId}",
                                      usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {

                        sh "git config credential.username ${env.GIT_USERNAME}"
                        sh "git config credential.helper '!f() { echo password=\$GIT_PASSWORD; }; f'"
                        sh "git tag ${versionNumber}"
                        sh "GIT_ASKPASS=true git push origin ${versionNumber}"
                    }
                } catch (e) {
                    throw e;
                }
            }

            stage('Build Artifacts') {
                container('maven') {
                    sh "mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${versionNumber}"
                    sh "mvn clean package"
                }
            }

            stage('Publish Artifacts') {
                container('maven') {
                    //sh "mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false --fail-at-end"
                }
            }

            stage ('Docker build and push') {
                container ('docker') {
                    def repository = "${pipelineParams.dockerRepository}"

                     withDockerRegistry(credentialsId: "${pipelineParams.dockerCredentialId}", url: "${pipelineParams.dockerRegistry}") {
                        sh "docker build -t ${repository}:${versionNumber} -t ${repository}:latest  -f target/docker-resources/Dockerfile target/"
                        sh "docker push ${repository}"
                    }
                }
            }
        }
    }

}
