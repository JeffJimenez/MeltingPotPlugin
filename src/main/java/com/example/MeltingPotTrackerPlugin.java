package com.example;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@Slf4j
@PluginDescriptor(
	name = "Melting Pot Tracker",
	description = "Tracks melting pot contents at the Blast Furnace using conveyor belt deposit varbits.",
	tags = {"osrs", "blast", "furnace", "smithing", "minigame", "melting", "pot", "conveyor"}
)
public class MeltingPotTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private MeltingPotTrackerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private MeltingPotTrackerOverlay overlay;

	private MeltingPotInfoBox infoBox;

	@Getter
	private GameObject meltingPot;

	@Getter
	private GameObject conveyorBelt;

	private final Map<MeltingPotOres, Integer> oreCounts = new EnumMap<>(MeltingPotOres.class);

	@Getter
	private String lastContents = "Not at Blast Furnace";

	@Getter
	private String lastContentsShort = "";

	@Getter
	private int totalOreCount;

	private Instant lastUpdated = null;

	@Override
	protected void startUp()
	{
		log.info("Melting Pot Tracker started!");
		overlayManager.add(overlay);
		resetOreCounts();

		if (config.showInfobox())
		{
			BufferedImage icon = itemManager.getImage(ItemID.COAL);
			if (icon == null)
			{
				icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
			}
			infoBox = new MeltingPotInfoBox(icon, this, itemManager);
			infoBoxManager.addInfoBox(infoBox);
			updateInfoBox();
		}
	}

	@Override
	protected void shutDown()
	{
		log.info("Melting Pot Tracker stopped!");
		overlayManager.remove(overlay);
		if (infoBox != null)
		{
			infoBoxManager.removeInfoBox(infoBox);
			infoBox = null;
		}
		meltingPot = null;
		conveyorBelt = null;
	}

	@Provides
	MeltingPotTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MeltingPotTrackerConfig.class);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		switch (gameObject.getId())
		{
			case ObjectID.BLAST_FURNACE_CHIMNEY:
				meltingPot = gameObject;
				refreshContentsFromVarbits();
				break;
			case ObjectID.BLAST_FURNACE_CONVEYER_BELT_CLICKABLE:
				conveyorBelt = gameObject;
				refreshContentsFromVarbits();
				break;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject gameObject = event.getGameObject();
		switch (gameObject.getId())
		{
			case ObjectID.BLAST_FURNACE_CHIMNEY:
				meltingPot = null;
				break;
			case ObjectID.BLAST_FURNACE_CONVEYER_BELT_CLICKABLE:
				conveyorBelt = null;
				break;
		}

		if (!isAtBlastFurnace())
		{
			lastContents = "Not at Blast Furnace";
			lastContentsShort = "";
			updateInfoBox();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			meltingPot = null;
			conveyorBelt = null;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!isAtBlastFurnace())
		{
			return;
		}

		for (MeltingPotOres ore : MeltingPotOres.values())
		{
			if (event.getVarbitId() == ore.getVarbit())
			{
				refreshContentsFromVarbits();
				return;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Fallback poll in case a varbit change is missed
		if (isAtBlastFurnace() && client.getTickCount() % 10 == 0)
		{
			refreshContentsFromVarbits();
		}
	}

	private boolean isAtBlastFurnace()
	{
		return conveyorBelt != null || meltingPot != null;
	}

	private void refreshContentsFromVarbits()
	{
		if (!isAtBlastFurnace())
		{
			return;
		}

		int total = 0;
		boolean changed = false;
		StringBuilder full = new StringBuilder();
		StringBuilder shortSummary = new StringBuilder();

		for (MeltingPotOres ore : MeltingPotOres.values())
		{
			int amount = client.getVarbitValue(ore.getVarbit());
			Integer previous = oreCounts.put(ore, amount);
			if (previous == null || previous != amount)
			{
				changed = true;
			}

			if (amount > 0)
			{
				total += amount;
				String name = itemManager.getItemComposition(ore.getItemId()).getName();
				full.append(name).append(": ").append(amount).append('\n');
				if (shortSummary.length() > 0)
				{
					shortSummary.append(", ");
				}
				shortSummary.append(name).append(": ").append(amount);
			}
		}

		if (!changed && total == totalOreCount)
		{
			return;
		}

		totalOreCount = total;
		lastUpdated = Instant.now();

		if (total == 0)
		{
			lastContents = "Melting pot is empty";
			lastContentsShort = "Empty";
		}
		else
		{
			full.append("\nTotal: ").append(total);
			lastContents = full.toString().trim();
			lastContentsShort = shortSummary.length() > 60
				? shortSummary.substring(0, 57) + "..."
				: shortSummary.toString();
		}

		updateInfoBox();
		log.debug("Updated melting pot from varbits: {} total", total);
	}

	private void updateInfoBox()
	{
		if (infoBox != null)
		{
			infoBox.updateContents(lastContents, lastUpdated);
		}
	}

	private void resetOreCounts()
	{
		oreCounts.clear();
		for (MeltingPotOres ore : MeltingPotOres.values())
		{
			oreCounts.put(ore, 0);
		}
		totalOreCount = 0;
	}

	public void resetTracker()
	{
		resetOreCounts();
		lastContents = isAtBlastFurnace() ? "Melting pot is empty" : "Not at Blast Furnace";
		lastContentsShort = isAtBlastFurnace() ? "Empty" : "";
		lastUpdated = null;
		updateInfoBox();
	}
}