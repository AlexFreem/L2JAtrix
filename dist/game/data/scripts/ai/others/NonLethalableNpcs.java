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
package ai.others;

import org.l2jmobius.gameserver.model.actor.Npc;

import ai.AbstractNpcAI;

/**
 * @author UnAfraid
 */
public class NonLethalableNpcs extends AbstractNpcAI
{
	private static final int[] NPCS =
	{
		35062, // Headquarters
		// Conquerable Hall bosses.
		35410, // Gustav
		35375, // Nurka
		35629, // Lidia
		35630, // Alfred
		35631, // Giselle
	};
	
	public NonLethalableNpcs()
	{
		addSpawnId(NPCS);
	}
	
	@Override
	public void onSpawn(Npc npc)
	{
		npc.setLethalable(false);
	}
	
	public static void main(String[] args)
	{
		new NonLethalableNpcs();
	}
}
