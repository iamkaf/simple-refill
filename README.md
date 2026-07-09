# Simple Refill

Simple Refill moves a matching inventory stack into the hand that just ran
out. It handles placed blocks, consumed items, and broken held tools on the
logical server, preserving stack components and counts exactly.

## Behavior

- Exact item-component matching for blocks and consumables.
- Tool replacement ignores durability damage but preserves every other
  component.
- Main-hand and off-hand refill support.
- Configurable feature categories and inventory search order.
- No shulker-box, bundle, armor-slot, cursor-stack, or open-container scans.

Generic right-click items are disabled by default. Simple Refill only reacts
to item-use and durability events; dropping or otherwise clearing a hand does
not trigger a refill.

## Requirements

- [Amber](https://github.com/iamkaf/amber)
- [Konfig](https://github.com/iamkaf/konfig)
- Fabric API when using Fabric

## Supported versions

Minecraft 1.21.1, 1.21.11, 26.1.2, and 26.2 on Fabric, Forge, and NeoForge.

## Building

```bash
just list-nodes
just build 26.2-fabric
just horizontal-jars
```

Merged multi-loader jars are additional build artifacts:

- Minecraft 26.1.2 and 26.2 merged jars are stable-tier.
- Minecraft 1.21.1 and 1.21.11 merged jars are experimental. Cross-loader
  mixins and addons can be unsafe on these versions, so use the loader-specific
  jars unless you are testing compatibility deliberately.

Loader-specific jars remain available and remain the publication default for
every Minecraft version.

## License

MIT
