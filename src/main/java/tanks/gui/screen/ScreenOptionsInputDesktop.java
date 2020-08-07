package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

public class ScreenOptionsInputDesktop extends Screen
{
    public static final String mouseTargetText = "Mouse target: ";

    public static ScreenOverlayControls overlay = new ScreenOverlayControls();

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptions();
        }
    }
    );

    Button mouseTarget = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Panel.showMouseTarget = !Panel.showMouseTarget;

            if (Panel.showMouseTarget)
                mouseTarget.text = mouseTargetText + ScreenOptions.onText;
            else
                mouseTarget.text = mouseTargetText + ScreenOptions.offText;

            Game.game.window.setShowCursor(!Panel.showMouseTarget);
        }
    },
            "When enabled, your mouse pointer---will be replaced by a target");

    Button controls = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Controls", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = ScreenOverlayControls.lastControlsScreen;
        }
    }
    );

    public ScreenOptionsInputDesktop()
    {
        this.music = "tomato_feast_1.ogg";
        this.musicID = "menu";

        if (Panel.showMouseTarget)
            mouseTarget.text = mouseTargetText + ScreenOptions.onText;
        else
            mouseTarget.text = mouseTargetText + ScreenOptions.offText;
    }

    @Override
    public void update()
    {
        mouseTarget.update();
        back.update();
        controls.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        mouseTarget.draw();
        back.draw();
        controls.draw();

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Input options and controls");
    }

}
