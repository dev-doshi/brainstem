# Brainstem

Vanilla-exact mob AI optimisation for Fabric servers (Minecraft 26.3).

Brainstem makes villagers, zombies, farm animals and brain-driven mobs cheaper to tick without changing how they behave. Every default-on optimisation returns exactly what vanilla would and consumes the same random numbers. It only skips work that provably can't change the result.

Download: [Modrinth](https://modrinth.com/mod/brainstem)

## Modules

| Config key | What it does | Default |
|---|---|---|
| `mtvg` | Zombies' *Move Through Village* goal: skips random position attempts (or the whole search) when no village centre can be in reach, still consuming the same random draws | on |
| `oneShotGate` | Brain behaviours: pre-checks the pure memory conditions and rejects behaviours that can't start before the trigger tree runs | on |
| `competitorScan` | Villager job-site competition: a claims index replaces the nearby-villager scan when there is no rival | on |
| `temptScan` | TemptGoal: drops players provably out of tempt range before the held-item check | on |
| `reachProbe` / `reachMemo` | Experimental path-result probe and memo for villager POI pathfinding | off |

## Config

`config/brainstem.json` is created on first start.

- **Module toggles:** each module can be turned off with its key from the table above.
- **Verify mode:** set `"verify": "sample"` or `"all"` to run vanilla alongside the optimised code and compare results and random state. A module that ever differs is switched off and the difference is logged.
- **Stats:** set `"stats": true` to collect counters, shown with `/brainstem stats`.

## Building

`build.sh` compiles against an unobfuscated 26.3 Fabric server (`../server-263`, including its `libraries/` and `.fabric/processedMods/`) and MixinExtras 0.5.5. No Loom or remapping is needed. It uses Java 25+.

## License

LGPL-3.0-only. See [COPYING.LESSER](COPYING.LESSER) and [COPYING](COPYING).
