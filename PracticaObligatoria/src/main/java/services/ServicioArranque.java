package services;

public class ServicioArranque {
	
	private String IP1;
	private String IP2;
	private String IP3;
	
	public ServicioArranque(String args[]) {				     
        IP1 = args[0];
        IP2 = args[1];
        IP3 = args[2];
		
		Servidor p1 = new Servidor(0, IP1, IP2, IP3);
		Servidor p2 = new Servidor(1, IP1, IP2, IP3);
		Servidor p3 = new Servidor(2, IP1, IP2, IP3);
		Servidor p4 = new Servidor(3, IP1, IP2, IP3);
		Servidor p5 = new Servidor(4, IP1, IP2, IP3);
		Servidor p6 = new Servidor(5, IP1, IP2, IP3);
		p1.start();
		p2.start();	
		p3.start();
		p4.start();	
		p5.start();
		p6.start();	
	}
	
	public static void main(String args[]) {
		new ServicioArranque(args);
	}
}