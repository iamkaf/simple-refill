package com.iamkaf.simplerefill.mixin;

import com.iamkaf.simplerefill.RefillQueue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique
    private static final ThreadLocal<ArrayDeque<ItemStack>> SIMPLEREFILL_DAMAGE_CAPTURES =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"))
    private void simplerefill$captureDamage(
            int amount,
            LivingEntity owner,
            EquipmentSlot slot,
            CallbackInfo callback) {
        ItemStack template = ItemStack.EMPTY;
        if (owner instanceof ServerPlayer player
                && (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND)
                && player.getItemBySlot(slot) == (Object) this) {
            template = ((ItemStack) (Object) this).copy();
        }
        SIMPLEREFILL_DAMAGE_CAPTURES.get().push(template);
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("RETURN"))
    private void simplerefill$queueDamage(
            int amount,
            LivingEntity owner,
            EquipmentSlot slot,
            CallbackInfo callback) {
        ArrayDeque<ItemStack> captures = SIMPLEREFILL_DAMAGE_CAPTURES.get();
        ItemStack template = captures.pop();
        if (captures.isEmpty()) {
            SIMPLEREFILL_DAMAGE_CAPTURES.remove();
        }
        if (!template.isEmpty() && owner instanceof ServerPlayer player) {
            InteractionHand hand = slot == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            RefillQueue.scheduleIfEmpty(player, hand, template);
        }
    }
}
