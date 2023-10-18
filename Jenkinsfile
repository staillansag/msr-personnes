final String PARAMETERS_FILE = 'parameters.yml'
final String CAAS_CONFIG_FILE_ID = 'caas-subscriptions-v4'
final String REPOSITORY_DEPLOYMENT_PREFIX = 'resources/openshift'
final String CLOUD_CONFIG_FILE_ID = 'cloud-subscriptions'
final String OPENSHIFT_REGISTRY_ROUTE = 'default-route-openshift-image-registry.apps.caas-int-hp.automation.edf.fr'

def fromImage = ""
def fromCredentialId = ""
def fromRegistry = ""
def fromNamespace = ""

def toImage = ""
def toCredentialId = ""
def toRegistry = ""
def toNamespace = ""

def fromToken = ""
def toToken = ""

def fromCreds = ""
def toCreds = ""

def CLOUD_CREDENTIAL_ID = ""
def CLOUD_ASSUME_ROLE= ""

def ACCESS_KEY_ID = ""
def SECRET_ACCESS_KEY = ""
def SESSION_TOKEN = ""

def performOpenShiftRollback = "false"
def openShiftRollbackVersion = ""
def performAWSRollback = "false"
def awsRollbackVersion = ""


pipeline {
   agent {
      label 'agent-openshift-latest'
   }
   options {
      buildDiscarder(logRotator(daysToKeepStr:'15', numToKeepStr:'15', artifactDaysToKeepStr: '', artifactNumToKeepStr:'3'))
      timeout(time: 120, unit: 'MINUTES')
      timestamps()
      ansiColor('xterm')
      durabilityHint('PERFORMANCE_OPTIMIZED')
   }

   stages{

        /*
        * Loading and reading JSON configuration file
        */
        stage("Load and check config") {
            options {
                timeout(time: 1, unit: 'MINUTES')
            }
            steps {
                script {

                    /*
                    * Set build env variables
                    */
                    if (! fileExists(PARAMETERS_FILE)) {
                        error("[ERROR] - Image parameters file ${PARAMETERS_FILE} not exists !")
                    }

                    /*
                    * Get Parameters from parameters.yml file
                    */
                    myParameters = readYaml file: "${PARAMETERS_FILE}"

                    fromPlatform = myParameters.parameters.FROM_PLATFORM.trim()          
                    fromImage = myParameters.parameters.FROM_IMAGE.trim()
                    fromNamespace = myParameters.parameters.FROM_NAMESPACE.trim()
                    deployNamespace = myParameters.parameters.DEPLOY_NAMESPACE.trim()
                    imageName = myParameters.parameters.IMAGE_NAME.trim()
                    maintainerEmail = myParameters.parameters.MAINTAINER_EMAIL.trim()
                    AWS_ACCOUNT = myParameters.parameters.AWS_ACCOUNT.trim()
                    routeSubDomain = myParameters.parameters.ROUTE_SUBDOMAIN.trim()

                    ecrUri = myParameters.parameters.ECR_URI.trim()

                    configFileProvider([configFile(fileId: "${CAAS_CONFIG_FILE_ID}", targetLocation: "${CAAS_CONFIG_FILE_ID}.json")]) {
                        caasConfig = readJSON file: "${CAAS_CONFIG_FILE_ID}.json"
                    }    

                    try {
                        currentCaasConfig = caasConfig["${deployNamespace.replaceAll('_', '-')}"]
                    } catch (Exception e) {
                        error("[ERROR] - Entry with key ${deployNamespace.replaceAll('_', '-')} not found in ${CAAS_CONFIG_FILE_ID} file")
                    }

                    openshift.withCluster(currentCaasConfig['url']) {
                        openshift.withProject(currentCaasConfig['namespaceName']){
                            openshift.withCredentials(currentCaasConfig['serviceAccountCredentialId']){
                                fromToken = openshift.raw("whoami", "-t").out.trim()
                                fromCreds = "unused:${fromToken}"
                            }
                        }
                    }                  



                    // /*
                    //  * Get Parameters from Cloud env 
                    //  */

                    println("[INFO] - retrieve cloud config")

                    configFileProvider([configFile(fileId: "${CLOUD_CONFIG_FILE_ID}", variable: "CLOUD_CONFIG")]) {
                        cloudConfig = readJSON file: "${CLOUD_CONFIG}"
                    }

                    try {
                        currentCloudConfig = cloudConfig["${AWS_ACCOUNT}"]
                    } catch (Exception e) {
                        error("[ERROR] - Entry with key ${AWS_ACCOUNT} not found in cloud-subscriptions.json")
                    }

                    CLOUD_CREDENTIAL_ID = currentCloudConfig["cloudCredentialId"]
                    CLOUD_ASSUME_ROLE = currentCloudConfig["cloudRole"]

                    if (fromImage.length() == 0
                            || fromNamespace.length() == 0
                            || deployNamespace.length() == 0
                            || routeSubDomain.length() == 0
                            || imageName.length() == 0
                            || maintainerEmail.length() == 0
                            || imageName.length() == 0
                            || ecrUri.length() == 0
                        ) {
                        error("[ERROR] - Missing required parameters in ${PARAMETERS_FILE}.")
                    }

                }

            }
        }


        // stage('Build') {
        //     options {
        //         timeout(time: 10, unit: 'MINUTES')
        //     }
        //     steps{
        //         script {
        //         openshift.withCluster(currentCaasConfig['url']) {
        //             openshift.withProject(currentCaasConfig['namespaceName']){
        //                 openshift.withCredentials(currentCaasConfig['serviceAccountCredentialId']){

        //                     imageReleaseDate = sh (script: "date --rfc-3339=seconds", returnStdout: true).trim()

        //                     /*
        //                     * Load and process template
        //                     */
        //                     def template = readYaml file: "${REPOSITORY_DEPLOYMENT_PREFIX}/build-template.yaml"
        //                     def processedTemplate = openshift.process(template ,
        //                     "-p", "IMAGE_NAME='${imageName}'",
        //                     "-p", "GIT_URL='${env.GIT_URL}'",
        //                     "-p", "GIT_BRANCH='${env.GIT_BRANCH}'",
        //                     "-p", "IMAGE_RELEASE_DATE='${imageReleaseDate}'",
        //                     "-p", "IMAGE_RELEASE_NUMBER='${env.BUILD_NUMBER}'",
        //                     "-p", "FROM_IMAGE='${fromImage}'",
        //                     "-p", "FROM_NAMESPACE='${fromNamespace}'",
        //                     "-p", "MAINTAINER_EMAIL='${maintainerEmail}'")

        //                     println "[INFO] - Generated build template:"
        //                     print processedTemplate

        //                     /*
        //                     * Create BuildConfig, or update if BuildConfig already exist
        //                     */

                            
        //                     if (openshift.selector("bc", "bc-${imageName}").exists()){
        //                     println("[INFO] - Buildconfig bc-${imageName} already exists")
        //                     openshift.apply(processedTemplate)

        //                     /*
        //                     * Start build
        //                     */
        //                     openshift.raw("start-build", "bc-${imageName}")
        //                     }
        //                     else
        //                     {
        //                     println("[INFO] - Creating bc-${imageName}")
        //                     openshift.create(processedTemplate)
        //                     }

        //                     /*
        //                     * Follow build
        //                     * Get Last Build and check his status
        //                     * Start build if no build running
        //                     */
        //                     println("[INFO] - Follow build")
        //                     def bc = openshift.selector("bc", "bc-${imageName}")

        //                     lastVersionBC = bc.object().status.lastVersion

        //                     if (lastVersionBC == 0)
        //                     {
        //                     openshift.raw("start-build", "bc-${imageName}")
        //                     }

        //                     /*
        //                     * Check build after running
        //                     */
        //                     def b = openshift.selector("build", "bc-${imageName}-${lastVersionBC}")
                            
        //                     /*
        //                     * Check build before running
        //                     */
        //                     timeout(5) {
        //                     waitUntil {
        //                         return b.object().status.phase == 'Running'
        //                     }
        //                     }

        //                     println("[INFO] - Follow build")
                            
        //                     bc.logs('-f')

        //                     /*
        //                     * Check build after running
        //                     */
        //                     timeout(1) {
        //                     waitUntil {
        //                         return b.object().status.phase != 'Running'
        //                     }
        //                     }

        //                     try {
        //                     bc.logs("--follow")
        //                     } catch (Exception e) {
        //                     println("[ERROR] - Build error")
        //                     }

        //                     timeout(5) {
        //                     waitUntil {
        //                         return b.object().status.phase != 'Running'
        //                     }
        //                     }

        //                     lastStatus = b.object().status.phase
                            
        //                     if (lastStatus != 'Complete') {
        //                     error("[ERROR] Build failed.")
        //                     }
                
        //                     println("[INFO] - Adding tag latest to built image")
        //                     openshift.raw("tag", "${imageName}:'${env.BUILD_NUMBER}'" , "${imageName}:latest" )

        //                 }
        //             }
        //         }
        //         }
        //     }
        // }

        // stage('OpenShift - Deploy') {
        //     options {
        //         timeout(time: 5, unit: 'MINUTES')
        //     }
        //     steps{
        //         script {
        //             openshift.withCluster(currentCaasConfig['url']) {
        //                 openshift.withProject(currentCaasConfig['namespaceName']){
        //                     openshift.withCredentials(currentCaasConfig['serviceAccountCredentialId']){

        //                         def templateDep = readYaml file: "${REPOSITORY_DEPLOYMENT_PREFIX}/deploy-${imageName}-template.yaml"
        //                         def templateService = readYaml file: "${REPOSITORY_DEPLOYMENT_PREFIX}/service-${imageName}-template.yaml"
        //                         def templateRoute = readYaml file: "${REPOSITORY_DEPLOYMENT_PREFIX}/route-${imageName}-template.yaml"

        //                         imageVersion = "${env.BUILD_NUMBER}"

        //                         def processedDepTemplate = openshift.process(templateDep,
        //                         "-p", "IMAGE_RELEASE_NUMBER='${imageVersion}'",
        //                         "-p", "IMAGE_NAME='${imageName}'",
        //                         "-p", "DEPLOY_NAMESPACE='${deployNamespace}'",
        //                         "-o", "yaml")

        //                         def processedServiceTemplate = openshift.process(templateService,
        //                         "-p", "DEPLOY_NAMESPACE='${deployNamespace}'",
        //                         "-p", "IMAGE_NAME='${imageName}'",
        //                         "-o", "yaml")

        //                         def processedRouteTemplate = openshift.process(templateRoute,
        //                         "-p", "ROUTE_SUBDOMAIN='${routeSubDomain}'",
        //                         "-p", "IMAGE_NAME='${imageName}'",
        //                         "-p", "DEPLOY_NAMESPACE='${deployNamespace}'",
        //                         "-o", "yaml")

        //                         /*
        //                         * Apply deployment
        //                         */
        //                         println("[INFO] - Applying new params to deployment ${imageName}")
        //                         deploymentResult = openshift.apply(processedDepTemplate)
        //                         println("[INFO] - new params to deployment ${imageName} should be applied")

        //                         /*
        //                         * Apply Service
        //                         */
        //                         println("[INFO] - Applying new params to Service ${imageName}")
        //                         openshift.apply(processedServiceTemplate)
        //                         println("[INFO] - new params to Service ${imageName} should be applied")

        //                         /*
        //                         * Apply Route
        //                         */
        //                         println("[INFO] - Applying new params to Route ${imageName}")
        //                         openshift.apply(processedRouteTemplate)
        //                         println("[INFO] - new params to Route ${imageName} should be applied")
                                
        //                         deploymentResult.rollout().status()

        //                     }
        //                 }
        //             }
        //         }
        //     }
        // }


        // stage('AWS - Copy image to ECR') {
        //     options {
        //         timeout(time: 5, unit: 'MINUTES')
        //     }
        //     environment {
        //         AWS_DEFAULT_REGION = 'eu-west-1'
        //         NO_PROXY = '*.edf.fr'
        //         HTTP_PROXY = 'vip-appli.proxy.edf.fr:3128'
        //         HTTPS_PROXY = 'vip-appli.proxy.edf.fr:3128'
        //         AWS_CREDENTIALS = credentials("${CLOUD_CREDENTIAL_ID}")
        //         AWS_ACCESS_KEY_ID = "${env.AWS_CREDENTIALS_USR}"
        //         AWS_SECRET_ACCESS_KEY = "${env.AWS_CREDENTIALS_PSW}"
        //     }
        //     steps{
        //         script {

        //             println("[INFO] - retrieve creds for Cloud")

        //             wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${CLOUD_ASSUME_ROLE}", var: 'SECRET']]]) {
        //                 ROLE = readJSON text: sh(script: "aws sts assume-role --role-arn '${CLOUD_ASSUME_ROLE}' --role-session-name '${AWS_ACCOUNT.replaceAll('-', '_')}'", returnStdout: true)
        //             }
                    
        //             ACCESS_KEY_ID = ROLE["Credentials"]["AccessKeyId"]
        //             SECRET_ACCESS_KEY = ROLE["Credentials"]["SecretAccessKey"]
        //             SESSION_TOKEN = ROLE["Credentials"]["SessionToken"]
                    
        //             // Login Docker Vs ECR
        //             wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${ACCESS_KEY_ID}", var: 'SECRET']]]) {
        //                 wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${SECRET_ACCESS_KEY}", var: 'SECRET']]]) {
        //                     wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${SESSION_TOKEN}", var: 'SECRET']]]) {
        //                         toCreds = sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && aws ecr get-authorization-token | jq .authorizationData[0].authorizationToken -r | base64 -d", returnStdout: true)
        //                     }
        //                 }
        //             }

        //             SOURCE = "${OPENSHIFT_REGISTRY_ROUTE}/${deployNamespace}/${imageName}:${env.BUILD_NUMBER}"
        //             DESTINATION = "${ecrUri}/${imageName}:${env.BUILD_NUMBER}"

        //             wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${fromCreds}", var: 'SECRET']]]) {
        //                 wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${toCreds}", var: 'SECRET']]]) {
        //                     if ("${fromCreds}" != "" && "${toCreds}" != "") {
        //                         sh("skopeo copy docker://${SOURCE} docker://${DESTINATION} --screds ${fromCreds} --dcreds ${toCreds}")
        //                     }
        //                     else if ("${fromCreds}" != "") {
        //                         sh("skopeo copy docker://${SOURCE} docker://${DESTINATION} --screds ${fromCreds}")
        //                     }
        //                     else if ("${toCreds}" != "") {
        //                         sh("skopeo copy docker://${SOURCE} docker://${DESTINATION} --dcreds ${toCreds}")
        //                     }
        //                     else {
        //                         sh("skopeo copy docker://${SOURCE} docker://${DESTINATION}")
        //                     }
        //                 }
        //             }

        //         }
        //     }
        // }

        // stage('AWS - EKS deployment') {
        //     options {
        //         timeout(time: 5, unit: 'MINUTES')
        //     }
        //     environment {
        //         AWS_DEFAULT_REGION = 'eu-west-1'
        //         NO_PROXY = '*.edf.fr'
        //         HTTP_PROXY = 'vip-appli.proxy.edf.fr:3128'
        //         HTTPS_PROXY = 'vip-appli.proxy.edf.fr:3128'
        //         KUBECONFIG = "/var/lib/jenkins/.kube/config"
        //         AWS_CREDENTIALS = credentials("${CLOUD_CREDENTIAL_ID}")
        //         AWS_ACCESS_KEY_ID = "${env.AWS_CREDENTIALS_USR}"
        //         AWS_SECRET_ACCESS_KEY = "${env.AWS_CREDENTIALS_PSW}"
        //     }
        //     steps{
        //         script {

        //             wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${CLOUD_ASSUME_ROLE}", var: 'SECRET']]]) {
        //                 ROLE = readJSON text: sh(script: "aws sts assume-role --role-arn '${CLOUD_ASSUME_ROLE}' --role-session-name '${AWS_ACCOUNT.replaceAll('-', '_')}'", returnStdout: true)
        //             }

        //             ACCESS_KEY_ID = ROLE["Credentials"]["AccessKeyId"]
        //             SECRET_ACCESS_KEY = ROLE["Credentials"]["SecretAccessKey"]
        //             SESSION_TOKEN = ROLE["Credentials"]["SessionToken"]                   

        //             wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${ACCESS_KEY_ID}", var: 'SECRET']]]) {
        //                 wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${SECRET_ACCESS_KEY}", var: 'SECRET']]]) {
        //                     wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${SESSION_TOKEN}", var: 'SECRET']]]) {

        //                         // Retrieval of kubeconfig to connect to the EKS cluster
        //                         sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && aws eks --region eu-west-1 update-kubeconfig --name exp-cluster", returnStdout: true)

        //                         // Positionning in the desired EKS namespace
        //                         def EKS_NAMESPACE = "${deployNamespace}"
        //                         kubeContext = sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl config current-context", returnStdout: true).trim()
        //                         sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl config set-context ${kubeContext} --namespace=${EKS_NAMESPACE}", returnStdout: true)

        //                         def imageVersion = "${env.BUILD_NUMBER}"
        //                         def deploymentFile = "resources/kubernetes/deploy-${imageName}.yaml"
        //                         def deploymentFileContent = readFile(file: deploymentFile)
        //                         def newDeploymentFileContent = deploymentFileContent.replaceAll("${imageName}:latest", "${imageName}:${imageVersion}")
        //                         writeFile (file: "newDeployment.yaml", text: newDeploymentFileContent)

        //                         try {
        //                             // Get the current deployment version - in case the tests go wrong we will rollback to this version
        //                             awsRollbackVersion = sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl rollout history deployment/${imageName} -o jsonpath='{.metadata.generation}'", returnStdout: true)
        //                         } catch (Exception e) {

        //                         }

        //                         // Apply the microservice configuration
        //                         // Note: this config relies on secrets that are not managed by this pipeline, they are part of the namespace / project config
        //                         sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl apply -f resources/kubernetes/cm-${imageName}.yaml", returnStdout: true)

        //                         // Apply the microservice deployment
        //                         sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl apply -f newDeployment.yaml", returnStdout: true)

        //                         // Apply the service
        //                         sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl apply -f resources/kubernetes/service-${imageName}.yaml", returnStdout: true)

        //                         // Wait for the end of the deployment
        //                         sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl rollout status deployment ${imageName} --timeout=300s", returnStdout: true)

        //                     }
        //                 }
        //             }

        //         }
        //     }

        // }

        stage('AWS - EKS E2E tests') {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            environment {
                AWS_DEFAULT_REGION = 'eu-west-1'
                NO_PROXY = '*.edf.fr'
                HTTP_PROXY = 'vip-appli.proxy.edf.fr:3128'
                HTTPS_PROXY = 'vip-appli.proxy.edf.fr:3128'
                KUBECONFIG = "/var/lib/jenkins/.kube/config"
                AWS_CREDENTIALS = credentials("${CLOUD_CREDENTIAL_ID}")
                AWS_ACCESS_KEY_ID = "${env.AWS_CREDENTIALS_USR}"
                AWS_SECRET_ACCESS_KEY = "${env.AWS_CREDENTIALS_PSW}"
            }
            steps{
                script {

                    wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${CLOUD_ASSUME_ROLE}", var: 'SECRET']]]) {
                        ROLE = readJSON text: sh(script: "aws sts assume-role --role-arn '${CLOUD_ASSUME_ROLE}' --role-session-name '${AWS_ACCOUNT.replaceAll('-', '_')}'", returnStdout: true)
                    }

                    ACCESS_KEY_ID = ROLE["Credentials"]["AccessKeyId"]
                    SECRET_ACCESS_KEY = ROLE["Credentials"]["SecretAccessKey"]
                    SESSION_TOKEN = ROLE["Credentials"]["SessionToken"]                   

                    wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${ACCESS_KEY_ID}", var: 'SECRET']]]) {
                        wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${SECRET_ACCESS_KEY}", var: 'SECRET']]]) {
                            wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${SESSION_TOKEN}", var: 'SECRET']]]) {

                                // Retrieval of kubeconfig to connect to the EKS cluster
                                sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && aws eks --region eu-west-1 update-kubeconfig --name exp-cluster", returnStdout: true)

                                // Positionning in the desired EKS namespace
                                def EKS_NAMESPACE = "${deployNamespace}"
                                kubeContext = sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl config current-context", returnStdout: true).trim()
                                sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl config set-context ${kubeContext} --namespace=${EKS_NAMESPACE}", returnStdout: true)

                                // Scale out the E2E tests microservice
                                sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl scale deployment dce-msr-tests --replicas=1", returnStdout: true)

                                // Wait for the E2E tests pod to be up
                                sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl rollout status deployment dce-msr-tests --timeout=300s", returnStdout: true)

                                // We get the name of the test pod
                                TEST_POD=sh(script: """
                                    export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} 
                                    export AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} 
                                    export AWS_SESSION_TOKEN=${SESSION_TOKEN}
                                    kubectl get pods --selector=app=dce-msr-tests --field-selector=status.phase=Running -o=jsonpath='{.items[0].metadata.name}'
                                    """, returnStdout: true).trim()
                                // TEST_POD=$(kubectl get pods --selector=app=dce-msr-tests --field-selector=status.phase=Running -o=jsonpath='{.items[0].metadata.name}')

                                println "[INFO] - Test pod name: ${TEST_POD}"

                                // We get the password to call APIs with basich auth
                                ADMIN_PASSWORD=sh(script: """
                                    export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} &&
                                    kubectl exec ${TEST_POD} -- sh -c 'cat /etc/secrets/ADMIN_PASSWORD'
                                    """, returnStdout: true).trim()
                                // ADMIN_PASSWORD=$(kubectl exec ${TEST_POD} -- sh -c "cat /etc/secrets/ADMIN_PASSWORD")

                                // Since we don't have a working ingress, we make a call to POST /personnesAPI/personnes/demande-zip within the test pod, via the service layer
                                ID_DEMANDE=sh(script: """
                                    export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} &&
                                    kubectl exec $TEST_POD -- curl --silent --request POST "http://dce-msr-frontend/personnesAPI/personnes/demande-zip" -u Administrator:$ADMIN_PASSWORD | jq -r '.idDemande'
                                    """, returnStdout: true).trim()                                 
                                // ID_DEMANDE=$(kubectl exec $TEST_POD -- curl --silent --request POST "http://dce-msr-frontend/personnesAPI/personnes/demande-zip" -u Administrator:$ADMIN_PASSWORD | jq -r '.idDemande')

                                // Wait one minute for the who integration process to finish
                                sh(script: "sleep 60", returnStdout: true)

                                // And still within the test pod we check if the zip files have been correctly placed in the SFTP server and S3 bucket
                                JSON_RESPONSE=sh(script: """
                                    export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} &&
                                    kubectl exec $TEST_POD -- curl --silent "http://localhost:5555/testAPI/personnes-zip/${ID_DEMANDE}" -u Administrator:$ADMIN_PASSWORD
                                    """, returnStdout: true).trim()                                 
                                // JSON_RESPONSE=$(kubectl exec $TEST_POD -- curl --silent "http://localhost:5555/testAPI/personnes-zip/${ID_DEMANDE}" -u Administrator:$ADMIN_PASSWORD)

                                println "[INFO] - Test status : ${JSON_RESPONSE}"

                                S3_STATUS=sh(script: "echo $JSON_RESPONSE | jq -r '.s3.statut'", returnStdout: true).trim()  
                                SFTP_STATUS=sh(script: "echo $JSON_RESPONSE | jq -r '.sftp.statut'", returnStdout: true).trim()  
                                // S3_STATUS=$(echo $JSON_RESPONSE | jq -r '.s3.statut')
                                // SFTP_STATUS=$(echo $JSON_RESPONSE | jq -r '.sftp.statut')

                                // Scale in the E2E tests microservice once the tests are finished
                                sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl scale deployment dce-msr-tests --replicas=0", returnStdout: true)

                                if (S3_STATUS != 'OK'
                                    || SFTP_STATUS != 'OK') {
                                    error("[ERROR] - E2E tests failed")
                                }

                            }
                        }
                    }

                }
            }

        }

    }

    // post {
    //     failure {

    //         steps{
    //             script {

    //                 // We do a rollback of the OpenShift deployment if instructed by performAWSRollback
    //                 if (performOpenShiftRollback == 'true' && openShiftRollbackVersion.length() != 0) {
    //                     println("[INFO] - Rollback to OpenShift deployment version ${openShiftRollbackVersion}")

    //                 }

    //                 // We do a rollback of the AWS deployment if instructed by performAWSRollback
    //                 if (performAWSRollback == 'true' && awsRollbackVersion.length() != 0) {
    //                     println("[INFO] - Rollback to AWS deployment version ${awsRollbackVersion}")

    //                     wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${CLOUD_ASSUME_ROLE}", var: 'SECRET']]]) {
    //                         ROLE = readJSON text: sh(script: "aws sts assume-role --role-arn '${CLOUD_ASSUME_ROLE}' --role-session-name '${AWS_ACCOUNT.replaceAll('-', '_')}'", returnStdout: true)
    //                     }

    //                     ACCESS_KEY_ID = ROLE["Credentials"]["AccessKeyId"]
    //                     SECRET_ACCESS_KEY = ROLE["Credentials"]["SecretAccessKey"]
    //                     SESSION_TOKEN = ROLE["Credentials"]["SessionToken"]    

    //                     // Retrieval of kubeconfig to connect to the EKS cluster
    //                     wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${ACCESS_KEY_ID}", var: 'SECRET']]]) {
    //                         wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${SECRET_ACCESS_KEY}", var: 'SECRET']]]) {
    //                             wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${SESSION_TOKEN}", var: 'SECRET']]]) {

    //                                 // Retrieval of kubeconfig to connect to the EKS cluster
    //                                 sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && aws eks --region eu-west-1 update-kubeconfig --name exp-cluster", returnStdout: true)

    //                                 // Positionning in the desired EKS namespace
    //                                 def EKS_NAMESPACE = "${deployNamespace}"
    //                                 kubeContext = sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl config current-context", returnStdout: true).trim()
    //                                 sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl config set-context ${kubeContext} --namespace=${EKS_NAMESPACE}", returnStdout: true)

    //                                 // Apply the microservice configuration
    //                                 // Note: this config relies on secrets that are not managed by this pipeline, they are part of the namespace / project config
    //                                 println("[INFO] - Rollback to revision = ${rollbackVersion}")
    //                                 sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl rollout undo deployment/${imageName} --to-revision=${rollbackVersion}", returnStdout: true)

    //                                 timeout(time: 5, unit: 'MINUTES') {
    //                                     // Wait for the end of the deployment
    //                                     sh(script: "export AWS_ACCESS_KEY_ID=${ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS_KEY} AWS_SESSION_TOKEN=${SESSION_TOKEN} && kubectl rollout status deployment ${imageName} --timeout=300s", returnStdout: true)
    //                                 }

    //                             }
    //                         }

    //                     }
    //                 }
    //             }

    //         }

    //     }
    // }



}
