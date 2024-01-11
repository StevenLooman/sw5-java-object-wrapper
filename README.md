# sw5-java-object-wrapper

Smallworld5 Java Object Wrapper. A wrapper which makes it possible to access Java classes from Magik using a Magik-wrapper.

## Loading

The product can be loaded as follows:

```magik
_block
    _local product_dir << "..."  # <-- Path to this product.
    smallworld_product.add_product(product_dir)
    sw_module_manager.load_module(:sw5_java_object_wrapper)
_endblock
$
```

Furthermore, if you are running Java 17 or up, you most likely need to add JVM startup arguments like `--add-opens java.base/java.util=ALL-UNNAMED` to your `environment`, in the environment variable `SW_LAUNCH_JAVA_ARGS`. This is to allow access from the `java.base` module to the `java.util` module.

## Usage

This allows one to access Java classes from Magik, such as using `java.util.ArrayList`:

```magik
Magik> list << java_object_wrapper.new_constructed("java.util.ArrayList")
java_object_wrapper(an array_list(obj#2029604328)) 
Magik> item1 << java_object_wrapper.new_constructed("java.lang.String", "string1")
java_object_wrapper(string1) 
Magik> list.add(item1)
java_object_wrapper(True) 
Magik> item2 << java_object_wrapper.new_constructed("java.lang.String", "string2")
java_object_wrapper(string2) 
Magik> list.add(item2)
java_object_wrapper(True) 
Magik> list.size()
java_object_wrapper(2) 
Magik> list.size().int!wrapped
2
Magik> list.to_string()
java_object_wrapper([string1, string22]) 
$
```

Note the `int!wrapped` method to get access to the 'raw' object, although the object is converted if needed. To get access to the raw object itself, use the `int!wrapped_raw` method.

Another example, which requests the system property `java.version`:

```magik
Magik> _block
    # Java: java.lang.System.getProperty("java.version")
    _local jvm_version << java_object_wrapper.int!invoke_static_method("java.lang.System", "getProperty", "java.version")
    write("JVM version: ", jvm_version.int!wrapped)
_endblock
$
```

It also allows you to access existing Java-interop objects exposed to Magik, and access their internals. For example, the `binary_operator` can now interrogated about the registered binary operators. For example:

```magik
Magik> java_binary_operator << java_object_wrapper.new(binary_operator).int!get_field_value("known_operators")
java_object_wrapper(a HashMap(obj#1460350454))
Magik> 
Magik> 
```

## Building

Before building, two jars from your Smallworld installation need to be installed into your local maven repository:

- `com.gesmallworld.magik.commons`
- `com.gesmallworld.magik.interop`

This can be done with the following commands. Change the paths and versions based on your Smallworld installation.

```sh
$ mvn install:install-file \
   -Dfile=/opt/Smallworld/core/libs/com.gesmallworld.magik.commons-5.3.0.0-490.jar \
   -DgroupId=com.gesmallworld \
   -DartifactId=magik.commons \
   -Dversion=5.3.0.0-490 \
   -Dpackaging=jar \
   -DgeneratePom=true
...

$ mvn install:install-file \
   -Dfile=/opt/Smallworld/core/libs/com.gesmallworld.magik.interop-5.3.0.0-490.jar \
   -DgroupId=com.gesmallworld \
   -DartifactId=magik.interop \
   -Dversion=5.3.0.0-490 \
   -Dpackaging=jar \
   -DgeneratePom=true
...
```

This project is built using [Maven](https://maven.apache.org/). Building should be as simple as:

```sh
$ mvn clean package
...
```

## License

This product is licensed under the GPLv3 license. If you require a different licensen, please do contact me.
