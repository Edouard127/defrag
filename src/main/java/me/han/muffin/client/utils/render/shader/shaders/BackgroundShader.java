package me.han.muffin.client.utils.render.shader.shaders;

import me.han.muffin.client.utils.render.RenderUtils;
import me.han.muffin.client.utils.render.shader.Shader;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL20;

public class BackgroundShader extends Shader {

    public final static BackgroundShader BACKGROUND_SHADER = new BackgroundShader();

    private float time;

    public BackgroundShader() {
        super("background.frag");
    }

    @Override
    public void setupUniforms() {
        setupUniform("iResolution");
        setupUniform("iTime");
    }

    @Override
    public void updateUniforms() {

        final int resolutionID = getUniform("iResolution");
        if (resolutionID > -1)
            GL20.glUniform2f(resolutionID, (float) Display.getWidth(), (float) Display.getHeight());
        final int timeID = getUniform("iTime");
        if (timeID > -1)
            GL20.glUniform1f(timeID, time);

        time += 0.003F * RenderUtils.deltaTime;
    }

}