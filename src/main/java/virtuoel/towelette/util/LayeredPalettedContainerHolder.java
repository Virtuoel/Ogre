package virtuoel.towelette.util;

import java.util.Map;

import org.apache.commons.lang3.tuple.MutableTriple;

import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.PalettedContainer;

public interface LayeredPalettedContainerHolder
{
	Map<Identifier, MutableTriple<PalettedContainer<?>, Short, Short>> getPalettedContainerDataMap();
}
