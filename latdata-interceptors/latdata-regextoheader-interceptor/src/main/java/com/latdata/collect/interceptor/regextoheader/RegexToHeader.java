package com.latdata.collect.interceptor.regextoheader;

import java.io.ByteArrayOutputStream;
import com.google.common.net.InetAddresses;
import com.latdata.collect.util.LoadCmdbUtil;
import com.latdata.collect.util.LoadEtypesUtil;
import com.logentries.re2.RE2;
import com.logentries.re2.RE2Matcher;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * An interceptor developed to do a few normalizations, checks and do a
 * pre-enrichment to the incoming events. Here we check if the event headers
 * already contain extraData and Source IP, so we can skip the enrichment and go
 * on to the ParsEnrich Interceptor which will parse fields and create the final
 * Jsons based ont he event data. Also, if the event headers doesn't contains
 * the extraData, we should add it based on the Source IP field. Finally, if it
 * doesn't have neither, we will try to get the Source IP from the event body.
 * </p>
 * <p>
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
 * <td>CSV file that contains the CMDB database containing IP,CI_ID. This
 * information will be used for the enrichment process. <b>Obs:</b> The CI_ID
 * expected is in the format "LServer_129817", we split this and use only the
 * numbers to do the search so, if the CI_ID format that you use is different
 * from ours, changes are needed here and at DataCmdb class.</td>
 * <td>File location</td>
 * <td>none (required)</td>
 * </tr>
 * <tr>
 * <td><tt>etypeFile</tt></td>
 * <td>CSV file that contains the necessary information to determine which is
 * the Event Type, based on a regex and also contains the regex to extract
 * Source IP from the event, if necessary. Which are informations needed for the
 * enrichment process. The CSV format expected is: "sType (Source Type)",
 * "eType (Event Type)", "Regex to match event type",
 * "Regex to extract IP from event".</td>
 * <td>File location</td>
 * <td>none (required)</td>
 * </tr>
 * </table>
 * <br>
 *
 * @author LatData
 */

public class RegexToHeader implements Interceptor {

    private static final Logger LOG = Logger.getLogger(RegexToHeader.class);
    private static Map<Long, String> mapCmdbip;
    private static Map<String, String> mapCmdbhost;
    private static Map<String, List<Object[]>> mapListsType;
    //	private static RE2 regex = RE2.compile("^\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}$");
    private static RE2 regex = RE2.compile("^(\\d|[1-9]\\d|1\\d\\d|2([0-4]\\d|5[0-5]))\\.(\\d|[1-9]\\d|1\\d\\d|2([0-4]\\d|5[0-5]))\\.(\\d|[1-9]\\d|1\\d\\d|2([0-4]\\d|5[0-5]))\\.(\\d|[1-9]\\d|1\\d\\d|2([0-4]\\d|5[0-5]))$");
    private String pathFileCmdb_ip = new String();
    private String pathFileCmdb_host = new String();
    private String pathFileEType = new String();
    private boolean cmdbFound;

    // private static Map<String, String> jsonHeaders;
    private RegexToHeader(String pathFileCmdb_ip, String pathFileCmdb_host, String pathFileEType) {
        this.pathFileCmdb_ip = pathFileCmdb_ip;
        this.pathFileCmdb_host = pathFileCmdb_host;
        this.pathFileEType = pathFileEType;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public void initialize() {
        /*
		 * try { hostValue = InetAddress.getLocalHost().getHostName(); } catch
		 * (UnknownHostException e) { throw new FlumeException(
		 * "Cannot get Hostname", e); }
		 */
    }

    @Override
    public Event intercept(Event event) {
        // This is the event's body
        cmdbFound = false;

	//String testBody = new String(event.getBody());
	//LOG.debug("JAJAJA: "+testBody);

        String[] got = new String(event.getBody()).split("\\|\\|", 2);
        String strHeaders = got[0];
        String strBody = got[1];

	//LOG.debug("STRHEADERS || "+strHeaders);
	//LOG.debug("STRBODY || "+strBody);

        ObjectMapper mapper = new ObjectMapper();
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

	event.setHeaders(header);

	event.getHeaders().put("timestamp", header.get("timestamp"));
        event.getHeaders().put("sType", header.get("sType"));

	
	LOG.debug("EVENT HEADERS AFTER"+event.getHeaders().toString());

        // try {
        // Map<String, String> headerNew= new
        // ObjectMapper().readValue(got[0]+"}", HashMap.class);
        // } catch (IOException e1) {
        // e1.printStackTrace();
        // }

		/*
		 * Get the event headers and check if there is extraData there; If it
		 * isn't and we have the Source IP field, we lookup for the CI_ID in the
		 * file pathFileCmdb, stored at the Map mapCmdb; Else, If we don't have
		 * neither extraData nor sIp, we get the sIp with a regex, from the
		 * event body;
		 */
        // Map<String, String> header = event.getHeaders();
        if ((header.get("extraData") == null) && (header.get("sIp") != null)) {
            int address32 = InetAddresses.coerceToInteger(InetAddresses.forString(header.get("sIp")));
            long address = address32 & 0xFFFFFFFFL;
            event.getHeaders().put("CI_ID", mapCmdbip.get(address));
            LOG.debug("Source Ip based CI_ID:" + mapCmdbip.get(address));
        }

		/*
		 * Based on sType header, we need to get the eType (event type), from
		 * the file pathFileEType that contains the sType, eType, regex format.
		 * With the regex, we define which is the eType for the event, that will
		 * be used on the next Flume layer, to get the proper regex for field
		 * extraction;
		 */
        List<Object[]> listeTypes = mapListsType.get(header.get("sType"));
        // if (listeTypes != null) {
        if (!listeTypes.isEmpty()) {
            LOG.debug("List eTypes size:" + listeTypes.size());
            boolean matched = false;
            for (Object[] listeTypesForPrint : listeTypes) {
                LOG.debug(listeTypesForPrint[0] + " - " + listeTypesForPrint[1]);
                RE2Matcher matcher = ((RE2) listeTypesForPrint[1]).matcher(strBody);
                if (matcher.find()) {
                    event.getHeaders().put("eType", (String) listeTypesForPrint[0]);
                    if ((!header.containsKey("CI_ID")) && (!header.containsKey("sIp"))) {
                        RE2Matcher ipmatcher = ((RE2) listeTypesForPrint[2]).matcher(strBody);
                        LOG.debug("Regex to extract sIp: " + listeTypesForPrint[2]);

                        if (ipmatcher.find()) {
                            // int groupCount = ipmatcher.groupCount(); // Contamos grupos
                            LOG.debug("groupCount: " + ipmatcher.groupCount());

                            LOG.debug("ipmatcher.find");
                            for (int i = 1; ((i < ipmatcher.groupCount()) && (cmdbFound == false)); i++) {
                                String foundsIp = ipmatcher.group(i);
                                if (foundsIp != null) {
                                    // LOG.info ("group: " + i + ", value: " + foundsIp);
                                    //LOG.info("foundsIp: " + foundsIp);
                                    if (regex.matcher(foundsIp).find()) {
                                        int address32 = InetAddresses.coerceToInteger(InetAddresses.forString(foundsIp));
                                        long address = address32 & 0xFFFFFFFFL;
                                        if (mapCmdbip.get(address) != null) {
                                            cmdbFound = true;
                                            LOG.debug("No source IP header, done with source IP extraction from. Found sIp: " + foundsIp + ". Found CI_ID:" + mapCmdbip.get(address));
                                            event.getHeaders().put("CI_ID", mapCmdbip.get(address));
                                            event.getHeaders().put("sIp", foundsIp);
                                        }
                                    } else {
                                        if (mapCmdbhost.get((foundsIp.toUpperCase().split("\\."))[0]) != null) {
                                            cmdbFound = true;
                                            LOG.debug("No source IP header, done with source IP extraction from event. Found hostname: " + (foundsIp.toUpperCase().split("\\.", 1))[0] + ". Found CI_ID:" + mapCmdbhost.get((foundsIp.toUpperCase().split("\\.", 1))[0]));
                                            event.getHeaders().put("CI_ID", mapCmdbhost.get((foundsIp.toUpperCase().split("\\."))[0]));
                                            event.getHeaders().put("sHost", foundsIp);
                                        }
                                    }
                                }
                            }
                        } else {
                            LOG.debug("The source IP couldn't be get from eventBody");
                        }
                    }
                    LOG.debug("Matched eType:" + listeTypesForPrint[0]);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                event.getHeaders().put("eType", "noeType");
                LOG.debug("No eType found");
            }
        } else {
            event.getHeaders().put("eType", "noeType");
            LOG.debug("No listeTypes found");
            LOG.debug("No eType found");
        }
	
	event.setBody(strBody.getBytes());
	//event.setBody((event.getHeaders().toString()+strBody).getBytes());

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
        private String pathFileCmdb_ip;
        private String pathFileCmdb_host;

        public Builder() {
            // TODO Auto-generated constructor stub
        }

        @Override
        public void configure(Context context) {
			/*
			 * Getting the config variables from flume conf file
			 */
            pathFileEType = context.getString("etypeFile");
            pathFileCmdb_ip = context.getString("cmdbFileIp");
            pathFileCmdb_host = context.getString("cmdbFileHost");
        }

        @Override
        public Interceptor build() {
			/*
			 * Open the needed files. They are opened here, so they are read
			 * only once.
			 */
            mapCmdbip = LoadCmdbUtil.obtainCmdbbyip(pathFileCmdb_ip);
            mapCmdbhost = LoadCmdbUtil.obtainCmdbbyhost(pathFileCmdb_host);
            mapListsType = LoadEtypesUtil.loadData(pathFileEType, "sType");
            return new RegexToHeader(pathFileCmdb_ip, pathFileCmdb_host, pathFileEType);
        }
    }

}
