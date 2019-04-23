package com.rutgers.neemi.util;

import android.content.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;


public class OpenNLP {

    Context context;
    TokenNameFinderModel personModel;
    TokenNameFinderModel locationModel;
    TokenNameFinderModel organizationModel;

    public OpenNLP(Context context) throws IOException {
        this.context=context;

        InputStream is = context.getAssets().open("en-ner-person.bin");
        // load the model from file
        personModel = new TokenNameFinderModel(is);
        is.close();

        is = context.getAssets().open("en-ner-location.bin");
        // load the model from file
        locationModel = new TokenNameFinderModel(is);
        is.close();

        is = context.getAssets().open("en-ner-organization.bin");
        // load the model from file
        organizationModel = new TokenNameFinderModel(is);
        is.close();
    }


    public ArrayList<String> findName(String text) throws IOException {

        ArrayList<String> people = new ArrayList<>();



        // feed the model to name finder class
        NameFinderME nameFinder = new NameFinderME(personModel);

        // input string array
        String[] tokens = tokenize(text);

        Span nameSpans[] = nameFinder.find(tokens);

        // nameSpans contain all the possible entities detected
        for(Span s: nameSpans){
            System.err.print(s.toString());
            System.err.print("  :  ");
            // s.getStart() : contains the start index of possible name in the input string array
            // s.getEnd() : contains the end index of the possible name in the input string array
            StringBuilder personName = new StringBuilder();
            for(int index=s.getStart();index<s.getEnd();index++){
                personName.append(tokens[index]);
                personName.append(" ");
                System.err.print(tokens[index]+" ");
            }
            people.add(personName.toString());
            System.err.println();
        }

        return people;
    }

    /**
     * method to find locations in the sentence
     * @throws IOException
     */
    public ArrayList<String>  findLocation(String text) throws IOException {

        ArrayList<String> locations = new ArrayList<>();

        // feed the model to name finder class
        NameFinderME nameFinder = new NameFinderME(locationModel);

        // input string array
        String[] tokens = tokenize(text);

        Span nameSpans[] = nameFinder.find(tokens);

        // nameSpans contain all the possible entities detected
        for(Span s: nameSpans){
            System.err.print(s.toString());
            System.err.print("  :  ");
            // s.getStart() : contains the start index of possible name in the input string array
            // s.getEnd() : contains the end index of the possible name in the input string array
            StringBuilder locationName = new StringBuilder();
            for(int index=s.getStart();index<s.getEnd();index++){
                locationName.append(tokens[index]);
                locationName.append(" ");
                System.err.print(tokens[index]+" ");
            }
            locations.add(locationName.toString());
            System.err.println();
        }


        return locations;
    }


    public ArrayList<String>  findOrganization(String text) throws IOException {

        ArrayList<String> locations = new ArrayList<>();

        // feed the model to name finder class
        NameFinderME nameFinder = new NameFinderME(organizationModel);

        // input string array
        String[] tokens = tokenize(text);

        Span nameSpans[] = nameFinder.find(tokens);


        // nameSpans contain all the possible entities detected
        for(Span s: nameSpans){
            System.err.print(s.toString());
            System.err.print("  :  ");
            // s.getStart() : contains the start index of possible name in the input string array
            // s.getEnd() : contains the end index of the possible name in the input string array
            StringBuilder organization = new StringBuilder();
            for(int index=s.getStart();index<s.getEnd();index++){
                organization.append(tokens[index]);
                organization.append(" ");
                System.err.print(tokens[index]+" ");
            }
            locations.add(organization.toString());
            System.err.println();
        }


        return locations;
    }


    public String[] tokenize(String sentence) throws IOException{
        InputStream inputStreamTokenizer = context.getAssets().open("en-token.bin");
        TokenizerModel tokenModel = new TokenizerModel(inputStreamTokenizer);
        TokenizerME tokenizer = new TokenizerME(tokenModel);
        return tokenizer.tokenize(sentence);
    }




}
