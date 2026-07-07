package com.example;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
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
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

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
	private ItemManager itemManager;

	@Inject
	private MeltingPotTrackerOverlay overlay;

	@Getter
	private GameObject meltingPot;

	@Getter
	private GameObject conveyorBelt;

	private final Map<MeltingPotOres, Integer> oreCounts = new EnumMap<>(MeltingPotOres.class);

	@Getter
	private int totalOreCount;

	private String[] contentLines = new String[0];

	public String[] getContentLines()
	{
		return contentLines;
	}

	@Override
	protected void startUp()
	{
		log.info("Melting Pot Tracker started!");
		overlayManager.add(overlay);
		resetOreCounts();
	}

	@Override
	protected void shutDown()
	{
		log.info("Melting Pot Tracker stopped!");
		overlayManager.remove(overlay);
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
			contentLines = new String[0];
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

	public boolean isAtBlastFurnace()
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
		List<String> lines = new ArrayList<>();

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
				lines.add(name + ": " + amount);
			}
		}

		if (!changed && total == totalOreCount)
		{
			return;
		}

		totalOreCount = total;

		if (total == 0)
		{
			contentLines = new String[]{"Empty"};
		}
		else
		{
			contentLines = lines.toArray(new String[0]);
		}

		log.debug("Updated melting pot from varbits: {} total", total);
	}

	private void resetOreCounts()
	{
		oreCounts.clear();
		for (MeltingPotOres ore : MeltingPotOres.values())
		{
			oreCounts.put(ore, 0);
		}
		totalOreCount = 0;
		contentLines = new String[0];
	}
}