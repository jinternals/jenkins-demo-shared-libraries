def call(Map pipelineParams) {

    def label = "jenkins-job-builder-slave-${UUID.randomUUID().toString()}"

    podTemplate(
            label: label,
            cloud: 'jenkins',
            containers: [
                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
                    containerTemplate(name: 'jenkins-job-builder', image: 'jinternals/jenkins-job-builder', ttyEnabled: true, command: 'cat')
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

        }
    }

}
