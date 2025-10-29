/*
 * Copyright 2002 - 2008 JEuclid, http://jeuclid.sf.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package net.sourceforge.jeuclid.swing;

import net.sourceforge.jeuclid.DOMBuilder;
import net.sourceforge.jeuclid.MathMLParserSupport;
import net.sourceforge.jeuclid.MathMLSerializer;
import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.elements.generic.DocumentElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Displays MathML content in a Swing Component.
 * <p>
 * There are two properties which expose the actual content, accessible though
 * {@link #getDocument()} / {@link #setDocument(org.w3c.dom.Node)} for content
 * already available as a DOM model, and {@link #getContent()} and
 * {@link #setContent(String)} for content available as a String.
 * <p>
 * This class exposes most of the rendering parameters as standard bean
 * attributes. If you need to set additional attributes, you may use the
 * {@link #setParameter(Parameter, Object)} function.
 * <p>
 * Please use only the attributes exposed through the attached
 * {@link JMathComponentBeanInfo} class. Additional attributes, such as
 * {@link #getFont()} and {@link #setFont(Font)} are provided for Swing
 * compatibility, but they may not work exactly as expected.
 *
 * @see net.sourceforge.jeuclid.awt.MathComponent
 * @version $Revision$
 */
public final class JMathComponent extends JComponent implements
        SwingConstants {

    private static final String FONT_SEPARATOR = ",";

    /**
     * Logger for this class
     */
    private static final Log LOGGER = LogFactory.getLog(JMathComponent.class);

    /** */
    private static final long serialVersionUID = 1L;

    private Node document;

    private int horizontalAlignment = SwingConstants.CENTER;

    private final MutableLayoutContext parameters = new LayoutContextImpl(
            LayoutContextImpl.getDefaultLayoutContext());

    private int verticalAlignment = SwingConstants.CENTER;

    /**
     * cursor listener instance.
     */
    private final CursorListener cursorListener;

    /**
     * Default constructor.
     */
    public JMathComponent() {
        this(null);
    }

    /**
     * Default constructor with cursor listener.
     *
     * @param listener
     *            cursor listener instance
     */
    public JMathComponent(final CursorListener listener) {
        this.cursorListener = listener;

        if (listener != null) {
            final JMathComponentMouseListener mouseListener = new JMathComponentMouseListener(
                this);
            this.addMouseListener(mouseListener);
        }

        this.updateUI();
        this.fontCompat();
        this.setDocument(new DocumentElement());
    }

    /**
     * gets cursor listener instance.
     *
     * @return cursor listener instance
     */
    public CursorListener getCursorListener() {
        return this.cursorListener;
    }

    /**
     * Provide compatibility for standard get/setFont() operations.
     */
    private void fontCompat() {
        final String fontName = this.getFontsSerif().split(
                JMathComponent.FONT_SEPARATOR)[0];
        final float fontSize = this.getFontSize();
        super.setFont(new Font(fontName, 0, (int) fontSize));
    }

    /**
     * Tries to return the content as a String.
     * <p>
     * This transforms the internal DOM tree back into a string, which may is
     * not guaranteed to be the literally same as the original content.
     * However, it will represent the same XML document.
     *
     * @return the content string.
     */
    public String getContent() {
        return MathMLSerializer.serializeDocument(this.getDocument(), false,
                false);
    }

    /**
     * @return the document
     */
    public Node getDocument() {
        return this.document;
    }

    private static String join(final List<String> list) {
        boolean first = true;
        final StringBuilder b = new StringBuilder();
        for (final String s : list) {
            if (first) {
                first = false;
            } else {
                b.append(JMathComponent.FONT_SEPARATOR);
            }
            b.append(s);
        }
        return b.toString();
    }

    /**
     * Font list for Doublestruck. Please see
     * {@link Parameter#FONTS_DOUBLESTRUCK} for an explanation of this
     * parameter.
     *
     * @return The list for Doublestruck.
     * @see Parameter#FONTS_DOUBLESTRUCK
     */
    @SuppressWarnings("unchecked")
    public String getFontsDoublestruck() {
        return JMathComponent.join((List<String>) this.parameters
                .getParameter(Parameter.FONTS_DOUBLESTRUCK));
    }

    /**
     * Font list for Fraktur. Please see {@link Parameter#FONTS_FRAKTUR} for an
     * explanation of this parameter.
     *
     * @return The list for Fraktur.
     * @see Parameter#FONTS_FRAKTUR
     */
    @SuppressWarnings("unchecked")
    public String getFontsFraktur() {
        return JMathComponent.join((List<String>) this.parameters
                .getParameter(Parameter.FONTS_FRAKTUR));
    }

    /**
     * @return the fontSize
     */
    public float getFontSize() {
        return (Float) this.parameters.getParameter(Parameter.MATHSIZE);
    }

    /**
     * Font list for Monospaced. Please see {@link Parameter#FONTS_MONOSPACED}
     * for an explanation of this parameter.
     *
     * @return The list for monospaced.
     * @see Parameter#FONTS_MONOSPACED
     */
    @SuppressWarnings("unchecked")
    public String getFontsMonospaced() {
        return JMathComponent.join((List<String>) this.parameters
                .getParameter(Parameter.FONTS_MONOSPACED));
    }

    /**
     * Font list for Sans-Serif. Please see {@link Parameter#FONTS_SANSSERIF}
     * for an explanation of this parameter.
     *
     * @return The list for sansserif.
     * @see Parameter#FONTS_SANSSERIF
     */
    @SuppressWarnings("unchecked")
    public String getFontsSanserif() {
        return JMathComponent.join((List<String>) this.parameters
                .getParameter(Parameter.FONTS_SANSSERIF));
    }

    /**
     * Font list for Script. Please see {@link Parameter#FONTS_SCRIPT} for an
     * explanation of this parameter.
     *
     * @return The list for Script.
     * @see Parameter#FONTS_SCRIPT
     */
    @SuppressWarnings("unchecked")
    public String getFontsScript() {
        return JMathComponent.join((List<String>) this.parameters
                .getParameter(Parameter.FONTS_SCRIPT));
    }

    /**
     * Font list for Serif (the default MathML font). Please see
     * {@link Parameter#FONTS_SERIF} for an explanation of this parameter.
     *
     * @return The list for serif.
     * @see Parameter#FONTS_SERIF
     */
    @SuppressWarnings("unchecked")
    public String getFontsSerif() {
        return JMathComponent.join((List<String>) this.parameters
                .getParameter(Parameter.FONTS_SERIF));
    }

    /** {@inheritDoc} */
    @Override
    public Color getForeground() {
        return (Color) this.parameters.getParameter(Parameter.MATHCOLOR);
    }

    /**
     * Horizontal alignment, as defined by
     * {@link javax.swing.JLabel#getHorizontalAlignment()}.
     * <p>
     * Supported are: {@link SwingConstants#LEADING},
     * {@link SwingConstants#LEFT}, {@link SwingConstants#CENTER},
     * {@link SwingConstants#TRAILING}, {@link SwingConstants#RIGHT}.
     *
     * @return the horizontalAlignment
     * @see javax.swing.JLabel#getHorizontalAlignment()
     */
    public int getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    /**
     * @return the UI implementation.
     */
    public MathComponentUI getUI() {
        return (MathComponentUI) this.ui;
    }

    /**
     * Vertical alignment, as defined by
     * {@link javax.swing.JLabel#getVerticalAlignment()}.
     * <p>
     * Supported are: {@link SwingConstants#TOP},
     * {@link SwingConstants#CENTER}, {@link SwingConstants#BOTTOM}.
     *
     * @return the verticalAlignment
     * @see javax.swing.JLabel#getVerticalAlignment()
     */
    public int getVerticalAlignment() {
        return this.verticalAlignment;
    }

    private void reval() {
        this.repaint();
        this.revalidate();
    }

    /** {@inheritDoc} */
    @Override
    public void setBackground(final Color c) {
        super.setBackground(c);
        this.reval();
    }

    /**
     * Set the content from a String containing the MathML content.
     *
     * @param contentString
     *            the content to set.
     */
    public void setContent(final String contentString) {
        try {
            final Document stdDomNode = MathMLParserSupport.parseString(contentString);
            final DocumentElement jEuclidDom = DOMBuilder.getInstance().createJeuclidDom(stdDomNode,
                    true, true);
            this.setDocument(jEuclidDom);
        } catch (final SAXException | ParserConfigurationException | IOException e) {
            throw new IllegalArgumentException(e);
        }

    }

    /**
     * Enables, or disables the debug mode.
     *
     * @param dbg
     *            Debug mode.
     */
    public void setDebug(final boolean dbg) {
        this.setParameter(Parameter.DEBUG, dbg);
    }

    /**
     * @param doc
     *            the document to set
     */
    public void setDocument(final Node doc) {
        final Node oldValue = this.document;
        this.firePropertyChange("document", oldValue, doc);
        this.document = doc;
        if (doc != oldValue) {
            this.revalidate();
            this.repaint();
        }
    }

    /**
     * Font emulator for standard component behavior.
     * <p>
     * Emulates the standard setFont function by setting the font Size and
     * adding the font to the front of the serif font list.
     * <p>
     * Please use the separate setters if possible.
     *
     * @param f
     *            font to set.
     * @see #setFontSize(float)
     * @see #setFontsSerif(String)
     * @deprecated use separate setters.
     */
    @Deprecated
    @Override
    public void setFont(final Font f) {
        super.setFont(f);
        this.setFontSize(f.getSize2D());
        this.setFontsSerif(f.getFamily() + JMathComponent.FONT_SEPARATOR
                + this.getFontsSerif());
    }

    private List<String> splitFonts(final String list) {
        return Arrays.asList(list.split(JMathComponent.FONT_SEPARATOR));
    }

    /**
     * Font list for Doublestruck. Please see
     * {@link Parameter#FONTS_DOUBLESTRUCK} for an explanation of this
     * parameter.
     *
     * @param newFonts
     *            new list for Doublestruck (comma seraparated).
     * @see Parameter#FONTS_DOUBLESTRUCK
     */
    public void setFontsDoublestruck(final String newFonts) {
        this.setParameter(Parameter.FONTS_DOUBLESTRUCK, this
                .splitFonts(newFonts));
    }

    /**
     * Font list for Fraktur. Please see {@link Parameter#FONTS_FRAKTUR} for
     * an explanation of this parameter.
     *
     * @param newFonts
     *            new list for Fraktur (comma seraparated).
     * @see Parameter#FONTS_FRAKTUR
     */
    public void setFontsFraktur(final String newFonts) {
        this.setParameter(Parameter.FONTS_FRAKTUR, this.splitFonts(newFonts));
    }

    /**
     * Sets a generic rendering parameter.
     *
     * @param key
     *            Key for the parameter
     * @param newValue
     *            newValue
     */
    public void setParameter(final Parameter key, final Object newValue) {
        this.setParameters(Collections.singletonMap(key, newValue));
    }

    /**
     * Sets generic rendering parameters.
     *
     * @param newValues
     *            map of parameter keys to new values
     */
    public void setParameters(final Map<Parameter, Object> newValues) {
        for (final Map.Entry<Parameter, Object> entry : newValues.entrySet()) {
            final Parameter key = entry.getKey();
            final Object oldValue = this.parameters.getParameter(key);
            this.parameters.setParameter(key, entry.getValue());
            this.firePropertyChange(key.name(), oldValue, this.parameters
                    .getParameter(key));
        }
        this.revalidate();
        this.repaint();
    }

    /**
     * sets the font size used.
     *
     * @param fontSize
     *            the font size.
     */
    public void setFontSize(final float fontSize) {
        this.setParameter(Parameter.MATHSIZE, fontSize);
    }

    /**
     * Font list for Monospaced. Please see {@link Parameter#FONTS_MONOSPACED}
     * for an explanation of this parameter.
     *
     * @param newFonts
     *            new list for Monospaced (comma seraparated).
     * @see Parameter#FONTS_MONOSPACED
     */
    public void setFontsMonospaced(final String newFonts) {
        this.setParameter(Parameter.FONTS_MONOSPACED, this
                .splitFonts(newFonts));
    }

    /**
     * Font list for Sans-Serif. Please see {@link Parameter#FONTS_SANSSERIF}
     * for an explanation of this parameter.
     *
     * @param newFonts
     *            new list for sansserif (comma seraparated).
     * @see Parameter#FONTS_SANSSERIF
     */
    public void setFontsSanserif(final String newFonts) {
        this.setParameter(Parameter.FONTS_SANSSERIF, this
                .splitFonts(newFonts));
    }

    /**
     * Font list for Script. Please see {@link Parameter#FONTS_SCRIPT} for an
     * explanation of this parameter.
     *
     * @param newFonts
     *            new list for Script (comma seraparated).
     * @see Parameter#FONTS_SCRIPT
     */
    public void setFontsScript(final String newFonts) {
        this.setParameter(Parameter.FONTS_SCRIPT, this.splitFonts(newFonts));
    }

    /**
     * Font list for Serif (the default MathML font). Please see
     * {@link Parameter#FONTS_SERIF} for an explanation of this parameter.
     *
     * @param newFonts
     *            new list for serif (comma seraparated).
     * @see Parameter#FONTS_SERIF
     */
    public void setFontsSerif(final String newFonts) {
        this.setParameter(Parameter.FONTS_SERIF, this.splitFonts(newFonts));
        this.fontCompat();
    }

    /** {@inheritDoc} */
    @Override
    public void setForeground(final Color fg) {
        super.setForeground(fg);
        this.setParameter(Parameter.MATHCOLOR, fg);
    }

    /**
     * Horizontal alignment, as defined by
     * {@link javax.swing.JLabel#setHorizontalAlignment(int)}.
     * <p>
     * Supported are: {@link SwingConstants#LEADING},
     * {@link SwingConstants#LEFT}, {@link SwingConstants#CENTER},
     * {@link SwingConstants#TRAILING}, {@link SwingConstants#RIGHT}.
     *
     * @param hAlignment
     *            the horizontalAlignment to set
     * @see javax.swing.JLabel#setHorizontalAlignment(int)
     */
    public void setHorizontalAlignment(final int hAlignment) {
        this.horizontalAlignment = hAlignment;
    }

    /** {@inheritDoc} */
    @Override
    public void setOpaque(final boolean opaque) {
        super.setOpaque(opaque);
        this.reval();
    }

    /**
     * Vertical alignment, as defined by
     * {@link javax.swing.JLabel#setVerticalAlignment(int)}.
     * <p>
     * Supported are: {@link SwingConstants#TOP},
     * {@link SwingConstants#CENTER}, {@link SwingConstants#BOTTOM}.
     *
     * @param vAlignment
     *            the verticalAlignment to set
     * @see javax.swing.JLabel#setVerticalAlignment(int)
     */
    public void setVerticalAlignment(final int vAlignment) {
        this.verticalAlignment = vAlignment;
    }

    /** {@inheritDoc} */
    @Override
    public void updateUI() {
        if (UIManager.get(this.getUIClassID()) == null) {
            this.setUI(new MathComponentUI16());
        } else {
            this.setUI(UIManager.getUI(this));
        }
    }

    /**
     * @return the parameters
     */
    public MutableLayoutContext getParameters() {
        return this.parameters;
    }

    /** {@inheritDoc} */
    @Override
    public void setSize(final int width, final int height) {
        // TODO Auto-generated method stub
        super.setSize(width, height);
    }


}
