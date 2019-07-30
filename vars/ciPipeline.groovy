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
            
    
            stage('Determine config version') {
                try {
                    configVersion = getCurrentTag()
                    currentBuild.displayName = "# ${configVersion} / ${VERSION}"
                    sh "pwd"
                    container('kubectl') {
                        sh "pwd"
                       sh "kubectl create configmap ${pipelineParams.name}-config-${configVersion} --from-env-file=${pipelineParams.name}/configuration/application.properties  --dry-run -o yaml | kubectl apply -f -"
                       def options = ["configVersion":"${configVersion}", "applicationVersion":"${VERSION}"]
                       template("./${pipelineParams.name}/kubernetes/deployment.yaml",options)
                    }
                } catch (e) {
                    throw e;
                }
            }
            
            stage('Deploy') {
                try {
                  echo "Deploy" 
                } catch (e) {
                    throw e;
                }
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
