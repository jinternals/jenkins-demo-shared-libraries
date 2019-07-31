def call(Map pipelineParams) {

    def label = "jenkins-pull-request-slave-${UUID.randomUUID().toString()}"

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
            
            stage('Checkout Configuration') {
                try {
                    currentBuild.displayName = "# PR ${ghprbPullId}"

                    checkout([$class: 'GitSCM',
                              branches: [[name: "FETCH_HEAD"]],
                              doGenerateSubmoduleConfigurations: false,
                              extensions: [[$class: 'LocalBranch'], [$class: 'RelativeTargetDirectory', relativeTargetDir: "jinternals"]],
                              userRemoteConfigs: [[refspec: "+refs/pull/${ghprbPullId}/head:refs/remotes/origin/PR-${ghprbPullId} +refs/heads/master:refs/remotes/origin/master",
                                                   credentialsId:  "${pipelineParams.gitCredentialId}",
                                                   url: "${pipelineParams.gitRepository}]]
                    ])
                                                  
                } catch (e) {
                    throw e;
                }
            }

        }
    }

}
