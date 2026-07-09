package com.iamkaf.simplerefill.mixin;

import com.iamkaf.simplerefill.RefillQueue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private static final ThreadLocal<ArrayDeque<InteractionHand>> SIMPLEREFILL_HAND_CAPTURES =
            ThreadLocal.withInitial(ArrayDeque::new);
    @Unique
    private static final ThreadLocal<ArrayDeque<ItemStack>> SIMPLEREFILL_ITEM_CAPTURES =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void simplerefill$captureCompletedUse(CallbackInfo callback) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack template = ItemStack.EMPTY;
        if ((Object) this instanceof ServerPlayer player && player.isUsingItem()) {
            hand = player.getUsedItemHand();
            template = player.getItemInHand(hand).copy();
        }
        SIMPLEREFILL_HAND_CAPTURES.get().push(hand);
        SIMPLEREFILL_ITEM_CAPTURES.get().push(template);
    }

    @Inject(method = "completeUsingItem", at = @At("RETURN"))
    private void simplerefill$queueCompletedUse(CallbackInfo callback) {
        InteractionHand hand = simplerefill$pop(SIMPLEREFILL_HAND_CAPTURES);
        ItemStack template = simplerefill$pop(SIMPLEREFILL_ITEM_CAPTURES);
        if ((Object) this instanceof ServerPlayer player && !template.isEmpty()) {
            RefillQueue.scheduleIfEmpty(player, hand, template);
        }
    }

    @Unique
    private static <T> T simplerefill$pop(ThreadLocal<ArrayDeque<T>> captures) {
        ArrayDeque<T> stack = captures.get();
        T capture = stack.pop();
        if (stack.isEmpty()) {
            captures.remove();
        }
        return capture;
    }
}
