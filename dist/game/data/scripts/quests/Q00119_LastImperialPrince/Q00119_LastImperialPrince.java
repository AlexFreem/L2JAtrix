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
package quests.Q00119_LastImperialPrince;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q00119_LastImperialPrince extends Quest
{
	// NPCs
	private static final int NAMELESS_SPIRIT = 31453;
	private static final int DEVORIN = 32009;
	// Item
	private static final int ANTIQUE_BROOCH = 7262;
	
	public Q00119_LastImperialPrince()
	{
		super(119, "Last Imperial Prince");
		registerQuestItems(ANTIQUE_BROOCH);
		addStartNpc(NAMELESS_SPIRIT);
		addTalkId(NAMELESS_SPIRIT, DEVORIN);
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
		
		switch (event)
		{
			case "31453-04.htm":
			{
				if (hasQuestItems(player, ANTIQUE_BROOCH))
				{
					st.startQuest();
				}
				else
				{
					htmltext = "31453-04b.htm";
					st.exitQuest(true);
				}
				break;
			}
			case "32009-02.htm":
			{
				if (!hasQuestItems(player, ANTIQUE_BROOCH))
				{
					htmltext = "31453-02a.htm";
					st.exitQuest(true);
				}
				break;
			}
			case "32009-03.htm":
			{
				st.setCond(2, true);
				break;
			}
			case "31453-07.htm":
			{
				giveAdena(player, 68787, true);
				st.exitQuest(false, true);
				break;
			}
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
				htmltext = (!hasQuestItems(player, ANTIQUE_BROOCH) || (player.getLevel() < 74)) ? "31453-00a.htm" : "31453-01.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getId())
				{
					case NAMELESS_SPIRIT:
					{
						if (cond == 1)
						{
							htmltext = "31453-04a.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31453-05.htm";
						}
						break;
					}
					case DEVORIN:
					{
						if (cond == 1)
						{
							htmltext = "32009-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "32009-04.htm";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = "31453-00b.htm";
				break;
			}
		}
		
		return htmltext;
	}
}
