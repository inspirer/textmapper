package net.sf.lapg.idea;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * User: egryaznov
 */
public class LapgFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(new LapgSyntaxFileType(), "s");
    }
}
