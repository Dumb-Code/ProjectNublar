package net.dumbcode.projectnublar.client.gui.tablet;

import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;

public interface BackgroundableScreen {
    void setBackground(TabletBackground background);
    TabletBackground getBackground();
}
