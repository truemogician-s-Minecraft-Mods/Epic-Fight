package yesman.epicfight.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.capabilities.ModCapabilities;
import yesman.epicfight.capabilities.entity.CapabilityEntity;
import yesman.epicfight.capabilities.entity.player.PlayerData;
import yesman.epicfight.capabilities.provider.ProviderEntity;
import yesman.epicfight.capabilities.provider.ProviderItem;
import yesman.epicfight.capabilities.provider.ProviderProjectile;
import yesman.epicfight.capabilities.provider.ProviderSkill;
import yesman.epicfight.main.EpicFightMod;

@Mod.EventBusSubscriber(modid=EpicFightMod.MODID)
public class CapabilityEvent {
	@SubscribeEvent
	public static void attachItemCapability(AttachCapabilitiesEvent<ItemStack> event) {
		ProviderItem prov = ProviderItem.isStreamOpened() ? new ProviderItem(event.getObject(), true) : new ProviderItem(event.getObject());
		if (prov.hasCapability()) {
			event.addCapability(new ResourceLocation(EpicFightMod.MODID, "item_cap"), prov);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
	public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
		ProviderEntity entityProvider = new ProviderEntity(event.getObject());
		if (entityProvider.hasCapability()) {
			CapabilityEntity entityCap = entityProvider.getCapability(ModCapabilities.CAPABILITY_ENTITY).orElse(null);
			entityCap.onEntityConstructed(event.getObject());
			event.addCapability(new ResourceLocation(EpicFightMod.MODID, "entity_cap"), entityProvider);
			if (entityCap instanceof PlayerData<?>) {
				if (event.getObject().getCapability(ModCapabilities.CAPABILITY_SKILL).orElse(null) == null) {
					PlayerData<?> playerdata = (PlayerData<?>)entityCap;
					if (playerdata != null) {
						ProviderSkill skillProvider = new ProviderSkill(playerdata);
						event.addCapability(new ResourceLocation(EpicFightMod.MODID, "skill_cap"), skillProvider);
					}
				}
			}
		}
		
		if (event.getObject() instanceof ProjectileEntity) {
			ProjectileEntity projectile = ((ProjectileEntity)event.getObject());
			if(event.getObject().getCapability(ModCapabilities.CAPABILITY_PROJECTILE).orElse(null) == null) {
				ProviderProjectile projectileProvider = new ProviderProjectile(projectile);
				if(projectileProvider.hasCapability()) {
					event.addCapability(new ResourceLocation(EpicFightMod.MODID, "projectile_cap"), projectileProvider);
				}
			}
		}
	}
}