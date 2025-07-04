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
package org.l2jmobius.gameserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.crypt.NewCrypt;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.network.base.BaseWritablePacket;
import org.l2jmobius.commons.util.HexUtil;
import org.l2jmobius.commons.util.TraceUtil;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ConnectionState;
import org.l2jmobius.gameserver.network.Disconnection;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.loginserverpackets.game.AuthRequest;
import org.l2jmobius.gameserver.network.loginserverpackets.game.BlowFishKey;
import org.l2jmobius.gameserver.network.loginserverpackets.game.ChangeAccessLevel;
import org.l2jmobius.gameserver.network.loginserverpackets.game.ChangePassword;
import org.l2jmobius.gameserver.network.loginserverpackets.game.PlayerAuthRequest;
import org.l2jmobius.gameserver.network.loginserverpackets.game.PlayerInGame;
import org.l2jmobius.gameserver.network.loginserverpackets.game.PlayerLogout;
import org.l2jmobius.gameserver.network.loginserverpackets.game.PlayerTracert;
import org.l2jmobius.gameserver.network.loginserverpackets.game.ReplyCharacters;
import org.l2jmobius.gameserver.network.loginserverpackets.game.SendMail;
import org.l2jmobius.gameserver.network.loginserverpackets.game.ServerStatus;
import org.l2jmobius.gameserver.network.loginserverpackets.game.TempBan;
import org.l2jmobius.gameserver.network.loginserverpackets.login.AuthResponse;
import org.l2jmobius.gameserver.network.loginserverpackets.login.ChangePasswordResponse;
import org.l2jmobius.gameserver.network.loginserverpackets.login.InitLS;
import org.l2jmobius.gameserver.network.loginserverpackets.login.KickPlayer;
import org.l2jmobius.gameserver.network.loginserverpackets.login.LoginServerFail;
import org.l2jmobius.gameserver.network.loginserverpackets.login.PlayerAuthResponse;
import org.l2jmobius.gameserver.network.loginserverpackets.login.RequestCharacters;
import org.l2jmobius.gameserver.network.serverpackets.CharSelectionInfo;
import org.l2jmobius.gameserver.network.serverpackets.LoginFail;
import org.l2jmobius.gameserver.network.serverpackets.ServerClose;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class LoginServerThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(LoginServerThread.class.getName());
	protected static final Logger ACCOUNTING_LOGGER = Logger.getLogger("accounting");
	
	/**
	 * @see org.l2jmobius.loginserver.LoginServer#PROTOCOL_REV
	 */
	private static final int REVISION = 0x0106;
	private final String _hostname;
	private final int _port;
	private final int _gamePort;
	private Socket _socket;
	private OutputStream _out;
	private NewCrypt _blowfish;
	private byte[] _hexID;
	private final boolean _acceptAlternate;
	private int _requestID;
	private final boolean _reserveHost;
	private int _maxPlayer;
	private final Set<WaitingClient> _waitingClients = ConcurrentHashMap.newKeySet();
	private final Map<String, GameClient> _accountsInGameServer = new ConcurrentHashMap<>();
	private int _status;
	private String _serverName;
	private final List<String> _subnets;
	private final List<String> _hosts;
	
	protected LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if (_hexID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = HexUtil.generateHexBytes(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_subnets = Config.GAME_SERVER_SUBNETS;
		_hosts = Config.GAME_SERVER_HOSTS;
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}
	
	@Override
	public void run()
	{
		while (!isInterrupted())
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				// Connection.
				LOGGER.info(getClass().getSimpleName() + ": Connecting to login on " + _hostname + ":" + _port);
				_socket = new Socket(_hostname, _port);
				final InputStream in = _socket.getInputStream();
				_out = new BufferedOutputStream(_socket.getOutputStream());
				
				// Initialize Blowfish.
				final byte[] blowfishKey = HexUtil.generateHexBytes(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (!isInterrupted())
				{
					lengthLo = in.read();
					lengthHi = in.read();
					length = (lengthHi * 256) + lengthLo;
					if (lengthHi < 0)
					{
						LOGGER.finer(getClass().getSimpleName() + ": Login terminated the connection.");
						break;
					}
					
					final byte[] incoming = new byte[length - 2];
					int receivedBytes = 0;
					int newBytes = 0;
					int left = length - 2;
					while ((newBytes != -1) && (receivedBytes < (length - 2)))
					{
						newBytes = in.read(incoming, receivedBytes, left);
						receivedBytes += newBytes;
						left -= newBytes;
					}
					
					if (receivedBytes != (length - 2))
					{
						LOGGER.warning(getClass().getSimpleName() + ": Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}
					
					// Decrypt if we have a key.
					_blowfish.decrypt(incoming, 0, incoming.length);
					checksumOk = NewCrypt.verifyChecksum(incoming);
					if (!checksumOk)
					{
						LOGGER.warning(getClass().getSimpleName() + ": Incorrect packet checksum, ignoring packet (LS)");
						break;
					}
					
					final int packetType = incoming[0] & 0xff;
					switch (packetType)
					{
						case 0x00:
						{
							final InitLS init = new InitLS(incoming);
							if (init.getRevision() != REVISION)
							{
								LOGGER.warning("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							
							RSAPublicKey publicKey;
							try
							{
								final KeyFactory kfac = KeyFactory.getInstance("RSA");
								final BigInteger modulus = new BigInteger(init.getRSAKey());
								final RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
							}
							catch (GeneralSecurityException e)
							{
								LOGGER.warning(getClass().getSimpleName() + ": Trouble while init the public key send by login");
								break;
							}
							
							// Send the blowfish key through the RSA encryption.
							sendPacket(new BlowFishKey(blowfishKey, publicKey));
							// Now, only accept packet with the new encryption.
							_blowfish = new NewCrypt(blowfishKey);
							sendPacket(new AuthRequest(_requestID, _acceptAlternate, _hexID, _gamePort, _reserveHost, _maxPlayer, _subnets, _hosts));
							break;
						}
						case 0x01:
						{
							final LoginServerFail lsf = new LoginServerFail(incoming);
							LOGGER.info(getClass().getSimpleName() + ": Damn! Registeration Failed: " + lsf.getReasonString());
							// Login will close the connection here.
							break;
						}
						case 0x02:
						{
							final AuthResponse aresp = new AuthResponse(incoming);
							final int serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(serverID, hexToString(_hexID));
							LOGGER.info(getClass().getSimpleName() + ": Registered on login as Server " + serverID + ": " + _serverName);
							
							final ServerStatus st = new ServerStatus();
							if (Config.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							st.addAttribute(ServerStatus.SERVER_TYPE, Config.SERVER_LIST_TYPE);
							if (Config.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							if (Config.SERVER_LIST_AGE == 15)
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_15);
							}
							else if (Config.SERVER_LIST_AGE == 18)
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_18);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_ALL);
							}
							sendPacket(st);
							final List<String> playerList = new ArrayList<>();
							for (Player player : World.getInstance().getPlayers())
							{
								if (!player.isInOfflineMode())
								{
									playerList.add(player.getAccountName());
								}
							}
							if (!playerList.isEmpty())
							{
								sendPacket(new PlayerInGame(playerList));
							}
							break;
						}
						case 0x03:
						{
							final PlayerAuthResponse par = new PlayerAuthResponse(incoming);
							final String account = par.getAccount();
							WaitingClient wcToRemove = null;
							synchronized (_waitingClients)
							{
								for (WaitingClient wc : _waitingClients)
								{
									if (wc.account.equals(account))
									{
										wcToRemove = wc;
										break;
									}
								}
							}
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									final PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									wcToRemove.gameClient.setConnectionState(ConnectionState.AUTHENTICATED);
									wcToRemove.gameClient.setSessionId(wcToRemove.sessionKey);
									final CharSelectionInfo cl = new CharSelectionInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.sendPacket(cl);
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
								}
								else
								{
									LOGGER.warning(getClass().getSimpleName() + ": Session key is not correct. Closing connection for account " + wcToRemove.account);
									// wcToRemove.gameClient.getConnection().sendPacket(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									wcToRemove.gameClient.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									sendLogout(wcToRemove.account);
								}
								_waitingClients.remove(wcToRemove);
							}
							break;
						}
						case 0x04:
						{
							final KickPlayer kp = new KickPlayer(incoming);
							doKickPlayer(kp.getAccount());
							break;
						}
						case 0x05:
						{
							final RequestCharacters rc = new RequestCharacters(incoming);
							getCharsOnServer(rc.getAccount());
							break;
						}
						case 0x06:
						{
							new ChangePasswordResponse(incoming);
							break;
						}
					}
				}
			}
			catch (UnknownHostException e)
			{
				LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": ", e);
			}
			catch (SocketException e)
			{
				LOGGER.warning(getClass().getSimpleName() + ": LoginServer not available, trying to reconnect...");
			}
			catch (IOException e)
			{
				LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Disconnected from Login, Trying to reconnect: ", e);
			}
			finally
			{
				try
				{
					_socket.close();
					if (isInterrupted())
					{
						return;
					}
				}
				catch (Exception e)
				{
					// Ignore.
				}
			}
			
			try
			{
				Thread.sleep(5000); // 5 seconds tempo.
			}
			catch (Exception e)
			{
				// Ignore.
			}
		}
	}
	
	/**
	 * Adds the waiting client and send request.
	 * @param accountName the account
	 * @param client the game client
	 * @param key the session key
	 */
	public void addWaitingClientAndSendRequest(String accountName, GameClient client, SessionKey key)
	{
		synchronized (_waitingClients)
		{
			_waitingClients.add(new WaitingClient(accountName, client, key));
		}
		
		sendPacket(new PlayerAuthRequest(accountName, key));
	}
	
	/**
	 * Removes the waiting client.
	 * @param client the client
	 */
	public void removeWaitingClient(GameClient client)
	{
		WaitingClient toRemove = null;
		synchronized (_waitingClients)
		{
			for (WaitingClient c : _waitingClients)
			{
				if (c.gameClient == client)
				{
					toRemove = c;
					break;
				}
			}
			if (toRemove != null)
			{
				_waitingClients.remove(toRemove);
			}
		}
	}
	
	/**
	 * Send logout for the given account.
	 * @param account the account
	 */
	public void sendLogout(String account)
	{
		if (account == null)
		{
			return;
		}
		
		_accountsInGameServer.remove(account);
		sendPacket(new PlayerLogout(account));
	}
	
	/**
	 * Adds the game server login.
	 * @param account the account
	 * @param client the client
	 * @return {@code true} if account was not already logged in, {@code false} otherwise
	 */
	public boolean addGameServerLogin(String account, GameClient client)
	{
		return _accountsInGameServer.putIfAbsent(account, client) == null;
	}
	
	/**
	 * Send access level.
	 * @param account the account
	 * @param level the access level
	 */
	public void sendAccessLevel(String account, int level)
	{
		sendPacket(new ChangeAccessLevel(account, level));
	}
	
	/**
	 * Send client tracert.
	 * @param account the account
	 * @param address the address
	 */
	public void sendClientTracert(String account, String[] address)
	{
		sendPacket(new PlayerTracert(account, address[0], address[1], address[2], address[3], address[4]));
	}
	
	/**
	 * Send mail.
	 * @param account the account
	 * @param mailId the mail id
	 * @param args the args
	 */
	public void sendMail(String account, String mailId, String... args)
	{
		sendPacket(new SendMail(account, mailId, args));
	}
	
	/**
	 * Send temp ban.
	 * @param account the account
	 * @param ip the ip
	 * @param time the time
	 */
	public void sendTempBan(String account, String ip, long time)
	{
		sendPacket(new TempBan(account, ip, time));
	}
	
	/**
	 * Hex to string.
	 * @param hex the hex value
	 * @return the hex value as string
	 */
	private String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
	
	/**
	 * Kick player for the given account.
	 * @param account the account
	 */
	private void doKickPlayer(String account)
	{
		final GameClient client = _accountsInGameServer.get(account);
		if (client != null)
		{
			final Player player = client.getPlayer();
			if (client.isDetached())
			{
				if (player != null)
				{
					player.deleteMe();
				}
				
				client.close(new SystemMessage(SystemMessageId.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT));
			}
			else
			{
				if (player != null)
				{
					player.sendPacket(SystemMessageId.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
				}
				
				Disconnection.of(client).defaultSequence(ServerClose.STATIC_PACKET);
				ACCOUNTING_LOGGER.info("Kicked by login, " + client);
			}
		}
		sendLogout(account);
	}
	
	/**
	 * Gets the chars on server.
	 * @param account the account
	 */
	private void getCharsOnServer(String account)
	{
		int chars = 0;
		final List<Long> charToDel = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT deletetime FROM characters WHERE account_name=?"))
		{
			ps.setString(1, account);
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					chars++;
					final long delTime = rs.getLong("deletetime");
					if (delTime != 0)
					{
						charToDel.add(delTime);
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Exception: getCharsOnServer: " + e.getMessage(), e);
		}
		
		sendPacket(new ReplyCharacters(account, chars, charToDel));
	}
	
	/**
	 * Send packet.
	 * @param packet the sendable packet
	 */
	private void sendPacket(BaseWritablePacket packet)
	{
		if ((_blowfish == null) || (_socket == null) || _socket.isClosed())
		{
			return;
		}
		
		try
		{
			packet.write(); // Write initial data.
			packet.writeInt(0); // Reserved for checksum.
			int size = packet.getLength() - 2; // Size without header.
			final int padding = size % 8; // Padding of 8 bytes.
			if (padding != 0)
			{
				for (int i = padding; i < 8; i++)
				{
					packet.writeByte(0);
				}
			}
			
			// Size header + encrypted[data + checksum (int) + padding].
			final byte[] data = packet.getSendableBytes();
			
			// Encrypt.
			size = data.length - 2; // Data size without header.
			
			synchronized (_out)
			{
				NewCrypt.appendChecksum(data, 2, size);
				_blowfish.crypt(data, 2, size);
				
				_out.write(data);
				try
				{
					_out.flush();
				}
				catch (IOException e)
				{
					// LoginServer might have terminated.
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.severe("LoginServerThread: IOException while sending packet " + packet.getClass().getSimpleName());
			LOGGER.severe(TraceUtil.getStackTrace(e));
		}
	}
	
	/**
	 * Sets the max player.
	 * @param maxPlayer The maxPlayer to set.
	 */
	public void setMaxPlayer(int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		_maxPlayer = maxPlayer;
	}
	
	/**
	 * Gets the max player.
	 * @return Returns the maxPlayer.
	 */
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}
	
	/**
	 * Send server status.
	 * @param id the id
	 * @param value the value
	 */
	public void sendServerStatus(int id, int value)
	{
		final ServerStatus serverStatus = new ServerStatus();
		serverStatus.addAttribute(id, value);
		sendPacket(serverStatus);
	}
	
	/**
	 * Send Server Type Config to LS.
	 */
	public void sendServerType()
	{
		final ServerStatus serverStatus = new ServerStatus();
		serverStatus.addAttribute(ServerStatus.SERVER_TYPE, Config.SERVER_LIST_TYPE);
		sendPacket(serverStatus);
	}
	
	/**
	 * Send change password.
	 * @param accountName the account name
	 * @param charName the char name
	 * @param oldpass the old pass
	 * @param newpass the new pass
	 */
	public void sendChangePassword(String accountName, String charName, String oldpass, String newpass)
	{
		sendPacket(new ChangePassword(accountName, charName, oldpass, newpass));
	}
	
	public int getServerStatus()
	{
		return _status;
	}
	
	/**
	 * Gets the status string.
	 * @return the status string
	 */
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}
	
	/**
	 * Gets the server name.
	 * @return the server name.
	 */
	public String getServerName()
	{
		return _serverName;
	}
	
	/**
	 * Sets the server status.
	 * @param status the new server status
	 */
	public void setServerStatus(int status)
	{
		switch (status)
		{
			case ServerStatus.STATUS_AUTO:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_DOWN:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_FULL:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_GM_ONLY:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_GOOD:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_NORMAL:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				_status = status;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Status does not exists:" + status);
			}
		}
	}
	
	public GameClient getClient(String name)
	{
		return name != null ? _accountsInGameServer.get(name) : null;
	}
	
	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		
		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}
		
		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}
	
	private static class WaitingClient
	{
		public String account;
		public GameClient gameClient;
		public SessionKey sessionKey;
		
		public WaitingClient(String acc, GameClient client, SessionKey key)
		{
			account = acc;
			gameClient = client;
			sessionKey = key;
		}
	}
	
	public static LoginServerThread getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LoginServerThread INSTANCE = new LoginServerThread();
	}
}
