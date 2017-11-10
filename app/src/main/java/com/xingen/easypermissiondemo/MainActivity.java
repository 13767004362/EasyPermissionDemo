package com.xingen.easypermissiondemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.xingen.easypermissiondemo.db.Constance;
import com.xingen.easypermissiondemo.permission.PermissionManager;
import com.xingen.easypermissiondemo.utils.CameraUtils;
import com.xingen.easypermissiondemo.utils.FileUtils;
import com.xingen.easypermissiondemo.utils.ObservableUtils;
import com.xingen.easypermissiondemo.utils.ToastUtils;
import com.xingen.easypermissiondemo.view.CreatePictureDialog;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by ${xinGen} on 2017/11/1.
 *
 * blog:http://blog.csdn.net/hexingen
 *
 */
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, CreatePictureDialog.ResultListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final CompositeSubscription compositeSubscription = new CompositeSubscription();
    private ImageView show_iv;
    private CreatePictureDialog picture_dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recoverState(savedInstanceState);
        checkWritePermission();
        initView();
    }

    /**
     * 恢复被系统销毁的数据
     * @param savedInstanceState
     */
    private void recoverState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.picturePath = savedInstanceState.getString(TAG);
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        this.show_iv = (ImageView) findViewById(R.id.main_show_iv);
        this.show_iv.setOnClickListener(view ->
                showPictureDialog()
        );
    }
    /**
     * 选择拍照或者图库的弹窗
     */
    private void showPictureDialog() {
        if (picture_dialog == null) {
            this.picture_dialog = new CreatePictureDialog(this);
            this.picture_dialog.setResultListener(this);
        }
        if (!picture_dialog.isShowing()) {
            picture_dialog.show();
        }
    }

    private void dismissPictureDialog(){
        if (picture_dialog != null && picture_dialog.isShowing()) {
            picture_dialog.dismiss();
        }
    }

    /**
     * 释放资源：
     * 关闭线程，dialog等
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.dismissPictureDialog();
        this.compositeSubscription.clear();
    }
    /**
     * 检查读写权限权限
     */
    private void checkWritePermission() {
        boolean result = PermissionManager.checkPermission(this, Constance.PERMS_WRITE);
        if (!result) {
            PermissionManager.requestPermission(this, Constance.WRITE_PERMISSION_TIP, Constance.WRITE_PERMISSION_CODE, Constance.PERMS_WRITE);
        }
    }
    /**
     * 重写onRequestPermissionsResult，用于接受请求结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //将请求结果传递EasyPermission库处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    /**
     * 请求权限成功
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        ToastUtils.showToast(getApplicationContext(), "用户授权成功");

    }
    /**
     * 请求权限失败
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        ToastUtils.showToast(getApplicationContext(), "用户授权失败");
        /**
         * 若是在权限弹窗中，用户勾选了'NEVER ASK AGAIN.'或者'不在提示'，且拒绝权限。
         * 这时候，需要跳转到设置界面去，让用户手动开启。
         */
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //当从软件设置界面，返回当前程序时候
            case AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE:
                break;
            //拍照返回
            case Constance.PICTURE_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    loadPictureBitmap();
                }
                break;
            //图库返回
            case Constance.GALLERY_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    loadGalleryBitmap(uri);
                }
                break;
            default:

                break;
        }
    }
    private void loadGalleryBitmap(Uri uri) {
        Observable<Bitmap> bitmapObservable=ObservableUtils.loadGalleryBitmap(getApplicationContext(),uri,show_iv);
        executeObservableTask(bitmapObservable);
    }
    private void loadPictureBitmap() {
      Observable<Bitmap> bitmapObservable= ObservableUtils.loadPictureBitmap(getApplicationContext(), picturePath, show_iv);
        executeObservableTask(bitmapObservable);
    }
    private void executeObservableTask(Observable<Bitmap> observable) {
        Subscription subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap ->
                                show_iv.setImageBitmap(bitmap)
                        , error ->
                                ToastUtils.showToast(getApplicationContext(), "加载图片出错")
                );
        this.compositeSubscription.add(subscription);
    }
    private String picturePath;
    @Override
    public void camera() {
        this.picturePath = FileUtils.getBitmapDiskFile(this.getApplicationContext());
        CameraUtils.openCamera(this, Constance.PICTURE_CODE, this.picturePath);
    }

    @Override
    public void photoAlbum() {

            CameraUtils.openGallery(this, Constance.GALLERY_CODE);
    }

    /**
     * 防止系统内存不足销毁Activity
     * ,这里保存数据，便于恢复。
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TAG, picturePath);
    }
}
