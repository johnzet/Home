package org.zehetner.homeautomation.mock;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

public class MockHttpServletResponse implements HttpServletResponse {

	private StringBuffer responseText = new StringBuffer();
	private String characterEncoding = "character encoding not set";
	private String contentType = "content type not set";
	private int status = -1;

	public MockHttpServletResponse() {
	}

    public String getCharacterEncoding() {
		return this.characterEncoding;
	}

    public String getContentType() {
		return this.contentType;
	}

    public ServletOutputStream getOutputStream() throws IOException {
		return new ServletOutputStream() {

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setWriteListener(final WriteListener writeListener) {

			}

			@Override
			public void write(final int b) throws IOException {
                MockHttpServletResponse.this.responseText.append((char) b);
			}
		};
	}

    public PrintWriter getWriter() throws IOException {
		return new PrintWriter(this.responseText.toString());
	}

    public void setCharacterEncoding(final String charset) {
        this.characterEncoding = charset;
	}

	public void setContentLength(final int len) {

	}

	public void setContentLengthLong(final long len) {

	}

	public void setContentType(final String type) {
        this.contentType = type;
	}

	public void setBufferSize(final int size) {

	}

	public int getBufferSize() {
		return Integer.MAX_VALUE;
	}

	public void flushBuffer() throws IOException {

	}

	public void resetBuffer() {

	}

	public boolean isCommitted() {
		return false;
	}

	public void reset() {

	}

	public void setLocale(final Locale loc) {

	}

	public Locale getLocale() {
		return null;
	}

	public void addCookie(final Cookie cookie) {

	}

	public boolean containsHeader(final String name) {
		return false;
	}

	public String encodeURL(final String url) {
		return null;
	}

	public String encodeRedirectURL(final String url) {
		return null;
	}

	public String encodeUrl(final String url) {
		return null;
	}

	public String encodeRedirectUrl(final String url) {
		return null;
	}

	public void sendError(final int sc, final String msg) throws IOException {

	}

	public void sendError(final int sc) throws IOException {

	}

	public void sendRedirect(final String location) throws IOException {

	}

	public void setDateHeader(final String name, final long date) {

	}

	public void addDateHeader(final String name, final long date) {

	}

	public void setHeader(final String name, final String value) {

	}

	public void addHeader(final String name, final String value) {

	}

	public void setIntHeader(final String name, final int value) {

	}

	public void addIntHeader(final String name, final int value) {

	}

	public void setStatus(final int sc) {
        this.status = sc;
	}

    public void setStatus(final int sc, final String sm) {
        this.status = sc;
	}

    public int getStatus() {
		return this.status;
	}

	public String getHeader(final String name) {
		return null;
	}

	public Collection<String> getHeaders(final String name) {
		return null;
	}

	public Collection<String> getHeaderNames() {
		return null;
	}
}
