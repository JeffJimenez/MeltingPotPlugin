package com.example;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.time.Instant;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
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
	description = "Tracks and displays the contents of the Blast Furnace melting pot. Use the in-game 'Check' option on the melting pot to update the tracked contents.",
	tags = {"osrs", "blast", "furnace", "smithing", "minigame", "melting", "pot"}
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
	private String lastContents = "Unknown - Use 'Check' option on melting pot";

	@Getter
	private String lastContentsShort = "";

	private Instant lastChecked = null;

	@Override
	protected void startUp()
	{
		log.info("Melting Pot Tracker started!");
		overlayManager.add(overlay);

		if (config.showInfobox())
		{
			BufferedImage icon = itemManager.getImage(net.runelite.api.ItemID.COAL);
			if (icon == null)
			{
				// Fallback, though should not happen
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
	}

	@Provides
	MeltingPotTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MeltingPotTrackerConfig.class);
	}

	private void updateInfoBox()
	{
		if (infoBox != null)
		{
			infoBox.updateContents(lastContents, lastChecked);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		String msg = event.getMessage();
		String lowerMsg = msg.toLowerCase();

		// Capture any message that seems related to melting pot or blast furnace contents
		if (lowerMsg.contains("melting pot") || 
			(lowerMsg.contains("blast furnace") && (lowerMsg.contains("contains") || lowerMsg.contains("ore") || lowerMsg.contains("coal") || lowerMsg.contains("added") || lowerMsg.contains("check"))))
		{
			lastContents = msg;
			lastChecked = Instant.now();
			
			// Create a short version for overlay
			if (msg.length() > 60)
			{
				lastContentsShort = msg.substring(0, 57) + "...";
			}
			else
			{
				lastContentsShort = msg;
			}

			updateInfoBox();
			log.debug("Updated melting pot contents from chat: {}", msg);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		// Detect when player clicks "Check" on the melting pot (object ID 9098)
		if (event.getMenuAction() == MenuAction.GAME_OBJECT_FIRST_OPTION ||
			event.getMenuAction() == MenuAction.GAME_OBJECT_SECOND_OPTION ||
			event.getMenuAction() == MenuAction.GAME_OBJECT_THIRD_OPTION ||
			event.getMenuAction() == MenuAction.GAME_OBJECT_FOURTH_OPTION ||
			event.getMenuAction() == MenuAction.GAME_OBJECT_FIFTH_OPTION)
		{
			if (event.getId() == 9098 && "Check".equalsIgnoreCase(event.getMenuOption()))
			{
				lastContents = "Checking melting pot... (contents will update shortly)";
				lastContentsShort = "Checking...";
				lastChecked = Instant.now();
				updateInfoBox();
				log.debug("Player initiated Check on melting pot");
			}
		}
	}

	// Optional: reset method if needed, exposed for future config button or hotkey
	public void resetTracker()
	{
		lastContents = "Unknown - Use 'Check' option on melting pot";
		lastContentsShort = "";
		lastChecked = null;
		updateInfoBox();
	}
}
