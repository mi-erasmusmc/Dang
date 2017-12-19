package nl.erasmusmc.biosemantics.eudra.solr.index;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SortableValueMap<K, V extends Comparable<V>>
extends LinkedHashMap<K, V> {
public SortableValueMap() { }

public SortableValueMap( Map<K, V> map ) {
  super( map );
}

public void sortByValue() {
  List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( entrySet() );

  Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
    public int compare( Map.Entry<K, V> entry1, Map.Entry<K, V> entry2 ) {
      return - entry1.getValue().compareTo( entry2.getValue() );
    }
  });

  clear();

  for( Map.Entry<K, V> entry : list ) {
    put( entry.getKey(), entry.getValue() );
  }
}

private static void print( String text, Map<String, Double> map ) {
  System.out.println( text );

  for( String key : map.keySet() ) {
    System.out.println( "key/value: " + key + "/" + map.get( key ) );
  }
}

/*	public static void main( String[] args ) {
	  SortableValueMap<String, Double> map =
	    new SortableValueMap<String, Double>();
	
	  map.put( "A", 67.5 );
	  map.put( "B", 99.5 );
	  map.put( "C", 82.4 );
	  map.put( "D", 42.0 );
	
	  print( "Unsorted map", map );
	  map.sortByValue();
	  print( "Sorted map", map );
	}*/
}
