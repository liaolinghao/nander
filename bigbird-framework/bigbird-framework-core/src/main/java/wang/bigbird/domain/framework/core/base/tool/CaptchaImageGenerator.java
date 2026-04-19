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
package wang.bigbird.domain.framework.core.base.tool;

import wang.bigbird.domain.framework.core.base.util.ColorUtils;
import wang.bigbird.domain.framework.core.base.util.DataUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 图像验证码生成器
 *
 * @author Bigbird
 */
public class CaptchaImageGenerator {

    private static Random random = new Random();

    private static final String[] FONTS = {"Georgia", "Verdana", "Arial",
            "Tahoma", "Time News Roman", "Courier New", "Arial Black",
            "Quantzite"};

    private static final int[] STYLES = {Font.PLAIN, Font.ITALIC};

    /**
     * 图片验证码最小字符数
     */
    private static final int MIN_CODE_LENGTH = 4;
    /**
     * 图片验证码最大字符数
     */
    private static final int MAX_CODE_LENGTH = 8;

    /**
     * 包含验证码的图像
     */
    private BufferedImage buffImg;
    /**
     * 验证码
     */
    private String code;

    private Graphics2D graphics;

    /**
     * x方向一个周期占多少像素
     */
    private int sinPeriodPoint = 200;
    /**
     * 正弦曲线粗细
     */
    private int sinMaxThick = 5;
    /**
     * 画圈数量
     */
    private int ovalCount = 3;
    /**
     * 干扰线数
     */
    private int lineCount = 10;
    /**
     * 字符倾斜最大角度
     */
    private int codeMaxRadian = 30;
    /**
     * 阴影宽度
     */
    private int codeShadow = 1;
    /**
     * 正弦曲线绘制步长
     */
    private int sinCurveStep = 2;

    /**
     * 生成验证码
     *
     * @param length 长度应该限制在4到8位之间
     */
    public void createCode(int length, int width, int height) {
        if (length < MIN_CODE_LENGTH) {
            length = MIN_CODE_LENGTH;
        } else if (length > MAX_CODE_LENGTH) {
            length = MAX_CODE_LENGTH;
        }
        code = StringUtils.getUniqueId(length).toUpperCase();
        buildBufferImage(width, height);
    }

    private void buildBufferImage(int width, int height) {
        buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        graphics = buffImg.createGraphics();
        // 制作背景色
        drawBg(width, height);
        // 画干扰正弦曲线
        drawSin(width, height);
        // 画干扰直线
        drawLine(width, height);
        // 画验证码
        drawCode(width, height);
        // 画圆圈
        drawOval(width, height);
        // 扭曲图片
        twistImage(width, height);
        graphics.dispose();
    }

    private void drawBg(int width, int height) {
        graphics.setPaint(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
    }

    private void drawSin(int width, int height) {
        Color c = ColorUtils.randomColor(0, 255);
        graphics.setColor(c);
        double dy;
        int d = -random.nextInt(width * 10);
        int begin = random.nextInt(width / 4);
        int end = width / 2 + random.nextInt(width);
        for (int i = begin; i < end; i = i + sinCurveStep) {
            for (int j = 0; j < height; j = j + sinCurveStep) {
                dy = (d + i) * width / sinPeriodPoint * 2.3;
                dy = height / 2 + height * Math.sin(Math.toRadians(dy)) / 2;
                graphics.fillRect(i, (int) (dy), random.nextInt(sinMaxThick),
                        random.nextInt(sinMaxThick));
            }
        }
    }

    private void drawLine(int width, int height) {
        for (int i = 0; i < lineCount; i++) {
            Color c = ColorUtils.randomColor(0, 255);
            graphics.setColor(c);
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = random.nextInt(width);
            int ye = random.nextInt(height);
            graphics.drawLine(xs, ys, xe, ye);
        }
    }

    private void drawCode(int width, int height) {
        // 每个字符的宽度
        int cWidth = width / (code.length() + 1);
        int codeY = height - 2;
        int index = 0;
        for (char c : code.toCharArray()) {
            // 字体颜色
            Color color = ColorUtils.randomColor(0, 130);
            // 阴影颜色
            Color shadowColor = ColorUtils.weakColor(color, 0.1);
            Font font = getRandomFont(height * 4 / 5, height);
            graphics.setFont(font);
            // 旋转度数radian
            double radian = Math.toRadians(codeMaxRadian
                    - random.nextInt(codeMaxRadian * 2));
            // Y坐标
            int tempy = codeY - random.nextInt(height / 3);
            // 旋转坐标系
            graphics.rotate(radian, index * cWidth + cWidth, tempy);
            // 绘制阴影
            for (int i = 1; i <= codeShadow; i++) {
                graphics.setColor(shadowColor);
                graphics.drawString(String.valueOf(c), index * cWidth + cWidth
                        / 2 + i, tempy);
            }
            // 绘制字符
            graphics.setColor(color);
            graphics.drawString(String.valueOf(c), index * cWidth + cWidth / 2,
                    tempy);
            // 还原坐标系
            graphics.rotate(-radian, index * cWidth + cWidth, tempy);
            index++;
        }
    }

    /**
     * 获取随机字体
     *
     * @param minSize
     * @param maxSize
     * @return
     */
    private Font getRandomFont(int minSize, int maxSize) {
        String fontName = FONTS[random.nextInt(FONTS.length - 1)];
        int style = STYLES[random.nextInt(STYLES.length - 1)];
        return new Font(fontName, style, DataUtils.getRandomData(minSize,
                maxSize));
    }

    private void drawOval(int width, int height) {
        Color c = ColorUtils.randomColor(0, 255);
        graphics.setColor(c);

        for (int i = 0; i < ovalCount; i++) {
            c = ColorUtils.randomColor(0, 255);
            graphics.setColor(c);
            int w = 5 + random.nextInt(10);
            int h = 5 + random.nextInt(10);
            int tx = random.nextInt(width - w);
            int ty = random.nextInt(height - h);
            graphics.drawOval(tx, ty, w, h);
        }
    }

    private void twistImage(int width, int height) {
        // TODO Auto-generated method stub

    }

    public BufferedImage getBuffImg() {
        return buffImg;
    }

    public String getCode() {
        return code;
    }

}
