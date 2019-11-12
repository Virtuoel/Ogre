package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Packet;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.api.FluidUpdateableChunkHolder;
import virtuoel.towelette.util.PacketUtils;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin implements FluidUpdateableChunkHolder
{
	private short[] fluidUpdatePositions = new short[64];
	private int fluidUpdateCount;
	
	@Shadow int sectionsNeedingUpdateMask;
	@Shadow int blockUpdateCount;
	@Shadow @Final ChunkPos pos;
	
	@Shadow abstract WorldChunk getWorldChunk();
	@Shadow abstract void sendPacketToPlayersWatching(Packet<?> packet_1, boolean boolean_1);
	
	@Override
	public void markForFluidUpdate(int x, int y, int z)
	{
		if (this.getWorldChunk() != null)
		{
			this.sectionsNeedingUpdateMask |= 1 << (y >> 4);
			if (this.fluidUpdateCount < 64)
			{
				final short pos = (short) (x << 12 | z << 8 | y);
				
				for (int i = 0; i < this.fluidUpdateCount; i++)
				{
					if (this.fluidUpdatePositions[i] == pos)
					{
						return;
					}
				}
				
				this.fluidUpdatePositions[this.fluidUpdateCount++] = pos;
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "flushUpdates(Lnet/minecraft/world/chunk/WorldChunk;)V")
	private void onPreFlushUpdates(WorldChunk chunk, CallbackInfo info)
	{
		if (this.fluidUpdateCount != 0)
		{
			if (this.fluidUpdateCount == 1)
			{
				final int x = (this.fluidUpdatePositions[0] >> 12 & 15) + this.pos.x * 16;
				final int y = this.fluidUpdatePositions[0] & 255;
				final int z = (this.fluidUpdatePositions[0] >> 8 & 15) + this.pos.z * 16;
				final BlockPos pos = new BlockPos(x, y, z);
				this.sendPacketToPlayersWatching(PacketUtils.fluidUpdate(chunk.getWorld(), pos), false);
			}
			else if (this.fluidUpdateCount == 64)
			{
				this.blockUpdateCount = 64;
			}
			else
			{
				this.sendPacketToPlayersWatching(PacketUtils.deltaUpdate(this.fluidUpdateCount, this.fluidUpdatePositions, chunk), false);
			}
			
			this.fluidUpdateCount = 0;
		}
	}
}
