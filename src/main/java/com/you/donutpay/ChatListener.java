package com.you.donutpay;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listens to chat and detects messages where a player paid you.
 * DonutSMP prints amounts with suffixes, e.g. "Steve paid you $2.5m".
 *
 * Handled patterns:
 *   PlayerName paid you $1,000
 *   PlayerName paid you $2.5m
 *   You received $1,000 from PlayerName
 */
public class ChatListener {

    // number with optional commas/decimal, optional k/m/b/t suffix
    private static final String AMOUNT = "\\$?\\s*([\\d,]*\\.?\\d+)\\s*([kmbt])?\\b";

    private static final Pattern PAID_YOU = Pattern.compile(
            "^(?:\\[.*?]\\s*)?([A-Za-z0-9_.]+)\\s+(?:paid|tipped|gave)\\s+you\\s+" + AMOUNT,
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern RECEIVED_FROM = Pattern.compile(
            "(?:received|got)\\s+" + AMOUNT + "\\s+from\\s+([A-Za-z0-9_.]+)",
            Pattern.CASE_INSENSITIVE
    );

    public static void register() {
        net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.GAME.register(
                (message, overlay) -> {
                    // DonutSMP also shows payments in the action bar (overlay) -
                    // skip those so each payment is only recorded once.
                    if (!overlay) handleMessage(message.getString());
                }
        );
    }

    private static void handleMessage(String text) {
        if (text == null || text.isEmpty()) return;
        String t = text.trim();

        Matcher m = PAID_YOU.matcher(t);
        if (m.find()) {
            double amount = parseAmount(m.group(2), m.group(3));
            if (amount > 0) record(m.group(1), amount);
            return;
        }

        m = RECEIVED_FROM.matcher(t);
        if (m.find()) {
            double amount = parseAmount(m.group(1), m.group(2));
            if (amount > 0) record(m.group(3), amount);
        }
    }

    private static void record(String name, double amount) {
        DonutPayClient.STORE.addPayment(new PaymentStore.Payment(
                UUID.randomUUID(), name, "tip", System.currentTimeMillis(), amount
        ));
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
}
