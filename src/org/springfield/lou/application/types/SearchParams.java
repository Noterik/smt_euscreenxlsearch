/* 
* EuscreenxlpreviewApplication.java
* 
* Copyright (c) 2012 Noterik B.V.
* 
* This file is part of Lou, related to the Noterik Springfield project.
*
* Lou is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Lou is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Lou.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.springfield.lou.application.types;

import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

public class SearchParams {
	public String provider = "all";
	public String datasource = "all";
	public String sortfield = "id";
	public String mtype = "all";
	public String sortdirection = "up";
	public String decade = "all";
	public String searchkey ="*";
	public int maxdisplay = 10000;

	
	public SearchParams(String xmls) {
		System.out.println("SearchParams(" + xmls + ")");
		try {
			Document xml = DocumentHelper.parseText(xmls);
			for(Iterator<Node> iter = xml.getRootElement().nodeIterator(); iter.hasNext(); ) {
				Element child = (Element)iter.next();
				if (child.getName().equals("properties")) {
					for(Iterator<Node> iter2 = child.nodeIterator(); iter2.hasNext(); ) {
						Node node = iter2.next();
						String name = node.getName();
						if (name.equals("provider")) {
							provider = node.getText();
						} else if (name.equals("searchkey")) {
							System.out.println("AT SEARCH KEY!!!!!");
							System.out.println(node.getText());
							searchkey = node.getText();
							if (searchkey.equals("")) searchkey="*";
						} else if (name.equals("mtype")) {
							mtype = node.getText();
						} else if (name.equals("sortfield")) {
							sortfield = node.getText();
						} else if (name.equals("sortdirection")) {
							sortdirection = node.getText();
						} else if (name.equals("decade")) {
							decade = node.getText();
						} else if (name.equals("maxdisplay")) {
							maxdisplay = Integer.parseInt(node.getText());
						} else if (name.equals("datasource")) {
							datasource = node.getText();
						}
					}
				}
			}
		    
		} catch(Exception e) {
			System.out.println("INVALID PARAMETER NODE");
			e.printStackTrace();
			
		}
	}
}
