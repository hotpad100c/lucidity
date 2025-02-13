package mypals.ml.features.commandHelper;

import mypals.ml.features.commandHelper.CommandParsers.CloneCommandParser;
import mypals.ml.features.commandHelper.CommandParsers.FillCommandParser;
import mypals.ml.features.selectiveRendering.AreaBox;
import mypals.ml.rendering.InformationRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;

public class ChatCommandScreenObserver {
    public static void onClientTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ChatScreen chatScreen) {
            try {
                TextFieldWidget chatField = (TextFieldWidget) chatScreen.children().get(0);
                String currentInput = chatField.getText();

                if (currentInput.startsWith("/fill ")) {
                    FillCommandParser.parseFillCommand(currentInput);
                }
                if (currentInput.startsWith("/clone ")) {
                    CloneCommandParser.parseCloneCommand(currentInput);
                }
            } catch (Exception ignored) {}
        }
    }

}
