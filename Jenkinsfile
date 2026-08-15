// Declarative pipeline: test -> build images -> push -> deploy to Kubernetes.
// All tooling used here is open source (Jenkins, Maven, Docker, kubectl, kustomize).
pipeline {
  agent any

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '30'))
    timeout(time: 45, unit: 'MINUTES')
    disableConcurrentBuilds()
  }

  environment {
    REGISTRY         = 'ghcr.io'
    IMAGE_OWNER      = 'sriindus'                 // GitHub user/org that owns the packages
    BACKEND_IMAGE    = "${REGISTRY}/${IMAGE_OWNER}/hello-fsd-backend"
    FRONTEND_IMAGE   = "${REGISTRY}/${IMAGE_OWNER}/hello-fsd-frontend"
    K8S_NAMESPACE    = 'hello-fsd'
    // Jenkins credentials IDs — create these in Manage Jenkins > Credentials
    REGISTRY_CREDENTIALS  = 'ghcr-credentials'   // username + PAT with write:packages
    KUBECONFIG_CREDENTIAL = 'kubeconfig'         // secret file
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        script {
          env.GIT_SHA = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          env.IMAGE_TAG = env.GIT_SHA
          currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.IMAGE_TAG}"
        }
      }
    }

    stage('Backend: Build & Test') {
      agent {
        docker {
          image 'maven:3.9-eclipse-temurin-21'
          reuseNode true
        }
      }
      steps {
        dir('backend') {
          sh 'mvn -B clean verify'
        }
      }
      post {
        always {
          junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
        }
      }
    }

    stage('Frontend: Install, Lint & Test') {
      agent {
        docker {
          image 'node:22-alpine'
          reuseNode true
        }
      }
      steps {
        dir('frontend') {
          sh '''
            set -eu
            npm ci
            npm run lint
            npm test
            npm run build
          '''
        }
      }
    }

    stage('Lint Manifests') {
      steps {
        sh '''
          set -eu
          if command -v kubectl >/dev/null 2>&1; then
            kubectl kustomize k8s/ > /dev/null
            echo "manifests render cleanly"
          else
            echo "kubectl not available on this agent; skipping manifest render"
          fi
        '''
      }
    }

    stage('Build & Push Images') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: env.REGISTRY_CREDENTIALS,
          usernameVariable: 'REGISTRY_USER',
          passwordVariable: 'REGISTRY_PASS'
        )]) {
          sh '''
            set -eu
            echo "$REGISTRY_PASS" | docker login "$REGISTRY" -u "$REGISTRY_USER" --password-stdin

            docker build \
              --build-arg BUILDKIT_INLINE_CACHE=1 \
              -t "$BACKEND_IMAGE:$IMAGE_TAG" -t "$BACKEND_IMAGE:latest" \
              -f backend/Dockerfile backend
            docker push "$BACKEND_IMAGE:$IMAGE_TAG"
            docker push "$BACKEND_IMAGE:latest"

            docker build \
              --build-arg BUILDKIT_INLINE_CACHE=1 \
              --build-arg BACKEND_UPSTREAM=hello-fsd-backend:8080 \
              -t "$FRONTEND_IMAGE:$IMAGE_TAG" -t "$FRONTEND_IMAGE:latest" \
              -f frontend/Dockerfile frontend
            docker push "$FRONTEND_IMAGE:$IMAGE_TAG"
            docker push "$FRONTEND_IMAGE:latest"

            docker logout "$REGISTRY"
          '''
        }
      }
    }

    stage('Deploy to Kubernetes') {
      when {
        branch 'main'
      }
      steps {
        withCredentials([file(credentialsId: env.KUBECONFIG_CREDENTIAL, variable: 'KUBECONFIG_FILE')]) {
          sh '''
            set -eu
            export KUBECONFIG="$KUBECONFIG_FILE"

            cd k8s
            kustomize edit set image \
              "$BACKEND_IMAGE=$BACKEND_IMAGE:$IMAGE_TAG" \
              "$FRONTEND_IMAGE=$FRONTEND_IMAGE:$IMAGE_TAG"
            cd ..

            kubectl apply -k k8s/
            kubectl -n "$K8S_NAMESPACE" rollout status deployment/hello-fsd-backend --timeout=180s
            kubectl -n "$K8S_NAMESPACE" rollout status deployment/hello-fsd-frontend --timeout=180s
          '''
        }
      }
    }

    stage('Smoke Test') {
      when {
        branch 'main'
      }
      steps {
        withCredentials([file(credentialsId: env.KUBECONFIG_CREDENTIAL, variable: 'KUBECONFIG_FILE')]) {
          sh '''
            set -eu
            export KUBECONFIG="$KUBECONFIG_FILE"
            kubectl -n "$K8S_NAMESPACE" run smoke-$BUILD_NUMBER \
              --image=curlimages/curl:8.11.1 --rm -i --restart=Never --quiet -- \
              -fsS http://hello-fsd-backend.$K8S_NAMESPACE.svc.cluster.local:8080/actuator/health
          '''
        }
      }
    }
  }

  post {
    success {
      echo "Deployed ${env.BACKEND_IMAGE}:${env.IMAGE_TAG} and ${env.FRONTEND_IMAGE}:${env.IMAGE_TAG}"
    }
    failure {
      echo "Build failed — see the stage logs above."
    }
    always {
      sh 'docker image prune -f --filter "until=24h" || true'
      cleanWs()
    }
  }
}
