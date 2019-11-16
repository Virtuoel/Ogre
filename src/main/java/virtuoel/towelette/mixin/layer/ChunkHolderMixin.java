package virtuoel.towelette.mixin.layer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Packet;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import virtuoel.towelette.api.StateUpdateableChunkHolder;
import virtuoel.towelette.api.PaletteRegistrar;
import virtuoel.towelette.util.PacketUtils;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin implements StateUpdateableChunkHolder
{
	@Shadow int sectionsNeedingUpdateMask;
	@Shadow int blockUpdateCount;
	@Shadow @Final ChunkPos pos;
	
	@Shadow abstract WorldChunk getWorldChunk();
	@Shadow abstract void sendPacketToPlayersWatching(Packet<?> packet_1, boolean boolean_1);
	
	@Unique final Map<Identifier, MutablePair<short[], Integer>> updateMap = new HashMap<>();
	
	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/util/math/ChunkPos;ILnet/minecraft/world/chunk/light/LightingProvider;Lnet/minecraft/server/world/ChunkHolder$LevelUpdateListener;Lnet/minecraft/server/world/ChunkHolder$PlayersWatchingChunkProvider;)V")
	private void onConstruct(ChunkPos pos, int level, LightingProvider lightingProvider, ChunkHolder.LevelUpdateListener listener, ChunkHolder.PlayersWatchingChunkProvider provider, CallbackInfo info)
	{
		for(final Identifier id : PaletteRegistrar.PALETTES.getIds())
		{
			updateMap.put(id, new MutablePair<short[], Integer>(new short[64], 0));
		}
	}
	
	@Override
	public void markForStateUpdate(Identifier layer, int x, int y, int z)
	{
		if (this.getWorldChunk() != null)
		{
			this.sectionsNeedingUpdateMask |= 1 << (y >> 4);
			
			final MutablePair<short[], Integer> data = updateMap.get(layer);
			
			final short[] positions = data.getLeft();
			int updateCount = data.getRight();
			
			if (updateCount < 64)
			{
				boolean exists = false;
				
				final short pos = (short) (x << 12 | z << 8 | y);
				
				for (int i = 0; i < updateCount; i++)
				{
					if (positions[i] == pos)
					{
						exists = true;
						break;
					}
				}
				
				if(!exists)
				{
					positions[updateCount++] = pos;
					data.setRight(updateCount);
				}
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "flushUpdates(Lnet/minecraft/world/chunk/WorldChunk;)V")
	private void onPreFlushUpdates(WorldChunk chunk, CallbackInfo info)
	{
		for(final Identifier id : PaletteRegistrar.PALETTES.getIds())
		{
			final MutablePair<short[], Integer> data = updateMap.get(id);
			
			if(this.blockUpdateCount == 64)
			{
				data.setRight(0);
				continue;
			}
			
			final short[] positions = data.getLeft();
			final int updateCount = data.getRight();
			
			if (updateCount != 0)
			{
				if (updateCount == 1)
				{
					final int x = (positions[0] >> 12 & 15) + this.pos.x * 16;
					final int y = positions[0] & 255;
					final int z = (positions[0] >> 8 & 15) + this.pos.z * 16;
					final BlockPos pos = new BlockPos(x, y, z);
					this.sendPacketToPlayersWatching(PacketUtils.stateUpdate(PaletteRegistrar.getPaletteData(id), chunk.getWorld(), pos), false);
				}
				else if (updateCount == 64)
				{
					this.blockUpdateCount = 64;
					data.setRight(0);
					continue;
				}
				else
				{
					this.sendPacketToPlayersWatching(PacketUtils.deltaUpdate(PaletteRegistrar.getPaletteData(id), updateCount, positions, chunk), false);
				}
				
				data.setRight(0);
			}
		}
	}
}
