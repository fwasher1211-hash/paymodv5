package com.you.donutpay;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * When someone pays you, appends a clickable [2x $amount] to the chat line.
 * Clicking it runs: /pay <name> <double the amount>
 */
public class ChatListener {

    // runs on text where every odd character/icon/special space was turned into one plain space
    private static final Pattern PAID_YOU = Pattern.compile(
            "([A-Za-z0-9_.]+) (?:paid|tipped|gave) you ([\\d,]*\\.?\\d+) ?([kmbt])?(?![A-Za-z])",
            Pattern.CASE_INSENSITIVE
    );

    public static void register() {
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            if (overlay) return message; // action bar can't be clicked

            String clean = message.getString().replaceAll("[^A-Za-z0-9_.,]+", " ");
            Matcher m = PAID_YOU.matcher(clean);

            if (!m.find()) {
                // says "paid you" but we couldn't read it -> show a red marker so we know
                if (clean.toLowerCase(Locale.ROOT).contains(" paid you ")) {
                    return message.copy().append(Text.literal(" [2x?]").formatted(Formatting.RED));
                }
                return message;
            }

            String name = m.group(1);
            double amount = parseAmount(m.group(2), m.group(3));
            if (amount <= 0) return message;

            long pay = Math.round(amount * 2);
            String cmd = "/pay " + name + " " + pay;

            Text button = Text.literal(" [2x $" + fmt(pay) + "]").setStyle(Style.EMPTY
                    .withColor(Formatting.GOLD)
                    .withBold(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Text.literal("Click to run " + cmd))));

            return message.copy().append(button);
        });
    }

    private static double parseAmount(String num, String suffix) {
        double v;
        try {
            v = Double.parseDouble(num.replace(",", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
        if (suffix != null) {
            switch (Character.toLowerCase(suffix.charAt(0))) {
                case 'k' -> v *= 1_000d;
                case 'm' -> v *= 1_000_000d;
                case 'b' -> v *= 1_000_000_000d;
                case 't' -> v *= 1_000_000_000_000d;
                default -> { }
            }
        }
        return v;
    }

    private static String fmt(double v) {
        if (v >= 1e12) return trim(v / 1e12) + "T";
        if (v >= 1e9) return trim(v / 1e9) + "B";
        if (v >= 1e6) return trim(v / 1e6) + "M";
        if (v >= 1e3) return trim(v / 1e3) + "K";
        return trim(v);
    }

    private static String trim(double d) {
        return String.format(Locale.ROOT, "%.2f", d).replaceAll("\\.?0+$", "");
    }
}
