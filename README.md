# FSS Insights Windows (금융 뉴스 & 연구자료 검색기)

[hdlee73/FSS_insights](https://github.com/hdlee73/FSS_insights) 안드로이드 앱의 기능을 Windows 네이티브 환경(Compose Multiplatform for Desktop)으로 최적화하여 구현한 데스크톱 애플리케이션입니다.

---

## 주요 기능

- **통합 뉴스 검색**: Google 뉴스 RSS와 Naver 뉴스 오픈API를 통합 검색
- **정교한 검색식 지원**:
  - `AND`, `OR`, 따옴표(`"내부 통제"`) 문구 검색
  - `-제외단어` 검색 (예: `금감원 -연예`)
- **실시간 필터링**:
  - 24시간 / 7일 / 30일 기간별 필터
  - 30대 주요 언론(종합지, 경제지, 지상파/종편/통신사) 또는 전체 언론 범위 선택
  - 정규화된 기사 제목 기반 중복 기사 자동 제거
- **금융당국 보도자료**:
  - 금융감독원, 금융위원회, 미국 SEC 최근 보도자료 실시간 수집 및 검색
- **금융관련 연구원 자료**:
  - 자본시장연구원(KCMI), 한국금융연구원(KIF), IOSCO 최신 보고서 열람
- **주요 금융 사이트**:
  - DART 전자공시, e-금융민원센터 등 업무에 유용한 금융 포털 바로가기 관리
- **로컬 데이터 보관**:
  - 사용자가 설정한 키워드, 네이버 API 키, 북마크는 사용자 PC (`%APPDATA%\FSSInsights`)에 안전하게 저장됩니다.

---

## 빌드 및 실행 방법

### 요구 사항
- **JDK 21** 이상 (Adoptium Temurin 추천)

### 로컬 실행
```powershell
./gradlew run
```

### Windows 설치 파일(MSI / EXE) 패키징
```powershell
./gradlew packageDistributionForCurrentOS
```
빌드된 파일은 `build/compose/binaries/main/` 경로에 생성됩니다.

---

## GitHub Actions 자동 릴리스

태그(예: `v1.0.0`)를 푸시하면 GitHub Actions를 통해 Windows 패키지가 자동 빌드되어 [Releases](https://github.com/hdlee73/FSS-Insights_win/releases)에 업로드됩니다.

```powershell
git tag v1.0.0
git push origin v1.0.0
```

---

## 라이선스
MIT License
