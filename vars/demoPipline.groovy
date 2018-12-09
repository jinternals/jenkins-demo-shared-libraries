def call(Map pipelineParams) {

    pipeline {
        agent any

        tools {
            maven 'maven3'
            jdk 'jdk8'
        }

        environment {
            branch = pipelineParams.branch
            scmUrl = pipelineParams.gitUrl
            alertEmail = pipelineParams.email
        }

        stages {

            stage('Checkout Git') {
                steps {
                    git branch: branch, url: gitUrl
                }
            }

            stage('Determine version') {
                steps {
                    script {
                        def version = generateVersion(pom: 'pom.xml')

                        currentBuild.displayName = "# ${version}"
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
                mail to: alertEmail , subject: 'Pipeline failed', body: "${env.BUILD_URL}"
            }

        }
    }

}