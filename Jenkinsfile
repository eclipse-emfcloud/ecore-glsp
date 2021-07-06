def kubernetes_config = """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: ci
    image: eclipseglsp/ci:uitest
    tty: true
    resources:
      limits:
        memory: "4Gi"
        cpu: "2"
      requests:
        memory: "4Gi"
        cpu: "2"
    command:
    - cat
    volumeMounts:
    - mountPath: "/home/jenkins"
      name: "jenkins-home"
      readOnly: false
    - mountPath: "/.yarn"
      name: "yarn-global"
      readOnly: false
    - name: global-cache
      mountPath: /.cache     
    - name: global-npm
      mountPath: /.npm   
  volumes:
  - name: "jenkins-home"
    emptyDir: {}
  - name: "yarn-global"
    emptyDir: {}
  - name: global-cache
    emptyDir: {}
  - name: global-npm
    emptyDir: {}
"""
pipeline {
    agent {
        kubernetes {
            label 'ecore-glsp-agent-pod'
            yaml kubernetes_config
        }
    }
    options {
        buildDiscarder logRotator(numToKeepStr: '15')
    }
    environment {
        YARN_CACHE_FOLDER = "${env.WORKSPACE}/yarn-cache"
        SPAWN_WRAP_SHIM_ROOT = "${env.WORKSPACE}"
        CHROME_BIN="/usr/bin/google-chrome"
    }
    stages {
        stage('Build client') {
            steps {
                container('ci') {
                    timeout(15){
                        dir('client') {
                            sh 'yarn install'
                        }
                    }
                }
            }
        }
        stage('Build server'){
            steps{
                container('ci'){
                    timeout(15){
                        dir('server'){
                        	sh 'mvn --version'
                            sh 'mvn clean verify --batch-mode -Dmaven.repo.local=/home/jenkins/.m2/repository'
                        }
                    }
                }
            }
        }
        stage('E2E tests'){
            steps{
                container('ci'){
                    timeout(15){
                        dir('client'){
                            sh 'yarn e2etest'
                        }
                    }
                }
            }
        }

        stage('Deploy (master only)') {
            when { branch 'master' }
            steps {
                build job: 'deploy-ecore-glsp-npm', wait: false
            }
        }
    }
    post {
        always {
            container('ci') {
                archiveArtifacts artifacts: 'client/tests/results/**', fingerprint: true
                junit 'client/tests/results/testcafe-report.xml'
            }
        }
    }
}