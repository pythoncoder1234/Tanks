package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.BulletFreeze;
import tanks.event.EventShootBullet;

import java.util.Arrays;

/**
 * A support tank which shoots freezing bullets that deal low damage
 */
public class TankCyan extends TankAIControlled
{
	public TankCyan(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 128, 255, 255, angle, ShootAI.straight);

		this.enableMovement = true;
		this.maxSpeed = 0.75;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.bullet.maxLiveBullets = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 240;
		this.aimTurretSpeed = 0.01;
		this.bullet.bounces = 0;
		this.bullet.effect = Bullet.BulletEffect.ice;
		this.bullet.speed = 25.0 / 8;
		this.bullet.damage = 0.25;
		this.bullet.bulletClass = BulletFreeze.class;
		this.bullet.name = "Freezing bullet";
		this.enableLookingAtTargetEnemy = false;
		this.motionChangeChance = 0.001;

		//TODO: attribute immunities
		this.attributeImmunities.addAll(Arrays.asList("ice_slip", "ice_accel", "ice_max_speed", "freeze"));
		
		this.coinValue = 4;

		this.description = "A support tank which shoots freezing---bullets that deal low damage";
	}
}
