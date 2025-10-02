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
```

Field notes:
- `weight`: relative weight in random selection; higher = more common.
- `min-level`: minimum fishing level to fish up sea creature. 
- `potion-effects`: `EFFECT:AMPLIFIER:DURATION_TICKS` (e.g. `SPEED:1:600`).
- `equipment`: keys are one of `HAND, OFF_HAND, HEAD, CHEST, LEGS, FEET`. Values are Bukkit `Material` names.
- `commands`: executed as console; `%player%` is replaced with the fisher's name.
- `biomes`, `worlds`, `min-y`, `max-y` are optional filters.
- `fishing-xp`: overrides default AuraSkills xp for that creature.
- `drops`: custom drop spec. Format:
  - YAML map form:
    ```yaml
    drops:
      replace-default: true|false   # if true, clear vanilla drops
      items:
        - material: DIAMOND
          min: 1
          max: 2
          chance: 0.25   # 25% chance
        - material: GOLD_NUGGET
          min: 2
          max: 5
          chance: 0.5
    ```
  - Quick string form (MATERIAL[:min-max][:chance]):
    ```yaml
    drops:
      items:
        - PRISMARINE_CRYSTALS:4-8:1.0
        - HEART_OF_THE_SEA:1:0.15
    ```
  - `chance` range: 0.0–1.0 (1.0 = 100%).
  - If `replace-default` omitted it defaults to false.

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

## CI / Releases
A GitHub Actions workflow (`.github/workflows/release.yml`) automatically builds and (if needed) publishes a GitHub Release on every push to `main` (or `master`).

How it works:
1. Checks out code, sets up Java 21.
2. Extracts `project.version` from `pom.xml`.
3. Builds the shaded jar: `target/SeaCreatures-<version>.jar`.
4. Creates (if absent) a tag named `v<version-without-"-SNAPSHOT">`.
5. Publishes a GitHub Release uploading the jar.
6. Marks the Release as a *prerelease* if the version ends with `-SNAPSHOT`.

Release versioning tips:
- For a stable release, change `<version>` in `pom.xml` from e.g. `1.0-SNAPSHOT` to `1.0.0` (or `1.1.0`) and push. The workflow will create tag `v1.0.0`.
- After releasing, bump to the next development snapshot (e.g. `1.1.0-SNAPSHOT`).
- If you re-run with the same version, the workflow will skip creating a duplicate release.

Manual trigger: You can also run the workflow manually from the *Actions* tab (workflow_dispatch).

Future enhancements (optional):
- Generate changelog notes from commit messages.
- Sign artifacts or attach additional files (e.g., sources jar).
- Auto-bump version via a separate workflow.

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

Example with custom drops:
```yaml
- id: ELDER_GUARDIAN
  type: ELDER_GUARDIAN
  weight: 1
  fishing-xp: 1000
  drops:
    replace-default: true
    items:
      - SPONGE:1-2:1.0
      - PRISMARINE_CRYSTALS:4-8:1.0
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
MIT License. See the `LICENSE` file for full text.

---
Questions or enhancements needed? Adjust the config and reload, or extend the manager / listener.
