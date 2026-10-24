# Reality Bending 1.1 — Forge 1.20.1

Minecraft Java Edition 1.20.1 supernatural/anime powers mod. Requires Java 17, Forge 47.2.0, and **GeckoLib 4.7.x for Forge 1.20.1**.

## Dependency setup

GeckoLib is now a required runtime dependency and is declared in Gradle and `mods.toml`. Install the matching **GeckoLib Forge 1.20.1** file in the same Minecraft instance as this mod. Do not use a Fabric or a different Minecraft-version build.

- GeckoLib official project: https://modrinth.com/mod/geckolib
- GeckoLib 4 installation guide: https://github.com/bernie-g/geckolib/wiki/Installation-%28Geckolib4%29

GeckoLib provides the animation framework for custom Geo item/projectile/portal models. This build establishes the dependency and dependency metadata; existing powers still use their current gameplay and particle effects until individual animated Geo models and client renderers are authored.

## Powers and audiovisual pass

- **Ki Blast** — hold to charge, then release to launch.
- **KAMEHAMEHA** — charge-up and release a longer, charge-scaled beam.
- **Telekinesis Unlock** — use once, then press **G** to toggle telekinesis.
- **X-Ray Pulse, Telepathy, Time Warp, Phase Shift, Void Pulse, Black Hole, Reality Rift, Lightning Spear, Reality Check, Plot Armor, Lag Spike, Pocket Dimension, Admin Wand, Chaos Dice, Meme Beacon, Gravity Boots** — supernatural powers with dedicated activation sound profiles and their existing particle/gameplay effects.
- **Elliot Rodger's Supreme Gentleman Fedora (Parody)** — retains its satire-focused in-game effects and now has its own stylized activation cue. The cue is mixed from existing Minecraft sound assets, not real-world audio or an imitation of a real person's voice.
- **Dark-satire archive references** — inert historical-reference items only; no shooter spawn eggs, buffs, or playable perpetrator characters.

## Sound implementation

Power cues are separate registered sound events with individual layered profiles, mixed from Minecraft's existing sound assets. They are distinct cues, not newly recorded WAV files. The next animation pass can connect GeckoLib model keyframes to animation-specific sound and particle triggers.

## Build and install

1. Open [GitHub Actions](https://github.com/iamlordst-jpg/ai-mod-mc/actions).
2. Wait for the latest **Build Reality Bending** run to succeed.
3. Download the `realitybending-forge-1.20.1` artifact and extract the ZIP.
4. Install Forge 1.20.1 and GeckoLib Forge 1.20.1 in the same Minecraft instance.
5. Put the Reality Bending JAR in that instance's `mods` folder.
