package org.zehetner.homeautomation.server.responder;

import javax.servlet.http.HttpServletRequest;

public class RootPageResponder implements PageResponder {

	@Override
    public String respond(final HttpServletRequest request) {
		final StringBuffer buffer = new StringBuffer();

		final String wundergroundSticker = "<a href=\"http://www.wunderground.com/cgi-bin/findweather/getForecast?query=zmw:80002.3.99999&bannertypeclick=wu_blueglass\"><img src=\"http://weathersticker.wunderground.com/weathersticker/cgi-bin/banner/ban/wxBanner?bannertype=wu_blueglass&airportcode=KDEN&ForcedCity=Denver&ForcedState=CO&zipcode=80002&language=EN\" alt=\"Click for Denver, Colorado Forecast\" height=\"90\" width=\"160\" /></a>";

		buffer.append(wundergroundSticker);

		return buffer.toString();
	}

}
