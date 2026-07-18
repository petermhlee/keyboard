# 천지인 키보드 (안드로이드 IME) — v1

PC 없이 GitHub Actions로 APK를 빌드해 폰에 설치하는 실제 시스템 키보드.
v1은 입력(천지인 조합 + 영어 QWERTY)만 지원. 오타 자동수정은 v2에서 이식 예정.

## 폴더 구조
```
settings.gradle
build.gradle
gradle.properties
app/
  build.gradle
  src/main/
    AndroidManifest.xml
    res/xml/method.xml
    res/values/strings.xml
    java/com/cheonjiin/kbd/CheonjiinIME.kt
.github/workflows/build.yml
```

## 1) GitHub에 올리기
- 새 저장소 생성 후 위 구조 그대로 파일을 만든다.
  (웹 편집기에서 "Create new file" → 파일명에 `app/src/main/...` 처럼 경로를 그대로 입력하면 폴더가 생성됨)

## 2) 자동 빌드
- 파일을 push하면 Actions가 자동 실행.
- 저장소 → **Actions** 탭 → 최신 실행 → **Artifacts**의 `cheonjiin-debug-apk` 다운로드.

## 3) 폰에 설치
- 다운로드한 zip을 풀면 `app-debug.apk`.
- 탭해서 설치 (브라우저/파일앱에 "출처를 알 수 없는 앱 설치" 허용 필요).

## 4) 키보드 활성화
- 설정 → 일반(또는 시스템) → 언어 및 입력 → 화면 키보드 → **천지인 키보드** 켜기
- 아무 입력창에서 키보드 전환(하단 키보드 아이콘) → 천지인 키보드 선택

## 빌드가 실패하면
- Actions 로그의 빨간 단계 내용을 그대로 알려주면 버전/설정을 맞춰 고칠 수 있음.
