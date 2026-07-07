package com.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.annotations.Varbit;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;

@AllArgsConstructor
@Getter
enum MeltingPotOres
{
	COPPER_ORE(VarbitID.BLAST_FURNACE_COPPER_ORE, ItemID.COPPER_ORE),
	TIN_ORE(VarbitID.BLAST_FURNACE_TIN_ORE, ItemID.TIN_ORE),
	IRON_ORE(VarbitID.BLAST_FURNACE_IRON_ORE, ItemID.IRON_ORE),
	COAL(VarbitID.BLAST_FURNACE_COAL, ItemID.COAL),
	MITHRIL_ORE(VarbitID.BLAST_FURNACE_MITHRIL_ORE, ItemID.MITHRIL_ORE),
	ADAMANTITE_ORE(VarbitID.BLAST_FURNACE_ADAMANTITE_ORE, ItemID.ADAMANTITE_ORE),
	RUNITE_ORE(VarbitID.BLAST_FURNACE_RUNITE_ORE, ItemID.RUNITE_ORE),
	SILVER_ORE(VarbitID.BLAST_FURNACE_SILVER_ORE, ItemID.SILVER_ORE),
	GOLD_ORE(VarbitID.BLAST_FURNACE_GOLD_ORE, ItemID.GOLD_ORE),
	LEAD_ORE(VarbitID.BLAST_FURNACE_LEAD_ORE, ItemID.LEAD_ORE),
	NICKEL_ORE(VarbitID.BLAST_FURNACE_NICKEL_ORE, ItemID.NICKEL_ORE);

	@Getter(onMethod_ = {@Varbit})
	private final int varbit;
	private final int itemId;
}