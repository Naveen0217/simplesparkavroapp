/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudera.sparkavro

import com.esotericsoftware.kryo.Kryo

import org.apache.avro.mapred.AvroKey
import org.apache.avro.mapreduce.AvroKeyInputFormat

import org.apache.hadoop.io.NullWritable
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.serializer.{KryoSerializer, KryoRegistrator}

object SparkSpecificAvroReader {
  class MyRegistrator extends KryoRegistrator {
    override def registerClasses(kryo: Kryo) {
      kryo.register(classOf[User])
    }
  }

  def main(args: Array[String]) {
    val inPath = args(0)

    val sparkConf = new SparkConf().setAppName("Spark Avro")
    sparkConf.set("spark.serializer", classOf[KryoSerializer].getName)
    sparkConf.set("spark.kryo.registrator", classOf[MyRegistrator].getName)
    val sc = new SparkContext(sparkConf)

    val conf = new Job()
    FileInputFormat.setInputPaths(conf, inPath)
    val records = sc.newAPIHadoopRDD(conf.getConfiguration,
      classOf[AvroKeyInputFormat[User]],
      classOf[AvroKey[User]],
      classOf[NullWritable]).map(x => x._1.datum)

    val names = records.map(_.getName).collect()
    println("num records: " + names.size)
    println("names: " + names.mkString(", "))
  }
}
