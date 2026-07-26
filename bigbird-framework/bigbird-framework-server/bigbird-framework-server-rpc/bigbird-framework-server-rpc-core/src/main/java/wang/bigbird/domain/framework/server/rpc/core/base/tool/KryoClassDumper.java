package wang.bigbird.domain.framework.server.rpc.core.base.tool;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.util.DefaultClassResolver;
import com.esotericsoftware.kryo.util.IntMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.serialize.kryo.utils.KryoUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Kryo注册类打印器
 *
 * @author Bigbird
 */
@Slf4j
public class KryoClassDumper {

    /**
     * 打印Kryo注册类
     *
     * @param missId 缺失类ID
     */
    public static void dumpRegisteredClasses(int missId) {
        try {
            Kryo kryo = KryoUtils.get();
            DefaultClassResolver resolver = (DefaultClassResolver) kryo.getClassResolver();
            // Kryo4.0.2 内部存储：idToRegistration 数组
            Field idToRegField = DefaultClassResolver.class.getDeclaredField("idToRegistration");
            idToRegField.setAccessible(true);
            IntMap intMap = (IntMap) idToRegField.get(resolver);
            List<Registration> regList = new ArrayList<>();
            // 遍历IntMap取出所有Registration
            IntMap.Entries entries = intMap.entries();
            while (entries.hasNext()) {
                Registration reg = (Registration) entries.next().value;
                regList.add(reg);
            }
            // 按ID升序打印
            regList.sort(Comparator.comparingInt(Registration::getId));
            log.info("===== Kryo 注册类列表 总数:{} =====", regList.size());
            for (Registration reg : regList) {
                int id = reg.getId();
                Class<?> clazz = reg.getType();
                if (id == missId) {
                    log.error("Kryo-ID:{} | Class:{}", id, clazz.getName());
                } else {
                    log.info("Kryo-ID:{} | Class:{}", id, clazz.getName());
                }
            }
        } catch (Exception e) {
            log.error("DumpRegisteredClasses:{}", e.getMessage(), e);
        }
    }

}
