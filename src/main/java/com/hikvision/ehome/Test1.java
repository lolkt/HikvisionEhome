package com.hikvision.ehome;


import java.io.File;
import java.io.FileOutputStream;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class Test1 {
    static HCISUPCMS hCEhomeCMS = HCISUPCMS.INSTANCE;
    static HCISUPSS hCEhomeSS = HCISUPSS.INSTANCE;
    static HCISUPStream hCEhomeStream = HCISUPStream.INSTANCE;
    static HCISUPAlarm mHCEHomeAlarm = HCISUPAlarm.INSTANCE;

    static int lLoginID = -1;
    static int m_lPlayBackLinkHandle = -1;   //回放句柄
    static int m_lPlayBackListenHandle = -1; //回放监听句柄
    static int lSessionID = -1;
    static int lHandle = -1;   //预览监听句柄

    HCISUPCMS.NET_EHOME_CMS_LISTEN_PARAM struCMSListenPara = new HCISUPCMS.NET_EHOME_CMS_LISTEN_PARAM();
    HCISUPCMS.NET_EHOME_XML_REMOTE_CTRL_PARAM struRemoteCtrl = new HCISUPCMS.NET_EHOME_XML_REMOTE_CTRL_PARAM();
    HCISUPStream.NET_EHOME_PLAYBACK_LISTEN_PARAM struPlayBackListen = new HCISUPStream.NET_EHOME_PLAYBACK_LISTEN_PARAM();
    HCISUPAlarm.NET_EHOME_ALARM_LISTEN_PARAM net_ehome_alarm_listen_param = new HCISUPAlarm.NET_EHOME_ALARM_LISTEN_PARAM();

    FRegisterCallBack fRegisterCallBack;//注册回调函数实现

    HCISUPCMS.BYTE_ARRAY m_struInbuffer;
    HCISUPCMS.BYTE_ARRAY m_struOutbuffer;
    HCISUPCMS.BYTE_ARRAY m_struStatusBuffer;

    HCISUPStream.NET_EHOME_LISTEN_PREVIEW_CFG struListen = new HCISUPStream.NET_EHOME_LISTEN_PREVIEW_CFG();

    String PSS_CLIENT_FILE_PATH_PARAM_NAME = "File-Path"; //图片文件路径
    String PSS_CLIENT_VRB_FILENAME_CODE = "Filename-Code";//VRB协议的FilenameCode
    String PSS_CLIENT_KMS_USER_NAME = "KMS-Username"; //KMS图片服务器用户名
    String PSS_CLIENT_KMS_PASSWORD = "KMS-Password"; //KMS图片服务器密码
    String PSS_CLIENT_CLOUD_AK_NAME = "Access-Key"; //云存储协议AcessKey
    String PSS_CLIENT_CLOUD_SK_NAME = "Secret-Key"; //云存储协议SecretKey
    String SS_STORAGE_PATH = "C:\\EhomePicServer"; //文件保存路径

    PSS_Message_Callback pSS_Message_Callback;// 信息回调函数(上报)
    PSS_Storage_Callback pSS_Storage_Callback;// 文件保存回调函数(下载)
    PLAYBACK_NEWLINK_CB fPLAYBACK_NEWLINK_CB; //回放监听回调函数实现
    PLAYBACK_DATA_CB fPLAYBACK_DATA_CB;   //回放回调实现
    FPREVIEW_NEWLINK_CB fPREVIEW_NEWLINK_CB;//预览监听回调函数实现
    FPREVIEW_DATA_CB fPREVIEW_DATA_CB;//预览回调函数实现
    EHomeMsgCallBack cbEHomeMsgCallBack;//报警监听回调函数实现

    HCISUPSS.NET_EHOME_SS_LISTEN_PARAM pSSListenParam = new HCISUPSS.NET_EHOME_SS_LISTEN_PARAM();
    int listenSS = -1;
    int client = -1;

    byte[] szUrl = new byte[HCISUPSS.MAX_URL_LEN_SS];
    String url = "";

    static int iCount = 0;
    static int Count = 0;

    File file = new File("F:\\mytest1234.mp4");
    FileOutputStream m_file = null;

    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        Test1 test = new Test1();
        //    test.ESTREAM_Init();
        test.EAlarm_Init();
        test.SS_Init();
        test.CMS_Init();

        while (lLoginID == -1) {
            Thread.sleep(1000);
        }
        while (true) {
            ;
        }
    }

    public void EAlarm_Init() {
        HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
        String strPathCrypto = ".\\lib\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径		
        System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
        ptrByteArrayCrypto.write();
        mHCEHomeAlarm.NET_EALARM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

        //设置libssl.so所在路径	
        HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
        String strPathSsl = ".\\lib\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
        System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
        ptrByteArraySsl.write();
        mHCEHomeAlarm.NET_EALARM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());

        boolean bRet = mHCEHomeAlarm.NET_EALARM_Init();
        if (!bRet) {
            System.out.println("NET_EALARM_Init failed!");
        }
        System.out.println("NET_EALARM_Init!");

        boolean logToFile = mHCEHomeAlarm.NET_EALARM_SetLogToFile(3, "C:\\javademo\\EHomeSDKLog", false);


        HCISUPAlarm.NET_EHOME_LOCAL_GENERAL_CFG m_struLocalCfg = new HCISUPAlarm.NET_EHOME_LOCAL_GENERAL_CFG();
        m_struLocalCfg.byAlarmPictureSeparate = 1;
        m_struLocalCfg.write();
        mHCEHomeAlarm.NET_EALARM_SetSDKLocalCfg(4, m_struLocalCfg.getPointer());

        if (cbEHomeMsgCallBack == null) {
            cbEHomeMsgCallBack = new EHomeMsgCallBack();
        }

        net_ehome_alarm_listen_param.struAddress.szIP = "10.0.0.109".getBytes();
        net_ehome_alarm_listen_param.struAddress.wPort = 7661;
        net_ehome_alarm_listen_param.fnMsgCb = cbEHomeMsgCallBack;
        net_ehome_alarm_listen_param.pUserData = null;
        net_ehome_alarm_listen_param.byProtocolType = 2; //协议类型：0- TCP，1- UDP, 2-MQTT
        net_ehome_alarm_listen_param.byUseCmsPort = 0; //是否复用CMS端口：0- 不复用，非0- 复用
        //如果复用cms端口，协议类型字段无效，此时AMS的本地监听信息struAddress填本地回环地址，
        //本地回环地址通过NET_ECMS_GetSDKLocalCfg、NET_ECMS_SetSDKLocalCfg获取和设置）

        int ARMListen = mHCEHomeAlarm.NET_EALARM_StartListen(net_ehome_alarm_listen_param);
        if (ARMListen < 0) {
            System.out.println("NET_EALARM_StartListen failed, error:" + hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {

            System.out.println("NET_EALARM_StartListen succeed");
        }
    }


    public void CMS_Init() {

        HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
        String strPathCrypto = System.getProperty("user.dir") + "\\lib\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
        System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
        ptrByteArrayCrypto.write();
        hCEhomeCMS.NET_ECMS_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

        //设置libssl.so所在路径
        HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
        String strPathSsl = System.getProperty("user.dir") + "\\lib\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
        System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
        ptrByteArraySsl.write();
        hCEhomeCMS.NET_ECMS_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());

        //设置HCAapSDKCom组件库文件夹所在路径
        HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
        String strPathCom = System.getProperty("user.dir") + "\\lib\\HCAapSDKCom";        //只支持绝对路径，建议使用英文路径
        System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
        ptrByteArrayCom.write();
        hCEhomeCMS.NET_ECMS_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());

        boolean binit = hCEhomeCMS.NET_ECMS_Init();

        if (fRegisterCallBack == null) {
            fRegisterCallBack = new FRegisterCallBack();
        }

        hCEhomeCMS.NET_ECMS_SetLogToFile(3, "C:\\javademo\\EHomeSDKLog", false);

        struCMSListenPara.struAddress.szIP = "10.0.0.109".getBytes();
        struCMSListenPara.struAddress.wPort = 7660;
        struCMSListenPara.fnCB = fRegisterCallBack;

        //启动监听，接收设备注册信息
        NativeLong lListen = hCEhomeCMS.NET_ECMS_StartListen(struCMSListenPara);
        if (lListen.longValue() < -1) {
            System.out.println("NET_ECMS_StartListen failed, error code:" + hCEhomeCMS.NET_ECMS_GetLastError());
            hCEhomeCMS.NET_ECMS_Fini();
            return;
        }
        System.out.println("NET_ECMS_StartListen succeed!");

    }


    public void CMS_ISAPIPassThrough() {
        HCISUPCMS.NET_EHOME_PTXML_PARAM m_struParam = new HCISUPCMS.NET_EHOME_PTXML_PARAM();
        m_struParam.read();

        String url = "GET /ISAPI/ContentMgmt/InputProxy/channels";
        HCISUPCMS.BYTE_ARRAY ptrUrl = new HCISUPCMS.BYTE_ARRAY(url.length());
        System.arraycopy(url.getBytes(), 0, ptrUrl.byValue, 0, url.length());
        ptrUrl.write();
        m_struParam.pRequestUrl = ptrUrl.getPointer();
        m_struParam.dwRequestUrlLen = url.length();

        HCISUPCMS.BYTE_ARRAY ptrOutByte = new HCISUPCMS.BYTE_ARRAY(10 * 1024);
        m_struParam.pOutBuffer = ptrOutByte.getPointer();
        m_struParam.dwOutSize = 10 * 1024;
        m_struParam.write();

        if (!hCEhomeCMS.NET_ECMS_ISAPIPassThrough(lLoginID, m_struParam)) {
            int iErr = hCEhomeCMS.NET_ECMS_GetLastError();
            System.out.println("NET_ECMS_ISAPIPassThrough failed,error：" + iErr);
            return;
        } else {
            m_struParam.read();
            ptrOutByte.read();
            System.out.println("NET_ECMS_ISAPIPassThrough succeed\n" + "ptrOutByte:" + new String(ptrOutByte.byValue).trim());
        }


    }


    public void CMS_XMLRemoteControl() {
        m_struOutbuffer = new HCISUPCMS.BYTE_ARRAY(2 * 1024);
        m_struStatusBuffer = new HCISUPCMS.BYTE_ARRAY(2 * 1024);

        struRemoteCtrl = new HCISUPCMS.NET_EHOME_XML_REMOTE_CTRL_PARAM();
        struRemoteCtrl.read();

        struRemoteCtrl.dwSize = struRemoteCtrl.size();
        struRemoteCtrl.dwRecvTimeOut = 5000;
        struRemoteCtrl.dwSendTimeOut = 5000;

        String inputCfg = "<?xml version=\"1.0\" encoding=\"GB2312\"?><PPVSPMessage><Version>2.5</Version><Sequence>1</Sequence><Method>CONTROL</Method><CommandType>REQUEST</CommandType><Command>CLEARACSPARAM</Command><Params><paramType>13</paramType></Params></PPVSPMessage>";

        m_struInbuffer = new HCISUPCMS.BYTE_ARRAY(inputCfg.length());
        System.arraycopy(inputCfg.getBytes(), 0, m_struInbuffer.byValue, 0, inputCfg.length());
        m_struInbuffer.write();

        struRemoteCtrl.lpInbuffer = m_struInbuffer.getPointer();
        struRemoteCtrl.dwInBufferSize = m_struInbuffer.size();
        struRemoteCtrl.lpOutBuffer = m_struOutbuffer.getPointer();
        struRemoteCtrl.dwOutBufferSize = m_struOutbuffer.size();
        struRemoteCtrl.lpStatusBuffer = m_struStatusBuffer.getPointer();
        struRemoteCtrl.dwStatusBufferSize = m_struStatusBuffer.size();
        struRemoteCtrl.write();


        if (!hCEhomeCMS.NET_ECMS_XMLRemoteControl(lLoginID, struRemoteCtrl, struRemoteCtrl.size())) {
            int iErr = hCEhomeCMS.NET_ECMS_GetLastError();
            System.out.println("NET_ECMS_XMLConfig failed,error：" + iErr);
            return;
        } else {
            struRemoteCtrl.read();
            m_struOutbuffer.read();
            m_struStatusBuffer.read();

            System.out.println("NET_ECMS_XMLConfig succeed：");
            System.out.println("lpOutBuffer:" + new String(m_struOutbuffer.byValue).trim());
            System.out.println("lpStatusBuffer:" + new String(m_struStatusBuffer.byValue).trim());
        }


    }


    public void ESTREAM_Init() {
        hCEhomeStream.NET_ESTREAM_Init();
        hCEhomeStream.NET_ESTREAM_SetLogToFile(3, "C:\\javademo\\EHomeSDKLog", false);
        //设置libcrypto.so所在路径	
        HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
        String strPathCrypto = System.getProperty("user.dir") + "\\lib\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
        System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
        ptrByteArrayCrypto.write();
        if (!hCEhomeStream.NET_ESTREAM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer())) {
            System.out.println("NET_ESTREAM_SetSDKInitCfg 0 failed, error:" + hCEhomeStream.NET_ESTREAM_GetLastError());
        } else {
            System.out.println("NET_ESTREAM_SetSDKInitCfg 0 succeed");
        }

        //设置libssl.so所在路径
        HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
        String strPathSsl = System.getProperty("user.dir") + "\\lib\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
        System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
        ptrByteArraySsl.write();
        if (!hCEhomeStream.NET_ESTREAM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer())) {
            System.out.println("NET_ESTREAM_SetSDKInitCfg 1 failed, error:" + hCEhomeStream.NET_ESTREAM_GetLastError());
        } else {
            System.out.println("NET_ESTREAM_SetSDKInitCfg 1 succeed");
        }

        //设置HCAapSDKCom组件库文件夹所在路径
        HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
        String strPathCom = System.getProperty("user.dir") + "\\lib\\HCAapSDKCom";      //只支持绝对路径，建议使用英文路径
        System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
        ptrByteArrayCom.write();
        if (!hCEhomeStream.NET_ESTREAM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer())) {
            System.out.println("NET_ESTREAM_SetSDKLocalCfg 5 failed, error:" + hCEhomeStream.NET_ESTREAM_GetLastError());
        } else {
            System.out.println("NET_ESTREAM_SetSDKLocalCfg 5 succeed");
        }


        //预览监听
        if (fPREVIEW_NEWLINK_CB == null) {
            fPREVIEW_NEWLINK_CB = new FPREVIEW_NEWLINK_CB();
        }

        struListen.struIPAdress.szIP = "10.0.0.109".getBytes();
        struListen.struIPAdress.wPort = 8001; //流媒体服务器监听端口
        struListen.fnNewLinkCB = fPREVIEW_NEWLINK_CB; //预览连接请求回调函数
        struListen.pUser = null;
        struListen.byLinkMode = 0; //0- TCP方式，1- UDP方式

        if (lHandle < 0) {
            lHandle = hCEhomeStream.NET_ESTREAM_StartListenPreview(struListen);
            if (lHandle == -1) {

                System.out.println("NET_ESTREAM_StartListenPreview failed, error code:" + hCEhomeStream.NET_ESTREAM_GetLastError());
                hCEhomeStream.NET_ESTREAM_Fini();
                return;
            } else {
                System.out.println("NET_ESTREAM_StartListenPreview succeed");
            }
        }


        //回放监听
        if (fPLAYBACK_NEWLINK_CB == null) {
            fPLAYBACK_NEWLINK_CB = new PLAYBACK_NEWLINK_CB();
        }

        struPlayBackListen.struIPAdress.szIP = "10.0.0.109".getBytes();
        struPlayBackListen.struIPAdress.wPort = 8888; //流媒体服务器监听端口
        struPlayBackListen.fnNewLinkCB = fPLAYBACK_NEWLINK_CB;
        struPlayBackListen.byLinkMode = 0; //0- TCP方式，1- UDP方式
        if (m_lPlayBackLinkHandle < 0) {
            m_lPlayBackListenHandle = hCEhomeStream.NET_ESTREAM_StartListenPlayBack(struPlayBackListen);
            if (m_lPlayBackListenHandle < -1) {
                System.out.println("NET_ESTREAM_StartListenPlayBack failed, error code:" + hCEhomeStream.NET_ESTREAM_GetLastError());
                hCEhomeStream.NET_ESTREAM_Fini();
                return;
            } else {
                System.out.println("NET_ESTREAM_StartListenPlayBack succeed");
            }
        }
    }


    public void RealPlay() {
        HCISUPCMS.NET_EHOME_PREVIEWINFO_IN struPreviewIn = new HCISUPCMS.NET_EHOME_PREVIEWINFO_IN();
        struPreviewIn.iChannel = 1; //通道号
        struPreviewIn.dwLinkMode = 0; //0- TCP方式，1- UDP方式
        struPreviewIn.dwStreamType = 0; //码流类型：0- 主码流，1- 子码流, 2- 第三码流
        struPreviewIn.struStreamSever.szIP = "10.0.0.109".getBytes();//流媒体服务器IP地址
        struPreviewIn.struStreamSever.wPort = 8001; //流媒体服务器端口，需要跟服务器启动监听端口一致

        //预览请求

        HCISUPCMS.NET_EHOME_PREVIEWINFO_OUT struPreviewOut = new HCISUPCMS.NET_EHOME_PREVIEWINFO_OUT();
        boolean getRS = hCEhomeCMS.NET_ECMS_StartGetRealStream(lLoginID, struPreviewIn, struPreviewOut);
        //Thread.sleep(10000);
        if (!hCEhomeCMS.NET_ECMS_StartGetRealStream(lLoginID, struPreviewIn, struPreviewOut)) {

            //hCEhomeCMS.NET_ECMS_Fini();
            System.out.println("NET_ECMS_StartGetRealStream failed, error code:" + hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            struPreviewOut.read();

            //        JOptionPane.showMessageDialog(null, "NET_ECMS_StartGetRealStream预览请求成功!");
            System.out.println("NET_ECMS_StartGetRealStream预览请求成功, sessionID:" + struPreviewOut.lSessionID);
        }
        HCISUPCMS.NET_EHOME_PUSHSTREAM_IN struPushInfoIn = new HCISUPCMS.NET_EHOME_PUSHSTREAM_IN();
        struPushInfoIn.read();
        struPushInfoIn.dwSize = struPushInfoIn.size();
        struPushInfoIn.lSessionID = struPreviewOut.lSessionID;
        struPushInfoIn.write();

        HCISUPCMS.NET_EHOME_PUSHSTREAM_OUT struPushInfoOut = new HCISUPCMS.NET_EHOME_PUSHSTREAM_OUT();
        struPushInfoOut.read();
        struPushInfoOut.dwSize = struPushInfoOut.size();
        struPushInfoOut.write();
        if (!hCEhomeCMS.NET_ECMS_StartPushRealStream(lLoginID, struPushInfoIn, struPushInfoOut)) {
            System.out.println("NET_ECMS_StartPushRealStream failed, error code:" + hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            System.out.println("NET_ECMS_StartPushRealStream succeed, sessionID:" + struPushInfoIn.lSessionID);
        }


    }


    public void ESTREAM_PlayBackByTime() {

        HCISUPCMS.NET_EHOME_PLAYBACK_INFO_IN m_struPlayBackInfoIn = new HCISUPCMS.NET_EHOME_PLAYBACK_INFO_IN();
        m_struPlayBackInfoIn.read();
        m_struPlayBackInfoIn.dwSize = m_struPlayBackInfoIn.size();
        m_struPlayBackInfoIn.dwChannel = 1;
        m_struPlayBackInfoIn.byPlayBackMode = 1;//0- 按文件名回放，1- 按时间回放
        m_struPlayBackInfoIn.unionPlayBackMode.setType(HCISUPCMS.NET_EHOME_PLAYBACKBYTIME.class);

        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.wYear = 2021;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byMonth = 4;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byDay = 8;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byHour = 10;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byMinute = 0;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.bySecond = 0;

        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.wYear = 2021;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byMonth = 4;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byDay = 8;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byHour = 30;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byMinute = 15;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.bySecond = 0;

        m_struPlayBackInfoIn.struStreamSever.szIP = "10.0.0.109".getBytes();
        m_struPlayBackInfoIn.struStreamSever.wPort = 8888;
        m_struPlayBackInfoIn.write();


        HCISUPCMS.NET_EHOME_PLAYBACK_INFO_OUT m_struPlayBackInfoOut = new HCISUPCMS.NET_EHOME_PLAYBACK_INFO_OUT();
        m_struPlayBackInfoOut.write();
        if (!hCEhomeCMS.NET_ECMS_StartPlayBack(lLoginID, m_struPlayBackInfoIn, m_struPlayBackInfoOut)) {
            System.out.println("NET_ECMS_StartPlayBack failed, error code:" + hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            m_struPlayBackInfoOut.read();
            System.out.println("NET_ECMS_StartPlayBack succeed, lSessionID:" + m_struPlayBackInfoOut.lSessionID);
        }

        HCISUPCMS.NET_EHOME_PUSHPLAYBACK_IN m_struPushPlayBackIn = new HCISUPCMS.NET_EHOME_PUSHPLAYBACK_IN();
        m_struPushPlayBackIn.read();
        m_struPushPlayBackIn.dwSize = m_struPushPlayBackIn.size();
        m_struPushPlayBackIn.lSessionID = m_struPlayBackInfoOut.lSessionID;
        m_struPushPlayBackIn.write();

        lSessionID = m_struPushPlayBackIn.lSessionID;

        HCISUPCMS.NET_EHOME_PUSHPLAYBACK_OUT m_struPushPlayBackOut = new HCISUPCMS.NET_EHOME_PUSHPLAYBACK_OUT();
        m_struPushPlayBackOut.read();
        m_struPushPlayBackOut.dwSize = m_struPushPlayBackOut.size();
        m_struPushPlayBackOut.write();

        if (!hCEhomeCMS.NET_ECMS_StartPushPlayBack(lLoginID, m_struPushPlayBackIn, m_struPushPlayBackOut)) {
            System.out.println("NET_ECMS_StartPushPlayBack failed, error code:" + hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            System.out.println("NET_ECMS_StartPushPlayBack succeed, sessionID:" + m_struPushPlayBackIn.lSessionID + ",lUserID:" + lLoginID);
        }
    }


    //测试暂停
    public void testPause() {
        HCISUPCMS.NET_EHOME_PLAYBACK_PAUSE_RESTART_PARAM struPlaybackPauseParam = new HCISUPCMS.NET_EHOME_PLAYBACK_PAUSE_RESTART_PARAM();
        struPlaybackPauseParam.read();
        struPlaybackPauseParam.lSessionID = lSessionID;
        struPlaybackPauseParam.write();

        if (!hCEhomeCMS.NET_ECMS_PlayBackOperate(lLoginID, 0, struPlaybackPauseParam.getPointer())) {
            System.out.println("NET_ECMS_PlayBackOperate failed, error code:" + hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            System.out.println("NET_ECMS_PlayBackOperate succeed, sessionID:" + lSessionID);
        }


    }


    public class FPREVIEW_NEWLINK_CB implements HCISUPStream.PREVIEW_NEWLINK_CB {
        @Override
        public boolean invoke(NativeLong lLinkHandle, HCISUPStream.NET_EHOME_NEWLINK_CB_MSG pNewLinkCBMsg, Pointer pUserData) {
            System.out.println("FPREVIEW_NEWLINK_CB callback");

            //预览数据回调参数
            HCISUPStream.NET_EHOME_PREVIEW_DATA_CB_PARAM struDataCB = new HCISUPStream.NET_EHOME_PREVIEW_DATA_CB_PARAM();
            if (fPREVIEW_DATA_CB == null) {
                fPREVIEW_DATA_CB = new FPREVIEW_DATA_CB();
            }
            struDataCB.fnPreviewDataCB = fPREVIEW_DATA_CB;

            if (!hCEhomeStream.NET_ESTREAM_SetPreviewDataCB(lLinkHandle, struDataCB)) {
                System.out.println("NET_ESTREAM_SetPreviewDataCB()错误代码号：" + hCEhomeStream.NET_ESTREAM_GetLastError());
                return false;
            }
            return true;
        }
    }

    public class FPREVIEW_DATA_CB implements HCISUPStream.PREVIEW_DATA_CB {
        //实时流回调函数/
        @Override
        public void invoke(NativeLong iPreviewHandle, HCISUPStream.NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData) {
            if (Count == 50) {//降低打印频率
                System.out.println("FPREVIEW_DATA_CB callback, data length:" + pPreviewCBMsg.dwDataLen);
                Count = 0;
            }
            Count++;

//            try {
//                m_file1 = new FileOutputStream(file1, true);
//                long offset = 0;
//                 ByteBuffer buffers = pPreviewCBMsg.pRecvdata.getByteBuffer(offset, pPreviewCBMsg.dwDataLen);
//                 byte [] bytes = new byte[pPreviewCBMsg.dwDataLen];
//                 buffers.rewind();
//                 buffers.get(bytes);                
//                 m_file1.write(bytes);             
//                 m_file1.close();                  
//            } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }      
        }
    }


    public class PLAYBACK_NEWLINK_CB implements HCISUPStream.PLAYBACK_NEWLINK_CB {
        @Override
        public boolean invoke(int lPlayBackLinkHandle, HCISUPStream.NET_EHOME_PLAYBACK_NEWLINK_CB_INFO pNewLinkCBInfo, Pointer pUserData) {
            pNewLinkCBInfo.read();
            System.out.println("PLAYBACK_NEWLINK_CB callback, szDeviceID:" + new String(pNewLinkCBInfo.szDeviceID).trim()
                    + ",lSessionID:" + pNewLinkCBInfo.lSessionID + ",dwChannelNo:" + pNewLinkCBInfo.dwChannelNo);
            m_lPlayBackLinkHandle = lPlayBackLinkHandle;


            //预览数据回调参数
            if (fPLAYBACK_DATA_CB == null) {
                fPLAYBACK_DATA_CB = new PLAYBACK_DATA_CB();
            }
            pNewLinkCBInfo.fnPlayBackDataCB = fPLAYBACK_DATA_CB;

            pNewLinkCBInfo.write();

            return true;
        }
    }

    public class PLAYBACK_DATA_CB implements HCISUPStream.PLAYBACK_DATA_CB {
        //实时流回调函数
        @Override
        public boolean invoke(int iPlayBackLinkHandle, HCISUPStream.NET_EHOME_PLAYBACK_DATA_CB_INFO pDataCBInfo, Pointer pUserData) {
            if (iCount == 50) {//降低打印频率
                System.out.println("PLAYBACK_DATA_CB callback , dwDataLen:" + pDataCBInfo.dwDataLen + ",dwType:" + pDataCBInfo.dwType);
                iCount = 0;
            }
            iCount++;
//			try {
//				m_file = new FileOutputStream(file, true);
//				long offset = 0;
//	             ByteBuffer buffers = pDataCBInfo.pData.getByteBuffer(offset, pDataCBInfo.dwDataLen);
//	             byte [] bytes = new byte[pDataCBInfo.dwDataLen];
//	             buffers.rewind();
//	             buffers.get(bytes);	             
//	             m_file.write(bytes);				
//	             m_file.close();		    		
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}


            return true;
        }
    }


    public void SS_Init() {
        HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
        String strPathCrypto = ".\\lib\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径		
        System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
        ptrByteArrayCrypto.write();
        hCEhomeSS.NET_ESS_SetSDKInitCfg(4, ptrByteArrayCrypto.getPointer());

        //设置libssl.so所在路径	
        HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
        String strPathSsl = ".\\lib\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
        System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
        ptrByteArraySsl.write();
        hCEhomeSS.NET_ESS_SetSDKInitCfg(5, ptrByteArraySsl.getPointer());

        boolean sinit = hCEhomeSS.NET_ESS_Init();
        if (!sinit) {
            System.out.println("NET_ESS_Init失败!");
        }


        boolean logToFile = hCEhomeSS.NET_ESS_SetLogToFile(3, "C:\\javademo\\EHomeSDKLog", false);

        if (pSS_Message_Callback == null) {
            pSS_Message_Callback = new PSS_Message_Callback();
        }

        if (pSS_Storage_Callback == null) {
            pSS_Storage_Callback = new PSS_Storage_Callback();
        }

        String strIP = "10.0.0.109";
        System.arraycopy(strIP.getBytes(), 0, pSSListenParam.struAddress.szIP, 0, strIP.length());


        pSSListenParam.struAddress.wPort = 8089;

        String strKMS_UserName = "test";
        System.arraycopy(strKMS_UserName.getBytes(), 0, pSSListenParam.szKMS_UserName, 0, strKMS_UserName.length());

        String strKMS_Password = "12345";
        System.arraycopy(strKMS_Password.getBytes(), 0, pSSListenParam.szKMS_Password, 0, strKMS_Password.length());

        String strAccessKey = "test";
        System.arraycopy(strAccessKey.getBytes(), 0, pSSListenParam.szAccessKey, 0, strAccessKey.length());


        String strSecretKey = "12345";
        System.arraycopy(strSecretKey.getBytes(), 0, pSSListenParam.szSecretKey, 0, strSecretKey.length());

        pSSListenParam.byHttps = 0;
        pSSListenParam.fnSSMsgCb = pSS_Message_Callback;
        pSSListenParam.fnSStorageCb = pSS_Storage_Callback;
        pSSListenParam.bySecurityMode = 1;
        pSSListenParam.write();

        listenSS = hCEhomeSS.NET_ESS_StartListen(pSSListenParam);
        if (listenSS == -1) {
            int err = hCEhomeSS.NET_ESS_GetLastError();
            System.out.println("NET_ESS_StartListen failed,error:" + err);
            return;
        } else {
            System.out.println("NET_ESS_StartListen succeed!");
        }

    }


    public void SS_CreateClient() {
        String filePath = "D:\\01.jpg";
        HCISUPSS.NET_EHOME_SS_CLIENT_PARAM pClientParam = new HCISUPSS.NET_EHOME_SS_CLIENT_PARAM();
        pClientParam.enumType = HCISUPSS.NET_EHOME_SS_CLIENT_TYPE_KMS;
        pClientParam.struAddress.szIP = "10.0.0.109".getBytes();
        pClientParam.struAddress.wPort = 8089;
        pClientParam.byHttps = 0;
        pClientParam.write();

        client = hCEhomeSS.NET_ESS_CreateClient(pClientParam);
        if (client < 0) {
            int err = hCEhomeSS.NET_ESS_GetLastError();
            System.out.println("创建图片上传/下载客户端出错,错误号：" + err + "  ,client=" + client);
            return;
        } else {
            if (!hCEhomeSS.NET_ESS_ClientSetTimeout(client, 6000, 6000)) {
                int err = hCEhomeSS.NET_ESS_GetLastError();
                System.out.println("NET_ESS_ClientSetTimeout失败,错误号：" + err);
            }
            boolean bSetParam = hCEhomeSS.NET_ESS_ClientSetParam(client, PSS_CLIENT_FILE_PATH_PARAM_NAME, filePath);

            boolean bKMS_UserName = hCEhomeSS.NET_ESS_ClientSetParam(client, PSS_CLIENT_KMS_USER_NAME, "test");
            boolean bKMS_PassWord = hCEhomeSS.NET_ESS_ClientSetParam(client, PSS_CLIENT_KMS_PASSWORD, "12345");
            boolean bCloud_AK = hCEhomeSS.NET_ESS_ClientSetParam(client, PSS_CLIENT_CLOUD_AK_NAME, "test");
            boolean bCloud_SK = hCEhomeSS.NET_ESS_ClientSetParam(client, PSS_CLIENT_CLOUD_SK_NAME, "12345");


        }
    }

    public void SS_UploadPic() {
        iCount++;
        szUrl = new byte[HCISUPSS.MAX_URL_LEN_SS];

        boolean doUpload = hCEhomeSS.NET_ESS_ClientDoUpload(client, szUrl, HCISUPSS.MAX_URL_LEN_SS - 1);
        if (!doUpload) {
            int err = hCEhomeSS.NET_ESS_GetLastError();
            System.out.println("NET_ESS_ClientDoUpload失败，错误号：" + err);
        } else {
            url = "http://10.0.0.109" + ":" + 8089 + new String(szUrl).trim();
            System.err.println("NET_ESS_ClientDoUpload succeed, Count:" + iCount + ",Pic Url:" + url);
        }
    }

    public void SS_DestroyClient() {
        if (hCEhomeSS.NET_ESS_DestroyClient(client))//释放资源
        {
            client = -1;
        }
    }


}//Test1  Class结束
