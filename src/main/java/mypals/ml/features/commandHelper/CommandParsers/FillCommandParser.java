package mypals.ml.features.commandHelper.CommandParsers;

import mypals.ml.features.selectiveRendering.AreaBox;
import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.CubeShape;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;

public class FillCommandParser {
    public static void parseFillCommand(String input) {
        ArrayList<BlockPos> points = parseFillCommandVectors(input);
        if (points == null) return;
        InformationRender.addCube(new CubeShape(points.get(0),0.5f,Color.RED,true));
        InformationRender.addCube(new CubeShape(points.get(1),0.5f,Color.BLUE,true));
        InformationRender.addAreaBox(new AreaBox(points.get(0), points.get(1), Color.ORANGE,0.2f,false));
    }
    private static ArrayList<BlockPos> parseFillCommandVectors(String input) {
        String[] parts = input.split("\\s+");
        if (parts.length < 7) return null;

        try {
            ArrayList<BlockPos> points = new ArrayList<>();
            points.add(new BlockPos(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
            points.add(new BlockPos(Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), Integer.parseInt(parts[6])));
            return points;
        }catch (Exception e) {
            return null;
        }

    }
}
