/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.bosses.DrChaos;

import org.l2jmobius.gameserver.ai.Intention;
import org.l2jmobius.gameserver.managers.GrandBossManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.network.enums.ChatType;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.SpecialCamera;

import ai.AbstractNpcAI;

/**
 * Dr. Chaos is a boss @ Pavel's Ruins. Some things to know :
 * <ul>
 * <li>As a mad scientist, he thinks all are spies, and for so if you stand too much longer near him you're considered as an "assassin from Black Anvil Guild".</li>
 * <li>You can chat with him, but if you try too much he will become angry.</li>
 * <li>That adaptation sends a decent cinematic made with the different social actions too.</li>
 * <li>The status of the RB is saved under GBs table, in order to retrieve the state if server restarts.</li>
 * <li>The spawn of the different NPCs (Dr. Chaos / War golem) is handled by that script aswell.</li>
 * </ul>
 * @author Kerberos, Tryskell
 */
public class DrChaos extends AbstractNpcAI
{
	private static final int DOCTOR_CHAOS = 32033;
	private static final int CHAOS_GOLEM = 25512;
	
	private static final byte NORMAL = 0; // Dr. Chaos is in NPC form.
	private static final byte CRAZY = 1; // Dr. Chaos entered on golem form.
	private static final byte DEAD = 2; // Dr. Chaos has been killed and has not yet spawned.
	private static final int CHAOS_X = 96320;
	private static final int CHAOS_Y = -110912;
	private static final int CHAOS_Z = -3328;
	private static final int CHAOS_HEAD = 8191;
	
	private long _lastAttackVsGolem = 0;
	private int _pissedOffTimer;
	
	public DrChaos()
	{
		addFirstTalkId(DOCTOR_CHAOS); // Different HTMs following actual humor.
		addSpawnId(DOCTOR_CHAOS); // Timer activation at 30sec + paranoia activity.
		addKillId(CHAOS_GOLEM); // Message + despawn.
		addAttackId(CHAOS_GOLEM); // Random messages when he attacks.
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(CHAOS_GOLEM);
		final int status = GrandBossManager.getInstance().getStatus(CHAOS_GOLEM);
		
		// Load the reset date and time for Dr. Chaos from DB.
		if (status == DEAD)
		{
			final long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
			{
				startQuestTimer("reset_drchaos", temp, null, null, false);
			}
			else
			{
				// The time has already expired while the server was offline. Delete the saved time and
				// immediately spawn Dr. Chaos. Also the state need to be changed for NORMAL
				addSpawn(DOCTOR_CHAOS, CHAOS_X, CHAOS_Y, CHAOS_Z, CHAOS_HEAD, false, 0, false);
				GrandBossManager.getInstance().setStatus(CHAOS_GOLEM, NORMAL);
			}
		}
		// Spawn the war golem.
		else if (status == CRAZY)
		{
			final int loc_x = info.getInt("loc_x");
			final int loc_y = info.getInt("loc_y");
			final int loc_z = info.getInt("loc_z");
			final int heading = info.getInt("heading");
			final int hp = info.getInt("currentHP");
			final int mp = info.getInt("currentMP");
			final GrandBoss golem = (GrandBoss) addSpawn(CHAOS_GOLEM, loc_x, loc_y, loc_z, heading, false, 0, false);
			GrandBossManager.getInstance().addBoss(golem);
			
			golem.setCurrentHpMp(hp, mp);
			golem.setRunning();
			
			// start monitoring Dr. Chaos's inactivity
			_lastAttackVsGolem = System.currentTimeMillis();
			startQuestTimer("golem_despawn", 60000, golem, null, true);
		}
		// Spawn the regular NPC.
		else
		{
			addSpawn(DOCTOR_CHAOS, 96320, -110912, -3328, 8191, false, 0, false);
		}
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "reset_drchaos":
			{
				GrandBossManager.getInstance().setStatus(CHAOS_GOLEM, NORMAL);
				addSpawn(DOCTOR_CHAOS, CHAOS_X, CHAOS_Y, CHAOS_Z, CHAOS_HEAD, false, 0, false);
				break;
			}
			case "golem_despawn": // despawn the live Dr. Chaos after 30 minutes of inactivity
			{
				if ((npc.getId() == CHAOS_GOLEM) && ((_lastAttackVsGolem + 1800000) < System.currentTimeMillis()))
				{
					final Npc chaos = addSpawn(DOCTOR_CHAOS, CHAOS_X, CHAOS_Y, CHAOS_Z, CHAOS_HEAD, false, 0, false); // spawn Dr. Chaos
					GrandBossManager.getInstance().setStatus(CHAOS_GOLEM, NORMAL); // mark Dr. Chaos is not crazy any more
					cancelQuestTimer("golem_despawn", chaos, null);
					cancelQuestTimers("DISTANCE_CHECK");
					
					// Despawn the war golem.
					npc.deleteMe();
				}
				break;
			}
			case "1":
			{
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 2));
				npc.broadcastPacket(new SpecialCamera(npc, 1, -200, 15, 5500, 1000, 13500, 0, 0, 0, 0, 0));
				break;
			}
			case "2":
			{
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
				break;
			}
			case "3":
			{
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
				break;
			}
			case "4":
			{
				npc.broadcastPacket(new SpecialCamera(npc, 1, -150, 10, 3500, 1000, 5000, 0, 0, 0, 0, 0));
				npc.getAI().setIntention(Intention.MOVE_TO, new Location(95928, -110671, -3340, 0));
				break;
			}
			case "5":
			{
				final GrandBoss golem = (GrandBoss) addSpawn(CHAOS_GOLEM, 96080, -110822, -3343, 0, false, 0, false);
				GrandBossManager.getInstance().addBoss(golem);
				startQuestTimer("DISTANCE_CHECK", 10000, golem, null, true);
				golem.broadcastPacket(new SpecialCamera(npc, 30, 200, 20, 6000, 700, 8000, 0, 0, 0, 0, 0));
				golem.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
				golem.broadcastPacket(new PlaySound(1, "Rm03_A", 0, 0, 0, 0, 0));
				
				// start monitoring Dr. Chaos's inactivity
				_lastAttackVsGolem = System.currentTimeMillis();
				startQuestTimer("golem_despawn", 60000, golem, null, true);
				
				// Delete Dr. Chaos after spawned the war golem.
				npc.deleteMe();
				break;
			}
			case "paranoia_activity":
			{
				if (GrandBossManager.getInstance().getStatus(CHAOS_GOLEM) == NORMAL)
				{
					for (Player obj : World.getInstance().getVisibleObjectsInRange(npc, Player.class, 500))
					{
						if (obj.isDead())
						{
							continue;
						}
						
						_pissedOffTimer -= 1;
						
						// Make him speak.
						if (_pissedOffTimer == 15)
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, "How dare you trespass into my territory! Have you no fear?");
						}
						
						// That was "too much" for that time.
						if (_pissedOffTimer <= 0)
						{
							crazyMidgetBecomesAngry(npc);
						}
					}
				}
				if (npc.calculateDistance2D(CHAOS_X, CHAOS_Y, CHAOS_Z) > 2000)
				{
					npc.asAttackable().clearAggroList();
					npc.getAI().setIntention(Intention.MOVE_TO, new Location(CHAOS_X, CHAOS_Y, CHAOS_Z, 0));
				}
				break;
			}
			case "DISTANCE_CHECK":
			{
				if ((npc == null) || npc.isDead())
				{
					cancelQuestTimers("DISTANCE_CHECK");
				}
				else if (npc.calculateDistance2D(npc.getSpawn()) > 10000)
				{
					npc.asAttackable().clearAggroList();
					// npc.getAI().setIntention(Intention.MOVE_TO, new Location(CHAOS_X, CHAOS_Y, CHAOS_Z, 0));
					npc.teleToLocation(npc.getSpawn(), false);
				}
				break;
			}
		}
		return super.onEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = "";
		if (GrandBossManager.getInstance().getStatus(CHAOS_GOLEM) == NORMAL)
		{
			_pissedOffTimer -= 1 + getRandom(5); // remove 1-5 secs.
			if ((_pissedOffTimer > 20) && (_pissedOffTimer <= 30))
			{
				htmltext = "<html><body>Doctor Chaos:<br>What?! Who are you? How did you come here?<br>You really look suspicious... Aren't those filthy members of Black Anvil guild send you? No? Mhhhhh... I don't trust you!</body></html>";
			}
			else if ((_pissedOffTimer > 10) && (_pissedOffTimer <= 20))
			{
				htmltext = "<html><body>Doctor Chaos:<br>Why are you standing here? Don't you see it's a private propertie? Don't look at him with those eyes... Did you smile?! Don't make fun of me! He will ... destroy ... you ... if you continue!</body></html>";
			}
			else if ((_pissedOffTimer > 0) && (_pissedOffTimer <= 10))
			{
				htmltext = "<html><body>Doctor Chaos:<br>I know why you are here, traitor! He discovered your plans! You are assassin ... sent by the Black Anvil guild! But you won't kill the Emperor of Evil!</body></html>";
			}
			else if (_pissedOffTimer <= 0)
			{
				crazyMidgetBecomesAngry(npc);
			}
		}
		return htmltext;
	}
	
	@Override
	public void onSpawn(Npc npc)
	{
		// 30 seconds timer at initialization.
		_pissedOffTimer = 30;
		
		// Initialization of the paranoia.
		startQuestTimer("paranoia_activity", 1000, npc, null, true);
		
		cancelQuestTimer("DISTANCE_CHECK", npc, null);
		startQuestTimer("DISTANCE_CHECK", 10000, npc, null, true);
	}
	
	@Override
	public void onKill(Npc npc, Player player, boolean isPet)
	{
		cancelQuestTimer("golem_despawn", npc, null);
		npc.broadcastSay(ChatType.NPC_GENERAL, "Urggh! You will pay dearly for this insult.");
		
		// "lock" Dr. Chaos for regular RB time (36H fixed +- 24H random)
		final long respawnTime = (36 + getRandom(-24, 24)) * 3600000;
		GrandBossManager.getInstance().setStatus(CHAOS_GOLEM, DEAD);
		startQuestTimer("reset_drchaos", respawnTime, null, null, false);
		
		// also save the respawn time so that the info is maintained past reboots
		final StatSet info = GrandBossManager.getInstance().getStatSet(CHAOS_GOLEM);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatSet(CHAOS_GOLEM, info);
		
		// Stop distance check task.
		cancelQuestTimers("DISTANCE_CHECK");
	}
	
	@Override
	public void onAttack(Npc npc, Player victim, int damage, boolean isPet)
	{
		final int chance = getRandom(300);
		
		// Choose a message from 3 choices (1/100)
		if (chance < 3)
		{
			String message = "";
			switch (chance)
			{
				case 0:
				{
					message = "Bwah-ha-ha! Your doom is at hand! Behold the Ultra Secret Super Weapon!";
					break;
				}
				case 1:
				{
					message = "Foolish, insignificant creatures! How dare you challenge me!";
					break;
				}
				default:
				{
					message = "I see that none will challenge me now!";
					break;
				}
			}
			
			// Make him speak.
			npc.broadcastSay(ChatType.NPC_GENERAL, message);
		}
	}
	
	/**
	 * Launches the complete animation.
	 * @param npc the midget.
	 */
	private void crazyMidgetBecomesAngry(Npc npc)
	{
		if (GrandBossManager.getInstance().getStatus(CHAOS_GOLEM) == NORMAL)
		{
			// Set the status to "crazy".
			GrandBossManager.getInstance().setStatus(CHAOS_GOLEM, CRAZY);
			
			// Cancels the paranoia timer.
			cancelQuestTimer("paranoia_activity", npc, null);
			
			// Makes the NPC moves near the Strange Box speaking.
			npc.getAI().setIntention(Intention.MOVE_TO, new Location(96323, -110914, -3328, 0));
			npc.broadcastSay(ChatType.NPC_GENERAL, "Fools! Why haven't you fled yet? Prepare to learn a lesson!");
			
			// Delayed animation timers.
			startQuestTimer("1", 2000, npc, null, false); // 2 secs, time to launch dr.C anim 2. Cam 1 on.
			startQuestTimer("2", 4000, npc, null, false); // 2,5 secs, time to launch dr.C anim 3.
			startQuestTimer("3", 6500, npc, null, false); // 6 secs, time to launch dr.C anim 1.
			startQuestTimer("4", 12500, npc, null, false); // 4,5 secs to make the NPC moves to the grotto. Cam 2 on.
			startQuestTimer("5", 17000, npc, null, false); // 4 secs for golem spawn, and golem anim. Cam 3 on.
		}
	}
	
	public static void main(String[] args)
	{
		new DrChaos();
	}
}