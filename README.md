# Ripening Plugin

## 📖 소개

Ripening Plugin은 마인크래프트 서버에 발효 시스템을 추가하여 플레이어가 다양한 재료를 조합하여 새로운 아이템을 만들 수 있도록 합니다. ItemsAdder 플러그인과 연동하여 커스텀 아이템을 발효 재료 및 결과물로 사용할 수 있으며, 바닐라 아이템도 지원합니다.

## ✨ 주요 기능

*   **커스텀 발효 레시피:** `recipes.yml` 파일을 통해 자신만의 발효 레시피를 정의할 수 있습니다.
*   **직관적인 GUI:** 발효 항아리 GUI를 통해 재료를 넣고 발효 과정을 시작하며, 완료된 아이템을 수령할 수 있습니다.
*   **ItemsAdder 연동:** ItemsAdder의 커스텀 아이템을 발효 재료 및 결과물로 완벽하게 지원합니다.
*   **바닐라 아이템 지원:** 마인크래프트 기본 아이템도 레시피에 사용할 수 있습니다.
*   **발효 진행 상황 표시:** 발효 중인 항아리를 우클릭하면 남은 시간을 확인할 수 있습니다.
*   **자동 아이템 수령:** 발효가 완료된 항아리를 우클릭하면 결과물이 자동으로 인벤토리에 지급됩니다.
*   **사운드 효과:** 발효 시작 및 완료 시 사운드 효과가 재생됩니다.
*   **관리자 명령어:** 설정 파일을 실시간으로 리로드할 수 있는 관리자 명령어를 제공합니다.
*   **탭 자동 완성:** 관리자 명령어에 대한 탭 자동 완성을 지원합니다.

## 📥 설치 방법

1.  **ItemsAdder 설치:** 이 플러그인은 ItemsAdder 플러그인에 의존합니다. 먼저 [ItemsAdder](https://www.spigotmc.org/resources/itemsadder.73355/)를 다운로드하여 서버의 `plugins` 폴더에 넣어주세요.
2.  **Ripening Plugin 설치:** [Ripening Plugin](https://github.com/your-repo/ripening_plugin/releases) (릴리즈 페이지 링크, 추후 추가) 최신 버전을 다운로드하여 서버의 `plugins` 폴더에 넣어주세요.
3.  **서버 재시작 또는 리로드:** 서버를 재시작하거나 `/reload` 명령어를 사용하여 플러그인을 로드합니다.

## ⚙️ 설정

플러그인 설치 후 `plugins/ripening_plugin` 폴더에 다음 설정 파일들이 생성됩니다.

*   `config.yml`: 플러그인의 전반적인 설정 (GUI 제목, 사운드 등)
*   `lang.yml`: 플러그인 메시지 설정 (다국어 지원)
*   `recipes.yml`: 발효 레시피 정의

### `config.yml` 예시

```yaml
# 이 값이 true이면, 모든 GUI 아이템의 재료가 STRUCTURE_VOID로 대체됩니다.
# 투명한 STRUCTURE_VOID 텍스처를 가진 커스텀 리소스 팩을 사용하는 경우에 유용합니다.
# 이를 통해 GUI 디자인의 자유도를 높일 수 있습니다.
items-all-structure-void: false

# GUI의 제목을 커스터마이징합니다.
gui-titles:
  jar_gui: "항아리"

sounds:
  fermentation_start: "BLOCK_BREWING_STAND_BREW" # 발효 시작 시 재생될 사운드
  fermentation_claim: "ENTITY_ITEM_PICKUP" # 발효 완료 후 아이템 수령 시 재생될 사운드
```

### `lang.yml` 예시

```yaml
start_button: "§a발효 시작"
claim_button: "§e아이템 수령"
fermentation_started: "§a발효가 시작되었습니다!"
fermentation_complete: "§a발효가 완료되었습니다! 아이템을 수령했습니다."
fermentation_in_progress: "§b발효 중... 남은 시간: {time_left}"
invalid_recipe: "§c올바른 재료 조합이 아닙니다."
no_permission: "§c이 명령어를 사용할 권한이 없습니다."
config_reloaded: "§a설정 파일이 리로드되었습니다."
```

### `recipes.yml` 예시

```yaml
recipes:
  gochujang:
    items:
      - "itemsadder:red_pepper_powder"
      - "itemsadder:fermented_soybean_block"
      - "itemsadder:salt"
    time: 3600 # 초 단위 (1시간)
    results:
      one_star:
        item: "itemsadder:gochujang_1star"
        chance: 50.0
      two_star:
        item: "itemsadder:gochujang_2star"
        chance: 35.0
      three_star:
        item: "itemsadder:gochujang_3star"
        chance: 14.0
      failure:
        item: "itemsadder:rotten_food"
        chance: 1.0

  golden_apple_recipe:
    items:
      - "minecraft:apple"
      - "minecraft:apple"
      - "minecraft:apple"
    time: 60 # 1분
    results:
      success:
        item: "minecraft:golden_apple"
        chance: 100.0
```

*   `items`: 레시피에 필요한 아이템 목록입니다. ItemsAdder 아이템은 `itemsadder:아이템ID` 형식으로, 바닐라 아이템은 `minecraft:아이템ID` 형식으로 입력합니다.
*   `time`: 발효에 걸리는 시간(초)입니다.
*   `results`: 발효 완료 후 얻을 수 있는 결과물 목록입니다. `chance`는 해당 아이템을 얻을 확률(%)입니다.

## 🎮 사용법

1.  **발효 항아리 배치:** ItemsAdder를 통해 발효 항아리 아이템을 얻어 원하는 위치에 배치합니다.
2.  **GUI 열기:** 배치된 발효 항아리를 우클릭하여 GUI를 엽니다.
3.  **재료 넣기:** GUI의 재료 슬롯(중앙 3x3 그리드)에 레시피에 맞는 아이템을 넣습니다.
4.  **발효 시작:** 재료를 모두 넣은 후 "발효 시작" 버튼을 클릭합니다.
5.  **발효 진행 확인:** 발효 중인 항아리를 우클릭하면 남은 시간을 확인할 수 있습니다.
6.  **아이템 수령:** 발효가 완료되면 항아리를 다시 우클릭하여 결과물을 인벤토리로 수령합니다.

## 💻 명령어

| 명령어             | 설명                               | 권한             |
| :----------------- | :--------------------------------- | :--------------- |
| `/ripening reload` | 플러그인의 설정 파일을 리로드합니다. | `ripening.admin` |

## 🔑 권한

*   `ripening.admin`: `/ripening` 관리자 명령어를 사용할 수 있는 권한입니다.

## ❓ 지원 및 문제 보고

문제가 발생하거나 도움이 필요하면 [GitHub Issues](https://github.com/your-repo/ripening_plugin/issues) (추후 추가)에 보고해 주세요.