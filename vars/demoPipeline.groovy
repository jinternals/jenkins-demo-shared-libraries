def call(Map pipelineParams) {

    pipeline {
        agent any

        tools {
            maven 'maven3'
            jdk 'jdk8'
        }

        stages {

            stage('Checkout Git') {
                steps {
                    sh "rm -rf sources"
                    sh "git clone ${pipelineParams.gitUrl} --branch ${pipelineParams.branch} --single-branch sources"
                    sh "cd ./sources"
                }
            }


            stage('Determine version') {
                steps {
                     sh "cd ./sources"
		     sh "pwd"
                     generateVersion(pom: 'pom.xml')
                }
            }

            stage('Build') {
                steps {
                    sh 'mvn clean package'
                }
            }

        }

        post {

            failure {
                mail to: pipelineParams.email, subject: 'Pipeline failed', body: "${env.BUILD_URL}"
            }

        }
    }

}
