# Lootr (1.7.10)

A port of [Lootr](https://github.com/LootrMinecraft/Lootr) by noobanidus and embeddedt to 1.7.10.
The code is MIT, and I remade the textures from scratch as the upstream ones are ARR.

## How it works

1.7.10 has no loot tables, so a chest's contents exist only at the moment world generation writes them.
So we have to watch chunk population: any chest that appears during population with items in it is swapped for a Lootr chest,
and the generated contents are kept as a template. On first open each player receives a copy of that template, stored in `data/lootr_<id>.dat` in the world save.

This works for anything that fills chests during generation:
vanilla structures, Roguelike Dungeons, Twilight Forest, and any other mod that places chests during chunk population.
Chests placed by terrain generation (before population), empty chests, and chests in chunks generated before the mod was installed are not converted.

## Commands

| Command | Effect |
|---|---|
| `/lootr custom` | Convert the vanilla chest you are looking at or standing on into a Lootr chest, using its current contents as the template |
| `/lootr clear <player>` | Remove that player's stored inventories from every Lootr chest |
| `/lootr reset [x y z]` | Remove all stored inventories of a chest and mark it unopened for everyone |
| `/lootr openers [x y z]` | List who has opened a chest |
| `/lootr info [x y z]` | Show a chest's id, template size, inventory count, loot category |

## Config

| Key | Default | Meaning |
|---|---|---|
| `convertibleBlocks` | `minecraft:chest, minecraft:trapped_chest` | Blocks whose freshly generated, non-empty tiles become Lootr chests |
| `dimensionWhitelist` / `dimensionBlacklist` | empty | Restrict conversion by dimension id |
| `convertEmptyChests` | `false` | Also convert generated chests that are still empty (for mods that fill later) |
| `rerollFromChestGenHooks` | `false` | Fresh loot per player for `ChestGenHooks`-filled chests |
| `shufflePerPlayer` | `false` | Shuffle template slots per player |
| `disableBreak` / `enableFakePlayerBreak` | `false` | Break protection rules |
| `blastResistant` / `blastImmune` | `false` | Explosion handling |
| `zeroComparator` | `false` | Comparators read 0 instead of 1 |
| `decayAll` / `decayDimensions` / `decayValue` | off / 5 min | Chests crumble some time after their first opening |
| `refreshAll` / `refreshDimensions` / `refreshValue` | off / 20 min | Chests reset for everyone some time after their first opening |
| `disableNotifications` / `notificationDelay` | `false` / 30 s | Decay and refresh chat notices |
| `vanillaTextures` | `false` | Render exactly like vanilla chests |
| `unopenedTint` / `openedTint` | `E0B040` / `8C8C8C` | Tints used when no custom textures are shipped |

## License

MIT. See [`LICENSE`](LICENSE) <p>
Credits:
- Original Lootr: noobanidus and contributors
- 1.7.10 port: Kolja