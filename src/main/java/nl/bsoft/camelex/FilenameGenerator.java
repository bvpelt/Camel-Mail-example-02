package nl.bsoft.camelex;

import org.apache.camel.example.reportincident.InputReportIncident;

/**
* Plain java class to be used for filename generation based on the reported incident
*/
public class FilenameGenerator {
	
	public String generateFilename(InputReportIncident input) {
		// compute the filename
		return "incident-" + input.getIncidentId() + ".txt";
	}
}