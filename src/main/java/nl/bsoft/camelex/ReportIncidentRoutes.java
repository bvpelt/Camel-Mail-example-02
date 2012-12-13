package nl.bsoft.camelex;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.language.bean.BeanLanguage;
import org.apache.log4j.Logger;

public class ReportIncidentRoutes extends RouteBuilder {
	
	private static final Logger logger = Logger.getLogger(ReportIncidentRoutes.class);
	
	public void configure() throws Exception {
		logger.info("Configure route");
			 			
		from("direct:start")
		// set the filename using the bean language and call the FilenameGenerator class.
		// the 2nd null parameter is optional methodname, to be used to avoid ambiguity.
		// if not provided Camel will try to figure out the best method to invoke, as we
		// only have one method this is very simple
		.setHeader(FileComponent.HEADER_FILE_NAME, 
				BeanLanguage.bean(FilenameGenerator.class, "generateFilename"))
		.to("velocity:MailBody.vm")
		.to("file://target/subfolder");
		
		// second part from the file backup -> send email
		from("file://target/subfolder")
		// set the subject of the email
		.setHeader("subject", constant("New incident reported"))
		// send the email
		.to("smtp://someone@localhost?password=secret&to=incident@mycompany.com");
		
	} 
}