package com.notheartyt.easygamemode;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;

public class EasyGamemodeClient implements ClientModInitializer {

    public static final String MOD_ID = "easygamemode";

    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {

        openKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.easygamemode.open",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_G,
                        KeyMapping.Category.MISC
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (openKey.consumeClick()) {

                if (client.screen instanceof EasyGamemodeScreen) {
                    client.setScreen(null);
                } else if (client.player != null) {
                    client.setScreen(new EasyGamemodeScreen());
                }
            }
        });
    }

    public static void executeGamemode(String mode, String target) {

        Minecraft client = Minecraft.getInstance();

        if (client.getConnection() == null || client.player == null) {
            return;
        }

        String command = "gamemode " + mode + " " + target;

        client.getConnection().sendCommand(command);

        client.player.displayClientMessage(
                Component.literal("Sent gamemode command: " + mode),
                false
        );
    }
}
