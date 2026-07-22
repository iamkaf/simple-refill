import { Capability, Readiness, describe, pos, test } from "@teakit/test";
import type { TeaKitTestContext } from "@teakit/test";

const placementTarget = pos(0, 79, 0);

describe.configure({
  timeout: "8m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.PlayerInteractions,
    Capability.PlayerInventory,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
  ],
});

describe("minirefill", () => {
  test("refills placed blocks in both hands without creating items", async (ctx) => {
    await prepare(ctx);
    await ctx.player.inventory().selectHotbar(0);
    await ctx.commands.batch([
      "/item replace entity @s weapon.mainhand with minecraft:stone 1",
      "/item replace entity @s inventory.0 with minecraft:stone 12",
    ]);
    await ctx.player.useBlock(placementTarget, { face: "up", hand: "main_hand" });
    await ctx.runtime.wait(500);
    await ctx.commands.assert("/execute if items entity @a[limit=1] weapon.mainhand minecraft:stone[count=12]");
    await ctx.commands.assert("/execute unless items entity @a[limit=1] inventory.0 *");

    await prepare(ctx);
    await ctx.commands.batch([
      "/item replace entity @s weapon.offhand with minecraft:cobblestone 1",
      "/item replace entity @s inventory.1 with minecraft:cobblestone 7",
    ]);
    await ctx.player.useBlock(placementTarget, { face: "up", hand: "off_hand" });
    await ctx.runtime.wait(500);
    await ctx.commands.assert("/execute if items entity @a[limit=1] weapon.offhand minecraft:cobblestone[count=7]");
    await ctx.commands.assert("/execute unless items entity @a[limit=1] inventory.1 *");
  });

  test("leaves a hand empty when no exact component match exists", async (ctx) => {
    await prepare(ctx);
    await ctx.player.inventory().selectHotbar(0);
    await ctx.commands.batch([
      "/item replace entity @s weapon.mainhand with minecraft:stone[minecraft:custom_data={minirefill_marker:1b}] 1",
      "/item replace entity @s inventory.0 with minecraft:stone[minecraft:custom_data={minirefill_marker:2b}] 8",
    ]);
    await ctx.player.useBlock(placementTarget, { face: "up", hand: "main_hand" });
    await ctx.runtime.wait(500);

    await ctx.commands.assert("/execute unless items entity @a[limit=1] weapon.mainhand *");
    await ctx.commands.assert("/execute if items entity @a[limit=1] inventory.0 minecraft:stone[count=8]");
    await ctx.commands.assert(
      "/execute if items entity @a[limit=1] inventory.0 minecraft:stone[minecraft:custom_data~{minirefill_marker:2b}]",
    );
  });

  test("refills a completed consumable use", async (ctx) => {
    await prepare(ctx);
    await ctx.player.inventory().selectHotbar(0);
    await ctx.commands.batch([
      "/item replace entity @s weapon.mainhand with minecraft:golden_apple 1",
      "/item replace entity @s inventory.0 with minecraft:golden_apple 4",
    ]);
    await ctx.player.holdUse(true);
    await ctx.runtime.wait(3000);
    await ctx.player.holdUse(false);
    await ctx.runtime.wait(250);

    await ctx.commands.assert("/execute if items entity @a[limit=1] weapon.mainhand minecraft:golden_apple[count=4]");
    await ctx.commands.assert("/execute unless items entity @a[limit=1] inventory.0 *");
  });

  test("matches replacement tools while ignoring only durability damage", async (ctx) => {
    await prepare(ctx);
    await ctx.commands.run("/setblock 0 80 0 minecraft:dirt");
    await ctx.player.inventory().selectHotbar(0);
    await ctx.commands.batch([
      "/item replace entity @s weapon.mainhand with minecraft:golden_shovel[minecraft:damage=31]",
      "/item replace entity @s inventory.0 with minecraft:golden_shovel[minecraft:damage=5]",
    ]);
    await ctx.player.mine(pos(0, 80, 0), { timeout: "5s" });
    await ctx.runtime.wait(250);

    await ctx.commands.assert(
      "/execute if items entity @a[limit=1] weapon.mainhand minecraft:golden_shovel[minecraft:damage=5]",
    );
    await ctx.commands.assert("/execute unless items entity @a[limit=1] inventory.0 *");
  });

  test("does not react to an intentional drop", async (ctx) => {
    await prepare(ctx);
    await ctx.player.inventory().selectHotbar(0);
    await ctx.commands.batch([
      "/item replace entity @s weapon.mainhand with minecraft:stone 1",
      "/item replace entity @s inventory.0 with minecraft:stone 9",
    ]);
    await ctx.player.dropMainHand({ count: 1 });
    await ctx.runtime.wait(250);

    await ctx.commands.assert("/execute unless items entity @a[limit=1] weapon.mainhand *");
    await ctx.commands.assert("/execute if items entity @a[limit=1] inventory.0 minecraft:stone[count=9]");
  });
});

async function prepare(ctx: TeaKitTestContext) {
  await ctx.commands.batch([
    "/gamemode survival @s",
    "/clear @s",
    "/fill -2 79 -2 2 79 2 minecraft:stone",
    "/fill -2 80 -2 2 84 2 minecraft:air",
    "/tp @s 0.5 80 -0.5 0 45",
  ]);
  await ctx.runtime.wait(250);
}
