package saadadb.command;

import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * @author michel
 *
 */
public class SaadaProcess {
	/*
	 * if end_value is >= 0 at the end of the creator, the PorgressDialog switch in monitor mode
	 * This variable is static because the part of the commands using it are static methods. 
	 */
	private static int end_value=-1;

	public SaadaProcess() {
		SaadaProcess.end_value = 0;
	}

	public SaadaProcess(int end_value) {
		SaadaProcess.end_value = end_value;
	}
	/**
	 * dummy task just for debugging purpose
	 * @throws AbortException 
	 */
	public void faitTonBoulot() throws AbortException  {
		Messenger.setMaxProgress(10);
		for( int i=0 ; i<10 ; i++ ) {
			Messenger.printMsg(Messenger.TRACE, " blabla " + i + " " + Thread.currentThread() + " " + Thread.currentThread().getState());
			if( (i%2) == 0 ) Messenger.diskAccess();
			else if( (i%3) == 0 ) Messenger.dbAccess();
			else if( (i%5) == 0 ) Messenger.procAccess();
			//			if( i == 3) {int[] x = new int[0]; x[12] = 6;}
			//try {
			this.processUserRequest();
			Messenger.setProgress(i);
			//			} catch (AbortException e1) {
			//				return ;
			//			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Messenger.printStackTrace(e);
			}
		}
		Messenger.noMoreAccess();
	}

	/**
	 * Ckeck for a pause or for an obort request
	 * @throws AbortException
	 */
	public void processUserRequest()  throws AbortException {
		this.waitIfPauseRequested() ;
		this.abortOnRequested();
	}
	/**
	 * Send an AbortException if an abort request has been detected by the Messenger
	 * @throws AbortException
	 */
	public void abortOnRequested() throws AbortException {
		synchronized (this) {
			if( Messenger.abortRequested() ) {	
				System.out.println("notify");
				this.notifyAll();	
				Messenger.resetUserRequests();
				AbortException.throwNewException(SaadaException.USER_ABORT, "User Request");
			}
			Messenger.resetUserRequests();
		}
	}

	/**
	 * Suspend the current thread if a pause request has been detected by the Messenger
	 * @throws AbortException 
	 */
	public synchronized void waitIfPauseRequested() throws AbortException {
		if( Messenger.pauseRequested() ) {		
			try {
				Messenger.printMsg(Messenger.TRACE, "Pended on user request");
				synchronized (this) {
					this.wait();	
					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@");
					SaadaProcess.this.abortOnRequested()	;
				}
			} catch (Exception e) {
				AbortException.throwNewException(SaadaException.USER_ABORT, e);
			}
		}
	}

	public void wakeUp() {
		synchronized (this) {
			Messenger.printMsg(Messenger.TRACE, "Send notification to all threads");
			this.notifyAll();	
		}

	}
	/**
	 * @return
	 */
	public int getEndValue() {
		return end_value;
	}

	/**
	 * @param end_value
	 */
	public void setEndValue(int end_value) {
		if( end_value < 0 ) {
			SaadaProcess.end_value = 0;
		}
		else {
			SaadaProcess.end_value  = end_value;
		}
	}

}
