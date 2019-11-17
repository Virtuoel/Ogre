package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public interface ModifiableWorldStateLayer<O, S extends PropertyContainer<S>> extends BlockViewStateLayer<S>
{
	default boolean setState(Identifier layer, BlockPos pos, S state)
	{
		return setState(layer, pos, state, 3);
	}
	
	boolean setState(Identifier layer, BlockPos pos, S state, int flags);
	
	default boolean clearState(Identifier layer, BlockPos pos, boolean flag)
	{
		return setState(layer, pos, PaletteRegistrar.<O, S>getPaletteData(layer).getPalette().getByIndex(-1), 3 | (flag ? 64 : 0));
	}
}
