package cat.nyaa.rpgitems.minion;

import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.plugin.Plugin;

public class I18n extends LanguageRepository {
    private static I18n instance;

    private String language = "en_US";

    I18n() {
        super();
        instance = this;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public static String format(String template, Object ... args){
        return instance.getFormatted(template, args);
    }

    @Override
    protected Plugin getPlugin() {
        return MinionExtensionPlugin.plugin;
    }

    @Override
    protected String getLanguage() {
        return language;
    }
}
