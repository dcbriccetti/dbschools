package com.dbschools.picker

import java.io.BufferedReader
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

/**
 * Helps manage the list of names to pick from
 */
class NamesManager {
  private val lineSep = System.getProperty("line.separator")
  var fileContents: String = null
  val sections: List[Section] = new ArrayList[Section]();

  class Section(val sectionData: String) {
    private val i = sectionData.indexOf(lineSep);
    val title = sectionData.substring(0, i);
    val data = sectionData.substring(i + lineSep.length());
  }

  /**
   * Gets the contents of the file of names via JNLP.
   * @return file contents
   */
  def getFileContentsWithJnlp(): String = {
    sections.clear()
    try {
      // Try to get the data using JNLP's FileOpenService
      val fos = ServiceManager.lookup("javax.jnlp.FileOpenService").asInstanceOf[FileOpenService]
      val contents = fos.openFileDialog(null, null)
      if (contents != null) {
        val bufferedReader = new BufferedReader(new InputStreamReader(
                contents.getInputStream()))
        val dataLength = contents.getLength().asInstanceOf[Int]
        if (dataLength > 0) {
          val buf = new Array[Char](dataLength)
          bufferedReader.read(buf)
          fileContents = new String(buf)
        }
      }
    } catch {
      case e: UnavailableServiceException => {
        // If we're not running under JNLP, we must be standalone testing
        fileContents = ":Math" + lineSep + "Dave" + lineSep + "Devon" + lineSep +
                ":Science" + lineSep + "Anne" + lineSep + "Ardy"
      }
      case e: IOException => e.printStackTrace()
    }

    if (fileContents.charAt(0) == ':') {
      // File contains group divisions
      fileContents.split(":").foreach(sectionData => {
        if (sectionData.trim().length() > 0) {
          sections.add(new Section(sectionData))
        }
      })
    }

    fileContents
  }
  
  def hasContents: Boolean = {
    fileContents != null
  }
  
}