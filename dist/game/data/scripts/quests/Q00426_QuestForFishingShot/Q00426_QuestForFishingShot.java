/*
 * Copyright (c) 2013 L2jMobius
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package quests.Q00426_QuestForFishingShot;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestSound;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q00426_QuestForFishingShot extends Quest
{
	private static final int SWEET_FLUID = 7586;
	private static final Map<Integer, Integer> MOBS1 = new HashMap<>();
	static
	{
		MOBS1.put(20005, 45);
		MOBS1.put(20013, 100);
		MOBS1.put(20016, 100);
		MOBS1.put(20017, 115);
		MOBS1.put(20030, 105);
		MOBS1.put(20132, 70);
		MOBS1.put(20038, 135);
		MOBS1.put(20044, 125);
		MOBS1.put(20046, 100);
		MOBS1.put(20047, 100);
		MOBS1.put(20050, 140);
		MOBS1.put(20058, 140);
		MOBS1.put(20063, 160);
		MOBS1.put(20066, 170);
		MOBS1.put(20070, 180);
		MOBS1.put(20074, 195);
		MOBS1.put(20077, 205);
		MOBS1.put(20078, 205);
		MOBS1.put(20079, 205);
		MOBS1.put(20080, 220);
		MOBS1.put(20081, 370);
		MOBS1.put(20083, 245);
		MOBS1.put(20084, 255);
		MOBS1.put(20085, 265);
		MOBS1.put(20087, 565);
		MOBS1.put(20088, 605);
		MOBS1.put(20089, 250);
		MOBS1.put(20100, 85);
		MOBS1.put(20103, 110);
		MOBS1.put(20105, 110);
		MOBS1.put(20115, 190);
		MOBS1.put(20120, 20);
		MOBS1.put(20131, 45);
		MOBS1.put(20135, 360);
		MOBS1.put(20157, 235);
		MOBS1.put(20162, 195);
		MOBS1.put(20176, 280);
		MOBS1.put(20211, 170);
		MOBS1.put(20225, 160);
		MOBS1.put(20227, 180);
		MOBS1.put(20230, 260);
		MOBS1.put(20232, 245);
		MOBS1.put(20234, 290);
		MOBS1.put(20241, 700);
		MOBS1.put(20267, 215);
		MOBS1.put(20268, 295);
		MOBS1.put(20269, 255);
		MOBS1.put(20270, 365);
		MOBS1.put(20271, 295);
		MOBS1.put(20286, 700);
		MOBS1.put(20308, 110);
		MOBS1.put(20312, 45);
		MOBS1.put(20317, 20);
		MOBS1.put(20324, 85);
		MOBS1.put(20333, 100);
		MOBS1.put(20341, 100);
		MOBS1.put(20346, 85);
		MOBS1.put(20349, 850);
		MOBS1.put(20356, 165);
		MOBS1.put(20357, 140);
		MOBS1.put(20363, 70);
		MOBS1.put(20368, 85);
		MOBS1.put(20371, 100);
		MOBS1.put(20386, 85);
		MOBS1.put(20389, 90);
		MOBS1.put(20403, 110);
		MOBS1.put(20404, 95);
		MOBS1.put(20433, 100);
		MOBS1.put(20436, 140);
		MOBS1.put(20448, 45);
		MOBS1.put(20456, 20);
		MOBS1.put(20463, 85);
		MOBS1.put(20470, 45);
		MOBS1.put(20471, 85);
		MOBS1.put(20475, 20);
		MOBS1.put(20478, 110);
		MOBS1.put(20487, 90);
		MOBS1.put(20511, 100);
		MOBS1.put(20525, 20);
		MOBS1.put(20528, 100);
		MOBS1.put(20536, 15);
		MOBS1.put(20537, 15);
		MOBS1.put(20538, 15);
		MOBS1.put(20539, 15);
		MOBS1.put(20544, 15);
		MOBS1.put(20550, 300);
		MOBS1.put(20551, 300);
		MOBS1.put(20552, 650);
		MOBS1.put(20553, 335);
		MOBS1.put(20554, 390);
		MOBS1.put(20555, 350);
		MOBS1.put(20557, 390);
		MOBS1.put(20559, 420);
		MOBS1.put(20560, 440);
		MOBS1.put(20562, 485);
		MOBS1.put(20573, 545);
		MOBS1.put(20575, 645);
		MOBS1.put(20630, 350);
		MOBS1.put(20632, 475);
		MOBS1.put(20634, 960);
		MOBS1.put(20636, 495);
		MOBS1.put(20638, 540);
		MOBS1.put(20641, 680);
		MOBS1.put(20643, 660);
		MOBS1.put(20644, 645);
		MOBS1.put(20659, 440);
		MOBS1.put(20661, 575);
		MOBS1.put(20663, 525);
		MOBS1.put(20665, 680);
		MOBS1.put(20667, 730);
		MOBS1.put(20766, 210);
		MOBS1.put(20781, 270);
		MOBS1.put(20783, 140);
		MOBS1.put(20784, 155);
		MOBS1.put(20786, 170);
		MOBS1.put(20788, 325);
		MOBS1.put(20790, 390);
		MOBS1.put(20792, 620);
		MOBS1.put(20794, 635);
		MOBS1.put(20796, 640);
		MOBS1.put(20798, 850);
		MOBS1.put(20800, 740);
		MOBS1.put(20802, 900);
		MOBS1.put(20804, 775);
		MOBS1.put(20806, 805);
		MOBS1.put(20833, 455);
		MOBS1.put(20834, 680);
		MOBS1.put(20836, 785);
		MOBS1.put(20837, 835);
		MOBS1.put(20839, 430);
		MOBS1.put(20841, 460);
		MOBS1.put(20845, 605);
		MOBS1.put(20847, 570);
		MOBS1.put(20849, 585);
		MOBS1.put(20936, 290);
		MOBS1.put(20937, 315);
		MOBS1.put(20939, 385);
		MOBS1.put(20940, 500);
		MOBS1.put(20941, 460);
		MOBS1.put(20943, 345);
		MOBS1.put(20944, 335);
		MOBS1.put(21100, 125);
		MOBS1.put(21101, 155);
		MOBS1.put(21103, 215);
		MOBS1.put(21105, 310);
		MOBS1.put(21107, 600);
		MOBS1.put(21117, 120);
		MOBS1.put(21023, 170);
		MOBS1.put(21024, 175);
		MOBS1.put(21025, 185);
		MOBS1.put(21026, 200);
		MOBS1.put(21034, 195);
		MOBS1.put(21125, 12);
		MOBS1.put(21263, 650);
		MOBS1.put(21520, 880);
		MOBS1.put(21526, 970);
		MOBS1.put(21536, 985);
		MOBS1.put(21602, 555);
		MOBS1.put(21603, 750);
		MOBS1.put(21605, 620);
		MOBS1.put(21606, 875);
		MOBS1.put(21611, 590);
		MOBS1.put(21612, 835);
		MOBS1.put(21617, 615);
		MOBS1.put(21618, 875);
		MOBS1.put(21635, 775);
		MOBS1.put(21638, 165);
		MOBS1.put(21639, 185);
		MOBS1.put(21641, 195);
		MOBS1.put(21644, 170);
	}
	private static final Map<Integer, Integer> MOBS2 = new HashMap<>();
	static
	{
		MOBS2.put(20579, 420);
		MOBS2.put(20639, 280);
		MOBS2.put(20646, 145);
		MOBS2.put(20648, 120);
		MOBS2.put(20650, 460);
		MOBS2.put(20651, 260);
		MOBS2.put(20652, 335);
		MOBS2.put(20657, 630);
		MOBS2.put(20658, 570);
		MOBS2.put(20808, 50);
		MOBS2.put(20809, 865);
		MOBS2.put(20832, 700);
		MOBS2.put(20979, 980);
		MOBS2.put(20991, 665);
		MOBS2.put(20994, 590);
		MOBS2.put(21261, 170);
		MOBS2.put(21263, 795);
		MOBS2.put(21508, 100);
		MOBS2.put(21510, 280);
		MOBS2.put(21511, 995);
		MOBS2.put(21512, 995);
		MOBS2.put(21514, 185);
		MOBS2.put(21516, 495);
		MOBS2.put(21517, 495);
		MOBS2.put(21518, 255);
		MOBS2.put(21636, 950);
	}
	private static final Map<Integer, Integer> MOBS3 = new HashMap<>();
	static
	{
		MOBS3.put(20655, 110);
		MOBS3.put(20656, 150);
		MOBS3.put(20772, 105);
		MOBS3.put(20810, 50);
		MOBS3.put(20812, 490);
		MOBS3.put(20814, 775);
		MOBS3.put(20816, 875);
		MOBS3.put(20819, 280);
		MOBS3.put(20955, 670);
		MOBS3.put(20978, 555);
		MOBS3.put(21058, 355);
		MOBS3.put(21060, 45);
		MOBS3.put(21075, 110);
		MOBS3.put(21078, 610);
		MOBS3.put(21081, 955);
		MOBS3.put(21264, 920);
	}
	private static final Map<Integer, Integer> MOBS4 = new HashMap<>();
	static
	{
		MOBS4.put(20815, 205);
		MOBS4.put(20822, 100);
		MOBS4.put(20824, 665);
		MOBS4.put(20825, 620);
		MOBS4.put(20983, 205);
		MOBS4.put(21314, 145);
		MOBS4.put(21316, 235);
		MOBS4.put(21318, 280);
		MOBS4.put(21320, 355);
		MOBS4.put(21322, 430);
		MOBS4.put(21376, 280);
		MOBS4.put(21378, 375);
		MOBS4.put(21380, 375);
		MOBS4.put(21387, 640);
		MOBS4.put(21393, 935);
		MOBS4.put(21395, 855);
		MOBS4.put(21652, 375);
		MOBS4.put(21655, 640);
		MOBS4.put(21657, 935);
	}
	private static final Map<Integer, Integer> MOBS5 = new HashMap<>();
	static
	{
		MOBS5.put(20828, 935);
		MOBS5.put(21061, 530);
		MOBS5.put(21069, 825);
		MOBS5.put(21382, 125);
		MOBS5.put(21384, 400);
		MOBS5.put(21390, 750);
		MOBS5.put(21654, 400);
		MOBS5.put(21656, 750);
	}
	private static final Map<Integer, int[]> MOB_SPECIAL = new HashMap<>();
	static
	{
		// @formatter:off
		MOB_SPECIAL.put(20829, new int[]{115, 6});
		MOB_SPECIAL.put(20859, new int[]{890, 8});
		MOB_SPECIAL.put(21066, new int[]{5, 5});
		MOB_SPECIAL.put(21068, new int[]{565, 11});
		MOB_SPECIAL.put(21071, new int[]{400, 12});
		// @formatter:on
	}
	
	public Q00426_QuestForFishingShot()
	{
		super(426, "Quest for Fishing Shot");
		registerQuestItems(SWEET_FLUID);
		addStartNpc(31562, 31563, 31564, 31565, 31566, 31567, 31568, 31569, 31570, 31571, 31572, 31573, 31574, 31575, 31576, 31577, 31578, 31579, 31696, 31697, 31989, 32007);
		addTalkId(31562, 31563, 31564, 31565, 31566, 31567, 31568, 31569, 31570, 31571, 31572, 31573, 31574, 31575, 31576, 31577, 31578, 31579, 31696, 31697, 31989, 32007);
		addKillId(MOBS1.keySet());
		addKillId(MOBS2.keySet());
		addKillId(MOBS3.keySet());
		addKillId(MOBS4.keySet());
		addKillId(MOBS5.keySet());
		addKillId(MOB_SPECIAL.keySet());
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("03.htm"))
		{
			st.startQuest();
		}
		else if (event.equals("08.htm"))
		{
			st.exitQuest(true, true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = "01.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = (hasQuestItems(player, SWEET_FLUID)) ? "05.htm" : "04.htm";
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public void onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState st = getRandomPartyMemberState(player, -1, 3, npc);
		if ((st == null) || !st.isStarted())
		{
			return;
		}
		
		final Player partyMember = st.getPlayer();
		int drop = 0;
		int chance = 0;
		final int npcId = npc.getId();
		if (MOBS1.containsKey(npcId))
		{
			chance = MOBS1.get(npcId);
		}
		else if (MOBS2.containsKey(npcId))
		{
			chance = MOBS2.get(npcId);
			drop = 1;
		}
		else if (MOBS3.containsKey(npcId))
		{
			chance = MOBS3.get(npcId);
			drop = 2;
		}
		else if (MOBS4.containsKey(npcId))
		{
			chance = MOBS4.get(npcId);
			drop = 3;
		}
		else if (MOBS5.containsKey(npcId))
		{
			chance = MOBS5.get(npcId);
			drop = 4;
		}
		else if (MOB_SPECIAL.containsKey(npcId))
		{
			chance = MOB_SPECIAL.get(npcId)[0];
			drop = MOB_SPECIAL.get(npcId)[1];
		}
		
		if (getRandom(1000) <= chance)
		{
			drop++;
		}
		
		if (drop != 0)
		{
			playSound(partyMember, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			rewardItems(partyMember, SWEET_FLUID, drop);
		}
	}
}
