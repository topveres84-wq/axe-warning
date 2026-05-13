package com.axewarning.mixin;

import com.axewarning.AxeWarningMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    // Kích thước hộp cảnh báo
    private static final int BOX_SIZE = 20;

    // Màu sắc — định dạng ARGB (0xAARRGGBB)
    private static final int COLOR_RED        = 0xCCFF0000; // Đỏ, hơi trong suốt
    private static final int COLOR_RED_BORDER = 0xFFFFFFFF; // Viền trắng

    /**
     * Inject vào cuối method render() của InGameHud.
     * AT = TAIL → luôn vẽ overlay lên trên tất cả HUD element khác.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;F)V",
        at = @At("TAIL")
    )
    private void onRenderHud(DrawContext context, float tickDelta, CallbackInfo ci) {

        // ── Guard: không làm gì nếu không có nguy hiểm ──────────────────
        if (!AxeWarningMod.dangerNearby) return;

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();

        // ── Tính tọa độ để căn giữa màn hình ────────────────────────────
        // Dịch lên 40px để không đè lên crosshair (tâm màn hình)
        int x1 = (screenW - BOX_SIZE) / 2;
        int y1 = (screenH - BOX_SIZE) / 2 - 40;
        int x2 = x1 + BOX_SIZE;
        int y2 = y1 + BOX_SIZE;

        // ── Vẽ viền trắng (1px xung quanh) ──────────────────────────────
        context.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, COLOR_RED_BORDER);

        // ── Vẽ hình vuông đỏ chính 20x20 ────────────────────────────────
        context.fill(x1, y1, x2, y2, COLOR_RED);

        // ── Vẽ chữ cảnh báo bên dưới hộp ────────────────────────────────
        context.drawCenteredTextWithShadow(
            MinecraftClient.getInstance().textRenderer,
            Text.literal("§c⚠ AXE NEARBY"),
            screenW / 2,
            y2 + 5,   // cách đáy hộp 5px
            0xFFFFFF
        );
    }
}
