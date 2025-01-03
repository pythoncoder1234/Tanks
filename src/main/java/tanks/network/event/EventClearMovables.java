package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;
import tanks.tank.Crate;
import tanks.tank.TankPlayerController;
import tanks.tank.TankRemote;

public class EventClearMovables extends PersonalEvent
{

    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            for (Movable m : Game.movables)
            {
                if (m instanceof Crate && ((((Crate) m).tank instanceof TankRemote && ((Crate) m).tank.name.equals("player")) || ((Crate) m).tank instanceof TankPlayerController))
                    continue;

                m.destroy = true;
            }
        }
    }
}
