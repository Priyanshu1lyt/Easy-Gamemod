package com.notheartyt.easygamemode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EasyGamemodeScreen extends Screen {

    private static final int PANEL_WIDTH = 390;
    private static final int ROW_HEIGHT = 31;

    private static final String[] MODES = {
            "survival", "creative", "adventure", "spectator"
    };

    private static final String[] LABELS = {"S", "C", "A", "SP"};

    private static final int[] COLORS = {
            0xFF55AA55, 0xFF55AAFF, 0xFFFFAA33, 0xFFAA66DD
    };

    private EditBox searchBox;
    private int scrollOffset;
    private int left;
    private int top;

    public EasyGamemodeScreen() {
        super(Component.literal("Easy Gamemode"));
    }

    @Override
    protected void init() {
        left = (width - PANEL_WIDTH) / 2;
        top = Math.max(15, (height - 350) / 2);

        searchBox = new EditBox(
                font,
                left + 12,
                top + 43,
                PANEL_WIDTH - 24,
                22,
                Component.literal("Search players")
        );

        searchBox.setHint(Component.literal("Search online players..."));
        addRenderableWidget(searchBox);
    }

    private List<PlayerInfo> getPlayers() {
        Minecraft client = Minecraft.getInstance();

        if (client.getConnection() == null) {
            return new ArrayList<>();
        }

        String query = searchBox == null
                ? ""
                : searchBox.getValue().toLowerCase(Locale.ROOT);

        return client.getConnection().getOnlinePlayers()
                .stream()
                .filter(info -> info.getProfile().name()
                        .toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private String getCurrentMode(PlayerInfo info) {
        GameType type = info.getGameMode();

        if (type == GameType.SURVIVAL) return "survival";
        if (type == GameType.CREATIVE) return "creative";
        if (type == GameType.ADVENTURE) return "adventure";
        if (type == GameType.SPECTATOR) return "spectator";

        return "";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);

        int right = left + PANEL_WIDTH;

        graphics.fill(left, top, right, height - 15, 0xF021212B);
        graphics.fill(left, top, right, top + 34, 0xFF303044);

        graphics.drawCenteredString(
                font, "EASY GAMEMODE", width / 2, top + 11, 0xFFFFFFFF
        );

        graphics.drawString(
                font, "ONLINE PLAYERS", left + 12, top + 34, 0xFFBBBBBB
        );

        super.render(graphics, mouseX, mouseY, delta);

        int everyoneY = top + 76;

        graphics.fill(
                left + 8, everyoneY, right - 8, everyoneY + 26, 0xFF41415D
        );

        graphics.drawString(
                font, "EVERYONE", left + 16, everyoneY + 9, 0xFFFFD45C
        );

        renderModeButtons(graphics, mouseX, mouseY, everyoneY, null);

        List<PlayerInfo> players = getPlayers();

        int listY = top + 111;
        int availableHeight = height - listY - 35;
        int visibleRows = Math.max(1, availableHeight / ROW_HEIGHT);

        int end = Math.min(players.size(), scrollOffset + visibleRows);

        for (int i = scrollOffset; i < end; i++) {
            PlayerInfo info = players.get(i);
            int rowY = listY + (i - scrollOffset) * ROW_HEIGHT;

            boolean hovered = mouseX >= left + 8 && mouseX <= right - 8
                    && mouseY >= rowY && mouseY <= rowY + 27;

            graphics.fill(
                    left + 8, rowY, right - 8, rowY + 27,
                    hovered ? 0xFF39394C : 0xFF292936
            );

            graphics.fill(
                    left + 13, rowY + 4, left + 31, rowY + 22, 0xFF555577
            );

            String name = info.getProfile().name();

            graphics.drawString(
                    font, name, left + 38, rowY + 9, 0xFFFFFFFF
            );

            renderModeButtons(
                    graphics, mouseX, mouseY, rowY, getCurrentMode(info)
            );
        }

        if (players.isEmpty()) {
            graphics.drawCenteredString(
                    font, "No online players found",
                    width / 2, listY + 12, 0xFFAAAAAA
            );
        }

        graphics.drawString(
                font, "Players: " + players.size(),
                left + 12, height - 29, 0xFFAAAAAA
        );

        graphics.drawString(
                font, "G: Close", right - 65, height - 29, 0xFFAAAAAA
        );
    }

    private void renderModeButtons(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            int rowY,
            String activeMode
    ) {
        int buttonStart = left + 139;

        for (int i = 0; i < MODES.length; i++) {
            int x = buttonStart + i * 57;

            boolean hovered = mouseX >= x && mouseX <= x + 51
                    && mouseY >= rowY + 2 && mouseY <= rowY + 23;

            boolean active = MODES[i].equals(activeMode);

            int background = active
                    ? COLORS[i]
                    : hovered ? 0xFF66667C : 0xFF454555;

            graphics.fill(x, rowY + 2, x + 51, rowY + 23, background);

            graphics.drawCenteredString(
                    font, LABELS[i], x + 25, rowY + 9, 0xFFFFFFFF
            );

            if (hovered) {
                graphics.renderTooltip(
                        font,
                        Component.literal(MODES[i]),
                        mouseX,
                        mouseY
                );
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button != 0) return false;

        int everyoneY = top + 76;

        if (mouseY >= everyoneY + 2 && mouseY <= everyoneY + 23) {
            int modeIndex = getClickedMode(mouseX);

            if (modeIndex != -1) {
                EasyGamemodeClient.executeGamemode(MODES[modeIndex], "@a");
                return true;
            }
        }

        List<PlayerInfo> players = getPlayers();

        int listY = top + 111;
        int visibleRows = Math.max(1, (height - listY - 35) / ROW_HEIGHT);
        int end = Math.min(players.size(), scrollOffset + visibleRows);

        for (int i = scrollOffset; i < end; i++) {
            int rowY = listY + (i - scrollOffset) * ROW_HEIGHT;

            if (mouseY >= rowY && mouseY <= rowY + 27) {
                int modeIndex = getClickedMode(mouseX);

                if (modeIndex != -1) {
                    String playerName = players.get(i).getProfile().name();

                    EasyGamemodeClient.executeGamemode(
                            MODES[modeIndex], playerName
                    );

                    return true;
                }
            }
        }

        return false;
    }

    private int getClickedMode(double mouseX) {
        int buttonStart = left + 139;

        for (int i = 0; i < MODES.length; i++) {
            int x = buttonStart + i * 57;

            if (mouseX >= x && mouseX <= x + 51) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (verticalAmount < 0) {
            scrollOffset++;
        } else if (verticalAmount > 0) {
            scrollOffset--;
        }

        int visibleRows = Math.max(1, (height - (top + 111) - 35) / ROW_HEIGHT);
        int max = Math.max(0, getPlayers().size() - visibleRows);

        scrollOffset = Math.max(0, Math.min(scrollOffset, max));

        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
                                             }
