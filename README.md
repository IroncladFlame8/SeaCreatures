# SeaCreatures Plugin

Fish up real and custom sea creatures instead of (or in place of) normal fishing loot.

## Features
- Weighted, configurable list of fishable creatures (vanilla & custom variants)
- Chance influenced by Luck of the Sea enchant level
- Custom display names (& color codes), glowing flag, potion effects, equipment, console commands
- Biome / world / Y-level constraints
- Reload command (`/seacreatures reload`)
- Configurable catch message

## Requirements
- Java 21 runtime (matches Paper 1.21.x requirements)
- PaperMC 1.21.7 build (or compatible 1.21.x if API unchanged)
- Maven 3.8+ to build

## Building
`mvn build` is **not** a valid Maven lifecycle phase; use one of the standard phases below.

Typical commands:
```bash
# Clean previous outputs and produce the shaded jar (if shade configured)
mvn clean package

# (Optional) install to local repo if you depend on it elsewhere
mvn clean install
```
The compiled plugin jar will appear at:
```
target/SeaCreatures-1.0-SNAPSHOT.jar
```
Copy that into your server's `plugins/` folder.

## Configuration (`config.yml`)
```yaml
chance:
  base-percent: 35.0
  per-luck-level-bonus: 2.5
creatures:
  - id: COD
    type: COD
    weight: 30
  # ... more creatures ...
messages:
  catch: '&bYou fished up a &e%creature%&b!'
logging: INFO
```

Field notes:
- `weight`: relative weight in random selection; higher = more common.
- `potion-effects`: `EFFECT:AMPLIFIER:DURATION_TICKS` (e.g. `SPEED:1:600`).
- `equipment`: keys are one of `HAND, OFF_HAND, HEAD, CHEST, LEGS, FEET`. Values are Bukkit `Material` names.
- `commands`: executed as console; `%player%` is replaced with the fisher's name.
- `biomes`, `worlds`, `min-y`, `max-y` are optional filters.

## Commands & Permissions
| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/seacreatures reload` | Reloads config | `seacreatures.reload` | op |

Base command permission: `seacreatures.command` (op by default).

## How It Works
1. Player successfully catches something (`PlayerFishEvent.State.CAUGHT_FISH`).
2. Roll chance = `base-percent + (per-luck-level-bonus * luckLevel)` (clamped 0–100).
3. If the roll passes, existing caught entity/item is removed.
4. A creature is chosen by weight among those whose constraints pass.
5. Creature spawns at hook location, customizations applied, commands executed.
6. Player receives the catch message with `%creature%` substituted.

## Troubleshooting
| Issue | Cause | Fix |
|-------|-------|-----|
| `Unknown lifecycle phase "build"` | Using `mvn build` | Use `mvn clean package` |
| Creature not spawning | Chance too low or filters exclude spawn | Increase chance, relax filters |
| Invalid entity/material warnings | Typos in config | Check `EntityType` / `Material` enums |
| Message shows raw codes | Using `§` or missing `&` codes | Use `&` color codes in config |

## Adding New Creatures
1. Stop server (or use reload command after editing config).
2. Add a new map entry under `creatures:` with at least `id`, `type`, and `weight`.
3. `/seacreatures reload` in-game or console.

Example custom entry:
```yaml
- id: FAST_GUARDIAN
  type: GUARDIAN
  weight: 2
  custom: true
  display-name: '&aSwift Guardian'
  glowing: true
  potion-effects:
    - SPEED:2:400
```

## Roadmap / Ideas (Not Implemented Yet)
- Cooldown per player
- Keep vanilla loot plus creature (toggle)
- Per-creature explicit base chance overrides
- Player progression scaling (e.g., permission tiers)
- Attribute customization re-introduction (was removed for simplicity)

## Contributing / Extending
- Clone and modify.
- Follow existing naming & package layout (`me.shreyjain.seaCreatures`).
- Avoid CraftBukkit internals for forward compatibility.

## License
Not specified. Add a LICENSE file if you intend to distribute publicly.

---
Questions or enhancements needed? Adjust the config and reload, or extend the manager / listener.

