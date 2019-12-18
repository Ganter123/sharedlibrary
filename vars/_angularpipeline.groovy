#!/usr/bin/env groovy

def call(Map pipelineParams) {
 pipeline {
    agent any



 tools {nodejs "node"}
environment {
     //   FRONTEND_DIR = 'front-end/portal'
       GITLAB_CONNECTION = 'volansys'
        GIT_USER = sh (script: 'git show -s --pretty=%an', returnStdout: true).trim()
    }
    stages {       
        stage('PreBuild') {
            steps {
                dir(pipelineParams.build_directory) {
	            sh pipelineParams.buildcmd
                
               
                }
            }
        } 
        stage ('Build') {
            steps {
            
                  dir(pipelineParams.build_directory) {
                  sh pipelineParams.angularbuild
                 // _angularbuild()
                    }
                }
                
           }
        
      stage ('Pretest') {
            steps {
               dir(pipelineParams.build_directory) {
               sh pipelineParams.angulartest
               // _angulartest()
                }
            }
        } 
        stage ('Publish Reports') {
            steps {
            dir(pipelineParams.build_directory) {
                  _tslintreport()
              //  _testreport()
               
             }
        }
        
        } 
        stage ('SonarScanner') {
            steps {
           dir(pipelineParams.build_directory) {
                _sonar()
            }
            }
        }
         stage ('Release') {
             steps {
                  _angularfileoperations()
                 _angularartifacts()

             }
         }
       
       } 
       
       post {
          failure {
              script{
                        def messages = _generatestagenotificationmessages(_pipelinestagedetail())
                        updateGitlabCommitStatus name: '"${JOB_NAME}"', state: 'failed'
                        _email("${messages['emailTemplate']}")
                      } 
          }
      
      always {
            addShortText(text: "${GIT_USER}", background: 'orange', border: 1) ;
            addShortText(text: "${GIT_BRANCH}", background: 'yellow', border: 1);
            addShortText(text: "${NODE_NAME}", background: 'cyan', border: 1)
            _sidebarlink()
          cleanWs() /* clean up our workspace */
            }
      }
      
     options {
      gitLabConnection('"${GITLAB_CONNECTION}"')
    }
}
}
