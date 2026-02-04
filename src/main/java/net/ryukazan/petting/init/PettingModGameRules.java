/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.ryukazan.petting.init;

import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.GameRules;

@EventBusSubscriber
public class PettingModGameRules {
	public static GameRules.Key<GameRules.BooleanValue> HIDE_TAMED_BOSS_BAR;

	@SubscribeEvent
	public static void registerGameRules(FMLCommonSetupEvent event) {
		HIDE_TAMED_BOSS_BAR = GameRules.register("hideTamedBossBar", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
	}
}