/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKFileUtil {

   private static final String HOLD_SUFFIX = ".__hold__";
   private static final Logger LOG = LoggerFactory.getLogger(DKFileUtil.class);

   /**
    * insert the String prepend_ at the beginning of the content in File target_
    */
   public static void prepend(String prepend_, File target_) throws IOException {
      if ((prepend_ == null) || (target_ == null))
         return;
      if (!(target_.canRead() && (target_.canWrite())))
         throw new RuntimeException(String.format("file is not readable/writeable [%s]",
            target_));
      File holdFile = new File(target_.getParentFile(), target_.getName() + HOLD_SUFFIX);
      target_.renameTo(holdFile);
      OutputStream outStream = toBufferedOutputStream(target_);
      Writer outWriter = new OutputStreamWriter(outStream);
      outWriter.write(prepend_);
      outWriter.flush();

      FileInputStream holdInStream = new FileInputStream(holdFile);
      IOUtils.copy(holdInStream, outStream);
      outStream.flush();
      outStream.close();
      holdFile.delete();
   }

   /**
    * silently eats all exceptions, so only call if you know for a fact that the
    * file is writable, etc.
    */
   public static OutputStream toBufferedOutputStream(File target_) {
      if (target_ == null)
         return null;
      try {
         return new BufferedOutputStream(new FileOutputStream(target_));
      }
      catch (Exception e_) {
         LOG.info(null, e_);
         return null;
      }
   }

   public static boolean isRelative(File target_) {
      if (target_ == null)
         return false;
      String path = target_.getPath();
      if (path == null)
         return false;
      return path.startsWith(".");
   }

   public static boolean exists(String filePath_) {
      if (filePath_ == null)
         return false;
      File file = new File(filePath_);
      return file.exists();
   }

   public static String readFullyAsString(File file_) {
      if (file_ == null)
         return null;

      try {
         FileInputStream fileInputStream = new FileInputStream(file_);
         return DKStreamUtil.readFullyAsString(fileInputStream);
      }
      catch (IOException e_) {
         LOG.error(null, e_);
         return null;
      }
   }

   /**
    * will throw an exception if file already exists
    */
   public static void writeContents(File file_, byte[] contents_) throws IOException {
      if ((file_ == null) || (contents_ == null))
         return;

      if (!file_.createNewFile())
         throw new IOException(String.format(
            "can't write to '%s', probably a file already exists there.", file_));

      OutputStream outStream = new BufferedOutputStream(new FileOutputStream(file_));
      for (int i = 0; i < contents_.length; i++) {
         outStream.write(contents_[i]);
      }
      outStream.flush();
      outStream.close();
   }
}
