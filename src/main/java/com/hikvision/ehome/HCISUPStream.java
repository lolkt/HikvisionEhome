package com.hikvision.ehome;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;

import java.util.Arrays;
import java.util.List;

public interface HCISUPStream extends StdCallLibrary {

    HCISUPStream INSTANCE = (HCISUPStream) Native.loadLibrary(".\\lib\\HCISUPStream",
            HCISUPStream.class);


    public static class BYTE_ARRAY extends Structure {
        public byte[] byValue;

        public BYTE_ARRAY(int iLen) {
            byValue = new byte[iLen];
        }

        @Override
        protected List<String> getFieldOrder() {
            // TODO Auto-generated method stub
            return Arrays.asList("byValue");
        }
    }

    public class NET_EHOME_PLAYBACK_LISTEN_PARAM extends Structure {
        public HCISUPCMS.NET_EHOME_IPADDRESS struIPAdress; //本地监听信息，IP为0.0.0.0的情况下，默认为本地地址，多个网卡的情况下，默认为从操作系统获取到的第一个
        public PLAYBACK_NEWLINK_CB fnNewLinkCB; //预览请求回调函数，当收到预览连接请求后，SDK会回调该回调函数。
        public Pointer pUser;        // 用户参数，在fnNewLinkCB中返回出来
        public byte byLinkMode;   //0：TCP，1：UDP 2: HRUDP方式
        public byte[] byRes = new byte[127];
    }


    public class NET_EHOME_LISTEN_PREVIEW_CFG extends Structure {
        public HCISUPCMS.NET_EHOME_IPADDRESS struIPAdress; //本地监听信息，IP为0.0.0.0的情况下，默认为本地地址，多个网卡的情况下，默认为从操作系统获取到的第一个
        public PREVIEW_NEWLINK_CB fnNewLinkCB; //预览请求回调函数，当收到预览连接请求后，SDK会回调该回调函数。
        public Pointer pUser;        // 用户参数，在fnNewLinkCB中返回出来
        public byte byLinkMode;   //0：TCP，1：UDP 2: HRUDP方式
        public byte[] byRes = new byte[127];
    }

    public class NET_EHOME_NEWLINK_CB_MSG extends Structure {
        public byte[] szDeviceID = new byte[HCISUPCMS.MAX_DEVICE_ID_LEN];   //设备标示符
        public NativeLong iSessionID;     //设备分配给该取流会话的ID
        public int dwChannelNo;    //设备通道号
        public byte byStreamType;   //0-主码流，1-子码流
        public byte[] byRes1 = new byte[3];
        public byte[] sDeviceSerial = new byte[HCISUPCMS.NET_EHOME_SERIAL_LEN];    //设备序列号，数字序列号
        public byte[] byRes = new byte[112];
    }

    public class NET_EHOME_PREVIEW_CB_MSG extends Structure {
        public byte byDataType;       //NET_DVR_SYSHEAD(1)-码流头，NET_DVR_STREAMDATA(2)-码流数据
        public byte[] byRes1 = new byte[3];
        public Pointer pRecvdata;      //码流头或者数据
        public int dwDataLen;      //数据长度
        public byte[] byRes2 = new byte[128];
    }

    public class NET_EHOME_PREVIEW_DATA_CB_PARAM extends Structure {
        public PREVIEW_DATA_CB fnPreviewDataCB;    //数据回调函数
        public Pointer pUserData;         //用户参数, 在fnPreviewDataCB回调出来
        public byte[] byRes = new byte[128];          //保留
    }

    public static final int NET_EHOME_DEVICEID_LEN = 256;  //设备ID长度
    public static final int NET_EHOME_SERIAL_LEN = 12;

    public class NET_EHOME_PLAYBACK_NEWLINK_CB_INFO extends Structure {
        public byte[] szDeviceID = new byte[NET_EHOME_DEVICEID_LEN];
        public int lSessionID;     //设备分配给该回放会话的ID，0表示无效(出参)
        public int dwChannelNo;    //设备通道号，0表示无效(出参)
        public byte[] sDeviceSerial = new byte[NET_EHOME_SERIAL_LEN/*12*/]; //设备序列号，数字序列号(出参)
        public byte byStreamFormat;         //码流封装格式：0-PS 1-RTP(入参)
        public byte[] byRes1 = new byte[3];
        public PLAYBACK_DATA_CB fnPlayBackDataCB;
        public Pointer pUserData;
        public byte[] byRes = new byte[88];
    }

    public class NET_EHOME_PLAYBACK_DATA_CB_INFO extends Structure {
        public int dwType;     //类型 1-头信息 2-码流数据
        public Pointer pData;      //数据指针
        public int dwDataLen;  //数据长度
        public byte[] byRes = new byte[128]; //保留
    }


    public interface PLAYBACK_DATA_CB extends StdCallCallback {
        public boolean invoke(int iPlayBackLinkHandle, NET_EHOME_PLAYBACK_DATA_CB_INFO pDataCBInfo, Pointer pUserData);
    }

    public interface PREVIEW_NEWLINK_CB extends StdCallCallback {
        public boolean invoke(NativeLong lLinkHandle, NET_EHOME_NEWLINK_CB_MSG pNewLinkCBMsg, Pointer pUserData);
    }

    public interface PLAYBACK_NEWLINK_CB extends StdCallCallback {
        public boolean invoke(int lPlayBackLinkHandle, NET_EHOME_PLAYBACK_NEWLINK_CB_INFO pNewLinkCBMsg, Pointer pUserData);
    }


    public interface PREVIEW_DATA_CB extends StdCallCallback {
        public void invoke(NativeLong iPreviewHandle, NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData);
    }

    public interface fExceptionCallBack extends StdCallCallback {
        public void invoke(int dwType, NativeLong iUserID, NativeLong iHandle, Pointer pUser);
    }


    public static class StringPointer extends Structure {
        public byte[] data;

        public StringPointer() {
        }

        public StringPointer(String sInput) {
            this.data = new byte[sInput.length()];
            this.data = sInput.getBytes();
        }
    }


    public boolean NET_ESTREAM_Init();

    public boolean NET_ESTREAM_SetSDKLocalCfg(int enumType, Pointer lpInBuff);

    public boolean NET_ESTREAM_SetSDKInitCfg(int enumType, Pointer lpInBuff);


    public boolean NET_ESTREAM_Fini();

    public boolean NET_ESTREAM_GetLastError();

    public boolean NET_ESTREAM_SetExceptionCallBack(int dwMessage, int hWnd, fExceptionCallBack cbExceptionCallBack, Pointer pUser);

    public boolean NET_ESTREAM_SetLogToFile(int iLogLevel, String strLogDir, boolean bAutoDel);

    //获取版本号
    public int NET_ESTREAM_GetBuildVersion();

    public int NET_ESTREAM_StartListenPreview(NET_EHOME_LISTEN_PREVIEW_CFG pListenParam);

    public int NET_ESTREAM_StartListenPlayBack(NET_EHOME_PLAYBACK_LISTEN_PARAM pListenParam);

    public boolean NET_ESTREAM_StopListenPreview(NativeLong iListenHandle);

    public boolean NET_ESTREAM_StopPreview(NativeLong iPreviewHandle);

    public boolean NET_ESTREAM_SetPreviewDataCB(NativeLong iHandle, NET_EHOME_PREVIEW_DATA_CB_PARAM pStruCBParam);
}
