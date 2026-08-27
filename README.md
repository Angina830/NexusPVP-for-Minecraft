# <p align="center">⚔️ NexusPVP Client ⚔️</p>
<p align="center">
  <strong>The Ultimate Next-Gen PvP & Visual Client for Minecraft Fabric</strong>
</p>

<p align="center">
  <a href="#-русская-версия"><img src="https://img.shields.io/badge/Язык-Русский-blue?style=for-the-badge&logo=google-translate" alt="Русский" /></a>
  <a href="#-english-version"><img src="https://img.shields.io/badge/Language-English-red?style=for-the-badge&logo=google-translate" alt="English" /></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.16.5%20%7C%201.20.1%20%7C%201.21.1-blue?style=for-the-badge&logo=minecraft" alt="Minecraft Versions" />
  <img src="https://img.shields.io/badge/Fabric-0.14+-black?style=for-the-badge&logo=fabric" alt="Fabric" />
  <img src="https://img.shields.io/badge/Version-0.1.33--beta-purple?style=for-the-badge" alt="Version" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License" />
  <img src="https://img.shields.io/badge/100%25-Free%20%26%20Clean-success?style=for-the-badge" alt="Free" />
</p>

<p align="center">
  <strong><a href="#-русская-версия">🇷🇺 Перейти к русской версии</a></strong> • 
  <strong><a href="#-english-version">🇬🇧 Switch to English Version</a></strong>
</p>

---

# 🇷🇺 Русская версия

## 🌟 О проекте

**NexusPVP** — это инновационный, полностью кастомизируемый визуальный PvP-клиент для Minecraft на платформе **Fabric** (`1.16.5`, `1.20.1`, `1.21.1`). Клиент создан специально для динамичных PvP-битв, анархии (**HolyWorld, FunTime, ReallyWorld**) и комфортной игры с максимальным FPS.

---

## 🎨 4 Архитектурных стиля интерфейса (GUI Layouts)

Меню `NexusPVP` переключается на лету между **4 принципиально разными дизайнами**:

| Стиль | Описание | Особенности |
|---|---|---|
| 💬 **Discord Modern** | Боковой сайдбар с иконками категорий + карточки | Полные описания, бегунки, SoundCloud плеер |
| 🪟 **Classic Windows** | Независимые перетаскиваемые окна по категориям | Drag & Drop окон, сворачивание, компактные списки |
| 💎 **Glass Dashboard** | Верхний навбар + матовая 2-колоночная сетка | Эффект Frosted Glass, iOS-переключатели, модальные настройки |
| ⚡ **Compact List** | Ультра-компактный плоский список | Молниеносное переключение в бою, выездной слайдер настроек |

---

## 🔥 Ключевые возможности

### ⚔️ PvP & Бой (Combat)
- 🎯 **TargetHUD**: Стильная карточка захваченной цели с 3D-моделью скина, здоровьем, броней и плавной шкалой HP.
- 🩸 **OverheadHealth**: Discord-карточка со здоровьем и поглощением ровно над головами мобов и игроков. Не видна сквозь стены, 0 искажений моделей.
- 💥 **DamageIndicator**: Всплывающие числа урона с параболической физикой и счетчик комбо-ударов (`HITS / DMG`).
- 🎯 **Crosshair & Hitmarker**: Кастомный прицел с динамическим разлетом от кулдауна удара и 45-градусным `X`-хитмаркером при попадании.
- 🏹 **TrajectoryPreview**: 3D-расчёт траектории и места приземления перлов (Ender Pearl), стрел, трезубцев и зелий.
- 🧨 **TNTTimer**: 3D-таймер над динамитом с индикатором опасной зоны взрыва.
- 🥊 **ViewModel & 1.7 Block-Hit**: Полная 3D-настройка рук и оружия + классическая анимация блока мечом.
- 🔔 **HitSounds с ComboPitch**: Настраиваемые звуки ударов с динамическим повышением тона при комбо.

### 🎒 Инвентарь, Экран и Удобство (Render & QoL)
- 📦 **ShulkerPreview (3x9)**: Мгновенный предпросмотр содержимого шалкеров при наведении.
- 🛡️ **ArmorHUD & LowDurabilityAlert**: Точная прочность брони (`480/528`) и предупреждение при критическом износе.
- 🧪 **PotionHUD с DurationBar**: Таймеры и цветные тающие полосы активных эффектов.
- 🌌 **GalaxySky**: Космическое звездное небо с градиентами.
- 🌊 **ClearWater**: Полное устранение мутного подводного тумана.
- 📜 **ChatTweaks**: Временные метки `[HH:mm:ss]` перед сообщениями в чате.
- 🌊 **MotionBlur**: Кинематографичное размытие в движении.
- 🎵 **SoundCloud Radio**: Встроенный музыкальный плеер прямо в меню игры.

---

## 📥 Установка

1. Скачайте и установите **[Fabric Loader](https://fabricmc.net/)** для вашей версии игры (`1.16.5`, `1.20.1` или `1.21.1`).
2. Установите **[Fabric API](https://modrinth.com/mod/fabric-api)**.
3. Скачайте `.jar` файл нужной версии из раздела **[Releases](../../releases)**:
   - `NexusPVP-1.16.5-v0.1.33-beta.jar`
   - `NexusPVP-1.20.1-v0.1.33-beta.jar`
   - `NexusPVP-1.21.1-v0.1.33-beta.jar`
4. Поместите `.jar` файл в папку `.minecraft/mods`.
5. Запустите игру и нажмите **`Right Shift`** для открытия меню!

---

## ⌨️ Управление

* **`Right Shift`** — Открыть / закрыть меню настроек (`NexusPVP GUI`).
* **`ЛКМ`** — Включить / выключить модуль.
* **`ПКМ`** — Раскрыть подробные настройки модуля.
* **`СКМ` (Колёсико)** или кнопка **`[BIND]`** — Назначить горячую клавишу на модуль.

---
---

# 🇬🇧 English Version

## 🌟 About NexusPVP

**NexusPVP** is an advanced, fully customizable visual PvP client for Minecraft built on the **Fabric** platform supporting (`1.16.5`, `1.20.1`, `1.21.1`). Engineered specifically for competitive PvP, Anarchy servers, and crystal-clear high FPS performance.

---

## 🎨 4 Distinct GUI Layouts

Switch seamlessly between **4 uniquely designed GUI layouts** in real time:

| Layout | Description | Highlights |
|---|---|---|
| 💬 **Discord Modern** | Sidebar navigation with category icons + module cards | Full descriptions, smooth sliders, SoundCloud player |
| 🪟 **Classic Windows** | Draggable floating windows for each category | Window drag-and-drop, collapsible headers, compact cards |
| 💎 **Glass Dashboard** | Top navigation bar + frosted 2-column grid | Frosted glass look, iOS toggle switches, clean modal settings |
| ⚡ **Compact List** | Ultra-minimalist flat list | Fast mid-fight toggling, slide-out setting panels |

---

## 🔥 Features Overview

### ⚔️ PvP & Combat
- 🎯 **TargetHUD**: Modern target card featuring the opponent's 3D skin model, health, armor durability, and smooth health bar animations.
- 🩸 **OverheadHealth**: Discord-styled floating health card positioned cleanly above players and entities. Line-of-sight checked (never visible through walls).
- 💥 **DamageIndicator**: Floating damage popups with parabolic bounce physics and active combo hit counter (`HITS / DMG`).
- 🎯 **Crosshair & Hitmarker**: Fully custom crosshair with dynamic attack spread and crisp 45° diagonal `X` hitmarkers.
- 🏹 **TrajectoryPreview**: Real-time 3D physics trajectory & landing prediction for Ender Pearls, arrows, tridents, and potions.
- 🧨 **TNTTimer**: 3D countdown timer above primed TNT with blast radius danger indicator.
- 🥊 **ViewModel & 1.7 Block-Hit**: Complete 3D hand/item translation, rotation, scaling, and classic 1.7 sword block animation.
- 🔔 **HitSounds with ComboPitch**: Customizable hit sound effects with dynamic pitch shifting on successive combo hits.

### 🎒 Inventory, HUD & QoL
- 📦 **ShulkerPreview (3x9)**: Instant hover preview of shulker box contents without opening chests.
- 🛡️ **ArmorHUD & LowDurabilityAlert**: Accurate armor durability counters (`480/528`) and flashing alerts when gear is near breaking.
- 🧪 **PotionHUD with DurationBar**: Active potion effect timers with colored draining progress bars.
- 🌌 **GalaxySky**: Custom deep-space starry sky renderer with smooth gradients.
- 🌊 **ClearWater**: Completely eliminates murky underwater fog for crystal-clear vision.
- 📜 **ChatTweaks**: Prepend accurate timestamps `[HH:mm:ss]` to in-game chat messages.
- 🌊 **MotionBlur**: Cinematic camera motion blur shader.
- 🎵 **SoundCloud Radio**: Integrated audio streamer built right into the client interface.

---

## 📥 Installation

1. Download and install **[Fabric Loader](https://fabricmc.net/)** for your Minecraft version (`1.16.5`, `1.20.1`, or `1.21.1`).
2. Install **[Fabric API](https://modrinth.com/mod/fabric-api)**.
3. Download the matching `.jar` from **[Releases](../../releases)**:
   - `NexusPVP-1.16.5-v0.1.33-beta.jar`
   - `NexusPVP-1.20.1-v0.1.33-beta.jar`
   - `NexusPVP-1.21.1-v0.1.33-beta.jar`
4. Place the `.jar` into your `.minecraft/mods` directory.
5. Launch Minecraft and press **`Right Shift`** to open the menu!

---

## ⌨️ Controls & Keybinds

* **`Right Shift`** — Open / Close the NexusPVP ClickGUI.
* **`Left Click`** — Toggle module on / off.
* **`Right Click`** — Expand detailed module settings.
* **`Middle Click` (Scroll wheel)** or **`[BIND]` button** — Assign a custom keybind.

---

## 📜 License
Distributed under the permissive **MIT License**. Contributions, suggestions, and pull requests are welcome! ⭐
