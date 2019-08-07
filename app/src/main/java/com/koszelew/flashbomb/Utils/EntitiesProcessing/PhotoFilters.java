package com.koszelew.flashbomb.Utils.EntitiesProcessing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;
import android.view.View;

import com.koszelew.flashbomb.R;
import com.zomato.photofilters.geometry.Point;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ToneCurveSubfilter;

public class PhotoFilters {

    public static Bitmap applyFilter1(Bitmap src) {

        Bitmap filtered = src.copy(src.getConfig(), true);
        Point[] rgbKnots = new Point[4];
        rgbKnots[0] = new Point(0, 32);
        rgbKnots[1] = new Point(62, 60);
        rgbKnots[2] = new Point(193, 186);
        rgbKnots[3] = new Point(255, 255);
        Filter filter = new Filter();
        filter.addSubFilter(new BrightnessSubfilter(15));
        filter.addSubFilter(new ContrastSubfilter(1.15f));
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, null, null, null));
        filter.addSubFilter(new SaturationSubfilter(1.15f));
        filtered = filter.processFilter(filtered);
        return filtered;

    }

    public static Bitmap applyFilter2(Bitmap src) {

        Bitmap filtered = src.copy(src.getConfig(), true);
        Point[] rgbKnots = new Point[4];
        rgbKnots[0] = new Point(0, 0);
        rgbKnots[1] = new Point(52, 48);
        rgbKnots[2] = new Point(209, 229);
        rgbKnots[3] = new Point(255, 245);
        Point[] gKnots = new Point[4];
        gKnots[0] = new Point(0, 8);
        gKnots[1] = new Point(106, 109);
        gKnots[2] = new Point(213, 229);
        gKnots[3] = new Point(255, 247);
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, null, gKnots, null));
        filtered = filter.processFilter(filtered);
        return filtered;

    }

    public static Bitmap applyFilter3(Bitmap src) {

        Bitmap filtered = src.copy(src.getConfig(), true);
        Filter filter = new Filter();
        filter.addSubFilter(new SaturationSubfilter(0.0f));
        filter.addSubFilter(new BrightnessSubfilter(10));
        filter.addSubFilter(new ContrastSubfilter(1.1f));
        filtered = filter.processFilter(filtered);
        return filtered;

    }

    public static Bitmap applyFilter4(Bitmap src) {

        Bitmap filtered = src.copy(src.getConfig(), true);
        Point[] rgbKnots = new Point[4];
        rgbKnots[0] = new Point(0, 0);
        rgbKnots[1] = new Point(58, 41);
        rgbKnots[2] = new Point(192, 192);
        rgbKnots[3] = new Point(255, 255);
        Point[] rKnots = new Point[4];
        rKnots[0] = new Point(14, 0);
        rKnots[1] = new Point(70, 68);
        rKnots[2] = new Point(191, 202);
        rKnots[3] = new Point(255, 255);
        Point[] gKnots = new Point[3];
        gKnots[0] = new Point(14, 0);
        gKnots[1] = new Point(126, 130);
        gKnots[2] = new Point(255, 255);
        Point[] bKnots = new Point[4];
        bKnots[0] = new Point(0, 0);
        bKnots[1] = new Point(62, 65);
        bKnots[2] = new Point(194, 187);
        bKnots[3] = new Point(255, 255);
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, rKnots, gKnots, bKnots));
        filter.addSubFilter(new BrightnessSubfilter(3));
        filter.addSubFilter(new ContrastSubfilter(0.97f));
        filtered = filter.processFilter(filtered);
        return filtered;

    }

    public static Bitmap applyFilter5(Bitmap src) {

        Bitmap filtered = src.copy(src.getConfig(), true);
        Point[] rKnots = new Point[8];
        rKnots[0] = new Point(0, 24);
        rKnots[1] = new Point(10, 25);
        rKnots[2] = new Point(23, 29);
        rKnots[3] = new Point(58, 52);
        rKnots[4] = new Point(130, 135);
        rKnots[5] = new Point(149, 152);
        rKnots[6] = new Point(217, 215);
        rKnots[7] = new Point(255, 242);
        Point[] gKnots = new Point[5];
        gKnots[0] = new Point(0, 23);
        gKnots[1] = new Point(13, 26);
        gKnots[2] = new Point(76, 82);
        gKnots[3] = new Point(185, 189);
        gKnots[4] = new Point(255, 242);
        Point[] bKnots = new Point[9];
        bKnots[0] = new Point(0, 10);
        bKnots[1] = new Point(11, 12);
        bKnots[2] = new Point(43, 31);
        bKnots[3] = new Point(60, 47);
        bKnots[4] = new Point(78, 70);
        bKnots[5] = new Point(118, 125);
        bKnots[6] = new Point(190, 196);
        bKnots[7] = new Point(230, 227);
        bKnots[8] = new Point(255, 246);
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, rKnots, gKnots, bKnots));
        filtered = filter.processFilter(filtered);
        return filtered;

    }

    public static Bitmap applyFilter6(Bitmap src) {

        Bitmap filtered = src.copy(src.getConfig(), true);
        Point[] rKnots = new Point[10];
        rKnots[0] = new Point(0, 51);
        rKnots[1] = new Point(16, 55);
        rKnots[2] = new Point(38, 59);
        rKnots[3] = new Point(50, 62);
        rKnots[4] = new Point(93, 73);
        rKnots[5] = new Point(131, 111);
        rKnots[6] = new Point(164, 192);
        rKnots[7] = new Point(179, 213);
        rKnots[8] = new Point(229, 238);
        rKnots[9] = new Point(255, 250);
        Point[] gKnots = new Point[6];
        gKnots[0] = new Point(0, 49);
        gKnots[1] = new Point(19, 52);
        gKnots[2] = new Point(32, 56);
        gKnots[3] = new Point(104, 92);
        gKnots[4] = new Point(198, 198);
        gKnots[5] = new Point(255, 255);
        Point[] bKnots = new Point[5];
        bKnots[0] = new Point(0, 40);
        bKnots[1] = new Point(69, 72);
        bKnots[2] = new Point(141, 127);
        bKnots[3] = new Point(215, 191);
        bKnots[4] = new Point(255, 204);
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, rKnots, gKnots, bKnots));
        filtered = filter.processFilter(filtered);
        return filtered;

    }

    public static Bitmap applyFilter7(Bitmap src) {

        Bitmap filtered = src.copy(src.getConfig(), true);
        Point[] rgbKnots = new Point[4];
        rgbKnots[0] = new Point(0, 0);
        rgbKnots[1] = new Point(58, 41);
        rgbKnots[2] = new Point(192, 192);
        rgbKnots[3] = new Point(255, 255);
        Point[] rKnots = new Point[4];
        rKnots[0] = new Point(0, 0);
        rKnots[1] = new Point(62, 65);
        rKnots[2] = new Point(194, 187);
        rKnots[3] = new Point(255, 255);
        Point[] gKnots = new Point[4];
        gKnots[0] = new Point(14, 0);
        gKnots[1] = new Point(70, 68);
        gKnots[2] = new Point(191, 202);
        gKnots[3] = new Point(255, 255);
        Point[] bKnots = new Point[3];
        bKnots[0] = new Point(14, 0);
        bKnots[1] = new Point(126, 130);
        bKnots[2] = new Point(255, 255);
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, rKnots, gKnots, bKnots));
        filtered = filter.processFilter(filtered);
        return filtered;

    }

    public static Bitmap applyFilter8(Bitmap src) {

        Bitmap filtered = src.copy(src.getConfig(), true);
        Point[] rgbKnots = new Point[5];
        rgbKnots[0] = new Point(0, 14);
        rgbKnots[1] = new Point(64, 64);
        rgbKnots[2] = new Point(128, 128);
        rgbKnots[3] = new Point(192, 192);
        rgbKnots[4] = new Point(255, 241);
        Point[] rKnots = new Point[5];
        rKnots[0] = new Point(0, 14);
        rKnots[1] = new Point(64, 64);
        rKnots[2] = new Point(128, 128);
        rKnots[3] = new Point(192, 192);
        rKnots[4] = new Point(255, 241);
        Point[] gKnots = new Point[5];
        gKnots[0] = new Point(0, 14);
        gKnots[1] = new Point(64, 64);
        gKnots[2] = new Point(128, 128);
        gKnots[3] = new Point(192, 192);
        gKnots[4] = new Point(255, 241);
        Point[] bKnots = new Point[5];
        bKnots[0] = new Point(0, 14);
        bKnots[1] = new Point(64, 64);
        bKnots[2] = new Point(128, 128);
        bKnots[3] = new Point(192, 192);
        bKnots[4] = new Point(255, 241);
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, rKnots, gKnots, bKnots));
        filtered = filter.processFilter(filtered);
        return filtered;

    }

    public static Bitmap applyFlashbombWatermark(Bitmap src, Context ctx) {

        android.graphics.Bitmap.Config bitmapConfig = src.getConfig();
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        Bitmap filtered = src.copy(bitmapConfig, true);
        Bitmap logo = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.logo);
        logo = scaleDown(logo, filtered.getWidth() / 6f);
        int x = filtered.getWidth() - logo.getWidth() - Math.round((float)logo.getHeight() * 0.4f);
        int y = filtered.getHeight() - Math.round((float)logo.getHeight() * 1.4f);

        float brightness = 0;
        int counter = 0;
        for(int i = x; i < filtered.getWidth() - Math.round((float)logo.getHeight() * 0.2f); i++) {
            for(int j = y; j < filtered.getHeight() - Math.round((float)logo.getHeight() * 0.2f); j++) {
                brightness += (float)(Color.red(filtered.getPixel(i, j)) + Color.green(filtered.getPixel(i, j)) + Color.blue(filtered.getPixel(i, j))) / 3;
                counter++;
                Log.d("Flashbomb-Log", counter + " => " + brightness);
            }
        }
        float meanBrightness = brightness / counter;
        Log.d("Flashbomb-Log", "mean: " + meanBrightness);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor(meanBrightness > 160 ? "#DD000000" : "#DDFFFFFF"));
        paint.setColorFilter(new PorterDuffColorFilter(Color.parseColor(meanBrightness > 160 ? "#DD000000" : "#DDFFFFFF"), PorterDuff.Mode.SRC_ATOP));
        new Canvas(filtered).drawBitmap(logo, x, y, paint);

        logo.recycle();
        src.recycle();
        System.gc();

        return filtered;

    }

    /* Static method to set rainbow gradient background to element */
    public static void SetRainbowGradient(final View v) {

        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                LinearGradient lg = new LinearGradient(0, 0, v.getWidth(), 0,
                        new int[] {
                                Color.parseColor("#ba375b"),
                                Color.parseColor("#243872"),
                                Color.parseColor("#1a733a"),
                                Color.parseColor("#f1d23d"),
                                Color.parseColor("#f05830")
                        },
                        null,
                        Shader.TileMode.REPEAT);
                return lg;
            }
        };
        PaintDrawable p = new PaintDrawable();
        p.setShape(new RectShape());
        p.setShaderFactory(sf);
        v.setBackground(p);

    }

    /* Static method to scale bitmap */
    private static Bitmap scaleDown(Bitmap realImage, float maxImageSize) {

        float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, true);

        realImage.recycle();
        System.gc();

        return newBitmap;

    }

}
