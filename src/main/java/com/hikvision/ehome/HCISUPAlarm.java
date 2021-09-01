package com.hikvision.ehome;


import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.GDI32.RECT;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.win32.StdCallLibrary;

//SDK接口说明,HCNetSDK.dll

//windows gdi接口,gdi32.dll in system32 folder, 在设置遮挡区域,移动侦测区域等情况下使用
public interface HCISUPAlarm extends StdCallLibrary {

    HCISUPAlarm INSTANCE = (HCISUPAlarm) Native.loadLibrary(".\\lib\\HCISUPAlarm",
            HCISUPAlarm.class);
    /***
     * 宏定义
     ***/
    //常量
    public static final int MAX_DEVICE_ID_LEN = 256;    //设备ID长度
    public static final int NET_EHOME_SERIAL_LEN = 12;  //设备序列号长度

    public static class NET_EHOME_IPADDRESS extends Structure {
        public byte[] szIP = new byte[128];
        public short wPort;     //端口
        public byte[] byRes = new byte[2];
    }

    public static class BYTE_ARRAY extends Structure {
        public byte[] byValue;

        public BYTE_ARRAY(int iLen) {
            byValue = new byte[iLen];
        }
    }

    public static class NET_EHOME_ALARM_MSG extends Structure {
        public int dwAlarmType;      //报警类型，见EN_ALARM_TYPE
        public Pointer pAlarmInfo;       //报警内容（结构体）
        public int dwAlarmInfoLen;   //结构体报警内容长度
        public Pointer pXmlBuf;          //报警内容（XML）
        public int dwXmlBufLen;      //xml报警内容长度
        public byte[] sSerialNumber = new byte[NET_EHOME_SERIAL_LEN]; //设备序列号，用于进行Token认证
        public byte[] byRes = new byte[20];
    }

    public static class NET_EHOME_ALARM_ISAPI_INFO extends Structure {
        public Pointer pAlarmData;           // 报警数据
        public int dwAlarmDataLen;   // 报警数据长度
        public byte byDataType;        // 0-invalid,1-xml,2-json
        public byte byPicturesNumber;  // 图片数量
        public byte[] byRes = new byte[2];
        public Pointer pPicPackData;         // 图片变长部分,byPicturesNumber个NET_EHOME_ALARM_ISAPI_PICDATA
        public byte[] byRes1 = new byte[32];
    }


    public static interface EHomeMsgCallBack extends StdCallCallback {
        public boolean invoke(NativeLong iHandle, NET_EHOME_ALARM_MSG pAlarmMsg, Pointer pUser);
    }

    public static class NET_EHOME_ALARM_LISTEN_PARAM extends Structure {
        public NET_EHOME_IPADDRESS struAddress;
        public EHomeMsgCallBack fnMsgCb; //报警信息回调函数
        public Pointer pUserData;   //用户数据
        public byte byProtocolType;    //协议类型，0-TCP,1-UDP
        public byte byUseCmsPort; //是否复用CMS端口,0-不复用，非0-复用，如果复用cms端口，协议类型字段无效（此时本地监听信息struAddress填本地回环地址）
        public byte byUseThreadPool;  //0-回调报警时，使用线程池，1-回调报警时，不使用线程池，默认情况下，报警回调的时候，使用线程池
        public byte byRes[] = new byte[29];
    }

    public static class NET_EHOME_LOCAL_GENERAL_CFG extends Structure {
        public byte byAlarmPictureSeparate;        //控制透传ISAPI报警数据和图片是否分离，0-不分离，1-分离（分离后走EHOME_ISAPI_ALARM回调返回）
        public byte[] byRes = new byte[127];
    }

    //初始化，反初始化
    boolean NET_EALARM_Init();

    boolean NET_EALARM_Fini();

    boolean NET_EALARM_SetSDKLocalCfg(int enumType, Pointer lpInbuffer);

    int NET_EALARM_StartListen(NET_EHOME_ALARM_LISTEN_PARAM pAlarmListenParam);

    boolean NET_EALARM_SetDeviceSessionKey(Pointer pDeviceKey);

    boolean NET_EALARM_SetLogToFile(int iLogLevel, String strLogDir, boolean bAutoDel);

    boolean NET_EALARM_SetSDKInitCfg(int enumType, Pointer lpInBuff);
}

//windows user32接口,user32.dll in system32 folder, 在设置遮挡区域,移动侦测区域等情况下使用
interface USER32 extends W32API {

    USER32 INSTANCE = (USER32) Native.loadLibrary("user32", USER32.class, DEFAULT_OPTIONS);

    public static final int BF_LEFT = 0x0001;
    public static final int BF_TOP = 0x0002;
    public static final int BF_RIGHT = 0x0004;
    public static final int BF_BOTTOM = 0x0008;
    public static final int BDR_SUNKENOUTER = 0x0002;
    public static final int BF_RECT = (BF_LEFT | BF_TOP | BF_RIGHT | BF_BOTTOM);

    boolean DrawEdge(HDC hdc, RECT qrc, int edge, int grfFlags);

    int FillRect(HDC hDC, RECT lprc, HANDLE hbr);
}
