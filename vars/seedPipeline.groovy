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
                    secretVolume(secretName: 'jenkins-job-builder', mountPath: '/etc/jenkins_jobs/')
            ]) {

        node(label) {

            stage('Checkout') {
                try {
                    git branch:  "${pipelineParams.gitBranch}", credentialsId: "${pipelineParams.gitCredentialId}", url: "${pipelineParams.gitRepository}"
                } catch (e) {
                    throw e;
                }
            }
            
            stage('Test job configuration') {
                try {
                    sh 'jenkins-jobs test -r ./configuration'
                } catch (e) {
                    throw e;
                }
            }
            
            stage('Test job configuration') {
                try {
                    sh 'jenkins-jobs update --delete-old -r ./configuration'
                } catch (e) {
                    throw e;
                }
            }
            
        }
    }

}
