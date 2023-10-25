
package services;

import javax.ws.rs.Path;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

@Singleton
@Path("hola") // ruta a la clase
public class Servidor extends Thread{
	public int min = 300;
	public int max = 500;
	public String estado;
	public long Ci; 
	public long Ti;
	public long Pi;
	public List<Peticion> cola = new ArrayList<Peticion>();
	
	public int numero_procesos = 6;
	public int numero_iteraciones = 100;
	
	public Client[] client = new Client[numero_procesos];
	public URI[] uri = new URI[numero_procesos];
	public WebTarget[] target = new WebTarget[numero_procesos];
	
	public Semaphore semaforo_proteccion = new Semaphore(1);

	public Semaphore semaforo_respuestas = new Semaphore(0);
	public Semaphore semaforo_inicializacion = new Semaphore(0);
	
	private int num_preparados = 0;
	public Semaphore semaforo_preparados = new Semaphore(0);
	
	private int num_listos = 0;
	public Semaphore semaforo_listos = new Semaphore(0);
	
	private int num_fin = 0;
	public Semaphore semaforo_fin = new Semaphore(0);
	
	public Semaphore semaforo_antibloqueos = new Semaphore(1);
	
	public String ruta = System.getProperty("user.home") + File.separator + "log";
	
	private long t1 = 0;
	private long t2 = 0;
	public int minNTP = 956;
	public int maxNTP = 1176;
	
	private int num_repeticiones = 10;
	private long d = 0;
	private long o = 0;
	private long t0 = 0;
	private long t3 = 0;
	private String[] temp = new String[2];
	private Par[] mejorParInicial = new Par[numero_procesos / 2];
	private Par[] mejorParFinal = new Par[numero_procesos / 2];
	private Par[] mejorPar = new Par[numero_procesos / 2];
	private ArrayList<Par>[] pares = new ArrayList[numero_procesos / 2];
	
	private String contenido_logs_procesos[] = new String[numero_procesos];
	
	private String IP1;
	private String IP2;
	private String IP3;
	
	public Servidor() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Servidor(int Pi) {
		this.Pi=Pi;
	}
	
	public Servidor(int Pi, String IP1, String IP2, String IP3) {
		this.Pi=Pi;
		this.IP1=IP1;
		this.IP2=IP2;
		this.IP3=IP3;
	}
		
	public void run() {
		System.out.println("Soy el proceso con id: " + Pi);
				
		//Inicializamos las URIs de los 6 hilos
		for (int i = 0; i < numero_procesos; i++) {
			if (i < 2) {
				client[i] = ClientBuilder.newClient();
				uri[i] = UriBuilder.fromUri("http://" + IP1 + ":808" + i + "/Obligatoria").build();
				target[i] = client[i].target(uri[i]);	
			}	
			else if (i >= 2 && i < 4) {
				client[i] = ClientBuilder.newClient();
				uri[i] = UriBuilder.fromUri("http://" + IP2 + ":808" + i + "/Obligatoria").build();
				target[i] = client[i].target(uri[i]);	
			}	
			else {
				client[i] = ClientBuilder.newClient();
				uri[i] = UriBuilder.fromUri("http://" + IP3 + ":808" + i + "/Obligatoria").build();
				target[i] = client[i].target(uri[i]);	
			}
		}
		
		//limpiamos el log asociado a cada proceso en caso de que este se encuentre creado y tenga informacion
		target[(int) Pi].path("rest").path("hola").path("limpiarLog").queryParam("P", ""+Pi).request(MediaType.TEXT_PLAIN).get(String.class);
		
		//Inicializamos las URIs de los 6 servidores
		target[0].path("rest").path("hola").path("inicializarURIS").queryParam("IP1", ""+IP1).queryParam("IP2", ""+IP2).queryParam("IP3", ""+IP3).request(MediaType.TEXT_PLAIN).get(String.class);
				
		//Esperamos a que todos los procesos esten creados e inicializados
		target[0].path("rest").path("hola").path("preparado").request(MediaType.TEXT_PLAIN).get(String.class);
		
		if (Pi == 0) { //el proceso supervisor se encarga de aplicar el algortimo NTP con los demas procesos
			for (int m = 1; m < numero_procesos / 2; m++) {
				System.out.println("==================MAQUINA  " + m + "===================");
			
				mejorParInicial[m-1] = new Par();
			
				//inicializamos el par a valores infinitos antes de empezar el bucle
				mejorParInicial[m-1].setO(999999999);
				mejorParInicial[m-1].setD(999999999);
		
				//empezamos el bucle
				pares[m-1] = new ArrayList<Par>();
		
				for (int i = 0; i < num_repeticiones; i++) {
					t0 = System.currentTimeMillis();
					
					//llamada a NTP/pedirTiempo							
					String t1t2 = target[m*2].path("rest").path("hola").path("pedirTiempo").request(MediaType.TEXT_PLAIN).get(String.class);	
					temp = t1t2.split("=");
					t1 = Long.parseLong(temp[0]);
					t2 = Long.parseLong(temp[1]);
			
				
					t3 = System.currentTimeMillis();
					
					o = determinarOffset(t0,t1,t2,t3);
					d = determinarDelay(t0,t1,t2,t3);
					
					Par p = new Par();
					p.setO(o);
					p.setD(d);
				
					pares[m-1].add(p);
					
					System.out.println("Par de la maquina " + m + " es de (" + o + ", " + d + ") [formato (offset, delay)]");	
			
					if (Math.abs(d) < Math.abs(mejorParInicial[m-1].getD())) {
						mejorParInicial[m-1].setO(o);
						mejorParInicial[m-1].setD(d);
					}
				}
			
				System.out.println("El valor del mejor par de la maquina " + m + " es de (" + mejorParInicial[m-1].getO() + ", " + mejorParInicial[m-1].getD() + ") [formato (offset, delay)]");	
			}
		}
		
		//Esperamos a que el proceso supervisor termine de realizar NTP para poder implementar el algortimo de Ricart y Agrawala
		target[0].path("rest").path("hola").path("listo").request(MediaType.TEXT_PLAIN).get(String.class);
		
			
		//invocamos a un servicio REST donde se implemente el algoritmo de Ricart y Agrawala
		target[(int) Pi].path("rest").path("hola").path("algorimoRicartAgrawala").queryParam("P", ""+Pi).queryParam("IP1", ""+IP1).queryParam("IP2", ""+IP2).queryParam("IP3", ""+IP3).request(MediaType.TEXT_PLAIN).get(String.class);	
		
		
		System.out.println("El proceso con id " + Pi + " ha acabado");
		
		if (Pi == 0) { //el proceso supervisor se encarga de aplicar el algortimo NTP con los demas procesos
			for (int m = 1; m < numero_procesos / 2; m++) {
				System.out.println("==================MAQUINA  " + m + "===================");
			
				mejorParFinal[m-1] = new Par();
			
				//inicializamos el par a valores infinitos antes de empezar el bucle
				mejorParFinal[m-1].setO(999999999);
				mejorParFinal[m-1].setD(999999999);
		
				//empezamos el bucle
		
				for (int i = 0; i < num_repeticiones; i++) {
					t0 = System.currentTimeMillis();
					
					//llamada a NTP/pedirTiempo							
					String t1t2 = target[m * 2].path("rest").path("hola").path("pedirTiempo").request(MediaType.TEXT_PLAIN).get(String.class);	
					temp = t1t2.split("=");
					t1 = Long.parseLong(temp[0]);
					t2 = Long.parseLong(temp[1]);
			
				
					t3 = System.currentTimeMillis();
					
					o = determinarOffset(t0,t1,t2,t3);
					d = determinarDelay(t0,t1,t2,t3);
					
					Par p = new Par();
					p.setO(o);
					p.setD(d);
				
					pares[m-1].add(p);
					
					System.out.println("Par de la maquina " + m + " es de (" + o + ", " + d + ") [formato (offset, delay)]");
					
					if (Math.abs(d) < Math.abs(mejorParFinal[m-1].getD())) {
						mejorParFinal[m-1].setO(o);
						mejorParFinal[m-1].setD(d);
					}
				}
			
				System.out.println("El valor del mejor par de la maquina " + m + " es de (" + mejorParFinal[m-1].getO() + ", " + mejorParFinal[m-1].getD() + ") [formato (offset, delay)]");	
			}
			
			//calculamos la media del mejor par inicial y el mejor par final para cada proceso
			for (int m = 0; m < (numero_procesos / 2) - 1; m++) {
				mejorPar[m] = new Par();
				
				mejorPar[m].setO((mejorParInicial[m].getO() + mejorParFinal[m].getO()) / 2);
				mejorPar[m].setD((mejorParInicial[m].getD() + mejorParFinal[m].getD()) / 2);
				
				mejorPar[m] = marzullo(pares[m]); //realmente se va a aplicar Marzullo para encontrar el mejor par pero se deja implementada la otra solucion. Para utilizar la otra solucion simplemente hay que comentar esta linea
				
				System.out.println("El valor del mejor par medio de la maquina " + m + " es de (" + mejorPar[m].getO() + ", " + mejorPar[m].getD() + ") [formato (offset, delay)]");	
			}				
		}
		
		//Los demas procesos envian el log al proceso supervisor para que este se encargue de fusionarlos
		contenido_logs_procesos[(int) Pi] = target[(int) Pi].path("rest").path("hola").path("leerFichero").queryParam("P", ""+Pi).request(MediaType.TEXT_PLAIN).get(String.class);
		
		target[0].path("rest").path("hola").path("recibirLog").queryParam("P", ""+Pi).queryParam("log", ""+contenido_logs_procesos[(int) Pi]).request(MediaType.TEXT_PLAIN).get(String.class);
		
		//El proceso supervisor espera a que todos los procesos hayan acabado para poder comprobar y fusionar los log
		target[0].path("rest").path("hola").path("fin").request(MediaType.TEXT_PLAIN).get(String.class);
		
		if (Pi == 0) { //el proceso con identificador 0 se encargara de recoger y fusionar los logs
			
			//Fusionamos el contenido de todos los logs en uno unico
			ArrayList<String> contenido_logs = new ArrayList<String>();
		
	        try {
	            //Crear un objeto File se encarga de crear o abrir acceso a un archivo que se especifica en su constructor
	            File archivo = new File(ruta + ".txt");
	            
	            target[(int) Pi].path("rest").path("hola").path("limpiarLog").queryParam("P", ""+1111).request(MediaType.TEXT_PLAIN).get(String.class);
	        	
	            File logs[] = new File[numero_procesos];
	            BufferedReader[] br = new BufferedReader[numero_procesos];
	            String[] texto = new String[numero_procesos];
	            
	        	for (int i = 0; i < numero_procesos; i++) {	
	        		logs[i] = new File(ruta + i + ".txt"); 
	        		
	        		br[i] = new BufferedReader(new FileReader(logs[i]));
	        		
	        		texto[i] = br[i].readLine();
	        		while(texto[i] != null) {
	        				if (texto[i].startsWith("P")) {
	        					if (texto[i].startsWith("P3") || texto[i].startsWith("P4")) {
	            					String[] tokens_p1 = texto[i].split(" ");
	            					long tiempo = Long.parseLong(tokens_p1[2]);
	            					tiempo = tiempo - mejorPar[0].getO();
	            					tokens_p1[2] = tiempo + "";
	            					
	            					texto[i] = tokens_p1[0] + " " +  tokens_p1[1] + " " + tiempo;
	            					
	            					contenido_logs.add(texto[i]);
	            				}
	        					else if (texto[i].startsWith("P5") || texto[i].startsWith("P6")) {
	        						String[] tokens_p1 = texto[i].split(" ");
	            					long tiempo = Long.parseLong(tokens_p1[2]);
	            					tiempo = tiempo - mejorPar[1].getO();
	            					tokens_p1[2] = tiempo + "";
	            					
	            					texto[i] = tokens_p1[0] + " " +  tokens_p1[1] + " " + tiempo;
	            					
	            					contenido_logs.add(texto[i]);
	        					}
	            				else {
	            					contenido_logs.add(texto[i]);
	            				} 
	        				}
	        				texto[i] = br[i].readLine();
	        		}
	        	}
	        	   	
	        	Collections.sort(contenido_logs, new Comparator<String>() {
	    			@SuppressWarnings("removal")
	    			@Override
	    			public int compare(String p1, String p2) {
	    				String[] tokens_p1 = p1.split(" ");
	    				String[] tokens_p2 = p2.split(" ");
	    				return new Long(tokens_p1[2]).compareTo(new Long(tokens_p2[2]));
	    			}
	    		});
	        	
	            FileWriter escribir = new FileWriter(archivo, true);
	        
	            for (int j = 0; j < contenido_logs.size(); j++) {
	            	 escribir.write(contenido_logs.get(j));
	            	 escribir.write("\r\n");
	            }
	           
	            escribir.close();
	        } 
	        catch (Exception e) {
	            System.out.println("Error al escribir en el log");
	        }
	        
	        String[] args_comprobador = new String[3];
	        args_comprobador[0] = ruta + ".txt";
	        args_comprobador[1] = mejorPar[0].getD() + "";
	        args_comprobador[2] = mejorPar[1].getD() + "";
	        Comprobador.main(args_comprobador);	        
		}
	}
	
	//METODOS AUXILIARES
	
	private long determinarOffset(long t0, long t1, long t2, long t3) {
		long offset;
		
		offset = ((t1 - t0) + (t2 - t3)) / 2 + (((t1 - t0) - (t2 - t3)) / 2);
				
		return offset;
	}

	private long determinarDelay(long t0, long t1, long t2, long t3) {
		long delay;
		
		delay = (t1 - t0) + (t3 - t2);
		
		return delay;
	}	
		
	private void escribirEntradaSCLog(long p) { //referencia https://es.stackoverflow.com/questions/138958/c%C3%B3mo-escribir-en-un-archivo-de-texto-en-java
        String mensaje = "P" + (Pi + 1) + " E " + System.currentTimeMillis();

        try {
            File archivo = new File(ruta + p + ".txt"); 

            FileWriter escribir = new FileWriter(archivo, true);

            escribir.write(mensaje);
            escribir.write("\r\n");

            escribir.close();
        } 
        catch (Exception e) {
            System.out.println("Error al escribir en el log");
        }
	}
	
	private void escribirSalidaSCLog(long p) { //referencia https://es.stackoverflow.com/questions/138958/c%C3%B3mo-escribir-en-un-archivo-de-texto-en-java
        String mensaje = "P" + (Pi + 1) + " S " + System.currentTimeMillis();

        try {
            File archivo = new File(ruta + p + ".txt"); 

            FileWriter escribir = new FileWriter(archivo, true);

            escribir.write(mensaje);
            escribir.write("\r\n");

            escribir.close();
        } 
        catch (Exception e) {
            System.out.println("Error al escribir en el log");
        }
	}
	
	private void escribirFichero(String contenido, String ruta, long p) {
		FileWriter fichero = null;
        PrintWriter pw = null;
        
        try
        {
            fichero = new FileWriter(ruta + p + ".txt");
            pw = new PrintWriter(fichero);
     
            pw.println(contenido);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
	}
	
	//PETICIONES REST
	
	@GET 
	@Produces(MediaType.TEXT_PLAIN) 
	@Path("inicializarURIS") 
	public void inicializarURIS(@QueryParam("IP1") String IP1, @QueryParam("IP2") String IP2, @QueryParam("IP3") String IP3) 
	{
		//generamos las URI para todos los procesos involucrados
		for (int i = 0; i < numero_procesos; i++) {
			if (i < 2) {
				client[i] = ClientBuilder.newClient();
				uri[i] = UriBuilder.fromUri("http://" + IP1 + ":808" + i + "/Obligatoria").build();
				target[i] = client[i].target(uri[i]);	
			}	
			else if (i >= 2 && i < 4) {
				client[i] = ClientBuilder.newClient();
				uri[i] = UriBuilder.fromUri("http://" + IP2 + ":808" + i + "/Obligatoria").build();
				target[i] = client[i].target(uri[i]);	
			}	
			else {
				client[i] = ClientBuilder.newClient();
				uri[i] = UriBuilder.fromUri("http://" + IP3 + ":808" + i + "/Obligatoria").build();
				target[i] = client[i].target(uri[i]);	
			}
		}
	}
	
	@GET // tipo de petición HTTP
	@Produces(MediaType.TEXT_PLAIN) // tipo de texto devuelto
	@Path("limpiarLog") // ruta al método
	public void limpiarLog(@QueryParam("P") long P) 
	{
		String ruta_log;
		if (P == 1111) {
			ruta_log = System.getProperty("user.home") + File.separator + "log.txt";
		}
		else {
			ruta_log = System.getProperty("user.home") + File.separator + "log" + P + ".txt";
		}
		
		try {
            File archivo = new File(ruta_log);

            BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));

            bw.write("");

            bw.close();
        } 
        catch (Exception e) {
            System.out.println("Error al vaciar el log");
        }
	}	
	
	@GET // tipo de petición HTTP
	@Produces(MediaType.TEXT_PLAIN) 
	@Path("preparado") 
	public void preparado()
	{	
		try {
			semaforo_proteccion.acquire(1); 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		num_preparados = num_preparados + 1;
		if (num_preparados < numero_procesos) {
			try {
				semaforo_proteccion.release(1); 
				semaforo_preparados.acquire(1); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			num_preparados = 0;
			semaforo_proteccion.release(1); 
			semaforo_preparados.release(numero_procesos - 1);
		}
	}
	
	@GET 
	@Produces(MediaType.TEXT_PLAIN) 
	@Path("pedirTiempo") 
	public String pedirTiempo() 
	{
		t1 = System.currentTimeMillis();
		
		double x = Math.random()*(maxNTP - minNTP)+minNTP;
		try {
			Thread.sleep((long) x);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		
		t2 = System.currentTimeMillis();
		
		return t1 + "=" + t2;
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN) 
	@Path("listo") 
	public void listo() 
	{	
		try {
			semaforo_proteccion.acquire(1); 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		num_listos = num_listos + 1;
		if (num_listos < numero_procesos) {
			try {
				semaforo_proteccion.release(1); 
				semaforo_listos.acquire(1); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			num_listos = 0;
			semaforo_proteccion.release(1); 
			semaforo_listos.release(numero_procesos - 1);
		}
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN) 
	@Path("algorimoRicartAgrawala") 
	public void algorimoRicartAgrawala(@QueryParam("P") long P, @QueryParam("IP1") String IP1, @QueryParam("IP2") String IP2, @QueryParam("IP3") String IP3)
	{	
		Pi = P;
		System.out.println("Arranca el proceso " + Pi);
			
		for (int i = 0; i < numero_procesos; i++) {
			if (i < 2) {
				client[i] = ClientBuilder.newClient();
				uri[i] = UriBuilder.fromUri("http://" + IP1 + ":808" + i + "/Obligatoria").build();
				target[i] = client[i].target(uri[i]);	
			}	
			else if (i >= 2 && i < 4) {
				client[i] = ClientBuilder.newClient();
				uri[i] = UriBuilder.fromUri("http://" + IP2 + ":808" + i + "/Obligatoria").build();
				target[i] = client[i].target(uri[i]);	
			}	
			else {
				client[i] = ClientBuilder.newClient();
				uri[i] = UriBuilder.fromUri("http://" + IP3 + ":808" + i + "/Obligatoria").build();
				target[i] = client[i].target(uri[i]);	
			}
		}
		 
        //Inicializar seccion critica
        estado = "LIBERADA";
		Ci = 0;
		cola.clear();
		
		for (int n = 0; n < numero_iteraciones; n++) {
			
			//Simulamos la realizacion de un calculo que lleva entre 0.3 y 0.5 segundos en realizarse
			double x = Math.random()*(max - min)+min;
			try {
				Thread.sleep((long) x);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//Entrada en la seccion critica (SC)
					
			estado = "BUSCADA";
			Ti = Ci;
		
			//enviar una peticion de entrada a la SC a los demas procesos involucrados
			
			for (int i = 0; i < numero_procesos; i++) {
				if (i != Pi) {													
					target[i].path("rest").path("hola").path("enviarPeticion").queryParam("Tj", ""+Ti).queryParam("Pj", ""+Pi).request(MediaType.TEXT_PLAIN).async().get(String.class);
				}
			}
				
			//Esperar hasta que respondan N - 1 procesos (semoforo)
			
			target[(int) Pi].path("rest").path("hola").path("esperarRespuestas").request(MediaType.TEXT_PLAIN).get(String.class);
			
			estado = "TOMADA";
			Ci = Ci + 1;
				
			//Escribir en el log correspodiente
			
			escribirEntradaSCLog(Pi);
			
			System.out.println("P" + Pi + " E " + System.currentTimeMillis());
		
			//Simulamos la realizacion de un calculo que lleva entre 0.1 y 0.3 segundos en realizarse
			min = 100;
			max = 300;
			x = Math.random()*(max - min)+min;
			try {
				Thread.sleep((long) x);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//Escribir en el log correspodiente
			escribirSalidaSCLog(Pi);
			
			System.out.println("P" + Pi + " S " + System.currentTimeMillis());
			
		
			//Salimos de la seccion critica			
			estado = "LIBERADA";
			
			try {
				semaforo_antibloqueos.acquire(1); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (int j = 0; j < cola.size(); j++) {
				//responder peticion
				target[(int) cola.get(j).getPi()].path("rest").path("hola").path("enviarRespuesta").request(MediaType.TEXT_PLAIN).get(String.class);
			}
			cola.clear();
						
			semaforo_antibloqueos.release(1);
		}            		
	} 
	
	@GET 
	@Produces(MediaType.TEXT_PLAIN)
	@Path("enviarPeticion")
	public void enviarPeticion(@QueryParam("Tj") long Tj, @QueryParam("Pj") long Pj)
	{	
		try {
			semaforo_antibloqueos.acquire(1); 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Ci >= Tj) {
			Ci = Ci + 1;
		}
		else {
			Ci = Tj + 1;
		}
		if (estado == "TOMADA" || (estado == "BUSCADA" && (Ti < Tj || (Ti == Tj && Pi < Pj)))) {
			Peticion p = new Peticion(Tj, Pj);
			cola.add(p);
		}
		else {
			//Responde inmediatamente a Pj
			target[(int) Pj].path("rest").path("hola").path("enviarRespuesta").request(MediaType.TEXT_PLAIN).async().get(String.class);
		}		
		semaforo_antibloqueos.release(1); 
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN) 
	@Path("esperarRespuestas") 
	public void esperarRespuestas()
	{		
		try {
			semaforo_respuestas.acquire(numero_procesos - 1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN) 
	@Path("enviarRespuesta") 
	public void enviarRespuesta()
	{
		semaforo_respuestas.release(1);
	}
	
	private Par marzullo(ArrayList<Par> pares) {	
		//ordenamos la lista de pares ascendentemente en funcion del offset
		Collections.sort(pares, new Comparator<Par>() {
			@SuppressWarnings("removal")
			@Override
			public int compare(Par p1, Par p2) {
				return new Long(p1.getO()).compareTo(new Long(p2.getO()));
			}
		});
			
		long mejor, contador;
		long izquierda = 0, derecha = 0;
		long limite_minimo = pares.get(0).getExtremoInferior(), limite_maximo = pares.get(0).getExtremoSuperior();
		
		mejor = 0;
		contador = 0;
		
		for (int n = 0; n < pares.size(); n++) {
			if (pares.get(n).getExtremoInferior() < limite_minimo) {
				limite_minimo = pares.get(n).getExtremoInferior();
			}
			if (pares.get(n).getExtremoSuperior() > limite_maximo) {
				limite_maximo = pares.get(n).getExtremoSuperior();
			}
		}
		
		for (long i = limite_minimo; i < limite_maximo; i++) { //recorremos intervalo de tiempo
			for (int j = 0; j < pares.size(); j++) {
				if (pares.get(j).getExtremoInferior() == i) { //Si se abre un nuevo intervalo, incrementamos el contador de solapamientos en una unidad
					contador = contador + 1;
				}
				if (pares.get(j).getExtremoSuperior() == i) { //Si se cierra un nuevo intervalo, decrementamos el contador de solapamientos en una unidad
					if (contador == mejor) { //si estamos en el mejor numero de solapamientos y vamos a decrementar contador en una unidad, actualizamos el valor del limite derecho ya que finaliza la region con el mejor numero de solapamientos
						derecha = i;
					}
					contador = contador - 1;
				}
			
				if (contador > mejor) { 
					izquierda = i; //registramos el limite inferior de la region con mejor numero de solapamientos
					mejor = contador; //actualizamos el mejor numero de solapamientos
				}
			}
		}
			
		Par p = new Par();
		p.setO((izquierda + derecha) / 2);
		p.setD(derecha - p.getO());
		
		return p;
	}
	
	@GET 
	@Produces(MediaType.TEXT_PLAIN) 
	@Path("leerFichero") 
	public String leerFichero(@QueryParam("P") long P) {
		String contenido_fichero_completo = new String();
		String ruta = System.getProperty("user.home") + File.separator + "log";
		
		File archivo = null;
	    FileReader fr = null;
	    BufferedReader br = null;
		
	    try {
	       archivo = new File(ruta + P + ".txt");
	       fr = new FileReader(archivo);
	       br = new BufferedReader(fr);

	       String linea;
	       while((linea=br.readLine())!=null) {
	    	   contenido_fichero_completo = contenido_fichero_completo + linea + "\r\n";
	       }
	    }
	    catch(Exception e){
	       e.printStackTrace();	
	    } finally {
	       try{                    
	          if (null != fr) {   
	             fr.close();     
	          }                  
	       } catch (Exception e2) { 
	          e2.printStackTrace();
	       }
	    }
	    
	    return contenido_fichero_completo;
	}
	
	@GET 
	@Produces(MediaType.TEXT_PLAIN) 
	@Path("recibirLog") 
	public void recibirLog(@QueryParam("P") long P, @QueryParam("log") String log) 
	{
		contenido_logs_procesos[(int) P] = log;
				
		target[(int) Pi].path("rest").path("hola").path("limpiarLog").queryParam("P", ""+P).request(MediaType.TEXT_PLAIN).get(String.class);
		
		escribirFichero(contenido_logs_procesos[(int) P], ruta, P);
	}	
	
	@GET 
	@Produces(MediaType.TEXT_PLAIN) 
	@Path("fin") 
	public void fin()
	{	
		try {
			semaforo_proteccion.acquire(1); 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		num_fin = num_fin + 1;
		if (num_fin < numero_procesos) {
			try {
				semaforo_proteccion.release(1); 
				semaforo_fin.acquire(1); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			num_fin = 0;
			semaforo_proteccion.release(1); 
			semaforo_fin.release(numero_procesos - 1);
		}
	}
}