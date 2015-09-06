/*
The MIT License (MIT)

Copyright (c) 2015 NuBean LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.nubean.michutil;

import java.awt.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

/**
 * <p>
 * Title: Michigan XML Editor
 * </p>
 * <p>
 * Description: This edits an XML document based on an XML schema.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2001
 * </p>
 * <p>
 * Company: Nubean LLC
 * </p>
 * 
 * @author Ajay Vohra
 * @version 1.0
 */

public class BasicPrint {
	private boolean PrintJobDone = false;

	public BasicPrint(File fileToPrint, DocFlavor flavor) throws Exception {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(
					fileToPrint));
			PrintService dservice = PrintServiceLookup
					.lookupDefaultPrintService();
			PrintService[] services = PrintServiceLookup.lookupPrintServices(
					flavor, null);

			if (services == null || services.length < 1)
				services = PrintServiceLookup.lookupPrintServices(null, null);

			PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
			aset.add(new Copies(1));
			aset.add(OrientationRequested.PORTRAIT);
			aset.add(Sides.ONE_SIDED);
			aset.add(MediaSizeName.ISO_A4);
			PrintService service = ServiceUI.printDialog(
					(GraphicsConfiguration) null, 60, 60, services,
					(PrintService) dservice, (DocFlavor) flavor, aset);

			if (service != null) {
				// Create the print job
				final DocPrintJob job = service.createPrintJob();
				Doc doc = new SimpleDoc(is, flavor, null);
				// Monitor print job events; for the implementation of
				// PrintJobWatcher,
				PrintJobWatcher pjDone = new PrintJobWatcher(job);
				// Print it
				job.print(doc, (PrintRequestAttributeSet) aset);
				// Wait for the print job to be done
				pjDone.waitForDone();
			}

			// It is now safe to close the input stream
			is.close();
		} finally {
			try {
				synchronized (BasicPrint.this) {
					PrintJobDone = true;
					BasicPrint.this.notify();
				}
			} catch (Exception e) {
			}
		}
	}

	public synchronized void waitForDone() throws InterruptedException {
		while (!PrintJobDone) {
			wait();
		}
	}

	private class PrintJobWatcher {

		// true iff it is safe to close the print job's input stream
		private boolean done = false;
		private int lastEvent = 0;

		PrintJobWatcher(DocPrintJob job) {
			// Add a listener to the print job
			job.addPrintJobListener(new PrintJobAdapter() {
				public void printJobRequiresAttention(PrintJobEvent pje) {
					lastEvent = pje.getPrintEventType();
				}

				public void printDataTransferCompleted(PrintJobEvent pje) {
					lastEvent = pje.getPrintEventType();
				}

				public void printJobCanceled(PrintJobEvent pje) {
					lastEvent = pje.getPrintEventType();
					allDone();
				}

				public void printJobCompleted(PrintJobEvent pje) {
					lastEvent = pje.getPrintEventType();
					allDone();
				}

				public void printJobFailed(PrintJobEvent pje) {
					lastEvent = pje.getPrintEventType();
				}

				public void printJobNoMoreEvents(PrintJobEvent pje) {
					lastEvent = pje.getPrintEventType();
					allDone();
				}

				private void allDone() {
					synchronized (PrintJobWatcher.this) {
						done = true;
						PrintJobWatcher.this.notify();
					}
				}
			});
		}

		/** Description of the Method */
		public synchronized void waitForDone() throws InterruptedException {
			while (!done) {
				wait();
			}
		}
	}

}