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

            stage('Deploy') {
                       checkout([$class: 'GitSCM', 
                         branches: [[name: '${ghprbPullId}']], 
                         doGenerateSubmoduleConfigurations: false, 
                         extensions: [], 
                         submoduleCfg: [], 
                         userRemoteConfigs: [
                             [
                                 credentialsId: 'github',
                                 name: 'origin',
                                 refspec: "+refs/pull/${ghprbPullId}/*:refs/remotes/origin/pr/${ghprbPullId}/*", 
                                 url: 'https://github.com/jinternals/spring-micrometer-demo.git'
                             ]
                         ]
                        ])
            }

        }
    }

}
