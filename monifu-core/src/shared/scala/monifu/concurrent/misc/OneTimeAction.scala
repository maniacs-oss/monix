/*
 * Copyright (c) 2014 by its authors. Some rights reserved. 
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
 
package monifu.concurrent.misc

final class OneTimeAction[+R] private (cb: => R) extends (() => R) {
  private[this] lazy val action = cb
  def apply() = action
}

object OneTimeAction {
  def apply[R](cb: => R): OneTimeAction[R] =
    new OneTimeAction[R](cb)
}
