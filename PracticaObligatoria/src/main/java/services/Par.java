package services;

public class Par {
	private long o;
	private long d;
	
	public Par() {
		o = 0;
		d = 0;
	}
	public long getO() {
		return o;
	}
	public void setO(long o) {
		this.o = o;
	}
	public long getD() {
		return d;
	}
	public void setD(long d) {
		this.d = d;
	}	
	public long getExtremoInferior() {
		if (d >= 0) {
			return o-d;
		}
		else {
			return o+d;
		}
	}
	public long getExtremoSuperior() {
		if (d >= 0) {
			return o+d;
		}
		else {
			return o-d;
		}
	}
}