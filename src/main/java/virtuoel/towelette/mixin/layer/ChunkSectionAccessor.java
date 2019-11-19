package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;

@Mixin(ChunkSection.class)
public interface ChunkSectionAccessor
{
	@Accessor("palette")
	public static Palette<BlockState> getBlockStatePalette()
	{
		throw new UnsupportedOperationException();
	}
}
