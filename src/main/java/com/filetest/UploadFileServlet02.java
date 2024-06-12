package com.filetest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.UUID;

public class UploadFileServlet02 extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("UploadFileServlet02 POST");
        // 判断表单是普通表单还是带文件的表单
        if (!ServletFileUpload.isMultipartContent(req)) {
            return; // 终止方法
        }

        // 创建上传文件的保存路径, 建议在 WEN-INF 路径下, 安全且用户无法直接访问
        String uploadPath = this.getServletContext().getRealPath("/WEB-INF/upload");
        File uploadFile = new File(uploadPath);
        if (!uploadFile.exists()) {
            // 如果文件夹不存在, 就创建
            uploadFile.mkdir();
        }

        // 缓存, 临时文件, 假如文件超出了预期大小, 就放到临时文件中, 过几天删除, 或者转为永久文件
        String tempPath = this.getServletContext().getRealPath("/WEB-INF/temp");
        File tempFile = new File(tempPath);
        if (!tempFile.exists()) {
            // 如果文件夹不存在, 就创建
            uploadFile.mkdir();
        }

        // 处理文件上传, 一般通过获取流, request.getInputStream() 原生方式十分麻烦
        // 可以使用 Apache 文件上传组件, commons-fileupload, 它依赖于 commons-io 组件

        // 1. 创建 DiskFileItemFactory 对象, 处理文件上传路径或者大小限制
        DiskFileItemFactory factory = getDiskFileItemFactory(uploadFile);

        // 2. 获取 ServletFileUpload 对象负责处理上传文件数据, 可以监听文件上传进度, 以及编码问题
        ServletFileUpload upload = getServletFileUpload(factory);

        // 3. 处理上传文件
        String msg = uploadParamsRequest(upload, uploadPath, req);

        req.setAttribute("msg", msg);
        req.getRequestDispatcher("info.jsp").forward(req, resp);
    }

    // 获取 DiskFileItemFactory 对象, 处理文件上传路径或者大小限制
    public static DiskFileItemFactory getDiskFileItemFactory(File file) {
        // 处理文件上传, 一般通过获取流, request.getInputStream() 原生方式十分麻烦
        // 可以使用 Apache 文件上传组件, commons-fileupload, 它依赖于 commons-io 组件

        // 1. 创建 DiskFileItemFactory 对象, 处理文件上传路径或者大小限制
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // 设置一个缓冲区大小, 当文件大于这个缓冲区大小的时候, 就会放到临时目录
        factory.setSizeThreshold(1024 * 1024); // 缓冲区大小 1 M
        factory.setRepository(file); // 设置临时目录, 接收一个 File

        return factory;
    }

    // 获取 ServletFileUpload 对象负责处理上传文件数据, 可以监听文件上传进度, 以及编码问题
    public static ServletFileUpload getServletFileUpload(DiskFileItemFactory factory) {
        // 2. 获取 ServletFileUpload 对象负责处理上传文件数据, 可以监听文件上传进度, 以及编码问题
        ServletFileUpload upload = new ServletFileUpload(factory);
        // 监听文件上传进度
        upload.setProgressListener(new ProgressListener() {
            @Override
            public void update(long readBytes, long contentLength, int i) {
                System.out.println("总大小: " + contentLength + " 已上传进度: " + readBytes);
            }
        });
        // 处理乱码问题
        upload.setHeaderEncoding("UTF-8");
        // 设置单个文件最大值
        upload.setFileSizeMax(1024 * 1024 * 10);
        // 设置总共能够上传文件大小
        upload.setFileSizeMax(1024 * 1024 * 10); // 10M

        return upload;
    }

    // 保存文件
    public static String uploadParamsRequest(ServletFileUpload upload, String uploadPath, HttpServletRequest req) {
        String msg = "";

        try {
            // 3. 处理上传文件
            // 解析前段请求, 封装成一个 FileItem 对象, 需要从 ServletFileUpload 对象中获取
            List<FileItem> fileItems = upload.parseRequest(req);
            for (FileItem fileItem : fileItems) {
                // 判断上传文件是普通表单还是带文件的表单, true 代表普通表单, false 代表文件表单
                if (fileItem.isFormField()) {
                    // 普通表单, 获取name属性
                    String fieldName = fileItem.getFieldName();
                    String value = fileItem.getString("UTF-8"); // 处理乱码
                    System.out.println(fieldName + " : " + value);
                } else {
                    //===================处理文件===================
                    // 获取文件名
                    String uploadFileName = fileItem.getName();
                    System.out.println("上传的文件名: " + uploadFileName);
                    // 判断文件名是否合法, 不合法直接跳过
                    if (uploadFileName.trim().equals("") || uploadFileName == null) {
                        continue;
                    }

                    // 获取上传的文件名
                    String fileName = uploadFileName.substring(uploadFileName.lastIndexOf("/") + 1);
                    // 获取上传文件后缀
                    String uploadExtName = uploadFileName.substring(uploadFileName.lastIndexOf(".") + 1);
                    System.out.println("文件信息 [文件名: " + fileName + " ---文件类型: " + uploadExtName + "]");

                    // 网络传输中的东西需要 序列化(Serializable) 才能传输 Serializable: 标记接口, JVM --> Java栈 、 本地方法栈 native  --> c++
                    // 使用 UUID (唯一识别通用码), 保证文件名唯一
                    UUID uuidPath = UUID.randomUUID();


                    //===================存放地址===================
                    // 文件真实存放的地址 readPath
                    String readPath = uploadPath + File.separator + uuidPath;
                    // 给文件开辟一个文件夹
                    File readPathFile = new File(readPath);
                    if (!readPathFile.exists()) {
                        // 如果没有该文件夹, 就创建
                        readPathFile.mkdir();
                    }

                    //===================文件传输===================
                    // 获取输入流
                    InputStream is = fileItem.getInputStream();

                    // 获取文件输出流
                    FileOutputStream fos = new FileOutputStream(readPathFile + File.separator + fileName);

                    // 建立缓存区
                    byte[] buffer = new byte[1024 * 1024];
                    int len = 0;
                    // 读取流
                    while ((len = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    // 关闭流
                    fos.close();
                    is.close();

                    // 清除临时文件
                    fileItem.delete();
                }
            }

            msg = "文件上传成功";
        } catch (Exception e) {
            e.printStackTrace();
            msg = "文件上传失败";
        }

        return msg;
    }
}

