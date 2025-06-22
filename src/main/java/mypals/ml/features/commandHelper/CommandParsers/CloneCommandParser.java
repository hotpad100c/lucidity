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

public class CloneCommandParser {

    public static void parseCloneCommand(String input) {
        ArrayList<BlockPos> points = parseCloneCommandVectors(input);
        if (points == null || points.size() < 3) return;

        InformationRender.addCube(new CubeShape(points.get(0),0.5f,Color.RED,true));
        InformationRender.addCube(new CubeShape(points.get(1),0.5f,Color.BLUE,true));
        InformationRender.addCube(new CubeShape(points.get(2),0.5f,Color.RED,true));
        InformationRender.addCube(new CubeShape(points.get(3),0.5f,Color.BLUE,true));

        InformationRender.addAreaBox(new AreaBox(points.get(0), points.get(1), Color.RED,0.2f,false));
        InformationRender.addAreaBox(new AreaBox(points.get(2), points.get(3), Color.cyan,0.2f,false));
    }
    public static ArrayList<BlockPos> parseCloneCommandVectors(String command) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        String[] tokens = command.split("\\s+");
        ArrayList<Integer> numbers = new ArrayList<>();
        PlayerEntity playerEntity = MinecraftClient.getInstance().player;
        // 提取所有整数
        try {
            numbers.add(parseCoordinateInt(tokens[1],playerEntity,0));
            numbers.add(parseCoordinateInt(tokens[2],playerEntity,1));
            numbers.add(parseCoordinateInt(tokens[3],playerEntity,2));

            numbers.add(parseCoordinateInt(tokens[4],playerEntity,0));
            numbers.add(parseCoordinateInt(tokens[5],playerEntity,1));
            numbers.add(parseCoordinateInt(tokens[6],playerEntity,2));

            numbers.add(parseCoordinateInt(tokens[7],playerEntity,0));
            numbers.add(parseCoordinateInt(tokens[8],playerEntity,1));
            numbers.add(parseCoordinateInt(tokens[9],playerEntity,2));
        }catch (NumberFormatException ignored) {}

        if (numbers.size() >= 9) {
            // 角点1
            BlockPos pos1 = new BlockPos(numbers.get(0), numbers.get(1), numbers.get(2));
            // 角点2
            BlockPos pos2 = new BlockPos(numbers.get(3), numbers.get(4), numbers.get(5));
            // 克隆区域最小角
            BlockPos pos3 = new BlockPos(numbers.get(6), numbers.get(7), numbers.get(8));

            // 计算选区尺寸
            int sizeX = Math.abs(pos2.getX() - pos1.getX());
            int sizeY = Math.abs(pos2.getY() - pos1.getY());
            int sizeZ = Math.abs(pos2.getZ() - pos1.getZ());

            // 计算克隆出的第二个角点
            BlockPos pos4 = new BlockPos(pos3.getX() + sizeX, pos3.getY() + sizeY, pos3.getZ() + sizeZ);

            // 添加克隆区域的两个角点
            positions.add(pos1);
            positions.add(pos2);
            positions.add(pos3);
            positions.add(pos4);
        }

        return positions;
    }
}
