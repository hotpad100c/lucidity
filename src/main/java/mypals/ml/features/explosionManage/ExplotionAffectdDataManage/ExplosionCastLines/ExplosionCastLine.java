package mypals.ml.features.explosionManage.ExplotionAffectdDataManage.ExplosionCastLines;

import mypals.ml.features.explosionManage.ExplotionAffectdDataManage.ExplosionCastLines.PointsOnLine.CastPoint;

import java.util.List;

public class ExplosionCastLine {
    public final int lineColor;
    public final List<CastPoint> points;

    public ExplosionCastLine(int c, List<CastPoint> pointList){
        this.lineColor = c;
        this.points = pointList;
    }

    public int getLineColor() {
        return lineColor;
    }
    public List<CastPoint> getPoints() {
        return points;
    }

}
