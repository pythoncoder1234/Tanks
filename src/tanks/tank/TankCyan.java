package tanks.tank;

import java.awt.Color;

import tanks.Bullet;
import tanks.BulletFreeze;
import tanks.Drawing;
import tanks.Game;

public class TankCyan extends TankAIControlled
{
	public TankCyan(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(128, 255, 255), angle, ShootAI.straight);

		this.enableMovement = true;
		this.speed = 1;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 240;
		this.aimTurretSpeed = 0.01;
		this.bulletBounces = 0;
		this.bulletEffect = Bullet.BulletEffect.ice;
		this.bulletSpeed = 25.0 / 4;
		this.bulletDamage = 0.25;
		this.enableLookingAtTargetEnemy = false;
		this.motionChangeChance = 0.001;
		
		this.coinValue = 5;
	}
	
	/** Actually fire a bullet*/
	public void launchBullet(double offset)
	{
		Drawing.playSound("resources/shoot.wav");

		Bullet b = new BulletFreeze(this.posX, this.posY, this.bulletBounces, this);
		b.setPolarMotion(angle + offset, this.bulletSpeed);
		b.moveOut((int) (25 / this.bulletSpeed * 2 * this.size / Game.tank_size));
		b.effect = this.bulletEffect;
		b.size = this.bulletSize;
		b.damage = this.bulletDamage;

		Game.movables.add(b);
		this.cooldown = (int) (Math.random() * this.cooldownRandom + this.cooldownBase);

		if (this.shootAIType.equals(ShootAI.alternate))
			this.straightShoot = !this.straightShoot;
	}


}
