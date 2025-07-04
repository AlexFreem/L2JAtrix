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
package ai.areas.BeastFarm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.ai.Intention;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.TamedBeast;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.enums.ChatType;
import org.l2jmobius.gameserver.util.ArrayUtil;

import ai.AbstractNpcAI;
import quests.Q00020_BringUpWithLove.Q00020_BringUpWithLove;
import quests.Q00655_AGrandPlanForTamingWildBeasts.Q00655_AGrandPlanForTamingWildBeasts;

/**
 * Growth-capable mobs: Polymorphing upon successful feeding.
 * @author Fulminus, Mobius
 */
public class FeedableBeasts extends AbstractNpcAI
{
	private static final int GOLDEN_SPICE = 6643;
	private static final int CRYSTAL_SPICE = 6644;
	private static final int SKILL_GOLDEN_SPICE = 2188;
	private static final int SKILL_CRYSTAL_SPICE = 2189;
	
	// @formatter:off
	private static final int[] TAMED_BEASTS =
	{
		16013, 16014, 16015, 16016, 16017, 16018
	};
	
	private static final int[] FEEDABLE_BEASTS =
	{
		21451, 21452, 21453, 21454, 21455, 21456, 21457, 21458, 21459, 21460, 21461, 21462, 21463, 21464, 21465, 21466, 21467, 21468, 21469, // Alpen Kookaburra
		21470, 21471, 21472, 21473, 21474, 21475, 21476, 21477, 21478, 21479, 21480, 21481, 21482, 21483, 21484, 21485, 21486, 21487, 21488, // Alpen Buffalo
		21489, 21490, 21491, 21492, 21493, 21494, 21495, 21496, 21497, 21498, 21499, 21500, 21501, 21502, 21503, 21504, 21505, 21506, 21507, // Alpen Cougar
		21824, 21825, 21826, 21827, 21828, 21829 // Alpen Kookaburra, Buffalo, Cougar
	};
	// @formatter:on
	
	private static final Map<Integer, Integer> MAD_COW_POLYMORPH = new HashMap<>();
	static
	{
		MAD_COW_POLYMORPH.put(21824, 21468);
		MAD_COW_POLYMORPH.put(21825, 21469);
		MAD_COW_POLYMORPH.put(21826, 21487);
		MAD_COW_POLYMORPH.put(21827, 21488);
		MAD_COW_POLYMORPH.put(21828, 21506);
		MAD_COW_POLYMORPH.put(21829, 21507);
	}
	
	private static final String[][] TEXT =
	{
		{
			"What did you just do to me?",
			"You want to tame me, huh?",
			"Do not give me this. Perhaps you will be in danger.",
			"Bah bah. What is this unpalatable thing?",
			"My belly has been complaining. This hit the spot.",
			"What is this? Can I eat it?",
			"You don't need to worry about me.",
			"Delicious food, thanks.",
			"I am starting to like you!",
			"Gulp!"
		},
		{
			"I do not think you have given up on the idea of taming me.",
			"That is just food to me. Perhaps I can eat your hand too.",
			"Will eating this make me fat? Ha ha.",
			"Why do you always feed me?",
			"Do not trust me. I may betray you."
		},
		{
			"Destroy!",
			"Look what you have done!",
			"Strange feeling...! Evil intentions grow in my heart...!",
			"It is happening!",
			"This is sad...Good is sad...!"
		}
	};
	
	private static final String[] SPAWN_CHATS =
	{
		"$s1, will you show me your hideaway?",
		"$s1, whenever I look at spice, I think about you.",
		"$s1, you do not need to return to the village. I will give you strength.",
		"Thanks, $s1. I hope I can help you.",
		"$s1, what can I do to help you?",
	};
	
	private static final Map<Integer, Integer> FEED_INFO = new ConcurrentHashMap<>();
	private static final Map<Integer, GrowthCapableMob> GROWTH_CAPABLE_MONSTERS = new HashMap<>();
	
	private static class GrowthCapableMob
	{
		private final int _growthLevel;
		private final int _chance;
		
		private final Map<Integer, int[][]> _spiceToMob = new HashMap<>();
		
		public GrowthCapableMob(int growthLevel, int chance)
		{
			_growthLevel = growthLevel;
			_chance = chance;
		}
		
		public void addMobs(int spice, int[][] mobs)
		{
			_spiceToMob.put(spice, mobs);
		}
		
		public Integer getMob(int spice, int mobType, int classType)
		{
			if (_spiceToMob.containsKey(spice))
			{
				return _spiceToMob.get(spice)[mobType][classType];
			}
			return null;
		}
		
		public Integer getRandomMob(int spice)
		{
			int[][] temp;
			temp = _spiceToMob.get(spice);
			return temp[0][getRandom(temp[0].length)];
		}
		
		public Integer getChance()
		{
			return _chance;
		}
		
		public Integer getGrowthLevel()
		{
			return _growthLevel;
		}
	}
	
	private FeedableBeasts()
	{
		addKillId(FEEDABLE_BEASTS);
		addSkillSeeId(FEEDABLE_BEASTS);
		
		// TODO: no grendels?
		GrowthCapableMob temp;
		
		// @formatter:off
		final int[][] Kookabura_0_Gold = {{ 21452, 21453, 21454, 21455 }};
		final int[][] Kookabura_0_Crystal = {{ 21456, 21457, 21458, 21459 }};
		final int[][] Kookabura_1_Gold_1= {{ 21460, 21462 }};
		final int[][] Kookabura_1_Gold_2 = {{ 21461, 21463 }};
		final int[][] Kookabura_1_Crystal_1 = {{ 21464, 21466 }};
		final int[][] Kookabura_1_Crystal_2 = {{ 21465, 21467 }};
		final int[][] Kookabura_2_1 = {{ 21468, 21824}, { 16017, 16018 }};
		final int[][] Kookabura_2_2 = {{ 21469, 21825}, { 16017, 16018 }};
		final int[][] Buffalo_0_Gold = {{ 21471, 21472, 21473, 21474 }};
		final int[][] Buffalo_0_Crystal = {{ 21475, 21476, 21477, 21478 }};
		final int[][] Buffalo_1_Gold_1 = {{ 21479, 21481 }};
		final int[][] Buffalo_1_Gold_2 = {{ 21481, 21482 }};
		final int[][] Buffalo_1_Crystal_1 = {{ 21483, 21485 }};
		final int[][] Buffalo_1_Crystal_2 = {{ 21484, 21486 }};
		final int[][] Buffalo_2_1 = {{ 21487,21826}, {16013, 16014 }};
		final int[][] Buffalo_2_2 = {{ 21488,21827}, {16013, 16014 }};
		final int[][] Cougar_0_Gold = {{ 21490, 21491, 21492, 21493 }};
		final int[][] Cougar_0_Crystal = {{ 21494,21495, 21496, 21497 }};
		final int[][] Cougar_1_Gold_1 = {{ 21498, 21500 }};
		final int[][] Cougar_1_Gold_2 = {{ 21499, 21501 }};
		final int[][] Cougar_1_Crystal_1 = {{ 21502,21504 }};
		final int[][] Cougar_1_Crystal_2 = {{ 21503,21505 }};
		final int[][] Cougar_2_1 = {{ 21506, 21828 }, { 16015,16016 }};
		final int[][] Cougar_2_2 = {{ 21507, 21829 }, { 16015,16016 }};
		// @formatter:on
		
		// Alpen Kookabura
		temp = new GrowthCapableMob(0, 100);
		temp.addMobs(GOLDEN_SPICE, Kookabura_0_Gold);
		temp.addMobs(CRYSTAL_SPICE, Kookabura_0_Crystal);
		GROWTH_CAPABLE_MONSTERS.put(21451, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Kookabura_1_Gold_1);
		GROWTH_CAPABLE_MONSTERS.put(21452, temp);
		GROWTH_CAPABLE_MONSTERS.put(21454, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Kookabura_1_Gold_2);
		GROWTH_CAPABLE_MONSTERS.put(21453, temp);
		GROWTH_CAPABLE_MONSTERS.put(21455, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Kookabura_1_Crystal_1);
		GROWTH_CAPABLE_MONSTERS.put(21456, temp);
		GROWTH_CAPABLE_MONSTERS.put(21458, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Kookabura_1_Crystal_2);
		GROWTH_CAPABLE_MONSTERS.put(21457, temp);
		GROWTH_CAPABLE_MONSTERS.put(21459, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Kookabura_2_1);
		GROWTH_CAPABLE_MONSTERS.put(21460, temp);
		GROWTH_CAPABLE_MONSTERS.put(21462, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Kookabura_2_2);
		GROWTH_CAPABLE_MONSTERS.put(21461, temp);
		GROWTH_CAPABLE_MONSTERS.put(21463, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Kookabura_2_1);
		GROWTH_CAPABLE_MONSTERS.put(21464, temp);
		GROWTH_CAPABLE_MONSTERS.put(21466, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Kookabura_2_2);
		GROWTH_CAPABLE_MONSTERS.put(21465, temp);
		GROWTH_CAPABLE_MONSTERS.put(21467, temp);
		
		// Alpen Buffalo
		temp = new GrowthCapableMob(0, 100);
		temp.addMobs(GOLDEN_SPICE, Buffalo_0_Gold);
		temp.addMobs(CRYSTAL_SPICE, Buffalo_0_Crystal);
		GROWTH_CAPABLE_MONSTERS.put(21470, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Buffalo_1_Gold_1);
		GROWTH_CAPABLE_MONSTERS.put(21471, temp);
		GROWTH_CAPABLE_MONSTERS.put(21473, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Buffalo_1_Gold_2);
		GROWTH_CAPABLE_MONSTERS.put(21472, temp);
		GROWTH_CAPABLE_MONSTERS.put(21474, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Buffalo_1_Crystal_1);
		GROWTH_CAPABLE_MONSTERS.put(21475, temp);
		GROWTH_CAPABLE_MONSTERS.put(21477, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Buffalo_1_Crystal_2);
		GROWTH_CAPABLE_MONSTERS.put(21476, temp);
		GROWTH_CAPABLE_MONSTERS.put(21478, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Buffalo_2_1);
		GROWTH_CAPABLE_MONSTERS.put(21479, temp);
		GROWTH_CAPABLE_MONSTERS.put(21481, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Buffalo_2_2);
		GROWTH_CAPABLE_MONSTERS.put(21480, temp);
		GROWTH_CAPABLE_MONSTERS.put(21482, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Buffalo_2_1);
		GROWTH_CAPABLE_MONSTERS.put(21483, temp);
		GROWTH_CAPABLE_MONSTERS.put(21485, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Buffalo_2_2);
		GROWTH_CAPABLE_MONSTERS.put(21484, temp);
		GROWTH_CAPABLE_MONSTERS.put(21486, temp);
		
		// Alpen Cougar
		temp = new GrowthCapableMob(0, 100);
		temp.addMobs(GOLDEN_SPICE, Cougar_0_Gold);
		temp.addMobs(CRYSTAL_SPICE, Cougar_0_Crystal);
		GROWTH_CAPABLE_MONSTERS.put(21489, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Cougar_1_Gold_1);
		GROWTH_CAPABLE_MONSTERS.put(21490, temp);
		GROWTH_CAPABLE_MONSTERS.put(21492, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(GOLDEN_SPICE, Cougar_1_Gold_2);
		GROWTH_CAPABLE_MONSTERS.put(21491, temp);
		GROWTH_CAPABLE_MONSTERS.put(21493, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Cougar_1_Crystal_1);
		GROWTH_CAPABLE_MONSTERS.put(21494, temp);
		GROWTH_CAPABLE_MONSTERS.put(21496, temp);
		
		temp = new GrowthCapableMob(1, 40);
		temp.addMobs(CRYSTAL_SPICE, Cougar_1_Crystal_2);
		GROWTH_CAPABLE_MONSTERS.put(21495, temp);
		GROWTH_CAPABLE_MONSTERS.put(21497, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Cougar_2_1);
		GROWTH_CAPABLE_MONSTERS.put(21498, temp);
		GROWTH_CAPABLE_MONSTERS.put(21500, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(GOLDEN_SPICE, Cougar_2_2);
		GROWTH_CAPABLE_MONSTERS.put(21499, temp);
		GROWTH_CAPABLE_MONSTERS.put(21501, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Cougar_2_1);
		GROWTH_CAPABLE_MONSTERS.put(21502, temp);
		GROWTH_CAPABLE_MONSTERS.put(21504, temp);
		
		temp = new GrowthCapableMob(2, 25);
		temp.addMobs(CRYSTAL_SPICE, Cougar_2_2);
		GROWTH_CAPABLE_MONSTERS.put(21503, temp);
		GROWTH_CAPABLE_MONSTERS.put(21505, temp);
	}
	
	private void spawnNext(Npc npc, int growthLevel, Player player, int food)
	{
		if (npc.isScriptValue(1))
		{
			return;
		}
		npc.setScriptValue(1);
		
		if (!npc.isSpawned())
		{
			return;
		}
		
		final int npcId = npc.getId();
		int nextNpcId = 0;
		
		// Find the next mob to spawn, based on the current npcId, growthlevel, and food.
		if (growthLevel == 2)
		{
			// If tamed, the mob that will spawn depends on the class type (fighter/mage) of the player!
			if (getRandom(2) == 0)
			{
				if (player.isMageClass())
				{
					nextNpcId = GROWTH_CAPABLE_MONSTERS.get(npcId).getMob(food, 1, 1);
				}
				else
				{
					nextNpcId = GROWTH_CAPABLE_MONSTERS.get(npcId).getMob(food, 1, 0);
				}
			}
			else
			{
				/*
				 * If not tamed, there is a small chance that have "mad cow" disease. that is a stronger-than-normal animal that attacks its feeder
				 */
				if (getRandom(5) == 0)
				{
					nextNpcId = GROWTH_CAPABLE_MONSTERS.get(npcId).getMob(food, 0, 1);
				}
				else
				{
					nextNpcId = GROWTH_CAPABLE_MONSTERS.get(npcId).getMob(food, 0, 0);
				}
			}
		}
		// All other levels of growth are straight-forward
		else
		{
			nextNpcId = GROWTH_CAPABLE_MONSTERS.get(npcId).getRandomMob(food);
		}
		
		// Remove the feedinfo of the mob that got despawned, if any
		if (FEED_INFO.getOrDefault(npc.getObjectId(), 0) == player.getObjectId())
		{
			FEED_INFO.remove(npc.getObjectId());
		}
		
		// Despawn the old mob
		npc.deleteMe();
		final Spawn spawn = npc.getSpawn();
		if (spawn != null)
		{
			spawn.decreaseCount(npc);
		}
		
		// if this is finally a trained mob, then despawn any other trained mobs that the player might have and initialize the Tamed Beast.
		if (ArrayUtil.contains(TAMED_BEASTS, nextNpcId))
		{
			if ((player.getTrainedBeasts() != null) && !player.getTrainedBeasts().isEmpty())
			{
				for (TamedBeast oldTrained : player.getTrainedBeasts())
				{
					oldTrained.deleteMe();
				}
			}
			
			final TamedBeast nextNpc = new TamedBeast(nextNpcId, player, food, npc.getX(), npc.getY(), npc.getZ());
			nextNpc.setRunning();
			
			// If player has Q020 going, give quest item
			final QuestState st = player.getQuestState(Q00020_BringUpWithLove.class.getSimpleName());
			if ((st != null) && (getRandom(100) < 5) && !hasQuestItems(player, 7185))
			{
				giveItems(player, 7185, 1);
				st.setCond(2);
			}
			
			Q00655_AGrandPlanForTamingWildBeasts.checkCrystalofPurity(player);
			
			// Also, perform a rare random chat
			final int rand = getRandom(20);
			if (rand < 5)
			{
				npc.broadcastSay(ChatType.GENERAL, SPAWN_CHATS[rand].replace("$s1", player.getName()));
			}
		}
		else
		{
			// If not trained, the newly spawned mob will automatically be aggro against its feeder
			// (what happened to "never bite the hand that feeds you" anyway?!)
			final Attackable nextNpc = addSpawn(nextNpcId, npc).asAttackable();
			if (MAD_COW_POLYMORPH.containsKey(nextNpcId))
			{
				startQuestTimer("polymorph Mad Cow", 10000, nextNpc, player, false);
			}
			
			// Register the player in the feedinfo for the mob that just spawned
			FEED_INFO.put(nextNpc.getObjectId(), player.getObjectId());
			
			nextNpc.setRunning();
			nextNpc.addDamageHate(player, 0, 99999);
			nextNpc.getAI().setIntention(Intention.ATTACK, player);
		}
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("polymorph Mad Cow") && (npc != null) && (player != null) && MAD_COW_POLYMORPH.containsKey(npc.getId()))
		{
			// remove the feed info from the previous mob
			if (FEED_INFO.getOrDefault(npc.getObjectId(), 0) == player.getObjectId())
			{
				FEED_INFO.remove(npc.getObjectId());
			}
			
			// despawn the mad cow
			npc.deleteMe();
			
			// spawn the new mob
			final Attackable nextNpc = addSpawn(MAD_COW_POLYMORPH.get(npc.getId()), npc).asAttackable();
			
			// register the player in the feedinfo for the mob that just spawned
			FEED_INFO.put(nextNpc.getObjectId(), player.getObjectId());
			
			nextNpc.setRunning();
			nextNpc.addDamageHate(player, 0, 99999);
			nextNpc.getAI().setIntention(Intention.ATTACK, player);
		}
		
		return super.onEvent(event, npc, player);
	}
	
	@Override
	public void onSkillSee(Npc npc, Player caster, Skill skill, List<WorldObject> targets, boolean isSummon)
	{
		// this behavior is only run when the target of skill is the passed npc (chest)
		// i.e. when the player is attempting to open the chest using a skill
		if (!targets.contains(npc))
		{
			return;
		}
		
		// Gather some values on local variables
		final int npcId = npc.getId();
		final int skillId = skill.getId();
		
		// Check if the npc and skills used are valid for this script. Exit if invalid.
		if (!ArrayUtil.contains(FEEDABLE_BEASTS, npcId) || ((skillId != SKILL_GOLDEN_SPICE) && (skillId != SKILL_CRYSTAL_SPICE)))
		{
			return;
		}
		
		// First gather some values on local variables
		final int objectId = npc.getObjectId();
		int growthLevel = 3; // if a mob is in FEEDABLE_BEASTS but not in GROWTH_CAPABLE_MOBS, then it's at max growth (3)
		if (GROWTH_CAPABLE_MONSTERS.containsKey(npcId))
		{
			growthLevel = GROWTH_CAPABLE_MONSTERS.get(npcId).getGrowthLevel();
		}
		
		// Prevent exploit which allows 2 players to simultaneously raise the same 0-growth beast
		// If the mob is at 0th level (when it still listens to all feeders) lock it to the first feeder!
		if ((growthLevel == 0) && FEED_INFO.containsKey(objectId))
		{
			return;
		}
		
		FEED_INFO.put(objectId, caster.getObjectId());
		
		int food = 0;
		if (skillId == SKILL_GOLDEN_SPICE)
		{
			food = GOLDEN_SPICE;
		}
		else // if (skillId == SKILL_CRYSTAL_SPICE)
		{
			food = CRYSTAL_SPICE;
		}
		
		// Display the social action of the beast eating the food.
		npc.broadcastSocialAction(2);
		
		// If the pet can grow
		if (GROWTH_CAPABLE_MONSTERS.containsKey(npcId))
		{
			// Do nothing if this mob doesn't eat the specified food (food gets consumed but has no effect).
			if (GROWTH_CAPABLE_MONSTERS.get(npcId).getMob(food, 0, 0) == null)
			{
				return;
			}
			
			// Rare random talk...
			if (getRandom(20) == 0)
			{
				npc.broadcastSay(ChatType.GENERAL, TEXT[growthLevel][getRandom(TEXT[growthLevel].length)]);
			}
			
			if ((growthLevel > 0) && (FEED_INFO.getOrDefault(objectId, 0) != caster.getObjectId()))
			{
				// check if this is the same player as the one who raised it from growth 0.
				// if no, then do not allow a chance to raise the pet (food gets consumed but has no effect).
				return;
			}
			
			// Polymorph the mob, with a certain chance, given its current growth level
			if (getRandom(100) < GROWTH_CAPABLE_MONSTERS.get(npcId).getChance())
			{
				spawnNext(npc, growthLevel, caster, food);
			}
		}
	}
	
	@Override
	public void onKill(Npc npc, Player killer, boolean isSummon)
	{
		// Remove the feedinfo of the mob that got killed, if any
		FEED_INFO.remove(npc.getObjectId());
	}
	
	public static void main(String[] args)
	{
		new FeedableBeasts();
	}
}
