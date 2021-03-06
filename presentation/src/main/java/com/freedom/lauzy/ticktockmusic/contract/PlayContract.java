package com.freedom.lauzy.ticktockmusic.contract;

import android.content.Context;
import android.graphics.Bitmap;

import com.freedom.lauzy.ticktockmusic.base.IBaseView;
import com.freedom.lauzy.ticktockmusic.model.SongEntity;
import com.lauzy.freedom.librarys.widght.music.lrc.Lrc;

import java.util.List;

/**
 * Desc :
 * Author : Lauzy
 * Date : 2017/9/7
 * Blog : http://www.jianshu.com/u/e76853f863a9
 * Email : freedompaladin@gmail.com
 */
public interface PlayContract {
    interface View extends IBaseView {

        Context getContext();

        void setCoverBackground(Bitmap background);

        void addFavoriteSong();

        void deleteFavoriteSong();

        void setViewBgColor(int paletteColor);

        void setPlayView(Bitmap resource);

        void showLightViews(boolean isFavorite);

        void showDarkViews(boolean isFavorite);

        void startDownloadLrc();

        void downloadLrcSuccess(List<Lrc> lrcs);

        void downloadFailed(Throwable e);
    }

    interface Presenter {
        void setCoverImgUrl(long songId, Object url);

        void addFavoriteSong(SongEntity entity);

        void deleteFavoriteSong(long songId);

        void loadLrc(SongEntity entity);

        void setRepeatMode(int mode);

        int getRepeatMode(int defautMode);
    }
}
