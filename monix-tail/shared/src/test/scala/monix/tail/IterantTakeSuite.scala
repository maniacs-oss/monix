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

package monix.tail

import monix.eval.{Coeval, Task}
import monix.execution.cancelables.BooleanCancelable
import monix.execution.exceptions.DummyException
import monix.execution.internal.Platform
import monix.tail.Iterant.Suspend
import org.scalacheck.Test
import org.scalacheck.Test.Parameters

object IterantTakeSuite extends BaseTestSuite {
  override lazy val checkConfig: Parameters = {
    if (Platform.isJVM)
      Test.Parameters.default.withMaxSize(256)
    else
      Test.Parameters.default.withMaxSize(32)
  }

  test("Iterant[Task].take equivalence with List.take") { implicit s =>
    check3 { (list: List[Int], idx: Int, nr: Int) =>
      val stream = arbitraryListToIterantTask(list, math.abs(idx) + 1)
      val length = list.length
      val n =
        if (nr == 0) 0
        else if (length == 0) math.abs(nr)
        else math.abs(math.abs(nr) % length)

      stream.take(n).toListL === stream.toListL.map(_.take(n))
    }
  }

  test("Iterant[Coeval].take triggers early stop") { implicit s =>
    check3 { (list: List[Int], idx: Int, nr: Int) =>
      val cancelable = BooleanCancelable()
      val stream = arbitraryListToIterantCoeval(list, math.abs(idx) + 1)
        .doOnEarlyStop(Coeval.eval(cancelable.cancel()))

      val length = list.length
      val n =
        if (nr == 0) 0
        else if (length == 0) math.abs(nr)
        else math.abs(math.abs(nr) % length)

      stream.take(n).toList == list.take(n) &&
        (n >= length || cancelable.isCanceled)
    }
  }

  test("Iterant.take protects against broken batches") { implicit s =>
    check1 { (iter: Iterant[Task, Int]) =>
      val dummy = DummyException("dummy")
      val suffix = Iterant[Task].nextBatchS[Int](new ThrowExceptionBatch(dummy), Task.now(Iterant[Task].empty), Task.unit)
      val stream = iter ++ suffix
      val received = stream.take(Int.MaxValue)
      received === Iterant[Task].haltS[Int](Some(dummy))
    }
  }

  test("Iterant.take protects against broken cursors") { implicit s =>
    check1 { (iter: Iterant[Task, Int]) =>
      val dummy = DummyException("dummy")
      val suffix = Iterant[Task].nextCursorS[Int](new ThrowExceptionCursor(dummy), Task.now(Iterant[Task].empty), Task.unit)
      val stream = iter ++ suffix
      val received = stream.take(Int.MaxValue)
      received === Iterant[Task].haltS[Int](Some(dummy))
    }
  }

  test("Iterant.take triggers early stop on exception") { _ =>
    check1 { (iter: Iterant[Coeval, Int]) =>
      val cancelable = BooleanCancelable()
      val dummy = DummyException("dummy")
      val suffix = Iterant[Coeval].nextCursorS[Int](new ThrowExceptionCursor(dummy), Coeval.now(Iterant[Coeval].empty), Coeval.unit)
      val stream = (iter ++ suffix).doOnEarlyStop(Coeval.eval(cancelable.cancel()))

      intercept[DummyException] { stream.toList }
      cancelable.isCanceled
    }
  }

  test("Iterant.take suspends execution for NextCursor or NextBatch") { _ =>
    val iter1 = Iterant[Coeval].nextBatchS(Batch(1,2,3), Coeval.now(Iterant[Coeval].empty[Int]), Coeval.unit)
    assert(iter1.take(2).isInstanceOf[Suspend[Coeval, Int]], "NextBatch should be suspended")
    assertEquals(iter1.take(2).toList, List(1, 2))

    val iter2 = Iterant[Coeval].nextCursorS(BatchCursor(1,2,3), Coeval.now(Iterant[Coeval].empty[Int]), Coeval.unit)
    assert(iter2.take(2).isInstanceOf[Suspend[Coeval, Int]], "NextCursor should be suspended")
    assertEquals(iter2.take(2).toList, List(1, 2))
  }
}