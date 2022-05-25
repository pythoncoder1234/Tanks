package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;
import tanks.tank.TankMimic;
import tanks.tank.TankRemote;

public class EventTankTransform extends PersonalEvent
{
    public int tank;

    public double red;
    public double green;
    public double blue;

    public double red2;
    public double green2;
    public double blue2;

    public double size;
    public double turretSize;
    public double turretLength;

    public String baseModel;
    public String colorModel;
    public String turretBaseModel;
    public String turretModel;

    public String texture;

    public static final int no_effect = 0;
    public static final int exclamation = 1;
    public static final int poof = 2;

    public int effect;

    public EventTankTransform()
    {

    }

    public EventTankTransform(Tank t, Tank newTank, int effect)
    {
        tank = t.networkID;

        this.red = newTank.colorR;
        this.green = newTank.colorG;
        this.blue = newTank.colorB;

        this.red2 = newTank.turret.colorR;
        this.green2 = newTank.turret.colorG;
        this.blue2 = newTank.turret.colorB;

        this.size = newTank.size;
        this.turretSize = newTank.turret.size;
        this.turretLength = newTank.turret.length;

        this.baseModel = newTank.baseModel.file;
        this.colorModel = newTank.colorModel.file;
        this.turretBaseModel = newTank.turretBaseModel.file;
        this.turretModel = newTank.turretModel.file;

        this.effect = effect;
        this.texture = newTank.texture;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(tank);

        if (this.clientID == null && t instanceof TankRemote)
        {
            t.size = size;
            t.turret.size = turretSize;
            t.turret.length = turretLength;

            t.baseModel = Drawing.drawing.createModel(baseModel);
            t.colorModel = Drawing.drawing.createModel(colorModel);
            t.turretBaseModel = Drawing.drawing.createModel(turretBaseModel);
            t.turretModel = Drawing.drawing.createModel(turretModel);

            t.texture = texture;

            if (effect == exclamation)
            {
                Effect e1 = Effect.createNewEffect(t.posX, t.posY, t.posZ + this.size * 0.75, Effect.EffectType.exclamation);
                e1.size = this.size;
                e1.colR = t.colorR;
                e1.colG = t.colorG;
                e1.colB = t.colorB;
                e1.glowR = this.red;
                e1.glowG = this.green;
                e1.glowB = this.blue;
                Game.effects.add(e1);
            }
            else if (effect == poof)
            {
                t.baseModel = TankMimic.base_model;
                t.turretBaseModel = TankMimic.turret_base_model;
                t.turretModel = TankMimic.turret_model;

                if (Game.effectsEnabled)
                {
                    for (int i = 0; i < 50 * Game.effectMultiplier; i++)
                    {
                        Effect e = Effect.createNewEffect(t.posX, t.posY, t.size / 4, Effect.EffectType.piece);
                        double var = 50;
                        e.colR = Math.min(255, Math.max(0, t.colorR + Math.random() * var - var / 2));
                        e.colG = Math.min(255, Math.max(0, t.colorG + Math.random() * var - var / 2));
                        e.colB = Math.min(255, Math.max(0, t.colorB + Math.random() * var - var / 2));

                        if (Game.enable3d)
                            e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, 1 + Math.random() * t.size / 50.0);
                        else
                            e.setPolarMotion(Math.random() * 2 * Math.PI, 1 + Math.random() * t.size / 50.0);

                        Game.effects.add(e);
                    }
                }
            }

            t.colorR = red;
            t.colorG = green;
            t.colorB = blue;

            t.turret.colorR = red2;
            t.turret.colorG = green2;
            t.turret.colorB = blue2;
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);

        b.writeDouble(this.red);
        b.writeDouble(this.green);
        b.writeDouble(this.blue);

        b.writeDouble(this.red2);
        b.writeDouble(this.green2);
        b.writeDouble(this.blue2);

        b.writeDouble(this.size);
        b.writeDouble(this.turretSize);
        b.writeDouble(this.turretLength);

        NetworkUtils.writeString(b, this.baseModel);
        NetworkUtils.writeString(b, this.colorModel);
        NetworkUtils.writeString(b, this.turretBaseModel);
        NetworkUtils.writeString(b, this.turretModel);

        NetworkUtils.writeString(b, this.texture);

        b.writeInt(this.effect);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();

        this.red = b.readDouble();
        this.green = b.readDouble();
        this.blue = b.readDouble();

        this.red2 = b.readDouble();
        this.green2 = b.readDouble();
        this.blue2 = b.readDouble();

        this.size = b.readDouble();
        this.turretSize = b.readDouble();
        this.turretLength = b.readDouble();

        this.baseModel = NetworkUtils.readString(b);
        this.colorModel = NetworkUtils.readString(b);
        this.turretBaseModel = NetworkUtils.readString(b);
        this.turretModel = NetworkUtils.readString(b);

        this.texture = NetworkUtils.readString(b);
        this.effect = b.readInt();
    }
}
