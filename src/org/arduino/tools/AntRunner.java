/**
 * AntRunner - A wrapper for the Apache ANT build tool
   Copyright (C) 2009 Christopher Ladden All rights reserved.

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this library; if not, write to the Free Software
   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
   USA
 *
 */

package org.arduino.tools;

import java.io.File;
import java.io.Writer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.listener.AnsiColorLogger;

public class AntRunner {
	static public final int MSG_WARN    = Project.MSG_WARN;
	static public final int MSG_INFO    = Project.MSG_INFO;
	static public final int MSG_VERBOSE = Project.MSG_VERBOSE;

	volatile int outputLevel = MSG_WARN;
	volatile boolean isRunning = false;
	volatile boolean runSuccessful = false;
	volatile String propertyList[];

	/**
	 *
	 * @return
	 * Returns the status of the runner. True means it's currently executing.
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 *
	 * @param level
	 * The output message level.
	 */
	public void setOutputLevel(int level) {
		outputLevel = level;
	}

	/**
	 * Most quiet output level
	 */
	public void setOutputQuiet() {
		outputLevel = MSG_WARN;
	}

	/**
	 * Most verbose output level.
	 */
	public void setOutputVerbose() {
		outputLevel = MSG_VERBOSE;
	}

	/**
	 * Check the last run result
	 * @return
	 * Returns the last run result.
	 */
	public boolean getLastRunStatus() {
		return runSuccessful;
	}

	/**
	 * A Busy-wait loop design to wait for the run to complete.
	 */
	public void waitForCompletion() {

		while(this.isRunning()) {
		    ;
		    if (0==1) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    }
		}
	}

	/**
	 *
	 * @param buildfile
	 *            The buildfile to run.
	 * @param antTaget
	 * 			  The target name in the buildfile to run.
	 * @param propertyList
	 * 		      A list of properties to set in the project.
	 * 			  The format is as follows: "property1","value1",
	 * 									    "property2", "value2",...
	 */
	public void run(String buildfile, final String antTarget,
				    final String[] propertyList) {

	final File buildFile = new File(buildfile);

		/*
		 * A class to run ant in a new thread.
		 */
		class AntRunnable implements Runnable {

			//@Override
			public void run() {
				runSuccessful = false; //reset the status

				Project p = new Project();
				p.setUserProperty("ant.file", buildFile.getAbsolutePath());
				for (int x=0; x < propertyList.length; x+=2) {
					p.setProperty(propertyList[x], propertyList[x+1]);
				}

				DefaultLogger consoleLogger = new DefaultLogger();
				consoleLogger.setErrorPrintStream(System.err);
				consoleLogger.setOutputPrintStream(System.out);
				consoleLogger.setMessageOutputLevel(outputLevel);

				p.addBuildListener(consoleLogger);

				try {
					p.fireBuildStarted();
					p.init();
					ProjectHelper helper = ProjectHelper.getProjectHelper();
					p.addReference("ant.projectHelper", helper);
					helper.parse(p, buildFile);

					if (antTarget != null) {
					   p.executeTarget(antTarget);
					} else {
					   p.executeTarget(p.getDefaultTarget());
					}

					p.fireBuildFinished(null);

					runSuccessful = true; //we're finished and good.

				} catch (BuildException e) {

					runSuccessful = false; //we're finished and unsuccessful
					p.fireBuildFinished(e);

				}

				//I've stopped
				isRunning = false;
			}
		}

		AntRunnable antRunnable = new AntRunnable();
		Thread antThread = new Thread(antRunnable);
		isRunning = true;
		antThread.start();

	}

	/**
	 * Wrapper code, could be used for running unit tests via the command line.
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		/* Nothing */
	}
}
