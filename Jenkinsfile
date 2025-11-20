pipeline {
    agent any

    environment {
        REGISTRY   = 'docker.io'                                // docker hub 사용
        MAIN_IMAGE_NAME = 'docker.io/teenyfinny/channel'        // 메인 브랜치에서 다룰 이미지 이름
        DEV_IMAGE_NAME = 'docker.io/teenyfinny/channeltest'     // 테스트 브랜치에서 다룰 이미지 이름
        TEST_APP_NAME = 'sw_team_3_channel'                     // 테스트 서버에서의 컨테이너명
    }

    stages {
        stage('CI : checkout') {
            steps {
                echo '빌드 시작됨'
                cleanWs()       // 워크 스페이스를 정리합니다.
                checkout scm    // 자동으로 webhook 브랜치를 체크아웃
            }
        }

        stage('CI : build') {
            steps{      // gradlew에 권한을 부여하고 클린 빌드를 수행합니다.
                sh '''
                set -euxo pipefail
                chmod +x ./gradlew

                ./gradlew clean build
                '''
            }
        }

        stage('CI : SonarQube analysis') {
            steps {     // Jenkins Credential에 저장된 소나큐브 관련 정보를 바탕으로, 정적 분석을 의뢰합니다.
                withSonarQubeEnv('sonarqube-server') {
                    sh '''
                    set -euxo pipefail
                    chmod +x ./gradlew

                    ./gradlew sonar -Dsonar.token=$SONAR_AUTH_TOKEN -Dsonar.host.url=$SONAR_HOST_URL
                    '''
                }
            }
        }

        stage('CI : Quality Gate') {
            steps{      // 정적 분석한 리포트를 바탕으로 퀄리티 체크를 수행합니다.
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
            steps {     // 빌드한 jar 파일을 도커 이미지로 만든 뒤 도커허브에 푸시합니다.
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub',
                    usernameVariable: 'REG_USER',
                    passwordVariable: 'REG_PASS'
                )]){
                    sh '''
                        echo $REG_PASS | docker login -u $REG_USER --password-stdin
                    '''
                    script {
                        def branch = env.GIT_BRANCH     // SCM 사용 시 제공되는 브랜치명을 조건문의 분기로 사용

                        // test/jenkins 부분의 경우 파이프라인이 완성되었다고 판단되면 삭제할 예정입니다.
                        // 동작 자체는 dev 브랜치의 경우와 동일합니다.
                        if (branch == 'test/jenkins' || branch == 'origin/test/jenkins') {
                            sh(label: 'Docker build & push (latest)', script: '''
                            set -euxo pipefail
                            docker build -t ${DEV_IMAGE_NAME}:latest .
                            docker push  ${DEV_IMAGE_NAME}:latest
                        ''')
                        }

                        // DEV_IMAGE_NAME 이름으로 이미지를 빌드한 뒤 도커 허브에 푸시합니다.
                        if (branch == 'dev' || branch == 'origin/dev') {
                            sh(label: 'Docker build & push (latest)', script: '''
                            set -euxo pipefail
                            docker build -t ${DEV_IMAGE_NAME}:latest .
                            docker push  ${DEV_IMAGE_NAME}:latest
                        ''')
                        }

                        // MAIN_IMAGE_NAME 이름으로 이미지를 빌드한 뒤 도커 허브에 푸시합니다.
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

                    // test/jenkins 부분의 경우 파이프라인이 완성되었다고 판단되면 삭제할 예정입니다.
                    // 동작 자체는 dev 브랜치의 경우와 동일합니다.
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

                    // dev 브랜치 : 온 프레미스에서 docker-compose로 작동하고 있는 컨테이너를 업데이트합니다.
                    // 헬스체크 후 응답이 있을 시 더 이상의 요청이 들어오는 것을 차단한 후 모든 요청이 완료되었을 시 compose up 합니다.
                    if (branch == 'dev' || branch == 'origin/dev') {
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
