package virtuoel.towelette.mixin.layer;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.state.State;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import virtuoel.towelette.api.ChunkSectionStateLayer;
import virtuoel.towelette.api.ChunkStateLayer;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.LayerRegistrar;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements ChunkStateLayer
{
	@Override
	public <O, S extends State<S>> S getState(LayerData<O, S> layer, int x, int y, int z)
	{
		final WorldChunk self = (WorldChunk) (Object) this;
		
		final ChunkSection[] sections = self.getSectionArray();
		
		try
		{
			if (y >= 0 && y >> 4 < sections.length)
			{
				final ChunkSection section = sections[y >> 4];
				if (!ChunkSection.isEmpty(section))
				{
					return ((ChunkSectionStateLayer) section).getState(layer, x & 15, y & 15, z & 15);
				}
			}
			
			return layer.getEmptyState();
		}
		catch (Throwable t)
		{
			final CrashReport report = CrashReport.create(t, "Getting state from layer " + LayerRegistrar.LAYERS.getId(layer).toString());
			final CrashReportSection section = report.addElement("State being got");
			section.add("Location", () ->
			{
				return CrashReportSection.createPositionString(x, y, z);
			});
			throw new CrashException(report);
		}
	}
	
	@Override
	@Nullable
	public <O, S extends State<S>> S setState(LayerData<O, S> layer, BlockPos pos, S state, boolean pushed)
	{
		final WorldChunk self = (WorldChunk) (Object) this;
		final World world = self.getWorld();
		
		final int x = pos.getX() & 15;
		final int y = pos.getY();
		final int z = pos.getZ() & 15;
		
		final ChunkSection[] sections = self.getSectionArray();
		
		ChunkSection section = sections[y >> 4];
		if (section == WorldChunk.EMPTY_SECTION)
		{
			if (layer.isEmpty(state))
			{
				return null;
			}
			
			section = new ChunkSection(y >> 4 << 4);
			sections[y >> 4] = section;
		}
		
		final ChunkSectionStateLayer s = ((ChunkSectionStateLayer) section);
		
		final boolean wasEmpty = section.isEmpty();
		final S oldState = s.setState(layer, x, y & 15, z, state);
		if (oldState == state)
		{
			return null;
		}
		else
		{
			final O entry = layer.getOwner(state);
			layer.trackHeightmapUpdate(self, x, y, z, state);
			final boolean isEmpty = section.isEmpty();
			if (wasEmpty != isEmpty)
			{
				world.getChunkManager().getLightingProvider().updateSectionStatus(pos, isEmpty);
			}
			
			layer.onStateRemoved(oldState, world, pos, state, pushed);
			
			if (layer.getOwner(s.getState(layer, x, y & 15, z)) != entry)
			{
				return null;
			}
			else
			{
				layer.onStateAdded(state, world, pos, oldState, pushed);
				
				self.setShouldSave(true);
				return oldState;
			}
		}
	}
}
