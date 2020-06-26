pipeline {
    agent any
    tools {
        maven 'apache-maven-latest'
        jdk 'openjdk-jdk11-latest'
    }
    stages {
        stage ('Build: Plain Maven (M2)') {
            steps {
            	dir('server') {
                	sh 'mvn clean verify'
                }
            }
        }
    }
}
