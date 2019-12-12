package virtuoel.towelette.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import virtuoel.towelette.api.LayerData;
import virtuoel.towelette.api.WorldStateLayer;

public class BlockStateLayerWrappedWorld extends WrappedWorld
{
	protected final LayerData<Block, BlockState> layer;
	
	public BlockStateLayerWrappedWorld(World delegate, LayerData<Block, BlockState> layer)
	{
		super(delegate);
		this.layer = layer;
	}
	
	@Override
	public boolean setBlockState(BlockPos blockPos_1, BlockState blockState_1, int int_1)
	{
		return ((WorldStateLayer) delegate).setState(layer, blockPos_1, blockState_1, int_1);
	}
	
	@Override
	public boolean setBlockState(BlockPos blockPos_1, BlockState blockState_1)
	{
		return ((WorldStateLayer) delegate).setState(layer, blockPos_1, blockState_1);
	}
	
	@Override
	public BlockState getBlockState(BlockPos blockPos_1)
	{
		return ((WorldStateLayer) delegate).getState(layer, blockPos_1);
	}
}
