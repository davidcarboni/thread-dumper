package org.workdocx.threaddump;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * This is how you run it.
 * 
 * @author David Carboni
 * 
 */
public class Main {

	/** The OS command which will be invoked in order to get a JVM thread dump. */
	public static final String COMMAND = "jstack";

	/** The interval in milliseconds between JVM thread dumps. */
	public static final long INTERVAL = 1000;

	private static int pid;
	private static List<String> searches = new ArrayList<String>();
	private static List<String> excludes = new ArrayList<String>();
	private static InputStream inputStream;

	/**
	 * Starts the process of taking thread dumps.
	 * 
	 * @param args
	 *            Define the pid and search terms.
	 * @throws IOException
	 *             If an error occurs.
	 */
	public static void main(String[] args) throws IOException {
		readParameters(args);
		dump();
	}

	private static void dump() throws IOException {

		do {
			ProcessBuilder processBuilder = new ProcessBuilder(COMMAND, String.valueOf(pid));
			Process process = processBuilder.start();
			inputStream = new BufferedInputStream(process.getInputStream());

			// Read the output:
			List<String> readLines;
			try {
				readLines = IOUtils.readLines(inputStream);
			} finally {
				IOUtils.closeQuietly(inputStream);
			}

			// Select output:
			if (searches.size() == 0) {
				// Print everything:
				for (String line : readLines) {
					System.out.println(line);
				}
			} else {

				List<List<String>> threads = splitThreads(readLines);
				for (List<String> thread : threads) {

					// Does this thread contain any frames we're interested in?
					boolean exclude = false;
					boolean print = false;
					for (String line : thread) {
						for (String search : searches) {
							if (StringUtils.containsIgnoreCase(line, search)) {
								print = true;
							}
						}
						for (String search : excludes) {
							if (StringUtils.containsIgnoreCase(line, search)) {
								exclude = true;
							}
						}
					}

					// Print away, Jeeves:
					if (print && !exclude) {
						for (String line : thread) {
							System.out.println(line);
						}
						System.out.println();
					}
				}
			}

			// Take a breath:
			sleep();

//			System.out.println();
//			System.out.println("---");
//			System.out.println();

		} while (true);
	}

	private static List<List<String>> splitThreads(List<String> readLines) {
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> thread = new ArrayList<String>();
		result.add(thread);

		int i = 0;
		while (i < readLines.size()) {

			while (i < readLines.size() && StringUtils.isNotBlank(readLines.get(i))) {
//				System.out.println("adding: " + readLines.get(i));
				thread.add(readLines.get(i));
				i++;
			}

			// Create a new list for the next thread:
			if (i < readLines.size() && result.get(result.size() - 1).size() != 0) {
//				System.out.println("Next thread..");
				thread = new ArrayList<String>();
				result.add(thread);
				i++;
			}
		}

		return result;
	}

	private static void sleep() {
		try {
			Thread.sleep(INTERVAL);
		} catch (InterruptedException e) {
			System.out.println(ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Reads the process ID parameter.
	 * 
	 * @param args
	 *            The command-line parameters.
	 */
	private static void readParameters(String[] args) {

		// Get the PID:
		try {
			pid = Integer.parseInt(args[0]);
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Please provide a Java process ID.");
			System.exit(1);
		} catch (NumberFormatException e) {
			System.out.println("Unable to parse Java process ID: [" + args[0] + "]");
			System.exit(1);
		}

		// Get the search/exclude strings:
		int i = 1;
		while (i < args.length) {
			String search = args[i++];
			if (search.startsWith("-")) {
				excludes.add(search.substring(1));
			} else if (search.startsWith("+")) {
				searches.add(search.substring(1));
			} else {
				searches.add(search);
			}
		}
	}
}
