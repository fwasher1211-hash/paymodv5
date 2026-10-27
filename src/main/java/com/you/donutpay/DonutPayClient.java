package com.you.donutpay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class DonutPayClient implements ClientModInitializer {

    public static final PaymentStore STORE = new PaymentStore();
    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open 2x pay menu", // change to real translation if wanted
                GLFW.GLFW_KEY_H,
                "DonutPay 2x"
        ));
        openMenuKey.setPressed(false);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                client.setScreen(new PayMenuScreen());
            }
        });

        ChatListener.register();
    }

    public static void sendPay(MinecraftClient client, String name, double amount) {
        if (client.player == null) return;
        long whole = Math.round(amount);
        client.player.networkHandler.sendChatCommand(
                "pay " + name + " " + whole
        );
    }
}
