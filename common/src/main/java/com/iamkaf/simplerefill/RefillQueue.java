package com.iamkaf.simplerefill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RefillQueue {
    private static final Map<UUID, EnumMap<InteractionHand, PendingRefill>> PENDING = new HashMap<>();

    private RefillQueue() {
    }

    public static void scheduleIfEmpty(ServerPlayer player, InteractionHand hand, ItemStack template) {
        if (template == null || template.isEmpty() || !player.getItemInHand(hand).isEmpty()) {
            return;
        }
        PENDING.computeIfAbsent(player.getUUID(), ignored -> new EnumMap<>(InteractionHand.class))
                .put(hand, new PendingRefill(player, template.copy()));
    }

    public static void drain() {
        if (PENDING.isEmpty()) {
            return;
        }

        Map<UUID, EnumMap<InteractionHand, PendingRefill>> work = new HashMap<>(PENDING);
        PENDING.clear();
        work.values().forEach(byHand -> byHand.forEach((hand, pending) ->
                RefillService.refillIfStillEmpty(pending.player(), hand, pending.template())));
    }

    private record PendingRefill(ServerPlayer player, ItemStack template) {
    }
}
