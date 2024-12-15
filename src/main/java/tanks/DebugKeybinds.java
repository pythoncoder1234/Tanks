package tanks;

import basewindow.BaseWindow;
import basewindow.InputCodes;
import basewindow.ShaderGroup;
import tanks.gui.ChatMessage;
import tanks.gui.ScreenElement;
import tanks.gui.screen.ScreenCrusadeDetails;
import tanks.gui.screen.ScreenOptions;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.obstacle.Obstacle;
import tanks.rendering.TerrainRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tanks.Panel.notifs;

public class DebugKeybinds
{
    public static void update()
    {
        if (!Game.game.window.pressedKeys.contains(InputCodes.KEY_F3))
            return;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_1))
        {
            double mx = Game.game.window.absoluteMouseX, my = Game.game.window.absoluteMouseY;
            Chunk.getTileOptional(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY()).ifPresent(t ->
            {
                if (t.obstacle == null)
                    return;

                int brightness = 0;
                if (Game.currentLevel != null && Level.isDark())
                    brightness = 255;

                Drawing.drawing.setColor(brightness, brightness, brightness);
                Drawing.drawing.setInterfaceFontSize(16);
                Game.game.window.fontRenderer.drawString(mx + 10, my + 30, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                        "M: " + t.obstacle.getMetadata());
            });
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_P))
        {
            Panel.pauseOnDefocus = !Panel.pauseOnDefocus;
            notifs.add(new ScreenElement.Notification("Pause on lost focus: \u00a7255200000255"
                    + (Panel.pauseOnDefocus ? "enabled" : "disabled")).setColor(255, 255, 128));
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_P);
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_B))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_B);
            Game.showHitboxes = !Game.showHitboxes;
            notifs.add(new ScreenElement.Notification("Collision boxes: \u00a7255200000255"
                    + (Game.showHitboxes ? "shown" : "hidden"), 200).setColor(255, 255, 128));
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_R))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_R);
            if (Game.currentLevel != null)
                Game.currentLevel.reloadTiles();
            notifs.add(new ScreenElement.Notification(Game.currentLevel != null ? "Reloaded tiles" : "Reload tiles failed: " +
                    "Game.\u00a7200125255255currentLevel\u00a7255255255255 = \u00a7255128128255null",
                    200).setColor(255, 255, 128));
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_G))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_G);
            Chunk.debug = !Chunk.debug;
            notifs.add(new ScreenElement.Notification("Chunk borders: \u00a7255200000255"
                    + (Chunk.debug ? "shown" : "hidden"), 200).setColor(255, 255, 128));
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_K))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_K);
            Function<List<Integer>, List<String>> func = l -> l.stream().map(Game.game.window::getKeyText).collect(Collectors.toList());;
            System.out.println("Game.screen = " + Game.screen.getClass().getSimpleName());
            System.out.println("pressedKeys: " + func.apply(Game.game.window.pressedKeys));
            System.out.println("validPressedKeys: " + func.apply(Game.game.window.validPressedKeys));
            System.out.println("textPressedKeys: " + func.apply(Game.game.window.textPressedKeys));
            notifs.add(new ScreenElement.Notification("Pressed keys have been logged to the console", 300).setColor(255, 255, 128));
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_BRACKET))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_LEFT_BRACKET);
            Panel.tickSprint = !Panel.tickSprint;

            if (Panel.tickSprint)
            {
                Game.vsync = false;
                Game.maxFPS = 0;
                Game.game.window.setVsync(false);
                Panel.currentMessage = new ScreenElement.CenterMessage("Game sprinting");
            }
            else
            {
                ScreenOptions.loadOptions(Game.homedir);
                Game.game.window.setVsync(Game.vsync);
                Panel.currentMessage = new ScreenElement.CenterMessage("Sprinting stopped");
            }
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_D))
        {
            ArrayList<ChatMessage> chat = null;
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_D);

            if (ScreenPartyLobby.isClient)
                chat = ScreenPartyLobby.chat;
            else if (ScreenPartyHost.isServer)
                chat = ScreenPartyHost.chat;

            if (chat != null)
            {
                synchronized (chat)
                {
                    chat.clear();
                }
                notifs.add(new ScreenElement.Notification("Chat cleared", 200).setColor(255, 255, 128));
            }
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_A))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_A);

            if (!(Game.screen instanceof ScreenCrusadeDetails))
            {
                Drawing.drawing.terrainRenderer.reset();
                notifs.add(new ScreenElement.Notification("Terrain reloaded!").setColor(255, 255, 128));
            }
            else
                notifs.add(new ScreenElement.Notification("F3+A doesn't work here!").setColor(255, 200, 128));
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_T))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_T);

            HashMap<Class<? extends ShaderGroup>, ShaderGroup> newShaders = new HashMap<>();
            for (Map.Entry<Class<? extends ShaderGroup>, ShaderGroup> entry : Game.game.shaderInstances.entrySet())
            {
                try
                {
                    ShaderGroup s;
                    try
                    {
                        s = entry.getKey().getConstructor(BaseWindow.class)
                                .newInstance(Game.game.window);
                    }
                    catch (NoSuchMethodException e)
                    {
                        s = entry.getKey().getConstructor(BaseWindow.class, String.class)
                                .newInstance(Game.game.window, entry.getValue().name);
                    }

                    s.initialize();
                    newShaders.put(entry.getKey(), s);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            Game.game.shaderInstances = newShaders;
            Drawing.drawing.terrainRenderer.reset();
            notifs.add(new ScreenElement.Notification("Shaders reloaded! (Remember to rebuild)").setColor(255, 255, 128));
        }

        int brightness = 0;
        if (Game.currentLevel != null && Level.isDark())
            brightness = 255;

        Drawing.drawing.setColor(brightness, brightness, brightness);
        Drawing.drawing.setInterfaceFontSize(16);

        double mx = Game.game.window.absoluteMouseX, my = Game.game.window.absoluteMouseY;

        String text;
        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_P))
        {
            text = "(" + (int) Game.game.window.absoluteWidth + ", " + (int) Game.game.window.absoluteHeight + ")";
        }
        else if (Game.game.window.pressedKeys.contains(InputCodes.KEY_S))
        {
            if (Game.game.window.shift)
                text = "(" + (int) (mx - Game.screen.getOffsetX()) + ", " + (int) (my - Game.screen.getOffsetY()) + ")  " + Drawing.drawing.interfaceScale + ", " + Drawing.drawing.interfaceScaleZoom;
            else
                text = "(" + Math.round(Drawing.drawing.getMouseX()) + ", " + Math.round(Drawing.drawing.getMouseY()) + ")";
        }
        else
        {
            int posX = (int) (((Math.round(Drawing.drawing.getMouseX() / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2) - 25) / 50);
            int posY = (int) (((Math.round(Drawing.drawing.getMouseY() / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2) - 25) / 50);

            if (Game.screen instanceof ScreenLevelEditor) {
                posX = (int) (((ScreenLevelEditor) Game.screen).mouseObstacle.posX / Game.tile_size - 0.5);
                posY = (int) (((ScreenLevelEditor) Game.screen).mouseObstacle.posY / Game.tile_size - 0.5);
            }

            text = "P: (" + posX + ", " + posY + ")";

            if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
            {
                Chunk c = Chunk.getChunk(posX, posY, true);
                Chunk.Tile t1 = Chunk.getTile(posX, posY);

                if (c != null)
                    text += " C: (" + c.chunkX + ", " + c.chunkY + ")";

                if (t1 != null)
                {
                    if (mx > Drawing.drawing.getInterfaceEdgeX(true) - 200)
                        mx -= 200;
                    if (my > Drawing.drawing.getInterfaceEdgeY(true) - 100)
                        my -= 100;

                    if (Level.isDark() && t1.obstacle != null)
                    {
                        Obstacle o = t1.obstacle;
                        if ((o.colorR + o.colorG + o.colorB + o.colorA / 2) / 4 > 200)
                            Drawing.drawing.setColor(0, 0, 0, 128);
                    }

                    Game.game.window.fontRenderer.drawString(mx + 10, my + 30, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                            "O: " + (t1.obstacle != null ? t1.obstacle.name : "none") + " SO: " + (t1.surfaceObstacle != null ? t1.surfaceObstacle.name : "none")
                                    + " E: " + (t1.extraObstacle != null ? t1.extraObstacle.name : "none"));
                    Game.game.window.fontRenderer.drawString(mx + 10, my + 50, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                            "H: " + (int) t1.height() + " GH+D: " + (int) (t1.groundHeight() + t1.depth) + " E: " + (int) TerrainRenderer.getExtra(posX, posY, t1.obstacle));
                    Game.game.window.fontRenderer.drawString(mx + 10, my + 70, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                            "S: " + t1.solid() + " U: " + t1.unbreakable());
                }
            }
        }

        Game.game.window.fontRenderer.drawString(mx + 10, my + 10, Drawing.drawing.fontSize, Drawing.drawing.fontSize, text);
    }
}