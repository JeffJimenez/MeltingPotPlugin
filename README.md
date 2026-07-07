# Melting Pot Tracker - RuneLite Plugin for OSRS Blast Furnace

A RuneLite plugin that helps you keep track of the contents of the **Melting Pot** at the Blast Furnace minigame in Old School RuneScape.

## Features
- **Persistent Infobox**: Shows the last known contents of the melting pot in a convenient RuneLite infobox (with tooltip details and timestamp).
- **World Overlay**: Draws a highlight and text label directly on the melting pot object in the game world so you always know where it is and its status.
- **Automatic Tracking**: Captures chat messages related to the melting pot (especially when you use the in-game **Check** option) and updates the display automatically.
- **"Check" Detection**: Detects when you click the "Check" option on the melting pot and temporarily shows "Checking..." status.
- **Configurable**: Toggle infobox and world overlay on/off, change highlight color.

## How to Use
1. **In-game**: Go to the Blast Furnace in Keldagrim.
2. **Check the pot**: Right-click the Melting Pot (large central furnace structure, object ID 9098) and select **Check**. This shows the current ores/coal inside (e.g. how many of each type).
3. **Plugin updates**: The plugin will automatically detect the check action and the resulting chat message(s) describing the contents. The infobox and overlay will update with the information.
4. **View info**:
    - Look at the infobox in the top-right (or wherever you have infoboxes positioned).
    - Hover the infobox for a detailed tooltip with timestamp and formatted contents.
    - In the game world, the melting pot will have an orange (configurable) highlight and text label.

The plugin "remembers" the last checked contents until you check again or reset.

**Tip**: The melting pot can hold up to ~200 ores total. Check it regularly to avoid overfilling or to plan your trips (e.g., how much more coal/ore to add).

## Installation (for development / personal use)
This is provided as source code. RuneLite plugins require the RuneLite client source to build against.

### Recommended: Local Development Setup
1. Clone the official RuneLite repository:
   ```bash
   git clone https://github.com/runelite/runelite.git
   cd runelite
   ```

2. Copy this plugin folder into the plugins directory:
   ```bash
   cp -r /path/to/meltingpottracker runelite-client/src/main/java/net/runelite/client/plugins/
   ```

3. Build and run the client (development mode):
   ```bash
   ./gradlew :runelite-client:run
   ```
   Or use your IDE (IntelliJ recommended) to run `RuneLite.main`.

4. In the running client, enable the plugin via the Plugin Manager (search for "Melting Pot Tracker").

### Alternative: External Plugin (Advanced)
For distribution via Plugin Hub or as a standalone .jar, you would need to:
- Create a proper Gradle build file with dependencies on `runelite-api`, `runelite-client`.
- Implement shading / relocation if needed.
- Submit to https://github.com/runelite/plugin-hub (requires review).

For personal use, the local dev setup above is simplest and most reliable.

## Notes & Limitations
- **Parsing**: The plugin captures relevant chat messages containing "melting pot", "blast furnace", ores, coal, etc. The exact wording of the "Check" response may vary slightly; if it doesn't capture perfectly, the raw message will still be shown. You can improve the `onChatMessage` filter if needed.
- **Shared Furnace**: The melting pot is shared among all players on the world. The contents reflect the global state of unsmelted ores in the furnace.
- **Accuracy**: Contents only update when you (or chat events) trigger a check. Smelting and other players adding ore will change the real state — re-check as needed.
- **Performance**: The world overlay searches the scene each frame for the melting pot object (ID 9098). This is lightweight but could be optimized with event-based caching in a future version.
- Tested conceptually against OSRS mechanics (as of 2026). Game updates may change chat text or object behavior.

## Future Improvements (ideas for you to extend)
- Better regex parsing to extract exact ore counts into a structured list (e.g. table in tooltip).
- Track additions automatically by monitoring conveyor belt deposits + inventory changes (more complex, as furnace is shared).
- Estimate bars producible from current contents + required coal ratios.
- Hotkey to force "reset tracker" or manually edit contents (via config panel or separate UI).
- Integration with the official RuneLite "Blast Furnace" plugin (show combined info).
- Support for the "empty furnace" use case of the Check option.

## Credits
- Built for Jeff (OSRS player & developer).
- Uses standard RuneLite plugin patterns, Overlay, InfoBox, event bus.
- Game data from OSRS Wiki (Melting Pot object ID 9098, Check mechanic).

If you have questions, improvements, or want me to refine the code (e.g. better parsing, caching the GameObject, adding more features), just let me know!

Enjoy efficient Blast Furnace runs! 🔥
