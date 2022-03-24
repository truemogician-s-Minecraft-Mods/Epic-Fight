package yesman.epicfight.events;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.EpicFightGamerules;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.ItemUseEndEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.RightClickItemEvent;

@Mod.EventBusSubscriber(modid=EpicFightMod.MODID)
public class PlayerEvents {
	@SubscribeEvent
	public static void arrowLooseEvent(ArrowLooseEvent event) {
		ColliderPreset.update();
	}
	
	@SubscribeEvent
	public static void rightClickItemServer(RightClickItem event) {
		if (event.getSide() == LogicalSide.SERVER) {
			ServerPlayerPatch playerpatch = (ServerPlayerPatch) event.getPlayer().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
			
			if (playerpatch != null && (playerpatch.getOriginal().getOffhandItem().getUseAnimation() == UseAnim.NONE || playerpatch.getHeldItemCapability(InteractionHand.MAIN_HAND).isTwoHanded())) {
				boolean canceled = playerpatch.getEventListener().triggerEvents(EventType.SERVER_ITEM_USE_EVENT, new RightClickItemEvent<>(playerpatch));
				event.setCanceled(canceled);
			}
		}
	}
	
	@SubscribeEvent
	public static void itemUseStartEvent(LivingEntityUseItemEvent.Start event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			PlayerPatch<?> playerpatch = (PlayerPatch<?>) event.getEntity().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
			InteractionHand hand = player.getItemInHand(InteractionHand.MAIN_HAND).equals(event.getItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			CapabilityItem itemCap = playerpatch.getHeldItemCapability(hand);
			
			if (!playerpatch.getEntityState().skillExecutionPossible()) {
				event.setCanceled(true);
			} else if (event.getItem() == player.getOffhandItem() && playerpatch.getHeldItemCapability(InteractionHand.MAIN_HAND).isTwoHanded()) {
				event.setCanceled(true);
			}
			
			if (itemCap.getUseAnimation(playerpatch) == UseAnim.BLOCK) {
				event.setDuration(1000000);
			}
		}
	}
	
	@SubscribeEvent
	public static void cloneEvent(PlayerEvent.Clone event) {
		event.getOriginal().reviveCaps();
		ServerPlayerPatch oldOne = (ServerPlayerPatch)event.getOriginal().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
		if (oldOne != null && (!event.isWasDeath() || event.getOriginal().level.getGameRules().getBoolean(EpicFightGamerules.KEEP_SKILLS))) {
			ServerPlayerPatch newOne = (ServerPlayerPatch)event.getPlayer().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
			newOne.initFromOldOne(oldOne);
		}
		event.getOriginal().invalidateCaps();
	}
	
	@SubscribeEvent
	public static void changeDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
		Player player = event.getPlayer();
		ServerPlayerPatch playerpatch = (ServerPlayerPatch)player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
		playerpatch.setLivingMotionCurrentItem(playerpatch.getHeldItemCapability(InteractionHand.MAIN_HAND), InteractionHand.MAIN_HAND);
		playerpatch.setLivingMotionCurrentItem(playerpatch.getHeldItemCapability(InteractionHand.OFF_HAND), InteractionHand.OFF_HAND);
	}
	
	@SubscribeEvent
	public static void itemUseStopEvent(LivingEntityUseItemEvent.Stop event) {
		if (event.getEntity().level.isClientSide()) {
			if (event.getEntity() instanceof LocalPlayer) {
				ClientEngine.instance.renderEngine.zoomOut(0);
			}
		} else {
			if (event.getEntity() instanceof ServerPlayer) {
				ServerPlayer player = (ServerPlayer) event.getEntity();
				ServerPlayerPatch playerpatch = (ServerPlayerPatch) player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
				if (playerpatch != null) {
					boolean canceled = playerpatch.getEventListener().triggerEvents(EventType.SERVER_ITEM_STOP_EVENT, new ItemUseEndEvent(playerpatch));
					event.setCanceled(canceled);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void itemUseTickEvent(LivingEntityUseItemEvent.Tick event) {
		if (event.getEntity() instanceof Player) {
			if (event.getItem().getItem() instanceof BowItem) {
				PlayerPatch<?> playerpatch = (PlayerPatch<?>) event.getEntity().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
				if (playerpatch.getEntityState().inaction()) {
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void attackEntityEvent(AttackEntityEvent event) {
		boolean isLivingTarget = event.getTarget() instanceof LivingEntity ? ((LivingEntity)event.getTarget()).attackable() : false;
		if (!event.getEntity().level.getGameRules().getBoolean(EpicFightGamerules.DO_VANILLA_ATTACK) && isLivingTarget) {
			event.setCanceled(true);
		}
	}
}