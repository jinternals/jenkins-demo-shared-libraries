def call(Map pipelineParams) {

    def label = "${pipelineParams.name}-library-build-${UUID.randomUUID().toString()}"

    podTemplate(
            label: label,
            cloud: 'jenkins',
            containers: [
                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
                    containerTemplate(name: 'docker', image: 'docker:18.02', ttyEnabled: true, command: 'cat'),
                    containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.15.1', command: 'cat', ttyEnabled: true)
            ],
            volumes: [
                    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
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
                    def versionNumber = generateVersion(pom: 'pom.xml')
                    currentBuild.displayName = "# ${versionNumber}"
                    createGitTag(pipelineParams,versionNumber)
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
                       sh "Publish Artifacts"
            }
        }
    }

}
