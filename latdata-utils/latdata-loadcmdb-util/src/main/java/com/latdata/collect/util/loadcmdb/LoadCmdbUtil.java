package com.latdata.collect.util;

import com.google.common.base.CharMatcher;
import com.google.common.net.InetAddresses;

import com.logentries.re2.RE2;
import com.logentries.re2.RE2Matcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class to is invoked to load each of the cmdb entries in a Map.
 * </p>
 *
 * @author LatData
 */

public class LoadCmdbUtil {

    /**
     * Method to generate a Map that contains each of the values of the CMDB
     * file
     *
     * @param pathFileCmdb Variable that contains the path of the file where is the CMDB
     *                     data
     * @return Map (Long, String). The key is an ip address converted to Int and
     * the value is a variable with the CMDB extraData or only the
     * CI_ID, depending on the needs of the class that is invoking it.
     */
    public static Map<Long, String> obtainCmdbbyip(String pathFileCmdb) {
        Map<Long, String> mapCmdb = new HashMap<Long, String>();
        RE2 regexip = RE2.compile(
                "^(\\d|[1-9]\\d|1\\d\\d|2([0-4]\\d|5[0-5]))\\.(\\d|[1-9]\\d|1\\d\\d|2([0-4]\\d|5[0-5]))\\.(\\d|[1-9]\\d|1\\d\\d|2([0-4]\\d|5[0-5]))\\.(\\d|[1-9]\\d|1\\d\\d|2([0-4]\\d|5[0-5]))$");
        // RE2 regexip = RE2.compile("^\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}$");
        BufferedReader bufferedInputStream = null;
        try {
            File file = new File(pathFileCmdb);
            if (file.isFile() && file.exists() && file.canRead()) {
                FileReader reader = new FileReader(file);
                bufferedInputStream = new BufferedReader(reader);

                String line = null;

                while ((line = bufferedInputStream.readLine()) != null) {
                    int indexComa = line.indexOf(',');
                    String strKey = line.substring(0, indexComa);
                    String strData = line.substring(indexComa + 1);
                    int address32 = 0;
                    long address = 0;
                    /*
					 * Check if ipaddress field (strKey) contains whitespaces if
					 * so, we do a split, to get all of the ip addresses at a
					 * time so we can insert into mapCMDB Key IP Value extraData
					 * After getting the field with only one IP, we convert it
					 * to Interger - so the later search will be faster - and
					 * insert into mapCmdb. If there is no whitespace, we go
					 * directly to conversion and insertion.
					 */

                    /*if (CharMatcher.WHITESPACE.matchesAnyOf(strKey)) {*/
                    if (CharMatcher.WHITESPACE.matchesAnyOf(strKey)) {
                        for (String ip : strKey.split(" ")) {
                            if (regexip.matcher(ip).find()) {
                                if (!mapCmdb.containsKey(ip)) {
                                    address32 = InetAddresses.coerceToInteger(InetAddresses.forString(ip));
                                    address = address32 & 0xFFFFFFFFL;
                                    mapCmdb.put(address, strData);
                                }
                                // System.out.println("Loading, field with two
                                // or more ips, hashMap cmdb Key"+ ip + " Data:"
                                // +strData);

                            }
                            // System.out.println("Test:"+ ip);
                        }
                    } else {
                        if (regexip.matcher(strKey).find()) {
                            if (!mapCmdb.containsKey(strKey)) {
                                address32 = InetAddresses.coerceToInteger(InetAddresses.forString(strKey));
                                address = address32 & 0xFFFFFFFFL;
                                mapCmdb.put(address, strData);
                            }
                            // System.out.println("Loading hashMap cmdb Key"+
                            // strKey + " Data:" +strData);
                        }
                    }
                }

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }

        return mapCmdb;
    }
	/*
	 * Using this class, one is able to test the code with and .csv input public
	 * static void main(String[] args) { Map<Long, String> map =
	 * obtenerCmdb("C:\\java\\cmdb.csv"); }
	 */

    /**
     * Method to generate a Map that contains each of the values contained into
     * CMDB file
     *
     * @param pathFileCmdb Variable that contains the path of the file where is the CMDB
     *                     data
     * @return Map (Long, String). The key is the CI_ID split by "_" assuming
     * the second index, that corresponds to the numeric part of the
     * CI_ID and the value is the CMDB extraData.
     */
    public static Map<Integer, String> obtainCmdbbyciid(String pathFileCmdb) {
        Map<Integer, String> mapCmdb = new HashMap<Integer, String>();
        BufferedReader bufferedInputStream = null;
        try {
            File file = new File(pathFileCmdb);
            if (file.isFile() && file.exists() && file.canRead()) {
                FileReader reader = new FileReader(file);
                bufferedInputStream = new BufferedReader(reader);

                String line = null;

                while ((line = bufferedInputStream.readLine()) != null) {
                    int indexComa = line.indexOf(',');
                    String strKey = line.substring(0, indexComa);
                    String strData = line.substring(indexComa + 1);
                    String[] ciid = (strKey.split("_"));
                    if (!mapCmdb.containsKey(ciid[1])) {
                        mapCmdb.put(Integer.parseInt(ciid[1]), strData);
                        // System.out.println("Loading, field with ciid, hashMap
                        // cmdb Key"+ Integer.parseInt(ciid[1]) + " Data:"
                        // +strData);
                    } else if (!mapCmdb.containsKey(strKey)) {
                        mapCmdb.put(Integer.parseInt(ciid[1]), strData);
                        // System.out.println("Loading hashMap cmdb Key"+ strKey
                        // + " Data:" +strData);
                    }
                }

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
        return mapCmdb;
    }

    public static Map<String, String> obtainCmdbbyhost(String pathFileCmdb) {
        Map<String, String> mapCmdb = new HashMap<String, String>();
        BufferedReader bufferedInputStream = null;
        try {
            File file = new File(pathFileCmdb);
            if (file.isFile() && file.exists() && file.canRead()) {
                FileReader reader = new FileReader(file);
                bufferedInputStream = new BufferedReader(reader);

                String line = null;

                while ((line = bufferedInputStream.readLine()) != null) {
                    int indexComa = line.indexOf(',');
                    String strKey = line.substring(0, indexComa);
                    String strData = line.substring(indexComa + 1);
                    String host = ((strKey.toUpperCase().split("\\."))[0]);
                    if (!mapCmdb.containsKey(host)) {
                        mapCmdb.put(host, strData);
                        // System.out.println("Loading Hashmap with host,ciid: " + host + " Data:" + strData);
                    } else if (!mapCmdb.containsKey(host)) {
                        mapCmdb.put(host, strData);
                        // System.out.println("Loading Hashmap with host,ciid: " + host + " Data:" + strData);
                    }
                }

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
        return mapCmdb;
    }

}
