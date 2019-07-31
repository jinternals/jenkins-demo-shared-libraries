def call(Map pipelineParams) {

    def label = "${pipelineParams.environment}-deployment-${UUID.randomUUID().toString()}"

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
            
    
            stage('Deploy Configuration') {
                try {
                    configVersion = getCurrentTag()
                    currentBuild.displayName = "# ${configVersion} / ${VERSION}"

                    container('kubectl') {
                       def configFile = "${pipelineParams.name}/${pipelineParams.environment}/configuration/application.properties"
                       def configMapName= "${pipelineParams.name}-config-${configVersion}"
                       
                       sh "kubectl create configmap ${configMapName} --from-env-file=${configFile} --namespace=${pipelineParams.environment}  --dry-run -o yaml | kubectl apply -f -"
                       
                    }
                } catch (e) {
                    throw e;
                }
            }
            
            stage('Deploy Application') {
                try {
                    container('kubectl') {
                      
                       def templateParameters = [
                           "configVersion": "${configVersion}", 
                           "appVersion": "${VERSION}", 
                           "environment": "${pipelineParams.environment}"
                       ]
                       def deploymentInputFile = "${pipelineParams.name}/${pipelineParams.environment}/kubernetes/deployment.yaml"
                       def deploymentOutputFile = "${pipelineParams.environment}-deployment.yaml"

                       substituteTemplate(deploymentInputFile, templateParameters, deploymentOutputFile)
                       
                       sh "kubectl apply -f ${deploymentOutputFile} --namespace=${pipelineParams.environment}"

                        
                       def serviceInputFile = "${pipelineParams.name}/${pipelineParams.environment}/kubernetes/service.yaml"
                       def serviceOutputFile = "${pipelineParams.environment}-service.yaml"
                      
                       substituteTemplate(serviceInputFile, templateParameters, serviceOutputFile)
                       
                       sh "kubectl apply -f ${serviceOutputFile} --namespace=${pipelineParams.environment}"
                    }
                } catch (e) {
                    throw e;
                }
            }
            
            stage('Cleanup') {
                 try {
                    container('kubectl') {
                       
                        sh "kubectl get configMaps --namespace=${pipelineParams.environment} --sort-by=.metadata.creationTimestamp -o=custom-columns=:.metadata.name > configMaps"                      
                        def confgiMapsString = readFile "configMaps"
                        
                        def confgiMaps = new String( confgiMapsString ).split( '\n' ).findAll { it.startsWith('micrometer-config') }

                        if(confgiMaps.size > 5)
                        {
                           confgiMaps.dropRight(5).each{ 
                               confgiMap -> sh "kubectl delete configMaps ${confgiMap} --namespace=${pipelineParams.environment}"
                           }
                        }
                        
                    }
                } catch (e) {
                    throw e;
                }
            }

        }
    }

}
