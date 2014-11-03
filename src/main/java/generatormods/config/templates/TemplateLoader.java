/* Source code for the Generator Mods (CARuins, Great Walls, Walled Cities) for the game Minecraft
 * Copyright (C) 2011-2014 by Noah Whitman (formivore) <wakatakeru@gmail.com>
 * Copyright (C) 2013-2014 by Olivier Sylvain (GotoLink) <gotolinkminecraft@gmail.com>
 * Copyright (C) 2014 William (B.J.) Snow Orvis (aetherknight) <aetherknight@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package generatormods.config.templates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.Logger;

public class TemplateLoader {
    public final static String BUILDING_DIRECTORY_NAME = "buildings";

    private Logger logger;
    private File modJarFile;
    private File modConfigDirectory;

    public TemplateLoader(Logger logger, File modJarFile, File modConfigDirectory) {
        this.logger = logger;
        this.modJarFile = modJarFile;
        this.modConfigDirectory = modConfigDirectory;
    }

    /**
     * Copy the templates that come with the mod to the modConfigDirectory.
     */
    public void extractTemplatesFromJar(String templateDirName) {
        File templateDir = new File(modConfigDirectory, templateDirName);
        if (templateDir.exists()) {
            logger.info("{} already exists, not extracting templates", templateDir);
            return;
        }
        logger.info("{} does not exist yet, extracting templates", templateDir);
        JarFile jarfile = null;
        try {
            String name = null;
            jarfile = new JarFile(modJarFile);
            Enumeration<JarEntry> jarentries = jarfile.entries();
            while (jarentries.hasMoreElements()) {
                JarEntry jarentry = jarentries.nextElement();
                name = jarentry.getName();
                // Only extract templates in the requested subdirectory:
                if (!name.startsWith("templates/" + templateDirName))
                    continue;
                name = name.substring(("templates/").length());
                File outfile = new File(modConfigDirectory, name);
                logger.debug("Extracting {}", outfile);

                if (jarentry.isDirectory()) {
                    outfile.mkdirs();
                } else {
                    FileOutputStream outstream = null;
                    InputStream jarfilestream = null;
                    try {
                        outfile.getParentFile().mkdirs();
                        outstream = new FileOutputStream(outfile);
                        jarfilestream = jarfile.getInputStream(jarentry);
                        byte[] buf = new byte[2048];
                        int len;
                        while ((len = jarfilestream.read(buf)) >= 0) {
                            outstream.write(buf, 0, len);
                        }
                    } catch (IOException e) {
                        logger.error("Unable to extract " + outfile, e);
                    } finally {
                        if (jarfilestream != null)
                            jarfilestream.close();
                        if (outstream != null)
                            outstream.close();
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } finally {
            if (jarfile != null) {
                try {
                    jarfile.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    public List<TemplateWall> loadWallStyles(String templateGroup) throws Exception {
        return loadWallStylesFromDir(new File(modConfigDirectory, templateGroup));
    }

    public List<TemplateWall> loadWallStylesAndStreets(String templateGroup) throws Exception {
        File templateDir = new File(modConfigDirectory, templateGroup);
        List<TemplateWall> tw = loadWallStylesFromDir(templateDir);
        loadStreets(tw, new File(templateDir, "streets"));
        return tw;
    }

    public static List<TemplateTML> loadTemplatesFromDir(File tmlDirectory, Logger logger) {
        List<TemplateTML> templates = new ArrayList<TemplateTML>();
        for (File f : tmlDirectory.listFiles()) {
            if (getFileType(f.getName()).equals("tml")) {
                try {
                    TemplateTML t = new TemplateTML(f, logger).buildLayout();
                    templates.add(t);
                } catch (Exception e) {
                    // TODO: make this an actual exception type
                    if (e == TemplateTML.ZERO_WEIGHT_EXCEPTION) {
                        logger.warn("Did not load template: {}, weight was zero", f.getName());
                    } else {
                        if (!e.getMessage().startsWith(
                                TemplateRule.BLOCK_NOT_REGISTERED_ERROR_PREFIX)) {
                            logger.error("There was a problem loading " + f.getName(), e);
                        } else
                            logger.error("There was a problem loading " + f.getName() + ": "
                                    + e.getMessage());
                    }
                }
            }
        }
        return templates;
    }

    protected List<TemplateWall> loadWallStylesFromDir(File stylesDirectory) throws Exception {
        if (!stylesDirectory.exists())
            throw new Exception("Could not find directory /" + stylesDirectory.getName()
                    + " in the config folder " + stylesDirectory.getParent() + "!");
        // load buildings
        logger.info("Loading building subfolder in {}/{} ...", stylesDirectory,
                BUILDING_DIRECTORY_NAME);
        Map<String, TemplateTML> buildingTemplates = new HashMap<String, TemplateTML>();
        Iterator<TemplateTML> itr = null;
        try {
            itr =
                    loadTemplatesFromDir(new File(stylesDirectory, BUILDING_DIRECTORY_NAME), logger)
                            .iterator();
        } catch (NullPointerException e) {
            logger.error("No buildings folder for " + stylesDirectory.getName(), e);
        }
        if (itr != null)
            while (itr.hasNext()) {
                TemplateTML t = itr.next();
                buildingTemplates.put(t.name, t);
            }
        // load walls
        logger.info("Loading wall styles from directory {} ...", stylesDirectory);
        List<TemplateWall> styles = new ArrayList<TemplateWall>();
        for (File f : stylesDirectory.listFiles()) {
            if (getFileType(f.getName()).equals("tml")) {
                try {
                    TemplateWall ws = new TemplateWall(f, buildingTemplates, logger);
                    styles.add(ws);
                } catch (Exception e) {
                    if (e == TemplateTML.ZERO_WEIGHT_EXCEPTION) {
                        logger.warn("Did not load template {}, weight was zero.", f.getName());
                    } else {
                        if (!e.getMessage().startsWith(
                                TemplateRule.BLOCK_NOT_REGISTERED_ERROR_PREFIX)) {
                            logger.error("Error loading wall style " + f.getName(), e);
                        } else
                            logger.error("Error loading wall style " + f.getName() + ": "
                                    + e.getMessage());
                    }
                }
            }
        }
        if (styles.size() == 0)
            throw new Exception("Did not find any valid wall styles!");
        return styles;
    }

    public void loadStreets(List<TemplateWall> cityStyles, File streetsDirectory) throws Exception {
        // streets, don't print error if directory DNE
        HashMap<String, TemplateWall> streetTemplateMap = new HashMap<String, TemplateWall>();
        Iterator<TemplateWall> itr;
        try {
            logger.info("Loading streets subfolder in {} ...", streetsDirectory);
            itr = loadWallStylesFromDir(streetsDirectory).iterator();
            while (itr.hasNext()) {
                TemplateWall cs = itr.next();
                streetTemplateMap.put(cs.name, cs);
            }
        } catch (Exception e) {
            logger.error("No street folder for " + streetsDirectory.getName(), e);
        }
        itr = cityStyles.iterator();
        while (itr.hasNext()) {
            TemplateWall cs = itr.next();
            cs.streets = cs.loadChildStyles("street_templates", streetTemplateMap);
            if (cs.streets.size() == 0 && !cs.underground) {
                itr.remove();
                logger.warn("No valid street styles for {}. Disabling this city style.", cs.name);
            }
        }
        if (cityStyles.size() == 0)
            throw new Exception("Did not find any valid city styles that had street styles!");
    }

    private static String getFileType(String s) {
        int mid = s.lastIndexOf(".");
        return s.substring(mid + 1, s.length());
    }

}
