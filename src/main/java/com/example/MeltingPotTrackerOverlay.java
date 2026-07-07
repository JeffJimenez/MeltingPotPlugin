package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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

		Rectangle rect = new Rectangle(canvasLoc.getX() - 30, canvasLoc.getY() - 40, 60, 80);
		java.awt.Polygon poly = new java.awt.Polygon();
		poly.addPoint(rect.x, rect.y);
		poly.addPoint(rect.x + rect.width, rect.y);
		poly.addPoint(rect.x + rect.width, rect.y + rect.height);
		poly.addPoint(rect.x, rect.y + rect.height);
		OverlayUtil.renderPolygon(graphics, poly, config.highlightColor());

		String text = "Melting Pot";
		String subText = plugin.getLastContentsShort();
		if (subText == null || subText.isEmpty())
		{
			subText = plugin.getTotalOreCount() + " ore";
		}

		OverlayUtil.renderTextLocation(graphics,
			new Point(canvasLoc.getX(), canvasLoc.getY() - 50),
			text, Color.WHITE);

		OverlayUtil.renderTextLocation(graphics,
			new Point(canvasLoc.getX(), canvasLoc.getY() - 35),
			subText, Color.YELLOW);

		return null;
	}
}