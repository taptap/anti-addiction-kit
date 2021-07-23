/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tapsdk.antiaddiction.skynet.okio;


import com.tapsdk.antiaddiction.skynet.okio.Segment;

/**
 * A collection of unused segments, necessary to avoid GC churn and zero-fill.
 * This pool is a thread-safe static singleton.
 */
final class SegmentPool {
    /**
     * The maximum number of bytes to pool.
     */
    // TODO: Is 64 KiB a good maximum size? Do we ever have that many idle segments?
    static final long MAX_SIZE = 64 * 1024; // 64 KiB.

    /**
     * Singly-linked list of segments.
     */
    static
    com.tapsdk.antiaddiction.skynet.okio.Segment next;

    /**
     * Total bytes in this pool.
     */
    static long byteCount;

    private SegmentPool() {
    }

    static com.tapsdk.antiaddiction.skynet.okio.Segment take() {
        synchronized (SegmentPool.class) {
            if (next != null) {
                com.tapsdk.antiaddiction.skynet.okio.Segment result = next;
                next = result.next;
                result.next = null;
                byteCount -= com.tapsdk.antiaddiction.skynet.okio.Segment.SIZE;
                return result;
            }
        }
        return new com.tapsdk.antiaddiction.skynet.okio.Segment(); // Pool is empty. Don't zero-fill while holding a lock.
    }

    static void recycle(com.tapsdk.antiaddiction.skynet.okio.Segment segment) {
        if (segment.next != null || segment.prev != null) throw new IllegalArgumentException();
        if (segment.shared) return; // This segment cannot be recycled.
        synchronized (SegmentPool.class) {
            if (byteCount + com.tapsdk.antiaddiction.skynet.okio.Segment.SIZE > MAX_SIZE) return; // Pool is full.
            byteCount += com.tapsdk.antiaddiction.skynet.okio.Segment.SIZE;
            segment.next = next;
            segment.pos = segment.limit = 0;
            next = segment;
        }
    }
}
