package de.skuzzle.inject.conf;

class ContentTypeFactoryImpl implements ContentTypeFactory {

    private final BeanUtil beanUtil;

    ContentTypeFactoryImpl(BeanUtil beanUtil) {
        this.beanUtil = beanUtil;
    }

    @Override
    public TextContentType newJsonContentType() {
        return new JsonContentType(this.beanUtil);
    }

    @Override
    public TextContentType newPropertiesContentType() {
        return new PropertiesContentType();
    }

    @Override
    public TextContentType newStringContentType() {
        return new StringTextContentType();
    }
}
