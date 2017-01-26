package InformationRetrieval;

import java.util.HashMap;
import java.util.Map;


//calculate the tfidf weight for both query and documents' words
public class Calculation {
	//calculating tfidf weight for every documents
	public static float Tfidf(float sumT, float t ,float d){
		float tf = 0;
		float idf = 0;
		float tfidf = 0;
		tf = t/sumT;
		idf = (float)Math.log10(InformationRetrieval.files.length/d);
		tfidf = tf*idf;
		return tfidf;
	}
	
	//calculating tfidf weight for query only
	public static float QTfidf(float sumT, float t ,float d){
		float tf = 0;
		float idf = 0;
		float tfidf = 0;
		tf = t/sumT;
		idf = (float)Math.log10((InformationRetrieval.files.length+1)/(d+1));
		tfidf = tf*idf;
		return tfidf;
	}
	
	//calculating length normalization for every documents
	public static float docNomalization(int doc){
		float no = 0;
		for(String s :InformationRetrieval.wordsInDoc.get(doc).keySet()){
			float d=0;
			float tf =0;
			float idf =0;
			tf = InformationRetrieval.wordsInDoc.get(doc).get(s);
			idf = InformationRetrieval.index.get(s).size();
			d = Tfidf(InformationRetrieval.sumOfTf.get(doc), tf, idf);
			no += Math.pow(d, 2);
		}
		no = (float)Math.sqrt(no);
		return no;
	}
	
	//making the query to be represented by a vector of term weights.
	public static Map<String, Float> QVector (Map<String, Integer> q){
		Map<String, Float> w = new HashMap<String, Float>();
		
		float sumT = 0;
		for(String s: q.keySet()){
			sumT += q.get(s);
		}
		
		for(String s: q.keySet()){
			float t = 0;
			t = QTfidf(sumT, q.get(s), InformationRetrieval.index.get(s).size());
			w.put(s, t);
		}
		
		float no= 0;		
		for(String s: w.keySet()){
			no += Math.pow(w.get(s), 2); 
		}
		
		no = (float)Math.sqrt(no); 
		
		for(String s: w.keySet()){
			w.put(s, w.get(s)/no);
		}
		
		return w;
	}
	
}
