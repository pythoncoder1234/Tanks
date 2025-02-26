package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenKicked;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.Client;
import tanks.network.NetworkEventMap;
import tanks.network.NetworkUtils;

public class EventKick extends PersonalEvent
{	
	public String reason;
	
	public EventKick()
	{
		
	}
	
	public EventKick(String reason)
	{
		this.reason = reason;
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
		{
			Game.cleanUp();
			ScreenPartyLobby.isClient = false;
			System.err.println(reason);
			System.err.println("Last event: " + NetworkEventMap.get(Client.handler.reader.lastID).getSimpleName());

			Client.handler.close();

			if (!(Game.screen instanceof ScreenKicked))
				Game.screen = new ScreenKicked(reason);
		}
	}

	@Override
	public void write(ByteBuf b)
	{
		NetworkUtils.writeString(b, this.reason);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.reason = NetworkUtils.readString(b);
	}
}
