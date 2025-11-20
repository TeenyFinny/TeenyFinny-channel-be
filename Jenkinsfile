pipeline {
    agent any

    environment {
        REGISTRY   = 'docker.io'
        MAIN_IMAGE_NAME = 'docker.io/teenyfinny/channel'
        DEV_IMAGE_NAME = 'docker.io/teenyfinny/channeltest'
        TEST_APP_NAME = 'sw_team_3_channel'
        TEST_PORT = '8260'
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
                sh 'docker images'

                script {
                    def branch = env.GIT_BRANCH

                    if (branch == 'test/jenkins' || branch == 'origin/test/jenkins') {
                        sh('''

# 0) 헬스체크 - 서버가 살아있는지 확인
echo "[health-check] Checking server health..."
health_status=$(curl -s --connect-timeout 2 --max-time 3 "http://192.168.0.79:8260/actuator/health" 2>/dev/null | jq -r '.status' 2>/dev/null)

if [ "$health_status" = "UP" ]; then
    echo "[health-check] Server is healthy. Proceeding with graceful shutdown..."

    # 1) readiness OFF 요청 보내고 응답 출력
    echo "[readiness/off] request"
    curl -XPOST "http://192.168.0.79:8260/internal/readiness/off" || echo "[readiness/off] curl failed: $?"
    echo ""  # 줄바꿈

    # 2) drain 루프 - 매번 응답 JSON 출력
    echo "[drain] start polling..."
    while true; do
        resp="$(curl -s "http://192.168.0.79:8260/actuator/drain")"
        echo "[drain] response: ${resp}"

        echo "${resp}" | jq -e '.drained == true' >/dev/null 2>&1 && {
            echo "[drain] drained == true, continue pipeline."
            break
        }

        echo "[drain] Waiting to drain..."
        sleep 1
    done
fi

# 4) 새 컨테이너 실행 (백그라운드)
docker rm -f ${TEST_APP_NAME} || true

cd /home/sw_team_3/backend

docker compose -p sw_team_3 up -d app-local-1

# 5) 상태 확인
docker ps --filter "name=${TEST_APP_NAME}"
docker logs --tail=50 "${TEST_APP_NAME}" || true
                        ''')
                    }

                    if (branch == 'dev' || branch == 'origin/dev') {
                        sh('''

                        ''')
                    }

                    if (branch == 'main' || branch == 'origin/main') {
                        sh('''
                            echo "TODO : AWS 분산환경 세팅 후 SSH로 AWS 접근 후 도커 허브의 이미지 요청 유실 없이 PULL, RUN 하기!"
                        ''')
                    }
                }
            }
        }
    } // end of stages
} // end of pipeline
