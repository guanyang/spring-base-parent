package org.gy.framework.util.file;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.imageio.spi.ServiceRegistry;
import org.gy.framework.util.file.enums.FileFilterTypeEnum;

/**
 * 功能描述：文件过滤器工厂
 *
 * @author gy
 * @version 1.0.0
 */
public class FileFilterFactory {

    private static final Map<FileFilterTypeEnum, FileFilter> FILTER_MAP = new HashMap<>();

    static {
        Iterator<FileFilter> providers = ServiceRegistry.lookupProviders(FileFilter.class);
        while (providers.hasNext()) {
            FileFilter fileFilter = providers.next();
            if (fileFilter.type() == null) {
                throw new IllegalStateException("FileFilter type is required:" + fileFilter);
            }
            FILTER_MAP.put(fileFilter.type(), fileFilter);
        }
    }

    public static FileFilter findFilter(FileFilterTypeEnum filterTypeEnum) {
        return FILTER_MAP.get(filterTypeEnum);
    }
}
