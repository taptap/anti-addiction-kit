/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tapsdk.antiaddiction.reactor.functions;

/**
 * A three-argument action.
 * @param <T1> the first argument type
 * @param <T2> the second argument type
 * @param <T3> the third argument type
 */
public interface Action3<T1, T2, T3> extends Action {
    void call(T1 t1, T2 t2, T3 t3);
}