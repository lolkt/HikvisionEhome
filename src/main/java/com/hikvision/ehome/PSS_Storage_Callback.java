package com.hikvision.ehome;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 文件保存回调函数(下载)
 * @author maq
 */
public class PSS_Storage_Callback implements HCISUPSS.EHomeSSStorageCallBack {

    @Override
    public boolean invoke(NativeLong iHandle, String pFileName, Pointer pFileBuf, int dwFileLen, Pointer pFilePath, Pointer pUser) {
        String strPath = "C://EhomePicServer/";
        String strFilePath = strPath + pFileName;

        //若此目录不存在，则创建之
        File myPath = new File(strPath);
        if (!myPath.exists()) {
            myPath.mkdir();
            System.out.println("创建文件夹路径为：" + strPath);
        }

        if (dwFileLen > 0 && pFileBuf != null) {
            FileOutputStream fout;
            try {
                fout = new FileOutputStream(strFilePath);
                //将字节写入文件
                long offset = 0;
                ByteBuffer buffers = pFileBuf.getByteBuffer(offset, dwFileLen);
                byte[] bytes = new byte[dwFileLen];
                buffers.rewind();
                buffers.get(bytes);
                fout.write(bytes);
                fout.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        pFilePath.write(0, strFilePath.getBytes(), 0, strFilePath.getBytes().length);
        return true;
    }
}
