pipeline {

	agent any

    environment {
	    STAGE = ''
	}

    parameters {
        choice choices: ['gradle', 'maven'], description: 'Indicar herramienta de construcción', name: 'buildTool'
        string (name: 'stage', defaultValue: '')
    }

	stages{
		
        stage('Pipeline'){
            steps{
                script{
                    env.STAGE = null
                    env.PSTAGE = null
                    if (params.stage == ''){
                        figlet 'Stage Vacío'
                        env.PSTAGE = "ALL"
                        if (params.buildTool == 'gradle') {
                            def ejecucion = load 'gradle.groovy'
                            ejecucion.call(verifyBranchName())
                        }else {
                            def ejecucion = load 'maven.groovy'
                            ejecucion.call(verifyBranchName())
                        }
                    } else {
                        def stages = params.stage.split(";")
                        println "STAGES: ${stages}"
                        println "CANTIDAD de STAGES: ${stages.size()}"
                        for (i=0; i < stages.size(); i++) {
                            env.STAGE = null
                            env.PSTAGE = stages[i]
                            println "ESTOY EN: ${env.PSTAGE}" 
                            if (params.buildTool == "gradle") {
                                def ejecucion = load 'gradle.groovy'
                                ejecucion.call(verifyBranchName())
                            } else {
                                def ejecucion = load 'maven.groovy'
                                ejecucion.call(verifyBranchName())
                            }
                        }    
                    }
                }
            }

        }

	}
    post {
        always {
            echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"            
        }
        success {
            slackSend color: 'good',  message: "[Samuel López][${env.JOB_NAME}][$params.buildTool] Ejecución exitosa "            
        }
        failure {
            slackSend color: 'danger', message: "[Samuel López][${env.JOB_NAME}][$params.buildTool] Ejecución fallida en stage ${STAGE}"            
        }
    }
}


def verifyBranchName(){
    if(env.GIT_BRANCH.contains('develop') || env.GIT_BRANCH.contains('feature-')){
        return 'CI'
    } 
    else {
        return 'CD'
    }

}