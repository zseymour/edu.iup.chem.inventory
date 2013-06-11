/* Copyright (c) 2002-2008 The University of the West Indies
 *
 * Contact: robert.lancashire@uwimona.edu.jm
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package jspecview.application;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.TreeMap;

import jspecview.common.JSpecViewUtils;
import jspecview.util.SimpleXmlReader;

/**
 * <code>DisplaySchemesProcessor</code> loads and saves the display schemes of
 * Jspecview. The Schemes are loaded from an XML file and saved in a TreeMap.
 * Also saves the schemes out to XML file after modification
 * @author Debbie-Ann Facey
 * @author Khari A. Bryan
 * @author Prof Robert J. Lancashire
 */
public class DisplaySchemesProcessor {

  /** The Name of the XML file that contains the display schemes */
  private String fileName;

  /** The list of displaySchemes that is loaded from file */
  private TreeMap<String, DisplayScheme> displaySchemes;

  private SimpleXmlReader reader;

  /**
   * Initialises the <code>DisplaySchemesProcessor</code>
   */
  public DisplaySchemesProcessor() {
    displaySchemes = new TreeMap<String, DisplayScheme>();
  }

  /**
   * Load a default DisplayScheme if xml file not found
   * @param dispSchemeFileName String
   * @return boolean
   */

public boolean loadDefault(String dispSchemeFileName) {
    Color black = new Color(0,0,0);
    Color white = new Color(255,255,255);

    DisplayScheme dsdef = new DisplayScheme("Default");
    dsdef.setFont("default");
    dsdef.setColor("title", black);
    dsdef.setColor("coordinates", black);
    dsdef.setColor("scale", black);
    dsdef.setColor("units", black);
    dsdef.setColor("grid", black);
    dsdef.setColor("plot", black);
    dsdef.setColor("plotarea", white);
    dsdef.setColor("background", white);
    displaySchemes.put("Default", dsdef);
    return true;
  }



  /**
   * Saves the display schemes to file in XML format
   * @throws IOException
   */
  public void store() throws IOException{
    serializeDisplaySchemes(new FileWriter(fileName));
  }

  /**
   * Returns the list of <code>DisplayScheme</code>s that were loaded
   * @return the list of <code>DisplayScheme</code>s that were loaded
   */
  public TreeMap<String, DisplayScheme> getDisplaySchemes(){
    return displaySchemes;
  }

  /**
   * Loads the display schemes into memory and stores them in a <code>Vector</code>
   * @param dispSchemeFileName the name of the file to load
   * @throws Exception
   * @return true if loaded successfully
   */
  public boolean load(String dispSchemeFileName) throws Exception {
    fileName = dispSchemeFileName;
    reader = new SimpleXmlReader(new FileInputStream(fileName));
    String defaultDS = "Default";
    DisplayScheme ds = null;
    String attr;
    while (reader.hasNext()) {
      if (reader.nextEvent() != SimpleXmlReader.START_ELEMENT)
        continue;
      String theTag = reader.getTagName();
      if (theTag.equals("displayschemes")) {
        defaultDS = reader.getAttrValue("default");
      }
      if (theTag.equals("displayscheme")) {
        String name = reader.getAttrValue("name");
        ds = new DisplayScheme(name);
        if (name.equals(defaultDS))
          ds.setDefault(true);
      }
      if (ds == null)
        continue;
      if (theTag.equals("font")) {
        attr = reader.getAttrValue("face");
        if (attr.length() > 0 && ds != null)
          ds.setFont(attr);
      } else if (theTag.equals("titlecolor")) {
        Color color = getColor();
        if (color == null)
          color = Color.decode("#0000ff");
        ds.setColor("title", color);
      } else if (theTag.equals("coordinatecolor")) {
        Color color = getColor();
        if (color == null)
          color = Color.decode("#ff0000");
        ds.setColor("coordinates", color);
      } else if (theTag.equals("scalecolor")) {
        Color color = getColor();
        if (color == null)
          color = Color.decode("#660000");
        ds.setColor("scale", color);
      } else if (theTag.equals("unitscolor")) {
        Color color = getColor();
        if (color == null)
          color = Color.decode("#ff0000");
        ds.setColor("units", color);
      } else if (theTag.equals("gridcolor")) {
        Color color = getColor();
        if (color == null)
          color = Color.decode("#4e4c4c");
        ds.setColor("grid", color);
      } else if (theTag.equals("plotcolor")) {
        Color color = getColor();
        if (color == null)
          Color.decode("#ff9900");
        ds.setColor("plot", color);
      } else if (theTag.equals("plotareacolor")) {
        Color color = getColor();
        if (color == null)
          color = Color.decode("#333333");
        ds.setColor("plotarea", color);
      } else if (theTag.equals("backgroundcolor")) {
        Color color = getColor();
        if (color == null)
          color = Color.decode("#c0c0c0");
        ds.setColor("background", color);
      }
      displaySchemes.put(ds.getName(), ds);
    }
    return true;
  }

  /**
   * Gets a hex color value from the attribute of a tag and returns a
   * <code>Color</code>
   * @return Returns a <code>Color</code> from the attribute
   */
  private Color getColor(){
    String value = reader.getAttrValueLC("hex");
    return (value.length() == 0 || value.equals("default") ? null
        : JSpecViewUtils.getColorFromString(value));
  }

  /**
   * Serializes the display schemes to the given writer
   * @param writer the writer for the output
   * @throws IOException
   */
  public void serializeDisplaySchemes(Writer writer) throws IOException{
    if(displaySchemes.size() == 0){
      return;
    }

    // find the default scheme
    // set default attr
    StringWriter sw = new StringWriter();
    BufferedWriter buffer = new BufferedWriter(sw);
    String defaultDSName = "";

    Iterator<String> interator = displaySchemes.keySet().iterator();
    while (interator.hasNext()){
      DisplayScheme ds = (DisplayScheme)displaySchemes.get(interator.next());
      if(ds.isDefault())
        defaultDSName = ds.getName();

      buffer.write("\t<displayScheme name=\"" + ds.getName() + "\">");
      buffer.newLine();
      buffer.write("\t\t<font face = \"" + ds.getFont() + "\"/>");
      buffer.newLine();
      buffer.write("\t\t<titleColor hex = \"" +
                   JSpecViewUtils.colorToHexString(ds.getColor("title")) +
                   "\"/>");
      buffer.newLine();
      buffer.write("\t\t<scaleColor hex = \"" +
                   JSpecViewUtils.colorToHexString(ds.getColor("scale")) +
                   "\"/>");
      buffer.newLine();
      buffer.write("\t\t<unitsColor hex = \"" +
                   JSpecViewUtils.colorToHexString(ds.getColor("units")) +
                   "\"/>");
      buffer.newLine();
      buffer.write("\t\t<coordinateColor hex = \"" +
                   JSpecViewUtils.colorToHexString(ds.getColor("coordinates")) +
                   "\"/>");
      buffer.newLine();
      buffer.write("\t\t<gridColor hex = \"" +
                   JSpecViewUtils.colorToHexString(ds.getColor("grid")) +
                   "\"/>");
      buffer.newLine();
      buffer.write("\t\t<plotColor hex = \"" +
                   JSpecViewUtils.colorToHexString(ds.getColor("plot")) +
                   "\"/>");
      buffer.newLine();
      buffer.write("\t\t<plotAreaColor hex = \"" +
                   JSpecViewUtils.colorToHexString(ds.getColor("plotarea")) +
                   "\"/>");
      buffer.newLine();
      buffer.write("\t\t<backgroundColor hex = \"" +
                   JSpecViewUtils.colorToHexString(ds.getColor("background")) +
                   "\"/>");
      buffer.newLine();
      buffer.write("\t</displayScheme>");
      buffer.newLine();
    }

    buffer.write("</displaySchemes>");
    buffer.flush();

    StringBuffer outBuffer = new StringBuffer();
    outBuffer.append("<?xml version=\"1.0\"?>" + JSpecViewUtils.newLine);
    outBuffer.append("<displaySchemes default=\""+ defaultDSName +"\">" + JSpecViewUtils.newLine);
    outBuffer.append(sw.getBuffer());

    writer.write(outBuffer.toString());
    writer.flush();
    writer.close();
  }

}
