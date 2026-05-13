package com.axewarning;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class AxeWarningMod implements ClientModInitializer {

    public static final String MOD_ID = "axewarning";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ── Biến shared: tick counter để giảm tần suất scan ──────────────────
    // Thay vì check mỗi frame render (60fps), chỉ check mỗi 10 game ticks (~0.5s)
    // → Tiết kiệm CPU đáng kể cho Oppo A5 2020
    public static boolean dangerNearby = false;
    private static int tickCounter = 0;
    private static final int SCAN_INTERVAL = 10; // ticks

    @Override
    public void onInitializeClient() {
        LOGGER.info("[AxeWarning] Mod loaded — scanning every {} ticks", SCAN_INTERVAL);

        // Đăng ký ClientTickEvent để scan định kỳ (nhẹ hơn nhiều so với render)
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
            .END_CLIENT_TICK.register(client -> {

            // Bỏ qua nếu chưa vào thế giới hoặc player chưa load
            if (client.world == null || client.player == null) {
                dangerNearby = false;
                return;
            }

            tickCounter++;
            if (tickCounter < SCAN_INTERVAL) return;
            tickCounter = 0;

            // ── Logic scan player trong bán kính 10 block ────────────────
            dangerNearby = isAxeNearby(client);
        });
    }

    /**
     * Kiểm tra có EntityPlayer nào trong 10 block đang cầm Axe không.
     * Dùng Box query thay vì loop toàn bộ entity → O(n) với n rất nhỏ.
     */
    private boolean isAxeNearby(net.minecraft.client.MinecraftClient client) {
        net.minecraft.util.math.Box scanBox = client.player.getBoundingBox().expand(10.0);

        java.util.List<net.minecraft.entity.player.PlayerEntity> nearbyPlayers =
            client.world.getEntitiesByClass(
                net.minecraft.entity.player.PlayerEntity.class,
                scanBox,
                p -> p != client.player && p.isAlive()
            );

        for (net.minecraft.entity.player.PlayerEntity player : nearbyPlayers) {
            net.minecraft.item.ItemStack mainHand =
                player.getEquippedStack(net.minecraft.entity.EquipmentSlot.MAINHAND);
            net.minecraft.item.ItemStack offHand =
                player.getEquippedStack(net.minecraft.entity.EquipmentSlot.OFFHAND);

            if (isAxe(mainHand) || isAxe(offHand)) {
                return true;
            }
        }
        return false;
    }

    /** Kiểm tra item có phải Axe không — dùng instanceof thay tag lookup để nhẹ hơn */
    private boolean isAxe(net.minecraft.item.ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.AxeItem;
    }
}
