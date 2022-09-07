def call(Map pipelineParams) {

    def label = "jenkins-job-builder-slave-${UUID.randomUUID().toString()}"

    podTemplate(
            label: label,
            cloud: 'jenkins',
            containers: [
                    containerTemplate(name: 'jnlp', image: 'jenkins/inbound-agent:latest', args: '${computer.jnlpmac} ${computer.name}'),
                    containerTemplate(name: 'jenkins-job-builder', image: 'homelabs.jfrog.io/homelabs-docker/jenkins-job-builder:latest', ttyEnabled: true, command: 'cat')
            ],
            volumes: [
                    secretVolume(secretName: 'jenkins-job-builder', mountPath: '/etc/jenkins_jobs/')
            ]) {

        node(label) {

            stage('Checkout') {
                try {
                    git branch: "${pipelineParams.gitBranch}", credentialsId: "${pipelineParams.gitCredentialId}", url: "${pipelineParams.gitRepository}"
                } catch (e) {
                    throw e;
                }
            }

            stage('Test jenkins jobs') {
                container('jenkins-job-builder') {
                    try {
                        sh 'jenkins-jobs test -r ./configuration'
                    } catch (e) {
                        throw e;
                    }
                }
            }

            stage('Update jenkins jobs') {
                container('jenkins-job-builder') {
                    try {
                        sh 'jenkins-jobs update --delete-old -r ./configuration'
                    } catch (e) {
                        throw e;
                    }
                }
            }

        }
    }

}
