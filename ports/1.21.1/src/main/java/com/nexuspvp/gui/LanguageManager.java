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
        translations.put("AttackVignette", "Виньетка готовности удара");
        translations.put("OverheadHealth", "Полоска здоровья над головой");
        translations.put("GalaxySky", "Галактическое небо");
        translations.put("ShulkerPreview", "Просмотр шалкеров");
        translations.put("MotionBlur", "Размытие движения (MotionBlur)");
        translations.put("TrajectoryPreview", "Траектория броска");
        translations.put("TNTTimer", "Таймер ТНТ");
        translations.put("ClearWater", "Прозрачная вода");
        translations.put("ChatTweaks", "Твики чата");
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
        translations.put("Highlights targeted entity with visual effects", "Подсветка существа под прицелом спецэффектами");
        translations.put("Entities flash a color when hit", "Вспышка мобов цветом при уроне");
        translations.put("Customize first-person hand and item display", "Настройка положения, угла и размера рук");
        translations.put("Change game lighting/time/sky color", "Кастомное освещение, цвет неба и время суток");
        translations.put("Customizable HUD showing enabled modules list, coordinates, FPS", "Отображение FPS, координат и списка модулей");
        translations.put("Shrink player model to baby size", "Уменьшение размера модельки персонажа");
        translations.put("Custom attack/swing animations", "Плавные кастомные анимации удара оружием");
        translations.put("Camera zoom on key hold", "Приближение камеры на клавишу C");
        translations.put("Smooth vignette screen pulse on weapon cooldown ready", "Плавная виньетка экрана при готовности удара");
        translations.put("TargetHUD-style Dota 2 health bar floating above entities", "Полоска здоровья с анимацией урона над мобами");
        translations.put("Renders starry cosmos, nebula and aurora in sky", "Звездный космос, туманности и северное сияние");
        translations.put("Shows 3x9 item inventory preview on hovering shulker boxes", "Предпросмотр предметов внутри шалкера при наведении");
        translations.put("Cinematic high-framerate motion blur effect", "Кинематографичное размытие в движении (240 FPS)");
        translations.put("Draws flight trajectory for pearls, bows, and potions", "Линия полета перлов, стрел из лука и зелий");
        translations.put("Shows precise countdown and danger zone over primed TNT", "Таймер взрыва динамита в секундах и зона опасности");
        translations.put("Makes water crystal clear and removes underwater fog", "Кристально чистая вода без тумана");
        translations.put("Adds timestamps and message copying to chat", "Время сообщений и быстрое копирование в чате");

        // Settings Translations
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
        translations.put("Circle", "Круг");
        translations.put("Box", "Рамка");
        translations.put("Diamond", "Ромб");
        translations.put("Cross", "Крестик");
        translations.put("Dot", "Точка");
        translations.put("None", "Нет");
        translations.put("Theme", "Тема");
        translations.put("Custom", "Свой");
        translations.put("Rainbow", "Радуга");
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
        translations.put("TranslateX", "Смещение X");
        translations.put("TranslateY", "Смещение Y");
        translations.put("TranslateZ", "Смещение Z");
        translations.put("RotateX", "Поворот X");
        translations.put("RotateY", "Поворот Y");
        translations.put("RotateZ", "Поворот Z");
        translations.put("ScaleX", "Масштаб X");
        translations.put("ScaleY", "Масштаб Y");
        translations.put("ScaleZ", "Масштаб Z");

        // Theme Names
        translations.put("Discord", "Discord (Фирменная)");
        translations.put("Purple", "Фиолетовая Неоновая");
        translations.put("Blue", "Лазурный Океан");
        translations.put("Red", "Кровавый Рубин");
        translations.put("Green", "Изумрудный Лес");
        translations.put("Pink", "Сакура Розовая");
        translations.put("Orange", "Закатный Оранж");
        translations.put("Cyan", "Киберпанк Циан");

        // Common UI
        translations.put("Search...", "Поиск модулей...");
        translations.put("Add Track...", "Ссылка на трек SoundCloud...");
        translations.put("Config Name...", "Имя профиля...");
        translations.put("Save Config", "Сохранить");
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
