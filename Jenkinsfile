pipeline {
    agent any

    environment {
        REGISTRY   = 'docker.io'                                // docker hub 사용
        MAIN_IMAGE_NAME = 'docker.io/teenyfinny/channel'        // 메인 브랜치에서 다룰 이미지 이름
        DEV_IMAGE_NAME = 'docker.io/teenyfinny/channeltest'     // 테스트 브랜치에서 다룰 이미지 이름
        TEST_APP_NAME = 'sw_team_3_channel'                     // 테스트 서버에서의 컨테이너명
        MAIN_APP_NAME = 'channel-server'                     // 운영 서버에서의 컨테이너명
    }

    stages {
        stage('CI : checkout') {
            steps {
                echo '파이프라인이 시작되었습니다.'
                echo '체크아웃이 시작되었습니다.'
                echo '해당 단계에서 실패 시 CI/CD 담당자에게 문의해주세요.'
                cleanWs()       // 워크 스페이스를 정리합니다.
                checkout scm    // 자동으로 webhook 브랜치를 체크아웃
                echo '체크아웃이 완료되었습니다.'
            }
        }
//
//         stage('CI : gradle 빌드 수행') {
//             steps{      // gradlew에 권한을 부여하고 클린 빌드를 수행합니다.
//                 echo '빌드가 시작되었습니다.'
//                 echo '해당 단계에서 실패 시 실패 로그를 확인해주세요.'
//                 sh '''
// set -euxo pipefail
// chmod +x ./gradlew
//
// ./gradlew clean build
//                 '''
//                 echo '빌드가 완료되었습니다.'
//             }
//         }
//
//         stage('CI : 소나큐브 정적분석') {
//             steps {     // Jenkins Credential에 저장된 소나큐브 관련 정보를 바탕으로, 정적 분석을 의뢰합니다.
//                 withSonarQubeEnv('sonarqube-server') {
//                     echo '정적분석이 시작되었습니다.'
//                     echo '해당 단계에서 실패 시 소나큐브를 확인해주세요.'
//                     echo '소나큐브 : 192.168.0.79:8251, ID/PW : 슬랙의 계정 정보 확인'
//                     sh '''
// set -euxo pipefail
// chmod +x ./gradlew
//
// ./gradlew sonar -Dsonar.token=$SONAR_AUTH_TOKEN -Dsonar.host.url=$SONAR_HOST_URL
//                     '''
//                     echo '정적분석이 완료되었습니다.'
//                 }
//             }
//         }
//
//         stage('CI : 퀄리티 체크 수행') {
//             steps{      // 정적 분석한 리포트를 바탕으로 퀄리티 체크를 수행합니다.
//                 timeout(time: 1, unit: 'MINUTES') {
//                     script{
//                         echo '퀄리티 체크가 시작되었습니다.'
//                         echo '해당 단계에서 실패 시 소나큐브를 확인해주세요.'
//                         echo '소나큐브 : 192.168.0.79:8251, ID/PW : 슬랙의 계정 정보 확인'
//                         def qg = waitForQualityGate()
//                         echo "Status: ${qg.status}"
//                         if(qg.status != 'OK') {
//                             echo "NOT OK Status: ${qg.status}"
//                             error "Pipeline aborted due to quality gate failure: ${qg.status}"
//                         } else{
//                             echo "status: ${qg.status}"
//                         }
//                         echo '퀄리티 체크가 완료되었습니다.'
//                     }
//                 }
//             }
//         }
//
//         stage('CD : main 브랜치 이미지 빌드 & 도커 허브 푸시') {
//             when {
//                 anyOf{
//                     branch 'main'
//                 }
//             }
//             steps {
//                 echo 'main branch : 도커 이미지 빌드 & 푸시가 시작되었습니다.'
//                 echo 'main branch : 해당 단계에서 실패 시 CI/CD 담당자에게 문의해주세요.'
//                 withCredentials([usernamePassword(
//                     credentialsId: 'docker-hub',
//                     usernameVariable: 'REG_USER',
//                     passwordVariable: 'REG_PASS'
//                 )]){
//                     sh(label: 'Docker build & push (latest)', script: '''
//                         set -euxo pipefail
//                         echo $REG_PASS | docker login -u $REG_USER --password-stdin
//                         docker build -t ${MAIN_IMAGE_NAME}:latest .
//                         docker push  ${MAIN_IMAGE_NAME}:latest
//                     ''')
//                 }
//                 echo 'main branch : 도커 이미지 빌드 & 푸시가 완료되었습니다.'
//             }
//         }
//
//         stage('CD : dev 브랜치 이미지 빌드 & 도커 허브 푸시') {
//             when {
//                 anyOf{
//                     branch 'dev'
//                 }
//             }
//             steps {
//                 echo 'dev branch : 도커 이미지 빌드 & 푸시가 시작되었습니다.'
//                 echo 'dev branch : 해당 단계에서 실패 시 CI/CD 담당자에게 문의해주세요.'
//                 withCredentials([usernamePassword(
//                     credentialsId: 'docker-hub',
//                     usernameVariable: 'REG_USER',
//                     passwordVariable: 'REG_PASS'
//                 )]){
//                     sh(label: 'Docker build & push (latest)', script: '''
//                         set -euxo pipefail
//                         echo $REG_PASS | docker login -u $REG_USER --password-stdin
//                         docker build -t ${DEV_IMAGE_NAME}:latest .
//                         docker push  ${DEV_IMAGE_NAME}:latest
//                     ''')
//                 }
//                 echo 'dev branch : 도커 이미지 빌드 & 푸시가 완료되었습니다.'
//             }
//         }

        stage('CD : main 브랜치 이미지를 운영서버에서 배포') {
            when {
                anyOf{
                    branch 'main'
                    branch 'feat/CI-CD'
                }
            }
            steps {
                echo 'main branch : 배포가 시작되었습니다.'
                echo 'main branch : 해당 단계에서 실패 시 CI/CD 담당자에게 문의해주세요.'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'docker-hub',
                        usernameVariable: 'REG_USER',
                        passwordVariable: 'REG_PASS'
                    ),
                    // 2) Bastion 서버 접속용 SSH 키
                    sshUserPrivateKey(
                        credentialsId: 'aws-bastion-key',   // Jenkins에 미리 만들어 둔 ID
                        keyFileVariable: 'BASTION_KEY',     // 쉘에서 쓸 파일 경로 변수명
                        usernameVariable: 'BASTION_USER'    // 쉘에서 쓸 유저명 변수명
                    ),
                ]) {
                    sh """
                       ssh -o StrictHostKeyChecking=no \
                           -i "$BASTION_KEY" \
                           "$BASTION_USER"@ec2-15-165-208-216.ap-northeast-2.compute.amazonaws.com << 'EOSSH'

echo "[bastion] \$(hostname)"

# 첫 번째 운영 서버
ssh -o StrictHostKeyChecking=no \\
   -i ~/.ssh/sw-team-3-bastion-rsa.pem \\
   ubuntu@172.31.66.7 << 'EOSSH_PRIV1'

echo "[private-1] \$(hostname)"
sudo docker ps

# Jenkins credentials/env를 Groovy가 먼저 치환
echo '${env.REG_PASS}' | sudo docker login -u '${env.REG_USER}' --password-stdin
sudo docker pull '${env.MAIN_IMAGE_NAME}':latest

# 0) 헬스체크 - 서버가 살아있는지 확인
echo "[health-check] Checking server health..."
health_status=\$(curl -s --connect-timeout 2 --max-time 3 "http://127.0.0.1:8080/actuator/health" 2>/dev/null | jq -r '.status' 2>/dev/null)

if [ "\$health_status" = "UP" ]; then
    echo "[health-check] Server is healthy. Proceeding with graceful shutdown..."

    # 1) readiness OFF 요청 보내고 응답 출력
    echo "[readiness/off] request"
    curl -XPOST "http://127.0.0.1:8080/internal/readiness/off" || echo "[readiness/off] curl failed: \$?"
    echo ""  # 줄바꿈

    # 2) drain 루프 - 매번 응답 JSON 출력
    echo "[drain] start polling..."
    while true; do
        resp="\$(curl -s "http://127.0.0.1:8080/actuator/drain")"
        echo "[drain] response: \${resp}"

        echo "\${resp}" | jq -e '.drained == true' >/dev/null 2>&1 && {
            echo "[drain] drained == true, continue pipeline."
            break
        }

        echo "[drain] Waiting to drain..."
        sleep 1
    done
fi

# 4) 새 컨테이너 실행 (백그라운드)
# 4) 기존 컨테이너 종료 & 삭제 (완료될 때까지 대기)
if sudo docker ps -a --format '{{.Names}}' | grep -q "^${env.MAIN_APP_NAME}\$"; then
  echo "[docker] Stopping existing container: ${env.MAIN_APP_NAME}"
  sudo docker stop ${env.MAIN_APP_NAME}

  # 완전히 내려갈 때까지 대기 (실행 중 컨테이너 목록에서 사라질 때까지)
  while sudo docker ps --format '{{.Names}}' | grep -q "^${env.MAIN_APP_NAME}\$"; do
    echo "[docker] Waiting for container to stop..."
    sleep 1
  done

  echo "[docker] Removing existing container: ${env.MAIN_APP_NAME}"
  sudo docker rm -f ${env.MAIN_APP_NAME}

  # 완전히 삭제될 때까지 대기 (모든 컨테이너 목록에서 사라질 때까지)
  while sudo docker ps -a --format '{{.Names}}' | grep -q "^${env.MAIN_APP_NAME}\$"; do
    echo "[docker] Waiting for container to be removed..."
    sleep 1
  done
else
  echo "[docker] No existing container named ${env.MAIN_APP_NAME}"
fi


sudo docker run -d \
  --name channel-server \
  -p 8080:8080 \
  -e TZ=Asia/Seoul \
  --restart unless-stopped \
  -e SPRING_PROFILES_ACTIVE=secret \
  -v /home/ubuntu/app-config/application-secret.yml:/config/application-secret.yml \
  teenyfinny/channel:latest

# 5) 상태 확인
# 5) 배포 후 health check (actuator/health = UP 될 때까지 대기)
echo "[post-deploy] Waiting for actuator health = UP..."

max_retries=60   # 최대 60번 (2분 정도)
retry=0
health_status="UNKNOWN"

while [ "\$retry" -lt "\$max_retries" ]; do
  health_status=\$(curl -s --connect-timeout 2 --max-time 3 \
    "http://127.0.0.1:8080/actuator/health" 2>/dev/null | jq -r '.status' 2>/dev/null)

  if [ "\$health_status" = "UP" ]; then
    echo "[post-deploy] Server is UP (actuator/health)."
    break
  fi

  echo "[post-deploy] Current status=\${health_status:-UNKNOWN}, retry=\$((retry+1))/\$max_retries"
  retry=\$((retry+1))
  sleep 2
done

if [ "\$health_status" != "UP" ]; then
  echo "[post-deploy] Server did NOT become healthy within timeout."
  exit 1   # 여기서 ssh 종료 → Jenkins stage 실패
fi

EOSSH_PRIV1

# 두 번째 운영 서버
ssh -o StrictHostKeyChecking=no \\
   -i ~/.ssh/sw-team-3-bastion-rsa.pem \\
   ubuntu@172.31.38.144 << 'EOSSH_PRIV2'

echo "[private-2] \$(hostname)"
sudo docker ps

# Jenkins credentials/env를 Groovy가 먼저 치환
echo '${env.REG_PASS}' | sudo docker login -u '${env.REG_USER}' --password-stdin
sudo docker pull '${env.MAIN_IMAGE_NAME}':latest

# 0) 헬스체크 - 서버가 살아있는지 확인
echo "[health-check] Checking server health..."
health_status=\$(curl -s --connect-timeout 2 --max-time 3 "http://127.0.0.1:8080/actuator/health" 2>/dev/null | jq -r '.status' 2>/dev/null)

if [ "\$health_status" = "UP" ]; then
    echo "[health-check] Server is healthy. Proceeding with graceful shutdown..."

    # 1) readiness OFF 요청 보내고 응답 출력
    echo "[readiness/off] request"
    curl -XPOST "http://127.0.0.1:8080/internal/readiness/off" || echo "[readiness/off] curl failed: \$?"
    echo ""  # 줄바꿈

    # 2) drain 루프 - 매번 응답 JSON 출력
    echo "[drain] start polling..."
    while true; do
        resp="\$(curl -s "http://127.0.0.1:8080/actuator/drain")"
        echo "[drain] response: \${resp}"

        echo "\${resp}" | jq -e '.drained == true' >/dev/null 2>&1 && {
            echo "[drain] drained == true, continue pipeline."
            break
        }

        echo "[drain] Waiting to drain..."
        sleep 1
    done
fi

# 4) 새 컨테이너 실행 (백그라운드)
# 4) 기존 컨테이너 종료 & 삭제 (완료될 때까지 대기)
if sudo docker ps -a --format '{{.Names}}' | grep -q "^${env.MAIN_APP_NAME}\$"; then
  echo "[docker] Stopping existing container: ${env.MAIN_APP_NAME}"
  sudo docker stop ${env.MAIN_APP_NAME}

  # 완전히 내려갈 때까지 대기 (실행 중 컨테이너 목록에서 사라질 때까지)
  while sudo docker ps --format '{{.Names}}' | grep -q "^${env.MAIN_APP_NAME}\$"; do
    echo "[docker] Waiting for container to stop..."
    sleep 1
  done

  echo "[docker] Removing existing container: ${env.MAIN_APP_NAME}"
  sudo docker rm -f ${env.MAIN_APP_NAME}

  # 완전히 삭제될 때까지 대기 (모든 컨테이너 목록에서 사라질 때까지)
  while sudo docker ps -a --format '{{.Names}}' | grep -q "^${env.MAIN_APP_NAME}\$"; do
    echo "[docker] Waiting for container to be removed..."
    sleep 1
  done
else
  echo "[docker] No existing container named ${env.MAIN_APP_NAME}"
fi


sudo docker run -d \
  --name channel-server \
  -p 8080:8080 \
  -e TZ=Asia/Seoul \
  --restart unless-stopped \
  -e SPRING_PROFILES_ACTIVE=secret \
  -v /home/ubuntu/app-config/application-secret.yml:/config/application-secret.yml \
  teenyfinny/channel:latest

# 5) 상태 확인
# 5) 배포 후 health check (actuator/health = UP 될 때까지 대기)
echo "[post-deploy] Waiting for actuator health = UP..."

max_retries=60   # 최대 60번 (2분 정도)
retry=0
health_status="UNKNOWN"

while [ "\$retry" -lt "\$max_retries" ]; do
  health_status=\$(curl -s --connect-timeout 2 --max-time 3 \
    "http://127.0.0.1:8080/actuator/health" 2>/dev/null | jq -r '.status' 2>/dev/null)

  if [ "\$health_status" = "UP" ]; then
    echo "[post-deploy] Server is UP (actuator/health)."
    break
  fi

  echo "[post-deploy] Current status=\${health_status:-UNKNOWN}, retry=\$((retry+1))/\$max_retries"
  retry=\$((retry+1))
  sleep 2
done

if [ "\$health_status" != "UP" ]; then
  echo "[post-deploy] Server did NOT become healthy within timeout."
  exit 1   # 여기서 ssh 종료 → Jenkins stage 실패
fi

EOSSH_PRIV2

EOSSH
                    """

                }

                echo 'main branch : 배포가 완료되었습니다.'
            }
        }

        stage('CD : dev 브랜치 이미지를 온프레미스에서 배포') {
            when {
                anyOf{
                    branch 'dev'
                    branch 'feat/CI-CD'
                }
            }
            steps {
                echo 'dev branch : 배포가 시작되었습니다.'
                echo 'dev branch : 해당 단계에서 실패 시 CI/CD 담당자에게 문의해주세요.'

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

                echo 'dev branch : 배포가 완료되었습니다.'
            }
        }
    } // end of stages
} // end of pipeline
