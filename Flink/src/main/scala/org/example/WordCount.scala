package org.example

import java.io.{FileWriter, PrintWriter}

import org.apache.flink.streaming.api.scala._

import scala.io.Source
import scala.collection.mutable.Map

object WordCount {
  def main(args: Array[String]): Unit = {
    // 获取运行时环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 全局并行度
    env.setParallelism(1)

    var neededDate: List[String] = List()
//    val months : Array[String] = Array("01","02","03","04","05","06","07","08","09","10","11","12")
    val months : Array[String] = Array("01","02","03","04","05","06","07","08","09","10","11")
    for (m <- months){
      neededDate = neededDate :+ ("2022-" + m)
    }

    for (dataDate <- neededDate) {
      val filePath = "/data/questions_" + dataDate + ".csv"
      val stream = env.readTextFile(filePath)

      val tmpStream = stream
        .flatMap(s => {
          val arr = s.split("\n")
          val re0 = new Array[Array[String]](arr.size)
          for (i <- 0 until arr.size) {
            re0(i) = arr(i)
              .split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")
          }
          re0
        })

      // 加权的数据
      val weightStream = tmpStream
        .flatMap(x => {
          var weight: Double = 1
          // 在最后会对整体除以1000，实际最终效果为对views除以1000
          // 为避免.sum对于double结果计算的不精确
          if (x.size > 4) {
            weight = 1 * (1000 * stringToDouble(x(x.size - 4)) + 1000 * stringToDouble(x(x.size - 3)) + stringToDouble(x(x.size - 2)))
            if (weight < 0) {
              weight = 0
            }
          }
          val wordArr = x(x.size - 1).split(";")
          val re = new Array[(String, Double)](wordArr.size)
          for (i <- 0 until wordArr.size) {
            re(i) = (wordArr(i), weight)
          }
          re
        })
        .keyBy(0)
        .sum(1)
        .map(r => {
          (r._1, r._2 / 1000)
        })

      // 没加权的数据
      val countStream = tmpStream
        .flatMap(x => {
          val wordArr : Array[String] = x(x.size - 1).split(";")
          val re = new Array[(String, Double)](wordArr.size)
          for (i <- 0 until wordArr.size) {
            re(i) = (wordArr(i), 1)
          }
          re
        })
        .keyBy(0)
        .sum(1)

      weightStream.print()
      weightStream.writeAsText("/data/outWeight/outWeight_" + dataDate + ".txt")

      countStream.print()
      countStream.writeAsText("/data/outCount/outCount_" + dataDate + ".txt")
    }

    // 执行
    env.execute()
    // env.execute()之后的代码都不会在被执行了，
    // 而所有有关flink流的部分都是在env.execute()时才会执行
    // 与流无关且在env.execute()之前的代码会先执行
  }

  def stringToDouble(str:String):Double = {
    if (str.equals("votes") || str.equals("answers") || str.equals("views")) {
      return 0
    }
    val s0 = str.replace("k", "000")
    val si = s0.replace("m", "000000")
    var result =  si.toDouble
//    if (result<0){
//      result = 0
//    }
    return result
  }
}
