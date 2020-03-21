
public class Timer {
	public long startTime;
	public long endTime;
	public double totalTime;
	public String name;
	
	public Timer(String processName) {
		this.name = processName;
	}
	
	public void start() {
		this.startTime = System.nanoTime();
		System.out.println("["+name+"] Timer start.");
	}
	
	public void stop() {
		this.endTime = System.nanoTime();
		
	}
	
	public void second() {
		this.totalTime = (double)(endTime - startTime)/1_000_000_000.0;
		System.out.println("["+name+"] Timer stop. Used "+totalTime+" s");
	}
	
	public void milisecond() {
		this.totalTime = (double)(endTime-startTime)/1_000_000.0;
		System.out.println("["+name+"] Timer stop. Used "+totalTime+" ms");
	}
}
