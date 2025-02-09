package mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionCastLines;

import mypals.ml.features.explosionVisualizer.explotionAffectdDataManage.ExplosionCastLines.PointsOnLine.CastPoint;

import java.util.List;

public class ExplosionCastLine {

    public final float orgBlastStrength;
    public final int lineColor;
    public final List<CastPoint> points;

    public ExplosionCastLine(float orgBlastStrength, int c, List<CastPoint> pointList){
        this.orgBlastStrength = orgBlastStrength;
        this.lineColor = c;
        this.points = pointList;
    }

    public int getLineColor() {
        return lineColor;
    }
    public float getOrgBlastStrength() {return orgBlastStrength;}
    public List<CastPoint> getPoints() {
        return points;
    }

}
