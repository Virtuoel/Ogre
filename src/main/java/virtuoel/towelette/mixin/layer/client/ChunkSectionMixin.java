package virtuoel.towelette.mixin.layer.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.chunk.ChunkSection;
import virtuoel.towelette.util.LayeredPalettedContainerHolder;

@Mixin(ChunkSection.class)
public abstract class ChunkSectionMixin implements LayeredPalettedContainerHolder
{
	@Inject(require = 0, at = @At("RETURN"), method = "fromPacket(Lnet/minecraft/util/PacketByteBuf;)V")
	public void onFromPacket(PacketByteBuf buffer, CallbackInfo info)
	{
		getPalettedContainerDataMap().values().forEach(data ->
		{
			data.setMiddle(buffer.readShort());
			data.getLeft().fromPacket(buffer);
		});
	}
}
