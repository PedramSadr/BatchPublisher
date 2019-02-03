package com.adv.utils

import java.io.File

object FileUtils {

  def allFiles(path:File):List[File]=
  {
    val parts=path.listFiles.toList.partition(_.isDirectory)
    parts._2 ::: parts._1.flatMap(allFiles)
  }
}
