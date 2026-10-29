# 독서 체크리스트 (PWA + 갤럭시 위젯)

투자·자기계발 책 12권의 읽기 순서와 진도, 연속 독서일, "내 매매 규칙에 추가할 것" 메모를 관리하는 앱입니다.
파일 하나(`index.html`)로 돌아가고, 기록은 폰 안(localStorage)에 저장됩니다.

## 구성
| 파일 | 역할 |
|---|---|
| `index.html` | 앱 전체 (오늘 / 책장 / 메모 / 설정) |
| `manifest.webmanifest` | 홈 화면 설치 정보, 아이콘 길게 누르기 바로가기(오늘·책장·메모) |
| `sw.js` | 오프라인 캐시 |
| `icons/` | 앱 아이콘 |

## 1. 폰에 앱으로 설치 (GitHub Pages)
1. GitHub에서 **public** 저장소 `reading-tracker`를 만들고 README 체크해서 생성
2. 이 폴더의 파일을 `main` 브랜치에 올리기
3. 저장소 **Settings → Pages → Branch: `main` / `(root)` → Save**
4. 1~2분 뒤 `https://jinhj423.github.io/reading-tracker/` 를 **삼성 인터넷 또는 크롬**으로 열기
5. 메뉴 → **홈 화면에 추가 / 앱 설치**

## 2. 위젯 연동 (선택)
앱의 진도를 GitHub `data` 브랜치의 `widget.txt`로 올리고, KWGT 위젯이 그 파일을 읽어 홈 화면에 보여줍니다.

1. GitHub → Settings → Developer settings → **Fine-grained tokens** → Generate
   - Repository access: **Only select repositories → reading-tracker**
   - Permissions: **Contents → Read and write** (나머지는 건드리지 않음)
2. 설치한 앱 → 설정 → 토큰 붙여넣기 → **지금 동기화**
   (처음 한 번 `data` 브랜치를 자동으로 만듭니다. 이후 기록할 때마다 3초 뒤 자동 업로드)
3. Play 스토어에서 **KWGT** 설치 → 홈 화면 길게 누르기 → 위젯 → KWGT 4x2 추가
4. 위젯 탭 → 편집기에서 **+ → Text** 추가 → 텍스트 칸에 앱 설정의 수식 붙여넣기:
   ```
   $wg("https://raw.githubusercontent.com/jinhj423/reading-tracker/data/widget.txt", raw)$
   ```
   줄바꿈이 안 보이면 마지막 `raw`를 `txt`로 바꿔보세요.
5. (권장) 위젯 **Touch → Launch App → 독서** 로 지정하면 위젯을 누를 때 앱이 열립니다.

위젯에 보이는 예:
```
🔥 5일 연속 · 오늘 ✓
📖 심리투자 42% (p.134)
📰 300문 Q121–130 (120/300)
✅ 1/12권 · 시험 모드
```

### 알아둘 점
- 위젯 내용은 **앱을 열거나 기록할 때** 갱신돼요. 앱을 안 연 날은 "마지막 10/29"처럼 마지막 기록 날짜가 보여요.
- KWGT는 웹 데이터를 주기적으로 다시 읽어서, 반영까지 수십 분 걸릴 수 있어요.
- 공개 저장소라 `widget.txt`는 누구나 볼 수 있어요. "메모 포함 전체 백업" 옵션은 기본 꺼짐이고, 켜면 `state.json`(메모 포함)도 공개돼요.
- 토큰은 폰 안에만 저장되고 JSON 백업에는 들어가지 않아요.
