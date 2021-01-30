package org.zehetner.homeautomation.mock;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MockHttpServletRequest  implements HttpServletRequest {

    private final Map<String, String[]> parameters = new HashMap<String, String[]>(16);
    private final Map<String, Object> attributes = new HashMap<String, Object>(16);
    private String characterEncoding = null;
    private String contentType = null;
	private String requestURI = null;

	public String getQueryString() {
		return queryString;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public StringBuffer getRequestURL() {
		return new StringBuffer(requestURI);
	}

	public void setRequestURI(final String requestURI) {
		this.requestURI = requestURI;
	}

	public void setQueryString(final String str) {
		this.queryString = str;
	}

	private String queryString = null;

    public Object getAttribute(final String name) {
		return this.attributes.get(name);
	}

    public String getCharacterEncoding() {
		return this.characterEncoding;
	}

    public void setCharacterEncoding(final String enc)  throws UnsupportedEncodingException{
        this.characterEncoding = enc;
	}

    public String getContentType() {
		return this.contentType;
	}

    public String getParameter(final String name) {
    	return this.parameters.get(name)[0];
	}

    public void setAttribute(final String name, final Object o) {
        this.attributes.put(name, o);
	}

	public String getAuthType() {
		return null;
	}

	public Cookie[] getCookies() {
		return new Cookie[0];
	}

	public long getDateHeader(final String name) {
		return 0;
	}

	public String getHeader(final String name) {
		return null;
	}

	public Enumeration<String> getHeaders(final String name) {
		return null;
	}

	public Enumeration<String> getHeaderNames() {
		return null;
	}

	public int getIntHeader(final String name) {
		return 0;
	}

	public String getMethod() {
		return null;
	}

	public String getPathInfo() {
		return null;
	}

	public String getPathTranslated() {
		return null;
	}

	public String getContextPath() {
		return null;
	}

	public String getRemoteUser() {
		return null;
	}

	public boolean isUserInRole(final String role) {
		return false;
	}

	public Principal getUserPrincipal() {
		return null;
	}

	public String getRequestedSessionId() {
		return null;
	}

	public String getServletPath() {
		return null;
	}

	public HttpSession getSession(final boolean create) {
		return null;
	}

	public HttpSession getSession() {
		return null;
	}

	public String changeSessionId() {
		return null;
	}

	public boolean isRequestedSessionIdValid() {
		return false;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException {
		return false;
	}

	public void login(final String username, final String password) throws ServletException {

	}

	public void logout() throws ServletException {

	}

	public Collection<Part> getParts() throws IOException, ServletException {
		return null;
	}

	public Part getPart(final String name) throws IOException, ServletException {
		return null;
	}

	public <T extends HttpUpgradeHandler> T upgrade(final Class<T> handlerClass) throws IOException, ServletException {
		return null;
	}

	public Enumeration<String> getAttributeNames() {
		return null;
	}

	public int getContentLength() {
		return 0;
	}

	public long getContentLengthLong() {
		return 0;
	}

	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	public Enumeration<String> getParameterNames() {
		return null;
	}

	public String[] getParameterValues(final String name) {
		return new String[0];
	}

	public Map<String, String[]> getParameterMap() {
		return this.parameters;
	}

	public String getProtocol() {
		return null;
	}

	public String getScheme() {
		return null;
	}

	public String getServerName() {
		return null;
	}

	public int getServerPort() {
		return 0;
	}

	public BufferedReader getReader() throws IOException {
		return null;
	}

	public String getRemoteAddr() {
		return null;
	}

	public String getRemoteHost() {
		return null;
	}

	public void removeAttribute(final String name) {

	}

	public Locale getLocale() {
		return null;
	}

	public Enumeration<Locale> getLocales() {
		return null;
	}

	public boolean isSecure() {
		return false;
	}

	public RequestDispatcher getRequestDispatcher(final String path) {
		return null;
	}

	public String getRealPath(final String path) {
		return null;
	}

	public int getRemotePort() {
		return 0;
	}

	public String getLocalName() {
		return null;
	}

	public String getLocalAddr() {
		return null;
	}

	public int getLocalPort() {
		return 0;
	}

	public ServletContext getServletContext() {
		return null;
	}

	public AsyncContext startAsync() throws IllegalStateException {
		return null;
	}

	public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IllegalStateException {
		return null;
	}

	public boolean isAsyncStarted() {
		return false;
	}

	public boolean isAsyncSupported() {
		return false;
	}

	public AsyncContext getAsyncContext() {
		return null;
	}

	public DispatcherType getDispatcherType() {
		return null;
	}
}
