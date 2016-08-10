/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alee.laf.scroll;

import com.alee.api.data.Corner;
import com.alee.extended.canvas.WebCanvas;
import com.alee.laf.WebLookAndFeel;
import com.alee.managers.style.*;
import com.alee.painter.DefaultPainter;
import com.alee.painter.Painter;
import com.alee.painter.PainterSupport;
import com.alee.utils.LafUtils;
import com.alee.utils.swing.DataRunnable;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollPaneUI;
import java.awt.*;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom UI for {@link JScrollPane} component.
 *
 * @author Mikle Garin
 */

public class WebScrollPaneUI extends BasicScrollPaneUI implements ShapeSupport, MarginSupport, PaddingSupport
{
    /**
     * Component painter.
     */
    @DefaultPainter ( ScrollPanePainter.class )
    protected IScrollPanePainter painter;

    /**
     * Listeners.
     */
    protected PropertyChangeListener propertyChangeListener;
    protected ContainerAdapter viewListener;

    /**
     * Runtime variables.
     */
    protected Insets margin = null;
    protected Insets padding = null;
    protected Map<Corner, JComponent> cornersCache = new HashMap<Corner, JComponent> ( 4 );

    /**
     * Returns an instance of the WebScrollPaneUI for the specified component.
     * This tricky method is used by UIManager to create component UIs when needed.
     *
     * @param c component that will use UI instance
     * @return instance of the WebScrollPaneUI
     */
    @SuppressWarnings ( "UnusedParameters" )
    public static ComponentUI createUI ( final JComponent c )
    {
        return new WebScrollPaneUI ();
    }

    /**
     * Installs UI in the specified component.
     *
     * @param c component for this UI
     */
    @Override
    public void installUI ( final JComponent c )
    {
        // Installing UI
        super.installUI ( c );

        // Scroll bars styling
        StyleId.scrollpaneViewport.at ( scrollpane ).set ( scrollpane.getViewport () );
        StyleId.scrollpaneVerticalBar.at ( scrollpane ).set ( scrollpane.getVerticalScrollBar () );
        StyleId.scrollpaneHorizontalBar.at ( scrollpane ).set ( scrollpane.getHorizontalScrollBar () );

        // Updating scrollpane corner
        updateCorners ();

        // Viewport listener
        viewListener = new ContainerAdapter ()
        {
            @Override
            public void componentAdded ( final ContainerEvent e )
            {
                removeCorners ();
                updateCorners ();
            }

            @Override
            public void componentRemoved ( final ContainerEvent e )
            {
                removeCorners ();
                updateCorners ();
            }
        };
        final JViewport viewport = scrollpane.getViewport ();
        if ( viewport != null )
        {
            viewport.addContainerListener ( viewListener );
        }

        // Property change listener
        propertyChangeListener = new PropertyChangeListener ()
        {
            @Override
            public void propertyChange ( final PropertyChangeEvent evt )
            {
                final String property = evt.getPropertyName ();
                if ( property.equals ( WebLookAndFeel.COMPONENT_ORIENTATION_PROPERTY ) )
                {
                    // Simply updating corners
                    removeCorners ();
                    updateCorners ();
                }
                else if ( property.equals ( WebLookAndFeel.VIEWPORT_PROPERTY ) )
                {
                    // Updating old viewport style and removing listener
                    if ( evt.getOldValue () != null )
                    {
                        final JViewport viewport = ( JViewport ) evt.getOldValue ();
                        viewport.removeContainerListener ( viewListener );
                        StyleId.viewport.set ( viewport );
                    }

                    // Updating new viewport style and adding listener
                    if ( evt.getNewValue () != null )
                    {
                        final JViewport viewport = ( JViewport ) evt.getNewValue ();
                        viewport.addContainerListener ( viewListener );
                        StyleId.scrollpaneViewport.at ( scrollpane ).set ( scrollpane.getViewport () );
                    }

                    // Updating corners
                    removeCorners ();
                    updateCorners ();
                }
                else if ( property.equals ( WebLookAndFeel.VERTICAL_SCROLLBAR_PROPERTY ) )
                {
                    final JScrollBar vsb = scrollpane.getVerticalScrollBar ();
                    if ( vsb != null )
                    {
                        StyleId.scrollpaneVerticalBar.at ( scrollpane ).set ( vsb );
                    }
                }
                else if ( property.equals ( WebLookAndFeel.HORIZONTAL_SCROLLBAR_PROPERTY ) )
                {
                    final JScrollBar hsb = scrollpane.getHorizontalScrollBar ();
                    if ( hsb != null )
                    {
                        StyleId.scrollpaneHorizontalBar.at ( scrollpane ).set ( hsb );
                    }
                }
            }
        };
        scrollpane.addPropertyChangeListener ( propertyChangeListener );

        // Applying skin
        StyleManager.installSkin ( scrollpane );
    }

    /**
     * Uninstalls UI from the specified component.
     *
     * @param c component with this UI
     */
    @Override
    public void uninstallUI ( final JComponent c )
    {
        // Uninstalling applied skin
        StyleManager.uninstallSkin ( scrollpane );

        // Cleaning up listeners
        scrollpane.removePropertyChangeListener ( propertyChangeListener );

        // Removing listener and custom corners
        removeCorners ();

        // Uninstalling UI
        super.uninstallUI ( c );
    }

    @Override
    public Shape getShape ()
    {
        return PainterSupport.getShape ( scrollpane, painter );
    }

    @Override
    public Insets getMargin ()
    {
        return margin;
    }

    @Override
    public void setMargin ( final Insets margin )
    {
        this.margin = margin;
        PainterSupport.updateBorder ( getPainter () );
    }

    @Override
    public Insets getPadding ()
    {
        return padding;
    }

    @Override
    public void setPadding ( final Insets padding )
    {
        this.padding = padding;
        PainterSupport.updateBorder ( getPainter () );
    }

    /**
     * Returns panel painter.
     *
     * @return panel painter
     */
    public Painter getPainter ()
    {
        return PainterSupport.getAdaptedPainter ( painter );
    }

    /**
     * Sets scroll pane painter.
     * Pass null to remove scroll pane painter.
     *
     * @param painter new scroll pane painter
     */
    public void setPainter ( final Painter painter )
    {
        PainterSupport.setPainter ( scrollpane, new DataRunnable<IScrollPanePainter> ()
        {
            @Override
            public void run ( final IScrollPanePainter newPainter )
            {
                WebScrollPaneUI.this.painter = newPainter;
            }
        }, this.painter, painter, IScrollPanePainter.class, AdaptiveScrollPanePainter.class );
    }

    /**
     * Updates custom scrollpane corners.
     */
    protected void updateCorners ()
    {
        final ScrollPaneCornerProvider provider = getScrollCornerProvider ();
        for ( final Corner type : Corner.values () )
        {
            JComponent corner = cornersCache.get ( type );
            if ( corner == null )
            {
                if ( provider != null )
                {
                    corner = provider.getCorner ( type );
                }
                if ( corner == null )
                {
                    if ( type == Corner.lowerLeading || type == Corner.lowerTrailing || type == Corner.upperTrailing )
                    {
                        corner = new WebCanvas ( StyleId.scrollpaneCorner.at ( scrollpane ), type.name () );
                    }
                }
            }
            if ( corner != null )
            {
                cornersCache.put ( type, corner );
                scrollpane.setCorner ( type.getScrollPaneConstant (), corner );
            }
        }
    }

    /**
     * Returns scroll corner provider.
     *
     * @return scroll corner provider
     */
    protected ScrollPaneCornerProvider getScrollCornerProvider ()
    {
        ScrollPaneCornerProvider scp = null;
        if ( scrollpane.getViewport () != null && scrollpane.getViewport ().getView () != null )
        {
            final Component view = scrollpane.getViewport ().getView ();
            if ( view instanceof ScrollPaneCornerProvider )
            {
                scp = ( ScrollPaneCornerProvider ) view;
            }
            else
            {
                final ComponentUI ui = LafUtils.getUI ( view );
                if ( ui != null && ui instanceof ScrollPaneCornerProvider )
                {
                    scp = ( ScrollPaneCornerProvider ) ui;
                }
            }
        }
        return scp;
    }

    /**
     * Removes custom scrollpane corners.
     */
    protected void removeCorners ()
    {
        // We do not remove corners by types here by components directly
        // This is required since internal types will be shifted upon component orientation change
        for ( final JComponent corner : cornersCache.values () )
        {
            scrollpane.remove ( corner );
        }
        cornersCache.clear ();
    }

    @Override
    public void paint ( final Graphics g, final JComponent c )
    {
        if ( painter != null )
        {
            painter.paint ( ( Graphics2D ) g, Bounds.component.of ( c ), c, this );
        }
    }

    @Override
    public Dimension getPreferredSize ( final JComponent c )
    {
        // return PainterSupport.getPreferredSize ( c, super.getPreferredSize ( c ), painter );
        return null;
    }
}