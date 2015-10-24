package de.skuzzle.inject.conf;

import com.google.gson.GsonBuilder;

class ContentTypeFactoryImpl implements ContentTypeFactory {

    private final BeanUtil beanUtil;

    ContentTypeFactoryImpl(BeanUtil beanUtil) {
        this.beanUtil = beanUtil;
    }

    @Override
    public TextContentType newJsonContentType(GsonBuilder builder) {
        return new JsonContentType(this.beanUtil, builder);
    }

    @Override
    public TextContentType newPropertiesContentType() {
        return new PropertiesContentType(this.beanUtil);
    }

    @Override
    public TextContentType newStringContentType() {
        return new StringTextContentType();
    }
}
