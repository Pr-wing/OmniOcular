package me.exz.omniocular.handler;

import me.exz.omniocular.reference.Reference;
import me.exz.omniocular.util.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings("CanBeFinal")
public class ConfigHandler {
    public static File minecraftConfigDirectory;
    public static String mergedConfig = "";
    public static Map<Pattern, Node> entityPattern = new HashMap<Pattern, Node>();
    public static Map<Pattern, Node> tileEntityPattern = new HashMap<Pattern, Node>();
    public static Map<Pattern, Node> tooltipPattern = new HashMap<Pattern, Node>();

    public static void initConfigFiles() {
        File configDir = new File(minecraftConfigDirectory, Reference.MOD_ID);
        if (!configDir.exists()) {
            if (!configDir.mkdir()) {
                LogHelper.fatal("Can't create config folder");
            }else {
                LogHelper.info("Config folder created");
            }
        }
        try {
            releasePreConfigFiles(configDir);
        } catch (Exception e) {
            LogHelper.error("Can't release pre-config files");
        }
    }


    private static void releasePreConfigFiles(File configDir) throws IOException {
        for (String configFileName : Reference.configList) {
            configFileName += ".xml";
            File targetFile = new File(configDir, configFileName);
            if (!targetFile.exists()) {
                ResourceLocation resourceLocation = new ResourceLocation(Reference.MOD_ID.toLowerCase(), "config/" + configFileName);
                IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
                FileUtils.copyInputStreamToFile(resource.getInputStream(), targetFile);
                LogHelper.info("Release pre-config file : " + configFileName);
            }
        }
    }

    public static void mergeConfig() {
        mergedConfig = "";
        File configDir = new File(minecraftConfigDirectory, Reference.MOD_ID);
        File[] configFiles = configDir.listFiles();
        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile()) {
                    try {
                        List<String> lines = Files.readAllLines(configFile.toPath(), Charset.forName("UTF-8"));
                        for (String line : lines) {
                            mergedConfig += line;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        mergedConfig = "<root>" + mergedConfig + "</root>";
    }

    public static void parseConfigFiles() {
//      System.out.println(mergedConfig);
        try {
            entityPattern.clear();
            tileEntityPattern.clear();
            tooltipPattern.clear();
            JSHandler.scriptSet.clear();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(mergedConfig)));
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();
            NodeList ooList = root.getElementsByTagName("oo");
            for (int i = 0; i < ooList.getLength(); i++) {
                NodeList entityList = ((Element) ooList.item(i)).getElementsByTagName("entity");
                for (int j = 0; j < entityList.getLength(); j++) {
                    Node node = entityList.item(j);
                    entityPattern.put(Pattern.compile(node.getAttributes().getNamedItem("id").getTextContent()), node);
                }
                NodeList tileEntityList = ((Element) ooList.item(i)).getElementsByTagName("tileentity");
                for (int j = 0; j < tileEntityList.getLength(); j++) {
                    Node node = tileEntityList.item(j);
                    tileEntityPattern.put(Pattern.compile(node.getAttributes().getNamedItem("id").getTextContent()), node);
                }
                NodeList tooltipList = ((Element) ooList.item(i)).getElementsByTagName("tooltip");
                for (int j = 0; j < tooltipList.getLength(); j++) {
                    Node node = tooltipList.item(j);
                    tooltipPattern.put(Pattern.compile(node.getAttributes().getNamedItem("id").getTextContent()), node);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}