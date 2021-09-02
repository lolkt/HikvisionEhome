package com.hikvision.ehome;

import com.sun.jna.Pointer;

public class FRegisterCallBack implements HCISUPCMS.DEVICE_REGISTER_CB {
    @Override
    public boolean invoke(int lUserID, int dwDataType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer, int dwInLen, Pointer pUser) {
        System.out.println("FRegisterCallBack, dwDataType:" + dwDataType + ", lUserID:" + lUserID);
        switch (dwDataType) {
            case 0:  //ENUM_DEV_ON
                HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12 strDevRegInfo = new HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12();
                strDevRegInfo.write();
                Pointer pDevRegInfo = strDevRegInfo.getPointer();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();
                HCISUPCMS.NET_EHOME_SERVER_INFO_V50 strEhomeServerInfo = new HCISUPCMS.NET_EHOME_SERVER_INFO_V50();
                strEhomeServerInfo.read();
                //strEhomeServerInfo.dwSize = strEhomeServerInfo.size();
                byte[] byIP = "10.0.0.109".getBytes();
                System.arraycopy(byIP, 0, strEhomeServerInfo.struUDPAlarmSever.szIP, 0, byIP.length);
                System.arraycopy(byIP, 0, strEhomeServerInfo.struTCPAlarmSever.szIP, 0, byIP.length);
                strEhomeServerInfo.dwAlarmServerType = 1; //报警服务器类型：0- 只支持UDP协议上报，1- 支持UDP、TCP两种协议上报 2-MQTT
                strEhomeServerInfo.struTCPAlarmSever.wPort = 7661;
                strEhomeServerInfo.struUDPAlarmSever.wPort = 7662;

                byte[] byClouldAccessKey = "test".getBytes();
                System.arraycopy(byClouldAccessKey, 0, strEhomeServerInfo.byClouldAccessKey, 0, byClouldAccessKey.length);
                byte[] byClouldSecretKey = "12345".getBytes();
                System.arraycopy(byClouldSecretKey, 0, strEhomeServerInfo.byClouldSecretKey, 0, byClouldSecretKey.length);

                strEhomeServerInfo.dwPicServerType = 3;
                System.arraycopy(byIP, 0, strEhomeServerInfo.struPictureSever.szIP, 0, byIP.length);
                strEhomeServerInfo.struPictureSever.wPort = 8089;
                strEhomeServerInfo.write();
                dwInLen = strEhomeServerInfo.size();
                pInBuffer.write(0, strEhomeServerInfo.getPointer().getByteArray(0, dwInLen), 0, dwInLen);

                System.out.println("Device online, DeviceID is:" + new String(strDevRegInfo.struRegInfo.byDeviceID).trim());
                Test1.lLoginID = lUserID;
                //     jTextFieldDevNo.setText(new String(strDevRegInfo.struRegInfo.byDeviceID));
                return true;
            case 3: //ENUM_DEV_AUTH
                strDevRegInfo = new HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12();
                strDevRegInfo.write();
                pDevRegInfo = strDevRegInfo.getPointer();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();
                String szEHomeKey = "hik12345";
                byte[] bs = szEHomeKey.getBytes();
                pInBuffer.write(0, bs, 0, szEHomeKey.length());
                break;
            case 4: //HCISUPCMS.ENUM_DEV_SESSIONKEY
                strDevRegInfo = new HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12();
                strDevRegInfo.write();
                pDevRegInfo = strDevRegInfo.getPointer();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();

                HCISUPCMS.NET_EHOME_DEV_SESSIONKEY struSessionKey = new HCISUPCMS.NET_EHOME_DEV_SESSIONKEY();
                System.arraycopy(strDevRegInfo.struRegInfo.byDeviceID, 0, struSessionKey.sDeviceID, 0, strDevRegInfo.struRegInfo.byDeviceID.length);
                System.arraycopy(strDevRegInfo.struRegInfo.bySessionKey, 0, struSessionKey.sSessionKey, 0, strDevRegInfo.struRegInfo.bySessionKey.length);
                struSessionKey.write();

                Pointer pSessionKey = struSessionKey.getPointer();

                Test1.hCEhomeCMS.NET_ECMS_SetDeviceSessionKey(pSessionKey);
                Test1.mHCEHomeAlarm.NET_EALARM_SetDeviceSessionKey(pSessionKey);
                break;
            case 5: //HCISUPCMS.ENUM_DEV_DAS_REQ
                String dasInfo = "{\n" +
                        "    \"Type\":\"DAS\",\n" +
                        "    \"DasInfo\":{\n" +
                        "        \"Address\":\"10.0.0.109\",\n" +
                        "        \"Domain\":\"\",\n" +
                        "        \"ServerID\":\"\",\n" +
                        "        \"Port\":7660,\n" +
                        "        \"UdpPort\":\n" +
                        "    }\n" +
                        "}";
                System.out.println(dasInfo);
                byte[] bs1 = dasInfo.getBytes();
                pInBuffer.write(0, bs1, 0, dasInfo.length());
                break;
            default:
                System.out.println("FRegisterCallBack default type:" + dwDataType);
                break;
        }
        return true;
    }
}
