package com.emdoor.douyindownloadtool;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

public class PlayvideoUtil {

    /***
     * 根据播放路径设置缩略图
     * @param filePath 视频资源的路径
     * @return 返回缩略图的Bitmap对象
     */
    public Bitmap getVideoThumbNail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            try {
                retriever.release();
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
