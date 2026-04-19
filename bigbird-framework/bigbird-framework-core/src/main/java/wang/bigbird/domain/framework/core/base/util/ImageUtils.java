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
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

/**
 * 图像工具类
 *
 * @author Bigbird
 */
@Slf4j
public class ImageUtils {

    /**
     * PNG格式
     */
    public static final String IMAGE_FORMAT_PNG = "png";

    /**
     * 将图像写入指定文件
     *
     * @param destFilePath 目标文件路径
     * @param srcImage     源图像
     * @param format       文件格式
     * @return 写入是否成功
     */
    public static boolean writeImage(String destFilePath, Image srcImage,
                                     String format) {
        return writeImage(new File(destFilePath), srcImage, format);
    }

    /**
     * 将图像写入指定文件
     *
     * @param destFile 目标文件
     * @param srcImage 源图像
     * @param format   文件格式
     * @return 写入是否成功
     */
    public static boolean writeImage(File destFile, Image srcImage,
                                     String format) {
        try {
            File dir = destFile.getParentFile();
            if (!FileUtils.newFolder(dir)) {
                return false;
            }
            OutputStream outputStream = new FileOutputStream(destFile);
            // 该处理代码是为了保持png格式的图片底色正确
            BufferedImage image = setImageCanvas(srcImage, null, format);
            ImageIO.write(image, format, outputStream);
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            log.error("WriteImage:", e);
            return false;
        }
    }

    /**
     * 设置图像背景色，该方法能保持底色为透明的png格式的图片任然为透明底色
     *
     * @param outputImage 要处理的图片
     * @param canvasColor 画布颜色
     * @param format      图片格式
     * @return 处理后的图像
     */
    public static BufferedImage setImageCanvas(Image outputImage,
                                               Color canvasColor, String format) {
        int outputWidth = outputImage.getWidth(null);
        if (outputWidth < 1) {
            throw new IllegalArgumentException("output image width "
                    + outputWidth + " is out of range");
        }
        int outputHeight = outputImage.getHeight(null);
        if (outputHeight < 1) {
            throw new IllegalArgumentException("output image height "
                    + outputHeight + " is out of range");
        }
        // Get a buffered image from the image.
        BufferedImage image = new BufferedImage(outputWidth, outputHeight,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        if (canvasColor == null) {
            if (format.equalsIgnoreCase(IMAGE_FORMAT_PNG)) {
                // png格式的图片，画布颜色应该为透明
                image = g2d.getDeviceConfiguration().createCompatibleImage(
                        outputWidth, outputHeight, Transparency.TRANSLUCENT);
                g2d.dispose();
                g2d = image.createGraphics();
            } else {
                // 非png格式的图片，画布颜色默认为白色
                g2d.setColor(Color.white);
                g2d.fillRect(0, 0, outputWidth, outputHeight);
            }
        } else {
            g2d.setColor(canvasColor);
            g2d.fillRect(0, 0, outputWidth, outputHeight);
        }
        // 设置画笔宽度
        g2d.setStroke(new BasicStroke(1));
        g2d.drawImage(outputImage, 0, 0, null);
        // 释放对象
        g2d.dispose();
        return image;
    }

    /**
     * 创建期望宽高的缓存图，保持原有图像宽高比
     *
     * @param image       原有缓存图
     * @param scaleWidth  缩放后期望得到的宽
     * @param scaleHeight 缩放后期望得到的高
     * @return 缩放后图像
     */
    public static BufferedImage createScaleBufferedImage(BufferedImage image,
                                                         int scaleWidth, int scaleHeight) {

        float defaultScale = scaleHeight * 1.0f / scaleWidth + 0.05f;
        // 真实高宽比以及高、宽值
        float width = image.getWidth();
        float height = image.getHeight();
        float scale = height / width;
        // 缩放后高宽值
        int iconWidth = 0;
        int iconHeight = 0;
        // 判断缩放以高为基准还是以宽为基准，该处为了使图片尽量符合期望尺寸，会对小于默认尺寸的图片进行放大
        if (scale <= defaultScale) {
            iconWidth = scaleWidth;
            iconHeight = (int) (scale * iconWidth);
        } else {
            iconHeight = scaleHeight;
            iconWidth = (int) (1.0f * iconHeight / scale);
        }
        BufferedImage img = new BufferedImage(iconWidth, iconHeight,
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2d.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), 0, 0,
                image.getWidth(), image.getHeight(), null);
        g2d.dispose();
        return img;
    }

    /**
     * 图象缩放
     *
     * @param image      原始图象
     * @param proportion 缩放比例
     * @return 缩放后图像
     */
    public static BufferedImage createScaleImage(Image image, float proportion) {
        int w = (int) (image.getWidth(null) * proportion);
        int h = (int) (image.getHeight(null) * proportion);
        return createScaleBufferedImageByProgressive(image, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
    }

    /**
     * 提供渐进缩放能力的缩放，用于提供更快更好地缩放效果
     *
     * @param image               要缩放的图像
     * @param targetWidth         目标宽
     * @param targetHeight        目标高
     * @param hint                渲染提示
     * @param progressiveBilinear 是否采用渐进方式
     * @return 缩放后图像
     */
    public static BufferedImage createScaleBufferedImageByProgressive(
            Image image, int targetWidth, int targetHeight, Object hint,
            boolean progressiveBilinear) {
        BufferedImage img = transferImage(image);
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        BufferedImage scratchImage = null;
        Graphics2D g2 = null;
        int w, h;
        int prevW = ret.getWidth();
        int prevH = ret.getHeight();

        // 图像是否透明
        boolean isTranslucent = img.getTransparency() != Transparency.OPAQUE;

        if (progressiveBilinear) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (progressiveBilinear && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            } else {
                // 放大直接进行，不采用渐进方式
                w = targetWidth;
            }

            if (progressiveBilinear && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            } else {
                // 放大直接进行，不采用渐进方式
                h = targetHeight;
            }

            if (scratchImage == null || isTranslucent) {
                // Use a single scratch buffer for all iterations
                // and then copy to the final, correctly-sized image
                // before returning
                scratchImage = new BufferedImage(w, h, type);
                g2 = scratchImage.createGraphics();
            }
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);
            prevW = w;
            prevH = h;

            ret = scratchImage;
        } while (w != targetWidth || h != targetHeight);

        if (g2 != null) {
            g2.dispose();
        }

        // If we used a scratch buffer that is larger than our target size,
        // create an image of the right size and copy the results into it
        if (targetWidth != ret.getWidth() || targetHeight != ret.getHeight()) {
            scratchImage = new BufferedImage(targetWidth, targetHeight, type);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }

        return ret;
    }

    /**
     * 将Image转化为TYPE_INT_RGB类型的BufferedImage，该方法意义还在于复制BufferedImage，
     * 以保证返回相同数据但是内存地址不相同的图片对象
     *
     * @param image 原始图像
     * @return 复制图像
     */
    public static BufferedImage transferImage(Image image) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        // 创建原始缓冲区图像
        BufferedImage originalBufImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        // 创建缓冲区图像的图形环境
        Graphics2D g2d = originalBufImage.createGraphics();
        // 保持原有的图像背景色
        originalBufImage = g2d.getDeviceConfiguration().createCompatibleImage(
                width, height, Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = originalBufImage.createGraphics();
        // 传输源图像数据到缓冲区图像中
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return originalBufImage;
    }

    /**
     * 裁剪图片
     *
     * @param srcFilePath 源文件
     * @param outFilePath 输出文件
     * @param x           坐标
     * @param y           坐标
     * @param width       宽度
     * @param height      高度
     * @return 图片剪裁是否成功
     */
    public static boolean cutImage(String srcFilePath, String outFilePath, int x, int y,
                                   int width, int height) {
        File srcFile = new File(srcFilePath);
        // 如果源图片不存在
        if (!srcFile.exists()) {
            return false;
        }
        try {
            // 获取文件格式
            BufferedImage srcImage = ImageIO.read(srcFile);
            String format = FileUtils.getSuffix(srcFilePath);
            Rectangle2D rect = new Rectangle2D.Float(0, 0, width, height);
            BufferedImage bufferedImage = cropByShape(srcImage, x, y, rect);
            // 保存新图片
            File tempOutFile = new File(outFilePath);
            if (!tempOutFile.exists()) {
                tempOutFile.mkdirs();
            }
            ImageIO.write(bufferedImage, format, tempOutFile);
            return true;
        } catch (Exception e) {
            log.error("CutImage:", e);
            return false;
        }
    }

    /**
     * 裁剪源图像中指定形状区域的图像
     *
     * @param srcImage 源图像
     * @param x        坐标
     * @param y        坐标
     * @param shape    剪裁形状，起始坐标必须为0，否则截图为黑色
     * @return 指定形状区域的图像
     */
    public static BufferedImage cropByShape(BufferedImage srcImage, int x, int y, Shape shape) {
        int srcWidth = srcImage.getWidth();
        int srcHeight = srcImage.getHeight();
        // 获取形状边界并校验（避免超出图片范围）
        Rectangle shapeBounds = shape.getBounds();
        // 修正形状坐标：确保形状在图片范围内
        int shapeX = Math.max(0, x);
        int shapeY = Math.max(0, y);
        int shapeWidth = Math.min(srcWidth - shapeX, shapeBounds.width);
        int shapeHeight = Math.min(srcHeight - shapeY, shapeBounds.height);
        if (shapeWidth <= 0 || shapeHeight <= 0) {
            throw new IllegalArgumentException("The cropped shape exceeds the picture range! Picture size: " + srcWidth + "x" + srcHeight);
        }
        // 创建带透明通道的缓冲图像（关键：TYPE_INT_ARGB支持透明）
        BufferedImage destImage = new BufferedImage(
                shapeBounds.width, shapeBounds.height,
                BufferedImage.TYPE_INT_ARGB
        );
        // 获取Graphics2D对象
        Graphics2D g2d = destImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // 设置裁剪形状（核心：只绘制形状内的区域）
        g2d.setClip(shape);
        // 将源图片绘制到新画布的指定位置（实现区域裁剪+形状裁剪）
        // 前4个参数目标位置和尺寸
        // 后4个参数源图片裁剪区域
        g2d.drawImage(srcImage,
                0, 0, shapeWidth, shapeHeight,
                x, y, x + shapeWidth, y + shapeHeight,
                null);
        // 释放资源
        g2d.dispose();
        return destImage;
    }

    /**
     * 压缩图片到指定宽高
     *
     * @param image  原始图片
     * @param width  宽
     * @param height 高
     * @return 压缩后图片
     * @throws IOException
     */
    public static BufferedImage compressImage(BufferedImage image, int width, int height) throws IOException {
        return Thumbnails.of(image)
                .forceSize(width, height)
                .asBufferedImage();
    }

    /**
     * 为图片添加水印
     *
     * @param source    原始图
     * @param watermark 水印图
     * @param locationX 水印位置横坐标
     * @param locationY 水印位置纵坐标
     * @param alpha     透明度
     * @return 结果图
     * @throws IOException
     */
    public static BufferedImage addWatermark(BufferedImage source, BufferedImage watermark, int locationX, int locationY, float alpha) {
        Graphics2D graphics2D = source.createGraphics();
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        graphics2D.drawImage(watermark, locationX, locationY, null);
        graphics2D.dispose();
        return source;
    }

    /**
     * 图片转化成base64
     *
     * @param image  图片
     * @param format 图片格式
     * @return 图片base64字符串
     * @throws IOException
     */
    public static String imageToBase64(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ImageIO.write(image, format, bao);
        return Base64.getEncoder().encodeToString(bao.toByteArray());
    }

}
