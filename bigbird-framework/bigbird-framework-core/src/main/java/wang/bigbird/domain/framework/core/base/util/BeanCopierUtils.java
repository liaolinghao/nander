/*
 * Copyright (c) 2026 廖凌浩 / 鸟域
 *
 * Licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package wang.bigbird.domain.framework.core.base.util;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 对象之间的同名属性拷贝，该拷贝器性能最高，据网络实验表明：
 * cglib > spring > hutool，性能差距：
 * 本机4c16g macbookpro，一亿条数据循环，
 * cglib300ms，spring10s，hutool120s
 * <p>
 * 该类不支持不同对象中的List对象集合类型的属性的拷贝，
 * 本质上对List是拷贝引用，即对List是弱拷贝，而不是深拷贝，
 * 如果只是做对象拷贝，然后直接抛出这个对象给前台使用是没有问题的，
 * 但是如果这个通过拷贝得到的对象要在代码中进行业务流转，
 * 则会报java.lang.ClassCastException 类强转异常
 * 因此，如果存在对象之间list属性的拷贝，建议采用BeanMapperUtils工具类
 *
 * @author Bigbird
 */
@Slf4j
public class BeanCopierUtils {

    /**
     * 创建copier的开销很大，所以创建一次后，需要缓存起来，下次需要直接取出来用
     */
    private static final Map<String, BeanCopier> BEAN_COPIER_HASH_MAP = new HashMap<>();
    private static final Map<String, BeanCopier> CONVERTER_BEAN_COPIER_MAP = new HashMap<>();

    /**
     * 拷贝所有属性，只会拷贝同名同类型的字段
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 拷贝属性值后的目标对象
     */
    public static <X> X copyProperties(Object source, X target) {
        if (source == null) {
            return null;
        }
        String beanKey = generateKey(source.getClass(), target.getClass());
        BeanCopier copier = null;
        if (!BEAN_COPIER_HASH_MAP.containsKey(beanKey)) {
            copier = create(source.getClass(), target.getClass(), false);
            BEAN_COPIER_HASH_MAP.put(beanKey, copier);
        } else {
            copier = BEAN_COPIER_HASH_MAP.get(beanKey);
        }
        copier.copy(source, target, null);
        return target;
    }

    /**
     * 只拷贝非空属性，对于同名不同类型的字段会执行转换，如果转换失败会抛出异常
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 拷贝属性值后的目标对象
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <X> X copyNotNullProperties(Object source, X target) {
        if (source == null) {
            return null;
        }
        BeanCopier copier = getConverterBeanCopier(source, target);
        copier.copy(source, target, new NotNullConverter(target));
        return target;
    }

    /**
     * 拷贝所有属性，只会拷贝同名同类型的字段，支持包装类型转换的复制方法，如果包装类型为空，那么对于基本数值类型的变量，会用默认值
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 拷贝属性值后的目标对象
     */
    public static <X> X copyPropertiesSupportWrapperType(Object source, X target) {
        if (source == null) {
            return null;
        }
        BeanCopier copier = getConverterBeanCopier(source, target);
        copier.copy(source, target, new Converter() {
            @Override
            public Object convert(Object value,
                                  @SuppressWarnings("rawtypes") Class aClass, Object o1) {
                return value;
            }
        });
        return target;
    }

    /**
     * 获取从源对象到目标对象的对象拷贝器
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 对象拷贝器
     */
    private static BeanCopier getConverterBeanCopier(Object source,
                                                     Object target) {
        String beanKey = generateKey(source.getClass(), target.getClass());
        BeanCopier copier = null;
        if (!CONVERTER_BEAN_COPIER_MAP.containsKey(beanKey)) {
            copier = create(source.getClass(), target.getClass(), true);
            CONVERTER_BEAN_COPIER_MAP.put(beanKey, copier);
        } else {
            copier = CONVERTER_BEAN_COPIER_MAP.get(beanKey);
        }
        return copier;
    }

    /**
     * 构造对象拷贝器键值
     *
     * @param source 源对象类
     * @param target 目标对象类
     * @return 对象拷贝器键值
     */
    private static String generateKey(Class<?> source, Class<?> target) {
        return source.toString() + "-" + target.toString();
    }

    /**
     * 创建从源对象到目标对象的对象拷贝器
     *
     * @param source       源对象
     * @param target       目标对象
     * @param useConverter 是否对同名不同类型的属性执行转换
     * @return 对象拷贝器
     */
    private static BeanCopier create(Class<? extends Object> source,
                                     Class<? extends Object> target, boolean useConverter) {
        GldGenerator gen = new GldGenerator();
        gen.setSource(source);
        gen.setTarget(target);
        gen.setUseConverter(useConverter);
        return gen.create();
    }

    public static class GldGenerator extends AbstractClassGenerator<Object> {
        private static final Type BEAN_COPIER = TypeUtils
                .parseType("net.sf.cglib.beans.BeanCopier");
        private static final Type CONVERTER = TypeUtils
                .parseType("net.sf.cglib.core.Converter");
        private static final Signature COPY;
        private static final Signature CONVERT;
        private static final BeanCopierKey KEY_FACTORY;
        private static final Source SOURCE;
        private Class<? extends Object> source;
        private Class<? extends Object> target;
        private boolean useConverter;

        public GldGenerator() {
            super(SOURCE);
        }

        public void setSource(Class<? extends Object> source) {
            if (!Modifier.isPublic(source.getModifiers())) {
                this.setNamePrefix(source.getName());
            }
            this.source = source;
        }

        public void setTarget(Class<? extends Object> target) {
            if (!Modifier.isPublic(target.getModifiers())) {
                this.setNamePrefix(target.getName());
            }
            this.target = target;
        }

        public void setUseConverter(boolean useConverter) {
            this.useConverter = useConverter;
        }

        @Override
        protected ClassLoader getDefaultClassLoader() {
            return this.source.getClassLoader();
        }

        public BeanCopier create() {
            Object key = KEY_FACTORY.newInstance(this.source.getName(),
                    this.target.getName(), this.useConverter);
            return (BeanCopier) super.create(key);
        }

        @Override
        public void generateClass(ClassVisitor v) {
            Type sourceType = Type.getType(this.source);
            Type targetType = Type.getType(this.target);
            ClassEmitter ce = new ClassEmitter(v);
            ce.begin_class(46, 1, this.getClassName(), BEAN_COPIER,
                    (Type[]) null, "<generated>");
            EmitUtils.null_constructor(ce);
            CodeEmitter e = ce.begin_method(1, COPY, (Type[]) null);
            PropertyDescriptor[] getters = ReflectUtils
                    .getBeanGetters(this.source);
            PropertyDescriptor[] setters = ReflectUtils
                    .getBeanGetters(this.target);
            Map<String, PropertyDescriptor> names = new HashMap<>(CollectionUtils.initialMapCapacity(getters.length));
            for (int i = 0; i < getters.length; ++i) {
                names.put(getters[i].getName(), getters[i]);
            }
            Local targetLocal = e.make_local();
            Local sourceLocal = e.make_local();
            if (this.useConverter) {
                e.load_arg(1);
                e.checkcast(targetType);
                e.store_local(targetLocal);
                e.load_arg(0);
                e.checkcast(sourceType);
                e.store_local(sourceLocal);
            } else {
                e.load_arg(1);
                e.checkcast(targetType);
                e.load_arg(0);
                e.checkcast(sourceType);
            }
            for (int i = 0; i < setters.length; ++i) {
                PropertyDescriptor setter = setters[i];
                PropertyDescriptor getter = (PropertyDescriptor) names
                        .get(setter.getName());
                if (getter != null && setter.getWriteMethod() != null) {
                    MethodInfo read = ReflectUtils.getMethodInfo(getter
                            .getReadMethod());
                    MethodInfo write = ReflectUtils.getMethodInfo(setter
                            .getWriteMethod());
                    if (this.useConverter) {
                        Type setterType = write.getSignature()
                                .getArgumentTypes()[0];
                        e.load_local(targetLocal);
                        e.load_arg(2);
                        e.load_local(sourceLocal);
                        e.invoke(read);
                        e.box(read.getSignature().getReturnType());
                        EmitUtils.load_class(e, setterType);
                        e.push(write.getSignature().getName());
                        e.invoke_interface(CONVERTER, CONVERT);
                        e.unbox_or_zero(setterType);
                        e.invoke(write);
                    } else if (compatible(getter, setter)) {
                        e.dup2();
                        e.invoke(read);
                        e.invoke(write);
                    }
                }
            }
            e.return_value();
            e.end_method();
            ce.end_class();
        }

        private static boolean compatible(PropertyDescriptor getter,
                                          PropertyDescriptor setter) {
            return setter.getPropertyType().isAssignableFrom(
                    getter.getPropertyType());
        }

        @Override
        protected Object firstInstance(@SuppressWarnings("rawtypes") Class type) {
            return ReflectUtils.newInstance(type);
        }

        @Override
        protected Object nextInstance(Object instance) {
            return instance;
        }

        static {
            COPY = new Signature("copy", Type.VOID_TYPE, new Type[]{
                    Constants.TYPE_OBJECT, Constants.TYPE_OBJECT, CONVERTER});
            CONVERT = TypeUtils
                    .parseSignature("Object convert(Object, Class, Object)");
            KEY_FACTORY = (BeanCopierKey) KeyFactory
                    .create(BeanCopierKey.class);
            SOURCE = new Source(BeanCopier.class.getName());
        }

        interface BeanCopierKey {
            /**
             * Creates a new instance of the bean copier.
             *
             * @param var1 the source class name
             * @param var2 the target class name
             * @param var3 whether to use converter
             * @return a new instance of the bean copier
             * @throws IllegalArgumentException if the class names are invalid or classes cannot be found
             */
            Object newInstance(String var1, String var2, boolean var3);
        }
    }

    public static class NotNullConverter<X> implements Converter {
        private X target;

        public NotNullConverter(X target) {
            this.target = target;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Object convert(Object value, Class targetValueClass,
                              Object context) {
            if (value == null) {
                String methodName = (String) context;
                try {
                    Method getter = this.target.getClass().getMethod(
                            methodName.replaceFirst("set", "get"),
                            (Class[]) null);
                    value = getter.invoke(this.target, (Object[]) null);
                } catch (Exception e) {
                    log.error("Convert:", e);
                }
            }
            return value;
        }
    }

}
