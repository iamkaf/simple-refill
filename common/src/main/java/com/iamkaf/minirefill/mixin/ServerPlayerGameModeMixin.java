package com.iamkaf.minirefill.mixin;

import com.iamkaf.minirefill.RefillQueue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Unique
    private static final ThreadLocal<ArrayDeque<ItemStack>> MINIREFILL_USE_CAPTURES =
            ThreadLocal.withInitial(ArrayDeque::new);
    @Unique
    private static final ThreadLocal<ArrayDeque<ItemStack>> MINIREFILL_USE_ON_CAPTURES =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "useItem", at = @At("HEAD"))
    private void minirefill$captureUse(
            ServerPlayer player,
            Level level,
            ItemStack stack,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> callback) {
        MINIREFILL_USE_CAPTURES.get().push(stack.copy());
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    private void minirefill$queueUse(
            ServerPlayer player,
            Level level,
            ItemStack stack,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> callback) {
        RefillQueue.scheduleIfEmpty(player, hand, minirefill$pop(MINIREFILL_USE_CAPTURES));
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void minirefill$captureUseOn(
            ServerPlayer player,
            Level level,
            ItemStack stack,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> callback) {
        MINIREFILL_USE_ON_CAPTURES.get().push(stack.copy());
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void minirefill$queueUseOn(
            ServerPlayer player,
            Level level,
            ItemStack stack,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> callback) {
        ItemStack template = minirefill$pop(MINIREFILL_USE_ON_CAPTURES);
        if (callback.getReturnValue().consumesAction()) {
            RefillQueue.scheduleIfEmpty(player, hand, template);
        }
    }

    @Unique
    private static ItemStack minirefill$pop(ThreadLocal<ArrayDeque<ItemStack>> captures) {
        ArrayDeque<ItemStack> stack = captures.get();
        ItemStack capture = stack.pop();
        if (stack.isEmpty()) {
            captures.remove();
        }
        return capture;
    }
}
