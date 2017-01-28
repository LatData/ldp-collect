package com.latdata.collect.util;

import com.logentries.re2.RE2;
import com.logentries.re2.RE2Matcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class to is invoked to load the eType Files.
 * </p>
 *
 * @author LatData
 */

public class LoadEtypesUtil {
    private static final String REGEX_EVENT_TYPE = "\"(\\S+)\",\"(\\S+)\",\"(\\S+)\"";
    private static final String REGEX_SOURCE_TYPE = "\"(\\S+)\",\"(\\S+)\",\"(\\S+)\",\"(\\S+)\"";
    private static final RE2 PATTERN_REGEX_EVENT_TYPE;
    private static final RE2 PATTERN_REGEX_SOURCE_TYPE;

    static {
        PATTERN_REGEX_EVENT_TYPE = RE2.compile(REGEX_EVENT_TYPE);
        PATTERN_REGEX_SOURCE_TYPE = RE2.compile(REGEX_SOURCE_TYPE);
    }

    /**
     * Method to generate a Map that contains each of the values of the
     * EventType files
     *
     * @param pathFile Variable that contains the path of the file where is the
     *                 EventType data
     * @return Map (Long, String). The key is the Event Type field and the and
     * the other values are variable, depending on the class invoking
     * it.
     */
    public static Map<String, List<Object[]>> loadData(String pathFile, String type) {
        Map<String, List<Object[]>> mapDatasType = new HashMap<>();
        BufferedReader bufferedInputStream = null;
        try {
            File file = new File(pathFile);
            FileReader reader = new FileReader(file);
            bufferedInputStream = new BufferedReader(reader);
            String line = null;
            RE2Matcher matcher;
            while ((line = bufferedInputStream.readLine()) != null) {
                if (type == "sType") {
                    matcher = PATTERN_REGEX_SOURCE_TYPE.matcher(line);
                    if (matcher.find()) {
                        String sTypeFile = matcher.group(1);
                        String eTypeFile = matcher.group(2);
                        String eRegexFile = matcher.group(3);
                        String extractsIpFile = matcher.group(4);
                        if (mapDatasType.containsKey(sTypeFile)) {
                            List<Object[]> temp = mapDatasType.get(sTypeFile);
                            Object[] StringTemp = new Object[3];
                            StringTemp[0] = eTypeFile;
                            StringTemp[1] = RE2.compile(eRegexFile);
                            StringTemp[2] = RE2.compile(extractsIpFile);
                            temp.add(StringTemp);
                            mapDatasType.put(sTypeFile, temp);
                        } else {
                            ArrayList<Object[]> temp = new ArrayList<>();
                            Object[] StringTemp = new Object[3];
                            StringTemp[0] = eTypeFile;
                            StringTemp[1] = RE2.compile(eRegexFile);
                            StringTemp[2] = RE2.compile(extractsIpFile);
                            temp.add(StringTemp);
                            mapDatasType.put(sTypeFile, temp);
                        }
                    }
                } else if (type == "eType") {
                    matcher = PATTERN_REGEX_EVENT_TYPE.matcher(line);
                    if (matcher.find()) {
                        String eTypeFile = matcher.group(1);
                        String regexFile = matcher.group(2);
                        String sendMessageFile = matcher.group(3);

                        if (mapDatasType.containsKey(eTypeFile)) {
                            List<Object[]> temp = mapDatasType.get(eTypeFile);
                            Object[] StringTemp = new Object[3];
                            StringTemp[0] = eTypeFile;
                            StringTemp[1] = RE2.compile(regexFile);
                            StringTemp[2] = sendMessageFile;
                            temp.add(StringTemp);
                            mapDatasType.put(eTypeFile, temp);
                        } else {
                            ArrayList<Object[]> temp = new ArrayList<>();
                            Object[] StringTemp = new Object[3];
                            StringTemp[0] = eTypeFile;
                            StringTemp[1] = RE2.compile(regexFile);
                            StringTemp[2] = sendMessageFile;
                            temp.add(StringTemp);
                            mapDatasType.put(eTypeFile, temp);
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
        return mapDatasType;
    }

    public static Map<String, List<Object[]>> obtainRegex(String pathFile) {
        return null;
    }
}
