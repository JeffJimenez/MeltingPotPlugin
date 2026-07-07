package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class MeltingPotTrackerOverlay extends Overlay
{
	private static final int LINE_HEIGHT = 15;

	private final Client client;
	private final MeltingPotTrackerConfig config;
	private final MeltingPotTrackerPlugin plugin;

	@Inject
	public MeltingPotTrackerOverlay(Client client, MeltingPotTrackerConfig config, MeltingPotTrackerPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showWorldOverlay())
		{
			return null;
		}

		GameObject meltingPot = plugin.getMeltingPot();
		if (meltingPot == null)
		{
			return null;
		}

		Point canvasLoc = Perspective.localToCanvas(client, meltingPot.getLocalLocation(), client.getPlane());
		if (canvasLoc == null)
		{
			return null;
		}

		int y = canvasLoc.getY() - 50;

		OverlayUtil.renderTextLocation(graphics,
			new Point(canvasLoc.getX(), y),
			"Melting Pot", Color.WHITE);

		y -= LINE_HEIGHT;

		for (String line : plugin.getContentLines())
		{
			OverlayUtil.renderTextLocation(graphics,
				new Point(canvasLoc.getX(), y),
				line, Color.YELLOW);
			y -= LINE_HEIGHT;
		}

		return null;
	}
}