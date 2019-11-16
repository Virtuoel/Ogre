package virtuoel.towelette.mixin.layer.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.chunk.ChunkSection;
import virtuoel.towelette.api.ChunkSectionStateLayer;

@Mixin(ChunkSection.class)
public abstract class ChunkSectionMixin// implements ChunkSectionStateLayer
{
/*	@Shadow short nonEmptyFluidCount;
	
	@Inject(at = @At("RETURN"), method = "fromPacket(Lnet/minecraft/util/PacketByteBuf;)V")
	public void onFromPacket(PacketByteBuf buffer, CallbackInfo info)
	{
		this.nonEmptyFluidCount = buffer.readShort();
		getFluidStateContainer().fromPacket(buffer);
	}*/
}
