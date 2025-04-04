package uk.co.jcox.chemvis;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class ChemVis {


    private final List<Vector3f> positions = new ArrayList<>();

    float vertices[] = {
            1.0f,  1.0f, 0.0f,  // top right
            1.0f, -1.0f, 0.0f,  // bottom right
            -1.0f, -1.0f, 0.0f,  // bottom left
            -1.0f,  1.0f, 0.0f   // top left
    };
    int indices[] = {  // note that we start from 0!
            0, 1, 3,   // first triangle
            1, 2, 3    // second triangle
    };

    private final long winPointer;

    public ChemVis() {
        if (! GLFW.glfwInit()) {
            throw new IllegalStateException();
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        this.winPointer = GLFW.glfwCreateWindow(1000, 800, "Render", 0, 0);

        GLFW.glfwSetMouseButtonCallback(this.winPointer, (win, button, action, mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS) {
                double[] x = new double[1];
                double[] y = new double[1];

                GLFW.glfwGetCursorPos(this.winPointer, x, y);
                positions.add(new Vector3f((float) x[0], 800 - (float) y[0], 0.0f));
            }

            if (button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_RELEASE) {
            }
        });

        GLFW.glfwMakeContextCurrent(this.winPointer);
        GL.createCapabilities();
        GLUtil.setupDebugMessageCallback();
    }

    public void run() {
        GL11.glClearColor(254/255f, 250/255f, 224/255f, 1.0f);


        Matrix4f perspective = new Matrix4f().ortho(0.0f, 1000.0f, 0.0f, 800.0f, 0.1f, 100.0f);
        Matrix4f camera = new Matrix4f().lookAt(new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f), new Vector3f(0.0f, 1.0f, 0.0f));



        ShaderProgram program = new ShaderProgram(readFile("./data/shaders/default.vsh"), readFile("./data/shaders/default.fsh"));
        program.init();
        program.bind();

        program.uniform("per", perspective);
        program.uniform("cam", camera);


//        Font
        BitmapFont font = BitmapFont.generate(new File("data/fonts/UbuntuMono-Regular.ttf"), 32, true);


        //TESTING - Create some basic geometry
        int vertexArray = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArray);
        int vertexBuff = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuff);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        int stride = 0;
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0L);
        GL20.glEnableVertexAttribArray(0);
        int indexBuff = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuff);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        while (! GLFW.glfwWindowShouldClose(this.winPointer)) {

            int[] xSize = new int[1];
            int[] ySize = new int[1];
            GLFW.glfwGetFramebufferSize(this.winPointer, xSize, ySize);
            GL11.glViewport(0, 0, xSize[0], ySize[0]);


            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            positions.forEach(pos -> {
                Matrix4f model = new Matrix4f().translate(pos);
                GL30.glBindVertexArray(vertexArray);
                GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
                program.uniform("model", model);
            });


            //Font Rendering
            font.text(program, "Hi!", 100, 600);
            font.text(program, "I am rendering this in", 100, 500);
            font.text(program, "OpenGL", 100, 400);

            font.text(program, "It's a bit laggy at the moment", 100, 200);

            GLFW.glfwSwapBuffers(this.winPointer);
            GLFW.glfwPollEvents();
        }
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(this.winPointer);
        GLFW.glfwTerminate();
    }


    private String readFile(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ERROR!!!!";
    }
}
