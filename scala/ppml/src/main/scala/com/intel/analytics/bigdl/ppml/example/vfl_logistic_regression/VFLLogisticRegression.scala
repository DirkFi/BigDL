/*
 * Copyright 2021 The BigDL Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.analytics.bigdl.ppml.example


import com.intel.analytics.bigdl.ppml.FLContext
import com.intel.analytics.bigdl.ppml.algorithms.{VFLLogisticRegression, PSI}
import com.intel.analytics.bigdl.ppml.example.DebugLogger
import scopt.OptionParser

import collection.JavaConverters._
import collection.JavaConversions._


object VFLLogisticRegression extends DebugLogger{
  def getData(pSI: PSI, dataPath: String, rowKeyName: String, batchSize: Int = 4) = {
    val salt = pSI.getSalt()

    val spark = FLContext.getSparkSession()
    val df = spark.read.option("header", "true").csv(dataPath)
    val intersectionDf = pSI.uploadSetAndDownloadIntersection(df, salt, rowKeyName)
    val (trainDf, valDf) = ExampleUtils.splitDataFrameToTrainVal(intersectionDf)
    val testDf = trainDf.drop("Outcome")
    trainDf.show()
    testDf.show()
    (trainDf, valDf, testDf)
  }

  def main(args: Array[String]): Unit = {
    case class Params(dataPath: String = null,
                      rowKeyName: String = "ID",
                      hasLabel: Boolean = true,
                      learningRate: Float = 0.005f,
                      batchSize: Int = 4)
    val parser: OptionParser[Params] = new OptionParser[Params]("VFL Logistic Regression") {
      opt[String]('d', "dataPath")
        .text("data path to load")
        .action((x, params) => params.copy(dataPath = x))
        .required()
      opt[String]('r', "rowKeyName")
        .text("row key name of data")
        .action((x, params) => params.copy(rowKeyName = x))
      opt[Boolean]('y', "hasLabel")
        .text("this party has label or not")
        .action((x, params) => params.copy(hasLabel = x))
      opt[String]('l', "learningRate")
        .text("learning rate of training")
        .action((x, params) => params.copy(learningRate = x.toFloat))
      opt[String]('b', "batchSize")
        .text("batchsize of training")
        .action((x, params) => params.copy(batchSize = x.toInt))
    }
    val argv = parser.parse(args, Params()).head
    // load args and get data
    val dataPath = argv.dataPath
    val rowKeyName = argv.rowKeyName
    val learningRate = argv.learningRate
    val batchSize = argv.batchSize


    /**
     * Usage of BigDL PPML starts from here
     */
    FLContext.initFLContext()
    val pSI = new PSI()
    val (trainData, valData, testData) = getData(pSI, dataPath, rowKeyName, batchSize)

    // create LogisticRegression object to train the model
    val featureNum = if (argv.hasLabel) trainData.columns.size - 1 else trainData.columns.size
    val lr = new VFLLogisticRegression(featureNum, learningRate)
    lr.fitDataFrame(trainData, valData = valData, hasLabel = argv.hasLabel)
    lr.evaluateDataFrame(valData, hasLabel = argv.hasLabel)
    lr.predictDataFrame(testData)
  }

}
