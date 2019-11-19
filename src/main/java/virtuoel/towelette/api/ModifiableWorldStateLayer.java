package virtuoel.towelette.api;

import net.minecraft.state.PropertyContainer;
import net.minecraft.util.math.BlockPos;

public interface ModifiableWorldStateLayer extends BlockViewStateLayer
{
	default <O, S extends PropertyContainer<S>> boolean setState(LayerData<O, S> layer, BlockPos pos, S state)
	{
		return setState(layer, pos, state, 3);
	}
	
	<O, S extends PropertyContainer<S>> boolean setState(LayerData<O, S> layer, BlockPos pos, S state, int flags);
	
	default <O, S extends PropertyContainer<S>> boolean clearState(LayerData<O, S> layer, BlockPos pos, boolean flag)
	{
		return setState(layer, pos, layer.getPalette().getByIndex(-1), 3 | (flag ? 64 : 0));
	}
}
