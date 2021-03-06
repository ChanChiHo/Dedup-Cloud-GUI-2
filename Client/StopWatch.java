
public class StopWatch {
	
	public static final int NANOSECOND = 1;
	public static final int MILISECOND = 2;
	public static final int SECOND = 3;
	
	public long startTime;
	public long endTime;
	public double totalTime;
	public String name;
	
	private int unit = this.NANOSECOND;
	
	public StopWatch(String processName) {
		this.name = processName;
		
		this.start();
	}
	
	public StopWatch(String processName, int unit) {
		this.name = processName;
		this.unit = unit;
		
		this.start();
	}
	
	public void start() {
		this.startTime = System.nanoTime();
		System.out.println("["+name+"] Timer start.");
	}
	
	public void stop() {
		this.endTime = System.nanoTime();
		switch (unit) {
			case NANOSECOND: 
				nanosecond();
				break;
			case MILISECOND:
				milisecond();
				break;
			case SECOND:
				second();
				break;
			default:
				milisecond();
		}
		
	}
	
	private void second() {
		this.totalTime = (double)(endTime - startTime)/1_000_000_000.0;
		System.out.println("["+name+"] Timer stop. Used "+totalTime+" s");
	}
	
	private void milisecond() {
		this.totalTime = (double)(endTime-startTime)/1_000_000.0;
		System.out.println("["+name+"] Timer stop. Used "+totalTime+" ms");
	}
	
	private void nanosecond() {
		this.totalTime = (double) (endTime-startTime);
		System.out.println("["+name+"] Timer stop. Used "+totalTime+" ns");
	}
}
