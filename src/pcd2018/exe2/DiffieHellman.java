package pcd2018.exe2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

/**
 * Classe da completare per l'esercizio 2.
 */
public class DiffieHellman {

  /**
   * Limite massimo dei valori segreti da cercare
   */
  private static final int LIMIT = 65536;

  private final long p;
  private final long g;

  public DiffieHellman(long p, long g) {
    this.p = p;
    this.g = g;
  }
  
  public class CrackThread implements Supplier<Callable<HashMap<String,ArrayList<Long>>>> {

	  public long min;
	  public long max;
	  public long publicA;
	  public long publicB;
	  
	  public CrackThread(long min ,long max ,long publicA ,long publicB) {
		  this.max=max;
		  this.min=min;
		  this.publicA=publicA;
		  this.publicB=publicB;
	  }
	  
	  @Override
	  public Callable<HashMap<String,ArrayList<Long>>> get() {

		    return () -> {
		    	//utilizzo una Hashmap per poter ritornare due liste di valori, tutti i valori possibili di a e quelli di b
		    	HashMap<String,ArrayList<Long>> m = new HashMap<String,ArrayList<Long>>();
			    ArrayList<Long> secretA = new ArrayList<Long>();
			    ArrayList<Long> secretB = new ArrayList<Long>();
			    
			    for(long i=min;i<max;i++) {
			    	if(publicA==DiffieHellmanUtils.modPow(g,i,p))
			    		secretA.add(i);
			    	if(publicB==DiffieHellmanUtils.modPow(g,i,p))
			    		secretB.add(i);
			    }
			    m.put("a",secretA);
			    m.put("b",secretB);
		    	return m;
		    };
	    };
  }

  /**
   * Metodo da completare
   * 
   * @param publicA valore di A
   * @param publicB valore di B
   * @return tutte le coppie di possibili segreti a,b
   * @throws InterruptedException 
   * @throws ExecutionException 
   */
  public List<Integer> crack(long publicA, long publicB) throws InterruptedException, ExecutionException {
	//utilizzo 20 thread, da cambiare in base al numero di core disponibili
	int size=20;
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(size);
    ArrayList<Callable<HashMap<String,ArrayList<Long>>>> callables = new ArrayList<Callable<HashMap<String,ArrayList<Long>>>>();
    
    //ogni thread cerca tutti i possibili valori di a e b entro un certo range
    for (int i = 0; i < size; i++) {
      callables.add(new CrackThread(LIMIT*i/size,LIMIT*(i+1)/size, publicA, publicB).get());
    }

    //avvio i thread
    List<Future<HashMap<String,ArrayList<Long>>>> futures = executor.invokeAll(callables);

    //unisco i valori forniti dai vari thread in due liste, una per i valori di a e una per quelli di b
    ArrayList<Long> secretA = new ArrayList<Long>();
    ArrayList<Long> secretB = new ArrayList<Long>();
    for (Future<HashMap<String,ArrayList<Long>>> f: futures) {
    	secretA.addAll(f.get().get("a"));
    	secretB.addAll(f.get().get("b"));
    }
    
    ArrayList<Integer> res = new ArrayList<Integer>();
    
    //controllo quali coppie di valori a e b sono soluzioni del problema
    for(long i : secretA)
    	for(long j : secretB)
    		if(DiffieHellmanUtils.modPow(publicB,i,p)==DiffieHellmanUtils.modPow(publicA,j,p)) {
    			res.add((int)i);
    			res.add((int)j);
    		}
    
    executor.shutdown();
    
    return res;
  }
}

