package com.shinkson47.opex.backend.runtime.entry;

import com.shinkson47.opex.backend.runtime.errormanagement.EMSHelper;
import com.shinkson47.opex.backend.runtime.console.Console;
import com.shinkson47.opex.backend.runtime.errormanagement.exceptions.OPEXDisambiguationException;
import com.shinkson47.opex.backend.runtime.hooking.HookKey;
import com.shinkson47.opex.backend.runtime.threading.OPEXThreadManager;
import com.shinkson47.opex.frontend.window.prefab.Splash;
import com.shinkson47.opex.backend.runtime.console.instructions.*;

/**
 * This class is intended for use only by OPEX. A container for organised and
 * fragmentation of OPEX's startup routine.
 *
 * Subroutines are written in source following the order of expected correct
 * execution.
 *
 * @author gordie
 */
public final class StartupHelper {

	/**
	 * Pre engine startup
	 */
	protected static void preStart(){
		try {
			OPEXThreadManager.createThread(new Splash(), "OPEXStartSplash");										//Open splash screen in background.
		} catch (OPEXDisambiguationException e) {}																		//A splash thread already exsist, do nothing. This should not be possible.
	}

	/**
	 * Post engine startup
	 */
	protected static void postStart(){
		try {
			OPEXThreadManager.createThread(OPEX.getGameSuper(), "OPEXPayloadThread"); // Startup game.
			Console.internalLog("Payload thread started.");
		} catch (Exception e) {
			EMSHelper.warn("An error occurred within the game's startup routine, OPEX will now shutdown.");
			EMSHelper.handleException(e);
			OPEX.getEngineSuper().stop();
		}
	}

	protected static void runStartupSubroutines(){
		startRunnables();
		addConsoleInstructions();
	}


	/**
	 * Adds OPEX's default internal console instructions to the console.
	 */
	private static void addConsoleInstructions() {
		Console.addInstruction(new INSTClear());
		Console.addInstruction(new INSTHelp());
		Console.addInstruction(new INSTList());
		Console.addInstruction(new INSTThread());
		Console.addInstruction(new INSTEngine());
	}

	/**
	 * Uses the thread manager to execute OPEX's internal runables, causing them to
	 * run thier own startup routines and begin to idle.
	 *
	 * This stage should be called once all resources the executables need have been
	 * loaded, and they've been configured correctly.
	 */
	private static void startRunnables() {
		try {
			OPEXThreadManager.createThread(new Console(), "OPEXConsole");

			/*
			 	Hooking
			 */
			OPEXThreadManager.createThread(OPEX.getHookUpdater(), "OPEXHookUpdaterThread");						// Run OPEX's default hook updater in a new thread.
			OPEX.getHookUpdater().registerUpdateHook(new OPEXThreadManager(), "OPEXThreadManager");				// Add the static thread manager to the default hook updater, so that it can manage threads.
		} catch (OPEXDisambiguationException e) { EMSHelper.handleException(e); }
	}

}
