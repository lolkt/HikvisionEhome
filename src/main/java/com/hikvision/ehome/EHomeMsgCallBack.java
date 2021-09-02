package com.hikvision.ehome;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * 报警监听回调函数实现
 * @author maq
 */
public class EHomeMsgCallBack implements HCISUPAlarm.EHomeMsgCallBack {
    @Override
    public boolean invoke(NativeLong iHandle, HCISUPAlarm.NET_EHOME_ALARM_MSG pAlarmMsg, Pointer pUser) {
        System.out.println(new String("EHomeMsgCallBack: ") + pAlarmMsg.dwAlarmType + ",dwAlarmInfoLen:" + pAlarmMsg.dwAlarmInfoLen + ",dwXmlBufLen:" + pAlarmMsg.dwXmlBufLen);
        if (pAlarmMsg.dwXmlBufLen != 0) {
            HCISUPAlarm.BYTE_ARRAY strXMLData = new HCISUPAlarm.BYTE_ARRAY(pAlarmMsg.dwXmlBufLen);
            strXMLData.write();
            Pointer pPlateInfo = strXMLData.getPointer();
            pPlateInfo.write(0, pAlarmMsg.pXmlBuf.getByteArray(0, strXMLData.size()), 0, strXMLData.size());
            strXMLData.read();

            String strXML = new String(strXMLData.byValue).trim();
            System.out.println(strXML + "aaaaaaaaaaaa");

        }
        switch (pAlarmMsg.dwAlarmType) {
            case 13:
                if (pAlarmMsg.pAlarmInfo != null) {
                    HCISUPAlarm.NET_EHOME_ALARM_ISAPI_INFO strISAPIData = new HCISUPAlarm.NET_EHOME_ALARM_ISAPI_INFO();
                    strISAPIData.write();
                    Pointer pISAPIInfo = strISAPIData.getPointer();
                    pISAPIInfo.write(0, pAlarmMsg.pAlarmInfo.getByteArray(0, strISAPIData.size()), 0, strISAPIData.size());
                    strISAPIData.read();
                    if (strISAPIData.dwAlarmDataLen != 0)//Json或者XML数据
                    {
                        HCISUPAlarm.BYTE_ARRAY m_strISAPIData = new HCISUPAlarm.BYTE_ARRAY(strISAPIData.dwAlarmDataLen);
                        m_strISAPIData.write();
                        Pointer pPlateInfo = m_strISAPIData.getPointer();
                        pPlateInfo.write(0, strISAPIData.pAlarmData.getByteArray(0, m_strISAPIData.size()), 0, m_strISAPIData.size());
                        m_strISAPIData.read();
                        System.out.println(new String(m_strISAPIData.byValue).trim() + "bbbbbbbbbb");
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }
}
