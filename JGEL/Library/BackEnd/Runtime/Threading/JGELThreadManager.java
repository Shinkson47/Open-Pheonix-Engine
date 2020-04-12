package BackEnd.Runtime.Threading;

import java.util.ArrayList;
import java.util.List;

import BackEnd.ErrorManagement.JGELEMS;
import BackEnd.ErrorManagement.Exceptions.JGELNotImplementedException;
import BackEnd.ErrorManagement.Exceptions.JGELStaticException;
import BackEnd.ErrorManagement.Exceptions.JGELThreadPersistance;
import BackEnd.Runtime.Hooking.HookKey;
import BackEnd.Runtime.Hooking.JGELHook;

/**
 * Main thread management tool for JGEL.
 * Creates, executes, stores, manages and disposes of threads.
 * 
 * @author gordie
 * @since V2
 * @see ThreadManagement
 */
public class JGELThreadManager implements JGELHook{
	
	/**
	 * Instantiator event hook use only.
	 * 
	 * @param key - parameter for unlocking instantiation for event hooking.
	 * This is trivial, but is an further attempt to enforce instantiation only for in valid areas.
	 * @deprecated NOT FOR TYPICAL USE.
	 */
	public JGELThreadManager(HookKey key) {}

	/**
	 * Hidden instantiator.
	 * This class is not instantiable.
	 */
	@SuppressWarnings("unused")
	private JGELThreadManager() throws JGELStaticException {
		throw new JGELStaticException("JGELThreadManager is not instantiable");
	}

	//Properties
	private static List<JGELThread> Threads = new ArrayList<JGELThread>();
	private static long threadcount = 0;
	
	//Methods
	
	
	/**
	 * Returns a copy of the list of threads.
	 * This was intended for DevTools use only, threads should never be directly edited.
	 */
	public static List<JGELThread> GetAllThreads(){
		return List.copyOf(Threads);
	}
	
	/**
	 * Create, stores and executes thread with the parameters given.
	 * 
	 * @param runnable - the class to run
	 * @param Name - custom identifyable name of the thread.
	 * @return null if a thread with the same name, or same runnable already exsists.
	 * @return The JGELThread container created.
	 */
	public static JGELThread CreateThread(JGELRunnable runnable, String Name){
		if (GetThread(runnable) != null) {
			JGELEMS.Warn("Tried to create a dublicate thread with a runnable that already exsists! Call will be ignored.");
			return null;
		}
		
		if (GetThread(Name) != null) {
			JGELEMS.Warn("Tried to create a thread with a thread name that's already in use! Call will be ignored.");
			return null;
		}
		
		JGELThread container = new JGELThread(new Thread(runnable), runnable, GenerateID(), Name);
		Threads.add(container);
		threadcount++;
		
		container.getThread().start();
		
		return container;
	}
	
	/**
	 * Thread ID's are sequential.
	 * @return
	 */
	private static Long GenerateID() {
		return threadcount + 1;
	}
	
	/**
	 * Search for a thread using runnable
	 *
	 * TODO test for ambiguity in comparisions between different instances of the same runnable. Do different instances return the same thread?
	 * 
	 * @param Runnable - the runnable in use
	 * @return null if no threads match
	 * @return the JGELThread container of a matching thread.
	 */
	public static JGELThread GetThread(JGELRunnable Runnable) {
		for (JGELThread thd : Threads) {
			if (thd.getRunnable() == Runnable) {
				return thd;
			}
		}
		return null;
	}
	
	
	/**
	 * Search for a thread using name
	 *
	 * @param Name - name of the thread
	 * @return null if no threads match
	 * @return the JGELThread container of a matching thread.
	 */
	public static JGELThread GetThread(String name) {
		for (JGELThread thd : Threads) {
			if (thd.getThread().getName().equals(name)) {
				return thd;
			}
		}
		return null;
	}
	
	
	/**
	 * Search for a thread using ID
	 *
	 * @param ID - The generated ID of the thread.
	 * @return null if no threads match
	 * @return the JGELThread container of a matching thread.
	 */
	public static JGELThread GetThread(Long ID) {
		for (JGELThread thd : Threads) {
			if (thd.getID() == ID) {
				return thd;
			}
		}
		return null;
	}
	
	/**
	 * Search for a thread using container
	 *
	 * @param container - the JGELThread container of the thread
	 * @return null if no threads match
	 * @return the JGELThread container of a matching thread.
	 * @deprecated This shit takes a container and returns a container lmao what the fuuuuuuck.
	 */
	public static JGELThread GetThread(JGELThread container) {
		for (JGELThread thd : Threads) {
			if (thd == container) {
				return thd;
			}
		}
		return null;
	}
	
	/**
	 * Invokes the JGELRunnable ThreadStop method on a thread.
	 * This method requests the thread to finish up and close itself. It is not forceful, and does not check for completion of a closure.
	 * 
	 * @see Thread Management, Disposing a thread, requesting a thread to close.
	 * @param thread
	 */
	public static void InvokeThreadStop(JGELThread thread) throws JGELThreadPersistance {
		try {
			thread.getRunnable().stop();
		} catch (Exception e) {
			JGELEMS.HandleException(new JGELThreadPersistance(thread, "an exception was thrown in the thread's stop method."));
		}
	}
	
	/**
	 * This method is more forceful in halting and removing threads.
	 * 
	 * 
	 * @param thread Thread container to close and remove.
	 * @deprecated by java since v1.2.
	 * @see Thread.stop();
	 * @return false if failed to close, dispose and remove thread sucessfully.
	 * @return true if thread already does not exsist, or was successfully removed.
	 */
	public static boolean ForceDisposeThread(JGELThread thread){
		if (GetThread(thread) == null) {
			JGELEMS.Warn("Attempted to kill a null thread. Ignoring Thread Dispose call.");
			return true;
		}
		
		try {
			InvokeThreadStop(thread);
		} catch (JGELThreadPersistance e) {}
	
		if (thread.getThread().isAlive()) {
			thread.getThread().interrupt();
			if (thread.getThread().isAlive()) {
				thread.getThread().stop();
			} 
		} 
		
		if (thread.getThread().isAlive()) {
			JGELEMS.HandleException(new JGELThreadPersistance(thread, "Java failed to force close the thread."));
			return false;
		} else {
			Threads.remove(thread);
			return true;
		}
	}

	@Override
	public void EnterUpdateEvent() {
		
	}

	@Override
	public void UpdateEvent() {
		JGELThreadManager.Update();
	}

	private static void Update() {
		for (JGELThread thread : Threads) {
			if (!thread.getThread().isAlive()) {
				Threads.remove(thread);
			}
		}
	}
	
	
	//TODO Thread waiting rewrite
	public static void WaitAllThreads() {
		JGELEMS.HandleException(new JGELNotImplementedException("WaitAllThreads"));
	}
	
	public static void WaitThread(JGELThread thread) {
		
	}
	

	@Override
	public void ExitUpdateEvent() {
		
	}	
}
