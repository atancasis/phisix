/**
 * Copyright 2012 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.phisix.api.resource;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.phisix.api.model.Price;
import com.googlecode.phisix.api.model.Stock;
import com.googlecode.phisix.api.model.Stocks;
import com.googlecode.phisix.api.parser.Parser;
import com.googlecode.phisix.api.urlfetch.URLFetchService;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;

public class StocksServletTest {

	private StocksServlet stocksServlet;
	private URLFetchService urlFetchService;
	private Parser<Reader, Stocks> parser;
	private Stock expectedStock;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		parser = mock(Parser.class);
		urlFetchService = mock(URLFetchService.class);
		stocksServlet = new StocksServlet(urlFetchService, parser);
		
		Stocks expectedStocks = new Stocks();
		expectedStocks.setAsOf(Calendar.getInstance());
		expectedStock = new Stock();
		expectedStock.setName("A");
		expectedStock.setPercentChange(new BigDecimal(0));
		Price price = new Price();
		price.setAmount(new BigDecimal(100));
		price.setCurrency("PHP");
		expectedStock.setPrice(price );
		expectedStock.setSymbol("A");
		expectedStock.setVolume(100);
		expectedStocks.getStocks().add(expectedStock );
		
		when(parser.parse(any(Reader.class))).thenReturn(expectedStocks);
		when(urlFetchService.fetch(any(URL.class))).thenReturn(mock(InputStream.class));

		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
	
	@Test
	public void getAllXmlStocks() throws Exception {
		request.setRequestURI("/stocks.xml");
		
		stocksServlet.doGet(request, response);
		
		assertTrue(response.containsHeader("Last-Modified"));
		assertEquals("text/xml", response.getContentType());
		
		String content = response.getOutputStreamContent();
		assertFalse(content.isEmpty());
		
		JAXBContext jaxbContext = JAXBContext.newInstance("com.googlecode.phisix.api.model");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
		StringReader reader = new StringReader(content);
		Stocks actualStocks = (Stocks) unmarshaller.unmarshal(reader);
		assertNotNull(actualStocks);
		assertEquals(1, actualStocks.getStocks().size());
		Stock actualStock = actualStocks.getStocks().get(0);
		assertEquals(expectedStock.getName(), actualStock.getName());
		assertEquals(expectedStock.getPercentChange(), actualStock.getPercentChange());
		assertEquals(expectedStock.getPrice().getAmount(), actualStock.getPrice().getAmount());
		assertEquals(expectedStock.getPrice().getCurrency(), actualStock.getPrice().getCurrency());
		assertEquals(expectedStock.getSymbol(), actualStock.getSymbol());
		assertEquals(expectedStock.getVolume(), actualStock.getVolume());
	}
	
	@Test
	public void getAllJsonStocks() throws Exception {
		request.setRequestURI("/stocks.json");
		
		stocksServlet.doGet(request, response);
		
		assertTrue(response.containsHeader("Last-Modified"));
		assertEquals("application/json", response.getContentType());
		
		String content = response.getWriter().toString();
		assertFalse(content.isEmpty());
	}
	
}
