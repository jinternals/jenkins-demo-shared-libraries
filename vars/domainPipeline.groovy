def call(Map pipelineParams) {

    def label = "mypod-${UUID.randomUUID().toString()}"

    podTemplate(label: label, containers: [
        containerTemplate(name: 'maven', image: 'maven:3.3.9-jdk-8-alpine', ttyEnabled: true, command: 'cat')
    ]) {

        node(label) {
            stage('Get a Maven project') {
                git 'https://github.com/jenkinsci/kubernetes-plugin.git'
                container('maven') {
                    stage('Build a Maven project') {
                       sh "rm -rf sources"
                       sh "git clone ${pipelineParams.gitUrl} --branch ${pipelineParams.branch} --single-branch sources"
                       sh "cd sources/"
                    }
                }
            }
        }
    }

}
