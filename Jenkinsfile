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

    stage('Docker Build & Push (amd64)') {
      steps {
        withCredentials([usernamePassword(credentialsId: CREDS, usernameVariable: 'U', passwordVariable: 'P')]) {
          sh '''
            set -e
            echo "$P" | docker login -u "$U" --password-stdin ${REGISTRY}

            docker buildx create --use --name msbuilder || true
            docker buildx inspect --bootstrap

            docker buildx build --platform linux/amd64 \
              -t ${IMAGE}:${IMG_TAG} \
              -t ${IMAGE}:latest \
              --push .

            docker logout ${REGISTRY}
          '''
        }
      }
    }

    stage('Deploy to OpenShift') {
      steps {
        withCredentials([string(credentialsId: 'oc-token', variable: 'OC_TOKEN')]) {
          sh '''
            set -e
            oc login --token="$OC_TOKEN" --server="https://api.rm2.thpm.p1.openshiftapps.com:6443" --insecure-skip-tls-verify=true
            oc project c-cagriyilmaz-dev

            echo "🟦 Applying manifests (create or update)..."
            oc apply -f k8s/catalog-service.yaml

            echo "🟦 Setting image to the fresh build tag..."
            oc set image deploy/catalog-service catalog-service=${IMAGE}:${IMG_TAG} --record=true

            echo "🟦 Ensuring replicas >= 1..."
            CUR=$(oc get deploy/catalog-service -o jsonpath='{.spec.replicas}' || echo "0")
            if [ -z "$CUR" ] || [ "$CUR" = "0" ]; then
              oc scale deploy/catalog-service --replicas=1
            fi

            echo "🟦 Waiting for rollout..."
            oc rollout status deploy/catalog-service
          '''
        }
      }
    }

  post {
    always { archiveArtifacts artifacts: 'target/*.jar', onlyIfSuccessful: false }
  }
}