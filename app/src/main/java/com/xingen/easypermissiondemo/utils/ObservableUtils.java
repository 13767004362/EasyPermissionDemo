package com.xingen.easypermissiondemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import rx.Observable;

/**
 * Created by ${xingen} on 2017/11/1.
 *
 * blog: http://blog.csdn.net/hexingen
 */

public class ObservableUtils {
    /**
     * 加载拍照的相片
     *
     * @param context
     * @param picturePath
     * @param imageView
     * @return
     */
    public static Observable<Bitmap> loadPictureBitmap(Context context, String picturePath, ImageView imageView) {
        return Observable.create(subscriber -> {
            Bitmap bitmap = BitmapUtils.decodeFileBitmap(context, picturePath, imageView.getWidth(), imageView.getHeight());
            subscriber.onNext(bitmap);
        });
    }

    /**
     * 加载图库中选取的相片
     * @param context
     * @param uri
     * @param imageView
     * @return
     */
    public static Observable<Bitmap> loadGalleryBitmap(Context context, Uri uri, ImageView imageView) {
        return Observable.create(subscriber -> {
            String picturePath = CameraUtils.uriConvertPath(context, uri);
            subscriber.onNext(picturePath);
        }).flatMap(path -> loadPictureBitmap(context, (String) path, imageView));
    }
}
