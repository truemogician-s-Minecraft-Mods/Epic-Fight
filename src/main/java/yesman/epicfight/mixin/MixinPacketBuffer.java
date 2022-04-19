package yesman.epicfight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

@Mixin(value = PacketBuffer.class)
public abstract class MixinPacketBuffer {
	@Inject(at = @At(value = "HEAD"), method = "readItemStack()Lnet/minecraft/item/ItemStack;", cancellable = true)
	private void checkCapabilityAndReadItem(CallbackInfoReturnable<ItemStack> info) {
		info.cancel();
		PacketBuffer pb = ((PacketBuffer)((Object)this));
		if (!pb.readBoolean()) {
			info.setReturnValue(ItemStack.EMPTY);
		} else {
			int i = pb.readVarInt();
			int j = pb.readByte();
			Item item = Item.getItemById(i);
			CompoundNBT constructTag = new CompoundNBT();
			CompoundNBT customTag = pb.readCompoundTag();
			if (customTag != null) {
				constructTag.put("tag", customTag);
			}
			constructTag.putInt("Count", j);
			constructTag.putString("id", item.getRegistryName().toString());
			ItemStack itemstack = ItemStack.read(constructTag);
			info.setReturnValue(itemstack);
		}
	}
}