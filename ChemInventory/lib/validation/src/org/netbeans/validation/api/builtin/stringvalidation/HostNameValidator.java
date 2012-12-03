/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.validation.api.builtin.stringvalidation;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
final class HostNameValidator extends StringValidator {

    private final boolean allowPort;

    HostNameValidator(boolean allowPort) {
        this.allowPort = allowPort;
    }

    @Override
    public void validate(Problems problems, String compName, String model) {
        if (model.length() == 0) {
            problems.add(NbBundle.getMessage(HostNameValidator.class,
                    "INVALID_HOST_NAME", compName, model)); //NOI18N
            return;
        }
        if (model.startsWith(".") || model.endsWith(".")) { //NOI18N
            problems.add(NbBundle.getMessage(IpAddressValidator.class,
                "HOST_STARTS_OR_ENDS_WITH_PERIOD", model)); //NOI18N
            return;
        }
        String[] parts = model.split("\\.");
        if (parts.length > 4) {
            problems.add(NbBundle.getMessage(IpAddressValidator.class,
                "TOO_MANY_LABELS", model)); //NOI18N
            return;
        }
        if (!allowPort && model.contains(":")) { //NOI18N
            problems.add(NbBundle.getMessage(HostNameValidator.class,
                    "MSG_PORT_NOT_ALLOWED", compName, model)); //NOI18N
            return;
        }
        new MayNotContainSpacesValidator().validate(problems, compName, model);
        if (model.endsWith("-") || model.startsWith("-")) {
            problems.add(NbBundle.getMessage(HostNameValidator.class,
                    "INVALID_HOST_NAME", compName, model)); //NOI18N
            return;
        }

        for (int i = 0; i < parts.length; i++) {
            String label = parts[i];
            if (label.length() > 63) {
                problems.add(NbBundle.getMessage(HostNameValidator.class,
                        "LABEL_TOO_LONG", label)); //NOI18N
                return;
            }
            if (i == parts.length - 1 && label.indexOf(":") > 0) {
                String[] labelAndPort = label.split(":");
                if (labelAndPort.length > 2) {
                    problems.add(NbBundle.getMessage(HostNameValidator.class,
                            "INVALID_PORT", compName, label)); //NOI18N
                    return;
                }
                if (labelAndPort.length == 1) {
                    problems.add(NbBundle.getMessage(HostNameValidator.class,
                            "INVALID_PORT", compName, "''")); //NOI18N
                    return;
                } 
                if (label.endsWith(":")) {
                    problems.add(NbBundle.getMessage(HostNameValidator.class,
                            "TOO_MANY_COLONS", compName, label)); //NOI18N
                    return;
                }
                try {
                    int port = Integer.parseInt(labelAndPort[1]);
                    if (port < 0) {
                        problems.add(NbBundle.getMessage(IpAddressValidator.class,
                                "NEGATIVE_PORT", port)); //NOI18N
                        return;
                    } else if (port >= 65536) {
                        problems.add(NbBundle.getMessage(IpAddressValidator.class,
                                "PORT_TOO_HIGH", port)); //NOI18N
                        return;
                    }
                } catch (NumberFormatException e) {
                    problems.add(NbBundle.getMessage(HostNameValidator.class,
                            "INVALID_PORT", compName, labelAndPort[1])); //NOI18N
                    return;
                }
                if(!checkHostPart(labelAndPort[0], problems, compName)){
                    return;
                }
            } else {
                checkHostPart(label, problems, compName);
            }
        } // for
    }

    private boolean checkHostPart(String label, Problems problems, String compName) {
        if (label.length() > 63) {
            problems.add(NbBundle.getMessage(HostNameValidator.class,
                    "LABEL_TOO_LONG", label)); //NOI18N
            return false;
        }
        if (label.length() == 0) {
            problems.add(NbBundle.getMessage(HostNameValidator.class,
                    "LABEL_EMPTY", compName, label)); //NOI18N
            return false;
        }
        try {
            Integer.parseInt(label);
            problems.add(NbBundle.getMessage(HostNameValidator.class,
                    "NUMBER_PART_IN_HOSTNAME", label)); //NOI18N
            return false;
        } catch (NumberFormatException e) {
            //do nothing
        }
        Problems tmp = new Problems();
        new EncodableInCharsetValidator().validate(tmp, compName, label);
        problems.putAll(tmp);
        if (!tmp.hasFatal()) {
            for (char c : label.toLowerCase().toCharArray()) {
                if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9' || c == '-')) { //NOI18N
                    continue;
                }
                problems.add(NbBundle.getMessage(HostNameValidator.class,
                        "BAD_CHAR_IN_HOSTNAME", new String(new char[]{c}))); //NOI18N
                return false;
            }
        }
        return true;
    }
}
