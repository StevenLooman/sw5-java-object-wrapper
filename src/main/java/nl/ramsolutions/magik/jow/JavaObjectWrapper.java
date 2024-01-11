package nl.ramsolutions.magik.jow;

import com.gesmallworld.magik.commons.interop.annotations.ExemplarInstance;
import com.gesmallworld.magik.commons.interop.annotations.MagikExemplar;
import com.gesmallworld.magik.commons.interop.annotations.MagikMethod;
import com.gesmallworld.magik.commons.interop.annotations.Name;
import com.gesmallworld.magik.interop.MagikInteropUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * Java Inspector.
 */
@SuppressWarnings({"checkstyle:RedundantModifier", "checkstyle:MagicNumber"})
@MagikExemplar(@Name(value = "java_object_wrapper"))
public final class JavaObjectWrapper {

    private final Object wrapped;

    /**
     * Constructor.
     * @param wrapped
     */
    private JavaObjectWrapper(final Object wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Get exemplar instance.
     * @return Exemplar instance.
     */
    @ExemplarInstance
    public static Object initialiseExemplar() {
        return new JavaObjectWrapper(null);
    }

    /**
     * Get the wrapped object.
     * @return Wrapped object.
     */
    @MagikMethod("int!wrapped_raw")
    public final Object getWrappedRaw() {
        return this.wrapped;
    }

    /**
     * Get the wrapped object, packed.
     * @return Packed object.
     */
    @SuppressWarnings("java:S1872")
    @MagikMethod("int!wrapped")
    public final Object getWrapped() {
        if (this.wrapped == null) {
            return null;
        }

        final Class<?> clazz = this.wrapped.getClass();
        if (clazz.getName().equals("java.lang.String")) {
            final String string = (String) this.wrapped;
            return MagikInteropUtils.toMagikString(string);
        }

        return this.wrapped;
    }

    /**
     * Magik constructor.
     * @param self Self.
     * @param wrapped Wrapped object.
     * @return New instance.
     */
    @MagikMethod("new()")
    public static Object magikNew(final Object self, final Object wrapped) {
        return new JavaObjectWrapper(wrapped);
    }

    /**
     * Invoke constructor and wrap result.
     * @param self Self.
     * @param magikClassName Class name.
     * @param magikArgs Arguments.
     * @return New instance.
     * @throws ReflectiveOperationException
     */
    @MagikMethod("new_constructed()")
    public static Object newConstructed(final Object self, final Object magikClassName, final Object... magikArgs)
            throws ReflectiveOperationException {
        final String className = MagikInteropUtils.fromMagikString(magikClassName);
        final Object[] args = JavaObjectWrapper.unpackObjects(magikArgs);

        final Class<?> clazz = Class.forName(className);
        final Class<?>[] argClazzes = Arrays.stream(args)
            .map(Object::getClass)
            .toArray(size -> new Class<?>[size]);
        final Constructor<?> constructor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, argClazzes);
        if (constructor == null) {
            final String argClazzesStr = Arrays.stream(argClazzes)
                .map(Class::getName)
                .collect(Collectors.joining(","));
            throw new IllegalStateException(
                "Unknown constructor: " + clazz.getName() + "(" + argClazzesStr + ")");
        }

        final Object result = constructor.newInstance(args);
        return new JavaObjectWrapper(result);
    }

    /**
     * Invoke method and wrap result.
     * @param magikMethodName Method name.
     * @param magikArgs Arguments.
     * @return Result, wrapped.
     * @throws ReflectiveOperationException
     */
    @MagikMethod("int!invoke_method()")
    public final Object invokeMethod(final Object magikMethodName, final Object... magikArgs)
            throws ReflectiveOperationException {
        final String methodName = MagikInteropUtils.fromMagikString(magikMethodName);
        final Object[] args = JavaObjectWrapper.unpackObjects(magikArgs);

        final Class<?> clazz = this.wrapped.getClass();
        final Class<?>[] argClazzes = Arrays.stream(args)
            .map(Object::getClass)
            .toArray(size -> new Class<?>[size]);
        final Method method = MethodUtils.getMatchingAccessibleMethod(clazz, methodName, argClazzes);
        if (method == null) {
            final String argClazzesStr = Arrays.stream(argClazzes)
                .map(Class::getName)
                .collect(Collectors.joining(","));
            throw new IllegalStateException(
                "Unknown method: " + clazz.getName() + "." + methodName + "(" + argClazzesStr + ")");
        }

        final Object result = method.invoke(this.wrapped, args);

        return new JavaObjectWrapper(result);
    }

    /**
     * Invoke static method and wrap result.
     * @param magikClassName Class name.
     * @param magikMethodName Method name.
     * @param magikArgs Arguments.
     * @return Result, wrapped.
     * @throws ReflectiveOperationException
     */
    @MagikMethod("int!invoke_static_method()")
    public final Object invokeStaticMethod(
            final Object magikClassName, final Object magikMethodName, final Object... magikArgs)
            throws ReflectiveOperationException {
        final String clazzName = MagikInteropUtils.fromMagikString(magikClassName);
        final String methodName = MagikInteropUtils.fromMagikString(magikMethodName);
        final Object[] args = JavaObjectWrapper.unpackObjects(magikArgs);

        final Class<?> clazz = Class.forName(clazzName);
        final Class<?>[] argClazzes = Arrays.stream(args)
            .map(Object::getClass)
            .toArray(size -> new Class<?>[size]);
        final Method method = MethodUtils.getMatchingAccessibleMethod(clazz, methodName, argClazzes);
        if (method == null) {
            final String argClazzesStr = Arrays.stream(argClazzes)
                .map(Class::getName)
                .collect(Collectors.joining(","));
            throw new IllegalStateException(
                "Unknown method: " + clazz.getName() + "." + methodName + "(" + argClazzesStr + ")");
        }

        final Object result = method.invoke(null, args);

        return new JavaObjectWrapper(result);
    }

    /**
     * Get a field value.
     * @param magikFieldName Field name.
     * @return Result, wrapped.
     * @throws ReflectiveOperationException
     */
    @SuppressWarnings("java:S3011")
    @MagikMethod("int!get_field_value()")
    public final Object getFieldValue(final Object magikFieldName)
            throws ReflectiveOperationException {
        final String fieldName = MagikInteropUtils.fromMagikString(magikFieldName);

        final Class<?> clazz = this.wrapped.getClass();
        if (clazz.isArray() && "length".equals(fieldName)) {
            final Object len = java.lang.reflect.Array.getLength(this.wrapped);
            return new JavaObjectWrapper(len);
        }

        final Field field = clazz.getDeclaredField(fieldName);
        final boolean staticField = Modifier.isStatic(field.getModifiers());
        final boolean fieldCanAccess = staticField
            ? field.canAccess(null)
            : field.canAccess(this.wrapped);
        try {
            field.setAccessible(true);
            final Object fieldValue = staticField
                ? field.get(null)
                : field.get(this.wrapped);
            return new JavaObjectWrapper(fieldValue);
        } finally {
            field.setAccessible(fieldCanAccess);
        }
    }

    /**
     * Get a static field value.
     * @param magikClassName Class name.
     * @param magikFieldName Field name.
     * @return Result, wrapped.
     * @throws ReflectiveOperationException
     */
    @SuppressWarnings("java:S3011")
    @MagikMethod("int!get_static_field_value()")
    public final Object getStaticFieldValue(final Object magikClassName, final Object magikFieldName)
            throws ReflectiveOperationException {
        final String clazzName = MagikInteropUtils.fromMagikString(magikClassName);
        final String fieldName = MagikInteropUtils.fromMagikString(magikFieldName);

        final Class<?> clazz = Class.forName(clazzName);
        final Field field = clazz.getDeclaredField(fieldName);
        final boolean staticField = Modifier.isStatic(field.getModifiers());
        if (!staticField) {
            throw new IllegalStateException("Field is not a static field");
        }
        final boolean fieldCanAccess = field.canAccess(null);
        try {
            field.setAccessible(true);
            final Object fieldValue = field.get(null);
            return new JavaObjectWrapper(fieldValue);
        } finally {
            field.setAccessible(fieldCanAccess);
        }
    }

    /**
     * Array indexer.
     * @param magikIndex Index in array.
     * @return Result, wrapped.
     */
    @MagikMethod("[]")
    public final Object getArrayItem(final Object magikIndex) {
        final int index = MagikInteropUtils.fromMagikInteger(magikIndex);

        final Class<?> clazz = this.wrapped.getClass();
        if (!clazz.isArray()) {
            throw new IllegalStateException("Wrapped object is not an array");
        }

        final Object result = java.lang.reflect.Array.get(this.wrapped, index);

        return new JavaObjectWrapper(result);
    }

    /**
     * Unpack objects. Wrapped objects are unwrapped, Magik types are converted to Java types.
     * @param magikArgs Wrapped/Magik objects to unpack.
     * @return Unpacked objects.
     */
    private static Object[] unpackObjects(final Object... magikObjects) {
        return Arrays.stream(magikObjects)
            .map(JavaObjectWrapper::unpackObject)
            .toArray(size -> new Object[size]);
    }

    /**
     * Unpack object. Wrapped objects are unwrapped, Magik types are converted to Java types.
     * @param magikObject Magik object to unpack.
     * @return Unpacked object.
     */
    @SuppressWarnings("java:S1872")
    private static Object unpackObject(final Object magikObject) {
        if (magikObject instanceof JavaObjectWrapper) {
            final JavaObjectWrapper wrapper = (JavaObjectWrapper) magikObject;
            return wrapper.wrapped;
        }

        final Class<?> clazz = magikObject.getClass();
        if (clazz.getName().equals("com.gesmallworld.magik.commons.runtime.objects.Char16Vector")) {
            return MagikInteropUtils.fromMagikString(magikObject);
        }

        return magikObject;
    }

    @Override
    public String toString() {
        return String.format(
            "%s@%s(%s)",
            this.getClass().getName(), Integer.toHexString(this.hashCode()),
            this.wrapped.toString());
    }

}
