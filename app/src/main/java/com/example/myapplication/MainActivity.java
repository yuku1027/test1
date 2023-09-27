package com.example.myapplication;

// Android繪圖類
import android.graphics.Canvas;         // 提供繪製到位圖的方法
import android.graphics.Color;          // 提供常見的顏色值和顏色操作方法
import android.graphics.Paint;          // 用於設定繪圖的樣式、顏色等
import android.graphics.Rect;           // 表示一個矩形的座標
import android.graphics.Bitmap;         // 用於操作和存儲圖片數據

// 基本的Android功能和權限類
import android.Manifest;                // Android的權限類，定義應用程序所需要的權限
import android.content.Intent;          // 用於啟動活動、服務或發送廣播
import android.content.pm.PackageManager; // 查詢和管理應用程序的包信息和權限
import android.provider.MediaStore;     // 用於存取多媒體數據

// Android生命周期和UI類
import android.os.Bundle;               // 用於存儲活動或片段的狀態信息
import androidx.annotation.NonNull;     // 標記參數、方法或返回值永遠不會是null
import androidx.appcompat.app.AppCompatActivity; // 提供現代Android特性的後向兼容性
import android.view.View;              // Android的視圖基類
import android.widget.ImageView;       // 用於顯示圖片
import android.widget.Toast;           // 用於顯示短暫的提示信息

// 其他Android類和函數
import androidx.core.app.ActivityCompat;   // 提供與Android版本兼容性相關的一些核心工具方法
import androidx.core.content.ContextCompat;    // 提供與Android版本兼容性相關的一些核心工具方法
import android.os.Environment;         // 提供訪問環境變數和特定文件目錄的方法
import android.net.Uri;                // 表示內容提供者的數據

// Java I/O類
import java.io.File;                   // 表示文件和目錄
import java.io.FileOutputStream;       // 用於寫入文件數據
import java.io.IOException;            // I/O異常的基類

// Debugging
import android.util.Log;               // 提供日誌記錄功能

// Google ML Kit類
import com.google.mlkit.vision.common.InputImage;         // ML Kit輸入圖像格式
import com.google.mlkit.vision.face.Face;                 // ML Kit的人臉識別API
import com.google.mlkit.vision.face.FaceDetection;        // ML Kit的人臉識別API
import com.google.mlkit.vision.face.FaceDetector;         // ML Kit的人臉識別API
import com.google.mlkit.vision.face.FaceDetectorOptions;  // ML Kit的人臉識別API配置選項

//mainactivity繼承來自AppcomatActivity(若程式需要運用在比較舊的android系統 通常都會用appcomatactivity) 提供的所有方法和屬性，並可以添加或覆寫這些方法和屬性以實現自己的功能.
public class MainActivity extends AppCompatActivity {



    // 這三個常數定義了請求碼，用於標識特定的活動結果或權限請求
    private static final int CAMERA_REQUEST_CODE = 100; //請求相機
    private static final int CAMERA_INTENT_REQUEST_CODE = 101; //請求打開相簿

    //請求點開相簿
    private static final int GALLERY_REQUEST_CODE = 102; //請求傳回結果

    //用來設定顯示圖片
    private ImageView imageView;

    @Override  //它表示這個方法是從父類中繼承來的，並且在當前類中被重寫(override)了。
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//
        setContentView(R.layout.activity_main); //設定用戶介面
        imageView = findViewById(R.id.imageView);  // 初始化ImageView 以及找圖片
    }
    // 用於啟動系統相機
    private void launchCamera() {
        // 創建一個意圖(Intent)，該意圖的動作是啟動相機
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //intent代表著要執行的動作 mediastore是一個指定啟動相機來捕獲圖像的標準動作字符串。
        // 檢查是否有應用可以處理此意圖（例如是否有相機應用）
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 使用startActivityForResult方法啟動相機
            // CAMERA_INTENT_REQUEST_CODE用作請求碼，稍後將在onActivityResult方法中使用
            startActivityForResult(cameraIntent, CAMERA_INTENT_REQUEST_CODE);
        }
    }
//開啟相機功能
    public void openCamera(View view) {
        Log.d("CameraAction", "打開相機");

        // 檢查相機權限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("CameraAction", "正在請求相機功能");

            // 如果沒有授權，則請求相機權限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            Log.d("CameraAction", "以獲得權限，將開啟相機.");

            launchCamera();
        }
    }

    // 這是一個回調方法，用於處理使用者對請求權限的響應
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 使用者授予了相機權限，啟動相機
                launchCamera();
            } else {
                // 使用者拒絕了相機權限
                Toast.makeText(this, "相機權限被拒絕", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override  //用於處理請求的結果
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //檢查相機是否有返回的結果且操作成功
        if (requestCode == CAMERA_INTENT_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            detectFaces(imageBitmap);
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {  //如果不是從攝像頭Intent返回，則檢查是否是從相簿選擇圖片返回的結果且操作成功。
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                detectFaces(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "圖片載入錯誤", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //主要在做人臉辨識 並在偵測到的人臉上 畫一個紅色的框
    private void detectFaces(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        FaceDetectorOptions options = new FaceDetectorOptions.Builder() //用於設置人臉辨識的選項
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        FaceDetector faceDetector = FaceDetection.getClient(options);

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    // 創建一個mutable的bitmap以繪製框
                    Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(mutableBitmap);
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(10f);

                    for (Face face : faces) {
                        Rect bounds = face.getBoundingBox();
                        canvas.drawRect(bounds, paint); // 在人臉上畫框

                        float rotY = face.getHeadEulerAngleY();
                        float rotZ = face.getHeadEulerAngleZ();
                        Toast.makeText(this,"偵測到臉孔!",Toast.LENGTH_SHORT).show();
                        Log.d("FaceDetection","檢測到臉孔:"+bounds.toString());
                    }

                    imageView.setImageBitmap(mutableBitmap); // 將繪製好的圖片設定到ImageView
                    saveBitmapToGallery(mutableBitmap);
                })
                .addOnFailureListener(e -> {
                    // 顯示 Toast 提示
                    Toast.makeText(this, "人臉辨識失敗：" + e.getMessage(), Toast.LENGTH_LONG).show();
                    // 打印錯誤日誌
                    Log.e("FaceDetection", "錯誤：" + e.getMessage(), e);
                });
    }
//
    //將拍攝的圖片儲存至手機
    private void saveBitmapToGallery(Bitmap bitmap) {
        String filename = "captured_image_" + System.currentTimeMillis() + ".jpg"; //建立文件以及取名
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YourAppName"); //圖片將被保存到某個文件夾

        if (!storageDir.exists() && !storageDir.mkdirs()) { //確保存儲目錄存在。如果不存在，則嘗試創建它。如果無法創建，將記錄一條錯誤並結束方法。
            Log.e("FaceDetection", "無法創建圖片保存目錄");
            return;
        }
        File imageFile = new File(storageDir, filename); //這行代碼確定將在哪個位置保存圖片。

        try (FileOutputStream out = new FileOutputStream(imageFile)) { //將圖片寫入所定義的文件 並被壓縮成jpeg
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();

            // 更新媒體掃描器，使圖片出現在相簿中
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, filename, "captured_image"); //將保存的圖片告訴手機端 他才會存儲在手機相簿內
        } catch (Exception e) {
            Log.e("FaceDetection", "保存圖片出錯", e);
        }
    }

    // 從相簿中選擇圖片
    //Intent在Android中用於描述要執行的操作。
    public void openGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //從外部儲存中選擇一張圖片
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE); //開啟這個應用 讓用戶選擇照片
    }

}
