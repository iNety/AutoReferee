package org.mctourney.AutoReferee;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.DefaultedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.material.Colorable;
import org.mctourney.AutoReferee.AutoRefMatch.TranscriptEvent;
import org.mctourney.AutoReferee.util.BlockData;
import org.mctourney.AutoReferee.util.BlockVector3;
import org.mctourney.AutoReferee.util.ColorConverter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ReportGenerator
{
	public static String generate(AutoRefMatch match)
	{
		String htm, css, js, images = "";
		try
		{
			htm = getResourceString("webstats/report.htm");
			css = getResourceString("webstats/report.css");
			js  = getResourceString("webstats/report.js" );
			
			images += getResourceString("webstats/image-block.css");
		//	images += getResourceString("webstats/image-items.css");
		}
		catch (IOException e) { return null; }
		
		StringWriter transcript = new StringWriter();
		TranscriptEvent endEvent = null;
		for (TranscriptEvent e : match.getTranscript())
		{
			transcript.write(transcriptEventHTML(e));
			if (e.getType() != TranscriptEvent.EventType.MATCH_END) endEvent = e;
		}
		
		AutoRefTeam win = match.getWinningTeam();
		String winningTeam = (win == null) ? "??" : 
			String.format("<span class='team team-%s'>%s</span>",
				win.getTag(), ChatColor.stripColor(win.getName()));
		
		return (htm
			// base files get replaced first
			.replaceAll("#base-css#", css.replaceAll("\\s+", " ") + images)
			.replaceAll("#base-js#", js)
			
			// followed by the team, player, and block styles
			.replaceAll("#team-css#", getTeamStyles(match).replaceAll("\\s+", " "))
			.replaceAll("#plyr-css#", getPlayerStyles(match).replaceAll("\\s+", " "))
			.replaceAll("#blok-css#", getBlockStyles(match).replaceAll("\\s+", " "))
			
			// then match and map names
			.replaceAll("#title#", match.getMatchName())
			.replaceAll("#map#", match.getMapName())
			
			// date and length of match
			.replaceAll("#date#", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL).format(new Date()))
			.replaceAll("#length#", endEvent == null ? "??" : endEvent.getTimestamp())
			
			// team information (all teams, and winning team)
			.replaceAll("#teams#", getTeamList(match))
			.replaceAll("#winners#", winningTeam)
			
			// and last, throw in the transcript and stats
			.replaceAll("#transcript#", transcript.toString())
			.replaceAll("#plyr-stats#", getPlayerStats(match))
		);
	}

	// helper method
	private static String getResourceString(String path) throws IOException
	{
		StringWriter buffer = new StringWriter();
		IOUtils.copy(AutoReferee.getInstance().getResource(path), buffer);
		return buffer.toString();
	}
	
	// generate player.css
	private static String getPlayerStyles(AutoRefMatch match)
	{
		StringWriter css = new StringWriter();
		for (AutoRefPlayer apl : match.getPlayers())
			css.write(String.format(".player-%s:before { background-image: url(http://minotar.net/avatar/%s/16.png); }\n", 
				apl.getTag(), apl.getPlayerName()));
		return css.toString();
	}
	
	// generate team.css
	private static String getTeamStyles(AutoRefMatch match)
	{
		StringWriter css = new StringWriter();
		for (AutoRefTeam team : match.getTeams())
			css.write(String.format(".team-%s { color: %s; }\n", 
				team.getTag(), ColorConverter.chatToHex(team.getColor())));
		return css.toString();
	}
	
	private static Map<BlockData, Integer> terrain_png = Maps.newHashMap();
	private static int terrain_png_size = 16;
	
	static
	{
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.WHITE.getData()), 4 * 16 + 0);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.BLACK.getData()), 7 * 16 + 1);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.GRAY.getData()), 7 * 16 + 2);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.RED.getData()), 8 * 16 + 1);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.PINK.getData()), 8 * 16 + 2);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.GREEN.getData()), 9 * 16 + 1);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.LIME.getData()), 9 * 16 + 2);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.BROWN.getData()), 10 * 16 + 1);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.YELLOW.getData()), 10 * 16 + 2);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.BLUE.getData()), 11 * 16 + 1);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.LIGHT_BLUE.getData()), 11 * 16 + 2);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.PURPLE.getData()), 12 * 16 + 1);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.MAGENTA.getData()), 12 * 16 + 2);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.CYAN.getData()), 13 * 16 + 1);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.ORANGE.getData()), 13 * 16 + 2);
		terrain_png.put(new BlockData(Material.WOOL, DyeColor.SILVER.getData()), 14 * 16 + 1);
	}
	
	private static Map<Material, Integer> items_png = Maps.newHashMap();
	private static int items_png_size = 16;
	
	static
	{
		items_png.put(Material.LEATHER_HELMET, 0 * 16 + 0);
		items_png.put(Material.LEATHER_CHESTPLATE, 1 * 16 + 0);
		items_png.put(Material.LEATHER_LEGGINGS, 2 * 16 + 0);
		items_png.put(Material.LEATHER_BOOTS, 3 * 16 + 0);
		items_png.put(Material.CHAINMAIL_HELMET, 0 * 16 + 1);
		items_png.put(Material.CHAINMAIL_CHESTPLATE, 1 * 16 + 1);
		items_png.put(Material.CHAINMAIL_LEGGINGS, 2 * 16 + 1);
		items_png.put(Material.CHAINMAIL_BOOTS, 3 * 16 + 1);
		items_png.put(Material.IRON_HELMET, 0 * 16 + 2);
		items_png.put(Material.IRON_CHESTPLATE, 1 * 16 + 2);
		items_png.put(Material.IRON_LEGGINGS, 2 * 16 + 2);
		items_png.put(Material.IRON_BOOTS, 3 * 16 + 2);
		items_png.put(Material.DIAMOND_HELMET, 0 * 16 + 3);
		items_png.put(Material.DIAMOND_CHESTPLATE, 1 * 16 + 3);
		items_png.put(Material.DIAMOND_LEGGINGS, 2 * 16 + 3);
		items_png.put(Material.DIAMOND_BOOTS, 3 * 16 + 3);
		items_png.put(Material.GOLD_HELMET, 0 * 16 + 4);
		items_png.put(Material.GOLD_CHESTPLATE, 1 * 16 + 4);
		items_png.put(Material.GOLD_LEGGINGS, 2 * 16 + 4);
		items_png.put(Material.GOLD_BOOTS, 3 * 16 + 4);

		items_png.put(Material.WOOD_SWORD, 4 * 16 + 0);
		items_png.put(Material.WOOD_SPADE, 5 * 16 + 0);
		items_png.put(Material.WOOD_PICKAXE, 6 * 16 + 0);
		items_png.put(Material.WOOD_AXE, 7 * 16 + 0);
		items_png.put(Material.WOOD_HOE, 8 * 16 + 0);
		items_png.put(Material.STONE_SWORD, 4 * 16 + 1);
		items_png.put(Material.STONE_SPADE, 5 * 16 + 1);
		items_png.put(Material.STONE_PICKAXE, 6 * 16 + 1);
		items_png.put(Material.STONE_AXE, 7 * 16 + 1);
		items_png.put(Material.STONE_HOE, 8 * 16 + 1);
		items_png.put(Material.IRON_SWORD, 4 * 16 + 2);
		items_png.put(Material.IRON_SPADE, 5 * 16 + 2);
		items_png.put(Material.IRON_PICKAXE, 6 * 16 + 2);
		items_png.put(Material.IRON_AXE, 7 * 16 + 2);
		items_png.put(Material.IRON_HOE, 8 * 16 + 2);
		items_png.put(Material.DIAMOND_SWORD, 4 * 16 + 3);
		items_png.put(Material.DIAMOND_SPADE, 5 * 16 + 3);
		items_png.put(Material.DIAMOND_PICKAXE, 6 * 16 + 3);
		items_png.put(Material.DIAMOND_AXE, 7 * 16 + 3);
		items_png.put(Material.DIAMOND_HOE, 8 * 16 + 3);
		items_png.put(Material.GOLD_SWORD, 4 * 16 + 4);
		items_png.put(Material.GOLD_SPADE, 5 * 16 + 4);
		items_png.put(Material.GOLD_PICKAXE, 6 * 16 + 4);
		items_png.put(Material.GOLD_AXE, 7 * 16 + 4);
		items_png.put(Material.GOLD_HOE, 8 * 16 + 4);

		items_png.put(Material.BOW, 6 * 16 + 5);
		items_png.put(Material.POTION, 9 * 16 + 10);
	}
	
	// generate block.css
	private static String getBlockStyles(AutoRefMatch match)
	{
		Set<BlockData> blocks = Sets.newHashSet();
		for (AutoRefTeam team : match.getTeams())
			blocks.addAll(team.winConditions.values());
		
		StringWriter css = new StringWriter();
		for (BlockData bd : blocks)
		{
			Integer x = terrain_png.get(bd);
			
			String selector = String.format(".block.mat-%d.data-%d", 
				bd.getMaterial().getId(), (int) bd.getData());
			css.write(selector + ":before ");
			
			if (x == null) css.write("{ display: none; }\n");
			else css.write(String.format("{ background-position: -%dpx -%dpx; }\n",
				terrain_png_size * (x % 16), terrain_png_size * (x / 16)));
			
			if ((bd.getMaterial().getNewData((byte) 0) instanceof Colorable))
			{
				DyeColor color = DyeColor.getByData(bd.getData());
				String hex = ColorConverter.dyeToHex(color);
				css.write(String.format("%s { color: %s; }\n", selector, hex));
			}
		}
			
		return css.toString();
	}
	
	private static String getTeamList(AutoRefMatch match)
	{
		StringWriter teamlist = new StringWriter();
		for (AutoRefTeam team : match.getSortedTeams())
		{
			Set<String> members = Sets.newHashSet();
			for (AutoRefPlayer apl : team.getPlayers())
				members.add("<li>" + playerHTML(apl) + "</li>\n");
			
			String memberlist = members.size() == 0 
				? "<li>{none}</li>" : StringUtils.join(members, "");
			
			teamlist.write("<div class='span3'>");
			teamlist.write(String.format("<h4 class='team team-%s'>%s</h4>",
				team.getTag(), ChatColor.stripColor(team.getName())));
			teamlist.write(String.format("<ul class='teammembers unstyled'>%s</ul></div>\n", memberlist));
		}
		
		return teamlist.toString();
	}
	
	private static class NemesisComparator implements Comparator<AutoRefPlayer>
	{
		private AutoRefPlayer target = null;
		
		public NemesisComparator(AutoRefPlayer target)
		{ this.target = target; }

		public int compare(AutoRefPlayer apl1, AutoRefPlayer apl2)
		{
			if (apl1.getTeam() == target.getTeam()) return -1;
			if (apl2.getTeam() == target.getTeam()) return +1;
			
			// get the number of kills on this player total
			int k = apl1.kills.get(target) - apl2.kills.get(target);
			if (k != 0) return k;
			
			// get the relative difference in "focus"
			return apl1.kills.get(target)*apl2.totalKills - 
				apl2.kills.get(target)*apl1.totalKills;
		}
	};

	@SuppressWarnings("unchecked")
	private static String getPlayerStats(AutoRefMatch match)
	{
		List<AutoRefPlayer> players = Lists.newArrayList(match.getPlayers());
		Collections.sort(players, new Comparator<AutoRefPlayer>()
		{
			public int compare(AutoRefPlayer apl1, AutoRefPlayer apl2)
			{ return apl2.getScore() - apl1.getScore(); }
		});
		
		Map<AutoRefPlayer, Integer> dd = new DefaultedMap(0);
		for (AutoRefPlayer apl : players)
			for (Map.Entry<AutoRefPlayer.DamageCause, Integer> dc : apl.damage.entrySet())
				if (dc.getKey().p instanceof AutoRefPlayer)
					dd.put((AutoRefPlayer) dc.getKey().p, dd.get(dc.getKey().p) + dc.getValue());
		
		int rank = 0;
		StringWriter playerstats = new StringWriter();
		for (AutoRefPlayer apl : players)
		{
			// get nemesis of this player
			AutoRefPlayer nms = Collections.max(players, new NemesisComparator(apl));
			if (nms != null && nms.getTeam() == apl.getTeam()) nms = null;
			
			playerstats.write(String.format("<tr><td>%d</td><td>%s</td>", 
					++rank, playerHTML(apl)));
			playerstats.write(String.format("<td>%d</td><td>%d</td><td>%s</td>", 
					apl.totalKills, apl.totalDeaths, apl.getExtendedAccuracyInfo()));
			playerstats.write(String.format("<td>%d</td><td>%d</td><td>%s</td></tr>\n", 
					999, dd.get(apl), nms == null ? "none" : playerHTML(nms)));
		}
		
		return playerstats.toString();
	}
	
	private static String playerHTML(AutoRefPlayer apl)
	{
		return String.format("<span class='player player-%s team-%s'>%s</span>",
			apl.getTag(), apl.getTeam().getTag(), apl.getPlayerName());
	}
	
	private static String transcriptEventHTML(TranscriptEvent e)
	{
		String m = e.getMessage();
		
		Set<AutoRefPlayer> players = Sets.newHashSet();
		if (e.icon1 instanceof AutoRefPlayer) players.add((AutoRefPlayer) e.icon1);
		if (e.icon2 instanceof AutoRefPlayer) players.add((AutoRefPlayer) e.icon2);
		
		for (AutoRefPlayer apl : players)
			m = m.replaceAll(apl.getPlayerName(), playerHTML(apl));
		
		if (e.icon2 instanceof BlockData)
		{
			BlockData bd = (BlockData) e.icon2;
			int mat = bd.getMaterial().getId();
			int data = bd.getData();
			
			m = m.replaceAll(bd.getRawName(), String.format(
				"<span class='block mat-%d data-%d'>%s</span>", 
					mat, data, bd.getRawName()));
		}
		
		String coords = BlockVector3.fromLocation(e.location).toCoords();
		String fmt = "<tr class='transcript-event %s' data-location='%s'>" + 
			"<td class='message'>%s</td><td class='timestamp'>%s</td></tr>";
		return String.format(fmt, e.getType().getEventClass(), coords, m, e.getTimestamp());
	}
}