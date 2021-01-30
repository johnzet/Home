package org.zehetner.homeautomation.mock;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

@SuppressWarnings("ReturnOfNull")
public class MockHttpServletResponse implements HttpServletResponse {

	private StringBuffer responseText = new StringBuffer();
	private String characterEncoding = "character encoding not set";
	private String contentType = "content type not set";
	private int status = -1;

	public MockHttpServletResponse() {
	}

	public String getResponseText() {
		return this.responseText.toString();
	}

	@Override
    public String getCharacterEncoding() {
		return this.characterEncoding;
	}

	@Override
    public String getContentType() {
		return this.contentType;
	}

	@Override
    public ServletOutputStream getOutputStream() throws IOException {
		return new ServletOutputStream() {

			@Override
			public void write(final int b) throws IOException {
                MockHttpServletResponse.this.responseText.append((char) b);
			}
		};
	}

	@Override
    public PrintWriter getWriter() throws IOException {
		return new PrintWriter(this.responseText.toString());
	}

	@Override
    public void setCharacterEncoding(final String charset) {
        this.characterEncoding = charset;
	}

	@Override
    public void setContentLength(final int len) {
	}

	@Override
    public void setContentType(final String type) {
        this.contentType = type;
	}

	@Override
    public void setBufferSize(final int size) {
	}

	@Override
    public int getBufferSize() {
		return Integer.MAX_VALUE;
	}

	@Override
    public void flushBuffer() throws IOException {
	}

	@Override
    public void resetBuffer() {
        this.responseText = new StringBuffer();
	}

	@Override
    public boolean isCommitted() {
		return false;
	}

	@Override
    public void reset() {
	}

	@Override
    public void setLocale(final Locale loc) {
	}

	@Override
    public Locale getLocale() {
		return Locale.getDefault();
	}

	@Override
    public void addCookie(final Cookie cookie) {
	}

	@Override
    public boolean containsHeader(final String name) {
		return false;
	}

	@Override
    public String encodeURL(final String url) {
		return null;
	}

	@Override
    public String encodeRedirectURL(final String url) {
		return null;
	}

	@Override
    public String encodeUrl(final String url) {
		return null;
	}

	@Override
    public String encodeRedirectUrl(final String url) {
		return null;
	}

	@Override
    public void sendError(final int sc, final String msg) throws IOException {
	}

	@Override
    public void sendError(final int sc) throws IOException {
	}

	@Override
    public void sendRedirect(final String location) throws IOException {
	}

	@Override
    public void setDateHeader(final String name, final long date) {
	}

	@Override
    public void addDateHeader(final String name, final long date) {
	}

	@Override
    public void setHeader(final String name, final String value) {
	}

	@Override
    public void addHeader(final String name, final String value) {
	}

	@Override
    public void setIntHeader(final String name, final int value) {
	}

	@Override
    public void addIntHeader(final String name, final int value) {
	}

	@Override
    public void setStatus(final int sc) {
        this.status = sc;
	}

	@Override
    public void setStatus(final int sc, final String sm) {
        this.status = sc;
	}

	@Override
    public int getStatus() {
		return this.status;
	}

	@Override
    public String getHeader(final String name) {
		return null;
	}

	@Override
    public Collection<String> getHeaders(final String name) {
		return null;
	}

	@Override
    public Collection<String> getHeaderNames() {
		return null;
	}

}
