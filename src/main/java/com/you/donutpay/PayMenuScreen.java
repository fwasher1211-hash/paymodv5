package com.you.donutpay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Overlay menu listing everyone who paid you recently with a "Pay 2x" button.
 * Press the menu key (default H) in game to open it.
 */
public class PayMenuScreen extends Screen {

    private static final int ROW_H = 22;
    private static final int BTN_W = 110;

    public PayMenuScreen() {
        super(Text.literal("Recent payers"));
    }

    @Override
    protected void init() {
        List<PaymentStore.Payment> payments = visiblePayments();
        int y = 40;

        for (PaymentStore.Payment p : payments) {
            final String name = p.playerName();
            final double amount = p.amount();
            final double double_ = amount * 2;

            addDrawableChild(ButtonWidget.builder(
                            Text.literal("2x $" + fmt(double_)).formatted(Formatting.GOLD),
                            b -> {
                                DonutPayClient.sendPay(MinecraftClient.getInstance(), name, double_);
                                close();
                            })
                    .dimensions(20, y, BTN_W, 20)
                    .build());
            y += ROW_H;
        }

        if (payments.isEmpty()) {
            return;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), b -> {
            DonutPayClient.STORE.clear();
            close();
        }).dimensions(width - BTN_W - 20, y + 4, BTN_W, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
        List<PaymentStore.Payment> payments = visiblePayments();
        int y = 40;
        for (PaymentStore.Payment p : payments) {
            String line = p.playerName() + "  paid you  $" + fmt(p.amount())
                    + "   (" + timeAgo(p.timestamp()) + ")";
            ctx.drawTextWithShadow(textRenderer, Text.literal(line).formatted(Formatting.WHITE),
                    20 + BTN_W + 10, y + 6, 0xFFFFFF);
            y += ROW_H;
        }
        if (payments.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("No payments detected yet. Someone needs to /pay you first!")
                            .formatted(Formatting.GRAY),
                    width / 2, 60, 0xFFFFFF);
        }
    }

    /** Only as many rows as fit on screen (newest first). */
    private List<PaymentStore.Payment> visiblePayments() {
        List<PaymentStore.Payment> all = DonutPayClient.STORE.getPayments();
        int maxRows = Math.max(1, (height - 80) / ROW_H);
        return all.size() > maxRows ? all.subList(0, maxRows) : all;
    }

    private static String fmt(double d) {
        return String.format("%,.0f", d);
    }

    private static String timeAgo(long ts) {
        long s = (System.currentTimeMillis() - ts) / 1000;
        if (s < 60) return s + "s ago";
        if (s < 3600) return (s / 60) + "m ago";
        return (s / 3600) + "h ago";
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
