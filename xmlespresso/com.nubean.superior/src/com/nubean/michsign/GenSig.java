/*
 * GenerateSig.java
 *
 * Created on March 2, 2003, 4:45 PM
 */

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

package com.nubean.michsign;

import java.io.*;
import java.security.*;
import javax.swing.*;

import com.nubean.michutil.LocalizedResources;

/**
 * 
 * @author Ajay Vohra
 */
public class GenSig {

	public static void main(String[] args) {
		/* Generate a DSA signature */
		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogTitle(LocalizedResources.applicationResources
					.getString("open"));

			int ok = fileChooser.showDialog(new JFrame(),
					LocalizedResources.applicationResources
							.getString("open.file"));
			if (ok == JFileChooser.APPROVE_OPTION) {
				java.io.File file = fileChooser.getSelectedFile();
				File dir = file.getParentFile();
				String name = file.getName();
				int index = name.indexOf(".");
				if (index > 0) {
					name = name.substring(0, index);
				}

				/* Generate a key pair */
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA",
						"SUN");
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG",
						"SUN");

				keyGen.initialize(1024, random);

				KeyPair pair = keyGen.generateKeyPair();
				PrivateKey priv = pair.getPrivate();
				PublicKey pub = pair.getPublic();

				/*
				 * Create a Signature object and initialize it with the private
				 * key
				 */

				Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");

				dsa.initSign(priv);

				/* Update and sign the data */
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bufin = new BufferedInputStream(fis);
				byte[] buffer = new byte[1024];
				int len;
				while (bufin.available() != 0) {
					len = bufin.read(buffer);
					dsa.update(buffer, 0, len);
				}
				;

				bufin.close();

				/*
				 * Now that all the data to be signed has been read in, generate
				 * a signature for it
				 */

				byte[] realSig = dsa.sign();

				/* Save the signature in a file */
				File sigFile = new File(dir, name + ".sig");
				FileOutputStream sigfos = new FileOutputStream(sigFile);
				sigfos.write(realSig);

				sigfos.close();

				/* Save the public key in a file */
				byte[] key = pub.getEncoded();
				File pkFile = new File(dir, name + ".pk");
				FileOutputStream keyfos = new FileOutputStream(pkFile);
				keyfos.write(key);

				keyfos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
