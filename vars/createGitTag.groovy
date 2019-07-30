def call(Map pipelineParams,String versionNumber) {
      withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "${pipelineParams.gitCredentialId}",
                        usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {

          sh "git config credential.username ${env.GIT_USERNAME}"
          sh "git config credential.helper '!f() { echo password=\$GIT_PASSWORD; }; f'"
          sh "git tag ${versionNumber}"
          sh "GIT_ASKPASS=true git push origin ${versionNumber}"
      }
  }
