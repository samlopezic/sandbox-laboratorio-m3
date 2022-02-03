def call(String pipelineType){

figlet pipelineType
figlet 'Gradle'
println "${env.GIT_BRANCH}"

if (pipelineType == 'CI'){
    figlet 'Integración Continua'
    stage('build'){
        if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') {
            figlet 'Build & Test'
            STAGE = env.STAGE_NAME
            sh './gradlew clean build'
        }
    }
    stage('sonar'){
        if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') {        
            figlet 'SonarQube Analysis'        
            STAGE = env.STAGE_NAME
            def scannerHome = tool 'sonar-scanner';
            withSonarQubeEnv('sonarqube-server'){
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=pipeline-devops-labm3-gradle -Dsonar.sources=src -Dsonar.java.binaries=build"
            }
        }
    }        
    stage('run'){
        if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') {         
            figlet 'Run Jar'    
            STAGE = env.STAGE_NAME
            sh 'JENKINS_NODE_COOKIE=dontKillMe nohup bash gradlew bootRun &'
            sleep 10
        }
    }
    stage('test'){
        if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') {         
            figlet 'Test'             
            STAGE = env.STAGE_NAME
            sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        } 
    }
    stage('nexusci') {
        if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') {         
            figlet 'NexusCI'            
            STAGE = env.STAGE_NAME
            nexusPublisher nexusInstanceId: 'nexus',
            nexusRepositoryId: 'pipeline-devops-labm3',
            packages: [
                        [
                            $class: 'MavenPackage',
                            mavenAssetList: [
                            [classifier: '', extension: '', filePath: "${env.WORKSPACE}/build/libs/DevOpsUsach2020-0.0.1.jar"]
                            ],
                            mavenCoordinate: [
                            artifactId: 'DevOpsUsach2020',
                            groupId: 'com.devopsusach2020',
                            packaging: 'jar',
                            version: '0.0.1'
                            ]
                        ]
                    ]
            sh "ls -ltr"
        }
    }

    if ("${env.GIT_BRANCH}" == "develop"){
        stage('gitCreateRelease') {
            if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') {         
                figlet env.STAGE_NAME           
                STAGE = env.STAGE_NAME

            }
        }
    }
    

} else {

    figlet 'Despliegue Continuo'
    stage('gitDiff'){
        if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') {         
            figlet env.STAGE_NAME            
            STAGE = env.STAGE_NAME
            
        }
    }
    
    stage('nexusDownload'){
        if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') {         
            figlet 'Download Nexus'            
            STAGE = env.STAGE_NAME
            sh "curl -X GET -u 'admin:koba' http://10.69.166.130:8082/repository/pipeline-devops-labm3/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar -O"
            sh "echo ${env.WORKSPACE}"
            //sh "mv DevOpsUsach2020-0.0.1.jar DevOpsUsach2020-1.0.1.jar"
            sh "ls -ltr"
        }
    }
    stage('run'){
        if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') { 
            figlet 'Run Downloaded Jar'            
            STAGE = env.STAGE_NAME
            sh 'JENKINS_NODE_COOKIE=dontKillMe nohup bash gradlew bootRun &'
            sleep 10
        }
    }
    stage('test'){
        if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') { 
            figlet env.STAGE_NAME 
            STAGE = env.STAGE_NAME
            sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        }
    }

    stage('gitMergeMain') {
        STAGE = env.STAGE_NAME
        figlet "Stage: ${env.STAGE_NAME}"
        // def git = new helpers.Git()
        // git.merge("${env.GIT_LOCAL_BRANCH}", 'main')

    }

    stage('gitMergeDevelop') {
        STAGE = env.STAGE_NAME
        figlet "Stage: ${env.STAGE_NAME}"
        // def git = new helpers.Git()
        // git.merge("${env.GIT_LOCAL_BRANCH}", 'develop')

    }

    stage('gitTagMaster') {
        STAGE = env.STAGE_NAME
        figlet "Stage: ${env.STAGE_NAME}"
        // def git = new helpers.Git()
        // git.tag("${env.GIT_LOCAL_BRANCH}",'main')
    }
    /* stage('nexuscd') {
        if (env.PSTAGE == env.STAGE_NAME || env.PSTAGE == 'ALL') {         
            figlet 'NexusCD' 
            STAGE = env.STAGE_NAME
            sh 'env'
            println "Stage: ${env.STAGE_NAME}"    
            nexusPublisher nexusInstanceId: 'nexus',
            nexusRepositoryId: 'pipeline-devops-labm3',
            packages: [
                        [
                            $class: 'MavenPackage',
                            mavenAssetList: [
                            [classifier: '', extension: '', filePath: "${env.WORKSPACE}/DevOpsUsach2020-1.0.1.jar"]
                            ],
                            mavenCoordinate: [
                            artifactId: 'DevOpsUsach2020',
                            groupId: 'com.devopsusach2020',
                            packaging: 'jar',
                            version: '1.0.1'
                            ]
                        ]
                    ]
        }
    } */    
} 

}


def merge(String ramaOrigen, String ramaDestino){
	println "Este método realiza un merge ${ramaOrigen} y ${ramaDestino}"

	checkout(ramaOrigen)
	checkout(ramaDestino)

	sh """
		git merge ${ramaOrigen}
		git push origin ${ramaDestino}
	"""
}

def tag(String ramaOrigen, String ramaDestino){
	println "Este método realiza un tag en master de ${ramaOrigen}"

	if (ramaOrigen.contains('release-v')){
		checkout(ramaDestino)
		def tagValue = ramaOrigen.split('release-')[1]
		sh """
			git tag ${tagValue}
			git push origin ${tagValue}
		"""

	} else {
		error "La rama ${ramaOrigen} no cumple con nomenclatura definida para rama release (release-v(major)-(minor)-(patch))."
	}
}

def checkout(String rama){
	sh "git reset --hard HEAD; git checkout ${rama}; git pull origin ${rama}"
}



return this;