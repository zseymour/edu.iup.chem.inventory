/*
 * Created on 25-Jan-2006
 */
package org.apache.lucene.queryparser.xml.builders;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.queryparser.xml.DOMUtils;
import org.apache.lucene.queryparser.xml.FilterBuilder;
import org.apache.lucene.queryparser.xml.ParserException;
import org.w3c.dom.Element;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Builder for {@link TermRangeFilter}
 */
public class RangeFilterBuilder implements FilterBuilder {

  public Filter getFilter(Element e) throws ParserException {
    String fieldName = DOMUtils.getAttributeWithInheritance(e, "fieldName");

    String lowerTerm = e.getAttribute("lowerTerm");
    String upperTerm = e.getAttribute("upperTerm");
    boolean includeLower = DOMUtils.getAttribute(e, "includeLower", true);
    boolean includeUpper = DOMUtils.getAttribute(e, "includeUpper", true);
    return TermRangeFilter.newStringRange(fieldName, lowerTerm, upperTerm, includeLower, includeUpper);
  }

}
