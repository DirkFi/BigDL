/*
 * Copyright 2016 The BigDL Authors.
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
package com.intel.analytics.bigdl.dllib.nn.abstractnn

import com.intel.analytics.bigdl.dllib.nn.{InitializationMethod, Zeros}

/**
 * The trait that contains setInitMethod
 */
trait Initializable {

  protected var weightInitMethod: InitializationMethod = Zeros
  protected var biasInitMethod: InitializationMethod = Zeros

  def setInitMethod(weightInitMethod: InitializationMethod = null,
                    biasInitMethod: InitializationMethod = null): this.type = {
    if (weightInitMethod != null) {
      this.weightInitMethod = weightInitMethod
    }

    if (biasInitMethod != null) {
      this.biasInitMethod = biasInitMethod
    }
    reset()
    this
  }

  def setInitMethod(initMethod: Array[InitializationMethod]): this.type = {
    throw new UnsupportedOperationException(s"setInitMethod with a array of InitializationMethod" +
      s" does not support for ${this.toString}")
  }

  def reset(): Unit

}
