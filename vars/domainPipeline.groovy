def call(Map pipelineParams) {

    def label = "jenkins-slave-${UUID.randomUUID().toString()}"

    podTemplate(label: label, containers: [
            containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
            containerTemplate(name: 'maven', image: 'maven:3.5-jdk-8', ttyEnabled: true, command: 'cat')
    ]) {

        node(label) {
             stage('Checkout') {
                git credentialsId: 'github', url: "${pipelineParams.gitUrl}"
                 
                versionNumber = generateVersion(pom: 'pom.xml')
                 
                echo "Generated new tag ${versionNumber}"
                
                sh "git tag ${versionNumber}"
                 
                sh "git push origin ${versionNumber}"

                 
                container('maven') {
                    stage('Build a Maven project') {
                        sh 'mvn -B clean install'
                    }
                }
            }
        }
    }

}
