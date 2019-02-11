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
                    sh "rm -rf source-code"
                    sh "git clone ${pipelineParams.gitUrl} --branch ${pipelineParams.branch} --single-branch source-code"
                    sh "cd source-code/"
                }
            }


            stage('Determine version') {
                steps {
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