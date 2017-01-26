package InformationRetrieval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InformationRetrieval{
	
	 private static String txt = "";
	 private static int totalNumberOfWords;
	 private static Map<String, Integer> vocabularySize = new HashMap<String,Integer>();
	 
	 static Map<String, Integer> tn = new LinkedHashMap<String, Integer>();//give a id to every term in top 500 frequency.
	 static Map<Integer, String> nt = new LinkedHashMap<Integer, String>();//give a term to every id in top 500 frequency.
	 
	 //Building Inverted Index
	 static Map<String, Map<Integer, Integer>> index = 
			 new HashMap<String,Map<Integer, Integer>>();
	 static File[] files;
	 	 	
	 //build the vector space model representations.
	 static float[][] vsmr;
	 
	 static Map<Integer, Map<String, Integer>> wordsInDoc = 
			 new HashMap<Integer, Map<String, Integer>>();
	 
	 static Map<String, Map<Integer, Float>> indexOfTfidf = 
			 new HashMap<String,Map<Integer, Float>>();
	 
	 //get the sum of the frequencies of all the words in each document
	 static Map<Integer, Float> sumOfTf = new HashMap<Integer, Float>();
	 
	 /** Main method */
	 public static void main(String[] args) throws IOException{
		 System.out.println("Please enter the corpus directory: "); 		 
		 Scanner scan = new Scanner(System.in);
		 String filepath =  scan.nextLine();//enter the corpus directory
		 //String filepath = "data\\cranfieldDocs";

		 Tokenizing(filepath);
		 transformation();

		 RemovingStopwords();// remove the common words

		 Statistics();
		 
		 vsmr = new float[files.length][nt.size()];//there 1400 documents and 500 words, so the model should be 1400X500. 
		 BuildVectorSpaceModel();
		 
		 /** This method is for getting the size of vector space model */
		 //try {getIndexSize();} catch (Exception e) {}

		 System.out.println("\nPlease enter the keywords of a search query : "); 		 		 
					 
		 String query = scan.nextLine();//enter the keywords of a search query
		 scan.close();
		 
		 /** This part is for task 2 */
		 /*
		 //The vector space model representations is used to perform search (Task 2)
		 long time1 = System.currentTimeMillis() ; 
		 Search.task2(query); 	
		 long time2 = System.currentTimeMillis() ; 
		 System.out.println("The query execution time is "+(time2-time1)+"ms");
		 */
		 
		 //Inverted Index is used to speed up this search (Task 3)
		 long time3 = System.currentTimeMillis() ; 
		 Search.task3(query); 	
		 long time4 = System.currentTimeMillis() ; 
		 System.out.println("The query execution time is "+(time4-time3)+"ms");//get the query execution time
	 }
	 
	 public static void BuildVectorSpaceModel (){		
		 		 
		 for(int i: wordsInDoc.keySet()){
			 float sum = 0;
			 for(String s: wordsInDoc.get(i).keySet()){
				 sum += wordsInDoc.get(i).get(s);
			 }
				 sumOfTf.put(i, sum);
		 }
		 		 
		 for(int i: wordsInDoc.keySet()){
			 wordsInDoc.get(i).keySet().retainAll(tn.keySet());
		 }		 
		 
		 //Building the vector space model representations for the top 500 frequent words in corpus
		//This part is for build the vector space model by using 2-d array
		 for(int doc = 0; doc<files.length; doc++){
			 float no =0;
			 no = Calculation.docNomalization(doc);
			 for(int termNum = 0; termNum<nt.size(); termNum++){
				 if(wordsInDoc.get(doc).containsKey(nt.get(termNum))==true){	
					 	float d =0;						
						float tfidf = 0;
						d = Calculation.Tfidf(sumOfTf.get(doc), index.get(nt.get(termNum)).get(doc), index.get(nt.get(termNum)).size());						
						tfidf = d/no;
						vsmr[doc][termNum] = tfidf;						
					 }else{
						vsmr[doc][termNum] = 0;	
					 }
			 }
		 }
		 
		 //create another index, but change the frequency to tfidf weight
		 for(String word: tn.keySet()){
			 indexOfTfidf.put(word, new HashMap<Integer, Float>());
			 for(int doc: index.get(word).keySet()){
					indexOfTfidf.get(word).put(doc, vsmr[doc][tn.get(word)]);
			 }
		 }
	 } 
	 
	 //output the index as file, and get the size of inverted index.
	 public static void getIndexSize() throws Exception{
		 String indexOutput ="P:\\vsmr";//change the indexOutput, if you want put it somewhere else
		 FileOutputStream fops = new FileOutputStream(indexOutput);
		 ObjectOutputStream oops = new ObjectOutputStream(fops);
		 oops.writeObject(vsmr);
		 oops.close();
	 }
	 
	 // the inverted index will also be built in this function
	 //Eliminating SGML tags, special character and upper-case changed to lower-case 
	 public static void Tokenizing(String s) throws IOException
	 {

		 File file = new File(s); 
		 files = file.listFiles();
		 String stringLine = "";
		 String words;
		 
		 for (int i = 0; i <files.length; i++)
		 {			 
			 FileInputStream f = new FileInputStream(files[i]);
			 BufferedReader b = new BufferedReader(new InputStreamReader(f));	
			 words ="";
			 
			 wordsInDoc.put(i, new HashMap<String, Integer>());
			 
			 while((stringLine = b.readLine()) != null)
			 {
				 words += stringLine+"\n";				 
			 }
			 words = words.replaceAll("<[^<>]+>", " ").toLowerCase();
			 txt += words;
			 
			 Pattern p = Pattern.compile("[a-z0-9]+");
			 Matcher m = p.matcher(words);
			 String word; 
			 while(m.find())
			 {		 
				 word = m.group();
				 if (index.containsKey(word) == false)
				 {
					 index.put(word, new HashMap<Integer,Integer>());
					 index.get(word).put(i, 1);//using integer i as the id of documents
				 }else {					 
					 if(index.get(word).containsKey(i)==false)
					 {
						 index.get(word).put(i, 1);
					 }else{
						 index.get(word).put(i, index.get(word).get(i)+1);
					 }
				 }
				 
				 if(wordsInDoc.get(i).containsKey(word)==false)
				 {
					 wordsInDoc.get(i).put(word, 1);
				 }else{
					 wordsInDoc.get(i).put(word, wordsInDoc.get(i).get(word)+1);
				 }
			}
		 b.close();
		 }
	 }	 
	 
	 //count the total number of words and unique words
	 public static void transformation ()
	 {		 
		 //according to the requirement of assignment,
		 //A token is a string of alphanumeric characters ([a-z0-9])
		 Pattern p = Pattern.compile("[a-z0-9]+");
		 Matcher m = p.matcher(txt);
		 
		 String s; 
		 int countor =0;
		 while(m.find())
		 {		 
			 ++countor;
			 s = m.group();
			 if (vocabularySize.containsKey(s) == false)
			 {
				 vocabularySize.put(s,1);
			 }else{
				 vocabularySize.put(s,vocabularySize.get(s)+1);
			 }
		 }
		 totalNumberOfWords = countor;
	 }
	 
	 
	 //print the total number of words, unique words and top 20(and 20 least) words in the ranking
	 public static void Statistics()
	 {		
		 System.out.println("Total number of words (excluding common/stop words) in the collection : "
				 +totalNumberOfWords+"\nVocabulary size : "+vocabularySize.size());	
		 
		 //sort items in the collection
		 Map<String, Integer> rankedWords = Ranking(vocabularySize);

		 List<Entry<String, Integer>> sw = new ArrayList<Entry<String, Integer>>(rankedWords.entrySet());
		 
		 //get the top 500 frequent words
		 for(int i = 0; i<500; i++){
			 tn.put(sw.get(i).getKey(), i);
			 nt.put(i, sw.get(i).getKey());
		 }
 		 
	 }
	  
	 // sort the items by their frequency
	 public static <K, V extends Comparable<? super V>> Map<K, V> Ranking( Map<K, V> map )
	 {
	     List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
	     Collections.sort( list, new Comparator<Map.Entry<K, V>>()
	     {
	         public int compare( Map.Entry<K, V> word1, Map.Entry<K, V> word2 )
	         {
	             return (word2.getValue()).compareTo( word1.getValue() );
	         }
	     } );	
	     Map<K, V> rankedMap = new LinkedHashMap<K, V>();
	     for (Map.Entry<K, V> entry : list)
	     {
	    	 rankedMap.put( entry.getKey(), entry.getValue() );
	     }
	     return rankedMap;
	 }
	 
	 //removing the common words and count the reduced total number of words again
	 public static void RemovingStopwords() throws IOException
	 {			
		 String commonWordsPath = "data\\common_words.txt";//change the commonWordsPath, if you want put it somewhere else
		 ArrayList<String> commonWorks = new ArrayList<String>();
		 String line = "";
		 File fcw= new File(commonWordsPath);
		 FileInputStream fi = new FileInputStream(fcw);
		 BufferedReader bu = new BufferedReader(new InputStreamReader(fi));
		 
		 while( (line = bu.readLine()) != null) 
		 {
			 commonWorks.add(line);
		 }
		 bu.close();
	 
		 for( String word: commonWorks )
		 {
			 vocabularySize.remove(word);//removing the common words
			 index.remove(word);
			 
			 for(int i : wordsInDoc.keySet()){
				 wordsInDoc.get(i).remove(word);
			 }
		 }
		 

		 Iterator<Entry<String, Integer>> it = vocabularySize.entrySet().iterator();
		 int wordsSum = 0;
		 while(it.hasNext())
		 {
			 Map.Entry<String, Integer> vs = (Map.Entry<String, Integer>)it.next();
			 wordsSum += vs.getValue();//count the reduced total number of words again
		 }
		 totalNumberOfWords = wordsSum;
	 } 	 
	 
}
