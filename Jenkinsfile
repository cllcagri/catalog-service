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
      steps {
        sh './mvnw clean package -DskipTests'
      }
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
    stage('Deploy to OpenShift') {
      steps {
        withCredentials([string(credentialsId: 'oc-token', variable: 'OC_TOKEN')]) {
          sh """
            oc login --token="$OC_TOKEN" --server="https://api.rm2.thpm.p1.openshiftapps.com:6443" --insecure-skip-tls-verify=true
            oc project c-cagriyilmaz-dev
            oc set image deploy/catalog-service catalog-service=${IMAGE}:${IMG_TAG} --record=true || true
            oc rollout status deploy/catalog-service
          """
        }
      }
    }
  }

  post {
    always { archiveArtifacts artifacts: 'target/*.jar', onlyIfSuccessful: false }
  }
}