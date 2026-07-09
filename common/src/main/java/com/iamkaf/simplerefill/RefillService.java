package com.iamkaf.simplerefill;

import com.iamkaf.amber.api.functions.v1.PlayerFunctions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class RefillService {
    private static final int HOTBAR_SIZE = 9;
    private static final int MAIN_INVENTORY_END = 36;

    private RefillService() {
    }

    public static boolean refillIfStillEmpty(ServerPlayer player, InteractionHand hand, ItemStack template) {
        if (template.isEmpty() || player.hasInfiniteMaterials() || !player.getItemInHand(hand).isEmpty()) {
            return false;
        }

        RefillCategory category = RefillCategory.of(template, player);
        if (!category.enabled()) {
            return false;
        }

        Inventory inventory = player.getInventory();
        int selectedSlot = PlayerFunctions.getSelectedSlot(player);
        int sourceSlot = findReplacement(inventory, selectedSlot, template, category);
        if (sourceSlot < 0) {
            return false;
        }

        ItemStack replacement = inventory.getItem(sourceSlot);
        inventory.setItem(sourceSlot, ItemStack.EMPTY);
        if (hand == InteractionHand.MAIN_HAND) {
            PlayerFunctions.setMainHandItem(player, replacement);
        } else {
            PlayerFunctions.setOffhandItem(player, replacement);
        }

        inventory.setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
        return true;
    }

    private static int findReplacement(
            Inventory inventory,
            int selectedSlot,
            ItemStack template,
            RefillCategory category) {
        if (SimpleRefillConfig.SEARCH_HOTBAR_FIRST.get()) {
            int hotbar = findInHotbar(inventory, selectedSlot, template, category);
            return hotbar >= 0 ? hotbar : findInMainInventory(inventory, template, category);
        }

        int main = findInMainInventory(inventory, template, category);
        return main >= 0 ? main : findInHotbar(inventory, selectedSlot, template, category);
    }

    private static int findInHotbar(
            Inventory inventory,
            int selectedSlot,
            ItemStack template,
            RefillCategory category) {
        for (int offset = 1; offset < HOTBAR_SIZE; offset++) {
            int slot = (selectedSlot + offset) % HOTBAR_SIZE;
            if (matches(template, inventory.getItem(slot), category)) {
                return slot;
            }
        }
        return -1;
    }

    private static int findInMainInventory(Inventory inventory, ItemStack template, RefillCategory category) {
        for (int slot = HOTBAR_SIZE; slot < MAIN_INVENTORY_END; slot++) {
            if (matches(template, inventory.getItem(slot), category)) {
                return slot;
            }
        }
        return -1;
    }

    static boolean matches(ItemStack template, ItemStack candidate, RefillCategory category) {
        if (candidate.isEmpty()) {
            return false;
        }
        if (category != RefillCategory.TOOL) {
            return ItemStack.isSameItemSameComponents(template, candidate);
        }

        ItemStack normalizedTemplate = template.copy();
        ItemStack normalizedCandidate = candidate.copy();
        normalizedTemplate.setDamageValue(0);
        normalizedCandidate.setDamageValue(0);
        return ItemStack.isSameItemSameComponents(normalizedTemplate, normalizedCandidate);
    }

    enum RefillCategory {
        BLOCK,
        TOOL,
        CONSUMABLE,
        GENERIC;

        static RefillCategory of(ItemStack stack, ServerPlayer player) {
            if (stack.getItem() instanceof BlockItem) {
                return BLOCK;
            }
            if (stack.isDamageableItem()) {
                return TOOL;
            }
            if (stack.getUseDuration(player) > 0) {
                return CONSUMABLE;
            }
            return GENERIC;
        }

        boolean enabled() {
            return switch (this) {
                case BLOCK -> SimpleRefillConfig.REFILL_BLOCKS.get();
                case TOOL -> SimpleRefillConfig.REFILL_TOOLS.get();
                case CONSUMABLE -> SimpleRefillConfig.REFILL_CONSUMABLES.get();
                case GENERIC -> SimpleRefillConfig.REFILL_GENERIC_USE_ITEMS.get();
            };
        }
    }
}
