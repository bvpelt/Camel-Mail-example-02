package nl.bsoft.camelex;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.example.reportincident.InputReportIncident;
import org.apache.camel.example.reportincident.OutputReportIncident;
import org.apache.camel.example.reportincident.ReportIncidentEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.log4j.Logger;

/**
 * The webservice we have implemented.
 */
public class ReportIncidentEndpointImpl implements ReportIncidentEndpoint {

	private static final Logger logger = Logger
			.getLogger(ReportIncidentEndpointImpl.class);

	private CamelContext context = null;

	private ProducerTemplate producer;

	public ReportIncidentEndpointImpl() throws Exception {
		init(false);
	}

	public ReportIncidentEndpointImpl(final boolean enableConsumer)
			throws Exception {
		init(enableConsumer);
	}

	private void init(final boolean enableConsumer) throws Exception {
		logger.info(">>> init <<<");

		// create the context
		context = new DefaultCamelContext();

		// append the routes to the context
		context.addRoutes(new ReportIncidentRoutes());

		// at the end start the camel context
		context.start();
	}

	/**
	 * This is the last solution displayed that is the most simple
	 */
	public OutputReportIncident reportIncident(
			final InputReportIncident parameters) {
		
		producer = context.createProducerTemplate();
		Object mailBody = producer.sendBodyAndHeader(
				"direct:start", parameters, FileComponent.HEADER_FILE_NAME, "incident.txt");
		
		System.out.println("Body:" + mailBody);
		
		// return an OK reply
		OutputReportIncident out = new OutputReportIncident();
		out.setCode("OK");
		return out;
	}

	private void generateEmailBodyAndStoreAsFile(
			final InputReportIncident parameters) {
		logger.info(">>> generateEmailBodyAndStoreAsFile <<<");
		// generate the mail body using velocity template
		// notice that we just pass in our POJO (= InputReportIncident) that we
		// got from Apache CXF to Velocity.
		final Object response = producer.sendBody("velocity:MailBody.vm",
				parameters);
		// Note: the response is a String and can be cast to String if needed

		// store the mail in a file
		final String filename = "mail-incident-" + parameters.getIncidentId()
				+ ".txt";
		producer.sendBodyAndHeader("file://target/subfolder", response,
				FileComponent.HEADER_FILE_NAME, filename);
	}

	private void addMailSendConsumer() throws Exception {
		logger.info(">>> addMailSendConsumer <<<");
		// Grab the endpoint where we should consume. Option - the first poll
		// starts after 2 seconds
		Endpoint endpoint = context
				.getEndpoint("file://target/subfolder?consumer.initialDelay=2000");

		// create the event driven consumer
		// the Processor is the code what should happen when there is an event
		// (think it as the onMessage method)
		Consumer consumer = endpoint.createConsumer(new Processor() {
			public void process(Exchange exchange) throws Exception {
				// get the mail body as a String
				String mailBody = exchange.getIn().getBody(String.class);

				// okay now we are read to send it as an email
				System.out.println("Sending email...");
				sendEmail(mailBody);
				System.out.println("Email send");
			}
		});

		// star the consumer, it will listen for files
		consumer.start();
	}

	private void sendEmail(final String body) {
		logger.info(">>> sendEmail <<<");
		// send the email to your mail server
		String url = "smtp://someone@localhost?password=secret&to=incident@mycompany.com";
		producer.sendBodyAndHeader(url, body, "subject",
				"New incident reported");
	}

	public ProducerTemplate getTemplate() {
		if (null == producer) {
			producer = context.createProducerTemplate();
		}
		return producer;
	}

}