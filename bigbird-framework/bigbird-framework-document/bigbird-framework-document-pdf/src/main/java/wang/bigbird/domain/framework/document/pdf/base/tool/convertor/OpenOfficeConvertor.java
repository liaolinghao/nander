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
package wang.bigbird.domain.framework.document.pdf.base.tool.convertor;

import lombok.extern.slf4j.Slf4j;
import org.jodconverter.OfficeDocumentConverter;
import org.jodconverter.office.DefaultOfficeManagerBuilder;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import wang.bigbird.domain.framework.core.base.tool.Assert;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 基于OpenOffice的PDF文档转换器，支持跨平台，转换原理为：
 * <p>
 * 机器上需要安装OpenOffice，利用OpenOffice的进程执行文档格式转换，一个进程同一时刻只能转换一个文档。
 * 为此，多个文档在短时间进行转换时，需要排队进行。但是，OpenOffice支持多端口，一个端口对应一个进程，
 * 可采用多进程的方式来加速处理。
 * <p>
 * 经过实践，目前依靠OpenOffice进行PDF文档转换，对于excel支持的效果还比较好，
 * 除了跨行合并的单元格文本不是水平居中对齐，其他效果都还比较理想。
 * 另外经过对比，转换格式在效果上弱于JACOB（仅支持windows系统，依赖微软OFFICE），
 * 在转换速度上，小文件openoffice转换更快，大文件libreoffice转换更快。
 *
 * @author Bigbird
 */
@Slf4j
public class OpenOfficeConvertor extends OfficeConvertor {

    /**
     * 用线程安全的queue管理运行的端口号
     */
    private static BlockingQueue<OfficeManager> officeManagerQueue;

    private OpenOfficeConvertor() {
    }

    /**
     * 开启OpenOffice服务
     *
     * @param officeHome openOffice安装目录，
     *                   windows，如：C:\Program Files (x86)\OpenOffice 4
     *                   Linux，如：/opt/OpenOffice
     *                   Mac，如：/Application/openOfficeSoft
     * @param ports      OpenOffice服务监听端口
     * @throws OfficeException      启动过程中，可能抛出该异常
     * @throws InterruptedException 启动过程中，可能抛出该异常
     */
    private static void startOfficeService(String officeHome, int[] ports) throws OfficeException, InterruptedException {
        if (officeManagerQueue == null) {
            synchronized (OpenOfficeConvertor.class) {
                if (officeManagerQueue == null) {
                    officeManagerQueue = new LinkedBlockingQueue<>(ports.length);
                    DefaultOfficeManagerBuilder builder = new DefaultOfficeManagerBuilder();
                    builder.setOfficeHome(officeHome);
                    for (int port : ports) {
                        builder.setPortNumbers(port);
                        OfficeManager officeManager = builder.build();
                        officeManager.start();
                        officeManagerQueue.put(officeManager);
                    }
                }
            }
        }
    }

    /**
     * 关闭OpenOffice服务
     */
    public static synchronized void stopOfficeService() {
        if (officeManagerQueue != null) {
            for (OfficeManager officeManager : officeManagerQueue) {
                try {
                    officeManager.stop();
                } catch (OfficeException e) {
                    log.error("StopOfficeManager:", e);
                }
            }
        }
    }

    /**
     * 利用OpenOffice将office文档转换为pdf文档，其过程为：
     *
     * @param officeHome openOffice安装目录，
     *                   windows，如：C:\Program Files (x86)\OpenOffice 4
     *                   Linux，如：/opt/OpenOffice
     *                   Mac，如：/Application/openOfficeSoft
     * @param ports      OpenOffice服务监听端口
     * @param officeFile 源office文档
     * @param destPdf    目标PDF文档
     * @throws OfficeException      转换过程中，可能抛出该异常
     * @throws InterruptedException 转换过程中，可能抛出该异常
     */
    public static void office2Pdf(String officeHome, int[] ports, File officeFile, File destPdf) throws OfficeException, InterruptedException {
        Assert.notNull(officeHome,
                "The parameter officeHome is null.");
        Assert.isTrue(new File(officeHome).exists(), "The parameter officeHome is not existed.");
        Assert.notNull(ports,
                "The parameter ports is null.");
        Assert.isTrue(ports.length > 0, "The parameter ports is empty.");
        verifyConvertFiles(officeFile, destPdf);
        if (!destPdf.getParentFile().exists()) {
            destPdf.getParentFile().mkdirs();
        }
        startOfficeService(officeHome, ports);
        OfficeManager officeManager = null;
        try {
            officeManager = officeManagerQueue.take();
            OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
            converter.convert(officeFile, destPdf);
        } finally {
            if (officeManager != null) {
                officeManagerQueue.put(officeManager);
            }
        }
    }

}
