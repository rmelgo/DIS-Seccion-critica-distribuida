package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Prueba {

	public int numero_procesos = 4;
	public String ruta = "C:\\Users\\Raul_\\Desktop\\log"; //cambiar segun el equipo
	
	public Prueba() {
		//Fusionamos el contenido de todos los logs en uno unico
		ArrayList<String> contenido_logs = new ArrayList<String>();
		
		String desktopPath = System.getProperty("user.home") + File.separator + "log";
		System.out.println(desktopPath);
	
        try {
            //Crear un objeto File se encarga de crear o abrir acceso a un archivo que se especifica en su constructor
            File archivo = new File(desktopPath + ".txt"); //adaptar ruta
            
            //limpiarLog(ruta + ".txt");
        	
            File logs[] = new File[numero_procesos];
            BufferedReader[] br = new BufferedReader[numero_procesos];
            String[] texto = new String[numero_procesos];
            
        	for (int i = 0; i < numero_procesos; i++) {	
        		logs[i] = new File(ruta + i + ".txt"); //adaptar ruta
        		
        		br[i] = new BufferedReader(new FileReader(logs[i]));
        		
        		texto[i] = br[i].readLine();
        		while(texto[i] != null) {
        			if (texto[i].startsWith("P")) {
        				if (texto[i].startsWith("P3") || texto[i].startsWith("P4")) {
        					String[] tokens_p1 = texto[i].split(" ");
        					long messi = Long.parseLong(tokens_p1[2]);
        					messi = messi - 0;
        					tokens_p1[2] = messi + "";
        					
        					texto[i] = tokens_p1[0] + " " +  tokens_p1[1] + " " + messi;
        					
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
        	
            //Crear objeto FileWriter que sera el que nos ayude a escribir sobre archivo
            FileWriter escribir = new FileWriter(archivo, true);
        
            //Escribimos en el archivo con el metodo write 
            for (int j = 0; j < contenido_logs.size(); j++) {
            	if (j > 0) {
            		if (contenido_logs.get(j).contains("E") && contenido_logs.get(j - 1).contains("E")) {
            			System.out.println("Fallo en la seccion critica. Fallo en la linea " + j);
            			
            		}
            		if (contenido_logs.get(j).contains("S") && contenido_logs.get(j - 1).contains("S")) {
            			System.out.println("Fallo en la seccion critica. Fallo en la linea " + j);
            		}
            	}
            	 escribir.write(contenido_logs.get(j));
            	 escribir.write("\r\n");
            }
           
            //Cerramos la conexion
            escribir.close();
        } //Si existe un problema al escribir cae aqui
        catch (Exception e) {
            System.out.println("Error al escribir en el log");
            System.out.println("Motivo " + e);
        }
	}
	
	public static void main(String args[]) {
		new Prueba();
	}
}
