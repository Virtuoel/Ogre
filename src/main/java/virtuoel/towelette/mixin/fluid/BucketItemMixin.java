package virtuoel.towelette.mixin.fluid;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.advancement.criterion.Criterions;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.BaseFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.towelette.api.LayerRegistrar;
import virtuoel.towelette.api.ModifiableWorldStateLayer;
import virtuoel.towelette.util.DummyDrainableBlock;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin
{
	@Shadow Fluid fluid;
	
	@Redirect(method = "use", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
	private BlockState onUseGetBlockStateProxy(World obj, BlockPos blockPos)
	{
		return DummyDrainableBlock.INSTANCE.getDefaultState();
	}
	
	@Shadow abstract boolean placeFluid(@Nullable PlayerEntity playerEntity, World world, BlockPos blockPos, @Nullable BlockHitResult blockHitResult);
	@Shadow abstract void onEmptied(World world, ItemStack itemStack, BlockPos blockPos);
	@Shadow abstract ItemStack getEmptiedStack(ItemStack itemStack, PlayerEntity playerEntity);
	
	@Inject(method = "use", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true, at = @At(value = "INVOKE", shift = Shift.AFTER, ordinal = 1, target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
	private void onUse(World world, PlayerEntity playerEntity, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info, ItemStack itemStack, BlockHitResult blockHitResult, BlockPos blockPos, BlockPos offsetPos)
	{
		final Vec3d hitPos = blockHitResult.getPos();
		final BlockPos hitBlockPos = blockHitResult.getBlockPos();
		final double x = hitPos.x - (double) hitBlockPos.getX();
		final double y = hitPos.y - (double) hitBlockPos.getY();
		final double z = hitPos.z - (double) hitBlockPos.getZ();
		final boolean insideCube = 0.0D < x && x < 1.0D && 0.0D < y && y < 1.0D && 0.0D < z && z < 1.0D;
		final BlockPos placedPos = insideCube ? blockPos : offsetPos;
		if (placeFluid(playerEntity, world, placedPos, blockHitResult))
		{
			onEmptied(world, itemStack, placedPos);
			if (playerEntity instanceof ServerPlayerEntity)
			{
				Criterions.PLACED_BLOCK.trigger((ServerPlayerEntity) playerEntity, placedPos, itemStack);
			}
			
			playerEntity.incrementStat(Stats.USED.getOrCreateStat((BucketItem) (Object) this));
			info.setReturnValue(TypedActionResult.success(getEmptiedStack(itemStack, playerEntity)));
		}
		else
		{
			info.setReturnValue(TypedActionResult.fail(itemStack));
		}
	}
	
	@Inject(at = @At(value = "RETURN", ordinal = 2), method = "use", locals = LocalCapture.CAPTURE_FAILSOFT)
	private void onUse(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info, ItemStack held, BlockHitResult hitResult, BlockPos pos, BlockPos offsetPos, BlockState state, Fluid drained)
	{
		final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
		w.setState(LayerRegistrar.FLUID_LAYER, pos, Fluids.EMPTY.getDefaultState(), 11);
	}
	
	@ModifyVariable(method = "placeFluid", ordinal = 0, at = @At(value = "INVOKE_ASSIGN"))
	private boolean modifyCanBucketPlace(boolean orig, @Nullable PlayerEntity playerEntity, World world, BlockPos blockPos, @Nullable BlockHitResult blockHitResult)
	{
		return orig || !world.getBlockState(blockPos).isFullOpaque(world, blockPos);
	}
	
	@ModifyVariable(method = "placeFluid", ordinal = 0, at = @At(value = "INVOKE_ASSIGN"))
	private Material modifyGetMaterial(Material orig)
	{
		return Material.WATER;
	}
	
	@Inject(at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/item/BucketItem;playEmptyingSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;)V"), method = "placeFluid")
	private void onPlaceFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult result, CallbackInfoReturnable<Boolean> info)
	{
		final ModifiableWorldStateLayer w = ((ModifiableWorldStateLayer) world);
		final FluidState fluidState = this.fluid instanceof BaseFluid ? ((BaseFluid) this.fluid).getStill(false) : this.fluid.getDefaultState();
		if(fluid != Fluids.WATER)
		{
			final BlockState state = world.getBlockState(pos);
			if(state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED))
			{
				world.setBlockState(pos, state.with(Properties.WATERLOGGED, false));
			}
		}
		
		w.setState(LayerRegistrar.FLUID_LAYER, pos, fluidState, 11);
	}
}
