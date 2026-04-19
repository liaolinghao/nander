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
package wang.bigbird.domain.framework.server.web.captcha.service.base;

/**
 * 邮件服务
 *
 * @author Bigbird
 */
public interface IEmailService {

    /**
     * 调用邮箱服务器发送邮箱验证码
     *
     * @param email    接收电子邮箱
     * @param number   验证码
     * @param template 内容模板
     */
    void send(String email, String number, String template) throws InterruptedException;
}
