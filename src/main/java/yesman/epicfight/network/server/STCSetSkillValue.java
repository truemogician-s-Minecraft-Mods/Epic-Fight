package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import yesman.epicfight.capabilities.ModCapabilities;
import yesman.epicfight.client.capabilites.player.ClientPlayerData;

public class STCSetSkillValue {
	private float floatSet;
	private boolean boolset;
	private int index;
	private Target target;

	public STCSetSkillValue() {
		this.floatSet = 0;
		this.index = 0;
	}

	public STCSetSkillValue(Target target, int slot, float amount, boolean boolset) {
		this.target = target;
		this.floatSet = amount;
		this.boolset = boolset;
		this.index = slot;
	}
	
	public static STCSetSkillValue fromBytes(PacketBuffer buf) {
		return new STCSetSkillValue(Target.values()[buf.readInt()], buf.readInt(), buf.readFloat(), buf.readBoolean());
	}
	
	public static void toBytes(STCSetSkillValue msg, PacketBuffer buf) {
		buf.writeInt(msg.target.ordinal());
		buf.writeInt(msg.index);
		buf.writeFloat(msg.floatSet);
		buf.writeBoolean(msg.boolset);
	}
	
	public static void handle(STCSetSkillValue msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			ClientPlayerData playerdata = (ClientPlayerData) Minecraft.getInstance().player.getCapability(ModCapabilities.CAPABILITY_ENTITY, null).orElse(null);
			
			if (playerdata != null) {
				switch (msg.target) {
				case COOLDOWN:
					playerdata.getSkill(msg.index).setResource(msg.floatSet);
					break;
				case DURATION:
					playerdata.getSkill(msg.index).setDuration((int) msg.floatSet);
					break;
				case MAX_DURATION:
					playerdata.getSkill(msg.index).setMaxDuration((int) msg.floatSet);
					break;
				case STACK:
					playerdata.getSkill(msg.index).setStack((int) msg.floatSet);
					break;
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
	
	public static enum Target {
		COOLDOWN, DURATION, MAX_DURATION, STACK;
	}
}