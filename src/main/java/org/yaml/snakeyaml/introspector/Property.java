/**
 * Copyright (c) 2008-2010 Andrey Somov
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
package org.yaml.snakeyaml.introspector;

import java.lang.reflect.Type;

public abstract class Property implements Comparable<Property> {
    private final String name;
    private final Class<? extends Object> type;

    public Property(String name, Class<? extends Object> type) {
        this.name = name;
        this.type = type;
    }

    public Class<? extends Object> getType() {
        return type;
    }

    public abstract Type[] getActualTypeArguments();

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName() + " of " + getType();
    }

    public int compareTo(Property o) {
        return name.compareTo(o.name);
    }

    abstract public void set(Object object, Object value) throws Exception;

    abstract public Object get(Object object);
}