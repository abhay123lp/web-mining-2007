/*
 * $Header$
 * $Revision: 3848 $
 * $Date: 2005-09-28 02:29:22 +0000 (Wed, 28 Sep 2005) $
 *
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.httpclient.cookie;

import java.util.Collection;
import java.util.SortedMap;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;

/**
 * A cookie spec that does nothing.  Cookies are neither parsed, formatted nor matched.
 * It can be used to effectively disable cookies altogether. 
 * 
 * @since 3.0
 */
public class IgnoreCookiesSpec implements CookieSpec {
    private static final Cookie[] NO_COOKIES = new Cookie[0];
    
    /**
     * 
     */
    public IgnoreCookiesSpec() {
        super();
    }

    /**
     * Returns an empty {@link Cookie cookie} array.  All parameters are ignored.
     */
    public Cookie[] parse(String host, int port, String path, boolean secure,
        String header) throws MalformedCookieException {
        return NO_COOKIES;
    }

    /**
     * @return <code>null</code>
     */
    public Collection getValidDateFormats() {
        return null;
    }

    /**
     * Does nothing.
     */
    public void setValidDateFormats(Collection datepatterns) {
    }

    /**
     * @return <code>null</code>
     */
    public String formatCookie(Cookie cookie) {
        return null;
    }

    /**
     * @return <code>null</code>
     */
    public Header formatCookieHeader(Cookie cookie)
        throws IllegalArgumentException {
        return null;
    }

    /**
     * @return <code>null</code>
     */
    public Header formatCookieHeader(Cookie[] cookies)
        throws IllegalArgumentException {
        return null;
    }

    /**
     * @return <code>null</code>
     */
    public String formatCookies(Cookie[] cookies)
        throws IllegalArgumentException {
        return null;
    }

    /**
     * @return <code>false</code>
     */
    public boolean match(String host, int port, String path, boolean secure,
        Cookie cookie) {
        return false;
    }

    /**
     * Returns an empty {@link Cookie cookie} array.  All parameters are ignored.
     */
    public Cookie[] match(String host, int port, String path, boolean secure,
        Cookie[] cookies) {
        return NO_COOKIES;
    }

    /**
     * Returns an empty {@link Cookie cookie} array.  All parameters are ignored.
     */
    public Cookie[] parse(String host, int port, String path, boolean secure,
        Header header) throws MalformedCookieException,
        IllegalArgumentException {
        return NO_COOKIES;
    }

    /**
     * Does nothing.
     */
    public void parseAttribute(NameValuePair attribute, Cookie cookie)
        throws MalformedCookieException, IllegalArgumentException {
    }

    /**
     * Does nothing.
     */
    public void validate(String host, int port, String path, boolean secure,
        Cookie cookie) throws MalformedCookieException,
        IllegalArgumentException {
    }

    /**
     * @return <code>false</code>
     */
    public boolean domainMatch(final String host, final String domain) {
        return false;
    }

    /**
     * @return <code>false</code>
     */
    public boolean pathMatch(final String path, final String topmostPath) {
        return false;
    }

    public Cookie[] match(String domain, int port, String path, boolean secure,
        SortedMap cookiesMap) {
        return NO_COOKIES;
    }
}
