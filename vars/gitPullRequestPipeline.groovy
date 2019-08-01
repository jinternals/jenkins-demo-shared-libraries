def call(Map pipelineParams) {

    def label = "jenkins-pull-request-slave-${UUID.randomUUID().toString()}"

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
            
            stage('Checkout Pull Request') {
                try {
                    currentBuild.displayName = "# PR ${ghprbPullId}"

                   /* checkout([$class: 'GitSCM',
                              branches: [[name: "FETCH_HEAD"]],
                              doGenerateSubmoduleConfigurations: false,
                              extensions: [[$class: 'LocalBranch'], [$class: 'RelativeTargetDirectory', relativeTargetDir: "jinternals"]],
                              userRemoteConfigs: [[refspec: "+refs/pull/${ghprbPullId}/head:refs/remotes/origin/PR-${ghprbPullId} +refs/heads/master:refs/remotes/origin/master",
                                                   credentialsId:  "${pipelineParams.gitCredentialId}",
                                                   url: "${pipelineParams.gitRepository}"]]
                    ])*/
                    
                     checkout([
                              $class: 'GitSCM',
                              branches: [[name: "${sha1}"]],
                              doGenerateSubmoduleConfigurations: false,
                              extensions: [[$class: 'CloneOption', noTags: false, shallow: false, depth: 0, reference: '']],
                              userRemoteConfigs: [[refspec: "+refs/pull/*:refs/remotes/origin/pr/*",
                                                   credentialsId:  "${pipelineParams.gitCredentialId}",
                                                   url: "${pipelineParams.gitRepository}"]]
                       ])

                    
                    if (env.CHANGE_ID) {
                        for (file in pullRequest.files) {
                           println "${file.filename}"
                        }
                    }
                   
                                                  
                } catch (e) {
                    throw e;
                }
            }

        }
    }

}
