package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import virtuoel.towelette.api.StateUpdateableChunkHolder;
import virtuoel.towelette.api.StateUpdateableChunkManager;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin implements StateUpdateableChunkManager
{
	@Shadow abstract ChunkHolder getChunkHolder(long pos);
	
	@Override
	public void onStateUpdate(Identifier layer, BlockPos pos)
	{
		ChunkHolder chunkHolder = this.getChunkHolder(ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4));
		if (chunkHolder != null)
		{
			((StateUpdateableChunkHolder) chunkHolder).markForStateUpdate(layer, pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		}
	}
}
