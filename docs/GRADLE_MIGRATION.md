# Maven → Gradle 마이그레이션 완료

## ✅ 생성된 파일

- `build.gradle` - Gradle 빌드 스크립트 (Groovy DSL)
- `settings.gradle` - 프로젝트 설정
- `gradle.properties` - Gradle 속성 (성능 최적화)

---

## 🚀 IntelliJ에서 Gradle 프로젝트로 전환하기

### 1단계: Gradle Wrapper 생성

IntelliJ 터미널에서 실행:

```bash
# Gradle이 설치되어 있는 경우
gradle wrapper --gradle-version 9.1

# 또는 IntelliJ가 자동으로 Gradle Wrapper를 생성하도록 대기
```

### 2단계: IntelliJ에서 프로젝트 새로고침

1. **View** → **Tool Windows** → **Gradle** 열기
2. Gradle 탭에서 **🔄 Reload All Gradle Projects** 클릭

또는:

1. **build.gradle** 파일 열기
2. 파일 상단에 나타나는 **Load Gradle Changes** 배너 클릭

### 3단계: Gradle 설정 확인

**Settings** → **Build, Execution, Deployment** → **Build Tools** → **Gradle**

- **Gradle JVM**: Java 25 선택
- **Build and run using**: Gradle
- **Run tests using**: Gradle

### 4단계: 빌드 테스트

IntelliJ 터미널에서:

```bash
# Windows
gradlew.bat clean build

# Mac/Linux
./gradlew clean build
```

---

## 📊 Maven vs Gradle 명령어 비교

| 작업 | Maven | Gradle |
|-----|-------|--------|
| **빌드** | `mvn clean package` | `./gradlew clean build` |
| **테스트** | `mvn test` | `./gradlew test` |
| **실행** | `mvn spring-boot:run` | `./gradlew bootRun` |
| **JAR 생성** | `mvn package` | `./gradlew bootJar` |
| **의존성 확인** | `mvn dependency:tree` | `./gradlew dependencies` |

---

## 🎯 주요 변경 사항

### 의존성 스코프 변환

| Maven | Gradle |
|-------|--------|
| `<scope>compile</scope>` | `implementation` |
| `<scope>runtime</scope>` | `runtimeOnly` |
| `<scope>provided</scope>` | `compileOnly` |
| `<scope>test</scope>` | `testImplementation` |

### QueryDSL 설정

**Maven (pom.xml):**
```xml
<plugin>
    <groupId>com.mysema.maven</groupId>
    <artifactId>apt-maven-plugin</artifactId>
</plugin>
```

**Gradle (build.gradle):**
```groovy
def querydslDir = layout.buildDirectory.dir("generated/querydsl").get().asFile

sourceSets {
    main.java.srcDirs += querydslDir
}

tasks.withType(JavaCompile).configureEach {
    options.getGeneratedSourceOutputDirectory().set(file(querydslDir))
}
```

---

## 🔧 Job 실행 방법

### Gradle 사용

```bash
# bootRun으로 실행
./gradlew bootRun --args='--spring.batch.job.name=orderPrvaMaskJob BASE_DT=20250203 PROC_CD=COMP'

# JAR 빌드 후 실행
./gradlew bootJar
java -jar build/libs/hc_job_batch-1.0.0-SNAPSHOT.jar --spring.batch.job.name=orderPrvaMaskJob BASE_DT=20250203 PROC_CD=COMP
```

### IntelliJ Run Configuration

1. **Run** → **Edit Configurations...**
2. **+** 클릭 → **Gradle** 선택
3. **Tasks**: `bootRun`
4. **Arguments**: `--args='--spring.batch.job.name=orderPrvaMaskJob BASE_DT=20250203 PROC_CD=COMP'`

---

## 🐛 문제 해결

### Q1. QueryDSL Q클래스가 생성되지 않습니다

```bash
# 클린 빌드
./gradlew clean compileJava

# IntelliJ: Build → Rebuild Project
```

생성 위치: `build/generated/querydsl/`

### Q2. Gradle Wrapper가 없습니다

IntelliJ가 자동으로 생성하지 않은 경우:

```bash
# Gradle 설치 후
gradle wrapper --gradle-version 9.1

# 권한 부여 (Mac/Linux)
chmod +x gradlew
```

### Q3. Java 25를 인식하지 못합니다

**IntelliJ 설정:**
1. **File** → **Project Structure** → **Project**
2. **SDK**: Java 25 선택
3. **Language level**: 25

**build.gradle 확인:**
```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

### Q4. 빌드가 느립니다

**gradle.properties 최적화 (이미 설정됨):**
```properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=true
```

---

## ✨ Gradle의 장점

1. **빌드 속도**: Maven 대비 30-50% 빠름
   - 증분 빌드 (변경된 부분만 빌드)
   - 빌드 캐싱
   - 병렬 실행

2. **유연성**: Groovy/Kotlin DSL로 복잡한 빌드 로직 작성 가능

3. **최신 기능**: Spring Boot 4.0 공식 권장

4. **성능 최적화**: Daemon, 캐싱, 병렬 처리

---

## 📝 다음 단계

### 1. pom.xml 제거 (선택)

Gradle로 완전히 전환 후:

```bash
# pom.xml 백업
mv pom.xml pom.xml.bak
```

### 2. CI/CD 파이프라인 업데이트

기존 Maven 명령어를 Gradle로 변경:

```yaml
# GitHub Actions 예시
- name: Build with Gradle
  run: ./gradlew clean build
```

### 3. IDE 플러그인 확인

IntelliJ에서 Gradle 플러그인이 활성화되어 있는지 확인:
- **Settings** → **Plugins** → "Gradle" 검색

---

## 📚 참고 자료

- [Gradle 공식 문서](https://docs.gradle.org/)
- [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/gradle-plugin/)
- [Maven to Gradle Migration Guide](https://docs.gradle.org/current/userguide/migrating_from_maven.html)

---

**마이그레이션 완료일**: 2025-02-05  
**Gradle 버전**: 9.1  
**Spring Boot**: 4.0.1
