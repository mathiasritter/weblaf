package com.alee.laf.filechooser;

import com.alee.painter.SpecificPainter;

import javax.swing.*;

/**
 * Base interface for {@link JFileChooser} component painters.
 *
 * @param <E> component type
 * @param <U> component UI type
 * @author Alexandr Zernov
 */

public interface IFileChooserPainter<E extends JFileChooser, U extends WFileChooserUI> extends SpecificPainter<E, U>
{
}