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
package wang.bigbird.domain.framework.server.core.support.response;

/**
 * 定义几类常规的响应状态信息
 *
 * @author Bigbird
 */
public interface IBaseResponseStatus {

    ResponseStatus OTHER = new ResponseStatus(-1, "其他");
    ResponseStatus OK0 = new ResponseStatus(0, "成功");
    ResponseStatus SESSION_TIMEOUT = new ResponseStatus(101, "没有登录或会话超时");
    ResponseStatus OK = new ResponseStatus(200, "成功");
    ResponseStatus CREATED = new ResponseStatus(201, "用户新建或修改数据成功");
    ResponseStatus ACCEPTED = new ResponseStatus(202, "请求已经进入后台排队");
    ResponseStatus NO_CONTENT = new ResponseStatus(204, "删除数据成功");
    /**
     * 传递的数据不能被服务器合法解析返回400
     */
    ResponseStatus INVALID_REQUEST = new ResponseStatus(400, "用户发送请求有错误");
    /**
     * 未授权，如果没有进行身份验证返回401
     * 请求的资源需要先进行身份验证，验证身份后才可能有访问权限，可以防止非真实用户恶意访问
     */
    ResponseStatus UNAUTHORIZED = new ResponseStatus(401, "用户没有权限");
    /**
     * 禁止访问返回403，相比401，这里是进行了身份验证后发现用户的权限不足，再拒绝用户请求
     */
    ResponseStatus FORBIDDEN = new ResponseStatus(403, "用户没有得到授权");
    ResponseStatus NOT_FOUND = new ResponseStatus(404, "请求记录不存在");
    ResponseStatus METHOD_NOT_ALLOWED = new ResponseStatus(405, "请求方式不支持");
    ResponseStatus NOT_ACCEPTABLE = new ResponseStatus(406, "用户请求的格式不可得");
    ResponseStatus GONE = new ResponseStatus(410, "用户请求的资源被永久删除");
    ResponseStatus UNPROCESSABLE_ENTITY = new ResponseStatus(422, "创建对象发生验证错误");
    ResponseStatus INTERNAL_SERVER_ERROR = new ResponseStatus(500, "服务器发生错误");
    ResponseStatus SERVER_NOT_AVAILABLE = new ResponseStatus(503, "服务不可用");
    ResponseStatus SERVER_TIMEOUT = new ResponseStatus(504, "服务超时");
    ResponseStatus SERVER_NONSUPPORT = new ResponseStatus(505, "服务不支持");

    ResponseStatus INVALID_JWT_SIGNATURE = new ResponseStatus(10000, "无效的JWT签名");
    ResponseStatus EXPIRED_JWT = new ResponseStatus(10001, "JWT过期");
    ResponseStatus UNSUPPORTED_JWT = new ResponseStatus(10002, "不被本服务支持的JWT");
    ResponseStatus INVALID_JWT = new ResponseStatus(10003, "无效的JWT");
    ResponseStatus KICK_OFF_JWT = new ResponseStatus(10004, "被踢下线的JWT");
    ResponseStatus DISPOSED_JWT = new ResponseStatus(10005, "被注销的JWT");
    ResponseStatus BAD_REQUEST_DATA = new ResponseStatus(10006, "非法的请求数据");
    ResponseStatus CALLER_NOT_FOUND = new ResponseStatus(10007, "接入系统未注册");
    ResponseStatus IP_INVALID = new ResponseStatus(10008, "非法的访问IP");
    ResponseStatus LIMIT_LIST_VALIDATE = new ResponseStatus(10009, "未通过黑白名单检查");
    ResponseStatus REPLAY_ATTACK = new ResponseStatus(10010, "重复的请求");
    ResponseStatus SERVICE_NOT_FOUND = new ResponseStatus(10011, "对外暴露服务未注册");
    ResponseStatus SIGNATURE_INVALID = new ResponseStatus(10012, "签名错误");
    ResponseStatus BASE_INVALID = new ResponseStatus(10013, "Base认证失败");
    ResponseStatus HAS_LOGIN = new ResponseStatus(10014, "认证对象已登录");

    ResponseStatus USER_NOT_EXIST = new ResponseStatus(20000, "用户不存在");
    ResponseStatus USER_NOT_LOGIN = new ResponseStatus(20001, "用户未登录");
    ResponseStatus USERNAME_OR_PASSWORD_INCORRECT = new ResponseStatus(20002, "用户或者密码不正确");
    ResponseStatus USER_FORBIDDEN = new ResponseStatus(20003, "用户没有访问权限");
    ResponseStatus PHONE_NUM_REGISTERED = new ResponseStatus(20004, "手机号码已经被注册");
    ResponseStatus EMAIL_REGISTERED = new ResponseStatus(20005, "该邮箱已经被注册");
    ResponseStatus NICKNAME_EXISTED = new ResponseStatus(20006, "昵称已经存在");
    ResponseStatus ACCOUNT_EMPTY = new ResponseStatus(20007, "账号为空");
    ResponseStatus ACCOUNT_FORMAT_ERROR = new ResponseStatus(20008, "账号格式错误");
    ResponseStatus ACCOUNT_LOCKED = new ResponseStatus(20009, "账户被锁定");
    ResponseStatus ACCOUNT_FREEZE = new ResponseStatus(20010, "账户被冻结");
    ResponseStatus ACCOUNT_ANOMALIES = new ResponseStatus(20011, "账户异常");
    ResponseStatus PASSWORD_EMPTY = new ResponseStatus(20012, "密码为空");
    ResponseStatus PASSWORD_FORMAT_ERROR = new ResponseStatus(20013, "密码格式错误");
    ResponseStatus PASSWORD_WEAK = new ResponseStatus(20014, "不满足密码安全规则");
    ResponseStatus PASSWORD_ERROR = new ResponseStatus(20015, "密码错误");
    ResponseStatus IDENTIFY_CODE_ERROR = new ResponseStatus(20016, "验证码错误");
    ResponseStatus IDENTIFY_CODE_EXPIRE = new ResponseStatus(20017, "验证码已过期");
    ResponseStatus EMAIL_FORMAT_ERROR = new ResponseStatus(20018, "邮箱格式错误");
    ResponseStatus PHONE_NUM_FORMAT_ERROR = new ResponseStatus(20019, "手机号码格式错误");
    ResponseStatus EMAIL_NOT_EXIST_IN_WHITE = new ResponseStatus(20020, "邮箱不在白名单内");
    ResponseStatus PHONE_NUM_NOT_EXIST_IN_WHITE = new ResponseStatus(20021, "手机号不在白名单内");
    ResponseStatus NAME_EXISTED = new ResponseStatus(20022, "名称已经存在");
    ResponseStatus FORBIDDEN_TO_DELETE = new ResponseStatus(20023, "不满足删除业务条件");
    ResponseStatus DUPLICATE_BUSINESS_DATA = new ResponseStatus(20024, "存在重复业务数据");
    ResponseStatus FORBIDDEN_TO_UPDATE = new ResponseStatus(20025, "不满足更新业务条件");
    ResponseStatus FORBIDDEN_TO_OPERATION = new ResponseStatus(20026, "不满足操作业务条件");

    ResponseStatus UNSUPPORTED_ENCODING = new ResponseStatus(30000, "不支持的编码格式");
    ResponseStatus PARAMETERS_ANOMALIES = new ResponseStatus(30001, "参数异常");
    ResponseStatus STATE_ANOMALIES = new ResponseStatus(30002, "状态异常");
    ResponseStatus PARAMETERS_INVALID = new ResponseStatus(30003, "无效业务参数");
    ResponseStatus DUPLICATE_DATA_SUB = new ResponseStatus(30004, "数据重复提交");
    ResponseStatus DATA_NOT_EXIST = new ResponseStatus(30005, "不存在数据");
    ResponseStatus EXCEED_SCOPE = new ResponseStatus(30006, "数据量超过限制范围");
    ResponseStatus MULTIPART_FILE_SIZE_EXCEEDED = new ResponseStatus(30007, "上传文件超过限制大小");
    ResponseStatus MULTIPART_FILE_SUFFIX_EMPTY = new ResponseStatus(30008, "上传文件格式非法");
    ResponseStatus MULTIPART_FILE_NOT_IN_WHITELIST = new ResponseStatus(30009, "上传文件格式不在白名单内");
    ResponseStatus MULTIPART_FILE_DIR_NOT_EXIST = new ResponseStatus(30010, "上传文件目录不存在");
    ResponseStatus MULTIPART_FILE_PATH_INVALID = new ResponseStatus(30011, "非法的文件上传路径");
    ResponseStatus MULTIPART_FILE_CONTENT_CONTAIN_JS = new ResponseStatus(30012, "上传文件内容包含脚本");
    ResponseStatus MOVE_FILE_ERROR = new ResponseStatus(30013, "移动上传文件发生错误");
    ResponseStatus DELETE_FILE_ERROR = new ResponseStatus(30014, "删除上传文件发生错误");
    ResponseStatus FILE_SUFFIX_ERROR = new ResponseStatus(30015, "文件类型错误");
    ResponseStatus FILE_FORMAT_ERROR = new ResponseStatus(30016, "文件格式错误");
    ResponseStatus DOWNLOAD_FILE_ERROR = new ResponseStatus(30017, "下载文件发生错误");

    ResponseStatus EXCEED_MAX_TIMES = new ResponseStatus(40000, "超过允许的最大次数");
    ResponseStatus EXCEED_MAX_TIMES_ONE_DAY = new ResponseStatus(40001, "超过1天内允许的最大次数");
    ResponseStatus EXCEED_MAX_TIMES_ONE_HOUR = new ResponseStatus(40002, "超过1小时内允许的最大次数");
    ResponseStatus EXCEED_MAX_TIMES_ONE_MINUTE = new ResponseStatus(40003, "超过1分钟内允许的最大次数");
    ResponseStatus EXCEED_MAX_TIMES_SAME_DAY = new ResponseStatus(40004, "超过当天内允许的最大次数");

    ResponseStatus CUT_IMAGE_ERROR = new ResponseStatus(50000, "图像剪裁发生错误");
    ResponseStatus IMPORT_DATA_ERROR = new ResponseStatus(50001, "数据导入发生错误");

    ResponseStatus THIRD_SYSTEM_BUSINESS_FAILED = new ResponseStatus(60000, "第三方系统业务失败");
    ResponseStatus THIRD_SYSTEM_BUSINESS_REFUSE = new ResponseStatus(60001, "第三方系统拒绝业务");

    ResponseStatus NOT_DEFINED_ERROR = new ResponseStatus(99999, "系统错误");

}
