package com.iamkaf.minirefill.mixin;

import com.iamkaf.minirefill.RefillQueue;
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
    private static final ThreadLocal<ArrayDeque<InteractionHand>> MINIREFILL_HAND_CAPTURES =
            ThreadLocal.withInitial(ArrayDeque::new);
    @Unique
    private static final ThreadLocal<ArrayDeque<ItemStack>> MINIREFILL_ITEM_CAPTURES =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void minirefill$captureCompletedUse(CallbackInfo callback) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack template = ItemStack.EMPTY;
        if ((Object) this instanceof ServerPlayer player && player.isUsingItem()) {
            hand = player.getUsedItemHand();
            template = player.getItemInHand(hand).copy();
        }
        MINIREFILL_HAND_CAPTURES.get().push(hand);
        MINIREFILL_ITEM_CAPTURES.get().push(template);
    }

    @Inject(method = "completeUsingItem", at = @At("RETURN"))
    private void minirefill$queueCompletedUse(CallbackInfo callback) {
        InteractionHand hand = minirefill$pop(MINIREFILL_HAND_CAPTURES);
        ItemStack template = minirefill$pop(MINIREFILL_ITEM_CAPTURES);
        if ((Object) this instanceof ServerPlayer player && !template.isEmpty()) {
            RefillQueue.scheduleIfEmpty(player, hand, template);
        }
    }

    @Unique
    private static <T> T minirefill$pop(ThreadLocal<ArrayDeque<T>> captures) {
        ArrayDeque<T> stack = captures.get();
        T capture = stack.pop();
        if (stack.isEmpty()) {
            captures.remove();
        }
        return capture;
    }
}
