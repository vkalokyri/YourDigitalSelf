package com.rutgers.neemi.util;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import serf.data.MatcherMerger;
import serf.data.Record;
import serf.data.io.XMLifyYahooData;
import serf.data.storage.impl.GetRecordsFromYahooXML;
import serf.deduplication.RSwoosh;
import serf.test.TestException;

public class ER {
    static String fileSource = "yd100.xml";
    //static String configFile = "config";
    static String outputFile = null;
    static Class matcherMerger;
    static Class algorithm;
    static final String MATCHER_MERGER_INTERFACE = "serf.data.MatcherMerger";
    static Properties properties = new Properties();

    public ER(Context context) throws TestException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException {

        ConfigReader config = new ConfigReader(context);


        /*get the keywords to search in the documents*/
        fileSource = config.getStr(PROPERTIES.File_Source);
        properties=config.getProperties();


        // fileSource = properties.getProperty("FileSource");
        if (fileSource == null) {
            throw new TestException("No File Source specified!");
        } else {
            matcherMerger = Class.forName(config.getStr(PROPERTIES.MatcherMerger));
            if (matcherMerger == null) {
                throw new TestException("No MatcherMerger Class specified!");
            } else if (!checkMatcherMergerInterface(matcherMerger)) {
                throw new TestException("Given MatcherMerger class does not implement SimpleMatcherMerger interface!");
            } else {
                runRSwoosh(context, config);
            }
        }
    }

    private static boolean checkMatcherMergerInterface(Class testClass) {
        Class[] interfaces = testClass.getInterfaces();
        Class superClass = testClass.getSuperclass();

        try {
            for(int i = 0; i < interfaces.length; ++i) {
                if (interfaces[i] == Class.forName("serf.data.MatcherMerger")) {
                    return true;
                }
            }

            if (superClass != null && checkMatcherMergerInterface(superClass)) {
                return true;
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return false;
    }

    private static void runRSwoosh(Context context, ConfigReader config) throws SAXException, IOException, ParserConfigurationException {
        InputSource is = new InputSource(context.openFileInput(fileSource));
        GetRecordsFromYahooXML yds = new GetRecordsFromYahooXML(is);
        yds.parseXML();
        Set<Record> records = yds.getAllRecords();
        Class[] mmPartypes = new Class[1];

        try {
            mmPartypes[0] = Properties.class;
            Constructor mmConstructor = matcherMerger.getConstructor(mmPartypes);
            Object matcherMerger = mmConstructor.newInstance(properties);
            System.out.println("Running RSwoosh on " + records.size() + " records.");
            Set<Record> result = RSwoosh.execute((MatcherMerger)matcherMerger, records);
            System.out.println("After running RSwoosh, there are " + result.size() + " records.");
            if ((outputFile = properties.getProperty("OutputFile")) != null) {

                File file = context.getFileStreamPath(outputFile);
                FileOutputStream fos2 = context.openFileOutput(outputFile, Context.MODE_PRIVATE);
                BufferedWriter fw  = new BufferedWriter(new OutputStreamWriter(fos2,"UTF-8"));//new FileWriter(xmlOutputFile);
                fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                //openRecordSet(fw);

                //FileWriter fw = new FileWriter(outputFile);
                //fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                XMLifyYahooData.openRecordSet(fw);
                Iterator var8 = result.iterator();

                while(var8.hasNext()) {
                    Record r = (Record)var8.next();
                    XMLifyYahooData.serializeRecord(r, fw);
                }

                XMLifyYahooData.closeRecordSet(fw);
                fw.close();
            }
        } catch (Exception var9) {
            System.out.println(var9);
        }

    }
}
