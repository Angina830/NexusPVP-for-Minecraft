package com.nexuspvp.gui;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static LanguageManager instance;
    private boolean russian = true;
    
    private final Map<String, String> translations = new HashMap<>();
    
    private LanguageManager() {
        // Categories
        translations.put("VISUAL", "ВИЗУАЛ");
        translations.put("PLAYER", "ИГРОК");
        translations.put("RENDER", "РЕНДЕР");
        translations.put("GUI", "ИНТЕРФЕЙС");
        translations.put("CATEGORIES", "КАТЕГОРИИ");

        // Tabs
        translations.put("PvP & Combat", "PvP и Бой");
        translations.put("HUD & Screen", "HUD и Экран");
        translations.put("Movement & Hands", "Движение и Руки");
        translations.put("Cosmetics & Effects", "Эффекты и Косметика");
        translations.put("SoundCloud Radio", "SoundCloud");
        translations.put("Themes & Accent", "Цветовые темы");
        translations.put("Config Profiles", "Конфиги");
        translations.put("Damage, TargetHUD & Hit feedback", "Урон, TargetHUD и звуки удара");
        translations.put("ArmorHUD, Potions & Screen elements", "Броня, зелья и экранные элементы");
        translations.put("SmartSprint, ViewModel & Animations", "Умный спринт, модель рук и анимации");
        translations.put("Hats, Trails, Halo & Particles", "Шляпы, следы, нимб и частицы");
        translations.put("Music Player & Proximity sync", "Музыкальный плеер SoundCloud");
        translations.put("Custom color schemes", "Цветовые темы оформления");
        translations.put("Save and load presets", "Сохранение и загрузка настроек");

        // Module Names
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
        translations.put("NoSlowFOV", "Фикс угла обзора");
        translations.put("NoHurtCam", "Анти-тряска камеры");
        translations.put("Radio", "Радио");
        translations.put("Particles", "Частицы");
        translations.put("JumpCircles", "Круги прыжка");
        translations.put("Trails", "Следы");
        translations.put("ChinaHat", "Китайская шляпа");
        translations.put("Nimb", "Нимб");
        translations.put("JumpParticles", "Частицы прыжка");
        translations.put("Targeting", "Прицеливание");
        translations.put("HitColor", "Цвет удара");
        translations.put("ViewModel", "Модель рук");
        translations.put("Ambience", "Окружение");
        translations.put("HudModule", "Интерфейс (HUD)");
        translations.put("BabyMode", "Режим ребенка");
        translations.put("SwingAnimations", "Анимации удара");
        translations.put("Zoom", "Приближение");
        translations.put("ClickGuiModule", "Меню");
        translations.put("DebugLogger", "Отладчик");

        // Module Descriptions
        translations.put("Tracks totem pops with clean HUD alerts", "Счетчик сбитых тотемов и фикс ослепления");
        translations.put("Displays pressed keys and CPS counter", "Виджет нажатия клавиш WASD, мыши и счетчик CPS");
        translations.put("Smooth circular cooldown timer over hotbar items", "Круговой индикатор и секунды отката на хотбаре");
        translations.put("Custom glowing block selection outline", "Неоновая 3D подсветка грани блока под прицелом");
        translations.put("Plays custom audio feedback on hitting entities", "Кастомные звуки при попадании по врагу");
        translations.put("Displays dealt damage on screen and world", "Отображение нанесенного урона на экране и 3D");
        translations.put("Discord-styled target health and armor info", "Отображение здоровья, брони и ника цели");
        translations.put("Displays equipped armor, durability and item counters", "Прочность надетой брони и счетчики предметов");
        translations.put("Displays active potion status effects and duration timers", "Отображение активных эффектов зелий и таймеров");
        translations.put("Custom PvP crosshair with hitmarkers", "Кастомизация прицела с хитмаркерами попадания");
        translations.put("Binds chat and server commands to keyboard keys", "Назначение команд сервера (/feed, /fix, /home) на клавиши");
        translations.put("Sprint by pressing Ctrl with S-pause mechanic", "Бег на Ctrl с остановкой на S и продолжением бега");
        translations.put("Lowers screen fire height for better visibility", "Уменьшение высоты пламени на экране для обзора");
        translations.put("Prevents FOV from decreasing when slowed", "Убирает сужение экрана при эффекте замедления");
        translations.put("Reduces or disables camera shake on hurt", "Уменьшение или отключение тряски экрана при уроне");
        translations.put("Beautiful particles on entity hit", "Красивые частицы и цифры при ударе");
        translations.put("Expanding circles at feet on jump", "Расширяющиеся круги под ногами при прыжке");
        translations.put("Bright trail behind player", "Яркий анимированный след за игроком");
        translations.put("Animated cone/disc hat", "Конусная шляпа над головой");
        translations.put("Glowing halo above head", "Светящийся нимб ангела над головой");
        translations.put("Particles burst on jump", "Всплеск ярких частиц при прыжке");
        translations.put("Highlights targeted entity", "Подсветка существа под прицелом");
        translations.put("Entities flash a color when hit", "Вспышка мобов цветом при уроне");
        translations.put("Customize first-person hand and item display", "Настройка положения, угла и размера рук");
        translations.put("Change game lighting/time/sky color", "Кастомное освещение, цвет неба и время суток");
        translations.put("Customizable HUD", "Отображение FPS, координат и списка модулей");
        translations.put("Shrink player model to baby size", "Уменьшение размера модельки персонажа");
        translations.put("Custom attack/swing animations", "Плавные кастомные анимации удара оружием");
        translations.put("Camera zoom on key hold", "Плавный зум камеры при удержании клавиши C");
        translations.put("Plays music using MQTT", "Музыкальный плеер и синхронное радио");
        translations.put("Logs debug info", "Вывод технической информации в лог");
        translations.put("Module that opens the ClickGui screen", "Открытие главного окна меню клиента");

        // Settings Names
        translations.put("OnScreen", "Показ на экране");
        translations.put("CleanTotem", "Анти-ослепление (CleanTotem)");
        translations.put("PlaySound", "Звук сбития");
        translations.put("Sound", "Звуковой эффект");
        translations.put("Volume", "Громкость");
        translations.put("PosX", "Позиция X");
        translations.put("PosY", "Позиция Y");
        translations.put("ShowWASD", "Клавиши WASD");
        translations.put("ShowMouse", "Кнопки мыши (LMB/RMB)");
        translations.put("ShowSpace", "Клавиша Пробел");
        translations.put("ShowCPS", "Счетчик CPS");
        translations.put("Circular", "Круговой прогресс");
        translations.put("Seconds", "Секунды отката");
        translations.put("CustomColor", "Свой цвет");
        translations.put("LineWidth", "Толщина линий");
        translations.put("Fill", "Заливка грани");
        translations.put("FillAlpha", "Прозрачность заливки");
        translations.put("Opacity", "Прозрачность");
        translations.put("Alpha", "Прозрачность");
        translations.put("Duration", "Длительность (сек)");
        translations.put("Mode", "Режим");
        translations.put("Pitch", "Высота тона");
        translations.put("OnlySlowness", "Только замедление");
        translations.put("StaticFOV", "Статичный FOV");
        translations.put("OnlyMyDamage", "Только мой урон");
        translations.put("ScreenDisplay", "Показ на экране");
        translations.put("ShowCombo", "Показывать комбо");
        translations.put("ComboOpacity", "Прозрачность комбо");
        translations.put("ComboOffsetX", "Смещение X (По горизонтали)");
        translations.put("ComboOffsetY", "Смещение Y (По вертикали)");
        translations.put("DamageNumbers", "Цифры урона");
        translations.put("Hearts", "Сердечки");
        translations.put("Color", "Цвет");
        translations.put("Size", "Размер");
        translations.put("Style", "Стиль");
        translations.put("MaxRadius", "Макс. радиус");
        translations.put("Speed", "Скорость");
        translations.put("Length", "Длина");
        translations.put("Width", "Ширина");
        translations.put("Rainbow", "Радуга");
        translations.put("Radius", "Радиус");
        translations.put("Height", "Высота");
        translations.put("Rotate", "Вращение");
        translations.put("ThirdPersonOnly", "Только от 3-го лица");
        translations.put("Thickness", "Толщина");
        translations.put("Glow", "Свечение");
        translations.put("Count", "Количество");
        translations.put("Range", "Дистанция");
        translations.put("Animate", "Анимация");
        translations.put("TranslateX", "Смещение X");
        translations.put("TranslateY", "Смещение Y");
        translations.put("TranslateZ", "Смещение Z");
        translations.put("RotateX", "Вращение X");
        translations.put("RotateY", "Вращение Y");
        translations.put("RotateZ", "Вращение Z");
        translations.put("ScaleX", "Размер X");
        translations.put("ScaleY", "Размер Y");
        translations.put("ScaleZ", "Размер Z");
        translations.put("OnlyMainHand", "Только правая рука");
        translations.put("Time", "Время");
        translations.put("CustomTime", "Свое время");
        translations.put("SkyColor", "Цвет неба");
        translations.put("CustomSky", "Свое небо");
        translations.put("Brightness", "Яркость");
        translations.put("Fullbright", "Макс. яркость");
        translations.put("ArrayList", "Список модулей");
        translations.put("Coords", "Координаты");
        translations.put("FPS", "Счетчик FPS");
        translations.put("Watermark", "Водяной знак");
        translations.put("ArrayListPosition", "Сторона списка");
        translations.put("Background", "Фон под текстом");
        translations.put("Scale", "Масштаб");
        translations.put("OnlySelf", "Только для себя");
        translations.put("Factor", "Сила зума");
        translations.put("Smooth", "Плавность");
        translations.put("ScrollStep", "Шаг колесика");
        translations.put("CtrlRun", "Бег только на Ctrl");
        translations.put("PauseOnS", "Остановка на S");
        translations.put("OnlyHitmarker", "Только хитмаркер");
        translations.put("Strength", "Сила тряски");
        translations.put("ShowDurability", "Показывать прочность");
        translations.put("ShowTotems", "Счетчик тотемов");
        translations.put("ShowArrows", "Счетчик стрел");
        translations.put("Orientation", "Ориентация");
        translations.put("Hitmarker", "Хитмаркер при ударе");
        translations.put("ShowNotification", "Уведомления");
        translations.put("ScreenNumbersOpacity", "Прозрачность чисел урона");
        translations.put("ScreenNumbersScale", "Размер чисел урона");
        translations.put("CritIndicator", "Значок крита");
        translations.put("Bloom", "Блум (Свечение)");
        
        // AttackVignette and OverheadHealth
        translations.put("AttackVignette", "Виньетка готовности удара (Vignette)");
        translations.put("Screen vignette transitions red to green on attack cooldown", "Плавная виньетка экрана от красного к зеленому по готовности удара");
        translations.put("MaxOpacity", "Макс. прозрачность");
        translations.put("OnlyWeapon", "Только с оружием");
        translations.put("FlashOnReady", "Вспышка при 100%");
        translations.put("VignetteSize", "Размер виньетки");

        translations.put("OverheadHealth", "ХП полоска над головой (Overhead)");
        translations.put("TargetHUD-style Dota 2 health bar floating above entities", "Полоска здоровья над головой врагов в стиле TargetHUD и Dota 2");
        translations.put("PlayersOnly", "Только игроки");
        translations.put("Absorption", "Поглощение (Золотые сердца)");
        translations.put("DotaHealth", "Dota 2 анимация урона (Белый след)");
        translations.put("Range", "Дистанция отображения");
        translations.put("YOffset", "Смещение по высоте");

        
        // GalaxySky, ShulkerPreview, MotionBlur
        translations.put("GalaxySky", "Космическое небо (GalaxySky)");
        translations.put("Custom cosmic galaxy sky and aurora borealis at night", "Кастомное звездное небо, галактика и северное сияние ночью");
        translations.put("Stars", "Количество звезд");
        translations.put("Twinkle", "Мерцание звезд");
        translations.put("Aurora", "Северное сияние (Aurora)");
        translations.put("Galaxy", "Галактика (Galaxy)");

        translations.put("ShulkerPreview", "Предпросмотр шалкеров (ShulkerPreview)");
        translations.put("Shows a 3x9 grid preview of items inside shulker boxes", "Отображение сетки 3x9 предметов при наведении на шалкер");
        translations.put("ShowEmpty", "Пустые слоты");

        translations.put("MotionBlur", "Плавность камеры (MotionBlur)");
        translations.put("Smooth cinematic camera motion blur effect", "Кинематографичный блюр при поворотах камеры");
        translations.put("Strength", "Сила размытия");

        
        // TrajectoryPreview, TNTTimer, ClearWater, ChatTweaks
        translations.put("TrajectoryPreview", "Траектория броска (Trajectory)");
        translations.put("Predicts and renders ballistic trajectory for pearls, bows and potions", "3D линия траектории полета перлов, стрел, трезубцев и зелий");
        translations.put("HitBox", "Маркер приземления");

        translations.put("TNTTimer", "Таймер динамита (TNTTimer)");
        translations.put("Shows remaining fuse seconds and blast radius over primed TNT", "Отображение секунд до взрыва и радиуса поражения над TNT");
        translations.put("ShowRadius", "Круг радиуса взрыва");

        translations.put("ClearWater", "Чистая вода (ClearWater)");
        translations.put("Removes murky underwater fog for crystal clear vision", "Убирает мутный подводный туман для кристальной видимости");

        translations.put("ChatTweaks", "Умный чат (ChatTweaks)");
        translations.put("Adds timestamps and enhancements to chat messages", "Добавление точного времени отправки к сообщениям в чате");
        translations.put("Timestamps", "Время сообщений [ЧЧ:ММ:СС]");

        
        // Upgrades translations
        translations.put("ComboPitch", "Динамический тон комбо");
        translations.put("ExactDurability", "Точные числа прочности");
        translations.put("LowDurabilityAlert", "Предупреждение о поломке (<10%)");
        translations.put("DurationBar", "Полоска оставшегося времени");

        translations.put("Save Config", "Сохранить конфиг");
        translations.put("Load", "Загрузить");
        translations.put("Delete", "Удалить");

        // Modes and Options
        translations.put("Ding", "Дзинь (Ding)");
        translations.put("Bell", "Колокольчик (Bell)");
        translations.put("Orb", "Сфера опыта (Orb)");
        translations.put("Anvil", "Наковальня (Anvil)");
        translations.put("Crit", "Крит (Crit)");
        translations.put("Horizontal", "Горизонтально");
        translations.put("Vertical", "Вертикально");
        translations.put("Theme", "По цвету темы");
        translations.put("Custom", "Пользовательский");
        translations.put("Neon", "Неоновый");
        translations.put("Solid", "Сплошной");
        translations.put("Fade", "Затухание");
        translations.put("Pulse", "Пульсация");
        translations.put("Default", "Стандарт");
        translations.put("Circle", "Круг");
        translations.put("Filled", "Заливка");
        translations.put("Gradient", "Градиент");
        translations.put("Line", "Линия");
        translations.put("Dots", "Точки");
        translations.put("Burst", "Всплеск");
        translations.put("Spiral", "Спираль");
        translations.put("Ring", "Кольцо");
        translations.put("Box", "Бокс");
        translations.put("Diamond", "Ромб");
        translations.put("Right", "Справа");
        translations.put("Left", "Слева");
        translations.put("Spin", "Вращение");
        translations.put("Sigma", "Сигма");
        translations.put("Push", "Толчок");
        translations.put("Down", "Вниз");
        translations.put("Station", "Станция");
        translations.put("MyPlaylist", "Мой плейлист");
        translations.put("LocalFolder", "Локальная папка");
        translations.put("ListenOnly", "Только слушать");
        translations.put("LocalVolume", "Громкость");
        translations.put("WorldVolume", "3D Громкость");
        translations.put("Paused", "Пауза");
        translations.put("Broadcast", "Вещать другим");
        translations.put("Solo", "Соло");
        translations.put("Play", "Старт");
        translations.put("Pause", "Пауза");
        translations.put("Next", "След.");
        translations.put("Add Track", "Добавить трек");
        translations.put("Online", "В сети");

        // Themes
        translations.put("Discord", "Discord");
        translations.put("Purple", "Фиолетовая (Purple)");
        translations.put("Blue", "Лазурная (Blue)");
        translations.put("Red", "Красная (Red)");
        translations.put("Green", "Зеленая (Green)");
        translations.put("Pink", "Розовая (Pink)");
        translations.put("Orange", "Оранжевая (Orange)");
        translations.put("Cyan", "Бирюзовая (Cyan)");
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
    
    public void toggle() {
        this.russian = !this.russian;
    }

    public void toggleLanguage() {
        toggle();
    }
    
    public String get(String key) {
        if (!russian || key == null) return key;
        return translations.getOrDefault(key, key);
    }
}
