/*
 * Copyright (c) 2014-2017 by its authors. Some rights reserved.
 * See the project homepage at: https://monix.io
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

package monix.interact

import monix.eval.{Coeval, Task}

object IterantFromIndexedSeqSuite extends BaseTestSuite {
  test("TaskStream.fromIndexedSeq") { implicit s =>
    check1 { (list: List[Int]) =>
      val result = TaskStream.fromIndexedSeq(list.toVector).toListL
      result === Task.now(list)
    }
  }

  test("CoevalStream.fromIndexedSeq") { implicit s =>
    check1 { (list: List[Int]) =>
      val result = CoevalStream.fromIndexedSeq(list.toVector).toListL
      result === Coeval.now(list)
    }
  }

  test("TaskStream.fromIndexedSeq(batchSize=1)") { implicit s =>
    check1 { (list: List[Int]) =>
      val result = TaskStream.fromIndexedSeq(list.toVector, batchSize = 1).toListL
      result === Task.now(list)
    }
  }

  test("CoevalStream.fromIndexedSeq(batchSize=1)") { implicit s =>
    check1 { (list: List[Int]) =>
      val result = CoevalStream.fromIndexedSeq(list.toVector, batchSize = 1).toListL
      result === Coeval.now(list)
    }
  }
}