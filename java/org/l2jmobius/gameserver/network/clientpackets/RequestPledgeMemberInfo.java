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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.serverpackets.PledgeReceiveMemberInfo;

/**
 * Format: (ch) dS
 * @author -Wooden-
 */
public class RequestPledgeMemberInfo extends ClientPacket
{
	@SuppressWarnings("unused")
	private int _unk1;
	private String _player;
	
	@Override
	protected void readImpl()
	{
		_unk1 = readInt();
		_player = readString();
	}
	
	@Override
	protected void runImpl()
	{
		// PacketLogger.info("C5: RequestPledgeMemberInfo d:"+_unk1);
		// PacketLogger.info("C5: RequestPledgeMemberInfo S:"+_player);
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		// do we need powers to do that??
		final Clan clan = player.getClan();
		if (clan == null)
		{
			return;
		}
		
		final ClanMember member = clan.getClanMember(_player);
		if (member == null)
		{
			return;
		}
		
		player.sendPacket(new PledgeReceiveMemberInfo(member));
	}
}