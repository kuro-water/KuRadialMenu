package dev.kurowater.kuradialmenu.client.util;

/**
 * 数学関連のユーティリティ関数
 */
public final class MathUtil {

    private MathUtil() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    /**
     * 2点間の角度を計算 (度数法、上方向が0度、時計回りに増加)
     *
     * @param centerX 中心X座標
     * @param centerY 中心Y座標
     * @param pointX 対象点X座標
     * @param pointY 対象点Y座標
     * @return 角度 (0-360度)
     */
    public static double getAngle(double centerX, double centerY, double pointX, double pointY) {
        double dx = pointX - centerX;
        double dy = pointY - centerY;

        // atan2は左方向が0度で反時計回り、Y軸が下向きなので調整
        double angle = Math.toDegrees(Math.atan2(dx, -dy));

        // 0-360の範囲に正規化
        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    /**
     * 2点間の距離を計算
     *
     * @param x1 点1のX座標
     * @param y1 点1のY座標
     * @param x2 点2のX座標
     * @param y2 点2のY座標
     * @return 距離
     */
    public static double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 角度からスロットインデックスを計算
     *
     * @param angle 角度 (0-360度)
     * @param slotCount スロット数
     * @return スロットインデックス (0から始まる)
     */
    public static int getSlotIndex(double angle, int slotCount) {
        double slotAngle = 360.0 / slotCount;
        // 最初のスロットの中心が上 (0度) になるようにオフセット
        double adjustedAngle = (angle + slotAngle / 2) % 360;
        return (int) (adjustedAngle / slotAngle);
    }
}

