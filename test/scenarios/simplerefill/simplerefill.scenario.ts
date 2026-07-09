import { Capability, Readiness, describe, expect, test } from "@teakit/test";
import type { ScenarioDefinition, ScenarioResult, TeaKitTestContext } from "@teakit/test";

describe.configure({
  timeout: "8m",
  readiness: [Readiness.ClientReady, Readiness.IntegratedServerReady, Readiness.PlayerSpawned],
  capabilities: [
    Capability.LegacyJsonScenarios,
    Capability.PlayerActions,
    Capability.PlayerInventory,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
  ],
});

describe("Simple Refill", () => {
  test("refills placed blocks in both hands without creating items", async (ctx) => {
    await runAndExpect(ctx, blockRefillDefinition);
    await ctx.commands.assert("/execute if items entity @a[limit=1] weapon.mainhand minecraft:stone[count=12]");
    await ctx.commands.assert("/execute unless items entity @a[limit=1] inventory.0 *");

    await runAndExpect(ctx, offhandRefillDefinition);
    await ctx.commands.assert("/execute if items entity @a[limit=1] weapon.offhand minecraft:cobblestone[count=7]");
    await ctx.commands.assert("/execute unless items entity @a[limit=1] inventory.1 *");
  });

  test("leaves a hand empty when no exact component match exists", async (ctx) => {
    await runAndExpect(ctx, componentMismatchDefinition);
    await ctx.commands.assert("/execute unless items entity @a[limit=1] weapon.mainhand *");
    await ctx.commands.assert(
      "/execute if items entity @a[limit=1] inventory.0 minecraft:stone[count=8]",
    );
    await ctx.commands.assert(
      "/execute if items entity @a[limit=1] inventory.0 minecraft:stone[minecraft:custom_data~{simplerefill_marker:2b}]",
    );
  });

  test("refills a completed consumable use", async (ctx) => {
    await runAndExpect(ctx, consumableRefillDefinition);
    await ctx.commands.assert("/execute if items entity @a[limit=1] weapon.mainhand minecraft:golden_apple[count=4]");
    await ctx.commands.assert("/execute unless items entity @a[limit=1] inventory.0 *");
  });

  test("matches replacement tools while ignoring only durability damage", async (ctx) => {
    await runAndExpect(ctx, toolRefillDefinition);
    await ctx.commands.assert(
      "/execute if items entity @a[limit=1] weapon.mainhand minecraft:golden_shovel[minecraft:damage=5]",
    );
    await ctx.commands.assert("/execute unless items entity @a[limit=1] inventory.0 *");
  });

  test("does not react to an intentional drop", async (ctx) => {
    await runAndExpect(ctx, intentionalDropDefinition);
    await ctx.commands.assert("/execute unless items entity @a[limit=1] weapon.mainhand *");
    await ctx.commands.assert("/execute if items entity @a[limit=1] inventory.0 minecraft:stone[count=9]");
  });

});

async function runAndExpect(ctx: TeaKitTestContext, definition: ScenarioDefinition) {
  const result = await ctx.scenario.run(definition, { timeoutMs: 30_000 });
  expect(failedSteps(result)).toEqual([]);
}

const baseSetup = [
  { action: "command", command: "/gamemode survival @s" },
  { action: "command", command: "/clear @s" },
  { action: "command", command: "/fill -2 79 -2 2 79 2 minecraft:stone" },
  { action: "command", command: "/fill -2 80 -2 2 84 2 minecraft:air" },
  { action: "command", command: "/tp @s 0.5 80 -0.5 0 45" },
] as const;

const blockRefillDefinition = {
  name: "simplerefill-main-hand-block",
  setup: [
    ...baseSetup,
    { action: "select_hotbar_slot", slot: 0 },
    { action: "wait_ms", durationMs: 500 },
    { action: "command", command: "/item replace entity @s weapon.mainhand with minecraft:stone 1" },
    { action: "command", command: "/item replace entity @s inventory.0 with minecraft:stone 12" },
  ],
  steps: [
    { action: "use_block_server", x: 0, y: 79, z: 0, direction: "up", hand: "main_hand" },
    { action: "wait_ms", durationMs: 500 },
  ],
} as ScenarioDefinition;

const offhandRefillDefinition = {
  name: "simplerefill-offhand-block",
  setup: [
    ...baseSetup,
    { action: "command", command: "/item replace entity @s weapon.offhand with minecraft:cobblestone 1" },
    { action: "command", command: "/item replace entity @s inventory.1 with minecraft:cobblestone 7" },
  ],
  steps: [
    { action: "use_block_server", x: 0, y: 79, z: 0, direction: "up", hand: "off_hand" },
    { action: "wait_ms", durationMs: 500 },
  ],
} as ScenarioDefinition;

const componentMismatchDefinition = {
  name: "simplerefill-component-mismatch",
  setup: [
    ...baseSetup,
    { action: "select_hotbar_slot", slot: 0 },
    { action: "wait_ms", durationMs: 500 },
    {
      action: "command",
      command: "/item replace entity @s weapon.mainhand with minecraft:stone[minecraft:custom_data={simplerefill_marker:1b}] 1",
    },
    {
      action: "command",
      command: "/item replace entity @s inventory.0 with minecraft:stone[minecraft:custom_data={simplerefill_marker:2b}] 8",
    },
  ],
  steps: [
    { action: "use_block_server", x: 0, y: 79, z: 0, direction: "up", hand: "main_hand" },
    { action: "wait_ms", durationMs: 500 },
  ],
} as ScenarioDefinition;

const consumableRefillDefinition = {
  name: "simplerefill-consumable",
  setup: [
    ...baseSetup,
    { action: "select_hotbar_slot", slot: 0 },
    { action: "wait_ms", durationMs: 250 },
    { action: "command", command: "/item replace entity @s weapon.mainhand with minecraft:golden_apple 1" },
    { action: "command", command: "/item replace entity @s inventory.0 with minecraft:golden_apple 4" },
  ],
  steps: [
    { action: "set_use_held", held: true },
    { action: "wait_ms", durationMs: 3_000 },
    { action: "set_use_held", held: false },
    { action: "wait_ms", durationMs: 250 },
  ],
} as ScenarioDefinition;

const toolRefillDefinition = {
  name: "simplerefill-tool-durability",
  setup: [
    ...baseSetup,
    { action: "command", command: "/setblock 0 80 0 minecraft:dirt" },
    { action: "select_hotbar_slot", slot: 0 },
    { action: "wait_ms", durationMs: 250 },
    {
      action: "command",
      command: "/item replace entity @s weapon.mainhand with minecraft:golden_shovel[minecraft:damage=31]",
    },
    {
      action: "command",
      command: "/item replace entity @s inventory.0 with minecraft:golden_shovel[minecraft:damage=5]",
    },
  ],
  steps: [
    { action: "break_block", x: 0, y: 80, z: 0, direction: "up", timeoutMs: 5_000, pollMs: 75 },
    { action: "wait_ms", durationMs: 250 },
  ],
} as ScenarioDefinition;

const intentionalDropDefinition = {
  name: "simplerefill-intentional-drop",
  setup: [
    ...baseSetup,
    { action: "select_hotbar_slot", slot: 0 },
    { action: "wait_ms", durationMs: 250 },
    { action: "command", command: "/item replace entity @s weapon.mainhand with minecraft:stone 1" },
    { action: "command", command: "/item replace entity @s inventory.0 with minecraft:stone 9" },
  ],
  steps: [
    { action: "drop_main_hand_item" },
    { action: "wait_ms", durationMs: 250 },
  ],
} as ScenarioDefinition;

function failedSteps(result: ScenarioResult): string[] {
  return ["setup", "steps", "cleanup"].flatMap((phase) => {
    const phaseResults = result[phase as keyof ScenarioResult];
    if (!Array.isArray(phaseResults)) {
      return [];
    }
    return phaseResults
      .filter((step) => {
        const stepResult = step.result as Record<string, unknown> | undefined;
        if (stepResult?.failure != null || stepResult?.failed === true) {
          return true;
        }
        return step.action !== "command" && stepResult?.success === false;
      })
      .map((step) => `${phase}[${step.index ?? "?"}] ${step.action ?? "unknown"}`);
  });
}
