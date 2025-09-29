pipeline {
  agent any
  environment {
    REGISTRY = 'docker.io'
    IMAGE    = 'celalcagri/catalog-service'
    CREDS    = 'dockerhub-creds'
  }
  options { timestamps(); skipDefaultCheckout(true) }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build & Test') {
      steps { sh './mvnw --no-transfer-progress clean verify' }
    }
    stage('Tag') {
      steps {
        script {
          env.IMG_TAG = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        }
      }
    }
    stage('Docker Build') {
      steps {
        sh """
          docker build -t ${IMAGE}:${IMG_TAG} -t ${IMAGE}:latest .
        """
      }
    }
    stage('Docker Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: CREDS, usernameVariable: 'U', passwordVariable: 'P')]) {
          sh """
            echo "$P" | docker login -u "$U" --password-stdin ${REGISTRY}
            docker push ${IMAGE}:${IMG_TAG}
            docker push ${IMAGE}:latest
            docker logout ${REGISTRY}
          """
        }
      }
    }
  }

  post {
    always { archiveArtifacts artifacts: 'target/*.jar', onlyIfSuccessful: false }
  }
}