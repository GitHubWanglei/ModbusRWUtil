package com.example.modbusrwutil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CYToast {

    public static int SUCCESS = 1;
    public static int ERROR = 2;
    public static int WARING = 3;

    public static void show(Context context, String title, int type) {
        Toast toast = Toast.makeText(context, title, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        TextView tv = (TextView) ((LinearLayout)toast.getView()).getChildAt(0);
        tv.setTextSize(25);
        tv.setTextColor(Color.WHITE);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);//形状
        gradientDrawable.setCornerRadius(30);//设置圆角Radius
        gradientDrawable.setColor(getColor(type));//颜色
        toast.getView().setBackground(gradientDrawable);//设置为background
        toast.show();
    }

    private static int getColor(int type) {
        if (type == SUCCESS) {
            return 0xFF2ece6f;
        } else if (type == ERROR) {
            return 0xFFff6562;
        } else if (type == WARING) {
            return 0xFFffb93a;
        }
        return Color.WHITE;
    }
}
