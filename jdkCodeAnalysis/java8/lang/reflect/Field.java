package java.lang.reflect;

import sun.reflect.CallerSensitive;
import sun.reflect.FieldAccessor;
import sun.reflect.Reflection;
import sun.reflect.generics.repository.FieldRepository;
import sun.reflect.generics.factory.CoreReflectionFactory;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.scope.ClassScope;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import sun.reflect.annotation.AnnotationParser;
import sun.reflect.annotation.AnnotationSupport;
import sun.reflect.annotation.TypeAnnotation;
import sun.reflect.annotation.TypeAnnotationParser;

public final class Field extends AccessibleObject implements Member {

    private Class<?>            clazz;
    private int                 slot;
    // This is guaranteed to be interned by the VM in the 1.4
    // reflection implementation
    private String              name;
    private Class<?>            type;
    private int                 modifiers;
    // Generics and annotations support
    private transient String    signature;
    // generic info repository; lazily initialized
    private transient FieldRepository genericInfo;
    private byte[]              annotations;
    // Cached field accessor created without override
    private FieldAccessor fieldAccessor;
    // Cached field accessor created with override
    private FieldAccessor overrideFieldAccessor;
    // For sharing of FieldAccessors. This branching structure is
    // currently only two levels deep (i.e., one root Field and
    // potentially many Field objects pointing to it.)
    //
    // If this branching structure would ever contain cycles, deadlocks can
    // occur in annotation code.
    private Field               root;

    // Generics infrastructure

    private String getGenericSignature() {return signature;}

    // Accessor for factory
    private GenericsFactory getFactory() {
        Class<?> c = getDeclaringClass();
        // create scope and factory
        return CoreReflectionFactory.make(c, ClassScope.make(c));
    }

    // Accessor for generic info repository
    private FieldRepository getGenericInfo() {
        // lazily initialize repository if necessary
        if (genericInfo == null) {
            // create and cache generic info repository
            genericInfo = FieldRepository.make(getGenericSignature(),
                                               getFactory());
        }
        return genericInfo; //return cached repository
    }


    Field(Class<?> declaringClass,
          String name,
          Class<?> type,
          int modifiers,
          int slot,
          String signature,
          byte[] annotations)
    {
        this.clazz = declaringClass;
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
        this.slot = slot;
        this.signature = signature;
        this.annotations = annotations;
    }

    Field copy() {
        // This routine enables sharing of FieldAccessor objects
        // among Field objects which refer to the same underlying
        // method in the VM. (All of this contortion is only necessary
        // because of the "accessibility" bit in AccessibleObject,
        // which implicitly requires that new java.lang.reflect
        // objects be fabricated for each reflective call on Class
        // objects.)
        if (this.root != null)
            throw new IllegalArgumentException("Can not copy a non-root Field");

        Field res = new Field(clazz, name, type, modifiers, slot, signature, annotations);
        res.root = this;
        // Might as well eagerly propagate this if already present
        res.fieldAccessor = fieldAccessor;
        res.overrideFieldAccessor = overrideFieldAccessor;

        return res;
    }

    public Class<?> getDeclaringClass() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean isEnumConstant() {
        return (getModifiers() & Modifier.ENUM) != 0;
    }

    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    public Class<?> getType() {
        return type;
    }

    public Type getGenericType() {
        if (getGenericSignature() != null)
            return getGenericInfo().getGenericType();
        else
            return getType();
    }


    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Field) {
            Field other = (Field)obj;
            return (getDeclaringClass() == other.getDeclaringClass())
                && (getName() == other.getName())
                && (getType() == other.getType());
        }
        return false;
    }

    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    public String toString() {
        int mod = getModifiers();
        return (((mod == 0) ? "" : (Modifier.toString(mod) + " "))
            + getType().getTypeName() + " "
            + getDeclaringClass().getTypeName() + "."
            + getName());
    }

    public String toGenericString() {
        int mod = getModifiers();
        Type fieldType = getGenericType();
        return (((mod == 0) ? "" : (Modifier.toString(mod) + " "))
            + fieldType.getTypeName() + " "
            + getDeclaringClass().getTypeName() + "."
            + getName());
    }

    @CallerSensitive
    public Object get(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).get(obj);
    }

    @CallerSensitive
    public boolean getBoolean(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getBoolean(obj);
    }

    @CallerSensitive
    public byte getByte(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getByte(obj);
    }

    @CallerSensitive
    public char getChar(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getChar(obj);
    }

    @CallerSensitive
    public short getShort(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getShort(obj);
    }

    @CallerSensitive
    public int getInt(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getInt(obj);
    }

    @CallerSensitive
    public long getLong(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getLong(obj);
    }

    @CallerSensitive
    public float getFloat(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getFloat(obj);
    }

    @CallerSensitive
    public double getDouble(Object obj)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        return getFieldAccessor(obj).getDouble(obj);
    }

    @CallerSensitive
    public void set(Object obj, Object value)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).set(obj, value);
    }

    @CallerSensitive
    public void setBoolean(Object obj, boolean z)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setBoolean(obj, z);
    }

    @CallerSensitive
    public void setByte(Object obj, byte b)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setByte(obj, b);
    }

    @CallerSensitive
    public void setChar(Object obj, char c)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setChar(obj, c);
    }

    @CallerSensitive
    public void setShort(Object obj, short s)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setShort(obj, s);
    }

    @CallerSensitive
    public void setInt(Object obj, int i)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setInt(obj, i);
    }

    @CallerSensitive
    public void setLong(Object obj, long l)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setLong(obj, l);
    }

    @CallerSensitive
    public void setFloat(Object obj, float f)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setFloat(obj, f);
    }

    @CallerSensitive
    public void setDouble(Object obj, double d)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).setDouble(obj, d);
    }

    // security check is done before calling this method
    private FieldAccessor getFieldAccessor(Object obj)
        throws IllegalAccessException
    {
        boolean ov = override;
        FieldAccessor a = (ov) ? overrideFieldAccessor : fieldAccessor;
        return (a != null) ? a : acquireFieldAccessor(ov);
    }

    // NOTE that there is no synchronization used here. It is correct
    // (though not efficient) to generate more than one FieldAccessor
    // for a given Field. However, avoiding synchronization will
    // probably make the implementation more scalable.
    private FieldAccessor acquireFieldAccessor(boolean overrideFinalCheck) {
        // First check to see if one has been created yet, and take it
        // if so
        FieldAccessor tmp = null;
        if (root != null) tmp = root.getFieldAccessor(overrideFinalCheck);
        if (tmp != null) {
            if (overrideFinalCheck)
                overrideFieldAccessor = tmp;
            else
                fieldAccessor = tmp;
        } else {
            // Otherwise fabricate one and propagate it up to the root
            tmp = reflectionFactory.newFieldAccessor(this, overrideFinalCheck);
            setFieldAccessor(tmp, overrideFinalCheck);
        }

        return tmp;
    }

    // Returns FieldAccessor for this Field object, not looking up
    // the chain to the root
    private FieldAccessor getFieldAccessor(boolean overrideFinalCheck) {
        return (overrideFinalCheck)? overrideFieldAccessor : fieldAccessor;
    }

    // Sets the FieldAccessor for this Field object and
    // (recursively) its root
    private void setFieldAccessor(FieldAccessor accessor, boolean overrideFinalCheck) {
        if (overrideFinalCheck)
            overrideFieldAccessor = accessor;
        else
            fieldAccessor = accessor;
        // Propagate up
        if (root != null) {
            root.setFieldAccessor(accessor, overrideFinalCheck);
        }
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return annotationClass.cast(declaredAnnotations().get(annotationClass));
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return AnnotationSupport.getDirectlyAndIndirectlyPresent(declaredAnnotations(), annotationClass);
    }

    public Annotation[] getDeclaredAnnotations()  {
        return AnnotationParser.toArray(declaredAnnotations());
    }

    private transient volatile Map<Class<? extends Annotation>, Annotation> declaredAnnotations;

    private Map<Class<? extends Annotation>, Annotation> declaredAnnotations() {
        Map<Class<? extends Annotation>, Annotation> declAnnos;
        if ((declAnnos = declaredAnnotations) == null) {
            synchronized (this) {
                if ((declAnnos = declaredAnnotations) == null) {
                    Field root = this.root;
                    if (root != null) {
                        declAnnos = root.declaredAnnotations();
                    } else {
                        declAnnos = AnnotationParser.parseAnnotations(
                                annotations,
                                sun.misc.SharedSecrets.getJavaLangAccess()
                                        .getConstantPool(getDeclaringClass()),
                                getDeclaringClass());
                    }
                    declaredAnnotations = declAnnos;
                }
            }
        }
        return declAnnos;
    }

    private native byte[] getTypeAnnotationBytes0();

    public AnnotatedType getAnnotatedType() {
        return TypeAnnotationParser.buildAnnotatedType(getTypeAnnotationBytes0(),
                                                       sun.misc.SharedSecrets.getJavaLangAccess().
                                                           getConstantPool(getDeclaringClass()),
                                                       this,
                                                       getDeclaringClass(),
                                                       getGenericType(),
                                                       TypeAnnotation.TypeAnnotationTarget.FIELD);
}
}
