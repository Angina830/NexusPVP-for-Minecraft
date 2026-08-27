package com.nexuspvp.gui;
import com.nexuspvp.util.Compat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;


import com.nexuspvp.NexusPVP;
import com.nexuspvp.config.ConfigManager;
import com.nexuspvp.gui.components.SettingComponent;
import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.modules.Radio;
import com.nexuspvp.setting.Setting;
import com.nexuspvp.util.RenderUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.*;

public class ClickGui extends Screen {

    public enum Tab {
        PVP("PvP & Combat", "Damage, TargetHUD & Hit feedback", "\u2694"),
        HUD("HUD & Screen", "ArmorHUD, Potions & Screen elements", "\u25C6"),
        PLAYER("Movement & Hands", "SmartSprint, ViewModel & Animations", "\u27A4"),
        VISUAL("Cosmetics & Effects", "Hats, Trails, Halo & Particles", "\u2728"),
        RADIO("SoundCloud Radio", "Music Player & Proximity sync", "\u266B"),
        THEMES("Themes & Accent", "Custom color schemes", "\u273F"),
        CONFIGS("Config Profiles", "Save and load presets", "\u2630");

        private final String title;
        private final String desc;
        private final String icon;

        Tab(String title, String desc, String icon) {
            this.title = title;
            this.desc = desc;
            this.icon = icon;
        }

        public String getTitle() { return title; }
        public String getDesc() { return desc; }
        public String getIcon() { return icon; }
    }

    private static Tab savedTab = Tab.PVP;
    private static int savedScrollY = 0;
    private static int savedPlaylistScrollY = 0;
    private static int savedConfigScrollY = 0;
    private static final Map<String, Boolean> savedExpandedCards = new HashMap<>();

    private Tab currentTab = Tab.PVP;
    private final Map<Category, List<ModuleButton>> moduleCards = new LinkedHashMap<>();
    
    // Window dimensions
    private int windowW = 510;
    private int windowH = 320;
    private final int titleBarH = 22;
    private final int sidebarW = 145;
    
    private int scrollY = 0;
    private TextFieldWidget searchField;
    private TextFieldWidget addTrackField;
    private TextFieldWidget configNameField;
    private int playlistScrollY = 0;
    private int configScrollY = 0;
    private boolean draggingVolume = false;
    private boolean draggingWorldVolume = false;

    // Window Pop-in Open Animation
    private long openTime = 0;
    private float openScale = 0.88f;

    public ClickGui() {
        super(Text.literal("NexusPVP"));
        this.currentTab = savedTab;
        this.scrollY = savedScrollY;
        this.playlistScrollY = savedPlaylistScrollY;
        this.configScrollY = savedConfigScrollY;
        
        for (Category category : Category.values()) {
            List<Module> modules = NexusPVP.getInstance().getModuleManager().getModulesByCategory(category);
            List<ModuleButton> buttons = new ArrayList<>();
            for (Module m : modules) {
                if (m.getName().equalsIgnoreCase("ClickGuiModule") || m.getName().equalsIgnoreCase("Radio") || m.getName().equalsIgnoreCase("DebugLogger")) {
                    continue;
                }
                ModuleButton mb = new ModuleButton(m, null);
                mb.setWidth(340);
                if (savedExpandedCards.containsKey(m.getName())) {
                    mb.setExpanded(savedExpandedCards.get(m.getName()));
                }
                buttons.add(mb);
            }
            moduleCards.put(category, buttons);
        }
    }

    @Override
    protected void init() {
        super.init();
        openTime = System.currentTimeMillis();
        openScale = 0.88f;

        int windowX = (this.width - windowW) / 2;
        int windowY = (this.height - windowH) / 2;
        
        searchField = new TextFieldWidget(this.client.textRenderer, windowX + sidebarW + 170, windowY + 4, 150, 14, Text.literal("Search..."));
        searchField.setMaxLength(32);
        searchField.setDrawsBackground(true);
        this.addDrawableChild(searchField);

        String placeholder = LanguageManager.getInstance().get("Add Track...");
        addTrackField = new TextFieldWidget(this.client.textRenderer, windowX + sidebarW + 16, windowY + titleBarH + 130, 240, 16, Text.literal(placeholder));
        addTrackField.setMaxLength(256);
        addTrackField.setDrawsBackground(true);

        String cfgPlaceholder = LanguageManager.getInstance().get("Config Name...");
        configNameField = new TextFieldWidget(this.client.textRenderer, windowX + sidebarW + 16, windowY + titleBarH + 40, 160, 16, Text.literal(cfgPlaceholder));
        configNameField.setMaxLength(32);
        configNameField.setDrawsBackground(true);
    }

    private static String getModuleTags(String moduleName) {
        switch (moduleName.toLowerCase()) {
            case "damageindicator": return "урон дамаг dmg damage комбо combo числа цифры хп hp хит hit поп ап popups размер прозрачность";
            case "targethud": return "таргет цель хп здоровье полоска дота dota враг игрок аватарка голова ник target health enemy absorption поглощение";
            case "hitsounds": return "звук звуки hitsound hitsounds bell ding skeet pop колокольчик щелчок звон клик громкость тон";
            case "nohurtcam": return "тряска камера урон нохерт hurt camera shake антитряска экран фикс";
            case "crosshair": return "прицел хитмаркер маркер крестик точка crosshair hitmarker aim круг apex cod";
            case "armorhud": return "броня шлем нагрудник поножи ботинки прочность тотем тотемы стрелы колчан armor durability totem arrow";
            case "potionhud": return "зелья баффы эффекты таймер время скорость сила реген potions buffs effects duration";
            case "smartsprint": return "бег спринт ctrl контрол ходьба автобег sprint autosprint s пауза остановка";
            case "viewmodel": return "руки предмет блендер гизмо hand viewmodel customhand размер рук угол поворот позиция 3d";
            case "swinganimations": return "анимация удар 1.7 меч свинг swing sigma spin плавный";
            case "noslowfov": return "фов fov угол обзора замедление зум noslow динамический экран";
            case "lowfire": return "огонь пламя экран обзор низкий lowfire fire текстурпак";
            case "commandkeybinds": return "бинды команды чат клавиши home feed fix heal binds cmd keybinds сервер";
            case "zoom": return "зум приближение камера оптифайн zoom c c-zoom колесико";
            case "radio": return "музыка песни радио саундклауд soundcloud треки плеер radio music аудио громкость вещание broadcast делиться стрим";
            case "chinahat": return "шляпа шапка конус косметика hat chinahat";
            case "nimb": return "нимб ангел кольцо nimb halo свет";
            case "jumpcircles": return "прыжок круги jump circles расширение ноги";
            case "jumpparticles": return "частицы всплеск jump particles прыжок";
            case "trails": return "след линия шлейф trail particles хвост";
            case "hudmodule": return "фпс координаты fps coords watermark ватермарка список модулей arraylist";
            case "hitcolor": return "цвет удара подсветка вспышка покраснение hitcolor";
            case "ambience": return "время небо ночь день солнце яркость фуллбрайт ambience time sky fullbright";
            case "babymode": return "ребенок бейби малыш масштаб baby babymode размер";
            case "targeting": return "таргетинг прицеливание подсветка выделение targeting";
            case "keystrokes": return "клавиши кейстрокс wasd cps клики мышь space пробел keystrokes";
            case "totempop": return "тотем тотемы сбитие pop тотемпоп popper звук килл totempop";
            case "itemcooldowns": return "кд кулдаун таймер перл яблоко хорус щит cooldown cooldowns itemcooldowns";
            case "blockoutline": return "блок рамка обводка подсветка неоновый сетка block outline blockoutline";
            case "attackvignette": return "виньетка удар готовность перезарядка кд зеленый красный экран vignette attack cooldown";
            case "overheadhealth": return "хп здоровье над головой полоска дота dota bar overhead health targethud враги";
            case "galaxysky": return "небо космос галактика звезды сияние аврора ночь galaxysky sky stars space";
            case "shulkerpreview": return "шалкер сундук просмотр предпросмотр лут инвентарь shulker preview tooltip";
            case "motionblur": return "блюр размытие моушен плавность кино 240гц motion blur motionblur";
            case "trajectorypreview": return "траектория перл жемчуг стрела лук прицел зелье линия trajectory pearl bow potion";
            case "tnttimer": return "тнт динамит таймер взрыв взрывы секунды fuse tnt timer explosion";
            case "clearwater": return "вода чистая прозрачная видимость туман ocean clear water clearwater";
            case "chattweaks": return "чат время таймстамп копировать сообщения chat tweaks timestamps";
            default: return "";
        }
    }

    public static boolean matchesSearch(Module module, String query) {
        if (query == null || query.isEmpty()) return true;
        query = query.toLowerCase().trim();

        String name = module.getName().toLowerCase();
        String transName = LanguageManager.getInstance().get(module.getName()).toLowerCase();
        String desc = LanguageManager.getInstance().get(module.getDescription()).toLowerCase();

        if (name.contains(query) || transName.contains(query) || desc.contains(query)) {
            return true;
        }

        for (Setting<?> s : module.getSettings()) {
            String sName = s.getName().toLowerCase();
            String sTrans = LanguageManager.getInstance().get(s.getName()).toLowerCase();
            if (sName.contains(query) || sTrans.contains(query)) {
                return true;
            }
        }

        String tags = getModuleTags(module.getName());
        if (tags != null && tags.contains(query)) {
            return true;
        }

        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Compat.setContext(context);
        MatrixStack matrices = context.getMatrices();
        RenderUtils.drawRect(matrices, 0, 0, (this.width)-(0), (this.height)-(0), 0xC0111214);

        // Smooth window pop-in spring physics
        long elapsed = System.currentTimeMillis() - openTime;
        float progress = Math.min(1.0f, elapsed / 180.0f);
        float ease = (float) (1.0 - Math.pow(1.0 - progress, 3));
        openScale = 0.88f + 0.12f * ease;

        int windowX = (this.width - windowW) / 2;
        int windowY = (this.height - windowH) / 2;
        
        ThemeManager tm = ThemeManager.getInstance();
        int accent = tm.getAccentColor().getRGB();

        matrices.push();
        if (openScale < 0.999f) {
            float cx = this.width / 2.0f;
            float cy = this.height / 2.0f;
            matrices.translate(cx, cy, 0);
            matrices.scale(openScale, openScale, 1.0f);
            matrices.translate(-cx, -cy, 0);
        }

        RenderUtils.drawRoundedRect(matrices, windowX - 1, windowY - 1, windowW + 2, windowH + 2, 7, 0xFF111214);
        RenderUtils.drawRoundedRect(matrices, windowX, windowY, windowW, windowH, 6, 0xFF313338);

        RenderUtils.drawRoundedRect(matrices, windowX, windowY, windowW, titleBarH, 6, 0xFF1E1F22);
        RenderUtils.drawRect(matrices, windowX, windowY + titleBarH - 3, windowW, 3, 0xFF1E1F22);

        // App Accent Logo Pill
        RenderUtils.drawRoundedRect(matrices, windowX + 8, windowY + 6, 10, 10, 5, accent);
        Compat.drawText(matrices, "NexusPVP", windowX + 22, windowY + 7, 0xFFF2F3F5);

        if (searchField != null) {
            searchField.setX(windowX + sidebarW + 170);
            searchField.setY(windowY + 4);
            
            if (searchField.getText().isEmpty() && !searchField.isFocused()) {
                Compat.drawText(matrices, LanguageManager.getInstance().isRussian() ? "\u041F\u043E\u0438\u0441\u043A..." : "Search...", searchField.getX() + 4, searchField.getY() + 3, 0xFF72767D);
            }
        }

        int closeBtnX = windowX + windowW - 18;
        Compat.drawText(matrices, "X", closeBtnX, windowY + 6, mouseX >= closeBtnX && mouseX <= closeBtnX + 12 && mouseY >= windowY && mouseY <= windowY + titleBarH ? 0xFFED4245 : 0xFF949BA4);

        int bodyY = windowY + titleBarH;
        int mainBodyH = windowH - titleBarH;
        int sidebarBodyH = mainBodyH - 36;

        RenderUtils.drawRect(matrices, windowX, bodyY, sidebarW, sidebarBodyH, 0xFF2B2D31);
        Compat.drawText(matrices, LanguageManager.getInstance().get("CATEGORIES"), windowX + 12, bodyY + 8, 0xFF949BA4);

        int tabY = bodyY + 22;
        int tabH = 22;

        for (Tab tab : Tab.values()) {
            boolean isSelected = (currentTab == tab);
            boolean isHovered = mouseX >= windowX + 6 && mouseX <= windowX + sidebarW - 6 && mouseY >= tabY && mouseY <= tabY + tabH;

            if (isSelected) {
                RenderUtils.drawRoundedRect(matrices, windowX + 6, tabY, sidebarW - 12, tabH, 4, 0xFF404249);
                RenderUtils.drawRoundedRect(matrices, windowX + 2, tabY + 4, 3, tabH - 8, 2, accent);
            } else if (isHovered) {
                RenderUtils.drawRoundedRect(matrices, windowX + 6, tabY, sidebarW - 12, tabH, 4, 0xFF35373C);
            }

            int textColor = isSelected ? 0xFFFFFFFF : (isHovered ? 0xFFDBDEE1 : 0xFF949BA4);
            String tabName = tab.getIcon() + " " + LanguageManager.getInstance().get(tab.getTitle());
            Compat.drawText(matrices, tabName, windowX + 14, tabY + 6, textColor);

            tabY += tabH + 2;
        }

        int userBarY = windowY + windowH - 34;
        RenderUtils.drawRect(matrices, windowX, userBarY, sidebarW, 34, 0xFF232428);
        RenderUtils.drawRoundedRect(matrices, windowX, userBarY + 32, sidebarW, 2, 2, 0xFF232428);

        if (this.client.player != null) {
            Compat.drawSkinHead(matrices, this.client.player.getSkinTextures().texture(), windowX + 8, userBarY + 6, 20);
        } else {
            RenderUtils.drawRoundedRect(matrices, windowX + 8, userBarY + 6, 20, 20, 10, accent);
        }
        RenderUtils.drawRoundedRect(matrices, windowX + 22, userBarY + 20, 7, 7, 4, 0xFF23A55A);

        String playerName = this.client.player != null ? this.client.player.getName().getString() : "Player";
        if (this.client.textRenderer.getWidth(playerName) > 60) {
            playerName = playerName.substring(0, Math.min(playerName.length(), 8)) + "..";
        }
        Compat.drawText(matrices, playerName, windowX + 32, userBarY + 7, 0xFFF2F3F5);
        Compat.drawText(matrices, LanguageManager.getInstance().get("Online"), windowX + 32, userBarY + 17, 0xFF949BA4);

        int langBtnW = 26;
        int langBtnH = 16;
        int langBtnX = windowX + sidebarW - langBtnW - 6;
        int langBtnY = userBarY + 8;
        boolean langHovered = mouseX >= langBtnX && mouseX <= langBtnX + langBtnW && mouseY >= langBtnY && mouseY <= langBtnY + langBtnH;
        int langBg = langHovered ? accent : 0xFF313338;
        RenderUtils.drawRoundedRect(matrices, langBtnX, langBtnY, langBtnW, langBtnH, 4, langBg);
        String langText = LanguageManager.getInstance().isRussian() ? "RU" : "EN";
        int ltw = this.client.textRenderer.getWidth(langText);
        Compat.drawText(matrices, langText, langBtnX + (langBtnW - ltw) / 2, langBtnY + 4, 0xFFFFFFFF);

        int contentX = windowX + sidebarW;
        int contentW = windowW - sidebarW;

        RenderUtils.drawRect(matrices, contentX, bodyY, contentW, 26, 0xFF313338);
        
        String searchQuery = (searchField != null) ? searchField.getText().trim().toLowerCase() : "";
        boolean isSearching = !searchQuery.isEmpty();

        String headerTitle = isSearching ? "[?] " + (LanguageManager.getInstance().isRussian() ? "\u041F\u043E\u0438\u0441\u043A: " : "Search: ") + searchQuery : currentTab.getIcon() + " " + LanguageManager.getInstance().get(currentTab.getTitle());
        Compat.drawText(matrices, headerTitle, contentX + 12, bodyY + 8, 0xFFF2F3F5);
        RenderUtils.drawRect(matrices, contentX + 8, bodyY + 25, contentW - 16, 1, 0xFF232428);

        int contentBodyY = bodyY + 28;
        int contentBodyH = mainBodyH - 32;

        if (isSearching) {
            RenderUtils.startScissor(contentX, contentBodyY, contentW, contentBodyH);
            int cardY = contentBodyY + 4 - scrollY;
            for (List<ModuleButton> list : moduleCards.values()) {
                for (ModuleButton card : list) {
                    if (matchesSearch(card.getModule(), searchQuery)) {
                        card.setX(contentX + 10);
                        card.setY(cardY);
                        card.render(matrices, mouseX, mouseY, delta);
                        cardY += card.getHeight() + 6;
                    }
                }
            }
            RenderUtils.endScissor();
        } else if (currentTab == Tab.PVP || currentTab == Tab.HUD || currentTab == Tab.PLAYER || currentTab == Tab.VISUAL) {
            List<ModuleButton> cards = new ArrayList<>();
            if (currentTab == Tab.PVP) {
                if (moduleCards.get(Category.PVP) != null) cards.addAll(moduleCards.get(Category.PVP));
            } else if (currentTab == Tab.HUD) {
                if (moduleCards.get(Category.HUD) != null) cards.addAll(moduleCards.get(Category.HUD));
            } else if (currentTab == Tab.PLAYER) {
                if (moduleCards.get(Category.PLAYER) != null) cards.addAll(moduleCards.get(Category.PLAYER));
            } else if (currentTab == Tab.VISUAL) {
                if (moduleCards.get(Category.VISUAL) != null) cards.addAll(moduleCards.get(Category.VISUAL));
                if (moduleCards.get(Category.RENDER) != null) cards.addAll(moduleCards.get(Category.RENDER));
            }
            if (cards != null) {
                RenderUtils.startScissor(contentX, contentBodyY, contentW, contentBodyH);
                int cardY = contentBodyY + 4 - scrollY;

                if (currentTab == Tab.HUD) {
                    int btnW = contentW - 20;
                    int btnH = 20;
                    int btnX = contentX + 10;
                    int btnY = cardY;
                    boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

                    RenderUtils.drawRoundedRect(matrices, btnX, btnY, btnW, btnH, 4, btnHover ? accent : 0xFF2B2D31);
                    String btnText = LanguageManager.getInstance().isRussian() ? "\u22BE \u041D\u0430\u0441\u0442\u0440\u043E\u0438\u0442\u044C \u0440\u0430\u0441\u043F\u043E\u043B\u043E\u0436\u0435\u043D\u0438\u0435 HUD (Drag & Drop)" : "\u22BE Customize HUD Layout (Drag & Drop)";
                    int btw = this.client.textRenderer.getWidth(btnText);
                    Compat.drawText(matrices, btnText, btnX + (btnW - btw) / 2, btnY + 6, 0xFFFFFFFF);

                    cardY += btnH + 8;
                }

                for (ModuleButton card : cards) {
                    card.setX(contentX + 10);
                    card.setY(cardY);
                    card.render(matrices, mouseX, mouseY, delta);
                    cardY += card.getHeight() + 6;
                }
                RenderUtils.endScissor();
            }
        } else if (currentTab == Tab.RADIO) {
            renderSoundCloudTab(matrices, contentX, contentBodyY, contentW, contentBodyH, mouseX, mouseY, delta);
        } else if (currentTab == Tab.THEMES) {
            renderThemesTab(matrices, contentX, contentBodyY, contentW, contentBodyH, mouseX, mouseY);
        } else if (currentTab == Tab.CONFIGS) {
            renderConfigsTab(matrices, contentX, contentBodyY, contentW, contentBodyH, mouseX, mouseY, delta);
        }

        matrices.pop();
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderConfigsTab(MatrixStack matrices, int contentX, int bodyY, int contentW, int bodyH, int mouseX, int mouseY, float delta) {
        ConfigManager cm = NexusPVP.getInstance().getConfigManager();
        if (cm == null) return;

        int startY = bodyY + 6 - configScrollY;
        int accent = ThemeManager.getInstance().getAccentColor().getRGB();

        Compat.drawText(matrices, LanguageManager.getInstance().isRussian() ? "\u041F\u0420\u041E\u0424\u0418\u041B\u0418 \u0418 \u041A\u041E\u041D\u0424\u0418\u0413\u0423\u0420\u0410\u0426\u0418\u0418" : "CONFIG PROFILES", contentX + 12, startY, 0xFFFFFFFF);
        String sub = LanguageManager.getInstance().isRussian() ? "\u0412\u0441\u0435 \u043D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438 \u0441\u043E\u0445\u0440\u0430\u043D\u044F\u044E\u0442\u0441\u044F \u0430\u0432\u0442\u043E\u043C\u0430\u0442\u0438\u0447\u0435\u0441\u043A\u0438." : "All settings save automatically.";
        Compat.drawText(matrices, sub, contentX + 12, startY + 12, 0xFF949BA4);

        int createY = startY + 30;
        RenderUtils.drawRoundedRect(matrices, contentX + 10, createY, contentW - 20, 32, 4, 0xFF2B2D31);

        if (configNameField != null) {
            configNameField.setX(contentX + 16);
            configNameField.setY(createY + 8);
            
        }

        int saveBtnX = contentX + 185;
        int saveBtnY = createY + 7;
        int saveBtnW = 70;
        int saveBtnH = 18;
        boolean saveHover = mouseX >= saveBtnX && mouseX <= saveBtnX + saveBtnW && mouseY >= saveBtnY && mouseY <= saveBtnY + saveBtnH;
        RenderUtils.drawRoundedRect(matrices, saveBtnX, saveBtnY, saveBtnW, saveBtnH, 4, saveHover ? 0xFF4752C4 : accent);
        String saveText = LanguageManager.getInstance().get("Save Config");
        int stw = this.client.textRenderer.getWidth(saveText);
        Compat.drawText(matrices, saveText, saveBtnX + (saveBtnW - stw) / 2, saveBtnY + 5, 0xFFFFFFFF);

        int listY = createY + 40;
        Compat.drawText(matrices, (LanguageManager.getInstance().isRussian() ? "\u0421\u041E\u0425\u0420\u0410\u041D\u0415\u041D\u041D\u042B\u0415 \u041A\u041E\u041D\u0424\u0418\u0413\u0418" : "SAVED CONFIGS") + " (" + cm.getAvailableConfigs().size() + "):", contentX + 12, listY, 0xFF949BA4);

        int rowY = listY + 14;
        String currentCfg = cm.getCurrentConfigName();

        for (String cfg : cm.getAvailableConfigs()) {
            boolean isCurrent = cfg.equalsIgnoreCase(currentCfg);
            int rowH = 24;

            RenderUtils.drawRoundedRect(matrices, contentX + 10, rowY, contentW - 20, rowH, 4, isCurrent ? 0xFF404249 : 0xFF2B2D31);
            if (isCurrent) {
                RenderUtils.drawRoundedRect(matrices, contentX + 10, rowY, 3, rowH, 2, accent);
            }

            String nameText = (isCurrent ? "* " : "  ") + cfg + (isCurrent ? " (Active)" : "");
            Compat.drawText(matrices, nameText, contentX + 16, rowY + 7, isCurrent ? accent : 0xFFF2F3F5);

            int loadBtnX = contentX + contentW - 130;
            int loadBtnY = rowY + 4;
            int loadBtnW = 50;
            int loadBtnH = 16;
            boolean loadHover = mouseX >= loadBtnX && mouseX <= loadBtnX + loadBtnW && mouseY >= loadBtnY && mouseY <= loadBtnY + loadBtnH;
            RenderUtils.drawRoundedRect(matrices, loadBtnX, loadBtnY, loadBtnW, loadBtnH, 3, loadHover ? 0xFF23A55A : 0xFF35373C);
            String loadText = LanguageManager.getInstance().get("Load");
            int ltw = this.client.textRenderer.getWidth(loadText);
            Compat.drawText(matrices, loadText, loadBtnX + (loadBtnW - ltw) / 2, loadBtnY + 4, 0xFFFFFFFF);

            if (!cfg.equalsIgnoreCase("default")) {
                int delBtnX = contentX + contentW - 72;
                int delBtnY = rowY + 4;
                int delBtnW = 56;
                int delBtnH = 16;
                boolean delHover = mouseX >= delBtnX && mouseX <= delBtnX + delBtnW && mouseY >= delBtnY && mouseY <= delBtnY + delBtnH;
                RenderUtils.drawRoundedRect(matrices, delBtnX, delBtnY, delBtnW, delBtnH, 3, delHover ? 0xFFDA373C : 0xFF35373C);
                String delText = LanguageManager.getInstance().get("Delete");
                int dtw = this.client.textRenderer.getWidth(delText);
                Compat.drawText(matrices, delText, delBtnX + (delBtnW - dtw) / 2, delBtnY + 4, 0xFFFFFFFF);
            }

            rowY += rowH + 4;
        }
    }

    private void renderThemesTab(MatrixStack matrices, int contentX, int bodyY, int contentW, int bodyH, int mouseX, int mouseY) {
        ThemeManager tm = ThemeManager.getInstance();
        if (tm == null) return;

        int accent = tm.getAccentColor().getRGB();

        // 1. GUI Layout Styles Section
        int startY = bodyY + 6;
        Compat.drawText(matrices, LanguageManager.getInstance().isRussian() ? "СТИЛИ И РАСКЛАДКИ ИНТЕРФЕЙСА" : "GUI LAYOUT STYLES", contentX + 12, startY, 0xFFFFFFFF);
        Compat.drawText(matrices, LanguageManager.getInstance().isRussian() ? "Выберите архитектуру и дизайн меню:" : "Choose overall menu layout and architecture:", contentX + 12, startY + 11, 0xFF949BA4);

        int styleY = startY + 24;
        GuiStyle[] styles = GuiStyle.values();
        int sCardW = (contentW - 28) / 2;
        int sCardH = 34;

        for (int i = 0; i < styles.length; i++) {
            GuiStyle s = styles[i];
            int col = i % 2;
            int row = i / 2;
            int sx = contentX + 10 + col * (sCardW + 8);
            int sy = styleY + row * (sCardH + 6);

            boolean active = tm.getCurrentStyle() == s;
            boolean hovered = mouseX >= sx && mouseX <= sx + sCardW && mouseY >= sy && mouseY <= sy + sCardH;

            RenderUtils.drawRoundedRect(matrices, sx, sy, sCardW, sCardH, 4, active ? 0xFF35373C : (hovered ? 0xFF2E3035 : 0xFF232428));
            if (active) {
                RenderUtils.drawRoundedRect(matrices, sx - 1, sy - 1, sCardW + 2, sCardH + 2, 5, accent);
                RenderUtils.drawRoundedRect(matrices, sx, sy, sCardW, sCardH, 4, 0xFF35373C);
                RenderUtils.drawRoundedRect(matrices, sx + 2, sy + 4, 3, sCardH - 8, 2, accent);
            }

            Compat.drawText(matrices, s.getIcon() + " " + s.getDisplayName(), sx + (active ? 8 : 6), sy + 5, active ? 0xFFFFFFFF : 0xFFDBDEE1);
            Compat.drawText(matrices, s.getDescription(), sx + (active ? 8 : 6), sy + 18, 0xFF888F9A);
        }

        // 2. Color Palettes Section
        int colorSecY = styleY + 2 * (sCardH + 6) + 8;
        Compat.drawText(matrices, LanguageManager.getInstance().isRussian() ? "ЦВЕТОВЫЕ АКЦЕНТЫ" : "COLOR PALETTES", contentX + 12, colorSecY, 0xFFFFFFFF);

        int gridY = colorSecY + 16;
        int gridX = contentX + 10;
        int themeBtnW = (contentW - 28) / 2;
        int themeBtnH = 22;

        int idx = 0;
        for (String themeName : tm.getThemeNames()) {
            int col = idx % 2;
            int row = idx / 2;
            int tx = gridX + col * (themeBtnW + 8);
            int ty = gridY + row * (themeBtnH + 4);

            boolean isCurrent = themeName.equalsIgnoreCase(tm.getCurrentTheme());
            boolean isHovered = mouseX >= tx && mouseX <= tx + themeBtnW && mouseY >= ty && mouseY <= ty + themeBtnH;

            Color tAccent = tm.getThemeAccent(themeName);
            RenderUtils.drawRoundedRect(matrices, tx, ty, themeBtnW, themeBtnH, 4, isCurrent ? 0xFF35373C : (isHovered ? 0xFF2E3035 : 0xFF232428));
            if (isCurrent) {
                RenderUtils.drawRoundedRect(matrices, tx - 1, ty - 1, themeBtnW + 2, themeBtnH + 2, 5, tAccent.getRGB());
                RenderUtils.drawRoundedRect(matrices, tx, ty, themeBtnW, themeBtnH, 4, 0xFF35373C);
            }

            RenderUtils.drawRoundedRect(matrices, tx + 6, ty + 4, 14, 14, 7, tAccent.getRGB());

            String transTheme = LanguageManager.getInstance().get(themeName);
            Compat.drawText(matrices, transTheme, tx + 26, ty + 7, isCurrent ? 0xFFFFFFFF : 0xFFDBDEE1);

            idx++;
        }
    }

    private void renderSoundCloudTab(MatrixStack matrices, int contentX, int bodyY, int contentW, int bodyH, int mouseX, int mouseY, float delta) {
        Radio radio = NexusPVP.getInstance().getModuleManager().getModule(Radio.class);
        if (radio == null) return;

        int playerCardH = 100;
        RenderUtils.drawRoundedRect(matrices, contentX + 10, bodyY + 4, contentW - 20, playerCardH, 6, 0xFF2B2D31);
        RenderUtils.drawRoundedRect(matrices, contentX + 10, bodyY + 4, contentW - 20, 3, 2, 0xFFFF5500);

        Compat.drawText(matrices, "\u266B SOUNDCLOUD RADIO & PROXIMITY SYNC", contentX + 18, bodyY + 12, 0xFFFF7700);

        String title = radio.getCurrentTrackTitle();
        if (this.client.textRenderer.getWidth(title) > 280) {
            title = title.substring(0, Math.min(title.length(), 40)) + "...";
        }
        Compat.drawText(matrices, title, contentX + 18, bodyY + 24, 0xFFFFFFFF);

        String status = radio.getStatusText();
        Compat.drawText(matrices, status, contentX + 18, bodyY + 36, 0xFF949BA4);

        int btnW = 65;
        int btnH = 18;
        int btnX = contentX + 18;
        int btnY = bodyY + 50;
        boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        int btnBg = radio.isPlaying() ? (btnHover ? 0xFFDA373C : 0xFFED4245) : (btnHover ? 0xFF23A55A : 0xFF57F287);
        RenderUtils.drawRoundedRect(matrices, btnX, btnY, btnW, btnH, 4, btnBg);

        String btnLabel = radio.isPlaying() ? "|| " + LanguageManager.getInstance().get("Pause") : "> " + LanguageManager.getInstance().get("Play");
        int bw = this.client.textRenderer.getWidth(btnLabel);
        Compat.drawText(matrices, btnLabel, btnX + (btnW - bw) / 2, btnY + 5, 0xFFFFFFFF);

        int nextBtnX = btnX + btnW + 6;
        int nextBtnW = 55;
        boolean nextHover = mouseX >= nextBtnX && mouseX <= nextBtnX + nextBtnW && mouseY >= btnY && mouseY <= btnY + btnH;
        RenderUtils.drawRoundedRect(matrices, nextBtnX, btnY, nextBtnW, btnH, 4, nextHover ? 0xFF404249 : 0xFF35373C);
        String nextLabel = ">> " + LanguageManager.getInstance().get("Next");
        int nbw = this.client.textRenderer.getWidth(nextLabel);
        Compat.drawText(matrices, nextLabel, nextBtnX + (nextBtnW - nbw) / 2, btnY + 5, 0xFFFFFFFF);

        int volX = nextBtnX + nextBtnW + 14;
        int volY = btnY + 3;
        int volW = 90;
        Compat.drawText(matrices, "\u266A " + (int)(radio.getVolume() * 100) + "%", volX, volY - 11, 0xFFDBDEE1);
        RenderUtils.drawRoundedRect(matrices, volX, volY + 2, volW, 5, 2, 0xFF1E1F22);
        int volFill = (int) (volW * radio.getVolume());
        if (volFill > 0) {
            RenderUtils.drawRoundedRect(matrices, volX, volY + 2, volFill, 5, 2, 0xFFFF7700);
        }
        RenderUtils.drawRoundedRect(matrices, volX + Math.max(0, volFill - 3), volY, 6, 9, 3, 0xFFFFFFFF);

        int row2Y = bodyY + 74;

        int bcBtnX = contentX + 18;
        int bcBtnW = 100;
        int bcBtnH = 18;
        boolean bcHover = mouseX >= bcBtnX && mouseX <= bcBtnX + bcBtnW && mouseY >= row2Y && mouseY <= row2Y + bcBtnH;
        boolean bcOn = radio.isBroadcastEnabled();
        int bcBg = bcOn ? (bcHover ? 0xFF23A55A : 0xFF289A50) : (bcHover ? 0xFF4E5058 : 0xFF35373C);
        RenderUtils.drawRoundedRect(matrices, bcBtnX, row2Y, bcBtnW, bcBtnH, 4, bcBg);
        String bcText = LanguageManager.getInstance().isRussian() ? (bcOn ? "\u0412\u0435\u0449\u0430\u043D\u0438\u0435: \u0412\u041A\u041B" : "\u0412\u0435\u0449\u0430\u043D\u0438\u0435: \u0412\u042B\u041A\u041B") : (bcOn ? "Share: ON" : "Share: OFF");
        int bcTw = this.client.textRenderer.getWidth(bcText);
        Compat.drawText(matrices, bcText, bcBtnX + (bcBtnW - bcTw) / 2, row2Y + 5, 0xFFFFFFFF);

        int stBtnX = bcBtnX + bcBtnW + 6;
        int stBtnW = 95;
        boolean stHover = mouseX >= stBtnX && mouseX <= stBtnX + stBtnW && mouseY >= row2Y && mouseY <= row2Y + bcBtnH;
        RenderUtils.drawRoundedRect(matrices, stBtnX, row2Y, stBtnW, bcBtnH, 4, stHover ? 0xFF404249 : 0xFF35373C);
        String stName = radio.getStation();
        String stLabel = stName.equals("MyPlaylist") ? (LanguageManager.getInstance().isRussian() ? "\u041F\u043B\u0435\u0439\u043B\u0438\u0441\u0442" : "Playlist") : (stName.equals("LocalFolder") ? (LanguageManager.getInstance().isRussian() ? "\u041F\u0430\u043F\u043A\u0430" : "Folder") : (LanguageManager.getInstance().isRussian() ? "\u0421\u043B\u0443\u0448\u0430\u0442\u044C" : "Listen"));
        String stFull = LanguageManager.getInstance().isRussian() ? "\u0420\u0435\u0436\u0438\u043C: " + stLabel : "Mode: " + stLabel;
        int stTw = this.client.textRenderer.getWidth(stFull);
        Compat.drawText(matrices, stFull, stBtnX + (stBtnW - stTw) / 2, row2Y + 5, 0xFFDBDEE1);

        int wVolX = stBtnX + stBtnW + 10;
        int wVolY = row2Y + 3;
        int wVolW = 65;
        Compat.drawText(matrices, "3D: " + (int)(radio.getWorldVolume() * 100) + "%", wVolX, wVolY - 11, 0xFF949BA4);
        RenderUtils.drawRoundedRect(matrices, wVolX, wVolY + 2, wVolW, 5, 2, 0xFF1E1F22);
        int wVolFill = (int) (wVolW * radio.getWorldVolume());
        if (wVolFill > 0) {
            RenderUtils.drawRoundedRect(matrices, wVolX, wVolY + 2, wVolFill, 5, 2, 0xFF5865F2);
        }
        RenderUtils.drawRoundedRect(matrices, wVolX + Math.max(0, wVolFill - 3), wVolY, 6, 9, 3, 0xFFFFFFFF);

        // Add track row
        int addY = bodyY + playerCardH + 10;
        RenderUtils.drawRoundedRect(matrices, contentX + 10, addY, contentW - 20, 26, 4, 0xFF2B2D31);

        if (addTrackField != null) {
            addTrackField.setX(contentX + 16);
            addTrackField.setY(addY + 5);
            
        }

        int addBtnX = contentX + contentW - 85;
        int addBtnY = addY + 4;
        int addBtnW = 70;
        int addBtnH = 18;
        boolean addHover = mouseX >= addBtnX && mouseX <= addBtnX + addBtnW && mouseY >= addBtnY && mouseY <= addBtnY + addBtnH;
        RenderUtils.drawRoundedRect(matrices, addBtnX, addBtnY, addBtnW, addBtnH, 4, addHover ? 0xFFFF7700 : 0xFFFF5500);
        String addLabel = "+ " + LanguageManager.getInstance().get("Add Track");
        int alw = this.client.textRenderer.getWidth(addLabel);
        Compat.drawText(matrices, addLabel, addBtnX + (addBtnW - alw) / 2, addBtnY + 5, 0xFFFFFFFF);

        // Playlist Queue Header
        int listY = addY + 32;
        Compat.drawText(matrices, LanguageManager.getInstance().isRussian() ? "\u041E\u0427\u0415\u0420\u0415\u0414\u042C \u0422\u0420\u0415\u041A\u041E\u0412:" : "PLAYLIST QUEUE:", contentX + 12, listY, 0xFF949BA4);

        // Scissored scrollable track queue (NO MORE BLEEDING OVER PLAYER CARD!)
        int queueStartY = listY + 12;
        int queueH = (bodyY + bodyH) - queueStartY - 4;

        if (queueH > 10) {
            RenderUtils.startScissor(contentX + 8, queueStartY, contentW - 16, queueH);

            int trackRowY = queueStartY - playlistScrollY;
            List<String> playlist = radio.getPlaylist();
            int currentIdx = radio.getCurrentTrackIndex();

            for (int i = 0; i < playlist.size(); i++) {
                String track = playlist.get(i);
                boolean isCur = (i == currentIdx);
                int rowH = 18;

                if (trackRowY + rowH >= queueStartY && trackRowY <= queueStartY + queueH) {
                    RenderUtils.drawRoundedRect(matrices, contentX + 10, trackRowY, contentW - 20, rowH, 3, isCur ? 0xFF404249 : 0xFF2B2D31);
                    if (isCur) {
                        RenderUtils.drawRoundedRect(matrices, contentX + 10, trackRowY, 3, rowH, 1, 0xFFFF7700);
                    }

                    String trackName = (isCur ? "> " : (i + 1) + ". ") + track;
                    if (this.client.textRenderer.getWidth(trackName) > contentW - 60) {
                        trackName = trackName.substring(0, Math.min(trackName.length(), 38)) + "...";
                    }
                    Compat.drawText(matrices, trackName, contentX + 16, trackRowY + 5, isCur ? 0xFFFF9933 : 0xFFDBDEE1);
                }

                trackRowY += rowH + 3;
            }

            RenderUtils.endScissor();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int windowX = (this.width - windowW) / 2;
        int windowY = (this.height - windowH) / 2;

        int closeBtnX = windowX + windowW - 18;
        if (mouseX >= closeBtnX && mouseX <= closeBtnX + 12 && mouseY >= windowY && mouseY <= windowY + titleBarH) {
            this.close();
            return true;
        }

        int userBarY = windowY + windowH - 34;
        int langBtnW = 26;
        int langBtnH = 16;
        int langBtnX = windowX + sidebarW - langBtnW - 6;
        int langBtnY = userBarY + 8;
        if (mouseX >= langBtnX && mouseX <= langBtnX + langBtnW && mouseY >= langBtnY && mouseY <= langBtnY + langBtnH && button == 0) {
            LanguageManager.getInstance().toggleLanguage();
            return true;
        }

        int bodyY = windowY + titleBarH;
        int tabY = bodyY + 22;
        int tabH = 22;

        for (Tab tab : Tab.values()) {
            if (mouseX >= windowX + 6 && mouseX <= windowX + sidebarW - 6 && mouseY >= tabY && mouseY <= tabY + tabH && button == 0) {
                currentTab = tab;
                savedTab = tab;
                scrollY = 0;
                savedScrollY = 0;
                if (searchField != null) {
                    searchField.setText("");
                }
                return true;
            }
            tabY += tabH + 2;
        }

        int contentX = windowX + sidebarW;
        int contentW = windowW - sidebarW;
        int mainBodyH = windowH - titleBarH;
        int contentBodyY = bodyY + 28;
        int contentBodyH = mainBodyH - 32;

        String searchQuery = (searchField != null) ? searchField.getText().trim().toLowerCase() : "";
        boolean isSearching = !searchQuery.isEmpty();

        if (searchField != null) {
            if (mouseX >= searchField.getX() - 4 && mouseX <= searchField.getX() + searchField.getWidth() + 4 &&
                mouseY >= searchField.getY() - 4 && mouseY <= searchField.getY() + searchField.getHeight() + 4) {
                searchField.setFocused(true);
                return true;
            } else if (button == 0) {
                searchField.setFocused(false);
            }
        }

        if (isSearching) {
            for (List<ModuleButton> list : moduleCards.values()) {
                for (ModuleButton card : list) {
                    if (matchesSearch(card.getModule(), searchQuery)) {
                        if (card.mouseClicked(mouseX, mouseY, button)) {
                            return true;
                        }
                    }
                }
            }
        } else if (currentTab == Tab.PVP || currentTab == Tab.HUD || currentTab == Tab.PLAYER || currentTab == Tab.VISUAL) {
            if (currentTab == Tab.HUD) {
                int btnW = contentW - 20;
                int btnH = 20;
                int btnX = contentX + 10;
                int btnY = contentBodyY + 4 - scrollY;
                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH && button == 0) {
                    if (this.client != null) {
                        Compat.setScreen(client, new HudEditorScreen(this));
                    }
                    return true;
                }
            }

            List<ModuleButton> cards = new ArrayList<>();
            if (currentTab == Tab.PVP) {
                if (moduleCards.get(Category.PVP) != null) cards.addAll(moduleCards.get(Category.PVP));
            } else if (currentTab == Tab.HUD) {
                if (moduleCards.get(Category.HUD) != null) cards.addAll(moduleCards.get(Category.HUD));
            } else if (currentTab == Tab.PLAYER) {
                if (moduleCards.get(Category.PLAYER) != null) cards.addAll(moduleCards.get(Category.PLAYER));
            } else if (currentTab == Tab.VISUAL) {
                if (moduleCards.get(Category.VISUAL) != null) cards.addAll(moduleCards.get(Category.VISUAL));
                if (moduleCards.get(Category.RENDER) != null) cards.addAll(moduleCards.get(Category.RENDER));
            }

            if (cards != null) {
                for (ModuleButton card : cards) {
                    if (card.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }
        } else if (currentTab == Tab.RADIO) {
            Radio radio = NexusPVP.getInstance().getModuleManager().getModule(Radio.class);
            if (radio != null) {
                int btnW = 65;
                int btnH = 18;
                int btnX = contentX + 18;
                int btnY = contentBodyY + 50;
                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH && button == 0) {
                    radio.togglePlayPause();
                    return true;
                }

                int nextBtnX = btnX + btnW + 6;
                int nextBtnW = 55;
                if (mouseX >= nextBtnX && mouseX <= nextBtnX + nextBtnW && mouseY >= btnY && mouseY <= btnY + btnH && button == 0) {
                    radio.nextTrack();
                    return true;
                }

                int volX = nextBtnX + nextBtnW + 14;
                int volY = btnY + 3;
                int volW = 90;
                if (mouseX >= volX && mouseX <= volX + volW && mouseY >= volY - 5 && mouseY <= volY + 15 && button == 0) {
                    draggingVolume = true;
                    float v = (float) ((mouseX - volX) / (double) volW);
                    radio.setVolume(v);
                    return true;
                }

                int row2Y = contentBodyY + 74;
                int bcBtnX = contentX + 18;
                int bcBtnW = 100;
                int bcBtnH = 18;
                if (mouseX >= bcBtnX && mouseX <= bcBtnX + bcBtnW && mouseY >= row2Y && mouseY <= row2Y + bcBtnH && button == 0) {
                    radio.toggleBroadcast();
                    return true;
                }

                int stBtnX = bcBtnX + bcBtnW + 6;
                int stBtnW = 95;
                if (mouseX >= stBtnX && mouseX <= stBtnX + stBtnW && mouseY >= row2Y && mouseY <= row2Y + bcBtnH && button == 0) {
                    radio.cycleStation();
                    return true;
                }

                int wVolX = stBtnX + stBtnW + 10;
                int wVolY = row2Y + 3;
                int wVolW = 65;
                if (mouseX >= wVolX && mouseX <= wVolX + wVolW && mouseY >= wVolY - 5 && mouseY <= wVolY + 15 && button == 0) {
                    draggingWorldVolume = true;
                    float v = (float) ((mouseX - wVolX) / (double) wVolW);
                    radio.setWorldVolume(v);
                    return true;
                }

                int addY = contentBodyY + 100 + 10;
                int addBtnX = contentX + contentW - 85;
                int addBtnY = addY + 4;
                int addBtnW = 70;
                int addBtnH = 18;
                if (mouseX >= addBtnX && mouseX <= addBtnX + addBtnW && mouseY >= addBtnY && mouseY <= addBtnY + addBtnH && button == 0) {
                    if (addTrackField != null && !addTrackField.getText().trim().isEmpty()) {
                        radio.addTrack(addTrackField.getText().trim());
                        addTrackField.setText("");
                    }
                    return true;
                }

                int listY = addY + 32;
                int queueStartY = listY + 12;
                int queueH = (bodyY + contentBodyH) - queueStartY;
                
                if (mouseY >= queueStartY && mouseY <= queueStartY + queueH) {
                    int trackRowY = queueStartY - playlistScrollY;
                    List<String> playlist = radio.getPlaylist();
                    for (int i = 0; i < playlist.size(); i++) {
                        int rowH = 18;
                        if (mouseX >= contentX + 10 && mouseX <= contentX + contentW - 10 && mouseY >= trackRowY && mouseY <= trackRowY + rowH && button == 0) {
                            radio.playTrack(i);
                            return true;
                        }
                        trackRowY += rowH + 3;
                    }
                }

                if (addTrackField != null && addTrackField.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        } else if (currentTab == Tab.THEMES) {
            ThemeManager tm = ThemeManager.getInstance();
            if (tm != null && button == 0) {
                int startY = contentBodyY + 6;
                int styleY = startY + 24;
                GuiStyle[] styles = GuiStyle.values();
                int sCardW = (contentW - 28) / 2;
                int sCardH = 34;

                for (int i = 0; i < styles.length; i++) {
                    GuiStyle s = styles[i];
                    int col = i % 2;
                    int row = i / 2;
                    int sx = contentX + 10 + col * (sCardW + 8);
                    int sy = styleY + row * (sCardH + 6);

                    if (mouseX >= sx && mouseX <= sx + sCardW && mouseY >= sy && mouseY <= sy + sCardH) {
                        tm.setStyle(s);
                        openCurrentStyleScreen();
                        return true;
                    }
                }

                int colorSecY = styleY + 2 * (sCardH + 6) + 8;
                int gridY = colorSecY + 16;
                int gridX = contentX + 10;
                int themeBtnW = (contentW - 28) / 2;
                int themeBtnH = 22;

                int idx = 0;
                for (String themeName : tm.getThemeNames()) {
                    int col = idx % 2;
                    int row = idx / 2;
                    int tx = gridX + col * (themeBtnW + 8);
                    int ty = gridY + row * (themeBtnH + 4);

                    if (mouseX >= tx && mouseX <= tx + themeBtnW && mouseY >= ty && mouseY <= ty + themeBtnH) {
                        tm.setTheme(themeName);
                        return true;
                    }
                    idx++;
                }
            }
        } else if (currentTab == Tab.CONFIGS) {
            ConfigManager cm = NexusPVP.getInstance().getConfigManager();
            if (cm != null) {
                int startY = contentBodyY + 6 - configScrollY;
                int createY = startY + 30;

                int saveBtnX = contentX + 185;
                int saveBtnY = createY + 7;
                int saveBtnW = 70;
                int saveBtnH = 18;
                if (mouseX >= saveBtnX && mouseX <= saveBtnX + saveBtnW && mouseY >= saveBtnY && mouseY <= saveBtnY + saveBtnH && button == 0) {
                    if (configNameField != null && !configNameField.getText().trim().isEmpty()) {
                        cm.saveConfig(configNameField.getText().trim());
                        configNameField.setText("");
                    }
                    return true;
                }

                int listY = createY + 40;
                int rowY = listY + 14;
                for (String cfg : cm.getAvailableConfigs()) {
                    int rowH = 24;

                    int loadBtnX = contentX + contentW - 130;
                    int loadBtnY = rowY + 4;
                    int loadBtnW = 50;
                    int loadBtnH = 16;
                    if (mouseX >= loadBtnX && mouseX <= loadBtnX + loadBtnW && mouseY >= loadBtnY && mouseY <= loadBtnY + loadBtnH && button == 0) {
                        cm.loadConfig(cfg);
                        return true;
                    }

                    if (!cfg.equalsIgnoreCase("default")) {
                        int delBtnX = contentX + contentW - 72;
                        int delBtnY = rowY + 4;
                        int delBtnW = 56;
                        int delBtnH = 16;
                        if (mouseX >= delBtnX && mouseX <= delBtnX + delBtnW && mouseY >= delBtnY && mouseY <= delBtnY + delBtnH && button == 0) {
                            cm.deleteConfig(cfg);
                            return true;
                        }
                    }

                    rowY += rowH + 4;
                }

                if (configNameField != null && configNameField.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingVolume = false;
        draggingWorldVolume = false;
        for (List<ModuleButton> list : moduleCards.values()) {
            for (ModuleButton card : list) {
                card.mouseReleased(mouseX, mouseY, button);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (currentTab == Tab.RADIO) {
            int windowX = (this.width - windowW) / 2;
            int contentX = windowX + sidebarW;
            Radio radio = NexusPVP.getInstance().getModuleManager().getModule(Radio.class);
            if (radio != null) {
                if (draggingVolume) {
                    int volX = contentX + 18 + 65 + 6 + 55 + 14;
                    int volW = 90;
                    float v = (float) ((mouseX - volX) / (double) volW);
                    radio.setVolume(v);
                    return true;
                }
                if (draggingWorldVolume) {
                    int wVolX = contentX + 18 + 100 + 6 + 95 + 10;
                    int wVolW = 65;
                    float v = (float) ((mouseX - wVolX) / (double) wVolW);
                    radio.setWorldVolume(v);
                    return true;
                }
            }
        }

        for (List<ModuleButton> list : moduleCards.values()) {
            for (ModuleButton card : list) {
                card.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentTab == Tab.PVP || currentTab == Tab.HUD || currentTab == Tab.PLAYER || currentTab == Tab.VISUAL) {
            scrollY = Math.max(0, scrollY - (int) (verticalAmount * 20));
            savedScrollY = scrollY;
            return true;
        } else if (currentTab == Tab.RADIO) {
            playlistScrollY = Math.max(0, playlistScrollY - (int) (verticalAmount * 16));
            savedPlaylistScrollY = playlistScrollY;
            return true;
        } else if (currentTab == Tab.CONFIGS) {
            configScrollY = Math.max(0, configScrollY - (int) (verticalAmount * 16));
            savedConfigScrollY = configScrollY;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (List<ModuleButton> list : moduleCards.values()) {
            for (ModuleButton mb : list) {
                if (mb.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }

        if (searchField != null && searchField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchField.setText("");
                searchField.setFocused(false);
                return true;
            }
            return searchField.keyPressed(keyCode, scanCode, modifiers);
        }

        if (addTrackField != null && addTrackField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                Radio radio = NexusPVP.getInstance().getModuleManager().getModule(Radio.class);
                if (radio != null && !addTrackField.getText().trim().isEmpty()) {
                    radio.addTrack(addTrackField.getText().trim());
                    addTrackField.setText("");
                }
                return true;
            }
            return addTrackField.keyPressed(keyCode, scanCode, modifiers);
        }

        if (configNameField != null && configNameField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                ConfigManager cm = NexusPVP.getInstance().getConfigManager();
                if (cm != null && !configNameField.getText().trim().isEmpty()) {
                    cm.saveConfig(configNameField.getText().trim());
                    configNameField.setText("");
                }
                return true;
            }
            return configNameField.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderInGameBackground(DrawContext context) {
        // Disable 1.21 post-processing background blur shader
    }

    public void close() {
        savedTab = this.currentTab;
        savedScrollY = this.scrollY;
        savedPlaylistScrollY = this.playlistScrollY;
        savedConfigScrollY = this.configScrollY;
        for (List<ModuleButton> buttons : moduleCards.values()) {
            for (ModuleButton mb : buttons) {
                savedExpandedCards.put(mb.getModule().getName(), mb.isExpanded());
            }
        }

        if (this.client != null) {
            Compat.setScreen(client, null);
        }
        if (NexusPVP.getInstance().getConfigManager() != null) {
            NexusPVP.getInstance().getConfigManager().saveConfig();
        }
        NexusPVP.getInstance().getModuleManager().getModuleByName("ClickGui").ifPresent(m -> {
            if (m.isEnabled()) {
                m.toggle();
            }
        });
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchField != null && searchField.isFocused()) {
            return searchField.charTyped(chr, modifiers);
        }
        if (addTrackField != null && addTrackField.isFocused()) {
            return addTrackField.charTyped(chr, modifiers);
        }
        if (configNameField != null && configNameField.isFocused()) {
            return configNameField.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    public void switchTab(Tab tab) {
        this.currentTab = tab;
        savedTab = tab;
    }

    public static void openCurrentStyleScreen() {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        GuiStyle style = ThemeManager.getInstance().getCurrentStyle();
        if (style == null) style = GuiStyle.DISCORD;
        net.minecraft.client.gui.screen.Screen screen;
        switch (style) {
            case CLASSIC_WINDOWS:
                screen = new com.nexuspvp.gui.styles.ClassicGuiScreen();
                break;
            case GLASS_DASHBOARD:
                screen = new com.nexuspvp.gui.styles.GlassDashboardScreen();
                break;
            case COMPACT_LIST:
                screen = new com.nexuspvp.gui.styles.CompactListScreen();
                break;
            case DISCORD:
            default:
                screen = new ClickGui();
                break;
        }
        Compat.setScreen(mc, screen);
    }

    public boolean shouldPause() {
        return false;
    }
}
