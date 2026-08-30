package com.nexuspvp.gui;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static LanguageManager instance;
    private boolean russian = true;
    
    private final Map<String, String> translations = new HashMap<>();
    
    private LanguageManager() {
        // ==========================================
        // 1. CATEGORIES & SIDEBAR
        // ==========================================
        translations.put("VISUAL", "ВИЗУАЛ");
        translations.put("PLAYER", "ИГРОК");
        translations.put("RENDER", "РЕНДЕР");
        translations.put("GUI", "ИНТЕРФЕЙС");
        translations.put("HUD", "HUD");
        translations.put("PVP", "PVP");
        translations.put("MISC", "РАЗНОЕ");
        translations.put("CATEGORIES", "КАТЕГОРИИ");

        translations.put("PvP & Combat", "PvP и Бой");
        translations.put("HUD & Screen", "HUD и Экран");
        translations.put("Movement & Hands", "Движение и Руки");
        translations.put("Cosmetics & Effects", "Эффекты и Косметика");
        translations.put("SoundCloud Radio", "SoundCloud Радио");
        translations.put("Themes & Accent", "Цветовые темы");
        translations.put("Config Profiles", "Конфиги и Профили");
        translations.put("Damage, TargetHUD & Hit feedback", "Урон, TargetHUD и звуки ударов");
        translations.put("ArmorHUD, Potions & Screen elements", "Броня, зелья и экранные виджеты");
        translations.put("SmartSprint, ViewModel & Animations", "Умный спринт, модель рук и анимации");
        translations.put("Hats, Trails, Halo & Particles", "Шляпы, следы, нимб и частицы");
        translations.put("Music Player & Proximity sync", "Музыкальный плеер SoundCloud");
        translations.put("Custom color schemes", "Цветовые темы оформления");
        translations.put("Save and load presets", "Сохранение и загрузка настроек");

        // ==========================================
        // 2. ALL MODULE NAMES
        // ==========================================
        translations.put("CrosshairHealth", "ХП под прицелом (CrosshairHealth)");
        translations.put("StunVisuals", "Визуализатор станов (HolyWorld)");
        translations.put("TotemPop", "Счетчик тотемов (TotemPop)");
        translations.put("Keystrokes", "Отображение клавиш (Keystrokes)");
        translations.put("ItemCooldowns", "Таймеры КД (ItemCooldowns)");
        translations.put("BlockOutline", "Подсветка блоков (BlockOutline)");
        translations.put("HitSounds", "Звуки удара");
        translations.put("DamageIndicator", "Индикатор урона");
        translations.put("TargetHUD", "Инфо о цели (TargetHUD)");
        translations.put("ArmorHUD", "Отображение брони (ArmorHUD)");
        translations.put("PotionHUD", "Таймеры зелий (PotionHUD)");
        translations.put("Crosshair", "Кастомный прицел");
        translations.put("CommandKeybinds", "Бинды команд чата");
        translations.put("SmartSprint", "Умный спринт (Ctrl)");
        translations.put("LowFire", "Низкий огонь");
        translations.put("NoSlowFOV", "Фикс угла обзора (FOV)");
        translations.put("NoHurtCam", "Анти-тряска камеры");
        translations.put("Radio", "Радио SoundCloud");
        translations.put("Particles", "Частицы ударов");
        translations.put("JumpCircles", "Круги прыжка");
        translations.put("Trails", "Следы за игроком");
        translations.put("ChinaHat", "Китайская шляпа");
        translations.put("Nimb", "Нимб ангела");
        translations.put("JumpParticles", "Частицы прыжка");
        translations.put("Targeting", "Подсветка цели");
        translations.put("HitColor", "Цвет вспышки удара");
        translations.put("ViewModel", "Кастомизация рук");
        translations.put("Ambience", "Окружение и время");
        translations.put("HudModule", "Экранный интерфейс (HUD)");
        translations.put("HUD", "Экранный интерфейс (HUD)");
        translations.put("BabyMode", "Маленькая моделька");
        translations.put("SwingAnimations", "Анимации удара");
        translations.put("Zoom", "Приближение камеры (Zoom)");
        translations.put("AttackVignette", "Виньетка кулдауна удара");
        translations.put("OverheadHealth", "ХП над головой врага");
        translations.put("GalaxySky", "Галактическое звездное небо");
        translations.put("ShulkerPreview", "Просмотр шалкеров");
        translations.put("MotionBlur", "Размытие движения (MotionBlur)");
        translations.put("TrajectoryPreview", "Траектория полета перла/стрелы");
        translations.put("TNTTimer", "Таймер взрыва ТНТ");
        translations.put("ClearWater", "Прозрачная вода");
        translations.put("ChatTweaks", "Улучшенный чат");
        translations.put("ClickGuiModule", "Меню настроек");
        translations.put("ClickGui", "Меню настроек");
        translations.put("DebugLogger", "Отладчик");

        // ==========================================
        // 3. ALL MODULE DESCRIPTIONS
        // ==========================================
        translations.put("Minimalistic under-crosshair health bar with smooth Dota ghost damage, liquid ghost heal & gold absorption", "Минималистичная полоска ХП под прицелом с плавной анимацией урона Dota, отхила и поглощения");
        translations.put("Minimalistic under-crosshair health bar with Dota ghost damage, electric ghost heal & gold absorption", "Минималистичная полоска ХП под прицелом с эффектом урона Dota, отхила и поглощения");
        translations.put("HolyWorld 30x30 Square Stun Trap / Anti-Pearl Zone continuous neon 3D barrier", "Неоновый 3D барьер зоны стана 30x30 HolyWorld с авто-глушением частиц");
        translations.put("Predicts ballistic trajectory and erases path dynamically as pearl/projectile flies", "Просчет траектории полета с динамическим стиранием хвоста за летящим перлом");
        translations.put("Tracks totem pops with clean HUD alerts", "Счетчик сбитых тотемов и фикс ослепляющей анимации");
        translations.put("Displays pressed keys and CPS counter", "Виджет нажатия клавиш WASD, кнопок мыши и счетчик CPS");
        translations.put("Smooth circular cooldown timer over hotbar items", "Круговой индикатор и точные секунды отката на хотбаре");
        translations.put("Custom glowing block selection outline", "Неоновая 3D подсветка грани блока под прицелом");
        translations.put("Stunning neon bloom block selection outline", "Неоновая 3D подсветка грани блока со свечением Bloom");
        translations.put("Plays custom audio feedback on hitting entities", "Кастомные приятные звуки при попадании по игрокам");
        translations.put("Displays dealt damage on screen and world", "Всплывающие цифры нанесенного урона на экране и в мире");
        translations.put("Discord-styled target health and armor info", "Отображение здоровья, брони и скина цели в стиле Discord");
        translations.put("Displays equipped armor, durability and item counters", "Прочность надетой брони и счетчики важных предметов");
        translations.put("Displays active potion status effects and duration timers", "Отображение активных эффектов зелий и секундных таймеров");
        translations.put("Custom PvP crosshair with hitmarkers", "Кастомизация прицела с хитмаркерами попадания");
        translations.put("Custom PvP crosshair with 60+ FPS dynamic spread and target lock", "Кастомный прицел с 60+ FPS динамическим разбегом и захватом цели");
        translations.put("Binds chat and server commands to keyboard keys", "Быстрое назначение команд сервера (/feed, /fix, /home) на клавиши");
        translations.put("Sprint by pressing Ctrl with S-pause mechanic", "Умный бег на Ctrl с остановкой на S и продолжением бега");
        translations.put("Lowers screen fire height for better visibility", "Уменьшение высоты горящего пламени для идеального обзора");
        translations.put("Prevents FOV from decreasing when slowed", "Убирает неприятное сужение экрана при эффекте замедления");
        translations.put("Reduces or disables camera shake on hurt", "Уменьшение или полное отключение тряски экрана при уроне");
        translations.put("Beautiful particles on entity hit", "Красивые частицы и цифры при ударе");
        translations.put("Damage numbers and hit particles", "Всплывающие цифры урона и эффектные частицы при ударе");
        translations.put("Expanding circles at feet on jump", "Расширяющиеся неоновые круги под ногами при прыжке");
        translations.put("Expanding neon shockwaves at feet on jump & landing", "Расширяющиеся неоновые волны под ногами при прыжке и приземлении");
        translations.put("Bright trail behind player", "Яркий анимированный след за персонажем");
        translations.put("Animated cone/disc hat", "Анимированная конусная шляпа над головой");
        translations.put("Glowing halo above head", "Светящийся ангельский нимб над головой");
        translations.put("Particles burst on jump", "Красивый всплеск ярких частиц при прыжках");
        translations.put("Highlights targeted entity with visual effects", "Подсветка существа под прицелом неоновыми спецэффектами");
        translations.put("Highlights targeted entity with neon bloom visual effects", "Подсветка существа под прицелом неоновым свечением");
        translations.put("Entities flash a color when hit", "Вспышка мобов кастомным цветом при получении урона");
        translations.put("Customize first-person hand and item display", "Настройка положения, угла поворота и размера рук");
        translations.put("Change game lighting/time/sky color", "Кастомное освещение, цвет неба и время суток");
        translations.put("Customizable HUD showing enabled modules list, coordinates, FPS", "Отображение FPS, координат, водяного знака и списка модулей");
        translations.put("On-screen display elements", "Экранные элементы: FPS, координаты и список модулей");
        translations.put("Shrink player model to baby size", "Уменьшение размера модельки персонажа до ребенка");
        translations.put("Custom attack/swing animations", "Плавные кастомные анимации удара оружием (1.7, Spin, Sigma)");
        translations.put("Camera zoom on key hold", "Плавное приближение камеры при зажатии клавиши C");
        translations.put("Smooth vignette screen pulse on weapon cooldown ready", "Плавная виньетка экрана при готовности удара оружием");
        translations.put("Screen vignette transitions red to green on attack cooldown", "Виньетка экрана от красного к зеленому по готовности удара");
        translations.put("Mini-TargetHUD floating health card above entities", "Мини-карточка TargetHUD со здоровьем и уроном над головой");
        translations.put("Renders starry cosmos, nebula and aurora in sky", "Звездный космос, анимированные туманности и северное сияние");
        translations.put("Transforms the night sky with animated nebulae and custom starfield", "Анимированные туманности и кастомное звездное небо");
        translations.put("Shows 3x9 item inventory preview on hovering shulker boxes", "Предпросмотр предметов внутри шалкера при наведении курсора");
        translations.put("Cinematic high-framerate motion blur effect", "Кинематографичное размытие в движении (240 FPS)");
        translations.put("Smooth cinematic camera motion blur effect", "Кинематографичное размытие камеры при поворотах взгляда");
        translations.put("Shows precise explosion countdown timer and danger radius above primed TNT", "Таймер до взрыва ТНТ в секундах и сфера опасности");
        translations.put("Removes murky underwater fog for crystal clear vision", "Удаление мутного подводного тумана для идеального обзора");
        translations.put("Adds timestamps and infinite chat history", "Таймстампы времени и бесконечная история сообщений чата");

        // ==========================================
        // 4. ALL SETTING NAMES
        // ==========================================
        translations.put("Color", "Цвет");
        translations.put("Size", "Размер");
        translations.put("Style", "Стиль");
        translations.put("Scale", "Масштаб");
        translations.put("Width", "Толщина");
        translations.put("Speed", "Скорость");
        translations.put("Range", "Дистанция");
        translations.put("Opacity", "Прозрачность");
        translations.put("Duration", "Длительность");
        translations.put("Height", "Высота");
        translations.put("Safe", "Безопасно");
        translations.put("Danger", "Опасно");
        translations.put("ShowTime", "Показывать время");
        translations.put("DangerZone", "Зона взрыва");
        translations.put("HitBox", "Блок приземления");
        translations.put("Hitmarker", "Хитмаркер");
        translations.put("CenterDot", "Точка в центре");
        translations.put("Numbers", "Числа");
        translations.put("Overlay", "Затенение");
        translations.put("Both", "Все вместе");
        translations.put("PearlOnly", "Только перлы");
        translations.put("Fullbright", "Фуллбрайт");
        translations.put("DamageNumbers", "Числа урона");
        translations.put("Hearts", "Сердечки");
        translations.put("Animate", "Анимация");
        translations.put("Smooth", "Плавный");
        translations.put("Theme", "Тема оформления");
        translations.put("Custom", "Свой");
        translations.put("Rainbow", "Радужный режим");
        translations.put("Fill", "Заливка");
        translations.put("FillAlpha", "Яркость заливки");
        translations.put("LineWidth", "Толщина линии");
        translations.put("PosX", "Позиция X");
        translations.put("PosY", "Позиция Y");
        translations.put("Volume", "Громкость");
        translations.put("Sound", "Звук");
        translations.put("PlaySound", "Воспроизводить звук");
        translations.put("OnScreen", "На экране");
        translations.put("CleanTotem", "Убрать ослепление");
        translations.put("ShowWASD", "Клавиши WASD");
        translations.put("ShowMouse", "Кнопки мыши");
        translations.put("ShowSpace", "Пробел");
        translations.put("ShowCPS", "Счетчик CPS");
        translations.put("TranslateX", "Смещение рук X");
        translations.put("TranslateY", "Смещение рук Y");
        translations.put("TranslateZ", "Смещение рук Z");
        translations.put("RotateX", "Поворот рук X");
        translations.put("RotateY", "Поворот рук Y");
        translations.put("RotateZ", "Поворот рук Z");
        translations.put("ScaleX", "Масштаб рук X");
        translations.put("ScaleY", "Масштаб рук Y");
        translations.put("ScaleZ", "Масштаб рук Z");
        translations.put("ShowNumbers", "Показывать цифры");
        translations.put("GhostDamage", "Призрачный урон (Dota)");
        translations.put("GhostHeal", "Призрачный отхил (GhostHeal)");
        translations.put("HideBehindWalls", "Скрывать за стенами");
        translations.put("InFlightTrack", "Стирать след в полете");
        translations.put("Warning", "Предупреждение в HUD");
        translations.put("TestMode", "Тестовый режим");
        translations.put("HideParticles", "Скрыть серверные точки");
        translations.put("Pulsing", "Пульсация");
        translations.put("ChargeIndicator", "Индикатор зарядки");
        translations.put("ChargeStyle", "Стиль зарядки");
        translations.put("ChargeReadyColor", "Цвет готовности");
        translations.put("DynamicSpread", "Динамический разбег");
        translations.put("MinSpread", "Мин. разбег");
        translations.put("MaxSpread", "Макс. разбег");
        translations.put("TargetHighlight", "Подсветка цели");
        translations.put("TargetFrame", "Рамка захвата цели");
        translations.put("TargetColor", "Цвет захвата цели");
        translations.put("PlayersOnly", "Только игроки");
        translations.put("ArrayList", "Список модулей");
        translations.put("ArrayListPos", "Позиция списка");
        translations.put("Background", "Тёмный фон");
        translations.put("Bloom", "Свечение (Bloom)");
        translations.put("Border", "Рамка");
        translations.put("Brightness", "Яркость");
        translations.put("Broadcast", "Вещание в эфир");
        translations.put("CircleColor", "Цвет кругов");
        translations.put("ComboOffsetX", "Смещение комбо X");
        translations.put("ComboOffsetY", "Смещение комбо Y");
        translations.put("ComboOpacity", "Прозрачность комбо");
        translations.put("ComboPitch", "Повышение тона комбо");
        translations.put("Coords", "Координаты XYZ");
        translations.put("CritIndicator", "Индикатор крита");
        translations.put("CtrlRun", "Спринт на Ctrl");
        translations.put("CustomColor", "Кастомный цвет");
        translations.put("CustomSky", "Кастомное небо");
        translations.put("CustomTime", "Кастомное время");
        translations.put("Distance", "Дистанция");
        translations.put("DurationBar", "Полоса длительности");
        translations.put("ExactDurability", "Точная прочность");
        translations.put("FPS", "Счетчик FPS");
        translations.put("Factor", "Кратность зума");
        translations.put("FlashOnReady", "Вспышка при готовности");
        translations.put("Gap", "Зазор линий");
        translations.put("InfiniteChat", "Бесконечный чат");
        translations.put("LocalVolume", "Громкость для себя");
        translations.put("LowDurabilityAlert", "Предупреждение о поломке");
        translations.put("MaxOpacity", "Макс. непрозрачность");
        translations.put("Mode", "Режим");
        translations.put("NeonBloom", "Неоновый блум");
        translations.put("OnlyMainHand", "Только главная рука");
        translations.put("OnlyMyDamage", "Только мой урон");
        translations.put("OnlySelf", "Только для себя");
        translations.put("OnlySlowness", "Только при замедлении");
        translations.put("OnlyWeapon", "Только с оружием");
        translations.put("Orientation", "Ориентация");
        translations.put("PauseOnS", "Пауза бега на S");
        translations.put("Paused", "На паузе");
        translations.put("Pitch", "Высота тона");
        translations.put("Preview", "Предпросмотр");
        translations.put("PreviewCombo", "Тест комбо");
        translations.put("Rotate", "Вращение");
        translations.put("ScreenDisplay", "Отображение на экране");
        translations.put("ScreenNumbersOpacity", "Прозрачность чисел");
        translations.put("ScreenNumbersScale", "Размер чисел экрана");
        translations.put("ScrollStep", "Шаг колесика");
        translations.put("ShowArrows", "Количество стрел");
        translations.put("ShowCombo", "Счетчик комбо");
        translations.put("ShowDurability", "Показ прочности");
        translations.put("ShowNotification", "Уведомления");
        translations.put("ShowTotems", "Количество тотемов");
        translations.put("SkyColor", "Цвет неба");
        translations.put("StarColor", "Цвет звезд");
        translations.put("Stars", "Количество звезд");
        translations.put("StaticFOV", "Статичный FOV");
        translations.put("Station", "Станция/Канал");
        translations.put("Strength", "Интенсивность");
        translations.put("TextColor", "Цвет текста");
        translations.put("Thickness", "Толщина линий");
        translations.put("Time", "Время суток");
        translations.put("Timestamps", "Время в чате");
        translations.put("VignetteSize", "Размер виньетки");
        translations.put("Watermark", "Водяной знак (Watermark)");
        translations.put("WorldNumbers", "Числа в 3D мире");
        translations.put("WorldVolume", "Громкость в мире");
        translations.put("ThirdPersonOnly", "Только от 3-го лица");
        translations.put("MaxRadius", "Макс. радиус");
        translations.put("Count", "Количество частиц");
        translations.put("Glow", "Пульсирующее свечение");
        translations.put("Length", "Длина следа");

        // ==========================================
        // 5. ALL MODE/DROPDOWN VALUES
        // ==========================================
        translations.put("ForcefieldPrism", "Силовой куб");
        translations.put("LaserWalls", "Лазерные стены");
        translations.put("GroundSquareOnly", "Только пол");
        translations.put("SquareBox", "Объемный куб");
        translations.put("SquareOutline", "Только контур");
        translations.put("WireframeCube", "Каркас");
        translations.put("Shockwave", "Ударная волна");
        translations.put("NeonRing", "Неоновый круг");
        translations.put("GradientDisc", "Градиентный диск");
        translations.put("DoubleWave", "Двойная волна");
        translations.put("Burst", "Всплеск");
        translations.put("Spiral", "Спираль");
        translations.put("Ring", "Кольцо");
        translations.put("Line", "Линия");
        translations.put("Dots", "Точки");
        translations.put("Cone", "Конус");
        translations.put("Disc", "Диск");
        translations.put("Torus", "Тор");
        translations.put("1.7", "Стиль 1.7");
        translations.put("Anvil", "Наковальня");
        translations.put("Bell", "Колокольчик");
        translations.put("Crit", "Критический");
        translations.put("Default", "По умолчанию");
        translations.put("Ding", "Дзинь (Ding)");
        translations.put("Down", "Сверху вниз");
        translations.put("Fade", "Затухание");
        translations.put("Horizontal", "Горизонтально");
        translations.put("Left", "Слева");
        translations.put("ListenOnly", "Только слушать");
        translations.put("LocalFolder", "Локальная папка");
        translations.put("MyPlaylist", "Мой плейлист");
        translations.put("NeonBox", "Неоновый куб");
        translations.put("NeonCircle", "Неоновый круг");
        translations.put("Orb", "Сфера");
        translations.put("Pop", "Щелчок (Pop)");
        translations.put("Pulse", "Пульсация");
        translations.put("Push", "Толчок вперед");
        translations.put("Right", "Справа");
        translations.put("Sigma", "Sigma стиль");
        translations.put("Skeet", "Skeet (Gamesense)");
        translations.put("Solid", "Сплошной");
        translations.put("Spin", "Вращение (360°)");
        translations.put("Vertical", "Вертикально");
        translations.put("Bar", "Полоска");
        translations.put("Chevron", "Шеврон");
        translations.put("Triangle", "Треугольник");
        translations.put("Circle", "Круг");
        translations.put("Box", "Рамка");
        translations.put("Diamond", "Ромб");
        translations.put("Cross", "Крестик");
        translations.put("Dot", "Точка");
        translations.put("None", "Нет");

        // ==========================================
        // 6. THEMES & UI LABELS
        // ==========================================
        translations.put("Discord", "Discord (Фирменная)");
        translations.put("Purple", "Фиолетовый Неон");
        translations.put("Blue", "Лазурный Океан");
        translations.put("Red", "Кровавый Рубин");
        translations.put("Green", "Изумрудный Лес");
        translations.put("Pink", "Сакура Розовая");
        translations.put("Orange", "Закатный Оранж");
        translations.put("Cyan", "Киберпанк Циан");

        translations.put("Online", "В сети");
        translations.put("Search...", "Поиск модулей...");
        translations.put("Add Track...", "Ссылка на трек SoundCloud...");
        translations.put("Config Name...", "Имя профиля...");
        translations.put("Save Config", "Сохранить профиль");
        translations.put("Load", "Загрузить");
        translations.put("Delete", "Удалить");
        translations.put("Play", "Играть");
        translations.put("Pause", "Пауза");
        translations.put("Next", "След.");
    }
    
    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }
    
    public boolean isRussian() {
        return russian;
    }
    
    public void setRussian(boolean russian) {
        this.russian = russian;
    }
    
    public void toggleLanguage() {
        this.russian = !this.russian;
    }
    
    public String get(String key) {
        if (!russian || key == null) return key;
        return translations.getOrDefault(key, key);
    }
}
