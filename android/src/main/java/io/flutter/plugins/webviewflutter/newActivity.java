package io.flutter.plugins.webviewflutter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;

import static android.content.ContentValues.TAG;

public class newActivity extends Activity {
    private static ValueCallback<Uri[]> mUploadMessageArray;

    public static void getfilePathCallback(ValueCallback<Uri[]> filePathCallback){
        mUploadMessageArray = filePathCallback;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        boolean b = isStoragePermissionGranted();
        //openCarem();
        openAblum();
//        AlertDialog.Builder builder = new AlertDialog.Builder(newActivity.this,R.layout.dialog);
//        builder.setTitle("请选择");
//        final String[] sex = {"打开相册", "打开相机", "未知操作"};
//        builder.setItems(sex, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                switch (which){
//                    case 0:
//                        openAblum();
//                        break;
//                    case 1:
//                        openCarem();
//                        break;
//                    case 2:
//                        Toast.makeText(newActivity.this, "未知操作", Toast.LENGTH_SHORT).show();
//                        onActivityResult(1,1,null);
//                        break;
//                    default:
//                        finish();
//                        break;
//                }
//            }
//
//        });
//        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                Log.i("TAG","SetOnCancel");
//                onActivityResult(1,1,null);
//            }
//        });
//        builder.show();
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }


    private void openAblum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,1);
    }

    private void openCarem(){
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //系统常量， 启动相机的关键
        startActivityForResult(openCameraIntent, 2); // 参数常量为自定义的request code, 在取返回结果时有用
    }

    private void showBottomDialog(){
        //1、使用Dialog、设置style
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        //2、设置布局
        View view = View.inflate(this, R.layout.dialog_custom_layout,null);
        dialog.setContentView(view);
        //点击其他空白处，退出dialog。
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //这样可以使返回值为null。
                onActivityResult(1,1,null);
            }
        });
        Window window = dialog.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        //设置弹出动画
        window.setWindowAnimations(R.style.main_menu_animStyle);
        //设置对话框大小
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.show();

        dialog.findViewById(R.id.tv_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                openCarem();
            }
        });

        dialog.findViewById(R.id.tv_take_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                openAblum();
            }
        });

        dialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                onActivityResult(1,1,null);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("TAG","forResult");
        if(data != null){
            Uri uri = data.getData();
            Log.i("TAG","! "+data.getClass()+" * "+data);
            Log.i("TAG","URi "+uri);

            if(uri==null){
                Log.i("TAG", String.valueOf(data));
                Bundle bundle = data.getExtras();
                try {
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                    //Uri lUri = Uri.parse(path);
                    Uri[] results = new Uri[]{uri};
                    mUploadMessageArray.onReceiveValue(results);
                }catch (Exception e){
                    mUploadMessageArray.onReceiveValue(null);
                }
            }
            else{
                Uri[] results = new Uri[]{uri};
                mUploadMessageArray.onReceiveValue(results);
            }

        }else{
            Log.i("TAG","onReceveValue");
            mUploadMessageArray.onReceiveValue(null);
        }
        finish();
    }
}
