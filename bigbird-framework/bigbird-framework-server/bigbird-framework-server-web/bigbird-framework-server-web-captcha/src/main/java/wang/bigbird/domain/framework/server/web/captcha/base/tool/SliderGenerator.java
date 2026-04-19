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
package wang.bigbird.domain.framework.server.web.captcha.base.tool;

import wang.bigbird.domain.framework.core.base.util.DataUtils;
import wang.bigbird.domain.framework.core.base.util.ImageUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.captcha.base.enums.LocationEnum;
import wang.bigbird.domain.framework.server.web.captcha.domain.pojo.Slider;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 滑块工具类
 *
 * @author Bigbird
 */
public class SliderGenerator {

    /**
     * 图片大小
     */
    private static int IMAGE_WIDTH = 360;
    private static int IMAGE_HEIGHT = 230;
    /**
     * 抠图上面的半径
     */
    private static int RADIUS = IMAGE_WIDTH / 36;
    /**
     * 抠图区域的高度
     */
    private static int CUT_HEIGHT = IMAGE_WIDTH / 8;
    /**
     * 抠图区域的宽度
     */
    private static int CUT_WIDTH = IMAGE_WIDTH / 8;
    /**
     * 抠图区域填充的颜色
     */
    private static int FLAG = 0xffffff;
    /**
     * 滑块图组件标识
     */
    private static String BASE_IMAGE_KEY = "BASE_IMAGE_KEY";
    private static String PATCH_IMAGE_KEY = "PATCH_IMAGE_KEY";
    /**
     * 边框粗度
     */
    private static int THICKNESS = 2;

    /**
     * 抠图部分凸起的方向
     */
    private LocationEnum location;
    /**
     * 抠图坐标
     */
    private int xPOS;
    private int yPOS;

    /**
     * 创建滑块
     *
     * @param filePath    滑块图路径
     * @param imageFormat 滑块图格式
     * @return 滑块对象
     * @throws IOException
     */
    public Slider generate(URL filePath, String imageFormat) throws IOException {
        BufferedImage oriBufferedImage = ImageIO.read(filePath.openStream());
        //检测图片大小
        oriBufferedImage = adjustImage(oriBufferedImage);
        //初始化原点坐标
        createXYPos(oriBufferedImage);
        //获取被扣图像的标志图
        int[][] blockData = getBlockData(oriBufferedImage);
        //计算抠图区域的信息
        int x = 0, y = 0;
        int w = 0, h = 0;
        if (location == LocationEnum.UP) {
            x = xPOS;
            y = yPOS - RADIUS;
            w = CUT_WIDTH;
            h = CUT_HEIGHT + RADIUS;
        } else if (location == LocationEnum.LEFT) {
            x = xPOS - RADIUS;
            y = yPOS;
            w = CUT_WIDTH + RADIUS;
            h = CUT_HEIGHT;
        } else if (location == LocationEnum.DOWN) {
            x = xPOS;
            y = yPOS;
            w = CUT_WIDTH;
            h = CUT_HEIGHT + RADIUS;
        } else if (location == LocationEnum.RIGHT) {
            x = xPOS;
            y = yPOS;
            w = CUT_WIDTH + RADIUS;
            h = CUT_HEIGHT;
        }
        //获取扣了图的原图和被扣部分的图
        Map<String, BufferedImage> imageMap = cutByTemplate(oriBufferedImage, blockData, x, y, w, h);
        String base64Prefix = "data:image/jpeg;base64,";
        String baseImg = base64Prefix + ImageUtils.imageToBase64(imageMap.get(BASE_IMAGE_KEY), imageFormat);
        //滑块本身（缺口块）需要透明背景 → 必须 PNG
        String patchImg = base64Prefix + ImageUtils.imageToBase64(imageMap.get(PATCH_IMAGE_KEY), ImageUtils.IMAGE_FORMAT_PNG);
        return new Slider(StringUtils.getUuid(), baseImg, patchImg, x, y);
    }

    /**
     * 调整图片到规定尺寸
     */
    private BufferedImage adjustImage(BufferedImage image) throws IOException {
        if ((image.getWidth() == IMAGE_WIDTH) && (image.getHeight() == IMAGE_HEIGHT)) {
            return image;
        } else {
            return ImageUtils.compressImage(image, IMAGE_WIDTH, IMAGE_HEIGHT);
        }
    }

    /**
     * 获取抠图区的坐标原点
     */
    private void createXYPos(BufferedImage oriImg) {
        int height = oriImg.getHeight();
        int width = oriImg.getWidth();
        xPOS = DataUtils.getRandomData(0, width - CUT_WIDTH - RADIUS - 1);
        yPOS = RADIUS + DataUtils.getRandomData(0, height - CUT_HEIGHT - RADIUS - RADIUS - 1);
        //确保横坐标位于2/4 ~ 3/4
        int div = (IMAGE_WIDTH / 4);
        if (xPOS / div == 0) {
            xPOS = xPOS + div * 2;
        } else if (xPOS / div == 1) {
            xPOS = xPOS + div;
        } else if (xPOS / div == 3) {
            xPOS = xPOS - div;
        }
    }

    /**
     * 获取抠图块数据，被扣的像素点将使用FLAG进行标记
     */
    private int[][] getBlockData(BufferedImage oriImage) {
        int height = oriImage.getHeight();
        int width = oriImage.getWidth();
        int[][] blockData = new int[width][height];
        LocationEnum locations[] = {LocationEnum.UP, LocationEnum.LEFT, LocationEnum.DOWN, LocationEnum.RIGHT};
        for (int x = 0; x < width && x >= 0; x++) {
            for (int y = 0; y < height && y >= 0; y++) {
                blockData[x][y] = 0;
                if ((x > xPOS) && (x < (xPOS + CUT_WIDTH))
                        && (y > yPOS) && (y < (yPOS + CUT_HEIGHT))) {
                    blockData[x][y] = FLAG;
                }
            }
        }
        //圆形突出区域
        //突出圆形的原点坐标(x,y)
        int xBulgeCenter = 0, yBulgeCenter = 0;
        int xConcaveCenter = 0, yConcaveCenter = 0;
        location = locations[DataUtils.getRandomData(0, 3)];
        if (location == LocationEnum.UP) {
            //上 凸起
            xBulgeCenter = xPOS + CUT_WIDTH / 2;
            yBulgeCenter = yPOS;
            //左　凹陷
            xConcaveCenter = xPOS;
            yConcaveCenter = yPOS + CUT_HEIGHT / 2;
        } else if (location == LocationEnum.DOWN) {
            //下　凸起
            xBulgeCenter = xPOS + CUT_WIDTH / 2;
            yBulgeCenter = yPOS + CUT_HEIGHT;
            //右　凹陷
            xConcaveCenter = xPOS + CUT_WIDTH;
            yConcaveCenter = yPOS + CUT_HEIGHT / 2;
        } else if (location == LocationEnum.LEFT) {
            //左　凸起
            xBulgeCenter = xPOS;
            yBulgeCenter = yPOS + CUT_HEIGHT / 2;
            //下　凹陷
            xConcaveCenter = xPOS + CUT_WIDTH / 2;
            yConcaveCenter = yPOS + CUT_HEIGHT;
        } else {
            //右　凸起
            xBulgeCenter = xPOS + CUT_WIDTH;
            yBulgeCenter = yPOS + CUT_HEIGHT / 2;
            //上　凹陷
            xConcaveCenter = xPOS + CUT_WIDTH / 2;
            yConcaveCenter = yPOS;
        }
        //半径的平方
        int RADIUS_POW2 = RADIUS * RADIUS;
        //凸起部分
        for (int x = xBulgeCenter - RADIUS; x <= xBulgeCenter + RADIUS && x >= 0; x++) {
            for (int y = yBulgeCenter - RADIUS; y <= yBulgeCenter + RADIUS && y >= 0; y++) {
                if (Math.pow((x - xBulgeCenter), 2) + Math.pow((y - yBulgeCenter), 2) <= RADIUS_POW2) {
                    blockData[x][y] = FLAG;
                }
            }
        }
        //凹陷部分
        for (int x = xConcaveCenter - RADIUS; x <= xConcaveCenter + RADIUS && x >= 0; x++) {
            for (int y = yConcaveCenter - RADIUS; y <= yConcaveCenter + RADIUS && y >= 0; y++) {
                if (Math.pow((x - xConcaveCenter), 2) + Math.pow((y - yConcaveCenter), 2) <= RADIUS_POW2) {
                    blockData[x][y] = 0;
                }
            }
        }
        return blockData;
    }

    public Map<String, BufferedImage> cutByTemplate(BufferedImage oriImage, int[][] blockData, int xPos, int yPos, int w, int h) {
        Map<String, BufferedImage> imgMap = new HashMap<>();
        BufferedImage patchImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage cutImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d_patch = patchImage.createGraphics();
        Graphics2D g2d_cut = cutImage.createGraphics();
        g2d_patch.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d_patch.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d_patch.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d_patch.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d_cut.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d_cut.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d_cut.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d_cut.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        int xmax = xPos + w;
        int ymax = yPos + h;
        for (int x = xPos; x <= xmax && x >= 0; x++) {
            for (int y = yPos; y <= ymax && y >= 0; y++) {
                int oriRgb = oriImage.getRGB(x, y);
                if (blockData[x][y] == FLAG) {
                    //描边判断是否为边界，如果是边界则填充为白色
                    if (isBorder(blockData, x, y)) {
                        oriImage.setRGB(x, y, FLAG);
                        g2d_patch.setColor(color(FLAG));
                        g2d_cut.setColor(color(FLAG));
                    } else {
                        g2d_patch.setColor(color(oriRgb));
                        g2d_cut.setColor(Color.black);
                    }
                    g2d_patch.setStroke(new BasicStroke(1f));
                    g2d_cut.setStroke(new BasicStroke(1f));
                    g2d_patch.fillRect(x - xPos, y - yPos, 1, 1);
                    g2d_cut.fillRect(x - xPos, y - yPos, 1, 1);
                }
            }
        }
        // 释放对象
        g2d_patch.dispose();
        g2d_cut.dispose();
        BufferedImage baseImage = ImageUtils.addWatermark(oriImage, cutImage, xPos, yPos, 0.6f);
        imgMap.put(BASE_IMAGE_KEY, baseImage);
        imgMap.put(PATCH_IMAGE_KEY, patchImage);
        return imgMap;
    }

    private boolean isBorder(int[][] blockData, int x, int y) {
        for (int i = 0; i <= THICKNESS; i++) {
            if (blockData[x - i][y] != FLAG || blockData[x + i][y] != FLAG || blockData[x][y + i] != FLAG || blockData[x][y - i] != FLAG) {
                return true;
            }
        }
        return false;
    }

    private Color color(int rgb) {
        int b = (0xff & rgb);
        int g = (0xff & (rgb >> 8));
        int r = (0xff & (rgb >> 16));
        return new Color(r, g, b);
    }
}
