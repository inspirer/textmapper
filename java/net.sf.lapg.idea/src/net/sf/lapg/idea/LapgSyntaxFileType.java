package net.sf.lapg.idea;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: egryaznov
 */
public class LapgSyntaxFileType extends LanguageFileType {

    private static final Icon ICON = IconLoader.getIcon("/lapg/syntax.png");

    public LapgSyntaxFileType() {
        super(new Language("net.sf.lapg.syntax") { });
    }


    @NotNull
    public String getName() {
        return "Lapg Syntax";
    }

    @NotNull
    public String getDescription() {
        return "Grammar file";
    }

    @NotNull
    public String getDefaultExtension() {
        return "s";
    }

    public Icon getIcon() {
        return ICON;
    }
}
