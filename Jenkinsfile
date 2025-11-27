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
                echo '파이프라인이 시작되었습니다.'
                echo '체크아웃이 시작되었습니다.'
                echo '해당 단계에서 실패 시 CI/CD 담당자에게 문의해주세요.'
                cleanWs()       // 워크 스페이스를 정리합니다.
                checkout scm    // 자동으로 webhook 브랜치를 체크아웃
                echo '체크아웃이 완료되었습니다.'
            }
        }

        stage('CI : gradle 빌드 수행') {
            steps{      // gradlew에 권한을 부여하고 클린 빌드를 수행합니다.
                echo '빌드가 시작되었습니다.'
                echo '해당 단계에서 실패 시 실패 로그를 확인해주세요.'
                sh '''
set -euxo pipefail
chmod +x ./gradlew

./gradlew clean build
                '''
                echo '빌드가 완료되었습니다.'
            }
        }

        stage('CI : 소나큐브 정적분석') {
            steps {     // Jenkins Credential에 저장된 소나큐브 관련 정보를 바탕으로, 정적 분석을 의뢰합니다.
                withSonarQubeEnv('sonarqube-server') {
                    echo '정적분석이 시작되었습니다.'
                    echo '해당 단계에서 실패 시 소나큐브를 확인해주세요.'
                    echo '소나큐브 : 192.168.0.79:8251, ID/PW : 슬랙의 계정 정보 확인'
                    sh '''
set -euxo pipefail
chmod +x ./gradlew

./gradlew sonar -Dsonar.token=$SONAR_AUTH_TOKEN -Dsonar.host.url=$SONAR_HOST_URL
                    '''
                    echo '정적분석이 완료되었습니다.'
                }
            }
        }

        stage('CI : 퀄리티 체크 수행') {
            steps{      // 정적 분석한 리포트를 바탕으로 퀄리티 체크를 수행합니다.
                timeout(time: 1, unit: 'MINUTES') {
                    script{
                        echo '퀄리티 체크가 시작되었습니다.'
                        echo '해당 단계에서 실패 시 소나큐브를 확인해주세요.'
                        echo '소나큐브 : 192.168.0.79:8251, ID/PW : 슬랙의 계정 정보 확인'
                        def qg = waitForQualityGate()
                        echo "Status: ${qg.status}"
                        if(qg.status != 'OK') {
                            echo "NOT OK Status: ${qg.status}"
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        } else{
                            echo "status: ${qg.status}"
                        }
                        echo '퀄리티 체크가 완료되었습니다.'
                    }
                }
            }
        }

        stage('CD : main 브랜치 이미지 빌드 & 도커 허브 푸시') {
            when {
                anyOf{
                    branch 'main'
                    // branch 'test/jenkins'
                }
            }
            steps {
                echo 'main branch : 도커 이미지 빌드 & 푸시가 시작되었습니다.'
                echo 'main branch : 해당 단계에서 실패 시 CI/CD 담당자에게 문의해주세요.'
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub',
                    usernameVariable: 'REG_USER',
                    passwordVariable: 'REG_PASS'
                )]){
                    sh(label: 'Docker build & push (latest)', script: '''
                        set -euxo pipefail
                        echo $REG_PASS | docker login -u $REG_USER --password-stdin
                        docker build -t ${MAIN_IMAGE_NAME}:latest .
                        docker push  ${MAIN_IMAGE_NAME}:latest
                    ''')
                }
                echo 'main branch : 도커 이미지 빌드 & 푸시가 완료되었습니다.'
            }
        }

        stage('CD : dev 브랜치 이미지 빌드 & 도커 허브 푸시') {
            when {
                anyOf{
                    branch 'dev'
                    branch 'test/jenkins'
                }
            }
            steps {
                echo 'dev branch : 도커 이미지 빌드 & 푸시가 시작되었습니다.'
                echo 'dev branch : 해당 단계에서 실패 시 CI/CD 담당자에게 문의해주세요.'
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub',
                    usernameVariable: 'REG_USER',
                    passwordVariable: 'REG_PASS'
                )]){
                    sh(label: 'Docker build & push (latest)', script: '''
                        set -euxo pipefail
                        echo $REG_PASS | docker login -u $REG_USER --password-stdin
                        docker build -t ${DEV_IMAGE_NAME}:latest .
                        docker push  ${DEV_IMAGE_NAME}:latest
                    ''')
                }
                echo 'dev branch : 도커 이미지 빌드 & 푸시가 완료되었습니다.'
            }
        }

        stage('CD : main 브랜치 이미지를 운영서버에서 배포') {
            when {
                anyOf{
                    branch 'main'
                    //branch 'test/jenkins'
                }
            }
            steps {
                echo 'main branch : 배포가 시작되었습니다.'
                echo 'main branch : 해당 단계에서 실패 시 CI/CD 담당자에게 문의해주세요.'
                echo 'main branch : 배포가 완료되었습니다.'
            }
        }

        stage('CD : dev 브랜치 이미지를 온프레미스에서 배포') {
            when {
                anyOf{
                    branch 'dev'
                    branch 'test/jenkins'
                }
            }
            steps {
                echo 'dev branch : 배포가 시작되었습니다.'
                echo 'dev branch : 해당 단계에서 실패 시 CI/CD 담당자에게 문의해주세요.'
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
                echo 'dev branch : 배포가 완료되었습니다.'
            }
        }
    } // end of stages
} // end of pipeline
