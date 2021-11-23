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
package org.yaml.snakeyaml;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.viaversion.viaversion.util.YamlConstructor;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 * Public YAML interface. Each Thread must have its own instance.
 */
public class Yaml {
    private final Dumper dumper;
    private final Loader loader;
    private final Resolver resolver;
    private String name;

    public Yaml(DumperOptions options) {
        this(new Loader(), new Dumper(options));
    }

    public Yaml(Dumper dumper) {
        this(new Loader(), dumper);
    }

    public Yaml(Loader loader) {
        this(loader, new Dumper(new DumperOptions()));
    }

    /**
     * Create Yaml instance. It is safe to create a few instances and use them
     * in different Threads.
     *
     * @param loader
     *            Loader to parse incoming documents
     * @param dumper
     *            Dumper to emit outgoing objects
     */
    public Yaml(Loader loader, Dumper dumper) {
        this(loader, dumper, new Resolver());
    }

    /**
     * Create Yaml instance. It is safe to create a few instances and use them
     * in different Threads.
     * @param loader
     *            Loader to parse incoming documents
     * @param dumper
     *            Dumper to emit outgoing objects
     * @param resolver
     */
    public Yaml(Loader loader, Dumper dumper, Resolver resolver) {
        this.loader = loader;
        loader.setAttached();
        this.dumper = dumper;
        dumper.setAttached();
        this.resolver = resolver;
        this.loader.setResolver(resolver);
        this.name = "Yaml:" + System.identityHashCode(this);
    }

    public Yaml() {
        this(new Loader(), new Dumper(new DumperOptions()));
    }

    /**
     * Serialize a Java object into a YAML String.
     * 
     * @param data
     *            Java object to be Serialized to YAML
     * @return YAML String
     */
    public String dump(Object data) {
        List<Object> list = new ArrayList<Object>(1);
        list.add(data);
        return dumpAll(list.iterator());
    }

    /**
     * Serialize a sequence of Java objects into a YAML String.
     * 
     * @param data
     *            Iterator with Objects
     * @return YAML String with all the objects in proper sequence
     */
    public String dumpAll(Iterator<? extends Object> data) {
        StringWriter buffer = new StringWriter();
        dumpAll(data, buffer);
        return buffer.toString();
    }

    /**
     * Serialize a Java object into a YAML stream.
     * 
     * @param data
     *            Java object to be Serialized to YAML
     * @param output
     *            stream to write to
     */
    public void dump(Object data, Writer output) {
        List<Object> list = new ArrayList<Object>(1);
        list.add(data);
        dumpAll(list.iterator(), output);
    }

    /**
     * Serialize a sequence of Java objects into a YAML stream.
     * 
     * @param data
     *            Iterator with Objects
     * @param output
     *            stream to write to
     */
    public void dumpAll(Iterator<? extends Object> data, Writer output) {
        dumper.dump(data, output, resolver);
    }

    /**
     * Parse the first YAML document in a String and produce the corresponding
     * Java object. (Because the encoding in known BOM is not respected.)
     * 
     * @param yaml
     *            YAML data to load from (BOM must not be present)
     * @return parsed object
     */
    public Object load(String yaml) {
        return loader.load(new StringReader(yaml));
    }

    /**
     * Parse the first YAML document in a stream and produce the corresponding
     * Java object.
     * 
     * @param io
     *            data to load from (BOM is respected and removed)
     * @return parsed object
     */
    public Object load(InputStream io) {
        return loader.load(new UnicodeReader(io));
    }

    /**
     * Parse the first YAML document in a stream and produce the corresponding
     * Java object.
     * 
     * @param io
     *            data to load from (BOM must not be present)
     * @return parsed object
     */
    public Object load(Reader io) {
        return loader.load(io);
    }

    /**
     * Parse all YAML documents in a String and produce corresponding Java
     * objects.
     * 
     * @param yaml
     *            YAML data to load from (BOM must not be present)
     * @return an iterator over the parsed Java objects in this String in proper
     *         sequence
     */
    public Iterable<Object> loadAll(Reader yaml) {
        return loader.loadAll(yaml);
    }

    /**
     * Parse all YAML documents in a String and produce corresponding Java
     * objects. (Because the encoding in known BOM is not respected.)
     * 
     * @param yaml
     *            YAML data to load from (BOM must not be present)
     * @return an iterator over the parsed Java objects in this String in proper
     *         sequence
     */
    public Iterable<Object> loadAll(String yaml) {
        return loadAll(new StringReader(yaml));
    }

    /**
     * Parse all YAML documents in a stream and produce corresponding Java
     * objects.
     * 
     * @param yaml
     *            YAML data to load from (BOM is respected and ignored)
     * @return an iterator over the parsed Java objects in this stream in proper
     *         sequence
     */
    public Iterable<Object> loadAll(InputStream yaml) {
        return loadAll(new UnicodeReader(yaml));
    }

    /**
     * Parse the first YAML document in a stream and produce the corresponding
     * representation tree.
     * 
     * @param io
     *            stream of a YAML document
     * @return parsed root Node for the specified YAML document
     */
    public Node compose(Reader io) {
        return loader.compose(io);
    }

    /**
     * Parse all YAML documents in a stream and produce corresponding
     * representation trees.
     * 
     * @param io
     *            stream of YAML documents
     * @return parsed root Nodes for all the specified YAML documents
     */
    public Iterable<Node> composeAll(Reader io) {
        return loader.composeAll(io);
    }

    /**
     * Add an implicit scalar detector. If an implicit scalar value matches the
     * given regexp, the corresponding tag is assigned to the scalar.
     * 
     * @deprecated use Tag instead of String
     * @param tag
     *            tag to assign to the node
     * @param regexp
     *            regular expression to match against
     * @param first
     *            a sequence of possible initial characters or null (which means
     *            any).
     * 
     */
    public void addImplicitResolver(String tag, Pattern regexp, String first) {
        addImplicitResolver(new Tag(tag), regexp, first);
    }

    /**
     * Add an implicit scalar detector. If an implicit scalar value matches the
     * given regexp, the corresponding tag is assigned to the scalar.
     * 
     * @param tag
     *            tag to assign to the node
     * @param regexp
     *            regular expression to match against
     * @param first
     *            a sequence of possible initial characters or null (which means
     *            any).
     */
    public void addImplicitResolver(Tag tag, Pattern regexp, String first) {
        resolver.addImplicitResolver(tag, regexp, first);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Get a meaningful name. It simplifies debugging in a multi-threaded
     * environment. If nothing is set explicitly the address of the instance is
     * returned.
     * 
     * @return human readable name
     */
    public String getName() {
        return name;
    }

    /**
     * Set a meaningful name to be shown in toString()
     * 
     * @param name
     *            human readable name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Parse a YAML stream and produce parsing events.
     * 
     * @param yaml
     *            YAML document(s)
     * @return parsed events
     */
    public Iterable<Event> parse(Reader yaml) {
        return loader.parse(yaml);
    }
}
