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
                    git branch: pipelineParams.branch, url: pipelineParams.gitUrl
                }
            }


            stage('Determine version') {
                steps {
                    script {
                        currentBuild.displayName = "# Some version"
                    }
                }
            }

            stage('Build') {
                steps {
                    sh 'mvn clean package -DskipTests=true'
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