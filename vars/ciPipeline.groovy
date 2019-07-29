def call(Map pipelineParams) {

    def label = "ci-deployment-${UUID.randomUUID().toString()}"

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
            
           stage('Checkout Configuration') {
                try {
                    git branch:  "${pipelineParams.configBranch}", credentialsId: "${pipelineParams.configCredentialId}", url: "${pipelineParams.configRepository}"
                } catch (e) {
                    throw e;
                }
            }

            stage('Configure Environment') {
              container('kubectl') {
                  configVersion = getCurrentTag()
                  sh "kubectl create configmap ${pipelineParams.name}-${configVersion} --from-env-file=${pipelineParams.name}/application.properties  --dry-run -o yaml | kubectl apply -f -"
              }
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
