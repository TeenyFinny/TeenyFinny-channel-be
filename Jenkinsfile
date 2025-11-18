pipeline {
    agent any

    environment {
        REGISTRY   = 'docker.io'
        MAIN_IMAGE_NAME = 'docker.io/teenyfinny/channel'
        DEV_IMAGE_NAME = 'docker.io/teenyfinny/channeltest'
    }

    stages {
        stage('CI : checkout') {
            steps {
                echo '빌드 시작됨!!'
                cleanWs()
                checkout scm // 자동으로 webhook 브랜치를 체크아웃
                sh 'ls -al' // 디버깅용
            }
        }

        stage('CI : build') {
            steps{
                sh '''
                set -euxo pipefail
                chmod +x ./gradlew

                ./gradlew clean build
                '''
            }
        }

        stage('CI : SonarQube analysis') {
            steps {
                withSonarQubeEnv('sonarqube-server') {
                    sh '''
                    set -euxo pipefail
                    chmod +x ./gradlew

                    echo '===== sonarqube 서버, 토큰 유효성 검증 ====='
                    curl http://sonarqube:9000/api/server/version
                    curl -sS -u "$SONAR_AUTH_TOKEN": http://sonarqube:9000/api/authentication/validate
                    curl -sS -u "$SONAR_AUTH_TOKEN": "http://sonarqube:9000/api/projects/search?projects=test"

                    ./gradlew sonar -Dsonar.token=$SONAR_AUTH_TOKEN -Dsonar.host.url=$SONAR_HOST_URL
                    '''
                }
            }
        }

        stage('CI : Quality Gate') {
            steps{
                timeout(time: 1, unit: 'MINUTES') {
                    script{
                        echo "Start"
                        def qg = waitForQualityGate()
                        echo "Status: ${qg.status}"
                        if(qg.status != 'OK') {
                            echo "NOT OK Status: ${qg.status}"
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        } else{
                            echo "status: ${qg.status}"
                        }
                        echo "End"
                    }
                }
            }
        }

        stage('CD : push to docker hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub',
                    usernameVariable: 'REG_USER',
                    passwordVariable: 'REG_PASS'
                )]){
                    sh '''
                        echo $REG_PASS | docker login -u $REG_USER --password-stdin
                    '''
                    script {
                        def branch = env.GIT_BRANCH

                        if (branch == 'test/jenkins' || branch == 'origin/test/jenkins') {
                            sh(label: 'Docker build & push (latest)', script: '''
                            set -euxo pipefail
                            docker build -t ${DEV_IMAGE_NAME}:latest .
                            docker push  ${DEV_IMAGE_NAME}:latest
                        ''')
                        }

                        if (branch == 'dev' || branch == 'origin/dev') {
                            sh(label: 'Docker build & push (latest)', script: '''
                            set -euxo pipefail
                            docker build -t ${DEV_IMAGE_NAME}:latest .
                            docker push  ${DEV_IMAGE_NAME}:latest
                        ''')
                        }

                        if (branch == 'main' || branch == 'origin/main') {
                            sh(label: 'Docker build & push (latest)', script: '''
                            set -euxo pipefail
                            docker build -t ${MAIN_IMAGE_NAME}:latest .
                            docker push  ${MAIN_IMAGE_NAME}:latest
                        ''')
                        }
                    }
                }
            }
        }

        stage('deploy') {
            steps{
                echo("TODO : AWS 분산환경 세팅 후 SSH로 AWS 접근 후 도커 허브의 이미지 요청 유실 없이 PULL, RUN 하기")
            }
        }
    } // end of stages
} // end of pipeline
