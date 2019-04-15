package com.rutgers.neemi.util;

import android.content.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;


public class OpenNLP {

    Context context;

    public OpenNLP(Context context) {
        this.context=context;
    }


    public void findName() throws IOException {
        InputStream is = context.getAssets().open("en-ner-person.bin");

        // load the model from file
        TokenNameFinderModel model = new TokenNameFinderModel(is);
        is.close();

        // feed the model to name finder class
        NameFinderME nameFinder = new NameFinderME(model);

        // input string array
        String[] sentence = new String[]{
                "John",
                "Smith",
                "is",
                "standing",
                "next",
                "to",
                "bus",
                "stop",
                "and",
                "waiting",
                "for",
                "Mike",
                "."
        };

        Span nameSpans[] = nameFinder.find(sentence);

        // nameSpans contain all the possible entities detected
        for(Span s: nameSpans){
            System.out.print(s.toString());
            System.out.print("  :  ");
            // s.getStart() : contains the start index of possible name in the input string array
            // s.getEnd() : contains the end index of the possible name in the input string array
            for(int index=s.getStart();index<s.getEnd();index++){
                System.out.print(sentence[index]+" ");
            }
            System.out.println();
        }
    }

//    public ArrayList<String> findName(String text) throws IOException {
//
//        ArrayList<String> people = new ArrayList<>();
//
//        InputStream is = context.getAssets().open("en-ner-person.bin");
//
//        // load the model from file
//        TokenNameFinderModel model = new TokenNameFinderModel(is);
//        is.close();
//
//        // feed the model to name finder class
//        NameFinderME nameFinder = new NameFinderME(model);
//
//        // input string array
//        String[] sentence = text.split(" ");
//
//        Span nameSpans[] = nameFinder.find(sentence);
//
//        // nameSpans contain all the possible entities detected
//        for(Span s: nameSpans){
//            System.out.print(s.toString());
//            System.out.print("  :  ");
//            // s.getStart() : contains the start index of possible name in the input string array
//            // s.getEnd() : contains the end index of the possible name in the input string array
//            StringBuilder personName = new StringBuilder();
//            for(int index=s.getStart();index<s.getEnd();index++){
//                personName.append(sentence[index]);
//                personName.append(" ");
//                System.out.print(sentence[index]+" ");
//            }
//            people.add(personName.toString());
//            System.out.println();
//        }
//
//        return people;
//    }

//    /**
//     * method to find locations in the sentence
//     * @throws IOException
//     */
//    public ArrayList<String>  findLocation(String text) throws IOException {
//
//        ArrayList<String> locations = new ArrayList<>();
//
//        InputStream is = context.getAssets().open("en-ner-location.bin");
//
//        // load the model from file
//        TokenNameFinderModel model = new TokenNameFinderModel(is);
//        is.close();
//
//        // feed the model to name finder class
//        NameFinderME nameFinder = new NameFinderME(model);
//
//        // input string array
//        String[] sentence = text.split(" ");
//
//
//        Span nameSpans[] = nameFinder.find(sentence);
//
//        // nameSpans contain all the possible entities detected
//        for(Span s: nameSpans){
//            System.out.print(s.toString());
//            System.out.print("  :  ");
//            // s.getStart() : contains the start index of possible name in the input string array
//            // s.getEnd() : contains the end index of the possible name in the input string array
//            StringBuilder locationName = new StringBuilder();
//            for(int index=s.getStart();index<s.getEnd();index++){
//                locationName.append(sentence[index]);
//                locationName.append(" ");
//                System.out.print(sentence[index]+" ");
//            }
//            locations.add(locationName.toString());
//            System.out.println();
//        }
//
//
//        return locations;
//    }
//
//
//
//
}
