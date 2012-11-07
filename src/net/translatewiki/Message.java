package net.translatewiki;

public class Message {
    private String mKey;
    private String mLanguage;
    private String mDefinition;
    private String mTranslation;
    private String mRevision;
    
    public String getKey() {
        return mKey;
    }
    
    public String getLang() {
        return mLanguage;
    }
    
    public String getDefinition() {
        return mDefinition;
    }
    
    public String getTranslation() {
        return mTranslation;
    }
    
    public String getRevision() {
        return mRevision;
    }
    
    public Message(String key, String lang, String definition, String translation, String revision) {
        this.mKey = key;
        this.mLanguage = lang;
        this.mDefinition = definition;
        this.mTranslation = translation;
        this.mRevision = revision;
    }
}
