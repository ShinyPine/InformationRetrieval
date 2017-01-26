package InformationRetrieval;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Search {
	
	//get the query from user, and search them by using vector space model
	 public static void task2(String query){
			Pattern p = Pattern.compile("([a-zA-Z0-9]+)");
			Matcher m = p.matcher(query);
			Map<String, Integer> qw = new HashMap<String, Integer>();
			Map<String, Float> qv = new HashMap<String, Float>();
			Map<Integer, Float> similarity = new HashMap<Integer, Float>();
			
			String s;
			while(m.find())
			{			
				s = m.group().toLowerCase();				
				if (qw.containsKey(s)==false){
					qw.put(s, 1);
				}else {
					qw.put(s, qw.get(s)+1);
				}				
			}
			
			for(String term: qw.keySet()){
				if(InformationRetrieval.index.containsKey(term)==false){
					System.out.println("No documents martch your search");
					return;
				}
			}
			
			qv = Calculation.QVector(qw);
			
			//calculating cosine scores between every doc with terms in query, then rank them!
			boolean allqInVsm = true;
			
			for(String q: qv.keySet()){
				if(InformationRetrieval.tn.containsKey(q)==false)
					allqInVsm = false;
			}
			
			if(allqInVsm == true){
				for(int doc = 0; doc<InformationRetrieval.files.length; doc++){
					float cos = 0;
					boolean allqInThisDoc = true;
					for(String q: qv.keySet()){
						if(InformationRetrieval.vsmr[doc][InformationRetrieval.tn.get(q)]==0.0)
							allqInThisDoc = false;
					}
					
					if(allqInThisDoc == true){
						for(String q: qv.keySet()){
							cos += (qv.get(q)*InformationRetrieval.vsmr[doc][InformationRetrieval.tn.get(q)]);
						}
					}
					
					if(cos != 0.0)
						similarity.put(doc, cos);
				}					
			}
									
			if(similarity.size()!=0)
				searchResult(similarity);
			else
				System.out.println("No documents martch your search");						
	 }
	 
	//get the query from user, and search them by using inverted index to speed up
	 public static void task3(String query){
			Pattern p = Pattern.compile("([a-zA-Z0-9]+)");
			Matcher m = p.matcher(query);
			Map<Integer, Float> last = null;
			Map<Integer, Float> now = null;
			Map<String, Integer> qw = new HashMap<String, Integer>();
			Map<String, Float> qv = new HashMap<String, Float>();
			Map<Integer, Float> similarity = new HashMap<Integer, Float>();

				String s;
				while(m.find())
				{			
					s = m.group().toLowerCase();
					now = InformationRetrieval.indexOfTfidf.get(s);
					
					if(now == null)
					{
						System.out.println("No documents martch your search");
						return;
					}
					
					if(last == null)
					{
						last = now;
					}else {
						last = intersection(last, now);
					}	
	
					if (qw.containsKey(s)==false){
						qw.put(s, 1);
					}else {
						qw.put(s, qw.get(s)+1);
					}				
				}

			
			qv = Calculation.QVector(qw);
			
			if(last == null||last.size() == 0) 
			{
				System.out.println("No documents martch your search");
			}
			else {
				
				for(int i: last.keySet()){
					float cos = 0;
					for(String q: qv.keySet()){
						cos += (qv.get(q)*InformationRetrieval.indexOfTfidf.get(q).get(i));						
					}
					similarity.put(i,cos);
				}				
				searchResult(similarity);
			}
					
	 }
	 
	 
	 //find the documents that contains all the keywords that user want to search
	 public static Map<Integer, Float> intersection(Map<Integer, Float> last,Map<Integer, Float> now){
		 	Map<Integer, Float> map = new HashMap<Integer, Float>(last); 
			map.keySet().retainAll( now.keySet() );
			for(int s: map.keySet())
			{
				map.put(s, last.get(s)+now.get(s));
			}
			return map;		 
	 }
	 
	 
	 // sort the documents that contain the keywords by the cosine similarity, and print them
	 public static void searchResult(Map<Integer, Float> l){
		 
		 Map<Integer, Float> rankedFrequency = InformationRetrieval.Ranking(l);
		 
		 Iterator<Entry<Integer, Float>> frequency = rankedFrequency.entrySet().iterator();
		 
		 System.out.println("The most relevant documents are (at most top 10):");
		 int i = 0;
		 while(frequency.hasNext() && i<10){
			 Entry<Integer, Float> e = frequency.next();
			 System.out.println((i+1)+". "+InformationRetrieval.files[e.getKey()].getName() +" ("+ e.getValue()+")" );
			 i++;
		 }
		 System.out.println("the total number of result: "+rankedFrequency.size());
	 }
}
