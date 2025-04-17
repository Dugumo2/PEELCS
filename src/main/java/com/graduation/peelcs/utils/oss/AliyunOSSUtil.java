package com.graduation.peelcs.utils.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public class AliyunOSSUtil {

    // OSS 配置参数
    private static final String ENDPOINT = "oss-cn-chengdu.aliyuncs.com";
    private static final String BUCKET_NAME = "pleecs";
    private static final String BUCKET_DOMAIN = "https://pleecs.oss-cn-chengdu.aliyuncs.com"; // 使用 https
    private static final String REGION = "cn-chengdu";

    /**
     * 上传文件到阿里云 OSS 并返回访问 URL
     *
     * @param file MultipartFile 文件
     * @return 文件访问 URL
     * @throws IOException 上传过程中发生的异常
     */
    public static String uploadFile(MultipartFile file) throws IOException, ClientException {
        // 创建 OSSClient 实例
        EnvironmentVariableCredentialsProvider credentialsProvider =
                CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(ENDPOINT)
                .credentialsProvider(credentialsProvider)
                .region(REGION)
                .build();

        try {
            // 生成唯一的文件名，避免覆盖
            String originalFilename = file.getOriginalFilename();
            String fileExt = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString().replaceAll("-", "") + fileExt;

            // 定义文件在 OSS 中存储的路径，可以根据需要进行调整
            String objectName = "images/" + uniqueFileName;

            // 创建 PutObjectRequest 对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, objectName, file.getInputStream());

            // 上传文件
            ossClient.putObject(putObjectRequest);

            // 构建文件访问 URL
            String fileUrl = BUCKET_DOMAIN + "/" + objectName;

            return fileUrl;
        } catch (IOException e) {
            throw e;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 从阿里云 OSS 删除文件
     *
     * @param fileUrl 完整的文件URL
     * @return 是否删除成功
     * @throws ClientException OSS客户端异常
     */
    public static boolean deleteFile(String fileUrl) throws ClientException {
        // 创建 OSSClient 实例
        EnvironmentVariableCredentialsProvider credentialsProvider =
                CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(ENDPOINT)
                .credentialsProvider(credentialsProvider)
                .region(REGION)
                .build();
                
        try {
            // 从URL提取文件路径
            String objectName = getObjectNameFromUrl(fileUrl);
            if (objectName == null || objectName.isEmpty()) {
                return false;
            }
            
            // 检查文件是否存在
            boolean exists = ossClient.doesObjectExist(BUCKET_NAME, objectName);
            if (!exists) {
                return false;
            }
            
            // 删除文件
            ossClient.deleteObject(BUCKET_NAME, objectName);
            return true;
        } catch (Exception e) {
            // 记录异常并返回失败
            e.printStackTrace();
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 从URL中提取对象名称
     *
     * @param fileUrl 完整的文件URL
     * @return 对象名称
     */
    private static String getObjectNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        // 去掉域名部分，提取对象名称
        if (fileUrl.startsWith(BUCKET_DOMAIN)) {
            return fileUrl.substring(BUCKET_DOMAIN.length() + 1);
        }
        
        return null;
    }
}