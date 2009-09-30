package com.dbschools.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class TextUtil {
    public static Dimension getTextDimensions(final String text, final Font font) {
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
        TextLayout layout = new TextLayout(text, font, frc);
        final Rectangle2D bounds = layout.getBounds();
        final double fudge = 1.2; // TODO find a cleaner way to do this
        final Dimension dimension = new Dimension((int) (bounds.getWidth() * fudge), 
                        (int) (bounds.getHeight() * fudge));
        return dimension;
    }
}