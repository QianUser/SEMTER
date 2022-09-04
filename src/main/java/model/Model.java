package model;

import jep.Interpreter;
import jep.JepException;
import jep.SharedInterpreter;

import java.util.List;


public class Model {

    private static final Interpreter interpreter;

    static {
        interpreter = new SharedInterpreter();
        interpreter.exec("import sys");
        interpreter.exec("sys.argv.append('')");
        interpreter.exec("sys.path.append('src/main/resources/resnet50')");
        interpreter.exec("import semantic_model");
    }

    public static void encodeTexts(List<String> texts) {
        try {
            interpreter.invoke("semantic_model.encode_texts", texts);
        } catch (JepException exception) {
            exception.printStackTrace();
        }
    }

    public static void encodeImages(List<byte[]> images) {
        try {
            interpreter.invoke("semantic_model.encode_images", images);
        } catch (JepException exception) {
            exception.printStackTrace();
        }
    }

    public static double getSimilarity(String text1, String text2) {
        try {
            return (double) interpreter.invoke("semantic_model.sim_text2text", text1, text2);
        } catch (JepException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public static double getSimilarity(String text, byte[] bytes) {
        try {
            return (double) interpreter.invoke("semantic_model.sim_text2image", text, bytes);
        } catch (JepException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public static double getSimilarity(byte[] bytes1, byte[] bytes2) {
        try {
            return (double) interpreter.invoke("semantic_model.sim_image2image", bytes1, bytes2);
        } catch (JepException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

}
