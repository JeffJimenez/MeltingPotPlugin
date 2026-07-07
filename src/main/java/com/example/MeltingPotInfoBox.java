package com.example;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;

public class MeltingPotInfoBox extends InfoBox
{
	private final MeltingPotTrackerPlugin plugin;
	private String contents = "Not at Blast Furnace";
	private Instant lastUpdate = null;
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

	@Inject
	public MeltingPotInfoBox(BufferedImage image, MeltingPotTrackerPlugin plugin, ItemManager itemManager)
	{
		super(image, plugin);
		this.plugin = plugin;
		setPriority(InfoBoxPriority.MED);
	}

	public void updateContents(String newContents, Instant updateTime)
	{
		this.contents = newContents;
		this.lastUpdate = updateTime;
	}

	@Override
	public String getText()
	{
		int total = plugin.getTotalOreCount();
		return total > 0 ? String.valueOf(total) : "Pot";
	}

	@Override
	public Color getTextColor()
	{
		return Color.ORANGE;
	}

	@Override
	public String getTooltip()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><b>Melting Pot Tracker</b><br/>");
		if (lastUpdate != null)
		{
			sb.append("Last updated: ").append(TIME_FORMAT.format(lastUpdate)).append("<br/><br/>");
		}
		sb.append("<b>Contents:</b><br/>");
		sb.append(contents.replace("\n", "<br/>").replaceAll("(?i)(coal|ore)", "<font color='#FFA500'>$1</font>"));
		sb.append("<br/><br/><i>Updated automatically from conveyor belt deposits</i></html>");
		return sb.toString();
	}

	@Override
	public boolean render()
	{
		return true; // Always render if added
	}
}
