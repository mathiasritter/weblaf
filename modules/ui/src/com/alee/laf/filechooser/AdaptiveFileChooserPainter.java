package com.alee.laf.filechooser;

import com.alee.painter.AdaptivePainter;
import com.alee.painter.Painter;

import javax.swing.*;

/**
 * Simple {@link FileChooserPainter} adapter class.
 * It is used to install simple non-specific painters into {@link WFileChooserUI}.
 *
 * @author Alexandr Zernov
 */

public final class AdaptiveFileChooserPainter<E extends JFileChooser, U extends WFileChooserUI> extends AdaptivePainter<E, U>
        implements IFileChooserPainter<E, U>
{
    /**
     * Constructs new {@link AdaptiveFileChooserPainter} for the specified painter.
     *
     * @param painter painter to adapt
     */
    public AdaptiveFileChooserPainter ( final Painter painter )
    {
        super ( painter );
    }
}