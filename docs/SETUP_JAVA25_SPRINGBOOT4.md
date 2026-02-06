# Java 25 + Spring Boot 4.0 + Spring Batch 6.x 설정 가이드

## ✅ 적용된 버전

```
Java JDK: 25
Spring Boot: 4.0.0-M1 (Milestone 1)
Spring Batch: 6.x (Spring Boot 4.0에 포함)
QueryDSL: 5.1.0
Lombok: 1.18.36
```

---

## 🚀 IntelliJ IDEA 설정

### 1️⃣ Java 25 설치 (필수!)

**Java 25는 EA(Early Access) 버전입니다.**

다운로드:
- [Oracle JDK 25 EA](https://jdk.java.net/25/)
- [OpenJDK 25 EA](https://jdk.java.net/25/)

설치 후 시스템 환경변수 설정:
```
JAVA_HOME=C:\Program Files\Java\jdk-25
PATH=%JAVA_HOME%\bin
```

터미널에서 확인:
```bash
java -version
# 출력: java version "25-ea" ...
```

---

### 2️⃣ IntelliJ Project Structure 설정

**File** → **Project Structure** (Ctrl+Alt+Shift+S)

#### Project 탭
- **SDK**: Java 25 선택
- **Language level**: 25 (Preview features)

#### Modules 탭
- **Language level**: 25

---

### 3️⃣ IntelliJ Gradle 설정

**File** → **Settings** (Ctrl+Alt+S)

**Build, Execution, Deployment** → **Build Tools** → **Gradle**

설정:
- **Gradle JVM**: Java 25 선택
- **Build and run using**: Gradle
- **Run tests using**: Gradle

---

### 4️⃣ Annotation Processing 활성화

**File** → **Settings** → **Compiler** → **Annotation Processors**

반드시 체크 ✅:
- **Enable annotation processing**
- **Obtain processors from project classpath**

---

### 5️⃣ Lombok 플러그인 설치

**File** → **Settings** → **Plugins**

1. 검색창에 **"Lombok"** 입력
2. **Lombok** 플러그인 설치
3. **Apply** → **OK**
4. **IntelliJ 재시작**

---

### 6️⃣ Gradle 프로젝트 로드

1. **build.gradle** 파일 열기
2. 상단에 나타나는 배너에서 **"Load Gradle Changes"** 클릭
3. Gradle이 의존성을 다운로드합니다 (1-3분 소요)

**진행 상황**: 우측 하단 상태바 확인

---

### 7️⃣ 프로젝트 재빌드

**Build** → **Rebuild Project**

또는 Gradle 탭에서:
- **Tasks** → **build** → **clean** 더블클릭
- **Tasks** → **build** → **build** 더블클릭

---

## ✅ 정상 작동 확인

다음 코드가 **빨간 줄 없이** 인식되어야 합니다:

```java
@Slf4j                    // ✅ Lombok
@Configuration            // ✅ Spring
@RequiredArgsConstructor  // ✅ Lombok
@EnableBatchProcessing    // ✅ Spring Batch 6.x
public class BatchConfig {
    
    @Bean                 // ✅ Spring
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        log.info("...");  // ✅ log 변수 인식
        return null;
    }
}
```

---

## 🔧 주요 설정 내용

### build.gradle

```gradle
// Java 25
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// Spring Boot 4.0.0-M1
plugins {
    id 'org.springframework.boot' version '4.0.0-M1'
}

// Milestone Repository 추가
repositories {
    mavenCentral()
    maven { url 'https://repo.spring.io/milestone' }
}

// Java 25 Preview Features 활성화
tasks.withType(JavaCompile).configureEach {
    options.compilerArgs += [
        '-parameters',
        '--enable-preview'
    ]
}
```

---

## 📊 Spring Batch 6.x API

### Job 정의

```java
@Bean
public Job orderPrvaMaskJob(JobRepository jobRepository, Step step) {
    return new JobBuilder("orderPrvaMaskJob", jobRepository)
        .start(step)
        .listener(listener)
        .build();
}
```

### Step 정의

```java
@Bean
public Step orderPrvaMaskStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              Tasklet tasklet) {
    return new StepBuilder("orderPrvaMaskStep", jobRepository)
        .tasklet(tasklet, transactionManager)
        .build();
}
```

### @EnableBatchProcessing

Spring Batch 6.x에서는 `@EnableBatchProcessing`을 사용합니다:

```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    // ...
}
```

---

## ⚠️ 문제 해결

### Q1. Java 25를 찾을 수 없습니다

**해결**:
1. Java 25 EA 버전 설치 확인
2. IntelliJ 재시작
3. **File** → **Project Structure** → **SDKs** → **+** → Java 25 추가

### Q2. Spring Boot 4.0.0-M1을 다운로드할 수 없습니다

**해결**:
build.gradle에 Milestone Repository가 추가되어 있는지 확인:
```gradle
repositories {
    mavenCentral()
    maven { url 'https://repo.spring.io/milestone' }
}
```

### Q3. @Slf4j가 인식되지 않습니다

**해결**:
1. Lombok 플러그인 설치 확인
2. Annotation Processing 활성화 확인
3. IntelliJ 재시작
4. **File** → **Invalidate Caches / Restart**

### Q4. --enable-preview 오류

**해결**:
Java 25 preview features가 필요합니다. build.gradle에 이미 추가되어 있습니다:
```gradle
tasks.withType(JavaCompile).configureEach {
    options.compilerArgs += ['--enable-preview']
}
```

### Q5. 빌드는 성공하는데 IDE에서 빨간 줄

**해결**:
```
File → Invalidate Caches / Restart...
→ Invalidate and Restart
```

---

## 📋 설정 체크리스트

순서대로 확인하세요:

- [ ] Java 25 EA 설치됨
- [ ] JAVA_HOME 환경변수 설정됨
- [ ] IntelliJ Project Structure에서 Java 25 선택
- [ ] Gradle JVM을 Java 25로 설정
- [ ] Annotation Processing 활성화
- [ ] Lombok 플러그인 설치
- [ ] IntelliJ 재시작
- [ ] Gradle 프로젝트 로드 (Load Gradle Changes)
- [ ] Build → Rebuild Project
- [ ] @Slf4j, @Configuration 등 빨간 줄 사라짐

---

## 🎯 터미널에서 빌드

IntelliJ 터미널에서:

```bash
# Windows
gradlew clean build

# Mac/Linux
./gradlew clean build
```

성공 시:
```
BUILD SUCCESSFUL in 30s
```

JAR 파일 생성:
```
build/libs/hc_job_batch-1.0.0-SNAPSHOT.jar
```

---

## 📚 참고 자료

- [Spring Boot 4.0.0-M1 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0.0-M1-Release-Notes)
- [Spring Batch 6.0 Documentation](https://docs.spring.io/spring-batch/docs/current/reference/html/)
- [Java 25 EA Release Notes](https://jdk.java.net/25/release-notes)

---

**설정 완료일**: 2025-02-05
**버전**: Java 25 + Spring Boot 4.0.0-M1 + Spring Batch 6.x
