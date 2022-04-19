package yesman.epicfight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetSlotPacket;
import yesman.epicfight.capabilities.provider.ProviderItem;

@Mixin(value = SSetSlotPacket.class)
public abstract class MixinSSetSlotPacket {
	@Shadow
	private int windowId;
	@Shadow
	private int slot;
	@Shadow
	private ItemStack item;
	
	@Inject(at = @At(value = "HEAD"), method = "readPacketData(Lnet/minecraft/network/PacketBuffer;)V", cancellable = true)
	private void readPacketDataAndCheckItemstack(PacketBuffer buf, CallbackInfo info) {
		info.cancel();
		this.windowId = buf.readByte();
	    this.slot = buf.readShort();
		
		if (!ProviderItem.initialized()) {
			ProviderItem.open();
		}
		
		this.item = buf.readItemStack();
		
		if (!ProviderItem.initialized()) {
			ProviderItem.close();
		}
	}
}