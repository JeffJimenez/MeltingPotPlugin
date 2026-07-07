package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(MeltingPotTrackerConfig.GROUP)
public interface MeltingPotTrackerConfig extends Config
{
	String GROUP = "meltingpottracker";

	@ConfigItem(
		keyName = "showWorldOverlay",
		name = "Show world overlay",
		description = "Draws text on the melting pot object in the game world.",
		position = 0
	)
	default boolean showWorldOverlay()
	{
		return true;
	}
}