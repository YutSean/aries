/*
 * Copyright (c) 2019 R.C
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

package org.apache.toy;

public class Counter {

  private String name;
  private int count;

  private Counter(String name, boolean to_upper_case) {
    this.name = to_upper_case ? name.toUpperCase() : name;
  }

  public void inc() {
    count++;
  }

  public int getCount() {
    return count;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Counter{" + "name='" + name + '\'' + ", count=" + count + '}';
  }

  public static Counter createCounter(String name, boolean to_upper_case) {
    return new Counter(name, to_upper_case);
  }

  public static void main(String[] args) {
    Counter c = Counter.createCounter("what", true);
    c.inc();
    c.inc();
    c.inc();
    System.out.println(c);
  }

}