package services;

public class Peticion {
	private long Ti;
	private long Pi;
	
	public Peticion() {
		Ti = 0;
		Pi = 0;
	}
	
	public Peticion(long ti, long pi) {
		this.Ti = ti;
		this.Pi = pi;
	}

	public long getTi() {
		return Ti;
	}

	public void setTi(long ti) {
		Ti = ti;
	}

	public long getPi() {
		return Pi;
	}

	public void setPi(long pi) {
		Pi = pi;
	}	
}
