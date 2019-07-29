def call(Map pipelineParams) {

    def label = "ci-deployment-${UUID.randomUUID().toString()}"

    podTemplate(
            label: label,
            cloud: 'jenkins',
            containers: [
                    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
                    containerTemplate(name: 'docker', image: 'docker:18.02', ttyEnabled: true, command: 'cat')
            ],
            volumes: [
                    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
            ]) {

        node(label) {

            stage('Configure Environment') {
                       sh "echo Configuring Environment"
            }
            
            stage('Deploy') {
                       sh "echo Deploy"
            }
            
            stage('Verify Deployment') {
                       sh "echo Verfiy Deployment"
            }

            stage('Promote to Staging') {
                       sh "echo Promoting to Staging"
            }
        }
    }

}
