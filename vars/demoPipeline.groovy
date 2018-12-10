def call(body) {

    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()


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