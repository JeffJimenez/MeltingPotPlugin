package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(MeltingPotTrackerConfig.GROUP)
public interface MeltingPotTrackerConfig extends Config
{
	String GROUP = "meltingpottracker";

	@ConfigItem(
		keyName = "showInfobox",
		name = "Show infobox",
		description = "Displays an infobox with the last known melting pot contents.",
		position = 0
	)
	default boolean showInfobox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showWorldOverlay",
		name = "Show world overlay",
		description = "Draws text on the melting pot object in the game world.",
		position = 1
	)
	default boolean showWorldOverlay()
	{
		return true;
	}
}
