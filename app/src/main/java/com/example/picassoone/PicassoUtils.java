package com.example.picassoone;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * Created by luoliwen on 16/4/22.
 * compile 'com.squareup.picasso:picasso:2.5.2'
 */
public class PicassoUtils {
    //以新的尺寸加载图片
    public static void loadImageWithSize(Context context, String path, int width, int height, ImageView imageView) {
        //resize重新裁剪图片的大小
        Picasso.with(context).load(path).resize(width, height).centerCrop().into(imageView);
    }

    //加载的时候，先展示resID图片资源，然后再动态的更新
    public static void loadImageWithHodler(Context context, String path, int resID, ImageView imageView) {
        //fit适合imageview图片的大小
        Picasso.with(context).load(path).fit().placeholder(resID).into(imageView);
    }

    //对图片进行相应的裁剪
    public static void loadImageWithCrop(Context context, String path, ImageView imageView) {
        Picasso.with(context).load(path).transform(new CropSquareTransformation()).into(imageView);
    }

    /**
     * 实现对图片的自定义裁剪
     */
    public static class CropSquareTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            //获得图片宽、高中的较小值
            int size = Math.min(source.getWidth(), source.getHeight());
            //计算出宽度、高度的缩减值
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != null) {
                source.recycle();
                ;
            }
            return result;
        }


        @Override
        public String key() {
            //起一个名称
            return "square()";
        }
    }
}
