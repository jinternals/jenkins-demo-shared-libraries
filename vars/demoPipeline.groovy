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
                    sh "cd sources/"
                }
            }


            stage('Determine version') {
                steps {
                    dir("sources") {
                        generateVersion(pom: 'pom.xml')
                    }
                }
            }

            stage("Run Test"){
                steps{
                    dir("sources"){
                        sh 'mvn clean test'
                    }
                }
            }

            stage('Build') {
                steps {
                    dir("sources"){
                        sh 'mvn clean package'
                    }
                }
            }

            stage('Publish Artifacts'){
                steps{
                    sh "echo todo"
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
