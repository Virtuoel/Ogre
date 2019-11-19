package virtuoel.towelette.api;

import java.util.Collection;

import net.fabricmc.loader.api.FabricLoader;

public interface ToweletteApi
{
	public static final String MOD_ID = "towelette";
	
	public static final Collection<ToweletteApi> ENTRYPOINTS = FabricLoader.getInstance().getEntrypoints(MOD_ID, ToweletteApi.class);
}
