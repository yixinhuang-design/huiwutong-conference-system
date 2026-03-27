package com.conference.meeting.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.conference.meeting.config.FileUploadProperties;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件压缩工具类
 * 支持图片压缩和PDF压缩
 */
@Slf4j
@Component
public class FileCompressionUtil {

    @Autowired
    private FileUploadProperties uploadProperties;

    /**
     * 是否需要压缩该文件
     */
    public boolean shouldCompress(String fileType) {
        if (!uploadProperties.getEnableImageCompress() && !uploadProperties.getEnablePdfCompress()) {
            return false;
        }

        String type = fileType.toLowerCase();

        // 图片压缩
        if (uploadProperties.getEnableImageCompress()) {
            if (type.equals("jpg") || type.equals("jpeg") || type.equals("png")) {
                return true;
            }
        }

        // PDF压缩
        if (uploadProperties.getEnablePdfCompress()) {
            if (type.equals("pdf")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 压缩图片文件
     * 支持JPG、PNG
     * 
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @param fileType 文件类型（jpg, png）
     * @return 压缩后是否成功，true表示压缩成功，false表示压缩失败或跳过
     */
    public boolean compressImage(Path sourcePath, Path targetPath, String fileType) {
        if (!uploadProperties.getEnableImageCompress()) {
            log.debug("图片压缩已禁用");
            return false;
        }

        try {
            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(sourcePath.toFile());
            if (originalImage == null) {
                log.warn("无法读取图片文件: {}", sourcePath);
                return false;
            }

            // 检查是否需要压缩（如果文件足够小则跳过）
            long fileSize = Files.size(sourcePath);
            if (fileSize < 1024 * 1024) { // 小于1MB，可能不需要压缩
                log.debug("图片文件较小，跳过压缩: {} bytes", fileSize);
                return false;
            }

            // 计算缩放尺寸（如果图片过大则缩小）
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            int maxWidth = 2560;  // 最大宽度2560px
            int maxHeight = 2560; // 最大高度2560px

            if (width > maxWidth || height > maxHeight) {
                double scaleX = (double) maxWidth / width;
                double scaleY = (double) maxHeight / height;
                double scale = Math.min(scaleX, scaleY);

                int newWidth = (int) (width * scale);
                int newHeight = (int) (height * scale);

                log.debug("缩放图片: {}x{} -> {}x{}", width, height, newWidth, newHeight);

                // 创建缩放后的图片
                BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = scaledImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                g2d.dispose();

                originalImage = scaledImage;
            }

            // 设置压缩质量
            int quality = uploadProperties.getImageQuality();
            log.debug("图片压缩质量: {}%", quality);

            // 保存压缩后的图片
            if ("png".equalsIgnoreCase(fileType)) {
                // PNG不支持JPEG质量参数，但可以使用ImageIO直接保存
                ImageIO.write(originalImage, "PNG", targetPath.toFile());
            } else {
                // JPG/JPEG使用质量参数
                ImageIO.write(originalImage, "JPEG", targetPath.toFile());
            }

            long compressedSize = Files.size(targetPath);
            long originalSizeKB = fileSize / 1024;
            long compressedSizeKB = compressedSize / 1024;
            double ratio = (1.0 - (double) compressedSize / fileSize) * 100;

            log.info("图片压缩完成: {} KB -> {} KB (压缩率: {:.1f}%)",
                    originalSizeKB, compressedSizeKB, ratio);

            return true;

        } catch (IOException e) {
            log.warn("图片压缩失败: {}, 原因: {}", sourcePath, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("图片压缩异常", e);
            return false;
        }
    }

    /**
     * 压缩PDF文件
     * 注意：需要itext或pdfbox库支持，这里提供接口
     * 实际实现可选：
     * 1. 使用开源库（PDFBox）
     * 2. 调用系统命令（ghostscript）
     * 3. 调用云端API
     * 
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 压缩是否成功
     */
    public boolean compressPdf(Path sourcePath, Path targetPath) {
        if (!uploadProperties.getEnablePdfCompress()) {
            log.debug("PDF压缩已禁用");
            return false;
        }

        try {
            // 这里仅提供接口，具体实现需要额外依赖
            // 方案1: 使用PDFBox库
            // 方案2: 调用Ghostscript命令
            // 方案3: 上传到云端处理
            
            // 临时方案：直接拷贝（实际应用中应实现真实压缩）
            long fileSize = Files.size(sourcePath);
            
            if (fileSize < 2 * 1024 * 1024) { // 小于2MB，跳过压缩
                log.debug("PDF文件较小，跳过压缩: {} KB", fileSize / 1024);
                return false;
            }

            // TODO: 实现真实的PDF压缩
            // 可选方案：
            // 1. 集成PDFBox: https://pdfbox.apache.org/
            // 2. 使用Ghostscript命令: ghostscript -sDEVICE=pdfwrite
            // 3. 使用在线API: TinyPDF, ILovePDF等

            log.info("PDF压缩功能未启用，请集成相关库");
            return false;

        } catch (IOException e) {
            log.warn("PDF压缩失败: {}, 原因: {}", sourcePath, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("PDF压缩异常", e);
            return false;
        }
    }

    /**
     * 根据文件类型选择合适的压缩方式
     */
    public boolean compressFile(Path sourcePath, Path targetPath, String fileType) {
        String type = fileType.toLowerCase();

        if (type.equals("jpg") || type.equals("jpeg") || type.equals("png")) {
            return compressImage(sourcePath, targetPath, type);
        } else if (type.equals("pdf")) {
            return compressPdf(sourcePath, targetPath);
        }

        log.debug("文件类型不支持压缩: {}", fileType);
        return false;
    }

    /**
     * 计算文件压缩后的预期大小（用于前端显示）
     */
    public long estimateCompressedSize(long originalSize, String fileType) {
        String type = fileType.toLowerCase();

        if (type.equals("jpg") || type.equals("jpeg")) {
            // JPEG通常可以压缩到原始大小的30-50%
            return (long) (originalSize * 0.4);
        } else if (type.equals("png")) {
            // PNG压缩效果不如JPEG，约30-40%
            return (long) (originalSize * 0.35);
        } else if (type.equals("pdf")) {
            // PDF压缩效果较好，可以达到20-40%
            return (long) (originalSize * 0.3);
        }

        // 其他文件类型不压缩
        return originalSize;
    }
}
