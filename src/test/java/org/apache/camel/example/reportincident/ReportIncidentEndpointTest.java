package org.apache.camel.example.reportincident;

import junit.framework.TestCase;
import javax.xml.ws.Endpoint;

import nl.bsoft.camelex.ReportIncidentEndpointImpl;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.log4j.Logger;

public class ReportIncidentEndpointTest extends TestCase {
	static Logger logger = Logger.getLogger(ReportIncidentEndpointTest.class);

	private static String ADDRESS = "http://localhost:9090/unittest";

	protected void startServer() throws Exception {
		logger.info(">>> startServer <<<");
		// We need to start a server that exposes or webservice during the unit
		// testing
		// We use jaxws to do this pretty simple
		ReportIncidentEndpointImpl server = new ReportIncidentEndpointImpl();
		Endpoint.publish(ADDRESS, server);
	}

	protected ReportIncidentEndpoint createCXFClient() {
		logger.info(">>> createCXFClient <<<");
		// we use CXF to create a client for us as its easier than JAXWS and
		// works
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(ReportIncidentEndpoint.class);
		factory.setAddress(ADDRESS);
		return (ReportIncidentEndpoint) factory.create();
	}

	public void testRendportIncident() throws Exception {
		logger.info(">>> testRendportIncident <<<");
		startServer();

		ReportIncidentEndpoint client = createCXFClient();

		InputReportIncident input = new InputReportIncident();
		input.setIncidentId("123");
		input.setIncidentDate("2008-07-16");
		input.setGivenName("Claus");
		input.setFamilyName("Ibsen");
		input.setSummary("bla bla");
		input.setDetails("more bla bla");
		input.setEmail("davsclaus@apache.org");
		input.setPhone("+45 2962 7576");

		OutputReportIncident out = client.reportIncident(input);
		assertEquals("Response code is wrong", "OK", out.getCode());

		// give the event driven consumer time to react
		Thread.sleep(10 * 1000);
	}
}
