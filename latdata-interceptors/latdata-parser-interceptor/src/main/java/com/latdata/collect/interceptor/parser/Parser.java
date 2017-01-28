package com.latdata.collect.interceptor.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.logentries.re2.RE2;
import com.logentries.re2.RE2Matcher;
// import com.logentries.re2.RE2Matcher;
import com.logentries.re2.entity.NamedGroup;
import com.latdata.collect.util.LoadCmdbUtil;
import com.latdata.collect.util.LoadEtypesUtil;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * <p>
 * An interceptor developed to enrich and parse to json any log data collected
 * from any sources. After being enriched and parsed, the event data is ready to
 * be sent to where it should be stored (Kafka, Hadoop, Hive, etc).
 * </p>
 * 
 * <table>
 * <tr>
 * <td colspan=4><b><center>Configuration options</center></b></td>
 * </tr>
 * <tr>
 * <th>Parameter</th>
 * <th>Description</th>
 * <th>Unit / Type</th>
 * <th>Default</th>
 * </tr>
 * <tr>
 * <td><tt>cmdbFile</tt></td>
 * <td>CSV file that contains the CMDB database containing CI_ID,extraData. This
 * information will be used for the enrichment process. <b>Obs:</b> The CI_ID
 * expected is in the format LServer_129817, we split this and use only the
 * numbers to do the search so, if the CI_ID format that you use is different
 * from ours, changes are needed here and at DataCmdb class.</td>
 * <td>File location</td>
 * <td>none (required)</td>
 * </tr>
 * <tr>
 * <td><tt>etypeFile</tt></td>
 * <td>CSV file that contains the regex used to extract fields from each Event
 * Type (eType). This information will be used for the parser process. The CSV
 * format expected is "etype","regex".</td>
 * <td>File location</td>
 * <td>none (required)</td>
 * </tr>
 * </table>
 * <br>
 * <p>
 * TODO: Add a check to make it optional to send the raw message Json, based on
 * the eventtype
 * </p>
 */

public class Parser implements Interceptor {

	private static final Logger LOG = Logger.getLogger(Parser.class);
	private String pathFileCmdb = new String();
	private String pathFileEType = new String();
	private static Map<Integer, String> mapCmdb;
	private static Map<String, List<Object[]>> mapListeType;
	private static RE2 regex = RE2.compile("^\\d{1,9}$");

	private Parser(String pathFileCmdb, String pathFileEType) {
		this.pathFileCmdb = pathFileCmdb;
		this.pathFileEType = pathFileEType;
	}

	@Override
	public void close() {
	}

	@Override
	public void initialize() {
	}

	@Override
	public Event intercept(Event event) {
		// Create Objects
		Map<String, Object> fields = new HashMap<String, Object>();
		StringBuilder finalbody = new StringBuilder();
		ObjectMapper mapper = new ObjectMapper();
		String topic = new String();
		String timestamp = new String();
		String[] got = new String(event.getBody()).split("}\\|\\|", 2);
		String strHeaders = got[0] + "}";
		String strBody = got[1];

		LOG.debug("strBody: " + strBody);
		LOG.debug("strHeaders: " + strHeaders);
		
		// List<String> jsonHeaders = new ArrayList<String>();
		// List<jsonHeaders> list =
		// mapper.readValue(strHeaders,TypeFactory.defaultInstance().constructCollectionType(List.class,
		// jsonHeaders.class));
		Map<String, String> header = new HashMap<String, String>();
		try {
			header = new ObjectMapper().readValue(strHeaders, HashMap.class);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// event.getHeaders().put("timestamp", header.get("timestamp"));
		// event.getHeaders().put("sType", header.get("sType"));
		// event.getHeaders().put("eType", header.get("eType"));
		// event.getHeaders().put("CI_ID", header.get("CI_ID"));
		// event.getHeaders().put("extraData", header.get("extraData"));

		// matcher for number
		// RE2 regex = RE2.compile("^\\d+|\\d+\\.$");

		RE2Matcher matcher;

		// Extradata? NO -> Put it to Headers
		LOG.debug("HEADER GET EXTRADATA: " + header.get("extraData"));





		if (header.get("extraData") == null) {
			if ((header.get("extraData") == null) && (header.get("CI_ID") != null)) {
				String ciid = (header.get("CI_ID")).split("_")[1];
				event.getHeaders().put("extraData", mapCmdb.get(Integer.parseInt(ciid)));
				LOG.debug("CI_ID: " + ciid);
				try {
					HashMap<String, String> extraData = new HashMap<>();
					extraData = new ObjectMapper().readValue(event.getHeaders().get("extraData"), HashMap.class);
					finalbody.append("{\"extraData\":").append(mapper.writeValueAsString(extraData)).append(",");
					LOG.debug("extraData:" + mapper.writeValueAsString(extraData));
				} catch (IOException e1) {
					LOG.debug("FALLO AL EXTRAER - INSERTAR EXTRADATA");
					e1.printStackTrace();
				}
			}
			else {
				finalbody.append("{\"extraData\":{\"CI_ID\":\"notFound\"},");
			}
		}
		else {
			LOG.debug("TIENE COSASSSS!!");
			finalbody.append("{\"extraData\":" + header.get("extraData") + ",");
		}
	

		// Get RegEx from eType
		// try {
		LOG.debug("que pedo 2");
		LOG.debug("header eType: " + header.get("eType"));
		LOG.debug("getheadereType: " + mapListeType.get(header.get("eType")).get(0));
		Object[] listRegex = mapListeType.get(header.get("eType")).get(0);
		for (int i = 0; i < listRegex.length; i++) {
			LOG.debug("valor listRegex.(" + i + "): " + listRegex[i]);
		}

		LOG.debug("List eTypes size:" + listRegex.length);

		// Extract groupNames
		List<String> groupNames = ((RE2) listRegex[1]).getCaptureGroupNames();

		// Get Values from each groupNames for each line
		List<NamedGroup> namedCaptureGroups = ((RE2) listRegex[1]).getNamedCaptureGroups(groupNames, strBody);

		// Add some debug
		LOG.debug("namedCaptureGroups: " + namedCaptureGroups);
		LOG.debug("Size namedCaptureGroups: " + namedCaptureGroups.size());

		// // If line matched
		if (namedCaptureGroups.size() > 0) {
			LOG.debug("Matched... " + namedCaptureGroups.size());
			// propagate topic and timestamp in header, if matched:
			// topic=sType_eType
			topic = header.get("sType") + "_" + header.get("eType");
			timestamp = header.get("timestamp");

			// Debug

			// Foreachelement in namedCaptureGroups.size
			for (int i = 0; i < namedCaptureGroups.size(); i++) {
				// Put captured group in field
				if (regex.matcher(namedCaptureGroups.get(i).captureGroup.matchingText).find()) {
					LOG.debug("Entering with number... " + groupNames.get(i) + " | "
							+ Integer.parseInt(namedCaptureGroups.get(i).captureGroup.matchingText));
					fields.put(groupNames.get(i),
							Integer.parseInt(namedCaptureGroups.get(i).captureGroup.matchingText));
				} else {
					LOG.debug("Entering with string... " + groupNames.get(i) + " | "
							+ namedCaptureGroups.get(i).captureGroup.matchingText);
					fields.put(groupNames.get(i), namedCaptureGroups.get(i).captureGroup.matchingText);
				}
			}
			// Add fields to Finalbody
		} else {
			LOG.debug("NO Matched... " + namedCaptureGroups.size());
			// propagate topic and timestamp in header, if no matched:
			// topic=sType_noparsed
			topic = header.get("sType") + "_" + header.get("eType") + "_noparsed";
			timestamp = header.get("timestamp");
			// Debug
			fields.put("raw", strBody);
		}

		// AFTER THE LOOP, add timestamp and fields eventBody
		try {
			mapper.configure(Feature.WRITE_NUMBERS_AS_STRINGS, false);
			finalbody.append("\"fields\":").append(mapper.writeValueAsString(fields));
			// LOG.debug("sendMessageFile MAP: " +
			// mapListeType.get(header.get("eType")).get(0));
			// LOG.debug("sendMessageFile STRING: " + listRegex[2]);
			// LOG.debug("EventBody:" + finalbody);
			if (listRegex[2].equals("true")) {
				strBody = strBody.replace("\\", "\\\\");
				strBody = strBody.replace("\"", "\\\"");
				finalbody.append(",\"raw\":\"").append(strBody).append("\"");
				
			}

			LOG.debug("topic: " + topic);
			LOG.debug("timestamp: " + timestamp);
			event.getHeaders().clear();
			event.getHeaders().put("topic", topic);
			finalbody.append(",\"timestamp\":\"").append(timestamp).append("\"");

			finalbody.append("}");
			event.setBody(finalbody.toString().getBytes());
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		// } catch (NullPointerException npe) {
		// npe.printStackTrace();
		// }

		/*
		 * Write input "fields" JSON into EventBody and clear the EventHeaders.
		 * At this point, we have a Json with ExtraData, Fields and Message
		 * (that corresponds to the raw event body).
		 */
		// try {
		// finalbody.append("\"fields\":").append(mapper.writeValueAsString(fields)).append(",");
		// } catch (IOException ioe) {
		// ioe.printStackTrace();
		// }

		return event;
	}

	@Override
	public List<Event> intercept(List<Event> events) {
		List<Event> interceptedEvents = new ArrayList<Event>(events.size());
		for (Event event : events) {
			Event interceptedEvent = intercept(event);
			interceptedEvents.add(interceptedEvent);
		
			LOG.debug("INTERC HEADER "+interceptedEvent.getHeaders().toString());
		        LOG.debug("INTERC BODY "+interceptedEvent.getBody().toString());

		}

		return interceptedEvents;
	}

	public static class Builder implements Interceptor.Builder {
		private String pathFileEType;
		private String pathFileCmdb;

		public Builder() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void configure(Context context) {
			/*
			 * Getting the config variables from flume.conf file
			 */
			pathFileEType = context.getString("etypeFile");
			pathFileCmdb = context.getString("cmdbFile");
		}

		@Override
		public Interceptor build() {
			/*
			 * Open the needed files. They are opened here, so they are read
			 * only once for the life cycle of the flume agent
			 */
			mapCmdb = LoadCmdbUtil.obtainCmdbbyciid(pathFileCmdb);
			mapListeType = LoadEtypesUtil.loadData(pathFileEType, "eType");
			return new Parser(pathFileCmdb, pathFileEType);
		}
	}

}
