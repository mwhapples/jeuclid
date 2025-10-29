/*
 * Copyright 2007 - 2009 JEuclid, http://jeuclid.sf.net
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

package net.sourceforge.jeuclid.elements;

import net.sourceforge.jeuclid.elements.content.semantic.Annotation;
import net.sourceforge.jeuclid.elements.content.semantic.Semantics;
import net.sourceforge.jeuclid.elements.generic.ForeignElement;
import net.sourceforge.jeuclid.elements.generic.MathImpl;
import net.sourceforge.jeuclid.elements.presentation.enlivening.Maction;
import net.sourceforge.jeuclid.elements.presentation.general.*;
import net.sourceforge.jeuclid.elements.presentation.script.*;
import net.sourceforge.jeuclid.elements.presentation.table.*;
import net.sourceforge.jeuclid.elements.presentation.token.*;
import org.apache.batik.dom.AbstractDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates MathElements from given element strings.
 * 
 * @version $Revision$
 */
public final class JEuclidElementFactory {

    /**
     * Logger for this class
     */
    private static final Log LOGGER = LogFactory
            .getLog(JEuclidElementFactory.class);

    private static final Map<String, Constructor<? extends JEuclidElement>> IMPL_CLASSES = new HashMap<>();;

    private JEuclidElementFactory() {
        // Empty on purpose
    }

    private static String removeNSPrefix(final String qualifiedName) {
        final int posSeparator = qualifiedName.indexOf(':');
        if (posSeparator >= 0) {
            return qualifiedName.substring(posSeparator + 1);
        }
        return qualifiedName;
    }

    /**
     * Retrieve the constructor for an JEuclidElement, if available.
     * 
     * @param nsUri
     *            namespace of the element
     * @param qualifiedName
     *            qualified name
     * @return A constructor to create the element, or null if no such
     *         constructor is available.
     */
    public static Constructor<? extends JEuclidElement> getJEuclidElementConstructor(
            final String nsUri, final String qualifiedName) {
        final String localName = JEuclidElementFactory
                .removeNSPrefix(qualifiedName);
        if ((nsUri == null) || (nsUri.isEmpty())
                || (AbstractJEuclidElement.URI.equals(nsUri))) {
            return JEuclidElementFactory.IMPL_CLASSES.get(localName);
        } else {
            return null;
        }
    }

    /**
     * Factory for MathML Elements.
     * 
     * @param nsUri
     *            namespace URI. May be null. May be ignored in the case of
     *            MathML.
     * @param qualifiedName
     *            name of the element with optional namespace prefix.
     * @param ownerDocument
     *            Document this element belongs to.
     * @return A new MathElement for this tag name.
     */
    public static Element elementFromName(final String nsUri,
            final String qualifiedName, final Document ownerDocument) {


        JEuclidElement element = null;

            final Constructor<? extends JEuclidElement> con = JEuclidElementFactory.
            getJEuclidElementConstructor(nsUri, qualifiedName);

            if (con != null) {
                try {
                    element = con.newInstance(qualifiedName,
                            ownerDocument);
                } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    element = null;
                }
            }
        
        if (element == null) {
            element = new ForeignElement(nsUri, qualifiedName,
                    (AbstractDocument) ownerDocument);
        }
        return element;
    }

    private static void addClass(final Class<? extends JEuclidElement> c) {
        try {
            final Field f = c.getField("ELEMENT");
            final String tag = (String) f.get(null);
            JEuclidElementFactory.IMPL_CLASSES.put(tag, c.getConstructor(
                    String.class, AbstractDocument.class));
        } catch (final NoSuchFieldException | NoSuchMethodException | IllegalAccessException e) {
            JEuclidElementFactory.LOGGER.warn(c.toString(), e);
        }
    }

    // CHECKSTYLE:OFF
    static {
        // CHECKSTYLE:ON
        JEuclidElementFactory.addClass(MathImpl.class);
        JEuclidElementFactory.addClass(Mfenced.class);
        JEuclidElementFactory.addClass(Mfrac.class);
        JEuclidElementFactory.addClass(Menclose.class);
        JEuclidElementFactory.addClass(Mphantom.class);
        JEuclidElementFactory.addClass(Msup.class);
        JEuclidElementFactory.addClass(Msub.class);
        JEuclidElementFactory.addClass(Mmultiscripts.class);
        JEuclidElementFactory.addClass(Mprescripts.class);
        JEuclidElementFactory.addClass(None.class);
        JEuclidElementFactory.addClass(Msubsup.class);
        JEuclidElementFactory.addClass(Munder.class);
        JEuclidElementFactory.addClass(Mover.class);
        JEuclidElementFactory.addClass(Munderover.class);
        JEuclidElementFactory.addClass(Mspace.class);
        JEuclidElementFactory.addClass(Ms.class);
        JEuclidElementFactory.addClass(Mstyle.class);
        JEuclidElementFactory.addClass(Msqrt.class);
        JEuclidElementFactory.addClass(Mroot.class);
        JEuclidElementFactory.addClass(Mtable.class);
        JEuclidElementFactory.addClass(Mtr.class);
        JEuclidElementFactory.addClass(Mlabeledtr.class);
        JEuclidElementFactory.addClass(Mtd.class);
        JEuclidElementFactory.addClass(Mo.class);
        JEuclidElementFactory.addClass(Mi.class);
        JEuclidElementFactory.addClass(Mn.class);
        JEuclidElementFactory.addClass(Mtext.class);
        JEuclidElementFactory.addClass(Mrow.class);
        JEuclidElementFactory.addClass(Maligngroup.class);
        JEuclidElementFactory.addClass(Malignmark.class);
        JEuclidElementFactory.addClass(Semantics.class);
        JEuclidElementFactory.addClass(Annotation.class);
        JEuclidElementFactory.addClass(Mpadded.class);
        JEuclidElementFactory.addClass(Merror.class);
        JEuclidElementFactory.addClass(Maction.class);
        JEuclidElementFactory.addClass(Mglyph.class);
    }

}
