package mypals.ml.features.commandHelper.CommandParsers;

import mypals.ml.features.selectiveRendering.AreaBox;
import mypals.ml.rendering.InformationRender;
import mypals.ml.rendering.shapes.CubeShape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;

import static mypals.ml.features.commandHelper.CommandParsers.BasicParsers.parseCoordinate;
import static mypals.ml.features.commandHelper.CommandParsers.BasicParsers.parseCoordinateInt;

public class FillCommandParser {
    public static void parseFillCommand(String input) {
        ArrayList<BlockPos> points = parseFillCommandVectors(input);
        if (points == null) return;
        InformationRender.addCube(new CubeShape(points.get(0),0.5f,Color.RED,true));
        InformationRender.addCube(new CubeShape(points.get(1),0.5f,Color.BLUE,true));
        InformationRender.addAreaBox(new AreaBox(points.get(0), points.get(1), Color.ORANGE,0.2f,false));
    }
    private static ArrayList<BlockPos> parseFillCommandVectors(String input) {
        String[] tokens = input.split("\\s+");
        if (tokens.length < 7) return null;
        PlayerEntity playerEntity = MinecraftClient.getInstance().player;
        try {
            ArrayList<BlockPos> points = new ArrayList<>();
            points.add(new BlockPos(
                    (int)parseCoordinateInt(tokens[1],playerEntity,0),
                    (int)parseCoordinateInt(tokens[2],playerEntity,1),
                    (int)parseCoordinateInt(tokens[3],playerEntity,2)));
            points.add(new BlockPos(
                    (int)parseCoordinateInt(tokens[4],playerEntity,0),
                    (int)parseCoordinateInt(tokens[5],playerEntity,1),
                    (int)parseCoordinateInt(tokens[6],playerEntity,2)));
            return points;
        }catch (Exception e) {
            return null;
        }

    }
}
