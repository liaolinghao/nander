package wang.bigbird.domain.framework.server.rpc.core.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * kryo序列化注解
 * 被该注解标记的类将在采用kryo序列化时进行注册
 *
 * @author Bigbird
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface KryoSerializable {

}
