package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import basewindow.ShaderGroup;
import tanks.*;
import tanks.gui.screen.leveleditor.selector.SelectorGroupID;
import tanks.gui.screen.leveleditor.selector.LevelEditorSelector;
import tanks.gui.screen.leveleditor.selector.SelectorRotation;
import tanks.gui.screen.leveleditor.selector.SelectorStackHeight;
import tanks.gui.screen.ILevelPreviewScreen;
import tanks.rendering.ShaderGroundObstacle;
import tanks.rendering.ShaderObstacle;

import java.util.ArrayList;

public class Obstacle extends GameObject implements IDrawableForInterface, ISolidObject, IDrawableWithGlow, IBatchRenderableObject
{
	public static final int default_max_height = 8;

	public Effect.EffectType destroyEffect = Effect.EffectType.obstaclePiece;
	public double destroyEffectAmount = 1;

	public boolean destructible = true;
	public boolean tankCollision = true;
	public boolean bulletCollision = true;

	/**
	 * If set to true, AI tanks will treat this block as breakable and shoot at it if there are tanks behind it
	 */
	public boolean shouldShootThrough = false;

	/**
	 * If set to a non-negative value, will override how much AI controlled tanks will avoid pathfinding over this
	 */
	public int unfavorability = -1;

	/**
	 * Full = a full block, nothing can be placed underneath
	 * Ground = replaces the ground tile, can have blocks/tanks on top
	 * Top = can be placed on top of a ground tile, can have tanks inside
	 * Extra = can be placed anywhere without a full tile, can have tanks inside
	 * */
	public enum ObstacleType { full, ground, top, extra }
	public ObstacleType type = ObstacleType.full;

	public boolean enableStacking = true;
	public double stackHeight = 1;
	public double startHeight = 0;

	public boolean enableGroupID = false;
	public int groupID = 0;

	public int drawLevel = 5;

	public boolean checkForObjects = false;
	public boolean update = false;
	public boolean draggable = true;
	public boolean bouncy = false;
	public boolean allowBounce = true;
	public boolean replaceTiles = true;

	/**
	 * If set to true, will draw as a VBO. Set to false for simpler rendering of more dynamic obstacles.
	 */
	public boolean batchDraw = true;
	public Class<? extends ShaderGroup> renderer = ShaderObstacle.class;
	public Class<? extends ShaderGroup> tileRenderer = ShaderGroundObstacle.class;

	/** Obstacles with different render numbers can have different values for their uniforms */
	public int rendererNumber = 0;
	public int tileRendererNumber = 0;

	public double colorR;
	public double colorG;
	public double colorB;
	public double colorA = 255;
	public double glow = 0;

	public boolean enableRotation = false;
	public double rotation;

	public double[] stackColorR = new double[default_max_height];
	public double[] stackColorG = new double[default_max_height];
	public double[] stackColorB = new double[default_max_height];

	public static double draw_size = 0;
	public static double lastDrawSize = 0;

	public boolean removed = false;

	public String name;
	public String description;

	public Face[] horizontalFaces;
	public Face[] verticalFaces;

	protected boolean[] validFaces = new boolean[2];

	protected byte[] options = new byte[default_max_height];
	protected byte[] lastOptions = new byte[default_max_height];

	public double baseGroundHeight;

	public boolean shouldClip = false;
	public boolean collisionWhenClipped = true;
	public int clipFrames = 0;

	public Obstacle(String name, double posX, double posY)
	{
		this.name = name;
		this.posX = (int) ((posX + 0.5) * Game.tile_size);
		this.posY = (int) ((posY + 0.5) * Game.tile_size);
		double[] col = Obstacle.getRandomColor();
		this.colorR = col[0];
		this.colorG = col[1];
		this.colorB = col[2];

		for (int i = 0; i < default_max_height; i++)
		{
			double[] col2;

			if (i != 0)
				col2 = Obstacle.getRandomColor();
			else
				col2 = col;

			this.stackColorR[i] = col2[0];
			this.stackColorG[i] = col2[1];
			this.stackColorB[i] = col2[2];
		}

		this.baseGroundHeight = Game.sampleDefaultGroundHeight(this.posX, this.posY);
		this.description = "A solid block which can be destroyed by mines";
	}

	@Override
	public void draw()
	{
		if (this.stackHeight <= 0)
			return;

		Drawing drawing = Drawing.drawing;

		drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA, this.glow);

		if (Game.enable3d)
		{
			for (int i = 0; i < Math.min(this.stackHeight, default_max_height); i++)
			{
				int in = default_max_height - 1 - i;
				drawing.setColor(this.stackColorR[in], this.stackColorG[in], this.stackColorB[in], this.colorA, this.glow);

				byte option = 0;

				if (stackHeight % 1 == 0)
				{
					byte o = (byte) (option | this.getOptionsByte(((i + 1) + stackHeight % 1.0) * Game.tile_size));

					if (Game.game.window.drawingShadow || !Game.shadowsEnabled)
						options[i] = o;

					drawing.fillBox(this, this.posX, this.posY, i * Game.tile_size + this.startHeight * Game.tile_size, draw_size, draw_size, draw_size, o);
				}
				else
				{
					byte o = (byte) (option | this.getOptionsByte((i + stackHeight % 1.0) * Game.tile_size));

					if (Game.game.window.drawingShadow || !Game.shadowsEnabled)
						options[i] = o;

					drawing.fillBox(this, this.posX, this.posY, (i - 1 + stackHeight % 1.0) * Game.tile_size + this.startHeight * Game.tile_size, draw_size, draw_size, draw_size, o);
				}
			}
		}
		else
			drawing.fillRect(this, this.posX, this.posY, draw_size, draw_size);
	}

	@Override
	public void drawGlow()
	{

	}

	@Override
	public boolean isGlowEnabled()
	{
		return false;
	}

	@Override
	public void drawAt(double x, double y)
	{
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.draw();
		this.posX = x1;
		this.posY = y1;
	}

	@Override
	public void drawForInterface(double x, double y)
	{
		Drawing drawing = Drawing.drawing;

		drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
		drawing.fillInterfaceRect(x, y, draw_size, draw_size);
	}

	public void drawOutlineAt(double x, double y)
	{
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.drawOutline();
		this.posX = x1;
		this.posY = y1;
	}

	public void drawOutline()
	{
		drawOutline(this.colorR, this.colorG, this.colorB, this.colorA);
	}

	public void drawOutline(double r, double g, double b, double a)
	{
		Drawing.drawing.setColor(r, g, b, a);
		Drawing.drawing.drawRect(this.posX, this.posY, draw_size, draw_size, Game.tile_size * 0.2);
	}

	public void draw3dOutline()
	{
		draw3dOutline(this.colorR, this.colorG, this.colorB, 128);
	}

	public void draw3dOutline(double r, double g, double b)
	{
		draw3dOutline(r, g, b, 128);
	}

	public void draw3dOutline(double r, double g, double b, double a)
	{
		if (!Game.enable3d)
		{
			drawOutline(r, g, b, a);
			return;
		}

		double sizeZ = this.stackHeight * Game.tile_size;
		if (!this.enableStacking)
			sizeZ = Game.screen instanceof ILevelPreviewScreen ? 15 : 50;

		Drawing.drawing.setColor(r, g, b, a, 0.5);
		Drawing.drawing.fillBox(this.posX, this.posY, this.startHeight * Game.tile_size,
				Game.tile_size + 1, Game.tile_size + 1, sizeZ + 1, this.getOptionsByte(this.getTileHeight()));
	}

	public void onObjectEntry(Movable m)
	{

	}

	/** Only for visual effects which are to be handled by each client separately*/
	public void onObjectEntryLocal(Movable m)
	{

	}

	public void afterAdd()
	{

	}

	public void update()
	{

	}

	public void onNeighborUpdate()
	{
		refreshHitboxes();
	}

	public void reactToHit(double bx, double by)
	{

	}

	public boolean hasNeighbor(int ox, int oy, boolean unbreakable)
	{
		int x = (int) (this.posX / Game.tile_size) + ox;
		int y = (int) (this.posY / Game.tile_size) + oy;

		if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
		{
			if (unbreakable)
				return Game.isUnbreakable(x, y);
            return Game.isSolid(x, y);
        }

		return false;
	}

	public boolean hasLeftNeighbor()
	{
		return hasNeighbor(-1, 0, false);
	}

	public boolean hasRightNeighbor()
	{
		return hasNeighbor(1, 0, false);
	}

	public boolean hasUpperNeighbor()
	{
		return hasNeighbor(0, -1, false);
	}

	public boolean hasLowerNeighbor()
	{
		return hasNeighbor(0, 1, false);
	}

	/**
	 * Draws the tile under the obstacle if it needs to be drawn differently than when not covered by an obstacle
	 *
	 * @param r Red
	 * @param g Green
	 * @param b Blue
	 * @param d Tile height
	 * @param extra The deepest tile next to the current tile, used to render sides underground
	 */
	public void drawTile(IBatchRenderableObject tile, double r, double g, double b, double d, double extra)
	{
		Drawing.drawing.setColor(r, g, b);
		Drawing.drawing.fillBox(tile, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, extra + d);
	}

	public void postOverride()
	{
		if (this.startHeight > 0)
			return;

		Game.setObstacle(posX, posY, this);
	}

	public String getMetadata()
	{
		StringBuilder s = new StringBuilder();

		int sc = this.selectorCount();

		for (int i = 0; i < sc; i++)
			s.append(this.selectors.get(saveOrder(i)).getMetadata()).append("-");

		if (this.startHeight > 0)
			s.append(this.startHeight);

		if (s.toString().endsWith("-"))
			return s.substring(0, s.length() - 1);
		return s.toString();
	}

	public void setMetadata(String s)
	{
		String[] metadata = s.split("-");

		for (int i = 0; i < Math.min(this.selectorCount(), metadata.length); i++)
		{
			LevelEditorSelector<Obstacle> sel = (LevelEditorSelector<Obstacle>) this.selectors.get(saveOrder(i));
			sel.setMetadata(metadata[i]);
		}

		try
		{
			if (metadata.length - this.selectorCount() == 1)
				this.startHeight = Double.parseDouble(metadata[metadata.length - 1]);
		}
		catch (Exception ignored) {}

		this.updateSelectors();
	}

	public static double[] getRandomColor()
	{
		double colorMul = Math.random() * 0.5 + 0.5;
		double[] col = new double[3];

		if (Game.fancyTerrain)
		{
			col[0] = (colorMul * (176 - Math.random() * 70));
			col[1] = (colorMul * (111 - Math.random() * 34));
			col[2] = (colorMul * 14);

		}
		else
			col = new double[]{87, 46, 8};

		return col;
	}

	@Override
	public Face[] getHorizontalFaces()
	{
		if (this.horizontalFaces == null)
		{
			this.horizontalFaces = new Face[2];
			double s = Game.tile_size / 2;
			this.horizontalFaces[0] = new Face(this, this.posX - s, this.posY - s, this.posX + s, this.posY - s, true, true, this.tankCollision, this.bulletCollision);
			this.horizontalFaces[1] = new Face(this, this.posX - s, this.posY + s, this.posX + s, this.posY + s, true, false, this.tankCollision, this.bulletCollision);
		}

		return this.horizontalFaces;
	}

	public boolean[] getValidHorizontalFaces(boolean unbreakable)
	{
		this.validFaces[0] = (!this.hasNeighbor(0, -1, unbreakable) || this.startHeight > 1) && (this.tankCollision || this.bulletCollision);
		this.validFaces[1] = (!this.hasNeighbor(0, 1, unbreakable) || this.startHeight > 1) && (this.tankCollision || this.bulletCollision);
		return this.validFaces;
	}

	@Override
	public Face[] getVerticalFaces()
	{
		if (this.verticalFaces == null)
		{
			this.verticalFaces = new Face[2];
			double s = Game.tile_size / 2;
			this.verticalFaces[0] = new Face(this, this.posX - s, this.posY - s, this.posX - s, this.posY + s, false, true, this.tankCollision, this.bulletCollision);
			this.verticalFaces[1] = new Face(this, this.posX + s, this.posY - s, this.posX + s, this.posY + s, false, false, this.tankCollision, this.bulletCollision);
		}

		return this.verticalFaces;
	}

	public boolean[] getValidVerticalFaces(boolean unbreakable)
	{
		this.validFaces[0] = (!this.hasNeighbor(-1, 0, unbreakable) || this.startHeight > 1) && (this.tankCollision || this.bulletCollision);
		this.validFaces[1] = (!this.hasNeighbor(1, 0, unbreakable) || this.startHeight > 1) && (this.tankCollision || this.bulletCollision);
		return this.validFaces;
	}

	/**
	 * @return height of tile in terms of drawing, for things like block particle collision
	 * */
	public double getTileHeight()
	{
		if (Obstacle.draw_size < Game.tile_size || this.startHeight > 1)
			return 0;

		return this.stackHeight * Game.tile_size;
	}

	/**
	 * @return how deep the edges of the tile span - for example, ice tiles go down to -15, but most tiles only go down to 0
	 */
	public double getEdgeDrawDepth()
	{
		return 0;
	}


	/**
	 * Returns height of tile in terms of where objects like mines or treads should be drawn on top of it
	 * */
	public double getGroundHeight()
	{
		return -1000;
	}

	public byte getOptionsByte(double h)
	{
		/* TODO: maybe re-implement pruning hidden obstacle faces, especially if adding obstacle grid

		byte o = 0;

		if (Obstacle.draw_size < Game.tile_size)
			return 0;

		if (Game.sampleObstacleHeight(this.posX, this.posY + Game.tile_size) >= h)
			o += 4;

		if (Game.sampleObstacleHeight(this.posX, this.posY - Game.tile_size) >= h)
			o += 8;

		if (Game.sampleObstacleHeight(this.posX - Game.tile_size, this.posY) >= h)
			o += 16;

		if (Game.sampleObstacleHeight(this.posX + Game.tile_size, this.posY) >= h)
			o += 32;

		return o;*/

		return 0;
	}

	public void onDestroy(Movable source)
	{
		Game.removeObstacles.add(this);
	}

	public void refreshHitboxes()
	{
		Chunk.FaceList f = Chunk.getChunk(posX, posY).staticFaces;
		f.removeFaces(this);
        if (tankCollision || bulletCollision)
            f.addFaces(this);
		afterAdd();
    }

	public ArrayList<Obstacle> getNeighbors()
	{
		ArrayList<Obstacle> neighbors = new ArrayList<>();
		for (int i = 0; i < 4; i++)
		{
			double newX = posX + Game.tile_size * Game.dirX[i];
			double newY = posY + Game.tile_size * Game.dirY[i];

			Obstacle o = Game.getObstacle(newX, newY);
			if (o != null)
				neighbors.add(o);
		}
		return neighbors;
	}

	public void playDestroyAnimation(double posX, double posY, double radius)
	{
		if (Game.effectsEnabled)
		{
			Effect.EffectType effect = this.destroyEffect;
			double freq = Math.min((Math.sqrt(Math.pow(posX - this.posX, 2) + Math.pow(posY - this.posY, 2)) + Game.tile_size * 2.5) / radius, 1);

			if (Game.enable3d)
			{
				if (effect == Effect.EffectType.obstaclePiece)
					effect = Effect.EffectType.obstaclePiece3d;

				double s = 12.5;
				for (double j = 0; j < Game.tile_size; j += s)
				{
					for (double k = 0; k < Game.tile_size; k += s)
					{
						for (double l = 0; l < Game.tile_size * this.stackHeight; l += s)
						{
							if (Math.random() > this.destroyEffectAmount * freq * freq * Game.effectMultiplier)
								continue;

							Effect e = Effect.createNewEffect(this.posX + j + s / 2 - Game.tile_size / 2, this.posY + k + s / 2 - Game.tile_size / 2, l, effect);

							int block = (int) ((this.stackHeight * Game.tile_size - (l + s)) / Game.tile_size);

							if (this.enableStacking)
							{
								e.colR = this.stackColorR[block];
								e.colG = this.stackColorG[block];
								e.colB = this.stackColorB[block];
							}
							else
							{
								e.colR = this.colorR;
								e.colG = this.colorG;
								e.colB = this.colorB;
							}

							double dist = Movable.distanceBetween(this, e);
							double angle = (Math.random() - 0.5) * 0.1 + Movable.getPolarDirection(e.posX - posX, e.posY - posY);
							double rad = radius - Game.tile_size / 2;
							double v = (rad * Math.sqrt(2) - dist) / (rad * 2);
							e.addPolarMotion(angle, (v + Math.random() * 2) * 1.5);
							e.vZ = 1.5 * (v + Math.random() * 2);

							Game.effects.add(e);
						}
					}
				}
			}
			else
			{
				for (int j = 0; j < Game.tile_size - 6; j += 4)
				{
					for (int k = 0; k < Game.tile_size - 6; k += 4)
					{
						if (Math.random() > this.destroyEffectAmount * freq * freq * Game.effectMultiplier)
							continue;

						Effect e = Effect.createNewEffect(this.posX + j + 5 - Game.tile_size / 2, this.posY + k + 5 - Game.tile_size / 2, effect);

						e.colR = this.colorR;
						e.colG = this.colorG;
						e.colB = this.colorB;

						double dist = Movable.distanceBetween(this, e);
						double angle = Movable.getPolarDirection(e.posX - posX, e.posY - posY);
						double rad = radius - Game.tile_size / 2;
						e.addPolarMotion(angle, (rad * Math.sqrt(2) - dist) / (rad * 2) + Math.random() * 2);

						Game.effects.add(e);
					}
				}
			}
		}
	}

	public void registerSelectors()
	{
		if (this.enableStacking)
			this.registerSelector(new SelectorStackHeight());

		if (this.enableGroupID)
			this.registerSelector(new SelectorGroupID());

		if (this.enableRotation)
			this.registerSelector(new SelectorRotation<Obstacle>());
	}

	public Effect getCompanionEffect()
	{
		return null;
	}

	public static boolean canPlaceOn(ObstacleType type, Chunk.Tile tile)
	{
		return tile != null && ((tile.obstacle == null || canPlaceOn(type, tile.obstacle.type))
				&& (tile.surfaceObstacle == null || canPlaceOn(type, tile.surfaceObstacle.type)));
	}

	public static boolean canPlaceOn(ObstacleType t1, ObstacleType t2)
	{
		if (t1 == ObstacleType.full || t2 == ObstacleType.full)
			return false;
		else if (t1 == ObstacleType.extra || t2 == ObstacleType.extra)
			return true;
		else
			return t1 != t2;
	}
}
