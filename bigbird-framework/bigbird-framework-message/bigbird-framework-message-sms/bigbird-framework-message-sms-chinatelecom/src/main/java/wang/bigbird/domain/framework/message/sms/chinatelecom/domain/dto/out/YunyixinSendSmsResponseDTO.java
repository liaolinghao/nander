package wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 云翼信平台短信发送响应体
 *
 * @author Bigbird
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YunyixinSendSmsResponseDTO implements Serializable {

    /**
     * 成功响应标识
     */
    public static final String OK = "0000";

    /**
     * 事务号，与请求事务号对应，后续匹配回执用
     */
    private String transactionID;
    /**
     * 接口调用结果码（0000表示成功）
     */
    private String retCode;
    /**
     * 接口调用说明
     */
    private String retMsg;

}
