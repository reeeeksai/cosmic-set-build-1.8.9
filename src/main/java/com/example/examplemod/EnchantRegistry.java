package com.example.examplemod;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

enum Rarity { COMMON, UNIQUE, ELITE, ULTIMATE, LEGENDARY, MASTERY, SOUL, HEROIC }

enum SlotGroup { BOOTS, LEGGINGS, CHESTPLATE, HELMET, ARMOR, WEAPON, SWORD, AXE }

final class EnchantDef {
  final String id;
  final String name;
  final int maxLevel;
  final Rarity rarity;
  final EnumSet<SlotGroup> appliesTo;
  final String buildsFromId; // null if not an upgrade/heroic

  EnchantDef(String id, String name, int maxLevel, Rarity rarity, EnumSet<SlotGroup> appliesTo, String buildsFromId) {
    this.id = id;
    this.name = toTitleCase(name);
    this.maxLevel = maxLevel;
    this.rarity = rarity;
    this.appliesTo = appliesTo;
    this.buildsFromId = buildsFromId;
  }

  private static String toTitleCase(String s) {
    if (s == null) return null;
    String[] parts = s.split("\\s+");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      String p = parts[i];
      if (p.length() == 0) continue;
      sb.append(Character.toUpperCase(p.charAt(0)));
      if (p.length() > 1) sb.append(p.substring(1));
      if (i < parts.length - 1) sb.append(' ');
    }
    return sb.toString();
  }
}

public final class EnchantRegistry {
  private static final Map<String, EnchantDef> BY_ID = new HashMap<String, EnchantDef>();

  static {
    put(new EnchantDef("absolute_domination", "absolute domination", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.WEAPON), "dominate"));
    put(new EnchantDef("aegis", "aegis", 6, Rarity.LEGENDARY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("alien_implants", "alien implants", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.HELMET), "implants"));
    put(new EnchantDef("angelic", "angelic", 5, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("anti_gank", "anti gank", 4, Rarity.LEGENDARY, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("anti_gravity", "anti gravity", 3, Rarity.ELITE, EnumSet.of(SlotGroup.BOOTS), null));
    put(new EnchantDef("anti_nether", "anti nether", 1, Rarity.ULTIMATE, EnumSet.of(SlotGroup.HELMET), null));
    put(new EnchantDef("aquatic", "aquatic", 1, Rarity.COMMON, EnumSet.of(SlotGroup.HELMET), null));
    put(new EnchantDef("armored", "armored", 4, Rarity.LEGENDARY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("arrow_break", "arrow break", 6, Rarity.ULTIMATE, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("arrow_deflect", "arrow deflect", 4, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("assassin", "assassin", 5, Rarity.ULTIMATE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("avenging_angel", "avenging angel", 4, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("barbarian", "barbarian", 4, Rarity.LEGENDARY, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("berserk", "berserk", 5, Rarity.UNIQUE, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("bewitched_hex", "bewitched hex", 5, Rarity.HEROIC, EnumSet.of(SlotGroup.AXE), "hex"));
    put(new EnchantDef("bitter_overwhelm", "bitter overwhelm", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.AXE), "overwhelm"));
    put(new EnchantDef("blackout", "blackout", 4, Rarity.MASTERY, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("blacksmith", "blacksmith", 5, Rarity.LEGENDARY, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("bleed", "bleed", 6, Rarity.ULTIMATE, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("blessed", "blessed", 4, Rarity.ULTIMATE, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("blind", "blind", 3, Rarity.ELITE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("block", "block", 3, Rarity.ULTIMATE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("blood_link", "blood link", 5, Rarity.LEGENDARY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("blood_lust", "blood lust", 6, Rarity.LEGENDARY, EnumSet.of(SlotGroup.CHESTPLATE), null));
    put(new EnchantDef("bloody_deep_wounds", "bloody deep wounds", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "deep_wounds"));
    put(new EnchantDef("boss_slayer", "boss slayer", 5, Rarity.LEGENDARY, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("brutal_barbarian", "brutal barbarian", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.AXE), "barbarian"));
    put(new EnchantDef("cactus", "cactus", 2, Rarity.ELITE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("chain_lifesteal", "chain lifesteal", 5, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("clarity", "clarity", 3, Rarity.LEGENDARY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("cleave", "cleave", 7, Rarity.ULTIMATE, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("commander", "commander", 5, Rarity.UNIQUE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("corrupt", "corrupt", 4, Rarity.ULTIMATE, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("creeper_armor", "creeper armor", 3, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("curse", "curse", 5, Rarity.UNIQUE, EnumSet.of(SlotGroup.CHESTPLATE), null));
    put(new EnchantDef("custom_creeper_armor", "custom creeper armor", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "creeper_armor"));
    put(new EnchantDef("deadly_disarmor", "deadly disarmor", 8, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "disarmor"));
    put(new EnchantDef("death_god", "death god", 3, Rarity.LEGENDARY, EnumSet.of(SlotGroup.HELMET), null));
    put(new EnchantDef("death_pact", "death pact", 3, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("deathbringer", "deathbringer", 3, Rarity.LEGENDARY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("decapitation", "decapitation", 3, Rarity.COMMON, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("deep_bleed", "deep bleed", 6, Rarity.HEROIC, EnumSet.of(SlotGroup.AXE), "bleed"));
    put(new EnchantDef("deep_wounds", "deep wounds", 3, Rarity.UNIQUE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("demonforged", "demonforged", 4, Rarity.ELITE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("demonic_gateway", "demonic gateway", 6, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("demonic_lifesteal", "demonic lifesteal", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.WEAPON), "lifesteal"));
    put(new EnchantDef("destruction", "destruction", 5, Rarity.LEGENDARY, EnumSet.of(SlotGroup.HELMET), null));
    put(new EnchantDef("devour", "devour", 4, Rarity.LEGENDARY, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("diminish", "diminish", 6, Rarity.LEGENDARY, EnumSet.of(SlotGroup.CHESTPLATE), null));
    put(new EnchantDef("disarmor", "disarmor", 8, Rarity.LEGENDARY, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("discombobulate", "discombobulate", 1, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("disintegrate", "disintegrate", 4, Rarity.ULTIMATE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("divine_enlighted", "divine enlighted", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "enlighted"));
    put(new EnchantDef("divine_immolation", "divine immolation", 4, Rarity.SOUL, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("divine_light", "divine light", 5, Rarity.MASTERY, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("dodge", "dodge", 5, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("dominate", "dominate", 4, Rarity.ULTIMATE, EnumSet.of(SlotGroup.WEAPON), null));
    put(new EnchantDef("double_strike", "double strike", 3, Rarity.LEGENDARY, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("drunk", "drunk", 4, Rarity.LEGENDARY, EnumSet.of(SlotGroup.HELMET), null));
    put(new EnchantDef("enchant_reflect", "enchant reflect", 10, Rarity.LEGENDARY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("ender_shift", "ender shift", 3, Rarity.UNIQUE, EnumSet.of(SlotGroup.HELMET), null));
    put(new EnchantDef("ender_walker", "ender walker", 5, Rarity.ULTIMATE, EnumSet.of(SlotGroup.BOOTS), null));
    put(new EnchantDef("enlighted", "enlighted", 3, Rarity.LEGENDARY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("enrage", "enrage", 3, Rarity.ULTIMATE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("epicness", "epicness", 3, Rarity.COMMON, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("epidemic_carrier", "epidemic carrier", 8, Rarity.HEROIC, EnumSet.of(SlotGroup.LEGGINGS), "plague_carrier"));
    put(new EnchantDef("ethereal_dodge", "ethereal dodge", 1, Rarity.HEROIC, EnumSet.of(SlotGroup.BOOTS), "dodge"));
    put(new EnchantDef("execute", "execute", 7, Rarity.ELITE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("explosives_expert", "explosives expert", 1, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("exterminator", "exterminator", 3, Rarity.LEGENDARY, EnumSet.of(SlotGroup.LEGGINGS), null));
    put(new EnchantDef("extreme_insanity", "extreme insanity", 8, Rarity.HEROIC, EnumSet.of(SlotGroup.AXE), "insanity"));
    put(new EnchantDef("featherweight", "featherweight", 3, Rarity.UNIQUE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("feign_death", "feign death", 4, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("gears", "gears", 3, Rarity.LEGENDARY, EnumSet.of(SlotGroup.BOOTS), null));
    put(new EnchantDef("ghost", "ghost", 3, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("ghostly_ghost", "ghostly ghost", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "ghost"));
    put(new EnchantDef("glowing", "glowing", 1, Rarity.COMMON, EnumSet.of(SlotGroup.HELMET), null));
    put(new EnchantDef("godly_overload", "godly overload", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "overload"));
    put(new EnchantDef("greatsword", "greatsword", 5, Rarity.ELITE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("guardians", "guardians", 10, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("guided_rocket_escape", "guided rocket escape", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.BOOTS), "rocket_escape"));
    put(new EnchantDef("hardened", "hardened", 3, Rarity.ELITE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("headless", "headless", 3, Rarity.COMMON, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("heavy", "heavy", 5, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("hero_killer", "hero killer", 3, Rarity.SOUL, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("heroic_enchant_reflect", "heroic enchant reflect", 10, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "enchant_reflect"));
    put(new EnchantDef("hex", "hex", 5, Rarity.LEGENDARY, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("holy_aegis", "holy aegis", 6, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "aegis"));
    put(new EnchantDef("horrify", "horrify", 4, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("ice_aspect", "ice aspect", 3, Rarity.ULTIMATE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("immortal", "immortal", 4, Rarity.SOUL, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("implants", "implants", 3, Rarity.ULTIMATE, EnumSet.of(SlotGroup.HELMET), null));
    put(new EnchantDef("infinite_luck", "infinite luck", 5, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "lucky"));
    put(new EnchantDef("inquisitive", "inquisitive", 4, Rarity.LEGENDARY, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("insanity", "insanity", 8, Rarity.LEGENDARY, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("insomnia", "insomnia", 7, Rarity.COMMON, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("inversion", "inversion", 4, Rarity.LEGENDARY, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("kill_aura", "kill aura", 5, Rarity.LEGENDARY, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("lava_strider", "lava strider", 1, Rarity.MASTERY, EnumSet.of(SlotGroup.BOOTS), null));
    put(new EnchantDef("leadership", "leadership", 5, Rarity.LEGENDARY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("lifebloom", "lifebloom", 5, Rarity.UNIQUE, EnumSet.of(SlotGroup.LEGGINGS), null));
    put(new EnchantDef("lifesteal", "lifesteal", 5, Rarity.LEGENDARY, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("lucky", "lucky", 10, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("maliciously_corrupt", "maliciously corrupt", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.AXE), "corrupt"));
    put(new EnchantDef("mark_of_the_beast", "mark of the beast", 6, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("marksman", "marksman", 4, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("martyr_valor", "martyr valor", 5, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "valor"));
    put(new EnchantDef("master_blacksmith", "master blacksmith", 5, Rarity.HEROIC, EnumSet.of(SlotGroup.AXE), "blacksmith"));
    put(new EnchantDef("master_inquisitive", "master inquisitive", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "inquisitive"));
    put(new EnchantDef("mega_heavy", "mega heavy", 5, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "heavy"));
    put(new EnchantDef("metaphysical", "metaphysical", 4, Rarity.ULTIMATE, EnumSet.of(SlotGroup.BOOTS), null));
    put(new EnchantDef("mighty_cactus", "mighty cactus", 2, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "cactus"));
    put(new EnchantDef("mighty_cleave", "mighty cleave", 5, Rarity.HEROIC, EnumSet.of(SlotGroup.AXE), "cleave"));
    put(new EnchantDef("mortal_coil", "mortal coil", 5, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("nature_wrath", "nature wrath", 4, Rarity.SOUL, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("neutralize", "neutralize", 5, Rarity.MASTERY, EnumSet.of(SlotGroup.WEAPON), null));
    put(new EnchantDef("nimble", "nimble", 5, Rarity.UNIQUE, EnumSet.of(SlotGroup.BOOTS), null));
    put(new EnchantDef("nocturnal_insomnia", "nocturnal insomnia", 7, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "insomnia"));
    put(new EnchantDef("obliterate", "obliterate", 5, Rarity.COMMON, EnumSet.of(SlotGroup.WEAPON), null));
    put(new EnchantDef("obsidian_guardians", "obsidian guardians", 10, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "guardians"));
    put(new EnchantDef("obsidianshield", "obsidianshield", 1, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("opening_combo", "opening combo", 5, Rarity.MASTERY, EnumSet.of(SlotGroup.WEAPON), null));
    put(new EnchantDef("overload", "overload", 3, Rarity.LEGENDARY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("overwhelm", "overwhelm", 4, Rarity.LEGENDARY, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("paladin_armored", "paladin armored", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "armored"));
    put(new EnchantDef("paradox", "paradox", 5, Rarity.SOUL, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("paralyze", "paralyze", 4, Rarity.ELITE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("perfect_solitude", "perfect solitude", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "solitude"));
    put(new EnchantDef("permafrost", "permafrost", 6, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("permanent_execute", "permanent execute", 7, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "execute"));
    put(new EnchantDef("phoenix", "phoenix", 3, Rarity.SOUL, EnumSet.of(SlotGroup.BOOTS), null));
    put(new EnchantDef("planetary_deathbringer", "planetary deathbringer", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "deathbringer"));
    put(new EnchantDef("poison", "poison", 3, Rarity.ELITE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("poisoned", "poisoned", 4, Rarity.ELITE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("polymorphic_metaphysical", "polymorphic metaphysical", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.BOOTS), "metaphysical"));
    put(new EnchantDef("protector", "protector", 5, Rarity.LEGENDARY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("pummel", "pummel", 3, Rarity.ELITE, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("ragdoll", "ragdoll", 4, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("rage", "rage", 6, Rarity.LEGENDARY, EnumSet.of(SlotGroup.WEAPON), null));
    put(new EnchantDef("reality_inversion", "reality inversion", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "inversion"));
    put(new EnchantDef("reflective_block", "reflective block", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "block"));
    put(new EnchantDef("reforged", "reforged", 10, Rarity.ELITE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("reinforced_tank", "reinforced tank", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "tank"));
    put(new EnchantDef("repair_guard", "repair guard", 3, Rarity.ELITE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("resilience", "resilience", 4, Rarity.ELITE, EnumSet.of(SlotGroup.HELMET), null));
    put(new EnchantDef("righteous_anti_gank", "righteous anti gank", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.AXE), "anti_gank"));
    put(new EnchantDef("robotic_ruse", "robotic ruse", 10, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "undead_ruse"));
    put(new EnchantDef("rocket_escape", "rocket escape", 3, Rarity.ELITE, EnumSet.of(SlotGroup.BOOTS), null));
    put(new EnchantDef("rot_and_decay", "rot and decay", 10, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("rouge", "rouge", 3, Rarity.SOUL, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("sabotage", "sabotage", 5, Rarity.SOUL, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("self_destruct", "self destruct", 3, Rarity.UNIQUE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("shackle", "shackle", 3, Rarity.ELITE, EnumSet.of(SlotGroup.WEAPON), null));
    put(new EnchantDef("shadow_assassin", "shadow assassin", 5, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "assassin"));
    put(new EnchantDef("shockwave", "shockwave", 5, Rarity.ELITE, EnumSet.of(SlotGroup.CHESTPLATE), null));
    put(new EnchantDef("silence", "silence", 4, Rarity.LEGENDARY, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("skill_swipe", "skill swipe", 5, Rarity.UNIQUE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("smoke_bomb", "smoke bomb", 8, Rarity.ELITE, EnumSet.of(SlotGroup.HELMET), null));
    put(new EnchantDef("solitude", "solitude", 3, Rarity.ELITE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("soul_hardened", "soul hardened", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.ARMOR), "hardened"));
    put(new EnchantDef("soul_master", "soul master", 5, Rarity.MASTERY, EnumSet.of(SlotGroup.WEAPON), null));
    put(new EnchantDef("soul_siphon", "soul siphon", 4, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("soul_trap", "soul trap", 3, Rarity.SOUL, EnumSet.of(SlotGroup.AXE), null));
    put(new EnchantDef("spirit_link", "spirit link", 5, Rarity.ELITE, EnumSet.of(SlotGroup.CHESTPLATE), null));
    put(new EnchantDef("spirits", "spirits", 10, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("springs", "springs", 3, Rarity.ELITE, EnumSet.of(SlotGroup.BOOTS), null));
    put(new EnchantDef("sticky", "sticky", 8, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("stormcaller", "stormcaller", 4, Rarity.ELITE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("tank", "tank", 4, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("thundering_blow", "thundering blow", 3, Rarity.COMMON, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("titan_trap", "titan trap", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "trap"));
    put(new EnchantDef("tombstone", "tombstone", 10, Rarity.MASTERY, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("training", "training", 10, Rarity.UNIQUE, EnumSet.of(SlotGroup.WEAPON), null));
    put(new EnchantDef("trap", "trap", 3, Rarity.ELITE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("trickster", "trickster", 8, Rarity.ELITE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("undead_ruse", "undead ruse", 10, Rarity.ELITE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("unrestrained_enrage", "unrestrained enrage", 3, Rarity.HEROIC, EnumSet.of(SlotGroup.SWORD), "enrage"));
    put(new EnchantDef("valor", "valor", 5, Rarity.ULTIMATE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("vampire", "vampire", 3, Rarity.ELITE, EnumSet.of(SlotGroup.SWORD), null));
    put(new EnchantDef("vampiric_devour", "vampiric devour", 4, Rarity.HEROIC, EnumSet.of(SlotGroup.AXE), "devour"));
    put(new EnchantDef("vengeful_diminish", "vengeful diminish", 6, Rarity.HEROIC, EnumSet.of(SlotGroup.CHESTPLATE), "diminish"));
    put(new EnchantDef("voodoo", "voodoo", 6, Rarity.ELITE, EnumSet.of(SlotGroup.ARMOR), null));
    put(new EnchantDef("web_walker", "web walker", 1, Rarity.MASTERY, EnumSet.of(SlotGroup.BOOTS), null));
    put(new EnchantDef("wither", "wither", 5, Rarity.ELITE, EnumSet.of(SlotGroup.ARMOR), null));
  }

  private static void put(EnchantDef def) { BY_ID.put(def.id, def); }

  public static EnchantDef get(String id) { return BY_ID.get(id); }
  public static Collection<EnchantDef> all() { return BY_ID.values(); }
}
