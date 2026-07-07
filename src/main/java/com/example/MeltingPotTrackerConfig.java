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
		description = "Draws text and highlight on the melting pot object in the game world.",
		position = 1
	)
	default boolean showWorldOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight color",
		description = "Color for the melting pot highlight.",
		position = 2
	)
	default java.awt.Color highlightColor()
	{
		return java.awt.Color.ORANGE;
	}
}
