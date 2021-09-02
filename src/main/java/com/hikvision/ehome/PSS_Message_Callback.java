package com.hikvision.ehome;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class PSS_Message_Callback implements HCISUPSS.EHomeSSMsgCallBack {

    @Override
    public boolean invoke(NativeLong iHandle, int enumType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer,
                          int dwInLen, Pointer pUser) {
        if (1 == enumType) {
            HCISUPSS.NET_EHOME_SS_TOMCAT_MSG pTomcatMsg = new HCISUPSS.NET_EHOME_SS_TOMCAT_MSG();
            String szDevUri = new String(pTomcatMsg.szDevUri).trim();
            int dwPicNum = pTomcatMsg.dwPicNum;
            String pPicURLs = pTomcatMsg.pPicURLs;
            System.out.println("szDevUri = " + szDevUri + "   dwPicNum= " + dwPicNum + "   pPicURLs=" + pPicURLs);
        } else if (2 == enumType) {

            //				int type = pInBuffer.dwAlarmServerType;
            //				int picServerType = pInBuffer.dwPicServerType;
            //				System.out.println("type=" + type + "   picType=" + picServerType);

        } else if (3 == enumType) {

        }
        return true;
    }
}
