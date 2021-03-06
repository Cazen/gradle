/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.internal.tasks.cache;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentMap;

public class MapBasedTaskOutputCache implements TaskOutputCache {
    private final String description;
    private final ConcurrentMap<String, byte[]> delegate;

    public MapBasedTaskOutputCache(String description, ConcurrentMap<String, byte[]> delegate) {
        this.description = description;
        this.delegate = delegate;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public TaskOutputReader get(TaskCacheKey key) throws IOException {
        final byte[] bytes = delegate.get(key.getHashCode().toString());
        if (bytes == null) {
            return null;
        }
        return new TaskOutputReader() {
            @Override
            public ByteSource read() throws IOException {
                return ByteSource.wrap(bytes);
            }
        };
    }

    @Override
    public void put(TaskCacheKey key, TaskOutputWriter output) throws IOException {
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        output.writeTo(new ByteSink() {
            @Override
            public OutputStream openStream() throws IOException {
                return data;
            }
        });
        delegate.put(key.getHashCode().toString(), data.toByteArray());
    }
}
