#!/bin/bash

##############################################################################
# Spring Batch Job 실행 스크립트 (Gradle)
#
# 사용법:
#   ./run-job.sh <JOB_NAME> [PARAMETERS]
#
# 예시:
#   ./run-job.sh orderPrvaMaskJob BASE_DT=20250203 PROC_CD=COMP
#   ./run-job.sh dayGoodsSumJob BASE_DT=20250203
##############################################################################

# 환경 설정
APP_HOME=$(cd "$(dirname "$0")" && pwd)
APP_NAME="hc_job_batch"
JAR_FILE="$APP_HOME/build/libs/${APP_NAME}-1.0.0-SNAPSHOT.jar"
PROFILE="prod"

# JVM 옵션
JAVA_OPTS="-Xms512m -Xmx2048m"
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$PROFILE"
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"
JAVA_OPTS="$JAVA_OPTS -Duser.timezone=Asia/Seoul"

# Job 파라미터
JOB_NAME=$1
shift
JOB_PARAMS="$@"

# 실행 시간 추가 (중복 실행 방지)
RUN_DATE=$(date +"%Y%m%d%H%M%S")
JOB_PARAMS="$JOB_PARAMS RUN_DATE=$RUN_DATE"

# 로그 디렉토리 생성
LOG_DIR="$APP_HOME/logs"
mkdir -p $LOG_DIR

# JAR 파일이 없으면 빌드
if [ ! -f "$JAR_FILE" ]; then
    echo "JAR 파일이 없습니다. 빌드를 시작합니다..."
    ./gradlew clean bootJar

    if [ $? -ne 0 ]; then
        echo "빌드 실패"
        exit 1
    fi
fi

# Job 실행
echo "==================================================================="
echo "Spring Batch Job 실행 (Gradle)"
echo "==================================================================="
echo "Job Name: $JOB_NAME"
echo "Parameters: $JOB_PARAMS"
echo "Profile: $PROFILE"
echo "Started at: $(date '+%Y-%m-%d %H:%M:%S')"
echo "==================================================================="

java $JAVA_OPTS \
     -jar $JAR_FILE \
     --spring.batch.job.name=$JOB_NAME \
     $JOB_PARAMS

EXIT_CODE=$?

echo "==================================================================="
echo "Job 실행 완료"
echo "Exit Code: $EXIT_CODE"
echo "Finished at: $(date '+%Y-%m-%d %H:%M:%S')"
echo "==================================================================="

exit $EXIT_CODE
