package virtuoel.towelette.mixin.layer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.fluid.Fluid;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.TagContainer;
import virtuoel.towelette.command.arguments.FluidArgumentParser;

@Mixin(FluidTags.class)
public class FluidTagsMixin
{
	@Inject(at = @At("RETURN"), method = "setContainer")
	private static void onSetContainer(TagContainer<Fluid> tagContainer_1, CallbackInfo info)
	{
		FluidArgumentParser.fluidTagContainer = tagContainer_1;
	}
}
