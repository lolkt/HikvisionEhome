package com.hikvision.ehome;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.ptr.NativeLongByReference;

//播放库函数声明,PlayCtrl.dll
public interface PlayCtrl extends StdCallLibrary {
    PlayCtrl INSTANCE = (PlayCtrl) Native.loadLibrary(".\\lib\\PlayCtrl", PlayCtrl.class);

    int STREAME_REALTIME = 0;
    int STREAME_FILE = 1;
    int T_UYVY = 1;
    int T_YV12 = 3;
    int T_RGB32 = 7;

    boolean PlayM4_GetPort(NativeLongByReference nPort);

    boolean PlayM4_OpenStream(NativeLong nPort, Pointer pFileHeadBuf, int nSize, int nBufPoolSize);

    boolean PlayM4_InputData(NativeLong nPort, Pointer pBuf, int nSize);

    boolean PlayM4_CloseStream(NativeLong nPort);

    boolean PlayM4_SetStreamOpenMode(NativeLong nPort, int nMode);

    boolean PlayM4_Play(NativeLong nPort, W32API.HWND hWnd);

    boolean PlayM4_Stop(NativeLong nPort);

    boolean PlayM4_FreePort(NativeLong nPort);

    boolean PlayM4_SetSecretKey(NativeLong nPort, NativeLong lKeyType, String pSecretKey, NativeLong lKeyLen);

    boolean PlayM4_GetJPEG(NativeLong nPort, byte[] pJpeg, int nBufSize, IntByReference pJpegSize);

    boolean PlayM4_SetDecCallBack(NativeLong nPort, DecCallBack decCBFun);

    boolean PlayM4_SetDecCallBackMend(NativeLong nPort, DecCallBack decCBFun, long nUser);

    boolean PlayM4_SetDecCallBackExMend(NativeLong nPort, DecCallBack decCBFun, Pointer pDest, long nDestSize, long nUser);

    int PlayM4_GetLastError(NativeLong nPort);

    interface DecCallBack extends StdCallCallback {
        void invoke(NativeLong nPort, Pointer pBuf, int nSize, FRAME_INFO pFrameInfo, NativeLong nReserved1, NativeLong nReserved2);
    }


    class FRAME_INFO extends Structure {
        public NativeLong nWidth;                   /* 画面宽，单位像素。如果是音频数据，则为音频声道数 */
        public NativeLong nHeight;                     /* 画面高，单位像素。如果是音频数据，则为样位率 */
        public NativeLong nStamp;                           /* 时标信息，单位毫秒 */
        public NativeLong nType;                            /* 数据类型，T_AUDIO16, T_RGB32, T_YV12 */
        public NativeLong nFrameRate;                /* 编码时产生的图像帧率，如果是音频数据则为采样率 */
        public int dwFrameNum;                      /* 帧号 */
    }
}