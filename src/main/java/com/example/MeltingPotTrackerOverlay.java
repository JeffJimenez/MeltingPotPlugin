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
import net.runelite.api.Tile;
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

		GameObject meltingPot = findMeltingPot();
		if (meltingPot == null)
		{
			return null;
		}

		// Get canvas location for the object
		Point canvasLoc = Perspective.localToCanvas(client, meltingPot.getLocalLocation(), client.getPlane());
		if (canvasLoc == null)
		{
			return null;
		}

		// Draw highlight polygon around the object (simple bounding box approx)
		// For better, could use object clickbox but simple rectangle for now
		Rectangle rect = new Rectangle(canvasLoc.getX() - 30, canvasLoc.getY() - 40, 60, 80);
		java.awt.Polygon poly = new java.awt.Polygon();
		poly.addPoint(rect.x, rect.y);
		poly.addPoint(rect.x + rect.width, rect.y);
		poly.addPoint(rect.x + rect.width, rect.y + rect.height);
		poly.addPoint(rect.x, rect.y + rect.height);
		OverlayUtil.renderPolygon(graphics, poly, config.highlightColor());

		// Draw text
		String text = "Melting Pot";
		String subText = plugin.getLastContentsShort();
		Color textColor = Color.WHITE;

		OverlayUtil.renderTextLocation(graphics, 
			new Point(canvasLoc.getX(), canvasLoc.getY() - 50), 
			text, textColor);

		if (subText != null && !subText.isEmpty())
		{
			OverlayUtil.renderTextLocation(graphics, 
				new Point(canvasLoc.getX(), canvasLoc.getY() - 35), 
				subText, Color.YELLOW);
		}

		return null;
	}

	private GameObject findMeltingPot()
	{
		if (client.getScene() == null || client.getScene().getTiles() == null)
		{
			return null;
		}

		Tile[][][] tiles = client.getScene().getTiles();
		int plane = client.getPlane();

		for (int x = 0; x < tiles[plane].length; x++)
		{
			for (int y = 0; y < tiles[plane][x].length; y++)
			{
				Tile tile = tiles[plane][x][y];
				if (tile == null)
				{
					continue;
				}

				for (GameObject obj : tile.getGameObjects())
				{
					if (obj != null && obj.getId() == 9098)
					{
						return obj;
					}
				}
			}
		}
		return null;
	}
}
