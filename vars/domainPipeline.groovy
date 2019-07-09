def call(Map pipelineParams) {

    def label = "jenkins-slave-${UUID.randomUUID().toString()}"

    podTemplate(label: label, containers: [
            containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
            containerTemplate(name: 'maven', image: 'maven:3.5-jdk-8', ttyEnabled: true, command: 'cat')
    ]) {

        node(label) {
             stage('Get a Maven project') {
                git "${pipelineParams.gitUrl}"
                generateVersion(pom: 'pom.xml')
                container('maven') {
                    stage('Build a Maven project') {
                        sh 'mvn -B clean install'
                    }
                }
            }
        }
    }

}
